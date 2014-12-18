package org.seerc.brokeratcloud.webservice;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.seerc.brokeratcloud.messagebroker.MessageBrokerTextMessagePublisher;

/*
 * Uses a MessageBrokerTextMessagePublisher to exchange TextMessage instead of ByteMessage.
 */
@Path("/topics/monitoring")
public class MonitoringTopicPublisher {

	@POST
	@Path("/{topicName}/{clientId}")
	public String publishToTopic(@PathParam("topicName") String topicName, @PathParam("clientId") String clientId, String message)
	{
		MessageBrokerTextMessagePublisher publisher = new MessageBrokerTextMessagePublisher(clientId, topicName);
		publisher.publishStringToTopic(message);
		
		return "OK";
	}
}
