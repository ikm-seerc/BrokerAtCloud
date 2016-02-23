package org.seerc.handlers.service;

import java.io.IOException;
import java.io.InputStream;

import org.seerc.brokeratcloud.policycompletenesscompliance.FusekiClient;
import org.seerc.brokeratcloud.messagebroker.ServiceLifecyclePublisher;
import org.seerc.brokeratcloud.messagebroker.WSO2GREGEvaluator;
import org.seerc.brokeratcloud.policycompletenesscompliance.CompletenessException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;

public class ServiceDeprecation extends Handler {

	FusekiClient fc;
	
	ServiceLifecyclePublisher slp;
	
	WSO2GREGEvaluator gregEvaluator;

	public ServiceDeprecation()
	{
		fc = new FusekiClient();
		
		slp = new ServiceLifecyclePublisher();
		
		gregEvaluator = new WSO2GREGEvaluator();
	}
	
	public void delete(RequestContext requestContext) throws RegistryException{
		// Delete it first from fuseki
		System.out.println("Deleting " + requestContext.getResourcePath() + " from Fuseki.");
		InputStream contentStream = requestContext.getRegistry().get(requestContext.getResourcePath().getCompletePath()).getContentStream();
		fc.deleteInputStreamFromFuseki(contentStream);
		
		// send the service deprecation event
		try {
			// reuse stream
			contentStream.reset();
			
			slp.serviceDeprecated(gregEvaluator.getPcc().getSDServiceInstanceURI(contentStream));
		} catch (IOException | CompletenessException e) {
			e.printStackTrace();
		}
		
		// Then delete resource from GReg
		super.delete(requestContext);
	}

}