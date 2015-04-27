package org.seerc.brokeratcloud.webservice;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.apache.commons.io.FileUtils;
import org.seerc.brokeratcloud.messagebroker.SDEvaluationListener;
import org.seerc.brokeratcloud.messagebroker.WSO2GREGClient;
import org.seerc.brokeratcloud.policycompletenesscompliance.BrokerPolicyException;
import org.seerc.brokeratcloud.policycompletenesscompliance.PolicyCompletenessCompliance;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

@Path("/serviceDescription")
public class ServiceDescriptionValidator {

	String sdContents = "";
	
	SDEvaluationListener sdEval;
	
	public static void main(String[] args) {
		ServiceDescriptionValidator sdv = new ServiceDescriptionValidator();
		String sdFile = "../PolicyCompletenessCompliance/Ontologies/Current/CAS-AddressApp1.ttl";
		String sdContents = "";
		try {
			sdContents = FileUtils.readFileToString(new File(sdFile), "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sdv.validateSD(sdContents);
	}
	
	public ServiceDescriptionValidator()
	{
		this.sdEval = new SDEvaluationListener();
	}
	
	@POST
	@Path("/validate")
	public String validateSD(String sdContents)
	{
		try {
			PolicyCompletenessCompliance pcc = new PolicyCompletenessCompliance();
			InputStream stream = new ByteArrayInputStream(sdContents.getBytes("UTF-8"));

			// get the BrokerPolicy for this SD by looking in GReg BPs folder
			InputStream bpInputStream = sdEval.getBpForSd(stream);
			
			// reuse stream
			stream.reset();

			pcc.getBrokerPolicy(bpInputStream);
			
			// perform evaluation
			sdEval.evaluateCompletenessCompliance(stream);
		} catch (Exception e) {
			e.printStackTrace();
			
			return e.getMessage();
		}
		
		return "OK";
	}

}
