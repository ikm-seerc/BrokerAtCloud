package org.seerc.brokeratcloud.webservice;

import java.io.InputStream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.seerc.brokeratcloud.messagebroker.WSO2GREGClient;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

@Path("/")
public class GovernanceRegistry {

	WSO2GREGClient greg;
	
	public GovernanceRegistry()
	{
		this.greg = new WSO2GREGClient();
	}
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/{resourcePath: .*}")
	public InputStream getResource(@PathParam("resourcePath") String resourcePath) throws RegistryException {
		Resource resource = this.greg.getRemote_registry().get("/" + resourcePath);
		return resource.getContentStream();
	}
	
	@PUT
	@Path("/{resourcePath: .*}")
	public void putResource(@PathParam("resourcePath") String resourcePath, String content) throws RegistryException {
		Resource resource = this.greg.getRemote_registry().newResource();
		resource.setMediaType("text/plain");
		resource.setContent(content);
		this.greg.getRemote_registry().put("/" + resourcePath, resource);
	}
	
	@DELETE
	@Path("/{resourcePath: .*}")
	public void putResource(@PathParam("resourcePath") String resourcePath) throws RegistryException {
		this.greg.getRemote_registry().delete("/" + resourcePath);
	}
	
}
