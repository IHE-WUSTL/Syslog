<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<axsl:stylesheet xmlns:axsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:iso="http://purl.oclc.org/dsdl/schematron" xmlns:schold="http://www.ascc.net/xml/schematron" version="2.0">
<!--Implementers: please note that overriding process-prolog or process-root is 
    the preferred method for meta-stylesheets to use where possible. -->
<axsl:param name="archiveDirParameter"/>
<axsl:param name="archiveNameParameter"/>
<axsl:param name="fileNameParameter"/>
<axsl:param name="fileDirParameter"/>
<axsl:variable name="document-uri">
<axsl:value-of select="document-uri(/)"/>
</axsl:variable>

<!--PHASES-->


<!--PROLOG-->
<axsl:output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" indent="yes" standalone="yes" omit-xml-declaration="no" method="xml"/>

<!--XSD TYPES FOR XSLT2-->


<!--KEYS AND FUNCTIONS-->


<!--DEFAULT RULES-->


<!--MODE: SCHEMATRON-SELECT-FULL-PATH-->
<!--This mode can be used to generate an ugly though full XPath for locators-->
<axsl:template mode="schematron-select-full-path" match="*">
<axsl:apply-templates mode="schematron-get-full-path" select="."/>
</axsl:template>

<!--MODE: SCHEMATRON-FULL-PATH-->
<!--This mode can be used to generate an ugly though full XPath for locators-->
<axsl:template mode="schematron-get-full-path" match="*">
<axsl:apply-templates mode="schematron-get-full-path" select="parent::*"/>
<axsl:text>/</axsl:text>
<axsl:choose>
<axsl:when test="namespace-uri()=''">
<axsl:value-of select="name()"/>
<axsl:variable select="1+    count(preceding-sibling::*[name()=name(current())])" name="p_1"/>
<axsl:if test="$p_1&gt;1 or following-sibling::*[name()=name(current())]">[<axsl:value-of select="$p_1"/>]</axsl:if>
</axsl:when>
<axsl:otherwise>
<axsl:text>*[local-name()='</axsl:text>
<axsl:value-of select="local-name()"/>
<axsl:text>']</axsl:text>
<axsl:variable select="1+   count(preceding-sibling::*[local-name()=local-name(current())])" name="p_2"/>
<axsl:if test="$p_2&gt;1 or following-sibling::*[local-name()=local-name(current())]">[<axsl:value-of select="$p_2"/>]</axsl:if>
</axsl:otherwise>
</axsl:choose>
</axsl:template>
<axsl:template mode="schematron-get-full-path" match="@*">
<axsl:text>/</axsl:text>
<axsl:choose>
<axsl:when test="namespace-uri()=''">@<axsl:value-of select="name()"/>
</axsl:when>
<axsl:otherwise>
<axsl:text>@*[local-name()='</axsl:text>
<axsl:value-of select="local-name()"/>
<axsl:text>' and namespace-uri()='</axsl:text>
<axsl:value-of select="namespace-uri()"/>
<axsl:text>']</axsl:text>
</axsl:otherwise>
</axsl:choose>
</axsl:template>

<!--MODE: SCHEMATRON-FULL-PATH-2-->
<!--This mode can be used to generate prefixed XPath for humans-->
<axsl:template mode="schematron-get-full-path-2" match="node() | @*">
<axsl:for-each select="ancestor-or-self::*">
<axsl:text>/</axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:if test="preceding-sibling::*[name(.)=name(current())]">
<axsl:text>[</axsl:text>
<axsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
<axsl:text>]</axsl:text>
</axsl:if>
</axsl:for-each>
<axsl:if test="not(self::*)">
<axsl:text/>/@<axsl:value-of select="name(.)"/>
</axsl:if>
</axsl:template>
<!--MODE: SCHEMATRON-FULL-PATH-3-->
<!--This mode can be used to generate prefixed XPath for humans 
	(Top-level element has index)-->
<axsl:template mode="schematron-get-full-path-3" match="node() | @*">
<axsl:for-each select="ancestor-or-self::*">
<axsl:text>/</axsl:text>
<axsl:value-of select="name(.)"/>
<axsl:if test="parent::*">
<axsl:text>[</axsl:text>
<axsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
<axsl:text>]</axsl:text>
</axsl:if>
</axsl:for-each>
<axsl:if test="not(self::*)">
<axsl:text/>/@<axsl:value-of select="name(.)"/>
</axsl:if>
</axsl:template>

<!--MODE: GENERATE-ID-FROM-PATH -->
<axsl:template mode="generate-id-from-path" match="/"/>
<axsl:template mode="generate-id-from-path" match="text()">
<axsl:apply-templates mode="generate-id-from-path" select="parent::*"/>
<axsl:value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')"/>
</axsl:template>
<axsl:template mode="generate-id-from-path" match="comment()">
<axsl:apply-templates mode="generate-id-from-path" select="parent::*"/>
<axsl:value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')"/>
</axsl:template>
<axsl:template mode="generate-id-from-path" match="processing-instruction()">
<axsl:apply-templates mode="generate-id-from-path" select="parent::*"/>
<axsl:value-of select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')"/>
</axsl:template>
<axsl:template mode="generate-id-from-path" match="@*">
<axsl:apply-templates mode="generate-id-from-path" select="parent::*"/>
<axsl:value-of select="concat('.@', name())"/>
</axsl:template>
<axsl:template priority="-0.5" mode="generate-id-from-path" match="*">
<axsl:apply-templates mode="generate-id-from-path" select="parent::*"/>
<axsl:text>.</axsl:text>
<axsl:value-of select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')"/>
</axsl:template>

<!--MODE: GENERATE-ID-2 -->
<axsl:template mode="generate-id-2" match="/">U</axsl:template>
<axsl:template priority="2" mode="generate-id-2" match="*">
<axsl:text>U</axsl:text>
<axsl:number count="*" level="multiple"/>
</axsl:template>
<axsl:template mode="generate-id-2" match="node()">
<axsl:text>U.</axsl:text>
<axsl:number count="*" level="multiple"/>
<axsl:text>n</axsl:text>
<axsl:number count="node()"/>
</axsl:template>
<axsl:template mode="generate-id-2" match="@*">
<axsl:text>U.</axsl:text>
<axsl:number count="*" level="multiple"/>
<axsl:text>_</axsl:text>
<axsl:value-of select="string-length(local-name(.))"/>
<axsl:text>_</axsl:text>
<axsl:value-of select="translate(name(),':','.')"/>
</axsl:template>
<!--Strip characters-->
<axsl:template priority="-1" match="text()"/>

<!--SCHEMA SETUP-->
<axsl:template match="/">
<svrl:schematron-output xmlns:svrl="http://purl.oclc.org/dsdl/svrl" schemaVersion="" title="Schematron rules for 110122/110114 (Login) ITI TF-2a R11 3.2.6">
<axsl:comment>
<axsl:value-of select="$archiveDirParameter"/>   
		 <axsl:value-of select="$archiveNameParameter"/>  
		 <axsl:value-of select="$fileNameParameter"/>  
		 <axsl:value-of select="$fileDirParameter"/>
</axsl:comment>
<svrl:active-pattern>
<axsl:attribute name="document">
<axsl:value-of select="document-uri(/)"/>
</axsl:attribute>
<axsl:apply-templates/>
</svrl:active-pattern>
<axsl:apply-templates mode="M1" select="/"/>
</svrl:schematron-output>
</axsl:template>

<!--SCHEMATRON PATTERNS-->
<svrl:text xmlns:svrl="http://purl.oclc.org/dsdl/svrl">Schematron rules for 110122/110114 (Login) ITI TF-2a R11 3.2.6</svrl:text>

<!--PATTERN -->


	<!--RULE EventIdentification-->
<axsl:template mode="M1" priority="1002" match="/AuditMessage/EventIdentification">
<svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="/AuditMessage/EventIdentification" id="EventIdentification"/>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="EventID[@code='110114']"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="EventID[@code='110114']">
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>EventID@code must be '110114'</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="EventID[@codeSystemName='DCM']"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="EventID[@codeSystemName='DCM']">
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>EventID@codeSystemName	must be 'DCM'</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="@EventActionCode='E'"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@EventActionCode='E'">
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>EventActionCode must be 'E' (Execute)</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="EventTypeCode[@code='110122']"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="EventTypeCode[@code='110122']">
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>EventTypeCode@code must be '110122'</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="EventTypeCode[@codeSystemName='DCM']"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="EventTypeCode[@codeSystemName='DCM']">
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>EventTypeCode@codeSystemName must be 'DCM'</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M1" select="*|comment()|processing-instruction()"/>
</axsl:template>

	<!--RULE top level elements-->
<axsl:template mode="M1" priority="1001" match="/AuditMessage">
<svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="/AuditMessage" id="top level elements"/>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="count(ActiveParticipant[RoleIDCode[@code='110150']])=1"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ActiveParticipant[RoleIDCode[@code='110150']])=1">
<axsl:attribute name="id">SourceElement</axsl:attribute>
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>Must contain one Source (RoleIDCode = 110150)</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="count(ActiveParticipant[RoleIDCode[@code='110152']])=0"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ActiveParticipant[RoleIDCode[@code='110152']])=0">
<axsl:attribute name="id">DestinationElement</axsl:attribute>
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>May not contain Destination (RoleIDCode = 110152)</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="count(AuditSourceIdentification)=1"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(AuditSourceIdentification)=1">
<axsl:attribute name="id">AuditSourceElement</axsl:attribute>
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>Must contain one AuditSourceIdentification</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="count(ParticipantObjectIdentification)=0"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="count(ParticipantObjectIdentification)=0">
<axsl:attribute name="id">ParticipantObjectElement</axsl:attribute>
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>May not contain ParticipantObject</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M1" select="*|comment()|processing-instruction()"/>
</axsl:template>

	<!--RULE Source-->
