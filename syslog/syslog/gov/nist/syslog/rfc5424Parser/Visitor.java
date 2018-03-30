package gov.nist.syslog.rfc5424Parser;
/* -----------------------------------------------------------------------------
 * Visitor.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Tue Jul 26 10:55:44 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

public interface Visitor
{
  public Object visit(Rule$SYSLOG_MSG rule);
  public Object visit(Rule$HEADER rule);
  public Object visit(Rule$PRI rule);
  public Object visit(Rule$PRIVAL rule);
  public Object visit(Rule$VERSION rule);
  public Object visit(Rule$HOSTNAME rule);
  public Object visit(Rule$APP_NAME rule);
  public Object visit(Rule$PROCID rule);
  public Object visit(Rule$MSGID rule);
  public Object visit(Rule$TIMESTAMP rule);
  public Object visit(Rule$FULL_DATE rule);
  public Object visit(Rule$DATE_FULLYEAR rule);
  public Object visit(Rule$DATE_MONTH rule);
  public Object visit(Rule$DATE_MDAY rule);
  public Object visit(Rule$FULL_TIME rule);
  public Object visit(Rule$PARTIAL_TIME rule);
  public Object visit(Rule$TIME_HOUR rule);
  public Object visit(Rule$TIME_MINUTE rule);
  public Object visit(Rule$TIME_SECOND rule);
  public Object visit(Rule$TIME_SECFRAC rule);
  public Object visit(Rule$TIME_OFFSET rule);
  public Object visit(Rule$TIME_NUMOFFSET rule);
  public Object visit(Rule$STRUCTURED_DATA rule);
  public Object visit(Rule$SD_ELEMENT rule);
  public Object visit(Rule$SD_PARAM rule);
  public Object visit(Rule$SD_ID rule);
  public Object visit(Rule$PARAM_NAME rule);
  public Object visit(Rule$PARAM_VALUE rule);
  public Object visit(Rule$SD_NAME rule);
  public Object visit(Rule$MSG rule);
  public Object visit(Rule$MSG_ANY rule);
  public Object visit(Rule$MSG_UTF8 rule);
  public Object visit(Rule$BOM rule);
  public Object visit(Rule$UTF_8_STRING rule);
  public Object visit(Rule$OCTET rule);
  public Object visit(Rule$SP rule);
  public Object visit(Rule$PRINTUSASCII rule);
  public Object visit(Rule$NONZERO_DIGIT rule);
  public Object visit(Rule$DIGIT rule);
  public Object visit(Rule$NILVALUE rule);

  public Object visit(Terminal$StringValue value);
  public Object visit(Terminal$NumericValue value);
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
