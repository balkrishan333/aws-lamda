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

public class ListLambdaFunctionHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private static final Logger LOG = Logger.getLogger(ListLambdaFunctionHandler.class);

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {
		LOG.info("received: " + input);

		AWSLambda client = AWSLambdaClientBuilder.standard().build();
		ListFunctionsRequest request = new ListFunctionsRequest().withMaxItems(123);
		ListFunctionsResult response = client.listFunctions(request);

		return ApiGatewayResponse.builder()
				.setStatusCode(200)
				.setObjectBody(response.toString())
				.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & serverless"))
				.build();
	}
}
