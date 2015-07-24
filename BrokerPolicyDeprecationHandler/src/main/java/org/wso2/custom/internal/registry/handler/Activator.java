package org.wso2.custom.internal.registry.handler;

import javax.xml.stream.XMLStreamException;

import org.wso2.carbon.registry.core.jdbc.handlers.HandlerManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.HandlerManager;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.Filter;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.apache.axiom.om.util.AXIOMUtil;
import org.seerc.handlers.brokerPolicyDeprecation.BrokerPolicyDeprecator;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.URLMatcher;

public class Activator implements BundleActivator {
	private BrokerPolicyDeprecator handler;
	private URLMatcher filter;
	private RegistryContext registryContext;
	private HandlerManager handlerManager;

	private String[] applyingFilters;

	public void start(final BundleContext bundleContext) throws Exception {
		registryContext = RegistryContext.getBaseInstance();
		handlerManager = registryContext.getHandlerManager();
		handler = new BrokerPolicyDeprecator();
		filter = new URLMatcher();

		String filterPropertyData = "";
		filter.setGetTagsPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setRateResourcePattern(filterPropertyData);
		filterPropertyData = "";
		filter.setDumpPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setMovePattern(filterPropertyData);
		filterPropertyData = "";
		filter.setRestoreVersionPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setGetAssociationsPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setRenamePattern(filterPropertyData);
		filterPropertyData = "";
		filter.setGetCommentsPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setPutPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setEditCommentPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setAddAssociationPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setGetVersionsPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setRestorePattern(filterPropertyData);
		filterPropertyData = "";
		filter.setGetAllAssociationsPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setCreateLinkPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setCopyPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setPutChildPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setGetPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setGetAverageRatingPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setResourceExistsPattern(filterPropertyData);
		filterPropertyData = "/brokerAtCloud/brokerPolicies/.*";
		filter.setDeletePattern(filterPropertyData);
		filterPropertyData = "";
		filter.setRemoveTagPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setInvokeAspectPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setApplyTagPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setRemoveAssociationPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setImportPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setRemoveLinkPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setGetRatingPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setRemoveCommentPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setImportChildPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setAddCommentPattern(filterPropertyData);
		filterPropertyData = "";
		filter.setCreateVersionPattern(filterPropertyData);

		applyingFilters = new String[] { Filter.DELETE };
		handlerManager.addHandlerWithPriority(applyingFilters, filter, handler);
	}

	public void stop(BundleContext context) throws Exception {
		handlerManager.removeHandler(applyingFilters, filter, handler);
	}

	protected static OMElement getElement(String xmlString)
			throws XMLStreamException {
		return AXIOMUtil.stringToOM(xmlString);
	}

}