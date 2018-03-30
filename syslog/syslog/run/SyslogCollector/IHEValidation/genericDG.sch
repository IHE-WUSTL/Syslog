<?xml version="1.0" encoding="UTF-8"?><schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <title>Schematron rules for ITI-8/110110 Ref: ITI TF-2a 3.8.5.1.1</title>
    <pattern>
    
        <!-- Correct EventIdentification-->
        
        <rule id="EventIdentification" context="/AuditMessage/EventIdentification">
            
            <assert test="EventID[@csd-code='110110']">EventID@code must  be '110110'</assert>
            <assert test="EventID[@codeSystemName='DCM']">EventID@codeSystemName must  be 'DCM'</assert>
        
            <assert test="@EventActionCode='C' or @EventActionCode='U'">EventActionCode must be 'C' for A01, A04, A05 or 'U' for A08</assert>
        
            <assert test="EventTypeCode[@csd-code='ITI-8']">EventTypeCode@code must  be 'ITI-8'</assert>
            <assert test="EventTypeCode[@codeSystemName='IHE Transactions']">EventTypeCode@codeSystemName must  be 'IHE Transactions'</assert>
        </rule>
        
        <rule id="TopLevelElements" context="/AuditMessage">
                
            <assert id="SourceElement" test="count(ActiveParticipant/RoleIDCode[@csd-code='110153'])=1">Must contain one Source ActiveParticipant (RoleIDCode = 110153)</assert>

            <assert id="DestinationElement" test="count(ActiveParticipant/RoleIDCode[@csd-code='110152'])=1">Must contain one Destination ActiveParticipant (RoleIDCode = 110152)</assert>

            <assert id="AuditSourceElement" test="count(AuditSourceIdentification)=1">Must contain one AuditSourceIdentification</assert>
            
            <assert id="DocumentElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and                                  @ParticipantObjectTypeCodeRole='3'])&gt;=1">Must contain at least one Document ParticipantObjectIdentification (TypeCode = 2 and TypeCodeRole = 3)</assert>
            
            <assert id="PatientElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='1' and                                  @ParticipantObjectTypeCodeRole='1'])=1">Must contain one Patient ParticipantObjectIdentification (TypeCode = 1 and TypeCodeRole = 1)</assert>
            
            <assert id="SubmissionSetElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and                                                 @ParticipantObjectTypeCodeRole='20'])=1">Must contain one SubmissionSet ParticipantObjectIdentification (TypeCode = 2 and TypeCodeRole = 20)</assert>
            
            <assert id="QueryParametersElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and                                  @ParticipantObjectTypeCodeRole='24'])=1">Must contain one Query Params ParticipantObjectIdentification (TypeCode = 2 and TypeCodeRole = 24)</assert>
            
            <assert id="ValueSetElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and                                  @ParticipantObjectTypeCodeRole='3'])=1">Must contain one Value Set Instance ParticipantObjectIdentification (TypeCode = 2 and TypeCodeRole = 3)</assert>
            
         </rule>
         
        <rule id="Source" context="/AuditMessage/ActiveParticipant[RoleIDCode[@csd-code='110153']]">
        		<assert test="@UserID!=''">Source UserID must be MSH sending facility and application</assert>
        		<assert test="@AlternativeUserID!=''">Source must have AlternativeUserID (process id)</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Source Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Source Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Source must have Network access point id</assert> 
        </rule>
        
        <!-- Destination ActiveParticipant -->
        <rule id="Destination" context="/AuditMessage/ActiveParticipant[RoleIDCode[@csd-code='110152']]">
        		<assert test="@UserID!=''">Destination must have UserID MSH receiving application and facility </assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Destination Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Destination Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Destination must have Network access point id</assert> 
        </rule>
        
        <!-- No specializations for Audit Source -->
        
        <!-- Patient  ParticipantObjectIdentification -->
        <rule id="Patient" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='1' and @ParticipantObjectTypeCodeRole='1']">
        		<assert test="ParticipantObjectIDTypeCode[@csd-code='2']">Patient ParticipantObjectIDTypeCode code must be '2'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='RFC-3881']">Patient ParticipantObjectIDTypeCode code system must be 'RFC-3881'</assert>
        		<assert test="@ParticipantObjectID!=''">Patient must have ParticipantObjectID value (HL7 CX)</assert>
       		<assert test="ParticipantObjectDetail[@type='MSH-10']">Patient ParticipantObjectDetail type must be 'MSH-10'</assert>
        		<assert test="ParticipantObjectDetail[@value!='']">Patient ParticipantObjectDetail must have MSH-10 value base64 encoded</assert>
        </rule>
        
        <rule id="SubmissionSet" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and @ParticipantObjectTypeCodeRole='20']">
        		<assert test="ParticipantObjectIDTypeCode[@csd-code='99SupHPD-ISO21091']">Provider ParticipantObjectIDTypeCode code must be '99SupHPD-ISO21091'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='IHE']">Provider ParticipantObjectIDTypeCode code system must be 'IHE'</assert>
        		<assert test="@ParticipantObjectID!=''">Provider must have ParticipantObjectID value provider ID in ISO 21091 format</assert>
        </rule>
        
        <rule id="Query parameters" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and @ParticipantObjectTypeCodeRole='24']">
        		<assert test="@ParticipantObjectID=''">Query Participant Object ID must be Stored Query ID (UUID)</assert>
        		<assert test="ParticipantObjectIDTypeCode[@csd-code='ITI-18']">Query ParticipantObjectIDTypeCode code must be 'ITI-18'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='IHE Transactions']">Query ParticipantObjectIDTypeCode code system must be 'IHE Transactions'</assert>
        		<assert test="ParticipantObjectQuery!=''">Query must have ParticipantObjectQuery (base64)</assert>
        		<assert test="count(ParticipantObjectDetail[@type='QueryEncoding'] and ParticipantObjectDetail[@value!=''])=1">Query must have 'QueryEncoding' detail element</assert>
        	</rule>
        
        <rule id="Document" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and @ParticipantObjectTypeCodeRole='3']">
        		<assert test="ParticipantObjectIDTypeCode[@csd-code='9']">Document ParticipantObjectIDTypeCode code must be '9'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='RFC-3881']">Document ParticipantObjectIDTypeCode code system must be 'RFC-3881'</assert>
        		<assert test="@ParticipantObjectID!=''">Document must have ParticipantObjectID value (Document Unique ID)</assert>
        		<assert test="count(ParticipantObjectDetail[@type='Repository Unique Id' and @value!=''])=1">Query must have 'Repository Unique Id' detail element</assert>
        		<assert test="count(ParticipantObjectDetail[@type='ihe:homeCommunityID' and @value!=''])=1">Query must have 'homeCommunityID' detail element</assert>
        </rule>
        
        <rule id="ValueSetInstance" context="/AuditMessage/ParticipantObjectIdentification[@ParticipantObjectTypeCode='2' and @ParticipantObjectTypeCodeRole='3']">
        		<assert test="ParticipantObjectIDTypeCode[@csd-code='9']">ValueSetInstance ParticipantObjectIDTypeCode code must be '9'</assert>
        		<assert test="ParticipantObjectIDTypeCode[@codeSystemName='RFC-3881']">ValueSetInstance ParticipantObjectIDTypeCode code system must be 'RFC-3881'</assert>
        		<assert test="@ParticipantObjectID!=''">ValueSetInstance must have ParticipantObjectID value (Value Set Unique ID)</assert>
        		<assert test="count(ParticipantObjectDetail[@type='Value Set Version' and @value!=''])=1">Value set instance must have 'Value Set Version' detail element</assert>
        </rule>
        
    </pattern>
</schema>