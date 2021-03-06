package org.seerc.brokeratcloud.webservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.seerc.brokeratcloud.policycompletenesscompliance.BrokerPolicyException;
import org.seerc.brokeratcloud.policycompletenesscompliance.PolicyCompletenessCompliance;
import org.seerc.brokeratcloud.policycompletenesscompliance.WSO2GREGClient;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

@Path("/brokerPolicy")
public class BrokerPolicyValidator {

	WSO2GREGClient greg;
	
	public BrokerPolicyValidator()
	{
		this.greg = new WSO2GREGClient();
	}
	
	@POST
	@Path("/validate")
	public String validateBP(String bpContents)
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
	
	@PUT
	@Path("/upload")
	public String uploadBP(String bpContents) throws RegistryException, URISyntaxException, IOException, BrokerPolicyException
	{
		PolicyCompletenessCompliance pcc = new PolicyCompletenessCompliance();
		String validationResult = this.validateBP(bpContents);
		if(validationResult.equals("OK"))
		{
			Resource resourceForName = this.greg.getRemote_registry().newResource();
			resourceForName.setMediaType("text/plain");
			resourceForName.setContent(bpContents);
			String bpInstanceUri = pcc.getBPInstanceUri(resourceForName.getContentStream());

			//resource.getContentStream().reset();
			Resource resource = this.greg.getRemote_registry().newResource();
			resource.setMediaType("text/plain");
			resource.setContent(bpContents);
			String pathToPutBP = WSO2GREGClient.getBrokerPoliciesFolder() + WSO2GREGClient.createNameFromUri(new URI(bpInstanceUri)) + ".ttl";
			if(greg.getRemote_registry().resourceExists(pathToPutBP))
			{	// BP exists, throw exception
				throw new BrokerPolicyException("A Broker Policy with instance URI " + bpInstanceUri + " already exists.");
			}
			else
			{
				this.greg.putWithRetryHack(pathToPutBP, resource);
			}

			return "OK";
		}
		else 
		{
			return validationResult;
		}
	}
	
	/*@PUT
	@Path("/update")
	public String updateBP(String bpContents) throws RegistryException, URISyntaxException, IOException, BrokerPolicyException
	{
		PolicyCompletenessCompliance pcc = new PolicyCompletenessCompliance();
		String validationResult = this.validateBP(bpContents);
		if(validationResult.equals("OK"))
		{
			Resource resourceForName = this.greg.getRemote_registry().newResource();
			resourceForName.setMediaType("text/plain");
			resourceForName.setContent(bpContents);
			String bpInstanceUri = pcc.getBPInstanceUri(resourceForName.getContentStream());

			//resource.getContentStream().reset();
			Resource resource = this.greg.getRemote_registry().newResource();
			resource.setMediaType("text/plain");
			resource.setContent(bpContents);
			String pathToPutBP = WSO2GREGClient.getBrokerPoliciesFolder() + WSO2GREGClient.createNameFromUri(new URI(bpInstanceUri)) + ".ttl";
			if(!greg.getRemote_registry().resourceExists(pathToPutBP))
			{	// BP exists, throw exception
				throw new BrokerPolicyException("A Broker Policy with instance URI " + bpInstanceUri + " could not be found.");
			}
			else
			{
				this.greg.putWithRetryHack(pathToPutBP, resource);
			}

			return "OK";
		}
		else 
		{
			return validationResult;
		}
	}*/
	
}
