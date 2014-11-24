package org.seerc.brokeratcloud.webservice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.seerc.brokeratcloud.messagebroker.WSO2GREGClient;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import com.google.gson.Gson;

@Path("/")
public class GovernanceRegistry {

	WSO2GREGClient greg;

	// the Gson POJO de/serializer
	Gson gson;
	
	public GovernanceRegistry()
	{
		this.greg = new WSO2GREGClient();
		this.gson = new Gson();
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
