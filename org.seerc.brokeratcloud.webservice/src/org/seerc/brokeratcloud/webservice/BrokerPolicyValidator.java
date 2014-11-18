package org.seerc.brokeratcloud.webservice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.seerc.brokeratcloud.policycompletenesscompliance.PolicyCompletenessCompliance;

@Path("/brokerPolicy/validate")
public class BrokerPolicyValidator {

	@POST
	@Path("/")
	public String publishToTopic(String bpContents)
	{
		try {
			PolicyCompletenessCompliance pcc = new PolicyCompletenessCompliance();
			InputStream stream = new ByteArrayInputStream(bpContents.getBytes("UTF-8"));

			pcc.validateBrokerPolicy(stream);
		} catch (Exception e) {
			e.printStackTrace();
			
			return e.getMessage();
		}
		
		return "OK";
	}
}
