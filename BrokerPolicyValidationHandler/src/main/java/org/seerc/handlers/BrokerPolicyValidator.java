package org.seerc.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.seerc.brokeratcloud.policycompletenesscompliance.BrokerPolicyException;
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
		} catch (NoSuchMethodException | ClassNotFoundException
				| InstantiationException | IllegalAccessException
				| InvocationTargetException | IOException
				| BrokerPolicyException e)
		{
			e.printStackTrace();
			
			throw new RegistryException(e.getMessage(), e);			
		}
		super.put(requestContext);
	}

}