package org.seerc.brokeratcloud.webservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.seerc.brokeratcloud.policycompletenesscompliance.CompletenessException;
import org.seerc.brokeratcloud.policycompletenesscompliance.PolicyCompletenessCompliance;
import org.seerc.brokeratcloud.policycompletenesscompliance.WSO2GREGClient;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.google.gson.Gson;

@Path("/")
public class GovernanceRegistry {

	WSO2GREGClient greg;

	// the Gson POJO de/serializer
	Gson gson;
	
	PolicyCompletenessCompliance pcc;
	
	public GovernanceRegistry()
	{
		this.greg = new WSO2GREGClient();
		this.gson = new Gson();
		this.pcc = new PolicyCompletenessCompliance();
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/getServiceDescription")
	public InputStream getServiceDescription(@QueryParam("serviceInstanceUri") String serviceInstanceUri) throws RegistryException, URISyntaxException {
		URI resourceUri = new URI(serviceInstanceUri);
		// resourcePath is URI, should get the SD with this service instance URI
		
		// get all SDs
		Collection sds = (Collection) this.greg.getRemote_registry().get(WSO2GREGClient.getServiceDescriptionsFolder());
		for(int i=0; i<sds.getChildCount(); i++)
		{
			Resource sd = this.greg.getRemote_registry().get(sds.getChildren()[i]);
			try {
				String sdUri = this.pcc.getSDServiceInstanceURI(sd.getContentStream());
				if(sdUri.equals(serviceInstanceUri))
				{	// we found the needed SD, return it
					return sd.getContentStream();
				}
			} catch (IOException | CompletenessException e) {
				System.out.println("Could not get service instance URI of " + sds.getChildren()[i]);
				e.printStackTrace();
			}
		}
		
		throw new RegistryException("Could not find service description with service instance URI: " + serviceInstanceUri);
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/{resourcePath: .*}")
	public InputStream getResource(@PathParam("resourcePath") String resourcePath) throws RegistryException {
		Object gregItem = this.greg.getRemote_registry().get("/" + resourcePath);
		if(gregItem instanceof Collection)
		{	// folder
			Collection collection = (Collection) gregItem;
			ArrayList<String> result = new ArrayList<String>();
			for(int i=0;i<collection.getChildCount();i++)
			{
				// get last path value
				String lastPath = collection.getChildren()[i].substring(collection.getChildren()[i].lastIndexOf("/") + 1);
				Object subResource = this.greg.getRemote_registry().get(collection.getChildren()[i]);
				if(subResource instanceof Collection)
				{	// sub-folder
					result.add(lastPath + "/");
				}
				else
				{	// sub-file
					result.add(lastPath);					
				}
			}
			
			String jsonChildren = gson.toJson(result);
			InputStream stream = null;
			try {
				stream = new ByteArrayInputStream(jsonChildren.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			return stream;
		}
		else 
		{	// file
			Resource resource = (Resource) gregItem;
			return resource.getContentStream();
		}
	}
	
	@PUT
	@Path("/{resourcePath: .*}")
	public void putResource(@PathParam("resourcePath") String resourcePath, String content) throws RegistryException {
		Resource resource = this.greg.getRemote_registry().newResource();
		resource.setMediaType("text/plain");
		resource.setContent(content);
		this.greg.putWithRetryHack("/" + resourcePath, resource);
	}
	
	@DELETE
	@Path("/{resourcePath: .*}")
	public void putResource(@PathParam("resourcePath") String resourcePath) throws RegistryException {
		this.greg.getRemote_registry().delete("/" + resourcePath);
	}
}
