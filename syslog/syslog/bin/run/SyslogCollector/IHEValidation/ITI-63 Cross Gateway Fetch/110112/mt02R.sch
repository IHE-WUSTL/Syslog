<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <title>Schematron rules for ITI-63/110112 ITI_Suppl_XCF_Rev1-1_TI_2011-08-19 3.63.6.1, see also TF-2a 3.18.5.1.2</title>
    <pattern>

        <rule id="EventIdentification" context="/AuditMessage/EventIdentification">
            
            <assert test="EventID[@code='110112']"
                >EventID@code must  be '110112'</assert>
            <assert test="EventID[@codeSystemName='DCM']"
                >EventID@codeSystemName must  be 'DCM'</assert>
        
            <assert test="@EventActionCode='E'"
                >EventActionCode must be 'E'</assert>
        
            <assert test="EventTypeCode[@code='ITI-63']"
                >EventTypeCode@code must  be 'ITI-63'</assert>
            <assert test="EventTypeCode[@codeSystemName='IHE Transactions']"
                >EventTypeCode@codeSystemName must  be 'IHE Transactions'</assert>
            
        </rule>

        <rule id="top level elements" context="/AuditMessage">
                
            <assert id="SourceElement" test="count(ActiveParticipant/RoleIDCode[@code='110153'])=1"
                >Must contain one Source ActiveParticipant (RoleIDCode = 110153)</assert>

            <assert id="DestinationElement" test="count(ActiveParticipant/RoleIDCode[@code='110152'])=1"
                >Must contain one Destination ActiveParticipant (RoleIDCode = 110152)</assert>

            <assert id="AuditSourceElement" test="count(AuditSourceIdentification)=1"
                >Must contain one AuditSourceIdentification</assert>
                
            <!-- Patient 0-n -->
            
            <assert id="QueryParametersElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and 
                                @ParticipantObjectTypeCodeRole='24'])=1"
                >Must contain one Query Params ParticipantObjectIdentification (TypeCode = 2 and TypeCodeRole = 24)</assert>
            
        </rule>
        
        <rule id="Source" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110153']]">
        		<assert test="@UserID!=''">Source UserID must be wsa:ReplyTo contents</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Source Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Source Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Source must have Network access point id</assert> 
        </rule>
        
        <rule id="destination" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110152']]">
        		<assert test="@UserID!=''">Destination UserID must be SOAP endpoint URL</assert>
        		<assert test="@AlternativeUserID!=''">Destination must have AlternativeUserID (process id)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Destination Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Destination Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Destination must have Network access point id</assert> 
        </rule>
        
        <!-- No specializations for Audit Source -->
        
        <rule id="Patient" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='1' and @ParticipantObjectTypeCodeRole='1']">
        		<assert test="ParticipantObjectIDTypeCode[@code='2']">Patient ParticipantObjectIDTypeCode code must be '2'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='RFC-3881']">Patient ParticipantObjectIDTypeCode code system must be 'RFC-3881'</assert>
        		<assert test="@ParticipantObjectID!=''">Patient must have ParticipantObjectID value HL7 CX id</assert>
        </rule>
        
        <rule id="Query parameters" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and @ParticipantObjectTypeCodeRole='24']">
        		<assert test="@ParticipantObjectID!=''">Query Participant Object ID must be Stored Query ID (UUID)</assert>
        		<assert test="ParticipantObjectIDTypeCode[@code='ITI-63']">Query ParticipantObjectIDTypeCode code must be 'ITI-63'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='IHE Transactions']">Query ParticipantObjectIDTypeCode code system must be 'IHE Transactions'</assert>
        		<assert test="ParticipantObjectQuery!=''">Query must have ParticipantObjectQuery (base64)</assert>
        		<assert test="count(ParticipantObjectDetail[@type='QueryEncoding'] and ParticipantObjectDetail[@value!=''])=1"
        			>Query must have 'QueryEncoding' detail element</assert>
        </rule>
    </pattern>
</schema>