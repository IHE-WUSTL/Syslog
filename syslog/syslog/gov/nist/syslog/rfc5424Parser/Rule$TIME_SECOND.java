package gov.nist.syslog.rfc5424Parser;
/* -----------------------------------------------------------------------------
 * Rule$TIME_SECOND.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Tue Jul 26 10:55:44 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

import java.util.ArrayList;

final public class Rule$TIME_SECOND extends Rule
{
  private Rule$TIME_SECOND(String spelling, ArrayList<Rule> rules)
  {
    super(spelling, rules);
  }

  public Object accept(Visitor visitor)
  {
    return visitor.visit(this);
  }

  public static Rule$TIME_SECOND parse(ParserContext context)
  {
    context.push("TIME-SECOND");

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
          for (int i1 = 0; i1 < 2 && f1; i1++)
          {
            rule = Rule$DIGIT.parse(context);
            if ((f1 = rule != null))
            {
              e1.add(rule);
              c1++;
            }
          }
          parsed = c1 == 2;
        }
        if (parsed)
          e0.addAll(e1);
        else
          context.index = s1;
      }
    }

    rule = null;
    if (parsed)
      rule = new Rule$TIME_SECOND(context.text.substring(s0, context.index), e0);
    else
      context.index = s0;

    context.pop("TIME-SECOND", parsed);

    return (Rule$TIME_SECOND)rule;
  }
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
