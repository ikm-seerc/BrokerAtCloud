package org.seerc.brokeratcloud.messagebroker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
}
