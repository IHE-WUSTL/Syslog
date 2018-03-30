package edu.wustl.mir.erl.IHETools.Util;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Encapsulates JDBC data strings and methods specific to a particular DBMS.
 * In this case, postgresql.
 * @author rmoult01
 */
public class DBUtil {

    public static SimpleDateFormat TS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public  static final String DRIVER_NAME   = "org.postgresql.Driver";
    public  static final String URL           = "jdbc:postgresql://localhost/";
    private static final String ROOT_DB       = "postgres";
    public  static final String DBNAME = "syslog";
    public static final String DBADMIN = "syslog_admin";
    public static Logger syslog = null;
    private static DataBaseConnection rootDb = null;

   public static final String[] SYSLOG_DELETE = {
      "DELETE FROM syslog WHERE id = ${id};"
   };

   public static final String[] CREATE_SYSLOG_ADMIN_TABLES = {
      "CREATE SEQUENCE seq_company_id START 1;",

      "CREATE TABLE company (" +
         "id INT PRIMARY KEY DEFAULT NEXTVAL('seq_company_id'), " +
         "name VARCHAR(64) NOT NULL UNIQUE, " +
         "contact VARCHAR(64) NOT NULL, " +
         "contact_email VARCHAR(64) NOT NULL, " +
         "contact_phone VARCHAR(24) NOT NULL);",

      "GRANT ALL ON company to GROUP public;",

      "CREATE SEQUENCE seq_user_id START 1;",

      "CREATE TABLE users (" +
         "id INT PRIMARY KEY DEFAULT NEXTVAL('seq_user_id'), " +
         "company_id INT REFERENCES company (id), " +
         "user_id VARCHAR(64) NOT NULL UNIQUE, " +
         "password VARCHAR(64) NOT NULL, " +
         "user_name VARCHAR(64) NOT NULL, " +
         "user_email VARCHAR(128) NOT NULL, " +
         "user_phone VARCHAR(24) NOT NULL, " +
         "admin BOOLEAN NOT NULL);",

      "GRANT ALL ON users to GROUP public;",

      "CREATE SEQUENCE seq_ip_id START 1;",

      "CREATE TABLE ip (" +
          "id INT PRIMARY KEY DEFAULT NEXTVAL('seq_ip_id'), " +
          "user_id INT REFERENCES users (id), " +
          "address VARCHAR(15) NOT NULL, " +
          "description VARCHAR(128) NOT NULL);",

      "GRANT ALL ON ip to GROUP public;",

      "CREATE TABLE filters (" +
          "id INT PRIMARY KEY, " +
          "address VARCHAR(15) NOT NULL DEFAULT 'Any', " +
          "port    VARCHAR(15) NOT NULL DEFAULT 'Any', " +
          "event_type VARCHAR(12) NOT NULL DEFAULT 'Any', " +
          "event_id   VARCHAR(12) NOT NULL DEFAULT 'Any', " +
          "start_time TIMESTAMP, " +
          "end_time TIMESTAMP, " +
          "record_limit INT NOT NULL DEFAULT 10);",

      "GRANT ALL ON filters to GROUP public;"
   };

   public static final String[] COMPANY_INSERT = {
      "INSERT INTO company VALUES(DEFAULT, '${name}', '${contact}', " +
          "'${contactEmail}', '${contactPhone}');"
   };
   private static final String[] COMPANY_LID = {
      "SELECT currval('seq_company_id') AS lid;"
   };
   public static int getCompanyLid(DataBaseConnection c) {
      try {
         ResultSet r = new Query(COMPANY_LID).dbQuery(c);
         if (!r.next()) throw new Exception("no results");
         return r.getInt("lid");
      } catch (Exception e) {
         syslog.warn(COMPANY_LID + " error: " + e.getMessage());
         return -1;
      }
   }
   public static final String[] COMPANY_UPDATE = {
      "UPDATE company SET name = '${name}', contact = '${contact}', " +
          "contact_email = '${contactEmail}', contact_phone = '${contactPhone}' " +
          "WHERE id = ${id};"
   };

   public static final String[] COMPANY_DELETE = {
      "DELETE FROM company WHERE id = ${id};"
   };

   public static final String[] SCOMPANY_BY_ID = {
      "SELECT * FROM company WHERE id = ${id};"
   };
   
   public static final String[] SCOMPANY_BY_NAME = {
	   "SELECT * FROM company WHERE name = '${name}';"
   };

   public static final String[] SCOMPANY_ALL = {
      "SELECT * FROM company ORDER BY name ASC;"
   };

