<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <title>Schematron rules for ITI-70/110112 ITI Supplement DSUB TI 2014-10-13 3.70.5.1.1</title>
    <pattern>
        
        <rule id="EventIdentification" context="/AuditMessage/EventIdentification">
            
            <assert test="EventID[@code='110112']"
                >EventID@code must  be '110112'</assert>
            <assert test="EventID[@codeSystemName='DCM']"
                >EventID@codeSystemName must be 'DCM'</assert>
        
            <assert test="@EventActionCode='E'"
                >EventActionCode must be 'E'(Execute)</assert>
        
            <assert test="EventTypeCode[@code='ITI-70']"
                >EventTypeCode@code must  be 'ITI-70'</assert>
            <assert test="EventTypeCode[@codeSystemName='IHE Transactions']"
                >EventTypeCode@codeSystemName must be 'IHE Transactions'</assert>
        </rule>

        <rule id="TopLevelElements" context="/AuditMessage">
                
            <assert id="SourceElement" test="count(ActiveParticipant/RoleIDCode[@code='110153'])=1"
                >Must contain one Source ActiveParticipant (RoleIDCode = 110153)</assert>
                
            <assert id="DestinationElement" test="count(ActiveParticipant/RoleIDCode[@code='110152'])=1"
                >Must contain one Destination ActiveParticipant (RoleIDCode = 110152)</assert>
            
            <assert id="Patient" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='1' and 
                                @ParticipantObjectTypeCodeRole='1'])&lt;=1"
                >Must contain at most one Patient ParticipantObjectIdentification (TypeCode = 1 and TypeCodeRole = 1)</assert>
            
            <assert id="PullPoint" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and 
                                @ParticipantObjectTypeCodeRole='1'])=1"
                >Must contain one Document ParticipantObjectIdentification (TypeCode = 2 and TypeCodeRole = 1)</assert>
        </rule>
        
        <rule id="Source" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110153']]">
        		<assert test="@UserID!=''">Source UserID must be contents of wsa:ReplyTo element</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Source Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Source Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Source must have Network access point id</assert> 
        </rule>
        
        <rule id="Destination" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110152']]">
        		<assert test="@UserID!=''">Destination must have UserID (SOAP endpoint URI)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Destination Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Destination Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Destination must have Network access point id</assert> 
        </rule>
        
        <rule id="Patient" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='1' and @ParticipantObjectTypeCodeRole='1']">
        		<assert test="ParticipantObjectIDTypeCode[@code='2']">Patient ParticipantObjectIDTypeCode code must be '2'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='RFC-3881']">Patient ParticipantObjectIDTypeCode codeSystemName must be 'RFC-3881'</assert>
        		<assert test="@ParticipantObjectID!=''">Patient must have ParticipantObjectID value (Patient ID in HL7 CX format)</assert>
        </rule>
        
        <rule id="PullPoint" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and @ParticipantObjectTypeCodeRole='4']">
        		<assert test="ParticipantObjectIDTypeCode[@code='12']">Patient ParticipantObjectIDTypeCode code must be '12'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='RFC-3881']">Patient ParticipantObjectIDTypeCode codeSystemName must be 'RFC-3881'</assert>
        		<assert test="@ParticipantObjectID!=''">Patient must have ParticipantObjectID value (URL of Pull Point resource)</assert>
        </rule>
    </pattern>
</schema>