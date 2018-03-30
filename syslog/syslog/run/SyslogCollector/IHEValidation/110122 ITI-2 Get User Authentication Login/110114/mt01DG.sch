<?xml version="1.0" encoding="UTF-8"?><schema xmlns="http://purl.oclc.org/dsdl/schematron">
	<title>Schematron rules for 110122/110114 (Login) ITI TF-2a R11 3.2.6</title>
	<pattern>

		<rule id="EventIdentification" context="/AuditMessage/EventIdentification">

			<assert test="EventID[@csd-code='110114']">EventID@code must be '110114'</assert>
			<assert test="EventID[@codeSystemName='DCM']">EventID@codeSystemName	must be 'DCM'</assert>

			<assert test="@EventActionCode='E'">EventActionCode must be 'E' (Execute)</assert>

			<assert test="EventTypeCode[@csd-code='110122']">EventTypeCode@code must be '110122'</assert>
			<assert test="EventTypeCode[@codeSystemName='DCM']">EventTypeCode@codeSystemName must be 'DCM'</assert>

		</rule>

		<rule id="top level elements" context="/AuditMessage">
		
			<assert id="SourceElement" test="count(ActiveParticipant[RoleIDCode[@csd-code='110150']])=1">Must contain one Source (RoleIDCode = 110150)</assert>

			<assert id="DestinationElement" test="count(ActiveParticipant[RoleIDCode[@csd-code='110152']])=0">May not contain Destination (RoleIDCode = 110152)</assert>

			<assert id="AuditSourceElement" test="count(AuditSourceIdentification)=1">Must contain one AuditSourceIdentification</assert>

			<assert id="ParticipantObjectElement" test="count(ParticipantObjectIdentification)=0">May not contain ParticipantObject</assert>
				
		</rule>
		
		<rule id="Source" context="/AuditMessage/ActiveParticipant[RoleIDCode[@csd-code='110150']]">
			<assert test="@UserID!=''">Source must have UserID (process id)</assert>
			<assert test="RoleIDCode[@codeSystemName='DCM']">Source Role ID code system must be DCM</assert>
			<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Source Network access type code must be 1 or 2</assert>
			<assert test="@NetworkAccessPointID!=''">Source must have Network access point id</assert>
		</rule>
		
		<!-- No specializations for Audit Source -->
		
	</pattern>
</schema>