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
import javax.jms.TextMessage;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.seerc.brokeratcloud.messagebroker.MessageBrokerSubscriber;
import org.wso2.carbon.event.client.broker.BrokerClientException;

@Path("/subscriptions/monitoring")
public class MonitoringTopicSubscriber extends TopicSubscriber {

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

					TextMessage textMessage = (TextMessage) message; 
					String msg = textMessage.getText();
					
					postMessageToWsCallbackEndpoint(wsCallbackEndpoint, msg);
				} catch (JMSException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, false);
		mbs.subscribeToTopic();
	}

	@POST
	@Path("/receive")
	public String receive(String message)
	{
		System.out.println("Got the WS callback message ==> " + message);
		
		return "OK";
	}
}
