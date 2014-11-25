package org.seerc.brokeratcloud.messagebroker;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class WSO2GREGClient {
	// GREG repo configuration properties
	protected Properties wso2_greg_properties = new Properties();
	protected static String wso2_greg_properties_path = "/properties/wso2_greg.properties";
	protected static String gregRepoURL;
	protected static String gregRepoUsername;
	protected static String gregRepoPassword;

	// Demo resource paths
	protected static String brokerPolicyPath;
	public static String getBrokerPolicyPath() {
		return brokerPolicyPath;
	}

	protected static String brokerPoliciesFolder;
	public static String getBrokerPoliciesFolder() {
		return brokerPoliciesFolder;
	}
	
	protected static String serviceDescriptionPath;
	public static String getServiceDescriptionPath() {
		return serviceDescriptionPath;
	}

	protected static String serviceDescriptionsFolder;
	public static String getServiceDescriptionsFolder() {
		return serviceDescriptionsFolder;
	}
	
	// The Remote Registry instance
	protected RemoteRegistry remote_registry;
	
	public WSO2GREGClient()
	{
		// load the WSO2 GREG related properties
		this.readWSO2GREGProperties();

		// Setup the client keystore with the packed key.
		// This is needed because the calls to GREG are made using https://
		this.setupClientKeystore();
		
		// Set the various system properties needed by GREG - uncomment this if we need write support 
		System.setProperty("carbon.repo.write.mode", "true");
		
		// Get the Remote Registry instance
		this.getRemoteRegistryInstance();
	}
	
	protected void readWSO2GREGProperties() 
	{
		try {
			wso2_greg_properties.load(this.getClass().getResourceAsStream(wso2_greg_properties_path));
			gregRepoURL = wso2_greg_properties.getProperty("gregRepoURL");
			gregRepoUsername = wso2_greg_properties.getProperty("gregRepoUsername");
			gregRepoPassword = wso2_greg_properties.getProperty("gregRepoPassword");
			brokerPolicyPath = wso2_greg_properties.getProperty("brokerPolicyPath");
			brokerPoliciesFolder = wso2_greg_properties.getProperty("brokerPoliciesFolder");
			serviceDescriptionPath = wso2_greg_properties.getProperty("serviceDescriptionPath");
			serviceDescriptionsFolder = wso2_greg_properties.getProperty("serviceDescriptionsFolder");
		} catch (IOException e) {
			System.err.println("Could not load properties file from: " + wso2_greg_properties_path);
			e.printStackTrace();
		}
	}

	private void setupClientKeystore() {
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			InputStream resourceAsStream = this.getClass().getResourceAsStream("/keystore/client-truststore.jks");
			ks.load(resourceAsStream, "wso2carbon".toCharArray());
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(ks);
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, "wso2carbon".toCharArray());
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLContext.setDefault(ctx); 
		} catch (KeyStoreException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}

	private void getRemoteRegistryInstance() {
		try {
			remote_registry = new RemoteRegistry(new URL(gregRepoURL), gregRepoUsername, gregRepoPassword);
		} catch (MalformedURLException e) {
			System.err.println("GREG repo URL is incorrect: " + gregRepoURL);
			e.printStackTrace();
		} catch (RegistryException e) {
			System.err.println("Could not connect to registry at " + gregRepoURL + " as user " + gregRepoUsername);
			e.printStackTrace();
		}
	}	

	public RemoteRegistry getRemote_registry() {
		return remote_registry;
	}
	
	/*
	 * This method is used in order to overcome a WSO2 bug where PUT in registry
	 * fails sometimes with no obvious reason.
	 * This method will retry 10 times with 1 second gap between them.  
	 */
	public void putWithRetryHack(String pathToPutSiUri, Resource sdResource)
			throws RegistryException {
		boolean success = false;
		int retries = 1;
		while(!success && retries < 10)
		{
			try
			{
				this.getRemote_registry().put(pathToPutSiUri, sdResource);
				success = true;
				break;
			}
			catch (RegistryException re)
			{
				System.out.println("Could not put resource at " + pathToPutSiUri + ". Retry #" + retries + "...");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			retries++;
		}

		if(!success)
		{
			throw new RegistryException("Failed to put resource at " + pathToPutSiUri + "!");
		}
		
		System.out.println("Successfully put resource at " + pathToPutSiUri + ".");
	}
}
