package edu.wustl.mir.erl.IHETools.Util;

import java.io.File;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import javax.faces.model.SelectItem;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Static utility methods
 * @author rmoult01
 *
 */
/**
 * @author rmoult01
 *
 */
@SuppressWarnings("restriction")
public class Util implements Serializable { 
	private static final long serialVersionUID = 1L;
	private static final String INIT_SECTION = "InitializeApplication";

	private static String applicationContext = null;
	private static String profileString = null;
	private static String displayName = null;
	private static String homePage = null;
	private static String homeURL = null;
	private static String wiki = null;

	private static Path runDirectory;
	private static HierarchicalINIConfiguration ini;

	private static Logger log = null;
	public static final String nl = System.lineSeparator();

	/**
	 * standard initialization routine for web application:<ul>
	 * <li/>Locates and validates runDirectory applicationName.
	 * <li/>Initializes logging using log4j.properties file in runDirectory.
	 * <li/>Loads {@link org.apache.commons.configuration.HierarchicalINIConfiguration 
	 * applicationName.ini} file in runDirectory. Exits on any error.
	 * @param applicationContext name of application to initialize.
	 */
	public static void initializeApplication(String applicationName) {
		try {
			//---------------------------------------- run profile
			profileString = FacesUtil.getContextString("profile", "");

			/* The application context defaults to the applicationName if
			 * the profile is blank or starts with "dev". Otherwise, it 
			 * defaults to applicationName-profileString
			 */
			applicationContext = 
					FacesUtil.getContextString("applicationContext", 
							applicationName + "-" + profileString);

			// --------------------------------- get run directory 
			String n = FacesUtil.getContextString("runDirectory", "runDirectory");
			runDirectory =  Paths.get(Util.class.getClassLoader()
					.getResource(n).getPath());
			Util.isValidPfn("run directory", runDirectory, true, "rx");

			// -------------------------- initialize logging
			Path path = runDirectory.resolve("log4j.properties");
			Util.isValidPfn("log properties", path, false, "r");
			PropertyConfigurator.configure(path.toString());
			log = Logger.getLogger("syslog");
			log.info("Util.initializeApplication(" + applicationName + ")");
			log.info("applicationName: " + applicationContext);
			log.info("profileString:   " + profileString);
			log.info("runDirectory:    " + runDirectory.toString());
			log.debug("logging initialized");

			// --------------------------- Load ini file
			path = runDirectory.resolve(applicationName + ".ini");
			Util.isValidPfn("properties file", path, false, "rw");
			ini = new HierarchicalINIConfiguration(path.toString());
			log.debug(path.getFileName() + " loaded.");

			displayName = Util.getParameterString(INIT_SECTION, 
					"DisplayName", applicationContext);
			log.info("displayName:     " + displayName);
			homePage = Util.getParameterString(INIT_SECTION, 
					"HomePage", applicationContext + ".xhtml");
			log.info("homePage:        " + homePage);
			homeURL = "/" + applicationContext + "/" + homePage + ".xhtml";			

			try {
				wiki = Util.getParameterString(INIT_SECTION, "Wiki", "");
				new URL(wiki);
				log.info("wiki:            " + wiki);
			} catch (Exception mue) {
				log.info("No valid wiki URL found");
				wiki = "";
			}

		} catch (Exception e) {
			String em = "initializeApplication failure: " + e.getMessage();
			if (log == null) System.err.println(em);
			else log.error(em);
			System.exit(1);
		}

	} // EO initializeApplication method

