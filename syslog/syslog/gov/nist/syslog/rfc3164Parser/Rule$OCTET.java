/* -----------------------------------------------------------------------------
 * Rule$OCTET.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Wed Jul 27 12:21:32 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

package gov.nist.syslog.rfc3164Parser;

import java.util.ArrayList;

final public class Rule$OCTET extends Rule
{
  private Rule$OCTET(String spelling, ArrayList<Rule> rules)
  {
    super(spelling, rules);
  }

  public Object accept(Visitor visitor)
  {
    return visitor.visit(this);
  }

  public static Rule$OCTET parse(ParserContext context)
  {
    context.push("OCTET");

    boolean parsed = true;
    int s0 = context.index;
    ArrayList<Rule> e0 = new ArrayList<Rule>();
    Rule rule;

    parsed = false;
    if (!parsed)
    {
      {
        ArrayList<Rule> e1 = new ArrayList<Rule>();
        int s1 = context.index;
        parsed = true;
        if (parsed)
        {
          boolean f1 = true;
          int c1 = 0;
          for (int i1 = 0; i1 < 1 && f1; i1++)
          {
            rule = Terminal$NumericValue.parse(context, "%d00-255", "[\\x00-\\xff]", 1);
            if ((f1 = rule != null))
            {
              e1.add(rule);
              c1++;
            }
          }
          parsed = c1 == 1;
        }
        if (parsed)
          e0.addAll(e1);
        else
          context.index = s1;
      }
    }

    rule = null;
    if (parsed)
      rule = new Rule$OCTET(context.text.substring(s0, context.index), e0);
    else
      context.index = s0;

    context.pop("OCTET", parsed);

    return (Rule$OCTET)rule;
  }
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
