package org.seerc.brokeratcloud.webservice;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.seerc.brokeratcloud.messagebroker.WSO2MBClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.google.gson.Gson;

@Path("/messageBroker")
public class MessageBroker {

	private WSO2MBClient mb = new WSO2MBClient();
	Gson gson = new Gson();
	
	@GET
	@Path("/getAllTopics")
	public String getAllTopics() throws RegistryException
	{
		List<String> topics = mb.getAllTopics();
		return gson.toJson(topics);
	}
}
