<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   
   <xsl:template match="node()|@*">
     <xsl:copy>
        <xsl:apply-templates select="node()|@*"/>
     </xsl:copy>
   </xsl:template>
   
   <xsl:template match="@test">
      <xsl:choose>
        <xsl:when test="contains(current(), '@csd-code=')">
           <xsl:attribute name="test">
              <xsl:value-of select="concat(substring-before(current(), '@csd-code='), '@code=', substring-after(current(), '@csd-code='))"/>
           </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
           <xsl:attribute name="test">
              <xsl:value-of  select="current()"/>
           </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   
   <xsl:template match="@contex">
      <xsl:choose>
        <xsl:when test="contains(current(), '@csd-code=')">
           <xsl:attribute name="contex">
              <xsl:value-of select="concat(substring-before(current(), '@csd-code='), '@code=', substring-after(current(), '@csd-code='))"/>
           </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
           <xsl:attribute name="contex">
              <xsl:value-of  select="current()"/>
           </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

      
</xsl:stylesheet>