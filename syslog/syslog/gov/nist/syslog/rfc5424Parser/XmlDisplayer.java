package gov.nist.syslog.rfc5424Parser;
/* -----------------------------------------------------------------------------
 * XmlDisplayer.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Tue Jul 26 10:55:44 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

import java.util.ArrayList;

public class XmlDisplayer implements Visitor
{

  public Object visit(Rule$SYSLOG_MSG rule)
  {
    System.out.println("<SYSLOG-MSG>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</SYSLOG-MSG>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$HEADER rule)
  {
    System.out.println("<HEADER>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</HEADER>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$PRI rule)
  {
    System.out.println("<PRI>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</PRI>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$PRIVAL rule)
  {
    System.out.println("<PRIVAL>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</PRIVAL>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$VERSION rule)
  {
    System.out.println("<VERSION>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</VERSION>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$HOSTNAME rule)
  {
    System.out.println("<HOSTNAME>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</HOSTNAME>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$APP_NAME rule)
  {
    System.out.println("<APP-NAME>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</APP-NAME>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$PROCID rule)
  {
    System.out.println("<PROCID>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</PROCID>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$MSGID rule)
  {
    System.out.println("<MSGID>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</MSGID>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$TIMESTAMP rule)
  {
    System.out.println("<TIMESTAMP>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</TIMESTAMP>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$FULL_DATE rule)
  {
    System.out.println("<FULL-DATE>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</FULL-DATE>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$DATE_FULLYEAR rule)
  {
    System.out.println("<DATE-FULLYEAR>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</DATE-FULLYEAR>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$DATE_MONTH rule)
  {
    System.out.println("<DATE-MONTH>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</DATE-MONTH>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$DATE_MDAY rule)
  {
    System.out.println("<DATE-MDAY>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</DATE-MDAY>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$FULL_TIME rule)
  {
    System.out.println("<FULL-TIME>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</FULL-TIME>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$PARTIAL_TIME rule)
  {
    System.out.println("<PARTIAL-TIME>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</PARTIAL-TIME>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$TIME_HOUR rule)
  {
    System.out.println("<TIME-HOUR>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</TIME-HOUR>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$TIME_MINUTE rule)
  {
    System.out.println("<TIME-MINUTE>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</TIME-MINUTE>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$TIME_SECOND rule)
  {
    System.out.println("<TIME-SECOND>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</TIME-SECOND>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$TIME_SECFRAC rule)
  {
    System.out.println("<TIME-SECFRAC>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</TIME-SECFRAC>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$TIME_OFFSET rule)
  {
    System.out.println("<TIME-OFFSET>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</TIME-OFFSET>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$TIME_NUMOFFSET rule)
  {
    System.out.println("<TIME-NUMOFFSET>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</TIME-NUMOFFSET>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$STRUCTURED_DATA rule)
  {
    System.out.println("<STRUCTURED-DATA>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</STRUCTURED-DATA>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$SD_ELEMENT rule)
  {
    System.out.println("<SD-ELEMENT>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</SD-ELEMENT>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$SD_PARAM rule)
  {
    System.out.println("<SD-PARAM>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</SD-PARAM>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$SD_ID rule)
  {
    System.out.println("<SD-ID>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</SD-ID>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$PARAM_NAME rule)
  {
    System.out.println("<PARAM-NAME>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</PARAM-NAME>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$PARAM_VALUE rule)
  {
    System.out.println("<PARAM-VALUE>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</PARAM-VALUE>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$SD_NAME rule)
  {
    System.out.println("<SD-NAME>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</SD-NAME>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$MSG rule)
  {
    System.out.println("<MSG>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</MSG>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$MSG_ANY rule)
  {
    System.out.println("<MSG-ANY>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</MSG-ANY>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$MSG_UTF8 rule)
  {
    System.out.println("<MSG-UTF8>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</MSG-UTF8>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$BOM rule)
  {
    System.out.println("<BOM>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</BOM>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$UTF_8_STRING rule)
  {
    System.out.println("<UTF-8-STRING>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</UTF-8-STRING>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$OCTET rule)
  {
    System.out.println("<OCTET>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</OCTET>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$SP rule)
  {
    System.out.println("<SP>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</SP>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$PRINTUSASCII rule)
  {
    System.out.println("<PRINTUSASCII>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</PRINTUSASCII>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$NONZERO_DIGIT rule)
  {
    System.out.println("<NONZERO-DIGIT>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</NONZERO-DIGIT>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$DIGIT rule)
  {
    System.out.println("<DIGIT>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</DIGIT>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$NILVALUE rule)
  {
    System.out.println("<NILVALUE>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</NILVALUE>");

    return Boolean.FALSE;
  }

  public Object visit(Terminal$StringValue value)
  {
    System.out.print(value.spelling);
    return Boolean.TRUE;
  }

  public Object visit(Terminal$NumericValue value)
  {
    System.out.print(value.spelling);
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

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
