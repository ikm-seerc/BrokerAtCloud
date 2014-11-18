package org.seerc.handlers;

import java.io.InputStream;

import org.seerc.brokeratcloud.policycompletenesscompliance.PolicyCompletenessCompliance;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

public class BrokerPolicyValidator extends Handler {

	public void put(RequestContext requestContext) throws RegistryException{
		InputStream resourceIS = requestContext.getResource().getContentStream();
		PolicyCompletenessCompliance pcc = new PolicyCompletenessCompliance();
		try {
			pcc.validateBrokerPolicy(resourceIS);
		} catch (Exception e)
		{
			e.printStackTrace();
			
			throw new RegistryException(e.getMessage(), e);			
		}
		super.put(requestContext);
	}

}