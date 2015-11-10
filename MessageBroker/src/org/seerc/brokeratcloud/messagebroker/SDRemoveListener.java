package org.seerc.brokeratcloud.messagebroker;

public class SDRemoveListener extends AbstractSDLifecycleListener {

	@Override
	protected void performLifecycleEventForService(String serviceID) {
		slp.serviceRemoved(serviceID);
	}
}
