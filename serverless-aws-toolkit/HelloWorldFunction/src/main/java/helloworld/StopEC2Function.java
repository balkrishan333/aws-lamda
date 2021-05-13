package helloworld;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StopEC2Function implements RequestHandler<Object, String> {

    private static final String BUCKET = "routing-lambda";
    private static final String KEY = "instance-ids";
//    30 15 ? * MON-FRI *

    @Override
    public String handleRequest(Object input, Context context) {

        List<String> exceptionInstanceIds = getExceptionInstanceIds();
        System.out.println("exceptionInstanceIds = " + exceptionInstanceIds);

        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
        DescribeInstancesRequest request = new DescribeInstancesRequest();

        DescribeInstancesResult instancesResult = ec2.describeInstances(request);
        List<String> stoppedInstanceIds = new ArrayList<>();

        for (Reservation reservation : instancesResult.getReservations()) {
            reservation.getInstances().forEach(instance -> {
                int stateCode = instance.getState().getCode();
                String name =  null;
                String instanceId = instance.getInstanceId();

                if (!exceptionInstanceIds.contains(instanceId)) {

                    if (stateCode == 16) {
                        List<Tag> tags = instance.getTags();
                        for (Tag tag : tags) {
                            if (tag.getKey().equalsIgnoreCase("Name")) {
                                name = tag.getValue();
                            }
                        }

                        StringBuilder builder = new StringBuilder("Stopping instance: ");
                        if (!StringUtils.isNullOrEmpty(name)) {
                            builder.append("Name : ").append(name).append(" ");
                        }
                        builder.append("Instance Id: ").append(instanceId);
                        System.out.println(builder);

                        StopInstancesRequest stopInstancesRequest = new StopInstancesRequest().withInstanceIds(instance.getInstanceId());
                        StopInstancesResult stopInstancesResult = ec2.stopInstances(stopInstancesRequest);
                        String requestId = stopInstancesResult.getSdkResponseMetadata().getRequestId();

                        System.out.println("requestId = " + requestId);
                        stoppedInstanceIds.add(instanceId);
                    }
                }
            });
        }
        return String.join(" , ", stoppedInstanceIds);
    }

    private List<String> getExceptionInstanceIds() {
        S3Object s3Object;
        AmazonS3 s3Client;

        System.out.println("Reading from S3...");
        try {
            s3Client = AmazonS3ClientBuilder.defaultClient();
            s3Object = s3Client.getObject(new GetObjectRequest(BUCKET, KEY));
        } catch (AmazonS3Exception s3e) {
            s3e.printStackTrace();
            throw new RuntimeException(s3e);
        }
        InputStream objectData = s3Object.getObjectContent();
        InputStreamReader streamReader = new InputStreamReader(objectData);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        String line;

        List<String> instanceIds = new ArrayList<>();
        try {
             line = bufferedReader.readLine();
             while (line != null) {
                 instanceIds.add(line);
                 line = bufferedReader.readLine();
             }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instanceIds;
    }
}
