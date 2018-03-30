<?xml version="1.0" encoding="UTF-8"?><schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <title>Schematron rules for ITI-48/110107 ITI_Suppl_SVS_Rev2-1_TI_2010-08-10 3.48.6.1.1</title>
    <pattern>

        <rule id="EventIdentification" context="/AuditMessage/EventIdentification">
            
            <assert test="EventID[@csd-code='110107']">EventID@code must be '110107'</assert>
            <assert test="EventID[@codeSystemName='DCM']">EventID@codeSystemName must be 'DCM'</assert>
        
            <assert test="@EventActionCode='C' or @EventActionCode='U'">EventActionCode must be 'C' (Create) or 'U' (Update)</assert>
        
            <assert test="EventTypeCode[@csd-code='ITI-60']">EventTypeCode@code must be 'ITI-60'</assert>
            <assert test="EventTypeCode[@codeSystemName='IHE Transactions']">EventTypeCode@codeSystemName must be 'IHE Transactions'</assert>
        </rule>

        <rule id="top level elements" context="/AuditMessage">
                
            <assert id="SourceElement" test="count(ActiveParticipant/RoleIDCode[@csd-code='110153'])=1">Must contain one Source ActiveParticipant (RoleIDCode = 110153)</assert>
                
            <assert id="DestinationElement" test="count(ActiveParticipant/RoleIDCode[@csd-code='110152'])=1">Must contain one Destination ActiveParticipant (RoleIDCode = 110152)</assert>

            <assert id="AuditSourceElement" test="count(AuditSourceIdentification)=1">Must contain one AuditSourceIdentification</assert>
            
            <assert id="ValueSetElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and                                  @ParticipantObjectTypeCodeRole='3'])=1">Must contain one Value Set Instance ParticipantObjectIdentification (TypeCode = 2 and TypeCodeRole = 3)</assert>
        </rule>
        
        <rule id="Source" context="/AuditMessage/ActiveParticipant[RoleIDCode[@csd-code='110153']]">
        		<assert test="@UserID!=''">Source must have UserID (repository HTTP SOAP endpoint URI)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Source Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Source Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Source must have Network access point id</assert> 
        </rule>
        
        <rule id="destination" context="/AuditMessage/ActiveParticipant[RoleIDCode[@csd-code='110152']]">
        		<assert test="@AlternativeUserID!=''">Source must have AlternativeUserID (process id)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Destination Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Destination Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Destination must have Network access point id</assert> 
        </rule>
        
        <!-- No specializations for Audit Source -->
        
        <rule id="ValueSetInstance" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and @ParticipantObjectTypeCodeRole='3']">
        		<assert test="ParticipantObjectIDTypeCode[@csd-code='9']">ValueSetInstance ParticipantObjectIDTypeCode code must be '9'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='RFC-3881']">ValueSetInstance ParticipantObjectIDTypeCode code system must be 'RFC-3881'</assert>
        		<assert test="@ParticipantObjectID!=''">ValueSetInstance must have ParticipantObjectID value (Value Set Unique ID)</assert>
        </rule>
    </pattern>
</schema>