	/**
	 * Validates that a directory or file exists and has needed permissions.
	 * @param name - Logical name of file/dir, for error message, for example,
	 * "Message file".
	 * @param path - file/dir path to validate
	 * @param dir boolean true for a directory, false for a file
	 * @param cds - String containing codes for needed permissions: r=read,
	 * w=write, x=executable; for example "rw" for read-write permissions 
	 * needed. Case is ignored.
	 * @throws Exception on error containing logical name, path, and error
	 * description
	 */
	public static void isValidPfn(String name, Path path, boolean dir, 
			String cds) throws Exception {

		String msg = name + " " + path + " ";
		String c = StringUtils.stripToEmpty(cds).toLowerCase();

		File file = path.toFile();

		if (!file.exists()) 
			throw new Exception(msg + "not found");

		if (dir) {
			if (!file.isDirectory())
				throw new Exception(msg + "is not a directory");
		} else {
			if (!file.isFile())
				throw new Exception(msg + "is not a file");
		}

		//----- return all permission errors at once
		String errs = "";
		if (c.contains("x"))
			if (!file.canExecute())
				errs += "is not executable" + nl;

		if (c.contains("r"))
			if (!file.canRead())
				errs += "is not readable" + nl;

		if (c.contains("w"))
			if(!file.canWrite())
				errs += "is not writable" + nl;

		if (errs.length() > 0)
			throw new Exception(msg + errs);

	} // EO isValidPfn method


