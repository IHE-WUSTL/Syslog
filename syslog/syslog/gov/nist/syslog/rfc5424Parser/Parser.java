package gov.nist.syslog.rfc5424Parser;
/* -----------------------------------------------------------------------------
 * Parser.java
 * -----------------------------------------------------------------------------
 *
 * Producer : com.parse2.aparse.Parser 2.0
 * Produced : Tue Jul 26 10:55:44 CDT 2011
 *
 * -----------------------------------------------------------------------------
 */

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
      arguments.setProperty("Rule", "SYSLOG-MSG");

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

  static public Rule parse(String rulename, String string, boolean trace)
  throws IllegalArgumentException,
         ParserException
  {
    if (rulename == null)
      throw new IllegalArgumentException("null rulename");
    if (string == null)
      throw new IllegalArgumentException("null string");

    ParserContext context = new ParserContext(string, trace);

    Rule rule = null;
    if (rulename.equalsIgnoreCase("SYSLOG-MSG")) rule = Rule$SYSLOG_MSG.parse(context);
    else if (rulename.equalsIgnoreCase("HEADER")) rule = Rule$HEADER.parse(context);
    else if (rulename.equalsIgnoreCase("PRI")) rule = Rule$PRI.parse(context);
    else if (rulename.equalsIgnoreCase("PRIVAL")) rule = Rule$PRIVAL.parse(context);
    else if (rulename.equalsIgnoreCase("VERSION")) rule = Rule$VERSION.parse(context);
    else if (rulename.equalsIgnoreCase("HOSTNAME")) rule = Rule$HOSTNAME.parse(context);
    else if (rulename.equalsIgnoreCase("APP-NAME")) rule = Rule$APP_NAME.parse(context);
    else if (rulename.equalsIgnoreCase("PROCID")) rule = Rule$PROCID.parse(context);
    else if (rulename.equalsIgnoreCase("MSGID")) rule = Rule$MSGID.parse(context);
    else if (rulename.equalsIgnoreCase("TIMESTAMP")) rule = Rule$TIMESTAMP.parse(context);
    else if (rulename.equalsIgnoreCase("FULL-DATE")) rule = Rule$FULL_DATE.parse(context);
    else if (rulename.equalsIgnoreCase("DATE-FULLYEAR")) rule = Rule$DATE_FULLYEAR.parse(context);
    else if (rulename.equalsIgnoreCase("DATE-MONTH")) rule = Rule$DATE_MONTH.parse(context);
    else if (rulename.equalsIgnoreCase("DATE-MDAY")) rule = Rule$DATE_MDAY.parse(context);
    else if (rulename.equalsIgnoreCase("FULL-TIME")) rule = Rule$FULL_TIME.parse(context);
    else if (rulename.equalsIgnoreCase("PARTIAL-TIME")) rule = Rule$PARTIAL_TIME.parse(context);
    else if (rulename.equalsIgnoreCase("TIME-HOUR")) rule = Rule$TIME_HOUR.parse(context);
    else if (rulename.equalsIgnoreCase("TIME-MINUTE")) rule = Rule$TIME_MINUTE.parse(context);
    else if (rulename.equalsIgnoreCase("TIME-SECOND")) rule = Rule$TIME_SECOND.parse(context);
    else if (rulename.equalsIgnoreCase("TIME-SECFRAC")) rule = Rule$TIME_SECFRAC.parse(context);
    else if (rulename.equalsIgnoreCase("TIME-OFFSET")) rule = Rule$TIME_OFFSET.parse(context);
    else if (rulename.equalsIgnoreCase("TIME-NUMOFFSET")) rule = Rule$TIME_NUMOFFSET.parse(context);
    else if (rulename.equalsIgnoreCase("STRUCTURED-DATA")) rule = Rule$STRUCTURED_DATA.parse(context);
    else if (rulename.equalsIgnoreCase("SD-ELEMENT")) rule = Rule$SD_ELEMENT.parse(context);
    else if (rulename.equalsIgnoreCase("SD-PARAM")) rule = Rule$SD_PARAM.parse(context);
    else if (rulename.equalsIgnoreCase("SD-ID")) rule = Rule$SD_ID.parse(context);
    else if (rulename.equalsIgnoreCase("PARAM-NAME")) rule = Rule$PARAM_NAME.parse(context);
    else if (rulename.equalsIgnoreCase("PARAM-VALUE")) rule = Rule$PARAM_VALUE.parse(context);
    else if (rulename.equalsIgnoreCase("SD-NAME")) rule = Rule$SD_NAME.parse(context);
    else if (rulename.equalsIgnoreCase("MSG")) rule = Rule$MSG.parse(context);
    else if (rulename.equalsIgnoreCase("MSG-ANY")) rule = Rule$MSG_ANY.parse(context);
    else if (rulename.equalsIgnoreCase("MSG-UTF8")) rule = Rule$MSG_UTF8.parse(context);
    else if (rulename.equalsIgnoreCase("BOM")) rule = Rule$BOM.parse(context);
    else if (rulename.equalsIgnoreCase("UTF-8-STRING")) rule = Rule$UTF_8_STRING.parse(context);
    else if (rulename.equalsIgnoreCase("OCTET")) rule = Rule$OCTET.parse(context);
    else if (rulename.equalsIgnoreCase("SP")) rule = Rule$SP.parse(context);
    else if (rulename.equalsIgnoreCase("PRINTUSASCII")) rule = Rule$PRINTUSASCII.parse(context);
    else if (rulename.equalsIgnoreCase("NONZERO-DIGIT")) rule = Rule$NONZERO_DIGIT.parse(context);
    else if (rulename.equalsIgnoreCase("DIGIT")) rule = Rule$DIGIT.parse(context);
    else if (rulename.equalsIgnoreCase("NILVALUE")) rule = Rule$NILVALUE.parse(context);
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
