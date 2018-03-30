<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <title>Schematron rules for ITI-53/110106 ITI Supplement DSUB TI 2014-10-13 3.53.5.1.2</title>
    <pattern>

        <rule id="EventIdentification" context="/AuditMessage/EventIdentification">
            
            <assert test="EventID[@code='110106']"
                >EventID@code must be '110106'</assert>
            <assert test="EventID[@codeSystemName='DCM']"
                >EventID@codeSystemName must be 'DCM'</assert>
        
            <assert test="@EventActionCode='R'"
                >EventActionCode must be 'R' (Read)</assert>
        
            <assert test="EventTypeCode[@code='ITI-53']"
                >EventTypeCode@code must be 'ITI-53'</assert>
            <assert test="EventTypeCode[@codeSystemName='IHE Transactions']"
                >EventTypeCode@codeSystemName must be 'IHE Transactions'</assert>
        </rule>

        <rule id="top level elements" context="/AuditMessage">
                
            <assert id="SourceElement" test="count(ActiveParticipant/RoleIDCode[@code='110153'])=1"
                >Must contain one Source ActiveParticipant (RoleIDCode = 110153)</assert>

            <assert id="DestinationElement" test="count(ActiveParticipant/RoleIDCode[@code='110152'])=1"
                >Must contain one Destination ActiveParticipant (RoleIDCode = 110152)</assert>

            <assert id="AuditSourceElement" test="count(AuditSourceIdentification)=1"
                >Must contain one AuditSourceIdentification</assert>
            
            <assert id="DocumentElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and 
                                @ParticipantObjectTypeCodeRole='3'])&gt;=1"
                >Must contain at least one Document ParticipantObjectIdentification (TypeCode = 2 and TypeCodeRole = 3)</assert>
        </rule>
        
        <rule id="Source" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110153']]">
        		<assert test="@UserID!=''">Source must have UserID (wsa:From)</assert>
        		<assert test="@AlternativeUserID!=''">Source AlternativeUserID must be process id</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Source Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Source Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Source must have Network access point id</assert> 
        </rule>
        
        <rule id="destination" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110152']]">
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Destination Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Destination Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Destination must have Network access point id</assert> 
        </rule>
        
        <!-- No specializations for Audit Source -->
        
        <rule id="Document" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and @ParticipantObjectTypeCodeRole='3']">
        		<assert test="ParticipantObjectIDTypeCode[@code='urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1'] or
        		ParticipantObjectIDTypeCode[@code='urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2'] or
        		ParticipantObjectIDTypeCode[@code='urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd']"
        		>Document ParticipantObjectIDTypeCode code must be one of 'urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1' (Document Entry), 
        		 'urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2' (Folder), 
        		  or 'urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd' (SubmissionSet)</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='IHE']">Document ParticipantObjectIDTypeCode code system must be 'IHE'</assert>
        		<assert test="@ParticipantObjectID!=''">Document must have ParticipantObjectID value one of DocumentUniqueId, XDSFolder.uniqueId, or XDSSubmissionSet.uniqueId</assert>
        </rule>
    </pattern>
</schema>