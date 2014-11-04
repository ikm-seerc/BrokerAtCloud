BrokerAtCloud
=============

This repository will contain the various software components developed by SEERC for the needs of Broker@Cloud project.

PolicyCompletenessCompliance
----------------------------

This is an Eclipse project that contains the code for the Service Description validation mechanism. The code needs as input a Service Description file and a Broker Policy file to validate against.

The path to the Broker Policy file is declared here,

https://github.com/chrispetsos-seerc/BrokerAtCloud/blob/master/PolicyCompletenessCompliance/src/org/seerc/brokeratcloud/policycompletenesscompliance/PolicyCompletenessCompliance.java#L39

and the path to the Service Description file is declared here,

https://github.com/chrispetsos-seerc/BrokerAtCloud/blob/master/PolicyCompletenessCompliance/src/org/seerc/brokeratcloud/policycompletenesscompliance/PolicyCompletenessCompliance.java#L40

You are free to change those paths to point to your own Broker Policy and Service Description files in order to demonstrate the mechanism.


MessageBroker
----------------------------

This is an Eclipse project that contains the code for various components that orchestrate the procedure of interacting with the WSO2 Message Broker (MB) and the WSO2 Governance Registry (GReg). Specifically,

Class https://github.com/chrispetsos-seerc/BrokerAtCloud/blob/master/MessageBroker/src/org/seerc/brokeratcloud/messagebroker/EvaluationComponentSDSubscriber.java 
is responsible for receiving new published Service Descriptions, sending them to GReg and publishing evaluation reports back to MB.

Class https://github.com/chrispetsos-seerc/BrokerAtCloud/blob/master/MessageBroker/src/org/seerc/brokeratcloud/messagebroker/RegistryRepositoryTopicSubscriber.java 
is responsible for listening for SD reports published at the MB and assigning those reports to SDs' properties inside GReg.

Some hepler classes are,
https://github.com/chrispetsos-seerc/BrokerAtCloud/blob/master/MessageBroker/src/org/seerc/brokeratcloud/messagebroker/MessageBrokerDemo.java that runs a demo of publishing a new SD in order to see the whole procedure in action.
https://github.com/chrispetsos-seerc/BrokerAtCloud/blob/master/MessageBroker/src/org/seerc/brokeratcloud/messagebroker/MessageBrokerStressTest.java contains some stress tests that bring WSO2 MB and GReg to their limits.

Folder https://github.com/chrispetsos-seerc/BrokerAtCloud/tree/master/MessageBroker/resources/properties has some configuration files used for connecting to the WSO2 MB and GReg.

org.seerc.brokeratcloud.webservice
----------------------------

This is an Eclipse project that contains a web application which publishes all REST services provided by SEERC.

BrokerPolicyValidationHandler
----------------------------
This is a WSO2 Developer Studio Registry Handler project. It implements the Handler functionality for validating Broker Policies when they are PUT inside GReg at their specified path.
