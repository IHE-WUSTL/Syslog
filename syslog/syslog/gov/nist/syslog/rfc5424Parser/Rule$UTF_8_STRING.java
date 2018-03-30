package gov.nist.syslog.rfc5424Parser;
/* -----------------------------------------------------------------------------
 * Rule$UTF_8_STRING.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Tue Jul 26 10:55:44 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

import java.util.ArrayList;

final public class Rule$UTF_8_STRING extends Rule
{
  private Rule$UTF_8_STRING(String spelling, ArrayList<Rule> rules)
  {
    super(spelling, rules);
  }

  public Object accept(Visitor visitor)
  {
    return visitor.visit(this);
  }

  public static Rule$UTF_8_STRING parse(ParserContext context)
  {
    context.push("UTF-8-STRING");

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
          @SuppressWarnings("unused")
		int c1 = 0;
          while (f1)
          {
            rule = Rule$OCTET.parse(context);
            if ((f1 = rule != null))
            {
              e1.add(rule);
              c1++;
            }
          }
          parsed = true;
        }
        if (parsed)
          e0.addAll(e1);
        else
          context.index = s1;
      }
    }

    rule = null;
    if (parsed)
      rule = new Rule$UTF_8_STRING(context.text.substring(s0, context.index), e0);
    else
      context.index = s0;

    context.pop("UTF-8-STRING", parsed);

    return (Rule$UTF_8_STRING)rule;
  }
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
