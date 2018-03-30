package gov.nist.syslog.util;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.text.StrSubstitutor;

public class Query implements Serializable {

   private static final long serialVersionUID = 1L;

   private ArrayList<StringBuilder> sql = new ArrayList<StringBuilder>();
   private String lastCommand = null;
   private ArrayList<String> lastSql = new ArrayList<String>();
   private Map<String, String> vars = new HashMap<String, String>();

   private static String lstCommand = null;
   private static ArrayList<String> lstSql = new ArrayList<String>();

   /**
    * Default constructor. Creates Query with single empty SQL Query String
    */
   public Query() {
      sql.add(new StringBuilder(""));
   }

   /**
    * Creates query with single SQL query string using passed query string as
    * starting point.
    * @param obj - {@link java.lang.Object Object} Starting SQL Query string is
    *        the .toString() value of this Object.
    */
   public Query(Object obj) {
      sql.add(new StringBuilder(obj.toString()));
   }

   /**
    * Creates query with query strings made from each of the passed objects in
    * the array.
    * @param objs
    */
   public Query(Object[] objs) {
      for (Object o : objs)
         sql.add(new StringBuilder(o.toString()));
   }

   /**
    * Appends the passed string to the SQL Query string (the first/0 string if
    * this query has multiple query strings.
    * @param obj {@link java.lang.Object Object} the .toString() value of this
    *        Object is appended to the Query.
    * @return a reference to this Query object.
    */
   public Query append(Object obj) {
      sql.get(0).append(obj.toString());
      return this;
   }

   /**
    * Appends the passed string to the SQL Query string indicated by index,
    * numbered 0 - size-1.
    * @param index number of sql line to append to.
    * @param obj {@link java.lang.Object Object} the .toString() value of this
    *        Object is appended to the Query.
    * @return a reference to this Query object.
    * @throws Exception on index out of bounds.
    */
   public Query append(int index, Object obj) throws Exception {
      if (index < 0 || index >= sql.size())
         throw new Exception("append to non-existent Query line " + index);
      sql.get(index).append(obj.toString());
      return this;
   }

   /**
    * Adds the value of the passed object to the query as the next query line.
    * @param obj {@link java.lang.Object Object} the .toString() value of this
    *        Object is added as the next query line
    * @return a reference to this Query object.
    */
   public Query add(Object obj) {
      sql.add(new StringBuilder(obj.toString()));
      return this;
   }

   /**
    * Adds the values of each object in the passed array to the query as the
    * next query lines.
    * @param obj {@link java.lang.Object Object} array. the .toString() values
    *        of these Objects are added as the next query lines
    * @return a reference to this Query object.
    */
   public Query add(Object[] objs) {
      for (Object o : objs)
         sql.add(new StringBuilder(o.toString()));
      return this;
   }

   /**
    * Sets a parameter value in the SQL Query string. Parameters must be of the
    * form ${parameter name}, and are handled by a default
    * {@link org.apache.commons.lang.text.StrSubstitutor StrSubstitutor}.<br/>
    * <b>Note:</b> Parameter substitutions are not actually processed until the
    * Query is submitted. If the same parameter is set multiple times, the last
    * value will be the effective one.
    * @param key {@link java.lang.Object Object} the .toString() value of this
    *        Object is the parameter name.
    * @param value {@link java.lang.Object Object} the .toString() value of this
    *        Object is the value which replaces the parameter entry. This value
    *        will be escaped for SQL before use.
    * @return a reference to this Query object.
    */
   public Query set(Object key, Object value) {
      if (value == null)
         Util.getSyslog().warn("Query.set " + key.toString() + " value null");
      if (value.getClass().isEnum()) {
         Enum e = (Enum) value;
         vars.put(key.toString(), ((Integer) e.ordinal()).toString());
         return this;
      }
      vars.put(key.toString(), StringEscapeUtils.escapeSql(value.toString()));
      return this;
   }

