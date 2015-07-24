package org.seerc.handlers.brokerPolicyDeprecation;

import java.io.InputStream;

import org.seerc.brokeratcloud.messagebroker.FusekiClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;

public class BrokerPolicyDeprecator extends Handler {

	// The Fuseki client
	FusekiClient fc;

	public BrokerPolicyDeprecator()
	{
		this.fc = new FusekiClient();
	}
	
	public void delete(RequestContext requestContext) throws RegistryException{
		// Delete it first from fuseki
		System.out.println("Deleting " + requestContext.getResourcePath() + " from Fuseki.");
		InputStream contentStream = requestContext.getRegistry().get(requestContext.getResourcePath().getCompletePath()).getContentStream();
		fc.deleteInputStreamFromFuseki(contentStream);
		
		// Then delete resource from GReg
		super.delete(requestContext);
	}

}