   public static final String[] USER_INSERT = {
      "INSERT INTO users VALUES(DEFAULT, ${companyId}, '${userId}', " +
          "'${password}', '${userName}', '${userEmail}', '${userPhone}', " +
          "'${admin}');"
   };
   private static final String[] USER_LID = {
      "SELECT currval('seq_user_id') AS lid;"
   };
   public static int getUserLid(DataBaseConnection c) {
      try {
         ResultSet r = new Query(USER_LID).dbQuery(c);
         if (!r.next()) throw new Exception("no results");
         return r.getInt("lid");
      } catch (Exception e) {
         syslog.warn(USER_LID + " error: " + e.getMessage());
         return -1;
      }
   }
   public static final String[] USER_UPDATE = {
      "UPDATE users SET company_id = ${companyId}, user_id = '${userId}', " +
          "password = '${password}', " +
          "user_name = '${userName}', user_email = '${userEmail}', " +
          "user_phone = '${userPhone}', admin = '${admin}' WHERE id = ${id};"
   };

   public static final String[] USER_DELETE = {
      "DELETE FROM users WHERE id = ${id};"
   };

   public static final String[] SUSER_ID = {
      "SELECT * FROM users where id = ${id};"
   };

   public static final String[] SUSER_USERID = {
      "SELECT * from users WHERE user_id = '${userId}';"
   };

   public static final String[] SUSER_DUPID = {
      "SELECT * from users WHERE user_id = '${userId}' AND id != ${id};"
   };

   public static final String[] SUSER_ALL = {
      "SELECT * FROM users;"
   };

   public static final String[] COUNT_IPS_FOR_USER_ID = {
	   "SELECT COUNT(*) AS count FROM ip WHERE user_id = ${userId};"
   };

   public static final String[] IP_INSERT = {
      "INSERT INTO ip VALUES(DEFAULT, ${userId}, '${address}', " +
          "'${description}');"
   };
   private static final String[] IP_LID = {
      "SELECT currval('seq_ip_id') AS lid;"
   };
   public static int getIpLid(DataBaseConnection c) {
      try {
         ResultSet r = new Query(IP_LID).dbQuery(c);
         if (!r.next()) throw new Exception("no results");
         return r.getInt("lid");
      } catch (Exception e) {
         syslog.warn(USER_LID + " error: " + e.getMessage());
         return -1;
      }
   }
   public static final String[] IP_UPDATE = {
      "UPDATE ip SET address + '${address}', description = '${description}' " +
          "WHERE id = ${id};"
   };

   public static final String[] IP_DELETE = {
      "DELETE FROM ip WHERE id = ${id};"
   };

   public static final String[] SIP_BY_USER_ID = {
      "SELECT * FROM ip WHERE user_id = ${userId};"
   };
   /**
    * Query used to validate that an IP address is not already reserved by
    * a different company. If the IP address is OK to add to this user, nothing
    * will be returned.  If the IP address is already reserved by another
    * company, the name of that company will be returned. The parameters to
    * fill in are the ipv4 address string of the address in question and the
    * company id of the user to whom it is to be added.
    */
   public static final String[] SIP_ADD_OK = {
      "SELECT company.name AS name FROM company, users, ip WHERE " +
         "company.id = users.company_id AND " +
         "users.id = ip.user_id AND " +
         "ip.address = '${ipAddress}' AND " +
         "company.id != ${companyId};"
   };

   public static final String[] FILTERS_INSERT = {
      "INSERT INTO filters VALUES (${id});"
   };

   public static final String[] FILTERS_UPDATE = {
      "UPDATE filters SET address = '${address}', port = '${port}', " +
          "event_type = '${eventType}', event_id = '${eventId}', " +
          "start_time = '${startTime}', end_time = '${endTime}', " +
          "record_limit = ${recordLimit} WHERE id = ${id};"
   };

   public static final String[] SFILTERS = {
      "SELECT * FROM filters WHERE id = ${id};"
   };

   static {
      syslog = Util.getLog();
      try {
         rootDb = DataBaseConnection.getDataBaseConnection(ROOT_DB);
      } catch (Exception e) {
         syslog.warn("Could not access " + ROOT_DB + " database");
      }

   }

