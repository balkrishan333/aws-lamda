package com.serverless;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.ListFunctionsRequest;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;

public class AccessEnvironmentVariableHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {
    private static final Logger LOG = Logger.getLogger(ListLambdaFunctionHandler.class);

    @Override
    public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
        LOG.info("received: " + input);

        return ApiGatewayResponse.builder()
                .setStatusCode(200)
                .setObjectBody(System.getenv("ENV_VAR"))
                .setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
                .build();
    }
}