   /**
    * Executes the query on the passed dbName. This call is for queries which
    * return a ResultSet. Uses
    * {@link gov.nist.registry.syslog.util.Util#dbQuery(String, String)
    * Util.dbQuery}
    * @param dbName String name of db to query.
    * @return {@link java.sql.ResultSet ResultSet}
    * @throws Exception on error or if query has multiple lines.
    */
   public ResultSet dbQuery(String dbName) throws Exception {
      lstCommand = lastCommand = "dbQuery(" + dbName + ")";
      if (sql.size() > 1)
         throw new Exception("Multi-line query; Use dbUpdates");
      return Util.dbQuery(dbName, prepQuery(0));
   }

   public ResultSet dbQuery() throws Exception {
      return dbQuery(DBUtil.DBNAME);
   }

   /**
    * Executes the query on the passed dbName. This call is for queries which
    * return record counts or nothing. Uses
    * {@link gov.nist.registry.syslog.util.Util#dbUpdate(String, String)
    * Util.dbUpdate}
    * 
    * @param dbName String name of db to query.
    * @return integer result of query.
    * @throws Exception on error.
    */
   public int dbUpdate(String dbName) throws Exception {
      lstCommand = lastCommand = "dbUpdate(" + dbName + ")";
      if (sql.size() > 1)
         throw new Exception("Multi-line query; Use dbUpdates");
      return Util.dbUpdate(dbName, prepQuery(0));
   }

   public int dbUpdate() throws Exception {
      return dbUpdate(DBUtil.DBNAME);
   }

   public int dbInsertSyslog() throws Exception {
      return Util.dbInsertSyslog(prepQuery(0));
   }

   public String getQuery() {
      StringBuilder q = new StringBuilder();
      for (int i = 0; i < sql.size(); i++) {
         if (i > 0) q.append(System.getProperty("line.separator"));
         q.append(prepQuery(i));
      }
      return q.toString();
   }

   /**
    * Executes the query on the passed dbName. This call is for multiple line
    * queries which return record counts or nothing. Uses
    * {@link gov.nist.registry.syslog.util.Util#dbUpdate(String, String)
    * Util.dbUpdate}
    * 
    * @param dbName String name of db to query.
    * @return integer arrays with results of queries.
    * @throws Exception on error.
    */
   public int[] dbUpdates(String dbName) throws Exception {
      lstCommand = lastCommand = "dbUpdates(" + dbName + ")";
      int[] ret = new int[sql.size()];
      for (int i = 0; i < sql.size(); i++)
         ret[i] = Util.dbUpdate(dbName, prepQuery(i));
      return ret;
   }

   public int[] dbUpdates() throws Exception {
      return dbUpdates(DBUtil.DBNAME);
   }

   private String prepQuery(int index) {
      String query = sql.get(index).toString();
      Util.getSyslog().debug("raw query = " + sql);
      if (!vars.isEmpty()) {
         query = StrSubstitutor.replace(query, vars);
      }
      Util.getSyslog().debug("    query = " + query);
      lastSql.add(index, query);
      lstSql.add(index, query);
      return query;
   }

   /**
    * Returns last call to Util db routines executed by this instance of Query.
    * Returns "N/A" if no routine has been executed.
    * @return string representation of command, with variables replaced.
    */
   public String lastCommand() {
      if (lastCommand == null) return "N/A";
      StringBuilder str = new StringBuilder(lastCommand).append("\n");
      for (String s : lastSql)
         str.append(s).append("\n");
      return str.toString();
   }

   /**
    * Returns last call to Util db routines executed by any instance of Query.
    * Returns "N/A" if no routine has been executed.
    * @return string representation of command, with variables replaced.
    */
   public static String LastCommand() {
      if (lstCommand == null) return "N/A";
      StringBuilder str = new StringBuilder(lstCommand).append("\n");
      for (String s : lstSql)
         str.append(s).append("\n");
      return str.toString();

   }
}
