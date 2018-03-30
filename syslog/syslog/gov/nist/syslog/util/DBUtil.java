package gov.nist.syslog.util;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import org.apache.commons.lang.StringUtils;
import org.postgresql.core.BaseConnection;

/**
 * Encapsulates JDBC data strings and methods specific to a particular DBMS. 
 * In this case, postgresql.
 * @author rmoult01
 */
public class DBUtil {
   
    public static SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public  static final String DRIVER_NAME   = "org.postgresql.Driver";
    public  static final String URL           = "jdbc:postgresql://golden/";
    private static final String ROOT_DB       = "postgres";
    public  static final String DBNAME = "syslog";
    
    /**
    * CREATE for syslog message tables.<p/>
    * Results for parsing for RFC5424 and RFC3164 message format:<ul>
    * <li/><b>parse</b> 0=not parsed, 1=parsed OK, 2=parsed, error.
    * <li/><b>error message</b> if parsed with error, error message, else empty.
    * <li/><b>error substring</b> if parsed with error, a substring of the 
    * message containing the point at which the parse error occurred, else empty.
    * <li/><b>error location</b>. if parsed with error, char offset of error in 
    * substring, else -1.</ul>
    */
   public static final String[] CREATE_SYSLOG_TABLE = {
       "CREATE SEQUENCE seq_syslog_id START 1;",
       
       " CREATE TABLE syslog (" +
           "id BIGINT PRIMARY KEY DEFAULT NEXTVAL('seq_syslog_id'), "+
           
           "sender_ip      VARCHAR(15) NOT NULL, "+
           "collector_ip   VARCHAR(15) NOT NULL, "+
           "collector_port INT NOT NULL, " +
           "arrival_time   TIMESTAMP NOT NULL, " +
           
           "error_message VARCHAR(256) NOT NULL, " +
           
           "rfc3164_parse SMALLINT NOT NULL DEFAULT 0, " +
           "rfc3164_error_message TEXT NOT NULL, " +
           "rfc3164_error_substring VARCHAR(256) NOT NULL, " +
           "rfc3164_error_location INT NOT NULL DEFAULT -1, " +
           
           "rfc5424_parse SMALLINT NOT NULL DEFAULT 0, " +
           "rfc5424_error_message TEXT NOT NULL, " +
           "rfc5424_error_substring VARCHAR(256) NOT NULL, " +
           "rfc5424_error_location INT NOT NULL DEFAULT -1, " +
           
           "xml_parse SMALLINT NOT NULL DEFAULT 0, " +
           "xml_parse_error_message TEXT NOT NULL, " +
           "xml_parse_line SMALLINT NOT NULL DEFAULT 0, " +
           "xml_parse_column SMALLINT NOT NULL DEFAULT 0, " +
           
           "rfc3881_validate SMALLINT NOT NULL DEFAULT 0," +
           "rfc3881_validate_error_message TEXT NOT NULL, " +
           "rfc3881_validate_line SMALLINT NOT NULL DEFAULT 0, " +
           "rfc3881_validate_column SMALLINT NOT NULL DEFAULT 0, " +
           
           "dicom_validate SMALLINT NOT NULL DEFAULT 0," +
           "dicom_validate_error_message TEXT NOT NULL, " +
           "dicom_validate_line SMALLINT NOT NULL DEFAULT 0, " +
           "dicom_validate_column SMALLINT NOT NULL DEFAULT 0, " +
           
           "event_type         VARCHAR(128) NOT NULL, " +
           "event_id           VARCHAR(128) NOT NULL, " +
           
           "message_name VARCHAR(256) NOT NULL, " +
           
           "raw_message TEXT NOT NULL DEFAULT '', " +
           "xml_message TEXT NOT NULL DEFAULT ''); ",
           
        "GRANT ALL ON syslog TO GROUP public;",
        
        "CREATE SEQUENCE seq_schematron_id START 1;",
        
        "CREATE TABLE schematron (" +
           "id BIGINT PRIMARY KEY DEFAULT NEXTVAL('seq_schematron_id'), "+
           "syslog_id BIGINT REFERENCES syslog (id) ON DELETE CASCADE, " +
           
           "audit_message_name VARCHAR(256) NOT NULL, " +
           
           "schematron_validate SMALLINT NOT NULL DEFAULT 0, " +
           "schematron_validate_error_message TEXT NOT NULL, " +
           "schematron_validate_line SMALLINT NOT NULL DEFAULT 0, " +
           "schematron_validate_column SMALLINT NOT NULL DEFAULT 0);",
           
           "GRANT ALL ON schematron TO GROUP public;"
   };
   
