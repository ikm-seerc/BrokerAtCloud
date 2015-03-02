package org.seerc.brokeratcloud.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.seerc.brokeratcloud.messagebroker.MessageBrokerSubscriber;
import org.wso2.carbon.event.client.broker.BrokerClientException;

@Path("/subscriptions/durable")
public class DurableTopicSubscriber {

	@PUT
	@Path("/{topicName}/{clientId}")
	public void subscribeToTopic(@PathParam("topicName") final String topicName, @PathParam("clientId") String clientId, @QueryParam("wsCallbackEndpoint") final String wsCallbackEndpoint) throws BrokerClientException
	{
		// Normal MessageBrokerSubscriber which delegates messages in XML format to
		// WS callback subscribers
		MessageBrokerSubscriber mbs = new MessageBrokerSubscriber(clientId, topicName, new MessageListener() {
			
			@Override
			public void onMessage(Message message) {
				try {
					System.out.println("TopicSubscriber received the message with ID==> "
							+ message.getJMSMessageID());

					BytesMessage byteMessage = (BytesMessage) message; 
					String msg = "";
					int i;
					while((i = byteMessage.readInt()) != -1)
					{
						msg += (char)i;
					}
					
					postMessageToWsCallbackEndpoint(wsCallbackEndpoint, msg);
				} catch (JMSException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, true);
		mbs.subscribeToTopic();
	}
	
	@PUT
	@Path("/{topicName}/{clientId}/unsubscribe")
	public void unsubscribeFromTopic(@PathParam("topicName") final String topicName, @PathParam("clientId") String clientId) throws BrokerClientException
	{
		// Normal MessageBrokerSubscriber which delegates messages in XML format to
		// WS callback subscribers
		MessageBrokerSubscriber mbs = new MessageBrokerSubscriber(clientId, topicName, new MessageListener() {
			
			@Override
			public void onMessage(Message message) {
				// empty message listener
			}
		}, false);
		mbs.unsubscribe();
	}
	
	protected void postMessageToWsCallbackEndpoint(String wsCallbackEndpoint, String msg) throws IOException
	{
		URL targetUrl = new URL(wsCallbackEndpoint);

		HttpURLConnection httpConnection = (HttpURLConnection) targetUrl.openConnection();
		httpConnection.setDoOutput(true);
		httpConnection.setRequestMethod("POST");

		OutputStream outputStream = httpConnection.getOutputStream();
		outputStream.write(msg.getBytes());
		outputStream.flush();

		if (httpConnection.getResponseCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
					+ httpConnection.getResponseCode());
		}

		BufferedReader responseBuffer = new BufferedReader(
				new InputStreamReader((httpConnection.getInputStream())));

		String output;
		System.out.println("Output from wsCallbackEndpoint:\n");
		while ((output = responseBuffer.readLine()) != null) {
			System.out.println(output);
		}

		httpConnection.disconnect();
	}

	@POST
	@Path("/receive")
	public String receive(String message)
	{
		System.out.println("Got the WS callback message ==> " + message);
		
		return "OK";
	}
	
	public static void main(String[] args) {
		DurableTopicSubscriber dts = new DurableTopicSubscriber();
		try {
			dts.subscribeToTopic("serviceUpdatedTopic", "durableSubsciber", "http://requestb.in/12q65i01");
			Thread.sleep(60000);
			dts.unsubscribeFromTopic("serviceUpdatedTopic", "durableSubsciber");
		} catch (BrokerClientException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
