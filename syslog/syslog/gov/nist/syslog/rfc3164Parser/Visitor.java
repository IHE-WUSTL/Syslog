/* -----------------------------------------------------------------------------
 * Visitor.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Wed Jul 27 12:21:32 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

package gov.nist.syslog.rfc3164Parser;

public interface Visitor
{
  public Object visit(Rule$SYSLOG_3164 rule);
  public Object visit(Rule$PRI rule);
  public Object visit(Rule$PRIVAL rule);
  public Object visit(Rule$HEADER rule);
  public Object visit(Rule$HOSTNAME rule);
  public Object visit(Rule$TIMESTAMP rule);
  public Object visit(Rule$MON rule);
  public Object visit(Rule$DD rule);
  public Object visit(Rule$HR rule);
  public Object visit(Rule$MIN rule);
  public Object visit(Rule$SEC rule);
  public Object visit(Rule$MSG rule);
  public Object visit(Rule$OCTET rule);
  public Object visit(Rule$SP rule);
  public Object visit(Rule$PRINTUSASCII rule);
  public Object visit(Rule$NONZERO_DIGIT rule);
  public Object visit(Rule$ZERO rule);
  public Object visit(Rule$ONE rule);
  public Object visit(Rule$TWO rule);
  public Object visit(Rule$ZERO_THREE rule);
  public Object visit(Rule$ZERO_FIVE rule);
  public Object visit(Rule$DIGIT rule);
  public Object visit(Rule$LESS_THAN rule);
  public Object visit(Rule$GREATER_THAN rule);

  public Object visit(Terminal$StringValue value);
  public Object visit(Terminal$NumericValue value);
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
