package org.seerc.brokeratcloud.messagebroker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.wso2.andes.util.FileUtils;

public class MessageBrokerStressTest {

	public static void main(String[] args) throws Exception {
		new MessageBrokerStressTest();
	}
	
	public MessageBrokerStressTest()
	{
		try {
			this.test2();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private void test1() throws Exception
	{
		for (int i = 1; i <= 1000; i++) {
			System.out.println("Stress test #" + (i));

			MessageBrokerPublisher mbp = new MessageBrokerPublisher("publisher1", "SD");
			MessageBrokerSubscriber mbs = new MessageBrokerSubscriber("subscriber1", "SD", new BytesMessageListener());
			
			mbs.subscribeToTopic();
			mbp.publishBytesMessageFromFileToTopic("files/SAP_HANA_Cloud_Apps_SD_test.ttl");

			Thread.sleep(1000);

			//mbs.releaseResources();
		}
	}

	private void test2() throws Exception
	{
		for (int i = 1; i <= 1000; i++) {
			System.out.println("Stress test #" + (i));

			doPut();

			doPost();

			Thread.sleep(10000);
		}
	}
	
	private void test3() throws Exception
	{
		for (int i = 1; i <= 30; i++) {
			doPut();
		}
		
		for (int i = 1; i <= 1000; i++) {
			System.out.println("Stress test #" + (i));

			doPost();

			Thread.sleep(1000);
		}

	}
	
	private void test4() throws Exception
	{
		MessageBrokerPublisher mbp = new MessageBrokerPublisher("publisher1", "SD");
		MessageBrokerSubscriber mbs = new MessageBrokerSubscriber("subscriber1", "SD", new BytesMessageListener());

		for (int i = 1; i <= 1000; i++) {
			System.out.println("Stress test #" + (i));

			mbs.subscribeToTopic();
			mbp.publishBytesMessageFromFileToTopic("files/SAP_HANA_Cloud_Apps_SD_test.ttl");

			Thread.sleep(10000);
		}

	}
	
	private void doPut() throws Exception
	{
		URL url = new URL(
				"http://localhost:8080/org.seerc.brokeratcloud.webservice/rest/subscriptions/evaluation/SD/client1?wsCallbackEndpoint=http://localhost:8080/org.seerc.brokeratcloud.webservice/rest/subscriptions/evaluation/receive");
		HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
		httpCon.setDoOutput(true);
		httpCon.setRequestMethod("PUT");
		OutputStreamWriter out = new OutputStreamWriter(
				httpCon.getOutputStream());
		out.write("Resource content");
		out.close();
		httpCon.getInputStream();
		httpCon.disconnect();
	}

	private void doPost() throws Exception
	{
		URL targetUrl = new URL(
				"http://localhost:8080/org.seerc.brokeratcloud.webservice/rest/topics/evaluation/SD/client1");

		HttpURLConnection httpConnection = (HttpURLConnection) targetUrl
				.openConnection();
		httpConnection.setDoOutput(true);
		httpConnection.setRequestMethod("POST");

		String input = FileUtils
				.readFileAsString("files/SAP_HANA_Cloud_Apps_SD_test.ttl");

		OutputStream outputStream = httpConnection.getOutputStream();
		outputStream.write(input.getBytes());
		outputStream.flush();

		if (httpConnection.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ httpConnection.getResponseCode());
		}

		BufferedReader responseBuffer = new BufferedReader(
				new InputStreamReader((httpConnection.getInputStream())));

		String output;
		System.out.println("Output from Server:\n");
		while ((output = responseBuffer.readLine()) != null) {
			System.out.println(output);
		}

		httpConnection.disconnect();
	}

}
