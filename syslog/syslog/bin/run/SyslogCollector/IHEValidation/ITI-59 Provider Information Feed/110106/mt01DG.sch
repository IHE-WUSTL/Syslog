<?xml version="1.0" encoding="UTF-8"?><schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <title>Schematron rules for ITI-59/110106 ITI_Suppl_HPD_Rev1-2_TI_2011-08-19 3.59.5.1.1</title>
    <pattern>

        <rule id="EventIdentification" context="/AuditMessage/EventIdentification">
            
            <assert test="EventID[@csd-code='110106']">EventID@code must be '110106'</assert>
            <assert test="EventID[@codeSystemName='DCM']">EventID@codeSystemName must be 'DCM'</assert>
        
            <assert test="@EventActionCode='R'">EventActionCode must be 'R' (Read)</assert>
        
            <assert test="EventTypeCode[@csd-code='ITI-59']">EventTypeCode@code must be 'ITI-59'</assert>
            <assert test="EventTypeCode[@codeSystemName='IHE Transactions']">EventTypeCode@codeSystemName must be 'IHE Transactions'</assert>
        </rule>

        <rule id="top level elements" context="/AuditMessage">
                
            <assert id="SourceElement" test="count(ActiveParticipant/RoleIDCode[@csd-code='110153'])=1">Must contain one Source ActiveParticipant (RoleIDCode = 110153)</assert>
                
            <assert id="DestinationElement" test="count(ActiveParticipant/RoleIDCode[@csd-code='110152'])=1">Must contain one Destination ActiveParticipant (RoleIDCode = 110152)</assert>

            <assert id="AuditSourceElement" test="count(AuditSourceIdentification)=1">Must contain one AuditSourceIdentification</assert>
            
            <assert id="ProviderElement" test="count(ParticipantObjectIdentification[(@ParticipantObjectTypeCode='1' or                                                @ParticipantObjectTypeCode='3') and                                                 @ParticipantObjectTypeCodeRole='15'])&gt;=1">Must contain at least one Provider ParticipantObjectIdentification (TypeCode = 1 or 3 and TypeCodeRole = 15)</assert>
        </rule>
        
        <rule id="Source" context="/AuditMessage/ActiveParticipant[RoleIDCode[@csd-code='110153']]">
        		<assert test="@UserID!=''">Source must have UserID (process id)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Source Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Source Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Source must have Network access point id</assert> 
        </rule>
        
        <rule id="destination" context="/AuditMessage/ActiveParticipant[RoleIDCode[@csd-code='110152']]">
        		<assert test="@UserID!=''">Source must have UserID (SOAP endpoint URI)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Destination Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Destination Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Destination must have Network access point id</assert> 
        </rule>
        
        <!-- No specializations for Audit Source -->
        
        <rule id="Provider" context="/AuditMessage/ParticipantObjectIdentification[(@ParticipantObjectTypeCode='1' or @ParticipantObjectTypeCode='3') and @ParticipantObjectTypeCodeRole='15']">
        		<assert test="ParticipantObjectIDTypeCode[@csd-code='99SupHPD-ISO21091']">Provider ParticipantObjectIDTypeCode code must be '99SupHPD-ISO21091'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='IHE']">Provider ParticipantObjectIDTypeCode code system name must be 'IHE'</assert>
        		<assert test="@ParticipantObjectID!=''">Provider must have ParticipantObjectID value provider ID in ISO 21091 format</assert>
        </rule>
    </pattern>
</schema>