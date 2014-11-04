package org.seerc.handlers;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;

public class BrokerPolicyValidator extends Handler {

	public void put(RequestContext requestContext) throws RegistryException{
		System.out.println("!!!!!!!!!!!!!!! put() called !!!!!!!!!!!!!!!!!!");
		super.put(requestContext);
	}

}