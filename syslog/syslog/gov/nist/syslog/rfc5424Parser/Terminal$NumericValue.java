package gov.nist.syslog.rfc5424Parser;
/* -----------------------------------------------------------------------------
 * Terminal$NumericValue.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Tue Jul 26 10:55:44 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

import java.util.ArrayList;
import java.util.regex.Pattern;

public class Terminal$NumericValue extends Rule
{
  private Terminal$NumericValue(String spelling, ArrayList<Rule> rules)
  {
    super(spelling, rules);
  }

  public static Terminal$NumericValue parse(
    ParserContext context, 
    String spelling, 
    String regex, 
    int length)
  {
    context.push("NumericValue", spelling + "," + regex);

    boolean parsed = true;

    Terminal$NumericValue numericValue = null;
    try
    {
      String value = 
        context.text.substring(
          context.index, 
          context.index + length);
      

      if ((parsed = Pattern.matches(regex, value)))
      {
        context.index += length;
        numericValue = new Terminal$NumericValue(value, null);
      }
    }
    catch (IndexOutOfBoundsException e) {parsed = false;}

    context.pop("NumericValue", parsed);

    return numericValue;
  }

  public Object accept(Visitor visitor)
  {
    return visitor.visit(this);
  }
}
/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
