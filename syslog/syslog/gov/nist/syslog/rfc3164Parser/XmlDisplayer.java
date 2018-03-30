/* -----------------------------------------------------------------------------
 * XmlDisplayer.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Wed Jul 27 12:21:32 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

package gov.nist.syslog.rfc3164Parser;

import java.util.ArrayList;

public class XmlDisplayer implements Visitor
{

  public Object visit(Rule$SYSLOG_3164 rule)
  {
    System.out.println("<SYSLOG-3164>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</SYSLOG-3164>");

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

  public Object visit(Rule$HEADER rule)
  {
    System.out.println("<HEADER>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</HEADER>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$HOSTNAME rule)
  {
    System.out.println("<HOSTNAME>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</HOSTNAME>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$TIMESTAMP rule)
  {
    System.out.println("<TIMESTAMP>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</TIMESTAMP>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$MON rule)
  {
    System.out.println("<MON>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</MON>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$DD rule)
  {
    System.out.println("<DD>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</DD>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$HR rule)
  {
    System.out.println("<HR>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</HR>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$MIN rule)
  {
    System.out.println("<MIN>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</MIN>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$SEC rule)
  {
    System.out.println("<SEC>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</SEC>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$MSG rule)
  {
    System.out.println("<MSG>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</MSG>");

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

  public Object visit(Rule$ZERO rule)
  {
    System.out.println("<ZERO>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</ZERO>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$ONE rule)
  {
    System.out.println("<ONE>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</ONE>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$TWO rule)
  {
    System.out.println("<TWO>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</TWO>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$ZERO_THREE rule)
  {
    System.out.println("<ZERO-THREE>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</ZERO-THREE>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$ZERO_FIVE rule)
  {
    System.out.println("<ZERO-FIVE>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</ZERO-FIVE>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$DIGIT rule)
  {
    System.out.println("<DIGIT>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</DIGIT>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$LESS_THAN rule)
  {
    System.out.println("<LESS-THAN>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</LESS-THAN>");

    return Boolean.FALSE;
  }

  public Object visit(Rule$GREATER_THAN rule)
  {
    System.out.println("<GREATER-THAN>");
    if (visitRules(rule.rules).booleanValue()) System.out.println("");
    System.out.println("</GREATER-THAN>");

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