    /**
     * Determines if the passed user exists in the DBMS.
    * @param dbc DataBaseConnection containing user to check for.
    * @return boolean true if user exists, false otherwise.
    * @throws Exception on error.
    */
   public static boolean userExists(DataBaseConnection dbc) throws Exception {
       String un = StringUtils.trimToEmpty(dbc.getUser());
       if (un.length() == 0) return false;
       ResultSet rs =
          new Query("select usename from pg_user where usename = lower('${user}');")
             .set("user", un)
             .dbQuery(DataBaseConnection.getDataBaseConnection(ROOT_DB));
       boolean exists = false;
       if (rs.next()) exists =  true;
       rs.close();
       return exists;
    }

   /**
    * Creates database user IAW with passed data.
    * @param DataBaseConnection; contains user and password
    * @param createDB boolean should user have power to create databases.
    * @throws Exception on error or if user name is invalid.
    */
   public static void createUser(DataBaseConnection dbc,
         boolean createDB) throws Exception {
      String un = StringUtils.trimToEmpty(dbc.getUser());
      String pw = StringUtils.trimToEmpty(dbc.getPassword());
      if (un.length() == 0) throw new Exception("invalid user name");
      Query qry = new Query("create user ${name}").set("name", un);
      if (pw.length() != 0)
         qry.append(" password '${password}'").set("password", pw);
      if (createDB) qry.append(" CREATEDB");
      qry.append(";");
      qry.dbUpdate(DataBaseConnection.getDataBaseConnection(ROOT_DB));
   }

   /**
    * Drops named user from DB.
    * @param userName String user name to drop
    * @throws Exception on error or if user name is invalid.
    */
   public static void dropUser(String userName) throws Exception {
      String un = StringUtils.trimToEmpty(userName);
      if (un.length() == 0) throw new Exception("invalid user name");
      new Query("drop user ${name}").set("name", un).dbUpdate(rootDb);
   }

    /**
     * Determines if the names database exists in the DBMS.
     * @param dbName String name of the database to check for.
     * @return boolean true if database exists, false otherwise.
     * @throws Exception on error.
     */
    public static boolean databaseExists(DataBaseConnection dbc) throws Exception {
    	
        if (dbc.getPhysicalDbName() == null) return false;
        ResultSet rs =
           new Query("select datname from pg_database where datname = lower('${dbName}');")
              .set("dbName", dbc.getPhysicalDbName())
              .dbQuery(rootDb);
        boolean exists = false;
        if (rs.next()) exists =  true;
        rs.close();
        return exists;
    }

    /**
     * Creates database with passed name.
     * @param dbName Name of database to create
     * @throws Exception on error.
     */
    public static void createDatabase(DataBaseConnection dbc)
        throws Exception {
        if (dbc.getPhysicalDbName() == null) 
        	throw new Exception("createDatabase error: dbName is null");
        new Query("CREATE DATABASE ${dbName} WITH OWNER ${user}")
        	.set("dbName", dbc.getPhysicalDbName())
        	.set("user", dbc.getUser())
        	.dbUpdate(rootDb);
    }

   /**
    * Drops passed database name (must not be in use)
    * @param c DataBaseConnection
    * @throws Exception on error
    */
   public static void dropDatabase(DataBaseConnection c) throws Exception {
        Util.dbClose(c);
        new Query("drop database if exists '${dbName}'")
        	.set("dbName", c.getDbName())
        	.dbUpdate(rootDb);
    }

    /**
     * Checks to see if passed db table exists
     * @param c DataBaseConnection
     * @param tblName name of table in dbName
     * @return boolean, Does this table exists? <br/>
     * NOTE: Does not distinguish between table not existing and whole db not
     * existing.
     */
    public static boolean tableExists(DataBaseConnection c, String tblName) {
        if (c.getDbName() == null || tblName == null) return false;
        try {
            ResultSet rs = new Query("Select * from ${tblName} limit 1")
            	.set("tblName", tblName)
            	.dbQuery(c);
            rs.close();
            return true;
        } catch (Exception e) {}
        return false;
    }

    public static void closeDb(DataBaseConnection c) {
       try { Util.dbClose(c); }
       catch (Exception e) {
          syslog.warn(e.getMessage());
       }
    }

    public static void beginTransaction(DataBaseConnection c) throws Exception {
       Util.dbUpdate(c, "begin;");
       syslog.debug(c.getDbName() + " begin transaction");

    }
    public static void commitTransaction(DataBaseConnection c) throws Exception {
       Util.dbUpdate(c, "commit;");
       syslog.debug(c.getDbName() + " commit transaction");
    }
    public static void rollbackTransaction(DataBaseConnection c) throws Exception {
       Util.dbUpdate(c, "rollback;");
       syslog.debug(c.getDbName() + " rollback transaction");

    }

}
