package org.seerc.brokeratcloud.messagebroker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class FusekiClient {

	String datasetURL;
	
	public FusekiClient(){
		Properties fuseki_properties = new Properties();

		try {
			fuseki_properties.load(this.getClass().getResourceAsStream("/properties/fuseki.properties"));
			datasetURL = fuseki_properties.getProperty("datasetURL");
		} catch (IOException e) {
			System.err.println("Could not load properties file from: /properties/fuseki.properties");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		FusekiClient fc = new FusekiClient();
		try {
			fc.addFileToFuseki(new File("files/SAP_HANA_Cloud_Apps_SD_test.ttl"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addInputStreamToFuseki(InputStream stream)
	{
		DatasetAccessor dataAccessor = DatasetAccessorFactory.createHTTP(datasetURL + "/data");

		OntModel ontmodel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		ontmodel.read(stream, null, "TTL");

		dataAccessor.add(ontmodel);
	}

	public void addFileToFuseki(File file) throws IOException
	{
		this.addInputStreamToFuseki(FileUtils.openInputStream(file));
	}

}
