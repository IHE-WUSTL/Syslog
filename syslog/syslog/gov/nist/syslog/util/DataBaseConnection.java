package gov.nist.syslog.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.log4j.Logger;

/**
 * Helper class contains information regarding the current connection for a
 * database. Creates DataBaseConnection objects and loads DB Drivers as needed.
 * Databases are set up in the {@link gov.nist.registry.syslog.util.Util Util}
 * configuration file using a section for each data base name:<br/>
 * [databaseName_DB]<br/>
 * UserId=userId (def "syslog")<br/>
 * Password=password (def "syslog")<br/>
 * ConnectionString=connStr (def DBUtil.URL<br/>
 * DriverName=driverName (def DBUtil.DRIVER_NAME)<br/>
 * 
 * @author rmoult01
 */
public class DataBaseConnection {
   
   //------------------------ connection properties
    private Connection conn = null;
    private ResultSet lastResultSet = null;
    private ResultSetMetaData lastSQLMetaData = null;
    private String dbName;
    private String user;
    private String password;
    private String driver;
    private String url;
    
    //-------------------------------- drivers which have been loaded
    private static HashSet<String> drvrs = new HashSet<String>();
    //--------------------------- existing DataBaseConnection objects
    private static HashMap<String, DataBaseConnection> dbs = 
       new HashMap<String, DataBaseConnection>();
    
   private DataBaseConnection(String dbName) throws Exception {
      this.dbName = dbName;
      String section = dbName + "_DB";
      Logger syslog = Util.getSyslog();
      user = Util.getParameterString(section + ".UserId", "");
      password = Util.getParameterString(section + ".Password", "");
      driver = Util.getParameterString(section + ".DriverName", DBUtil.DRIVER_NAME);
      url = Util.getParameterString(section + ".ConnectionString", DBUtil.URL);

      if (!drvrs.contains(driver)) {
         try {
            Class.forName(driver).newInstance();
         } catch (Exception e) {
            syslog.error("Couldn't load " + driver + " " + e.getMessage());
            throw new Exception(e);
         }
         drvrs.add(driver);
      }

   }
   
   /**
    * Returns {@link gov.nist.registry.syslog.util.DataBaseConnection
      DataBaseConnection} object for passed data base name. Creates if needed.
    * @param dbName String name of database
    * @return DatabaseConnection object
    * @throws Exception on error.
    */
   public static DataBaseConnection getDataBaseConnection(String dbName)
      throws Exception {
      if (!dbs.containsKey(dbName))
         dbs.put(dbName, new DataBaseConnection(dbName));
      return dbs.get(dbName);
   }
   public static DataBaseConnection getExistingDataBaseConnection(String dbName) {
   return dbs.get(dbName);
}
   
   /**
    * Returns current value of connection property.  May be a valid 
    * {@link java.sql.Connection Connection} or null.
    * @return
    */
   public Connection getConn() {
      return conn;
   }
   
   /**
    * Gets a valid {@link java.sql.Connection Connection} for this 
    * {@link gov.nist.registry.syslog.util.DataBaseConnection DataBaseConnection}
    * object. Opens a new Connection if needed.
    * @return Connection 
    * @throws Exception on error
    */
   public Connection getConnection() throws Exception {
      if (conn == null) 
         conn = DriverManager.getConnection(url, user, password);
      return conn;
   }
   
   /**
    * Gets a valid {@link java.sql.Connection Connection} for this 
    * {@link gov.nist.registry.syslog.util.DataBaseConnection DataBaseConnection}
    * object to use with dbInsert. Opens a new Connection if needed.
    * @return Connection 
    * @throws Exception on error
    */
   public Connection getIconnection() throws Exception {
         return DriverManager.getConnection(url, user, password);
   }
   
   public static void closeConnection(String dbName) {
      DataBaseConnection c = DataBaseConnection
            .getExistingDataBaseConnection(dbName);
      try {
         if (c.conn != null) {
            c.conn.close();
            c.conn = null;
         }
      } catch (Exception e) {
      }
   }
   
   public static void closeResultSet(String dbName) {
      DataBaseConnection c = 
         DataBaseConnection.getExistingDataBaseConnection(dbName);
      if (c == null) return;
      try {
         if (c.getLastResultSet() != null) 
            c.getLastResultSet().close();
         c.setLastResultSet(null);
         c.setLastSQLMetaData(null);
      } catch (Exception e) {}
   }

   public ResultSet getLastResultSet() {
      return lastResultSet;
   }

   public void setLastResultSet(ResultSet lastResultSet) {
      this.lastResultSet = lastResultSet;
   }

   public ResultSetMetaData getLastSQLMetaData() {
      return lastSQLMetaData;
   }

   public void setLastSQLMetaData(ResultSetMetaData lastSQLMetaData) {
      this.lastSQLMetaData = lastSQLMetaData;
   }

   public String getDbName() {
      return dbName;
   }

   public void setDbName(String dbName) {
      this.dbName = dbName;
   }

   public String getUser() {
      return user;
   }

   public void setUser(String user) {
      this.user = user;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getDriver() {
      return driver;
   }

   public void setDriver(String driver) {
      this.driver = driver;
   }

   public String getUrl() {
      return url;
   }

   public void setUrl(String url) {
      this.url = url;
   }

   public String toString() {
       return new StringBuilder("[")
          .append(this.getClass().getName()).append(": ")
          .append("dbName=").append(dbName)
          .append(", driver=").append(driver)
          .append(", url=").append(url)
          .append(", user=").append(user)
          .append(", pw=").append(password)
          .append((conn != null) ? " connected" : " not connected")
          .append((lastResultSet != null) ? " has results" : "no results")
          .append("]")
          .toString();
    }
}
