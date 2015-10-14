package org.seerc.brokeratcloud.messagebroker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.wso2.andes.util.FileUtils;

public class HttpPostClient {
	public static String postMessageToEndpoint(String endpoint, String msg) throws IOException
	{
		URL targetUrl = new URL(endpoint);

		HttpURLConnection httpConnection = (HttpURLConnection) targetUrl.openConnection();
		httpConnection.setDoOutput(true);
		httpConnection.setRequestMethod("POST");

		OutputStream outputStream = httpConnection.getOutputStream();
		outputStream.write(msg.getBytes());
		outputStream.flush();

		if (httpConnection.getResponseCode() != 200) {
			return "Failed : HTTP error code : " + httpConnection.getResponseCode();
		}

		BufferedReader responseBuffer = new BufferedReader(
				new InputStreamReader((httpConnection.getInputStream())));

		String output;
		String responseContent = "";
		System.out.println("Output from Endpoint:\n");
		while ((output = responseBuffer.readLine()) != null) {
			System.out.println(output);
			responseContent += output;
		}

		httpConnection.disconnect();
		
		return responseContent;
	}
	
	public static void main(String[] args) {
		System.out.println(new Date().getTime());
		testMultipleRequests(5);
	}

	private static void testMultipleRequests(int numberOfRequests) {
		for(int i=0;i<numberOfRequests;i++)
		{
			testSingleRequest();
		}
	}
	
	private static void testSingleRequest() {
		try {
			HttpPostClient.postMessageToEndpoint("http://213.249.38.66:3335/org.seerc.brokeratcloud.webservice/rest/topics/evaluation/SD/seerc", 
					FileUtils.readFileAsString("../PolicyCompletenessCompliance/Ontologies/Current/CAS-AddressApp1.ttl"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
