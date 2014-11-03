package org.seerc.brokeratcloud.webservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.seerc.brokeratcloud.policycompletenesscompliance.BrokerPolicyException;
import org.seerc.brokeratcloud.policycompletenesscompliance.PolicyCompletenessCompliance;

@Path("/brokerPolicy/validate")
public class BrokerPolicyValidator {

	@POST
	@Path("/")
	public String publishToTopic(String bpContents)
	{
		PolicyCompletenessCompliance pcc = new PolicyCompletenessCompliance();
		InputStream stream = new ByteArrayInputStream(bpContents.getBytes(StandardCharsets.UTF_8));

		try {
			pcc.validateBrokerPolicy(stream);
		} catch (NoSuchMethodException | ClassNotFoundException
				| InstantiationException | IllegalAccessException
				| InvocationTargetException | IOException
				| BrokerPolicyException e) 
		{
			e.printStackTrace();
			
			return e.getMessage();
		}
		
		return "OK";
	}
}