   public static final String[] SYSLOG_INSERT = {
      "INSERT INTO syslog VALUES (NEXTVAL('seq_syslog_id'), " + 
      "'${senderIp}', '${collectorIp}', " +
      "${collectorPort}, 'now', '${errorMessage}', ${rfc3164Parse}, " + 
      "'${rfc3164ErrorMessage}', '${rfc3164ErrorSubstring}', " + 
      "${rfc3164ErrorLocation}, ${rfc5424Parse}, '${rfc5424ErrorMessage}', " + 
      "'${rfc5424ErrorSubstring}', ${rfc5424ErrorLocation}, " +
      "${xmlParse}, '${xmlParseErrorMessage}', ${xmlParseLine}, " +
      "${xmlParseColumn}, ${rfc3881Validate}, " + 
      "'${rfc3881ValidateErrorMessage}', ${rfc3881ValidateLine}, " + 
      "${rfc3881ValidateColumn}, ${dicomValidate}," +
      "'${dicomValidateErrorMessage}', ${dicomValidateLine}, " + 
      "${dicomValidateColumn}, '${eventType}', '${eventId}', " +
      "'${messageName}', '${rawMessage}', '${xmlMessage}');"
      };
   
   public static final String SYSLOG_LID = "SELECT currval('seq_syslog_id') AS lid;";
   
   public static final String[] SCHEMATRON_INSERT = {
      "INSERT INTO schematron VALUES (NEXTVAL('seq_schematron_id'), " + 
         "${syslogId}, '${atnaMessageName}', ${schematronValidate}, " +
         "'${schematronValidateErrorMessage}', ${schematronValidateLine}, " +
         "${schematronValidateColumn});"
   };
    
    /**
     * Determines if the passed user exists in the DBMS.
    * @param userName String name of the user to check for.
    * @return boolean true if user exists, false otherwise.
    * @throws Exception on error.
    */
   public static boolean userExists(String userName) throws Exception {
       String un = StringUtils.trimToEmpty(userName);
       if (un.length() == 0) return false;
       ResultSet rs = 
          new Query("select usename from pg_user where usename = lower('${user}');")
             .set("user", un)
             .dbQuery(ROOT_DB);
       boolean exists = false;
       if (rs.next()) exists =  true;
       rs.close();
       return exists;
    }
      
   /**
    * Creates database user IAW with passed data.
    * @param userName String user name
    * @param password String user password. If null, empty, or whitespace, user
    * will have no password.
    * @param createDB boolean should user have power to create databases.
    * @throws Exception on error or if user name is invalid.
    */
   public static void createUser(String userName, String password, 
         boolean createDB) throws Exception {
      String un = StringUtils.trimToEmpty(userName);
      String pw = StringUtils.trimToEmpty(password);
      if (un.length() == 0) throw new Exception("invalid user name");
      Query qry = new Query("create user ${name}").set("name", un);
      if (pw.length() != 0) 
         qry.append(" password '${password}'").set("password", pw);
      if (createDB) qry.append(" CREATEDB");
      qry.append(";");
      qry.dbUpdate(ROOT_DB);
   }
   
   /**
    * Drops named user from DB.
    * @param userName String user name to drop
    * @throws Exception on error or if user name is invalid.
    */
   public static void dropUser(String userName) throws Exception {
      String un = StringUtils.trimToEmpty(userName);
      if (un.length() == 0) throw new Exception("invalid user name");
      new Query("drop user ${name}").set("name", un).dbUpdate();
   }
    
