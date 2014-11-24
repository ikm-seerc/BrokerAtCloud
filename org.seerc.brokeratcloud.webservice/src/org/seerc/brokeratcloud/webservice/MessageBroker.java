package org.seerc.brokeratcloud.webservice;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Topic;
import javax.naming.Binding;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/messageBroker")
public class MessageBroker {

	@GET
	@Path("/getAllTopics")
	public String getAllTopics()
	{
		/*try {
			List<Topic> topics = scanJndiForQueues("");
			int i=0;
		} catch (NamingException e) {
			e.printStackTrace();
		}*/
		return "Hey";
	}
}