<axsl:template mode="M1" priority="1000" match="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110150']]">
<svrl:fired-rule xmlns:svrl="http://purl.oclc.org/dsdl/svrl" context="/AuditMessage/ActiveParticipant[RoleIDCode[@code='110150']]" id="Source"/>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="@UserID!=''"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@UserID!=''">
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>Source must have UserID (process id)</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="RoleIDCode[@codeSystemName='DCM']"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="RoleIDCode[@codeSystemName='DCM']">
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>Source Role ID code system must be DCM</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@NetworkAccessPointTypeCode='1' or @NetworkAccessPointTypeCode='2'">
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>Source Network access type code must be 1 or 2</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>

		<!--ASSERT -->
<axsl:choose>
<axsl:when test="@NetworkAccessPointID!=''"/>
<axsl:otherwise>
<svrl:failed-assert xmlns:svrl="http://purl.oclc.org/dsdl/svrl" test="@NetworkAccessPointID!=''">
<axsl:attribute name="location">
<axsl:apply-templates mode="schematron-select-full-path" select="."/>
</axsl:attribute>
<svrl:text>Source must have Network access point id</svrl:text>
</svrl:failed-assert>
</axsl:otherwise>
</axsl:choose>
<axsl:apply-templates mode="M1" select="*|comment()|processing-instruction()"/>
</axsl:template>
<axsl:template mode="M1" priority="-1" match="text()"/>
<axsl:template mode="M1" priority="-2" match="@*|node()">
<axsl:apply-templates mode="M1" select="*|comment()|processing-instruction()"/>
</axsl:template>
</axsl:stylesheet>