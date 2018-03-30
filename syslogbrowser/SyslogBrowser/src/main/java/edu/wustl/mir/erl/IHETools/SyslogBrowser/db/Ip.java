/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.erl.IHETools.SyslogBrowser.db;

import edu.wustl.mir.erl.IHETools.Util.DBUtil;
import edu.wustl.mir.erl.IHETools.Util.DataBaseConnection;
import edu.wustl.mir.erl.IHETools.Util.Query;
import edu.wustl.mir.erl.IHETools.Util.Util;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation for ip table in admin DB. Includes:<ul>
 * <li/>Object representation of table row.
 * <li/>Constructor and methods to build objects from JDBC
 * {@link java.sql.ResultSet ResultSet}
 * <li/>Insert, delete, and update methods.
 * <li/>Comparator for sorting arrays of Ip objects
 * <li/>toString method useful for inserting object in log.</ul>
 * @author rmoult01
 */
public class Ip implements Serializable {
   static final long serialVersionUID = 1L;

   private int id = 0;
   private int userId = 0;
   private String address = "";
   private String description = "";

   private boolean selected = false;

   public Ip() {}

    /**
    * Constructor builds Ip object from the current row of the passed
    * ResultSet.
    * @param result ResultSet. Must be positioned at row for which Ip
    * object is desired.
    * @throws SQLException on error.
    */
   public Ip(ResultSet result) throws SQLException {
      id = result.getInt("id");
      userId = result.getInt("user_id");
      address = result.getString("address");
      description = result.getString("description");
   }

   public String getAddress() {
      return address;
   }

   public void setAddress(String address) {
      this.address = address;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public boolean isSelected() {
      return selected;
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
   }

   public int getUserId() {
      return userId;
   }

   public void setUserId(int userId) {
      this.userId = userId;
   }

   public String toString() {
      return "[Ip: id=" + id + " user_id=" + userId + " address=" + address +
              " description=" + description +
              "]";
   }

    /**
    * Loads the ips in a JDBC ResultSet into Ip objects and returns
    * them as an array.
    * @param result ResultSet, assumed to contain zero or more rows from the
    * ip table, using their default column names. The ResultSet is assumed
    * to be positioned beforeFirst.
    * @return Array ofIpy objects
    * @throws SQLException on error
    */
   public static Ip[] load(ResultSet result) throws SQLException {
      List<Ip> ips = new ArrayList<Ip>();
      while (result.next()) {
         ips.add(new Ip(result));
      }
      return ips.toArray(new Ip[0]);
   }

   /**
    * Inserts this Ip object into the ip table, using the passed
    * DataBaseConnection.
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @return int id assigned by the database to the new row.
    * @throws Exception on error.
    */
   public int insert(DataBaseConnection dbc) throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
         dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      new Query(DBUtil.IP_INSERT)
           .set("userId", userId)
           .set("address", address)
           .set("description", description)
           .dbUpdate(dbc, false);
      id = DBUtil.getIpLid(dbc);
      return id;
   }

   /**
    * Updates this IP object in the
    * ip table, using the passed DataBaseConnection object.
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @throws Exception on error.
    */
   public void update(DataBaseConnection dbc) throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
         dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      new Query(DBUtil.IP_UPDATE)
           .set("id", id)
           .set("userId", userId)
           .set("address", address)
           .set("description", description)
           .dbUpdate(dbc);
   }

   /**
    * Deletes this Ip object from the ip table, using the passed
    * DataBaseConnection
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @throws Exception on error.
    */
   public void delete(DataBaseConnection dbc) throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
         dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      new Query(DBUtil.IP_DELETE)
              .set("id", id)
              .dbUpdate(dbc);
   }

 public static class Comp implements Comparator<Ip> {

      private String col;
      private boolean asc;
      public Comp(String columnName, boolean ascending) {
         col = columnName;
         asc = ascending;
      }

      @Override
      public int compare(Ip one, Ip two) {
         if (col == null) return 0;
         if (col.equalsIgnoreCase("address")) {
            return asc ?
               Util.compareIp(one.getAddress(), two.getAddress()):
               Util.compareIp(two.getAddress(), one.getAddress());
         } else if (col.equalsIgnoreCase("description")) {
            return asc ?
               one.getDescription().compareToIgnoreCase(two.getDescription()) :
               two.getDescription().compareToIgnoreCase(one.getDescription()) ;
         } else return 0;
      }

   } // EO Company Comparator inner class

} // EO Ip class
