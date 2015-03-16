package org.seerc.brokeratcloud.policycompletenesscompliance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class EvaluationStressTest {

	String[] elementsThatMakeTriplesIgnored = 
		{
			"@rdfs:label",
			"@http://www.linked-usdl.org/ns/usdl-core/cloud-broker#higherIsBetter",
			"@http://www.broker-cloud.eu/service-descriptions/CAS/broker#measuredBy",
			"@http://purl.org/goodrelations/v1#lesser",
			"@http://purl.org/goodrelations/v1#greater",
			"@http://www.w3.org/2004/02/skos/core#inScheme",
			"@http://www.w3.org/2004/02/skos/core#broader",
			"@http://www.w3.org/2004/02/skos/core#altLabel",
			"@http://www.w3.org/2004/02/skos/core#prefLabel",
			"@rdfs:comment",
			"@http://purl.org/goodrelations/v1#taxID",
			"@http://purl.org/goodrelations/v1#legalName",
			"@http://xmlns.com/foaf/0.1/logo",
			"@http://xmlns.com/foaf/0.1/homepage",
			"@http://www.w3.org/2004/02/skos/core#narrower",
			"@http://www.w3.org/2004/02/skos/core#altLabel",
			"@http://www.w3.org/2004/02/skos/core#prefLabel",
			"@owl:versionInfo",
			"@http://purl.org/dc/terms/modified",
			"@http://purl.org/dc/terms/created",
			"@http://purl.org/dc/terms/description",
			"@http://purl.org/dc/terms/title",
			"owl:Ontology",
			"/broker-pref-attr#",
			"@http://www.w3.org/2004/02/skos/core#topConceptOf",
			"@http://www.w3.org/2004/02/skos/core#skos:prefLabel",
			"@http://purl.org/dc/terms/publisher",
			"@http://purl.org/dc/terms/creator",
			"@http://www.w3.org/2004/02/skos/core#hasTopConcept"
		};

	private static Object[] bpResources = PolicyCompletenessCompliance.brokerPolicyStressTestResources;
	private static Object[] sdResources = PolicyCompletenessCompliance.serviceDescriptionStressTestResources;

	int problemNumber = 0;
	int okElementsInTriples = 0;
	int totalOK = 0;
	int totalIgnored = 0;

	public static void main(String[] args) {
		EvaluationStressTest est = new EvaluationStressTest();
		est.normalEvaluate();
		est.performStressTest();
	}

	private void normalEvaluate()
	{
		try {
			PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();

			// validate broker policy first
			pc.validateBrokerPolicy(bpResources);
			//pc.validateBrokerPolicy(brokerPolicyPath);

			// Get broker policy in Java object structure
			pc.getBrokerPolicy(bpResources);

			// Perform completeness check
			pc.validateSDForCompletenessCompliance(PolicyCompletenessCompliance.serviceDescriptionStressTestResources);

			//pc.performStressTest();

		} catch (Exception e) {
			System.out.println("Failure: " + e.getClass().getName() + " - "
					+ e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	private void performStressTest() 
	{
		/*
		 * For each triple (T) in model:
		 * 		For all three elements (E) in triple (T):
		 * 			1) Make a typo in every element s, p, o of (E)
		 * 			2) Run validation mechanism. You should get Exception (X).
		 * 			3) if(X)
		 * 					correct typo in (E)
		 * 					continue loop
		 * 			   else
		 * 					report that failure has not been caught.
		 * 			
		 */

		nullifySystemOut();

		stressTestBP();
	}

	private void stressTestBP() {
		//get triples
		List<Triple> bpTriplesList = this.getTriples(bpResources);
		
		System.err.println("Total number of triples: " + bpTriplesList.size());
		System.err.println("Total number of elements in triples: " + bpTriplesList.size() * 3);
		System.err.println("Elements changed that did not create problem:");
		
		PolicyCompletenessCompliance pc;
		for(Triple t:bpTriplesList)
		{
			if(tripleShouldBeIgnored(t))
			{
				//System.err.println("Ignoring " + t);
				totalIgnored++;
				continue;
			}

			try {
				pc = createPcWithErroredSubjectT(bpResources, t);
				pc.validateBrokerPolicy(convertTriplesToInputStream(pc));

				// no exception with erroredT, this is a problem
				if(okElementsInTriples > 0)
				{
					totalOK += okElementsInTriples;
					//System.err.println("... in the meantime " + okTriples + " OK ...");
					okElementsInTriples = 0;
				}
				System.err.println(++problemNumber + " with changed subject) " + t);
				/*System.err.println(t);
					System.err.println("to:");
					System.err.println(erroredT);
					System.err.println("did not cause a problem!");
					System.err.println("-------------------------------------------------------------------");
					System.err.println();*/
			} catch (BrokerPolicyException | CompletenessException | ComplianceException e) {
				okElementsInTriples++;
			} catch (Exception e) {
				// other exception with erroredT, this is a problem
				if(okElementsInTriples > 0)
				{
					totalOK += okElementsInTriples;
					//System.err.println("... in the meantime " + okTriples + " OK ...");
					okElementsInTriples = 0;
				}
				System.err.println(++problemNumber + ") " + e.getMessage() + " for " + t);
				e.printStackTrace();
			}
			
			try {
				pc = createPcWithErroredPredicateT(bpResources, t);
				pc.validateBrokerPolicy(convertTriplesToInputStream(pc));

				// no exception with erroredT, this is a problem
				if(okElementsInTriples > 0)
				{
					totalOK += okElementsInTriples;
					//System.err.println("... in the meantime " + okTriples + " OK ...");
					okElementsInTriples = 0;
				}
				System.err.println(++problemNumber + " with changed predicate) " + t);
				/*System.err.println(t);
					System.err.println("to:");
					System.err.println(erroredT);
					System.err.println("did not cause a problem!");
					System.err.println("-------------------------------------------------------------------");
					System.err.println();*/
			} catch (BrokerPolicyException | CompletenessException | ComplianceException e) {
				okElementsInTriples++;
			} catch (Exception e) {
				// other exception with erroredT, this is a problem
				if(okElementsInTriples > 0)
				{
					totalOK += okElementsInTriples;
					//System.err.println("... in the meantime " + okTriples + " OK ...");
					okElementsInTriples = 0;
				}
				System.err.println(++problemNumber + ") " + e.getMessage() + " for " + t);
				e.printStackTrace();
			}
			
			try {
				pc = createPcWithErroredObjectT(bpResources, t);
				pc.validateBrokerPolicy(convertTriplesToInputStream(pc));

				// no exception with erroredT, this is a problem
				if(okElementsInTriples > 0)
				{
					totalOK += okElementsInTriples;
					//System.err.println("... in the meantime " + okTriples + " OK ...");
					okElementsInTriples = 0;
				}
				System.err.println(++problemNumber + " with changed object) " + t);
				/*System.err.println(t);
					System.err.println("to:");
					System.err.println(erroredT);
					System.err.println("did not cause a problem!");
					System.err.println("-------------------------------------------------------------------");
					System.err.println();*/
			} catch (BrokerPolicyException | CompletenessException | ComplianceException e) {
				okElementsInTriples++;
			} catch (Exception e) {
				// other exception with erroredT, this is a problem
				if(okElementsInTriples > 0)
				{
					totalOK += okElementsInTriples;
					//System.err.println("... in the meantime " + okTriples + " OK ...");
					okElementsInTriples = 0;
				}
				System.err.println(++problemNumber + ") " + e.getMessage() + " for " + t);
				e.printStackTrace();
			}
			
			System.err.println("-----------------------------------------------------------------");
		}

		System.err.println("Total number of elements in triples that caused problem: " + (totalOK + okElementsInTriples));
		System.err.println("Total number of elements in triples that did not cause problem: " + problemNumber);
		System.err.println("Total number of elements in triples ignored: " + totalIgnored*3);
	}

	private PolicyCompletenessCompliance createPcWithErroredSubjectT(Object[] resources, Triple t) throws IOException {
		PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();
		
		Triple erroredT;
		// add data
		pc.addDataToJenaModel(resources);
		// delete triple
		pc.modelMem.getGraph().delete(t);
		// create erroredSubjectT
		erroredT = createErroredSubjectT(t);
		// add erroredT
		pc.modelMem.getGraph().add(erroredT);
		
		return pc;
	}

	private PolicyCompletenessCompliance createPcWithErroredPredicateT(Object[] resources, Triple t) throws IOException {
		PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();
		
		Triple erroredT;
		// add data
		pc.addDataToJenaModel(resources);
		// delete triple
		pc.modelMem.getGraph().delete(t);
		// create erroredSubjectT
		erroredT = createErroredPredicateT(t);
		// add erroredT
		pc.modelMem.getGraph().add(erroredT);
		
		return pc;
	}

	private PolicyCompletenessCompliance createPcWithErroredObjectT(Object[] resources, Triple t) throws IOException {
		PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();
		
		Triple erroredT;
		// add data
		pc.addDataToJenaModel(resources);
		// delete triple
		pc.modelMem.getGraph().delete(t);
		// create erroredSubjectT
		erroredT = createErroredObjectT(t);
		// add erroredT
		pc.modelMem.getGraph().add(erroredT);
		
		return pc;
	}

	private List<Triple> getTriples(Object[] resources)
	{
		PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();

		// load BP first
		try {
			pc.addDataToJenaModel(resources);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//get triples
		List<Triple> bpTriplesList = pc.modelMem.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toList();
		
		return bpTriplesList;
	}
	
	private void nullifySystemOut() {
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {
				//DO NOTHING
			}
		}));
	}

	private boolean tripleShouldBeIgnored(Triple t)
	{
		for(String element:elementsThatMakeTriplesIgnored)
		{
			if(t.toString().contains(element))
			{
				return true;				
			}
		}

		return false;
	}

	private Triple createErroredSubjectT(Triple t) {
		Node erroredSubject = createErroredNode(t.getSubject());
		Triple erroredT = new Triple(erroredSubject, t.getPredicate(), t.getObject());
		return erroredT;
	}

	private Triple createErroredPredicateT(Triple t) {
		Node erroredPredicate = createErroredNode(t.getPredicate());
		Triple erroredT = new Triple(t.getSubject(), erroredPredicate, t.getObject());
		return erroredT;
	}

	private Triple createErroredObjectT(Triple t) {
		Node erroredObject = createErroredNode(t.getObject());
		Triple erroredT = new Triple(t.getSubject(), t.getPredicate(), erroredObject);
		return erroredT;
	}

	private Node createErroredNode(Node node) {
		Node erroredNode = null;
		if(node.isLiteral())
		{
			erroredNode = Node.createLiteral(node.toString() + "1");
		}
		else if(node.isURI())
		{
			erroredNode = Node.createURI(node.toString() + "1");
		}
		else if(node.isVariable())
		{
			erroredNode = Node.createVariable(node.toString() + "1");
		}
		return erroredNode;
	}

	private InputStream convertTriplesToInputStream(PolicyCompletenessCompliance pc) {
		ByteArrayOutputStream outA = new ByteArrayOutputStream();
		pc.modelMem.write(outA, "TURTLE");
		InputStream decodedInput=new ByteArrayInputStream(outA.toByteArray());
		return decodedInput;
	}

	private void loadTriples(List<Triple> triplesList,
			PolicyCompletenessCompliance pc) {
		for(Triple t:triplesList)
		{
			//PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();
			pc.modelMem.getGraph().add(t);
			// initially set the same model
			//pc.modelMem = cachedModel;
			// delete the current triple
			//pc.modelMem.getGraph().delete(t);
			// add the "errored" with typo triple
			//Triple erroredT = Triple.create(t.getSubject(), t.getPredicate(), t.getObject());

		}
	}
}
