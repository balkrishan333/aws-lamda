package com.serverless;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageThumbnailHandler implements RequestHandler<S3Event, String> {

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        try {
            S3EventNotification.S3EventNotificationRecord record = s3Event.getRecords().get(0);

            String srcBucket = record.getS3().getBucket().getName();
            String srcKey = record.getS3().getObject().getUrlDecodedKey();
            String dstBucket = srcBucket;// + "resized";
            String dstKey = "resized-" + srcKey;

            System.out.println("dstBucket = " + dstBucket);
            System.out.println("dstKey = " + dstKey);

            //Download image from S3
            System.out.println("Download image from S3...");
            AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
            S3Object s3Object = s3Client.getObject(new GetObjectRequest(srcBucket, srcKey));
            InputStream objectData = s3Object.getObjectContent();
            System.out.println("Image downloaded.");

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            System.out.println("Resizing image...");
            //Resize image
            BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            img.createGraphics().drawImage(ImageIO.read(objectData).getScaledInstance(50, 50, Image.SCALE_SMOOTH),0,0,null);
            ImageIO.write(img, "png", os);
            System.out.println("Image resized.");

            // Set Content-Length and Content-Type
            ObjectMetadata meta = new ObjectMetadata();
            System.out.println("os.size() = " + os.size());
            meta.setContentLength(os.size());
            meta.setContentType("image/png");

            //uploading to destination bucket
            System.out.println("Writing to: " + dstBucket + "/" + dstKey);
            try {
                InputStream is = new ByteArrayInputStream(os.toByteArray());
                s3Client.putObject(dstBucket, dstKey, is, meta);
            } catch (AmazonServiceException e) {
                System.err.println("Error writing to bucket...");
                System.err.println(e.getErrorMessage());
                System.exit(1);
            }
            System.out.println("Successfully resized " + srcBucket + "/"
                    + srcKey + " and uploaded to " + dstBucket + "/" + dstKey);
            return "Ok";

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*public static void main(String[] args)  throws Exception {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        img.createGraphics().drawImage(ImageIO.read(new File("D:\\temp\\new-designer.png")).getScaledInstance(200, 200, Image.SCALE_SMOOTH),0,0,null);
        ImageIO.write(img, "png", new File("D:\\temp\\new-designer_thumb.png"));
    }*/
}
