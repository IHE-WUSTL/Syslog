package gov.nist.syslog.rfc5424Parser;
/* -----------------------------------------------------------------------------
 * Displayer.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Tue Jul 26 10:55:44 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

import java.util.ArrayList;

public class Displayer implements Visitor
{

  public Object visit(Rule$SYSLOG_MSG rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$HEADER rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$PRI rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$PRIVAL rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$VERSION rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$HOSTNAME rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$APP_NAME rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$PROCID rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$MSGID rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$TIMESTAMP rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$FULL_DATE rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$DATE_FULLYEAR rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$DATE_MONTH rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$DATE_MDAY rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$FULL_TIME rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$PARTIAL_TIME rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$TIME_HOUR rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$TIME_MINUTE rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$TIME_SECOND rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$TIME_SECFRAC rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$TIME_OFFSET rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$TIME_NUMOFFSET rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$STRUCTURED_DATA rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$SD_ELEMENT rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$SD_PARAM rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$SD_ID rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$PARAM_NAME rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$PARAM_VALUE rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$SD_NAME rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$MSG rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$MSG_ANY rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$MSG_UTF8 rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$BOM rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$UTF_8_STRING rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$OCTET rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$SP rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$PRINTUSASCII rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$NONZERO_DIGIT rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$DIGIT rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$NILVALUE rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Terminal$StringValue value)
  {
    System.out.print(value.spelling);
    return null;
  }

  public Object visit(Terminal$NumericValue value)
  {
    System.out.print(value.spelling);
    return null;
  }

  private Object visitRules(ArrayList<Rule> rules)
  {
    for (Rule rule : rules)
      rule.accept(this);
    return null;
  }
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
