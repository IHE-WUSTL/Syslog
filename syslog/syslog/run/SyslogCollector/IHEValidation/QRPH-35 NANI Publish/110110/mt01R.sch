<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron">
    <title>Schematron rules for QRPH-35/110100 QRPH NANI TI R1.1 2012-08-27 3.35.5.1.2</title>
    <pattern>
        
        <rule id="EventIdentification" context="/AuditMessage/EventIdentification">
            
            <assert test="EventID[@code='110110']"
                >EventID@code must  be '110110'</assert>
            <assert test="EventID[@codeSystemName='DCM']"
                >EventID@codeSystemName must be 'DCM'</assert>
        
            <assert test="@EventActionCode='C' or @EventActionCode='U' or @EventActionCode='D'"
                >EventActionCode must be 'C' (create), 'U' (update), or 'D' (delete) as appropriate </assert>
        
            <assert test="EventTypeCode[@code='QRPH-35']"
                >EventTypeCode@code must  be 'QRPH-35'</assert>
            <assert test="EventTypeCode[@codeSystemName='IHE Transactions']"
                >EventTypeCode@codeSystemName must be 'IHE Transactions'</assert>
        </rule>

        <rule id="TopLevelElements" context="/AuditMessage">
                
            <assert id="SourceElement" test="count(ActiveParticipant/RoleIDCode[@code='110153'])=1"
                >Must contain one Source ActiveParticipant (RoleIDCode = 110153)</assert>
                
            <assert id="DestinationElement" test="count(ActiveParticipant/RoleIDCode[@code='110152'])=1"
                >Must contain one Destination ActiveParticipant (RoleIDCode = 110152)</assert>
                
            <assert id="AuditSourceElement" test="count(AuditSourceIdentification)=1"
                >Must contain one AuditSourceIdentifcation</assert>
            
            <assert id="PatientElement" test="count(ParticipantObjectIdentification[@ParticipantObjectTypeCode='1' and 
                                @ParticipantObjectTypeCodeRole='1'])=1"
                >Must contain one Patient ParticipantObjectIdentification (TypeCode = 1 and TypeCodeRole = 1)</assert>
        </rule>
        
        <rule id="Source" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110153']]">
        		<assert test="@UserID!=''">Source UserID must be wsa:ReplyTo contents</assert>
        		<assert test="@AlternativeUserID!=''">AlternativeUserID must be process id</assert>
        		<assert test="RoleIDCode[@codeSystemName='DCM']">Source Role ID code system must be DCM</assert>
        		<assert test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">Source Network access type code must be 1 or 2</assert>
        		<assert test="@NetworkAccessPointID!=''">Source must have Network access point id</assert> 
        </rule>
        
        <rule id="Destination" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110152']]">
        		<assert test="@UserID!=''">Destination UserID must be SOAP endpoint URI</assert>
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
       		    <assert test="ParticipantObjectDetail[@type='II']">Patient ParticipantObjectDetail type must be 'II'</assert>
        		<assert test="ParticipantObjectDetail[@value!='']">Patient ParticipantObjectDetail must message id value</assert>
        </rule>
    </pattern>
</schema>