	/**
	 * Generates a List of Path objects representing the files in the passed
	 * directory which match the fileNamePattern. Only files, not directories
	 * are included.
	 * @param name - Logical name of directory for error message, for example,
	 * "Message directory".
	 * @param directory Path object of directory files are in.
	 * @param fileNamePattern 'wild card' pattern to match files to, for 
	 * example, "*.xml".
	 * @return a List<Path> of the files in directory matching fileNamePattern.
	 * May be empty, but won't be null.
	 * @throws Exception if directory is not a directory Path, is not
	 * readable, or an IO error occurs. 
	 */
	public static List<Path> getFileList(String name, Path directory,
			String fileNamePattern) throws Exception {
		Util.isValidPfn(name, directory, true, "r");
		List<Path> files = new ArrayList<>();
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(directory,
				fileNamePattern)) {
			for (Path path : ds) {
				File f = path.toFile();
				if (f.isFile())
					files.add(path);
			}
		} 
		return files;
	} // EO getFileList method

	/**
	 * Returns the one and only one file in directory which matches the 
	 * fileNamePattern.
	 * @param name - Logical name of directory for error message, for example,
	 * "Message directory".
	 * @param directory Path object of directory file is in.
	 * @param fileNamePattern 'wild card' pattern to match file to, for 
	 * example, "*.xml".
	 * @return Path of file; will not be null.
	 * @throws Exception if directory is not a directory Path, is not
	 * readable, an IO error occurs, if there are no files in the directory 
	 * matching the fileNamePattern or if there is more than one matching file.
	 */
	public static Path getOneAndOnlyFile(String name, Path directory,
			String fileNamePattern) throws Exception {
		List<Path> files = getFileList(name, directory, fileNamePattern);
		if (files.size() == 0) 
			throw new Exception("no " + fileNamePattern + " file in [" +
					directory.toString() + "] directory");
		if (files.size() > 1) 
			throw new Exception("more than one " + fileNamePattern + 
					" file in [" + directory.toString() + "] directory");
		return files.get(0);
	}


	/**
	 * Does the first string match any of the following strings? Note: Not case
	 * sensitive.
	 * 
	 * @param str
	 *            String to match
	 * @param matches
	 *            possible matching strings
	 * @return boolean <code>true</code> if the first string matches any of the
	 *         subsequent passed Strings, <code>false</code> otherwise.
	 */
	public static boolean isOneOf(String str, String... matches) {
		for (String match : matches) {
			if (str.equalsIgnoreCase(match))
				return true;
		}
		return false;
	}

	/**
	 * Formats Document for pretty printing.
	 * @param doc existing document to format.
	 * @return String of XML message in pretty print format. 
	 * Returns null on error.
	 */
	public static String prettyPrintXML(Document doc) {
		try {
			//---------------------------- make sure something is there
			if (doc == null) throw new Exception("message empty or null");
			//------------------------------------ Pretty print format
			OutputFormat format = new OutputFormat();
			format.setMediaType("text");
			format.setLineWidth(80);
			format.setIndenting(true);
			format.setIndent(3);
			format.setEncoding("UTF-8");
			//-------------------------------------- Document => String
			StringWriter stringOut = new StringWriter();
			XMLSerializer serial = new XMLSerializer(stringOut, format);
			serial.serialize(doc);
			return stringOut.toString();

		} catch (Exception e) {
			log.warn("prettyPrintXML error:" + e.getMessage());
			return null;
		}
	}

	/**
	 * Formats XML String for pretty printing.
	 * @param msg XML String to format.
	 * @return String of XML message in pretty print format. <br/>
	 * Returns copy of the original message on error.
	 */
	public static String prettyPrintXML(String msg) {
		String n = System.getProperty("line.separator");
		try {
			//---------------------------- make sure something is there
			String m = StringUtils.stripToEmpty(msg);
			if (m.length() == 0) throw new Exception("message empty or null");
			String hdr = StringUtils.substringBefore(m, "<?xml");
			if (hdr.length() == m.length())
				throw new Exception("no XML document in message");
			String xml = StringUtils.substringAfter(m, hdr);
			//-------------------------------------- String => Document
			InputSource src = new InputSource(new StringReader(xml));
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(true);
			Document doc = dbFactory.newDocumentBuilder().parse(src);
			//------------------------------------ Pretty print format
			OutputFormat format = new OutputFormat();
			format.setMediaType("text");
			format.setLineWidth(80);
			format.setIndenting(true);
			format.setIndent(3);
			format.setEncoding("UTF-8");
			//-------------------------------------- Document => String
			StringWriter stringOut = new StringWriter();
			XMLSerializer serial = new XMLSerializer(stringOut, format);
			serial.serialize(doc);
			return hdr + n + stringOut.toString();

		} catch (Exception e) {
			log.warn("prettyPrintXML error:" + e.getMessage());
			return msg;
		}
	}
		/**
	    * Comparator class for SelectItem. Implements label and description.
	    */
	   public static class CompSelectItem implements Comparator<SelectItem> {

	      private String col;
	      private boolean asc;
	      public CompSelectItem(String columnName, boolean ascending) {
	         col = columnName;
	         asc = ascending;
	      }

	      @Override
	      public int compare(SelectItem one, SelectItem two) {
	         if (col == null) return 0;
	         if (col.equalsIgnoreCase("label")) {
	            return asc ?
	               one.getLabel().compareToIgnoreCase(two.getLabel()) :
	               two.getLabel().compareToIgnoreCase(one.getLabel()) ;
	         } else if (col.equalsIgnoreCase("description")) {
	            return asc ?
	               one.getDescription().compareToIgnoreCase(two.getDescription()) :
	               two.getDescription().compareToIgnoreCase(one.getDescription()) ;
	         } else return 0;
	      }

	   } // EO Company Comparator inner class

	//************************************************************************
	//************************************************************************
	//************************ ini file Methods ******************************
	//************************************************************************
	//************************************************************************

	/**
	 * Returns string value of configuration parameter in the default .ini.
	 * If parameter does not exist, the default value is returned.
	 *
	 * @param section of configuration file
	 * @param key parameter key
	 * @param defaultValue returned if parameter does not exist
	 * @return parameter value, or default value
	 * @throws Exception
	 */
	public static String getParameterString(String section, String key,
			String defaultValue) throws Exception {
		return getParameterString(section, key, defaultValue, ini);
	}

	/**
	 * Returns string value of configuration parameter in the passed .ini.
	 * If parameter does not exist, the default value is returned.
	 *
	 * @param section of configuration file
	 * @param key parameter key
	 * @param defaultValue returned if parameter does not exist
	 * @param HierarchicalINIConfiguration ini 
	 * @return parameter value, or default value
	 * @throws Exception
	 */
	public static String getParameterString(String section, String key,
			String defaultValue, HierarchicalINIConfiguration ini) 
					throws Exception {
		String k = (section == null)? key : section + "." + key;
		String kv = ini.getString(k, defaultValue);
		log.debug("getParam [" + k + "] value: " + kv);
		return kv;
	}

	/**
	 * sets value of configuration parameter in the default .ini if parameter
	 * does not exist, it will be created. NOT THREAD SAFE.
	 * 
	 * @param section of configuration
	 * @param key parameter key
	 * @param value stored for parameter
	 * @param HierarchicalINIConfiguration ini
	 * @throws Exception
	 */
	public static void setParameterString(String section, String key,
			String value) throws Exception {
		setParameterString(section, key, value, ini);
	}

	/**
	 * sets value of configuration parameter in the passed .ini
	 * if parameter does not exist, it will be created. NOT THREAD SAFE.
	 * @param section of configuration
	 * @param key parameter key
	 * @param value stored for parameter
	 * @param HierarchicalINIConfiguration ini 
	 * @throws Exception
	 */
	public static void setParameterString(String section, String key,
			String value, HierarchicalINIConfiguration ini) throws Exception {
		String k = (section == null)? key : section + "." + key;
		ini.setProperty(k, value);
		log.debug("setParam [" + k + "] to: " + value);
	}

	/**
	 * Returns array of values for configuration parameter in the passed .ini.
	 * The values are represented as comma delimited strings. For example:
	 * Key=abc,def,123,456 would return a String array with four elements, abc,
	 * def, 123, and 456.
	 * @param section of configuration
	 * @param key parameter key in section
	 * @param HierarchicalINIConfiguration ini
	 * @return an array of String objects.
	 * @throws Exception on error
	 */
	public static String[] getParameterStringArray(String section, String key,
			HierarchicalINIConfiguration ini) throws Exception {
		String k = (section == null) ? key : section + "." + key;
		String kv = ini.getString(k);
		String[] values = kv.split(",");
		StringBuilder s = new StringBuilder("getParam [").append(section)
				.append("] ").append(key).append(" value[")
				.append(values.length).append("] ");
		for (int i = 0; i < values.length; i++) {
			s.append(values[i]);
			if ((i + 1) < values.length)
				s.append(", ");
		}
		log.debug(s.toString());
		return values;
	}
	
	/**
	 * Returns array of values for configuration parameter in the default .ini.
	 * The values are represented as comma delimited strings. For example:
	 * Key=abc,def,123,456 would return a String array with four elements, abc,
	 * def, 123, and 456.
	 * @param section of configuration
	 * @param key parameter key in section
	 * @param HierarchicalINIConfiguration ini
	 * @return an array of String objects.
	 * @throws Exception on error
	 */
	public static String[] getParameterStringArray(String section, String key)
		throws Exception {
		return getParameterStringArray(section, key, ini);
	}

	/**
	 * Returns integer value of configuration parameter in passed .ini
	 * If parameter does not exist, the default value will be returned.
	 * @param section of configuration
	 * @param key parameter key
	 * @param defaultValue value returned if key is not found.
	 * @return parameter value, or default value
	 * @throws Exception
	 */
	public static int getParameterInt(String section, String key, 
		int defaultValue, HierarchicalINIConfiguration ini) throws Exception {
		String k = (section == null)? key : section + "." + key;
		int kv = ini.getInt(k, defaultValue);
		log.debug("getParam [" + k + "] value: " + kv);
		return kv;
	}
	
	/**
	 * Returns integer value of configuration parameter in default .ini
	 * If parameter does not exist, the default value will be returned.
	 * @param section of configuration
	 * @param key parameter key
	 * @param defaultValue value returned if key is not found.
	 * @return parameter value, or default value
	 * @throws Exception
	 */
	public static int getParameterInt(String section, String key, 
		int defaultValue) throws Exception {
		return getParameterInt(section, key, defaultValue, ini);
	}

	/**
	 * Stores integer value of configuration parameter in passed .ini
	 * format. if parameter does not exist, it will be created. NOT THREAD SAFE.
	 * @param section of configuration
	 * @param key parameter key
	 * @param value to store
	 * @throws Exception on error
	 */
	public static void setParameterInt(String section, String key, int value, 
		HierarchicalINIConfiguration ini) throws Exception {
		String k = (section == null)? key : section + "." + key;
		ini.setProperty(k, value);
		log.debug("setParam [" + k + "] to: " + value);
	}
	
	/**
	 * Stores integer value of configuration parameter in default .ini
	 * format. if parameter does not exist, it will be created. NOT THREAD SAFE.
	 * @param section of configuration
	 * @param key parameter key
	 * @param value to store
	 * @throws Exception on error
	 */
	public static void setParameterInt(String section, String key, int value) 
		throws Exception {
		setParameterInt(section, key, value, ini);
	}

	//************************************************************************
	//************************************************************************
	//********************* Direct JDBC Methods ******************************
	//************************************************************************
	//************************************************************************

	/*------------------------------------------------------------------------
	 * Databases are set up in the configuration file using a section:<br/>
	 *   [databaseName_DB]<br/>
	 *   UserId=userId            (def "syslog")<br/>
	 *   Password=password        (def "syslog")<br/>
	 *   ConnectionString=connStr (def "jdbc:postgresql://127.0.0.1/databaseName")<br/>
	 *   DriverName=driverName    (def "org.postgresql.Driver")<br/>
	 *----------------------------------------------------------------------*/


	/**
	 * Perform SQL query on database, returning ResultSet
	 * This routine will load driver if required, and will make or re-establish
	 * JDBC connection.  user is responsible for freeing JDBC resources, and
	 * should call dbClose(databaseName) when finished using db. 
	 * @param c DataBaseConnection
	 * @param querySQL String, StringBuffer or StringBuilder
	 * @param resultSetType the {@link java.sql.ResultSet ResultSet} type
	 * for this transaction, 
	 * {@link java.sql.ResultSet#TYPE_FORWARD_ONLY TYPE_FORWARD_ONLY},
	 * {@link java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE TYPE_SCROLL_INSENSITIVE},
	 * or {@link java.sql.ResultSet#TYPE_SCROLL_SENSITIVE TYPE_SCROLL_SENSITIVE}
	 * @param resultSetConcurrency the {@link java.sql.ResultSet ResultSet}
	 * concurrency mode for this transaction,
	 * {@link java.sql.ResultSet#CONCUR_READ_ONLY CONCUR_READ_ONLY} or
	 * {@link java.sql.ResultSet#CONCUR_UPDATABLE CONCUR_UPDATABLE}
	 * @return {@link java.sql.ResultSet ResultSet}, null on error
	 * @throws Exception on error
	 */
	public static ResultSet dbQuery(DataBaseConnection c,
			String querySQL, int resultSetType, int resultSetConcurrency)
					throws Exception {
		try {
			log.info(c.getDbName() + " query = " + querySQL);
			Connection conn = c.getConnection();
			Statement select =
					conn.createStatement(resultSetType, resultSetConcurrency);
			c.lastResultSet = select.executeQuery(querySQL);
			c.lastSQLMetaData = c.lastResultSet.getMetaData();
			return c.lastResultSet;
		} catch (Exception e) {
			StringBuilder b = new StringBuilder();
			b.append(c.getDbName());
			b.append(" query: ").append(querySQL);
			b.append("\n Error: ").append(e.getMessage());
			log.error(b.toString());
			throw e;
		}
	}/**
	 * Perform SQL query on database, returning ResultSet
	 * This routine will load driver if required, and will make or re-establish
	 * JDBC connection.  user is responsible for freeing JDBC resources, and
	 * should call dbClose(databaseName) when finished using db. ResultSet
	 * returned is scrollable read-only<p/>
	 * @param databasename (from configuration file)
	 * @param querySQL String, StringBuffer or StringBuilder
	 * @return ResultSet, null on error
	 * @throws Exception on error
	 */
	/**
	 * Perform SQL query on database, returning ResultSet
	 * This routine will load driver if required, and will make or re-establish
	 * JDBC connection.  user is responsible for freeing JDBC resources, and
	 * should call dbClose(databaseName) when finished using db. Returned
	 * ResultSet is
	 * {@link java.sql.ResultSet#TYPE_SCROLL_SENSITIVE TYPE_SCROLL_SENSITIVE}
	 * {@link java.sql.ResultSet#CONCUR_READ_ONLY CONCUR_READ_ONLY}.
	 *
	 * @param c DataBaseConnection
	 * @param querySQL String or StringBuffer
	 * @return {@link java.sql.ResultSet ResultSet}, null on error
	 * @throws Exception on error
	 */
	public static ResultSet dbQuery(DataBaseConnection c, String querySQL)
			throws Exception {
		return dbQuery(c, querySQL,
				ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
	}
	/**
	 * Convenience method to call
	 * {@link gov.nist.syslog.util.Util#dbQuery(String databasename,
         String querySQL) dqQuery} using a StringBuffer to hold the query.
	 * @param c DataBaseConnection
	 * @param querySQL String or StringBuffer
	 * @return {@link java.sql.ResultSet ResultSet}, null on error
	 * @throws Exception on error
	 *
	 */
	public static ResultSet dbQuery(DataBaseConnection c, StringBuffer querySQL)
			throws Exception {
		return dbQuery(c, querySQL.toString());
	}/**
	 * Perform SQL query on database, returning ResultSet
	 * This routine will load driver if required, and will make or re-establish
	 * JDBC connection.  user is responsible for freeing JDBC resources, and
	 * should call dbClose(databaseName) when finished using db. ResultSet
	 * returned is scrollable read-only<p/>
	 * @param c DataBaseConnection
	 * @param querySQL String, StringBuffer or StringBuilder
	 * @return ResultSet, null on error
	 * @throws Exception on error
	 */
	public static ResultSet dbQuery(DataBaseConnection c, StringBuilder querySQL)
			throws Exception {
		return dbQuery(c, querySQL.toString());
	}
	/**
	 * Perform SQL query on database, returning record counts
	 * This routine will load driver if required, and will make or re-establish
	 * JDBC connection.  user is responsible for freeing JDBC resources, and
	 * should call dbClose(databaseName) when finished using db.<p/>
	 * @param c DataBaseConnection
	 * @param querySQL
	 * @return integer record count, -1 on error
	 * @throws Exception on error
	 */
	public static int dbUpdate(DataBaseConnection c, String querySQL)
			throws Exception {
		int recordCount = -1;
		try {
			log.debug(c.getDbName() + " query = " + querySQL);
			Connection conn = c.getConnection();
			Statement update = conn.createStatement();
			recordCount = update.executeUpdate(querySQL);
			return recordCount;
		} catch (Exception e) {
			StringBuilder b = new StringBuilder();
			b.append(c.getDbName());
			b.append(" query: ").append(querySQL);
			b.append("\n Error: ").append(e.getMessage());
			log.error(b.toString());
			throw e;
		}
	}
	public static int dbUpdate(DataBaseConnection c, StringBuffer querySQL)
			throws Exception {
		return dbUpdate(c, querySQL.toString());
	}

	/**
	 * Closes JDBC connection for databaseName<p/>
	 * @param c DataBaseConnection
	 */
	public static void dbClose(DataBaseConnection c) {
		try {
			if(c.lastResultSet != null) c.lastResultSet.close();
			if (c.conn != null) c.conn.close();
		} catch (Exception e) {}
	}

	private static int callerStackIndex = 0;
	/**
	 * Returns the method name of the method which called the method which
	 * is calling this method, that is, "Who is calling me?"
	 * @return calling method name, or "Unknown" if not known.
	 */
	public static String methodCallingMe() {
		StackTraceElement[] ste = Thread.currentThread().getStackTrace();
		// First time through we find out what level in the stack trace
		// is this method. Caller of caller is two farther on.
		if (callerStackIndex == 0) {
			callerStackIndex = -2;
			for (int i = 0; i < ste.length; i++) {
				if (ste[i].getMethodName().equals("methodCallingMe")) {
					callerStackIndex = i + 2;
					break;
				}
			}
		}
		if (ste.length < (callerStackIndex + 1)) return "Unknown";
		return ste[callerStackIndex].getMethodName();
	}


	/**
	 * Resolves an http(s) URL using a host name to one using the ip address.
	 * @param url String url to resolve
	 * @return resolved url String, null on error.
	 */
	public static String resolveHost(String url) {
		String retVal = null;
		try {
			URL u = new URL(url); 
			String protocol = u.getProtocol();
			if (!protocol.equalsIgnoreCase("http") && !protocol.equalsIgnoreCase("https"))
				throw new Exception("protocol " + protocol + " invalid");

			String hostName = u.getHost();
			String ip = InetAddress.getByName(hostName).getHostAddress();
			if (StringUtils.isBlank(ip)) 
				throw new Exception("ip address is empty");

			int port = u.getPort();

			String file = u.getFile();

			retVal = protocol + "://" + ip;
			if (port > 0) retVal += ":" + ip;
			retVal += file;

		} catch (Exception e ) {
			log.warn("resolveHost error: " + e.getMessage());
		}
		return retVal;
	}

	/**
	 * generates MD5 hash of password to store in DB.
	 * @param pw String plain text password
	 * @return String md5 hash of password
	 */
	public static synchronized String hashPw(String pw) {
		return DigestUtils.md5Hex(pw);
	}
	/**
	 * Comparator for boolean objects. Considers true to be high.
	 * @param one boolean value
	 * @param two boolean value
	 * @return int -1 if true-false, 0 if equal, 1 if false-true
	 */
	public static int compareBoolean(boolean one, boolean two) {
		if (one == two) return 0;
		if (one == false) return -1;
		else return 1;
	}

	/**
	 * Comparator for ipv4 address strings in dot notation.
	 * @param one ipv4 address
	 * @param two ipv4 address
	 * @return int comparison value.
	 */
	public static int compareIp(String one, String two) {
		Scanner s = new Scanner(one); s.useDelimiter("\\.");
		Long lone = (s.nextLong() << 24) + (s.nextLong() << 16) +
				(s.nextLong() << 8) + s.nextLong();
		s.close();
		s = new Scanner(two); s.useDelimiter("\\.");
		Long ltwo = (s.nextLong() << 24) + (s.nextLong() << 16) +
				(s.nextLong() << 8) + s.nextLong();
		s.close();
		return lone.compareTo(ltwo);
	}
	/**
	 * Validates the passed string as an ipv4 address in dot notation. If
	 * so, formats it to have all four tuples with no
	 * leading zeros except for zero value tuple.
	 * @param in ip address to format
	 * @return same ip address, formatted to remove excess zeroes
	 * @throws Exception if passed string is not a valid ipv4 address in
	 * dot format
	 */
	public static String validateFormatIpv4 (String in) throws Exception {
		int[] tuple = {0,0,0,0};
		int i = 0, val;

		try (Scanner s = new Scanner(in);) {
			s.useDelimiter("\\.");
			while (s.hasNext()) {
				if (i == 4) throw new Exception("Too much information");
				if (!s.hasNextInt()) throw new Exception("invalid integer found");
				val = s.nextInt();
				if (val < 0 || val > 255) throw new Exception("not 0-255");
				tuple[i] = val;
				i++;
			}
		} catch (Exception e) {
			log.warn(in + " not valid ipv4 address: " + e.getMessage());
			throw e;
		}
		return tuple[0] + "." + tuple[1] + "." + tuple[2] + "." + tuple[3];
	}    
	
	//*********************************************************************
	//*********************************************************************
	//****************** Property getters/setters *************************
	//*********************************************************************
	//*********************************************************************

	public static String getApplicationContext() {
		return applicationContext;
	}

	public static String getProfileString() {
		return profileString;
	}

	public static String getDisplayName() {
		return displayName;
	}

	public static String getHomePage() {
		return homePage;
	}

	public static String getHomeURL() {
		return homeURL;
	}

	public static String getWiki() {
		return wiki;
	}

	public static Path getRunDirectory() {
		return runDirectory;
	}

	public static HierarchicalINIConfiguration getIni() {
		return ini;
	}

	public static Logger getLog() {
		return log;
	}

} // EO Util class

