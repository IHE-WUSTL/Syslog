package gov.nist.syslog.rfc5424Parser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

public class XMLConvert implements Visitor {
   
   StringBuilder xml;
   Rule rule;
   DocumentBuilder parser = null; 

   
   public XMLConvert(Rule rule) {
      this.rule = rule;
      try {
         parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      } catch (ParserConfigurationException e) { }
   };
   
   public String xmlString() {
      xml = new StringBuilder();
      rule.accept(this);
      return xml.toString();
   }
   
   public Document xmlDocument() throws Exception {
      xml = new StringBuilder();
      rule.accept(this);
      InputStream is = new ByteArrayInputStream(xml.toString().getBytes());
      return parser.parse(is);
   }

   public Object visit(Rule$SYSLOG_MSG rule)
   {
     xml.append("<SYSLOG-MSG>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</SYSLOG-MSG>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$HEADER rule)
   {
     xml.append("<HEADER>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</HEADER>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$PRI rule)
   {
     xml.append("<PRI>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</PRI>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$PRIVAL rule)
   {
     xml.append("<PRIVAL>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</PRIVAL>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$VERSION rule)
   {
     xml.append("<VERSION>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</VERSION>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$HOSTNAME rule)
   {
     xml.append("<HOSTNAME>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</HOSTNAME>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$APP_NAME rule)
   {
     xml.append("<APP-NAME>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</APP-NAME>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$PROCID rule)
   {
     xml.append("<PROCID>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</PROCID>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$MSGID rule)
   {
     xml.append("<MSGID>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</MSGID>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$TIMESTAMP rule)
   {
     xml.append("<TIMESTAMP>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</TIMESTAMP>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$FULL_DATE rule)
   {
     xml.append("<FULL-DATE>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</FULL-DATE>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$DATE_FULLYEAR rule)
   {
     xml.append("<DATE-FULLYEAR>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</DATE-FULLYEAR>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$DATE_MONTH rule)
   {
     xml.append("<DATE-MONTH>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</DATE-MONTH>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$DATE_MDAY rule)
   {
     xml.append("<DATE-MDAY>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</DATE-MDAY>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$FULL_TIME rule)
   {
     xml.append("<FULL-TIME>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</FULL-TIME>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$PARTIAL_TIME rule)
   {
     xml.append("<PARTIAL-TIME>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</PARTIAL-TIME>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$TIME_HOUR rule)
   {
     xml.append("<TIME-HOUR>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</TIME-HOUR>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$TIME_MINUTE rule)
   {
     xml.append("<TIME-MINUTE>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</TIME-MINUTE>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$TIME_SECOND rule)
   {
     xml.append("<TIME-SECOND>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</TIME-SECOND>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$TIME_SECFRAC rule)
   {
     xml.append("<TIME-SECFRAC>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</TIME-SECFRAC>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$TIME_OFFSET rule)
   {
     xml.append("<TIME-OFFSET>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</TIME-OFFSET>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$TIME_NUMOFFSET rule)
   {
     xml.append("<TIME-NUMOFFSET>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</TIME-NUMOFFSET>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$STRUCTURED_DATA rule)
   {
     xml.append("<STRUCTURED-DATA>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</STRUCTURED-DATA>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$SD_ELEMENT rule)
   {
     xml.append("<SD-ELEMENT>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</SD-ELEMENT>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$SD_PARAM rule)
   {
     xml.append("<SD-PARAM>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</SD-PARAM>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$SD_ID rule)
   {
     xml.append("<SD-ID>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</SD-ID>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$PARAM_NAME rule)
   {
     xml.append("<PARAM-NAME>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</PARAM-NAME>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$PARAM_VALUE rule)
   {
     xml.append("<PARAM-VALUE>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</PARAM-VALUE>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$SD_NAME rule)
   {
     xml.append("<SD-NAME>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</SD-NAME>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$MSG rule)
   {
     xml.append("<MSG>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</MSG>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$MSG_ANY rule)
   {
     xml.append("<MSG-ANY>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</MSG-ANY>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$MSG_UTF8 rule)
   {
     xml.append("<MSG-UTF8>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</MSG-UTF8>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$BOM rule)
   {
     xml.append("<BOM>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</BOM>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$UTF_8_STRING rule)
   {
     xml.append("<UTF-8-STRING>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</UTF-8-STRING>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$OCTET rule)
   {
     xml.append("<OCTET>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</OCTET>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$SP rule)
   {
     xml.append("<SP>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</SP>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$PRINTUSASCII rule)
   {
     xml.append("<PRINTUSASCII>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</PRINTUSASCII>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$NONZERO_DIGIT rule)
   {
     xml.append("<NONZERO-DIGIT>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</NONZERO-DIGIT>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$DIGIT rule)
   {
     xml.append("<DIGIT>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</DIGIT>");

     return Boolean.FALSE;
   }

   public Object visit(Rule$NILVALUE rule)
   {
     xml.append("<NILVALUE>");
     if (visitRules(rule.rules).booleanValue()) xml.append("");
     xml.append("</NILVALUE>");

     return Boolean.FALSE;
   }

   public Object visit(Terminal$StringValue value)
   {
     xml.append(value.spelling);
     return Boolean.TRUE;
   }

   public Object visit(Terminal$NumericValue value)
   {
     xml.append(value.spelling);
     return Boolean.TRUE;
   }

   private Boolean visitRules(ArrayList<Rule> rules)
   {
     Boolean terminal = Boolean.FALSE;
     for (Rule rule : rules)
       terminal = (Boolean)rule.accept(this);
     return terminal;
   }
 }

