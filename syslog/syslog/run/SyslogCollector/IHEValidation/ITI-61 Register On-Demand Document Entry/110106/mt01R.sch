<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <title>Schematron rules for ITI-61/110106 ITI TF-2b R11 3.61.7.1.1</title>
    <pattern>

        <rule id="EventIdentification" context="/AuditMessage/EventIdentification">
            
            <assert test="EventID[@code='110106']"
                >EventID@code must be '110106'</assert>
            <assert test="EventID[@codeSystemName='DCM']"
                >EventID@codeSystemName must be 'DCM'</assert>
        
            <assert test="@EventActionCode='R'"
                >EventActionCode must be 'R' (Read)</assert>
        
            <assert test="EventTypeCode[@code='ITI-61']"
                >EventTypeCode@code must be 'ITI-61'</assert>
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
            
            <assert id="PatientElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='1' and 
                                               @ParticipantObjectTypeCodeRole='1'])=1"
                >Must contain one Patient ParticipantObjectIdentification (TypeCode = 1 and TypeCodeRole = 1)</assert>
            
            <assert id="SubmissionSetElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and 
                                               @ParticipantObjectTypeCodeRole='20'])=1"
                >Must contain one SubmissionSet ParticipantObjectIdentification (TypeCode = 2 and TypeCodeRole = 20)</assert>
        </rule>
        
        <rule id="Source" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110153']]">
        		<assert test="@AlternativeUserID!=''">Source must have AlternativeUserID (process id)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Source Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Source Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Source must have Network access point id</assert> 
        </rule>
        
        <rule id="destination" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110152']]">
        		<assert test="@UserID!=''">Source must have UserID (SOAP endpoint URI)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Destination Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Destination Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Destination must have Network access point id</assert> 
        </rule>
        
        <!-- No specializations for Audit Source -->
        
        <!-- Patient  ParticipantObjectIdentification -->
        <rule id="Patient" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='1' and @ParticipantObjectTypeCodeRole='1']">
        		<assert test="ParticipantObjectIDTypeCode[@code='2']">Patient ParticipantObjectIDTypeCode code must be '2'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='RFC-3881']">Patient ParticipantObjectIDTypeCode code system must be 'RFC-3881'</assert>
        		<assert test="@ParticipantObjectID!=''">Patient must have ParticipantObjectID value (HL7 CX)</assert>
        </rule>
        
        <rule id="SubmissionSet" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and @ParticipantObjectTypeCodeRole='20']">
        		<assert test="ParticipantObjectIDTypeCode[@code='urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd']">Provider ParticipantObjectIDTypeCode code must be 'urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='IHE XDS Metadata']">Provider ParticipantObjectIDTypeCode code system must be 'IHE XDS Metadata'</assert>
        		<assert test="@ParticipantObjectID!=''">Provider must have ParticipantObjectID value submission set UID</assert>
        </rule>
    </pattern>
</schema>