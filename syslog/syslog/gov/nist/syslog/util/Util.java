package gov.nist.syslog.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Utility methods for:
 * <ul>
 * <li/>
 * command line processing
 * <li/>
 * ini file processing
 * <li/>
 * JDBC
 * </ul>
 */
public class Util {

   private static final String nl = System.getProperty("line.separator");
   private static final String fs = System.getProperty("file.separator");

   private static String actorName = null;
   private static String runDirectory = System.getProperty("user.dir");
   private static String iniFileName = null;
   private static String log4jFileName = null;

   private static String runDirectoryPath;
   private static boolean initialized = false;

   private static HierarchicalINIConfiguration ini;

   private static HashMap<String, Logger> loggers = new HashMap<String, Logger>();
   private static Logger syslog = null;
   private static ArrayList<LogMsg> logHold = new ArrayList<LogMsg>();
   private static Executor exec = Executors.newCachedThreadPool();

   public Util() {
   }

   // ************************************************************************
   // ************************************************************************
   // ************************* logging Methods ******************************
   // ************************************************************************
   // ************************************************************************

   /**
    * Gets the syslog {@link org.apache.log4j.Logger Logger} object.
    * <p/>
    * syslog is the general system log used for debug warning and error logging.
    * Equivalent to getLog("system");
    * @return Logger object
    */
   public static Logger getSyslog() {
      return syslog;
   }

   /**
    * Returns the {@link org.apache.log4j.Logger Logger} object for passed log
    * name. Creates logger if needed.
    * @param logName String log name for log to created.
    * @return Logger object
    */
   public static Logger getLog(String logName) {
      if (loggers.containsKey(logName))
         return loggers.get(logName);
      Logger l = Logger.getLogger(logName);
      loggers.put(logName, l);
      return l;
   }

   /*-----------------------------------------------------------------------
    * routines to hold system log until logging initialized. Calls to syslog
    * in Actor are routed through systemlog, which holds them in an array if
    * logging hasn't been configured, then dumps the array to syslog when it
    * has.  This looses the exact timing of the first log messages, but that
    * is not critical.
    *---------------------------------------------------------------------*/
   public enum LogType {
      TRACE, DEBUG, INFO, WARN, ERROR
   };

   private static void systemlog(LogType t, String msg) {
      // ---------- If syslog is defined, log directly
      if (syslog != null) {
         switch (t) {
         case TRACE:
            syslog.trace(msg);
            break;
         case DEBUG:
            syslog.debug(msg);
            break;
         case INFO:
            syslog.info(msg);
            break;
         case WARN:
            syslog.warn(msg);
            break;
         case ERROR:
            syslog.error(msg);
         }
         return;
      }
      logHold.add(new LogMsg(t, msg));
   }

   // ************************************************************************
   // ************************************************************************
   // *********************** Parameter Methods ******************************
   // ************************************************************************
   // ************************************************************************

