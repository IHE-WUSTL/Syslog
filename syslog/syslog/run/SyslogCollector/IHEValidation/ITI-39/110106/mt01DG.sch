<?xml version="1.0" encoding="UTF-8"?><schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <title>Schematron rules for ITI-39/110106 Ref: ITI TF-2b R11 3:39.4.1.4 and ITI TF 2b R11 3.43.6.1.2"</title>
    <pattern>

        <rule id="EventIdentification" context="/AuditMessage/EventIdentification">
            
            <assert test="EventID[@csd-code='110106']">EventID@code must  be '110106'</assert>
            <assert test="EventID[@codeSystemName='DCM']">EventID@codeSystemName must  be 'DCM'</assert>
        
            <assert test="@EventActionCode='R'">EventActionCode must be 'R'</assert>
        
            <assert test="EventTypeCode[@csd-code='ITI-39']">EventTypeCode@code must  be 'ITI-39'</assert>
            <assert test="EventTypeCode[@codeSystemName='IHE Transactions']">EventTypeCode@codeSystemName must  be 'IHE Transactions'</assert>
            
        </rule>
                
        <rule id="TopLevelElements" context="/AuditMessage">
                
            <assert id="SourceElement" test="count(ActiveParticipant/RoleIDCode[@csd-code='110153'])=1">Must contain one Source ActiveParticipant (RoleIDCode = 110153)</assert>

            <assert id="DestinationElement" test="count(ActiveParticipant/RoleIDCode[@csd-code='110152'])=1">Must contain one Destination ActiveParticipant (RoleIDCode = 110152)</assert>

            <assert id="AuditSourceElement" test="count(AuditSourceIdentification)=1">Must contain one AuditSourceIdentification</assert>
            
            <assert id="DocumentElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and                                  @ParticipantObjectTypeCodeRole='3'])&gt;=1">Must contain at least one Document ParticipantObjectIdentification (TypeCode = 2 and TypeCodeRole = 3)</assert>
        </rule>
        
        <!-- Source ActiveParticipant -->
        <rule id="Source" context="/AuditMessage/ActiveParticipant[RoleIDCode[@csd-code='110153']]">
        		<assert test="@UserID!=''">Source must have UserID (SOAP endpoint URI)</assert>
        		<assert test="@AlternativeUserID!=''">Source must have AlternativeUserID (process id)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Source Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Source Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Source must have Network access point id</assert> 
        </rule>
        
        <rule id="Destination" context="/AuditMessage/ActiveParticipant[RoleIDCode[@csd-code='110152']]">
        		<assert test="@UserID!=''">Destination must have UserID attribute (wsa:ReplyTo)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Destination Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Destination Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Destination must have Network access point id</assert> 
        </rule> 
        
        <!-- No specializations for Audit Source -->
        
        <rule id="Document" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and @ParticipantObjectTypeCodeRole='3']">
        		<assert test="ParticipantObjectIDTypeCode[@csd-code='9']">Document ParticipantObjectIDTypeCode code must be '9'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='RFC-3881']">Document ParticipantObjectIDTypeCode code system must be 'RFC-3881'</assert>
        		<assert test="@ParticipantObjectID!=''">Document must have ParticipantObjectID value (Document Unique ID)</assert>
        		<assert test="count(ParticipantObjectDetail[@type='Repository Unique Id' and @value!=''])=1">Query must have 'Repository Unique Id' detail element</assert>
        		<assert test="count(ParticipantObjectDetail[@type='ihe:homeCommunityID' and @value!=''])=1">Query must have 'homeCommunityID' detail element</assert>
        </rule>
    </pattern>
</schema>