package org.seerc.brokeratcloud.webservice;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.seerc.brokeratcloud.messagebroker.MessageBrokerStringPublisher;

@Path("/topics")
public class TopicPublisher {

	@POST
	@Path("/evaluation/{topicName}/{clientId}")
	public String publishToTopic(@PathParam("topicName") String topicName, @PathParam("clientId") String clientId, String message)
	{
		MessageBrokerStringPublisher publisher = new MessageBrokerStringPublisher(clientId, topicName);
		publisher.publishStringToTopic(message);
		
		return "OK";
	}

	@POST
	@Path("/recommendations/{topicName}/{clientId}")
	public String publishToRecommendationTopic(@PathParam("topicName") String topicName, @PathParam("clientId") String clientId, String message)
	{
		return this.publishToTopic(topicName, clientId, message);
	}
}