   /**
    * Handles command line arguments for programs invoked from the command line,
    * shell script or ant task, directory or via an IDE.<br/>
    * Programs using Util have a run directory, which by default is the current
    * working directory, and an "actor name", whose default value is passed as
    * the first parameter of this method. In the run directory, Util expects to
    * find a property file in the Windows .ini format, with the file name
    * actorname.ini.<br/>
    * Command line parameters are expected to be in POSIX format. Default
    * parameters are:
    * <ul>
    * <li/>-r or -runDirectory <i>runDir</i> which would override the default
    * run directory.
    * <li/>-n or -name <i>actorName</i> which would override the default actor
    * name
    * </ul>
    * It is expected that in most cases, other parameters would be placed in the
    * actorName.ini file.
    * <p/>
    * Additional command line parameters made be added by creating an Object
    * array consisting of {@link org.apache.commons.cli.Option Option} and or
    * {@link org.apache.commons.cli.OptionGroup OptionGroup} objects and passing
    * the array to this method as the addOns argument. If additional parameters
    * are entered, their values will be inserted into the
    * {@link org.apache.commons.configuration.HierarchicalINIConfiguration ini}
    * object in the default section using their POSIX longname value as their
    * key, overriding any existing parameter values with the same key. This
    * addition-override will be done <b>AFTER</b> the .ini file is read; The
    * values in the .ini file on disc will not be changed.
    * 
    * @param actorName name of the actor/program, e.g., SyslogCollector
    * @param args command line arguments from main(args[])
    * @param addOns Option and OptionGroup (s) to add to basic params
    * @return boolean true if program should terminate (no error).
    * @throws Exception on error, program should terminate
    */
   public static boolean processMainArguments(String actorName, String[] args,
         Object[] addOns) throws Exception {

      // --------------------------------------- default values
      Util.actorName = actorName;
      Util.runDirectory = System.getProperty("user.dir");
      HashMap<String, String> pmap = new HashMap<String, String>();

      // Create config of standard and extra command line pars for actor.
      CommandLineParser parser = new PosixParser();
      Options opts = new Options();
      opts.addOption("h", "help", false, "help message and exit");
      opts.addOption("r", "runDirectory", true,
            "working directory for program instance");
      opts.addOption("a", "actorName", true, "name of instance actor/program");
      opts.addOption("i", "iniFile", true, "name of .ini file");
      opts.addOption("l", "log4jFile", true, "name of log4j properties file");

      if (addOns != null) {
         for (Object o : addOns) {
            if (o instanceof Option)
               opts.addOption((Option) o);
            if (o instanceof OptionGroup)
               opts.addOptionGroup((OptionGroup) o);
         }
      }

      try {
         // ------------------------------------- parse command line arguments
         CommandLine line = parser.parse(opts, args);

         if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(Util.actorName + " ", opts);
            return true;
         }

         // ----- Build parameter map, using long (or short) option name key
         for (Option o : line.getOptions()) {
            // --------- Name option overrides passed actorName
            if (o.getLongOpt().equalsIgnoreCase("actorName")) {
               Util.actorName = o.getValue();
               continue;
            }
            // --------- runDirectory option overrides user.dir
            if (o.getLongOpt().equalsIgnoreCase("runDirectory")) {
               Util.runDirectory = o.getValue();
               continue;
            }

            // --------- iniFile name overrides actorName.ini
            if (o.getLongOpt().equalsIgnoreCase("iniFile")) {
               iniFileName = o.getValue();
               continue;
            }

            // ------------- log4jFile overrides "log4j.properties"
            if (o.getLongOpt().equalsIgnoreCase("log4jFile")) {
               log4jFileName = o.getValue();
               continue;
            }

            // --- The rest go into the map.
            pmap.put(o.hasLongOpt() ? o.getLongOpt() : o.getOpt(), o.getValue());
         }

      } catch (Exception e) {
         /*
          * Exceptions to this point mean that something was wrong with the
          * command line parameters. Display error message and usage info.
          */
         System.out.println(nl + "Error during parameter entry: "
               + e.getMessage());
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp(Util.actorName + " ", opts);
         throw e;
      }
      initialize(pmap);
      return false;
   } // EO processMainArguments method

   /**
    * Method called for programs not run from the command line, e.g., web
    * applications. Null arguments means use the default.
    * @param runDir run directory, default user.dir
    * @param actor actor/program name NO DEFAULT
    * @param pmap key-value pairs for parameters, if any.
    * @throws Exception on error, program should terminate.
    */
   public static void initialize(String runDir, String actor,
         Map<String, String> pmap) throws Exception {
      if (runDir != null)
         runDirectory = runDir;
      actorName = actor;
      initialize(pmap);
   }

   /**
    * Internal initialization method.
    * @param pmap Hash map of additional parameters, if any, or null
    * @return boolean true if program should terminate now (no error)
    * @throws Exception on error, program should terminate.
    */
   private static void initialize(Map<String, String> pmap) throws Exception {
      if (initialized)
         throw new Exception("initialize run multiple times");
      initialized = true;

      // ---------------------------------------- Validate run Directory Path
      File f = new File(runDirectory);
      runDirectoryPath = f.getAbsolutePath();
      if (!f.exists())
         throw new Exception(runDirectoryPath + " not found");
      if (!f.isDirectory())
         throw new Exception(runDirectoryPath + " not directory");
      if (!f.canRead() || !f.canWrite())
         throw new Exception(runDirectoryPath + " not read/write/execute");

      // -------------------------------------------- Validate log4j properties
      String log4jPfn = runDirectoryPath + fs + "log4j.properties";
      if (log4jFileName != null)
         log4jPfn = log4jFileName;
      f = new File(log4jPfn);
      if (!f.exists())
         throw new Exception(log4jPfn + " not found");
      if (!f.isFile())
         throw new Exception(log4jPfn + " not valid file");
      if (!f.canRead())
         throw new Exception(log4jPfn + " not readable");
      PropertyConfigurator.configure(log4jPfn);
      // --------------------- log messages held during initialize
      syslog = Util.getLog("syslog");
      for (LogMsg lm : logHold)
         systemlog(lm.logType, lm.message);
      logHold = null;

      // ----------------------------------------------- Validate ini file
      String iniPfn = runDirectoryPath + fs + actorName + ".ini";
      if (iniFileName != null)
         iniPfn = runDirectoryPath + fs + iniFileName;
      f = new File(iniPfn);
      if (!f.exists())
         throw new Exception(iniPfn + " not found");
      if (!f.isFile())
         throw new Exception(iniPfn + " not valid file");
      if (!f.canRead() || !f.canWrite())
         throw new Exception(iniPfn + " not read/write");
      ini = new HierarchicalINIConfiguration(iniPfn);

      syslog = Logger.getLogger("system");
      loggers.put("system", syslog);

      syslog.debug("run directory: " + runDirectoryPath);
      syslog.debug("ini file: " + iniPfn);
      syslog.debug("log4j file: " + log4jPfn);

      if (pmap != null) {
         for (String key : pmap.keySet()) {
            String value = pmap.get(key);
            syslog.debug("param: " + key + "=" + value);
            ini.setProperty(key, value);
         }
      }
   } // EO initialize method

   /**
    * Returns string value of configuration parameter with single part key if
    * parameter does not exist, it will be created with the default value
    * 
    * @param key parameter key
    * @param defaultValue returned if parameter does not exist
    * @return parameter value, or default value
    */
   public static String getParameterString(String key, String defaultValue)
         throws Exception {
      String kv = ini.getString(key, defaultValue);
      systemlog(LogType.DEBUG, "getParam " + key + " value: " + kv);
      return kv;
   }

   /**
    * Returns string value of configuration parameter with section/key format if
    * parameter does not exist, it will be created with the default value
    * 
    * @param section of configuration
    * @param key parameter key
    * @param defaultValue returned if parameter does not exist
    * @return parameter value, or default value
    */
   public static String getParameterString(String section, String key,
         String defaultValue) throws Exception {
      String kv = ini.getString(section + "." + key, defaultValue);
      systemlog(LogType.DEBUG, "getParam [" + section + "] " + key + " value: "
            + kv);
      return kv;
   }

   public static String[] getParameterStringArray(String section, String key)
         throws Exception {
      String value = Util.getParameterString(section, key, null);
      if (value == null)
         return new String[0];
      String[] values = value.split(",");
      StringBuilder s = new StringBuilder("getParam [").append(section)
            .append("] ").append(key).append(" value[").append(values.length)
            .append("] ");
      for (int i = 0; i < values.length; i++) {
         s.append(values[i]);
         if ((i + 1) < values.length)
            s.append(", ");
      }
      systemlog(LogType.DEBUG, s.toString());
      return values;
   }

   public static String[] getParameterStringArray(String key) throws Exception {
      String value = Util.getParameterString(key, null);
      if (value == null)
         return new String[0];
      String[] values = value.split(",");
      StringBuilder s = new StringBuilder("getParam ").append(key)
            .append(" value[").append(values.length).append("] ");
      for (int i = 0; i < values.length; i++) {
         s.append(values[i]);
         if ((i + 1) < values.length)
            s.append(", ");
      }
      systemlog(LogType.DEBUG, s.toString());
      return values;
   }

   /**
    * sets value of configuration parameter in with single part key if param
    * does not exist, it will be created
    * 
    * @param key parameter key
    * @param value stored for parameter
    */
   public static void setParameterString(String key, String value)
         throws Exception {
      ini.setProperty(key, value);
      systemlog(LogType.DEBUG, "setParam " + key + " to: " + value);
   }

   /**
    * sets value of configuration parameter with section/key format if parameter
    * does not exist, it will be created
    * 
    * @param section of configuration
    * @param key parameter key
    * @param value stored for parameter
    */
   public static void setParameterString(String section, String key,
         String value) throws Exception {
      ini.setProperty(section + "." + key, value);
      systemlog(LogType.DEBUG, "setParam [" + section + "] " + key + " to: "
            + value);
   }

   /**
    * Returns integer value of configuration parameter with single part key if
    * parameter does not exist, it will be created with the default value
    * 
    * @param key parameter key
    * @param defaultValue , returned if parameter does not exist
    * @return parameter value, or default value
    */
   public static int getParameterInt(String key, int defaultValue)
         throws Exception {
      int kv = ini.getInt(key, defaultValue);
      systemlog(LogType.DEBUG, "getParam " + key + " value: " + kv);
      return kv;
   }

   /**
    * Returns integer value of configuration parameter with section/key format
    * if parameter does not exist, it will be created with the default value
    * 
    * @param section of configuration
    * @param key parameter key
    * @param defaultValue , returned if parameter does not exist
    * @return parameter value, or default value
    */
   public static int getParameterInt(String section, String key,
         int defaultValue) throws Exception {
      int kv = ini.getInt(section + "." + key, defaultValue);
      systemlog(LogType.DEBUG, "getParam [" + section + "] " + key + " value: "
            + kv);
      return kv;
   }

   /**
    * Stores integer value of parameter with single part key
    * 
    * @param key parameter key
    * @param value to store
    * @throws Exception on error
    */
   public static void setParameterInt(String key, int value) throws Exception {
      ini.setProperty(key, value);
      systemlog(LogType.DEBUG, "setParam " + key + " to: " + value);
   }

   /**
    * Stores integer value of parameter with section/key format
    * 
    * @param section of configuration
    * @param key parameter key
    * @param value to store
    * @throws Exception on error
    */
   public static void setParameterInt(String section, String key, int value)
         throws Exception {
      ini.setProperty(section + "." + key, value);
      systemlog(LogType.DEBUG, "setParam [" + section + "] " + key + " to: "
            + value);
   }

   // *********************************************************************
   // *********************************************************************
   // ********************* Direct JDBC Methods ***************************
   // *********************************************************************
   // *********************************************************************

   /*----------------------------------------------------------------------
    * Databases are set up in the configuration file using a section:<br/>
    *   [databaseName_DB]<br/>
    *   UserId=userId            (def "syslog")<br/>
    *   Password=password        (def "syslog")<br/>
    *   ConnectionString=connStr (def DBUtil.URL<br/>
    *   DriverName=driverName    (def DBUtil.DRIVER_NAME)<br/>
    *--------------------------------------------------------------------*/

   /**
    * Returns {@link java.sql.Connection Connection} for passed db name.
    * 
    * @param databasename
    * @return Connection object
    * @throws Exception on error.
    */
   public static Connection getConnection(String databasename) throws Exception {
      return DataBaseConnection.getDataBaseConnection(databasename)
            .getConnection();
   }

   /**
    * Perform SQL query on database, returning ResultSet This routine will load
    * driver if required, and will make or re-establish JDBC connection. user is
    * responsible for freeing JDBC resources, and should call
    * dbClose(databaseName) when finished using db. ResultSet returned is
    * scrollable read-only
    * <p/>
    * 
    * @param databaseName (from configuration file)
    * @param query String or StringBuffer
    * @return ResultSet, null on error
    * @throws Exception on error
    */
   public static ResultSet dbQuery(String databaseName, String querySQL)
         throws Exception {
      systemlog(LogType.DEBUG, databaseName + " query = " + querySQL);
      try {
         DataBaseConnection c = DataBaseConnection
               .getDataBaseConnection(databaseName);
         Connection conn = c.getConnection();
         Statement select = conn.createStatement(
               ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
         c.setLastResultSet(select.executeQuery(querySQL));
         c.setLastSQLMetaData(c.getLastResultSet().getMetaData());
         return c.getLastResultSet();
      } catch (Exception e) {
         StringBuffer b = new StringBuffer();
         b.append(databaseName);
         b.append(" query\n\n   " + querySQL);
         b.append("\n\n   Error: " + e.getMessage());
         StackTraceElement[] s = e.getStackTrace();
         for (int i = 0; i < s.length; i++) {
            if (i == 0)
               b.append("\n\n  Stack Trace:\n");
            b.append("\n   " + s[i].toString());
         }
         systemlog(LogType.ERROR, b.toString());
         throw e;
      } finally {
         DataBaseConnection.closeConnection(databaseName);
      }
   }

   public ResultSet dbQuery(String databasename, StringBuffer querySQL)
         throws Exception {
      return dbQuery(databasename, querySQL.toString());
   }

   /**
    * Perform SQL query on database, returning record counts This routine will
    * load driver if required, and will make or re-establish JDBC connection.
    * user is responsible for freeing JDBC resources, and should call
    * dbClose(databaseName) when finished using db.
    * <p/>
    * 
    * @param databaseName (from configuration file)
    * @param querySQL
    * @return integer record count, -1 on error
    * @throws Exception on error
    */
   public static int dbUpdate(String databaseName, String querySQL)
         throws Exception {
      int recordCount = -1;
      try {
         systemlog(LogType.DEBUG, databaseName + " query = " + querySQL);
         DataBaseConnection c = DataBaseConnection
               .getDataBaseConnection(databaseName);
         Connection conn = c.getConnection();
         Statement update = conn.createStatement();
         recordCount = update.executeUpdate(querySQL);
         return recordCount;
      } catch (Exception e) {
         StringBuffer b = new StringBuffer();
         b.append(databaseName);
         b.append(" query\n\n   " + querySQL);
         b.append("\n\n   Error: " + e.getMessage());
         StackTraceElement[] s = e.getStackTrace();
         for (int i = 0; i < s.length; i++) {
            if (i == 0)
               b.append("\n\n  Stack Trace:\n");
            b.append("\n   " + s[i].toString());
         }
         systemlog(LogType.ERROR, b.toString());
         throw e;
      } finally {
         DataBaseConnection.closeConnection(databaseName);
      }
   }

   /**
    * Inserts a syslog record into the DB, returning the id. User is responsible
    * to insure that the Query is for an insert of a single syslog record.
    * @param querySQL String query which inserts one syslog row.
    * @return integer id of inserted syslog row.
    * @throws Exception on error
    */
   public static synchronized int dbInsertSyslog(String querySQL)
            throws Exception {
      Connection conn = null;
      Statement stmt = null;
      ResultSet rs = null;
      int recordCount = -1, id = -1;
      DataBaseConnection c = DataBaseConnection
               .getDataBaseConnection(DBUtil.DBNAME);
      try {
         conn = c.getIconnection();
         stmt = conn.createStatement();
         recordCount = stmt.executeUpdate(querySQL);
         if (recordCount != 1)
            throw new Exception("not a single insert");
         stmt.close();
         stmt = conn.createStatement();
         rs = stmt.executeQuery(DBUtil.SYSLOG_LID);
         rs.next();
         id = rs.getInt("lid");
         rs.close();
         conn.close();
         return id;
      } catch (Throwable t) { throw t; 
      } finally {
         try { if (stmt != null) stmt.close();} catch (Exception e){}
         try { if (conn != null) conn.close();} catch (Exception e){}
      }
   }

   public static int dbUpdate(String databasename, StringBuffer querySQL)
         throws Exception {
      return dbUpdate(databasename, querySQL.toString());
   }

   public static ResultSet getLastResultSet(String databasename) {
      DataBaseConnection c = DataBaseConnection
            .getExistingDataBaseConnection(databasename);
      if (c != null)
         return c.getLastResultSet();
      return null;
   }

   /**
    * @param databasename
    * @return
    */
   public static ResultSetMetaData getlastSQLMetaData(String databasename)
         throws Exception {
      DataBaseConnection c = DataBaseConnection
            .getDataBaseConnection(databasename);
      return c.getLastSQLMetaData();
   }

   /**
    * Closes the most resent ResultSet for passed database name
    * 
    * @param databasename
    */
   public static void closeResultSet(String databasename) {
      DataBaseConnection.closeResultSet(databasename);
   }

   /**
    * Closes JDBC connection for databaseName
    * <p/>
    * 
    * @param databaseName (from configuration file)
    * @throws Exception on error
    */
   public static void dbClose(String databaseName) throws Exception {
      DataBaseConnection.closeConnection(databaseName);
   }

   // ---------------------------------- Getters and Setters
   public static Executor getExec() {
      return exec;
   }

   public static void setExec(Executor exec) {
      Util.exec = exec;
   }

   public static String getRunDirectoryPath() {
      return runDirectoryPath;
   }

   // -------------------------- gets time stamp in RFC-3339 format
   private static SimpleDateFormat df = new SimpleDateFormat(
         "yyyy-MM-dd'T'HH:mm:ss.SSSZ");

   public static String getRFC3339TimeStamp() {
      return getRFC3339TimeStamp(new Date());
   }

   public static String getRFC3339TimeStamp(Date ts) {
      String a = df.format(ts);
      return a.substring(0, a.length() - 2) + ":" + a.substring(a.length() - 2);
   }

   /**
    * Gets a string representing the pid of this program - Java VM
    */
   public static String getPid() {
      String retVal = "-";
      Vector<String> commands = new Vector<String>();
      commands.add("/bin/bash");
      commands.add("-c");
      commands.add("echo $PPID");
      ProcessBuilder pb = new ProcessBuilder(commands);
      try {
         Process pr = pb.start();
         pr.waitFor();
         if (pr.exitValue() == 0) {
            BufferedReader outReader = new BufferedReader(
                  new InputStreamReader(pr.getInputStream()));
            retVal = outReader.readLine().trim();
         }
      } catch (Exception e) {
      }
      return retVal;
   }

   /*
    * Get a string representing the hex of the input string
    */
   public static String convertStringToHex(String str) {

      char[] chars = str.toCharArray();

      StringBuffer hex = new StringBuffer();
      for (int i = 0; i < chars.length; i++) {
         hex.append(Integer.toHexString((int) chars[i]));
      }

      return hex.toString();
   }

} // EO Util class
