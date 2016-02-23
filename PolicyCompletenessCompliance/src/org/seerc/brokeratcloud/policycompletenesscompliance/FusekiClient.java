package org.seerc.brokeratcloud.policycompletenesscompliance;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.h2.util.StatementBuilder;

import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

public class FusekiClient {

	String datasetURL;
	
		Properties fuseki_properties = new Properties();
		public FusekiClient(){

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
			fc.addFileToFuseki(new File("files/CAS-AddressApp.ttl"));
			fc.deleteFileFromFuseki(new File("files/CAS-AddressApp.ttl"));
			fc.deleteFileFromFuseki(new File("files/SAP_HANA_Cloud_Apps_SD_test.ttl"));
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
	
	public void deleteFileFromFuseki(File file) throws IOException
	{
		this.deleteInputStreamFromFuseki(FileUtils.openInputStream(file));
	}

	public void deleteInputStreamFromFuseki(InputStream stream) {
		DatasetAccessor dataAccessor = DatasetAccessorFactory.createHTTP(datasetURL + "/data");

		// current model in Fuseki
		OntModel ontmodel = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		ontmodel.add(dataAccessor.getModel());
		
		// model to delete
		OntModel modelToDelete = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM);
		modelToDelete.read(stream, null, "TTL");
		
		// delete from Fuseki model
		ontmodel.remove(modelToDelete);
		
		// replace Fusseki model
		dataAccessor.putModel(ontmodel);
	}
	
	public Model getModel()
	{
		DatasetAccessor dataAccessor = DatasetAccessorFactory.createHTTP(datasetURL + "/data");
		
		return dataAccessor.getModel();
	}

}
