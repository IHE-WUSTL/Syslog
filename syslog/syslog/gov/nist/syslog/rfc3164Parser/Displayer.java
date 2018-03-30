/* -----------------------------------------------------------------------------
 * Displayer.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Wed Jul 27 12:21:32 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

package gov.nist.syslog.rfc3164Parser;

import java.util.ArrayList;

public class Displayer implements Visitor
{

  public Object visit(Rule$SYSLOG_3164 rule)
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

  public Object visit(Rule$HEADER rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$HOSTNAME rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$TIMESTAMP rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$MON rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$DD rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$HR rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$MIN rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$SEC rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$MSG rule)
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

  public Object visit(Rule$ZERO rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$ONE rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$TWO rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$ZERO_THREE rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$ZERO_FIVE rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$DIGIT rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$LESS_THAN rule)
  {
    return visitRules(rule.rules);
  }

  public Object visit(Rule$GREATER_THAN rule)
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
