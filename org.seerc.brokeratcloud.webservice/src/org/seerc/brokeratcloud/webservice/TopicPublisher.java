package org.seerc.brokeratcloud.webservice;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.seerc.brokeratcloud.messagebroker.MessageBrokerStringPublisher;

@Path("/topics/evaluation")
public class TopicPublisher {

	@POST
	@Path("/{topicName}/{clientId}")
	public String publishToTopic(@PathParam("topicName") String topicName, @PathParam("clientId") String clientId, String message)
	{
		MessageBrokerStringPublisher publisher = new MessageBrokerStringPublisher(clientId, topicName);
		publisher.publishStringToTopic(message);
		
		return "OK";
	}
}