    /**
     * Determines if the names database exists in the DBMS.
     * @param dbName String name of the database to check for.
     * @return boolean true if database exists, false otherwise.
     * @throws Exception on error.
     */
    public static boolean databaseExists(String dbName) throws Exception {
        if (dbName == null) return false;
        ResultSet rs = 
           new Query("select datname from pg_database where datname = lower('${dbName}');")
              .set("dbName", dbName)
              .dbQuery(ROOT_DB);
        boolean exists = false;
        if (rs.next()) exists =  true;
        rs.close();
        return exists;
    }
    
    /**
     * Creates database with passed name.
     * @param dbName name of DB to be created. DB owner is user in .ini file
     * @throws Exception on error.
     */
    public static void createDatabase(String dbName) 
        throws Exception {
        if (dbName == null) throw new Exception("createDatabase error: dbName is null");
        new Query("CREATE DATABASE ${dbName} WITH OWNER ${user}")
        	.set("dbName", dbName)
        	.set("user", Util.getParameterString(dbName + "_DB.UserId", dbName))
        	.dbUpdate(ROOT_DB);
    }
    
   /**
    * Drops passed database name (must not be in use)
    * @param dbName string name of database to be dropped.
    * @throws Exception on error
    */
   public static void dropDatabase(String dbName) throws Exception {
        Util.dbClose(dbName);
        new Query("drop database if exists '${dbName}'")
        	.set("dbName", dbName)
        	.dbUpdate(ROOT_DB);
    }
    
    /**
     * Checks to see if passed db table exists
     * @param dbName name of database 
     * @param tblName name of table in dbName
     * @return boolean, Does this table exists? <br/>
     * NOTE: Does not distinguish between table not existing and whole db not
     * existing.
     */
    public static boolean tableExists(String dbName, String tblName) {
        if (dbName == null || tblName == null) return false;
        try {
            ResultSet rs = new Query("Select * from ${tblName} limit 1")
            	.set("tblName", tblName)
            	.dbQuery(dbName);
            rs.close();
            return true;
        } catch (Exception e) {}
        return false;
    }
    /**
     * Checks to see if passed db table exists in the default database
     * @param tblName name of table in default database
     * @return boolean, Does this table exists? <br/>
     * NOTE: Does not distinguish between table not existing and whole db not
     * existing.
     */
    public static boolean tableExists(String tblName) {
       return tableExists(DBNAME, tblName);
    }
    
    public static void closeDb(String dbName) {
       try { Util.dbClose(dbName); }
       catch (Exception e) {
          Util.getSyslog().warn(e.getMessage());
       }
    }
    public static void closeDb() {
       closeDb(DBNAME);
    }
    
    public static void beginTransaction(String dbName) throws Exception {
       Util.dbUpdate(dbName, "begin;");
       Util.getSyslog().debug(dbName + " begin transaction");
       
    }
    public static void beginTransaction() throws Exception {
       Util.dbUpdate(DBNAME, "begin;");
       Util.getSyslog().debug(DBNAME + " begin transaction");
    }
    public static void commitTransaction(String dbName) throws Exception {
       Util.dbUpdate(dbName, "commit;");
       Util.getSyslog().debug(dbName + " commit transaction");       
    }
    public static void rollbackTransaction() throws Exception {
       Util.dbUpdate(DBNAME, "rollback;");
       Util.getSyslog().debug(DBNAME + " rollback transaction");
    }
    public static void rollbackTransaction(String dbName) throws Exception {
       Util.dbUpdate(dbName, "rollback;");
       Util.getSyslog().debug(dbName + " rollback transaction");
       
    }
    public static void commitTransaction() throws Exception {
       Util.dbUpdate(DBNAME, "rollback;");
       Util.getSyslog().debug(DBNAME + " rollback transaction");
    }
    
    public static String escape(String in) {
       return escape(in, DBNAME);
    }
    public static String escape(String in, String dbName) {
       try {
          BaseConnection bconn = (BaseConnection) DataBaseConnection.getDataBaseConnection(dbName).getConnection();
          return bconn.escapeString(in);
       } catch (Exception e) {
          String em = "SQL escape error: " + e.getMessage();
          Util.getSyslog().warn(em);
          return in;
       }
    }

}