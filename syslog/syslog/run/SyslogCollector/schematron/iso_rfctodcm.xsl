<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:template match="node()|@*">
	  <xsl:copy>
	     <xsl:apply-templates select="node()|@*"/>
	  </xsl:copy>
	</xsl:template>
	
	<xsl:template match="@test">
	   <xsl:choose>
	     <xsl:when test="contains(current(), '@code=')">
           <xsl:attribute name="test">
              <xsl:value-of select="concat(substring-before(current(), '@code='), '@csd-code=', substring-after(current(), '@code='))"/>
           </xsl:attribute>
	     </xsl:when>
	     <xsl:otherwise>
           <xsl:attribute name="test">
              <xsl:value-of  select="current()"/>
           </xsl:attribute>
	     </xsl:otherwise>
	   </xsl:choose>
	</xsl:template>
   
   <xsl:template match="@context">
      <xsl:choose>
        <xsl:when test="contains(current(), '@code=')">
           <xsl:attribute name="context">
              <xsl:value-of select="concat(substring-before(current(), '@code='), '@csd-code=', substring-after(current(), '@code='))"/>
           </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
           <xsl:attribute name="context">
              <xsl:value-of  select="current()"/>
           </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

		
</xsl:stylesheet>