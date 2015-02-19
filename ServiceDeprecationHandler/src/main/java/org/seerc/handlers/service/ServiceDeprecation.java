package org.seerc.handlers.service;

import org.seerc.brokeratcloud.messagebroker.FusekiClient;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;

public class ServiceDeprecation extends Handler {

	FusekiClient fc;
	
	public ServiceDeprecation()
	{
		fc = new FusekiClient();
	}
	
	public void delete(RequestContext requestContext) throws RegistryException{
		// Delete it first from fuseki
		System.out.println("Deleting " + requestContext.getResourcePath() + " from Fuseki.");
		fc.deleteInputStreamFromFuseki(requestContext.getRegistry().get(requestContext.getResourcePath().getCompletePath()).getContentStream());
		
		// Then delete resource from GReg
		super.delete(requestContext);
	}

}