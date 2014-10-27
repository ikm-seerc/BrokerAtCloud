BrokerAtCloud
=============

This repository will contain the various software components developed by SEERC for the needs of Broker@Cloud project.

PolicyCompletenessCompliance
----------------------------

This is an Eclipse project that contains the code for the Service Description validation mechanism. The code needs as input a Service Description file and a Broker Policy file to validate against.

The path to the Broker Policy file is declared here,

https://github.com/chrispetsos-seerc/BrokerAtCloud/blob/master/PolicyCompletenessCompliance/src/org/seerc/brokeratcloud/policycompletenesscompliance/PolicyCompletenessCompliance.java#L91

and the path to the Service Description file is declared here,

https://github.com/chrispetsos-seerc/BrokerAtCloud/blob/master/PolicyCompletenessCompliance/src/org/seerc/brokeratcloud/policycompletenesscompliance/PolicyCompletenessCompliance.java#L97

and here,

https://github.com/chrispetsos-seerc/BrokerAtCloud/blob/master/PolicyCompletenessCompliance/src/org/seerc/brokeratcloud/policycompletenesscompliance/PolicyCompletenessCompliance.java#L107


You are free to change those paths to point to your own Broker Policy and Service Description files in order to demonstrate the mechanism.

