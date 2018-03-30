/* -----------------------------------------------------------------------------
 * Parser.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Wed Jul 27 12:21:32 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

package gov.nist.syslog.rfc3164Parser;

import java.util.Stack;
import java.util.Properties;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;

public class Parser
{
  private Parser() {}

  static public void main(String[] args)
  {
    Properties arguments = new Properties();
    String error = "";
    boolean ok = args.length > 0;

    if (ok)
    {
      arguments.setProperty("Trace", "Off");
      arguments.setProperty("Rule", "SYSLOG-3164");

      for (int i = 0; i < args.length; i++)
      {
        if (args[i].equals("-trace"))
          arguments.setProperty("Trace", "On");
        else if (args[i].equals("-visitor"))
          arguments.setProperty("Visitor", args[++i]);
        else if (args[i].equals("-file"))
          arguments.setProperty("File", args[++i]);
        else if (args[i].equals("-string"))
          arguments.setProperty("String", args[++i]);
        else if (args[i].equals("-rule"))
          arguments.setProperty("Rule", args[++i]);
        else
        {
          error = "unknown argument: " + args[i];
          ok = false;
        }
      }
    }

    if (ok)
    {
      if (arguments.getProperty("File") == null &&
          arguments.getProperty("String") == null)
      {
        error = "insufficient arguments: -file or -string required";
        ok = false;
      }
    }

    if (!ok)
    {
      System.out.println("error: " + error);
      System.out.println("usage: Parser [-rule rulename] [-trace] <-file file | -string string> [-visitor visitor]");
    }
    else
    {
      try
      {
        Rule rule = null;

        if (arguments.getProperty("File") != null)
        {
          rule = 
            parse(
              arguments.getProperty("Rule"), 
              new File(arguments.getProperty("File")), 
              arguments.getProperty("Trace").equals("On"));
        }
        else if (arguments.getProperty("String") != null)
        {
          rule = 
            parse(
              arguments.getProperty("Rule"), 
              arguments.getProperty("String"), 
              arguments.getProperty("Trace").equals("On"));
        }

        if (arguments.getProperty("Visitor") != null)
        {
          Visitor visitor = 
            (Visitor)Class.forName(arguments.getProperty("Visitor")).newInstance();
          rule.accept(visitor);
        }
      }
      catch (IllegalArgumentException e)
      {
        System.out.println("argument error: " + e.getMessage());
      }
      catch (IOException e)
      {
        System.out.println("io error: " + e.getMessage());
      }
      catch (ParserException e)
      {
        System.out.println("parser error: " + e.getMessage());
      }
      catch (ClassNotFoundException e)
      {
        System.out.println("visitor error: class not found - " + e.getMessage());
      }
      catch (IllegalAccessException e)
      {
        System.out.println("visitor error: illegal access - " + e.getMessage());
      }
      catch (InstantiationException e)
      {
        System.out.println("visitor error: instantiation failure - " + e.getMessage());
      }
    }
  }

  static public Rule parse(String rulename, String string)
  throws IllegalArgumentException,
         ParserException
  {
    return parse(rulename, string, false);
  }

  static public Rule parse(String rulename, InputStream in)
  throws IllegalArgumentException,
         IOException,
         ParserException
  {
    return parse(rulename, in, false);
  }

  static public Rule parse(String rulename, File file)
  throws IllegalArgumentException,
         IOException,
         ParserException
  {
    return parse(rulename, file, false);
  }

  public static Rule parse(String rulename, String string, boolean trace)
  throws IllegalArgumentException,
         ParserException
  {
    if (rulename == null)
      throw new IllegalArgumentException("null rulename");
    if (string == null)
      throw new IllegalArgumentException("null string");

    ParserContext context = new ParserContext(string, trace);

    Rule rule = null;
    if (rulename.equalsIgnoreCase("SYSLOG-3164")) rule = Rule$SYSLOG_3164.parse(context);
    else if (rulename.equalsIgnoreCase("PRI")) rule = Rule$PRI.parse(context);
    else if (rulename.equalsIgnoreCase("PRIVAL")) rule = Rule$PRIVAL.parse(context);
    else if (rulename.equalsIgnoreCase("HEADER")) rule = Rule$HEADER.parse(context);
    else if (rulename.equalsIgnoreCase("HOSTNAME")) rule = Rule$HOSTNAME.parse(context);
    else if (rulename.equalsIgnoreCase("TIMESTAMP")) rule = Rule$TIMESTAMP.parse(context);
    else if (rulename.equalsIgnoreCase("MON")) rule = Rule$MON.parse(context);
    else if (rulename.equalsIgnoreCase("DD")) rule = Rule$DD.parse(context);
    else if (rulename.equalsIgnoreCase("HR")) rule = Rule$HR.parse(context);
    else if (rulename.equalsIgnoreCase("MIN")) rule = Rule$MIN.parse(context);
    else if (rulename.equalsIgnoreCase("SEC")) rule = Rule$SEC.parse(context);
    else if (rulename.equalsIgnoreCase("MSG")) rule = Rule$MSG.parse(context);
    else if (rulename.equalsIgnoreCase("OCTET")) rule = Rule$OCTET.parse(context);
    else if (rulename.equalsIgnoreCase("SP")) rule = Rule$SP.parse(context);
    else if (rulename.equalsIgnoreCase("PRINTUSASCII")) rule = Rule$PRINTUSASCII.parse(context);
    else if (rulename.equalsIgnoreCase("NONZERO-DIGIT")) rule = Rule$NONZERO_DIGIT.parse(context);
    else if (rulename.equalsIgnoreCase("ZERO")) rule = Rule$ZERO.parse(context);
    else if (rulename.equalsIgnoreCase("ONE")) rule = Rule$ONE.parse(context);
    else if (rulename.equalsIgnoreCase("TWO")) rule = Rule$TWO.parse(context);
    else if (rulename.equalsIgnoreCase("ZERO-THREE")) rule = Rule$ZERO_THREE.parse(context);
    else if (rulename.equalsIgnoreCase("ZERO-FIVE")) rule = Rule$ZERO_FIVE.parse(context);
    else if (rulename.equalsIgnoreCase("DIGIT")) rule = Rule$DIGIT.parse(context);
    else if (rulename.equalsIgnoreCase("LESS-THAN")) rule = Rule$LESS_THAN.parse(context);
    else if (rulename.equalsIgnoreCase("GREATER-THAN")) rule = Rule$GREATER_THAN.parse(context);
    else throw new IllegalArgumentException("unknown rule");

    if (rule == null)
    {
      throw new ParserException(
        "rule \"" + (String)context.getErrorStack().peek() + "\" failed",
        context.text,
        context.getErrorIndex(),
        context.getErrorStack());
    }

    if (context.text.length() > context.index)
    {
      throw new ParserException(
        "extra data found",
        context.text,
        context.index,
        new Stack<String>());
    }

    return rule;
  }

  static private Rule parse(String rulename, InputStream in, boolean trace)
  throws IllegalArgumentException,
         IOException,
         ParserException
  {
    if (rulename == null)
      throw new IllegalArgumentException("null rulename");
    if (in == null)
      throw new IllegalArgumentException("null input stream");

    int ch = 0;
    StringBuffer out = new StringBuffer();
    while ((ch = in.read()) != -1)
      out.append((char)ch);

    return parse(rulename, out.toString(), trace);
  }

  static private Rule parse(String rulename, File file, boolean trace)
  throws IllegalArgumentException,
         IOException,
         ParserException
  {
    if (rulename == null)
      throw new IllegalArgumentException("null rulename");
    if (file == null)
      throw new IllegalArgumentException("null file");

    BufferedReader in = new BufferedReader(new FileReader(file));
    int ch = 0;
    StringBuffer out = new StringBuffer();
    while ((ch = in.read()) != -1)
      out.append((char)ch);

    in.close();

    return parse(rulename, out.toString(), trace);
  }
}

/* -----------------------------------------------------------------------------
 * eof
 * -----------------------------------------------------------------------------
 */
