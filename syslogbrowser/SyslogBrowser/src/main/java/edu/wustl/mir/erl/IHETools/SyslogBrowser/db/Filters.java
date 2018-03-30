/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.erl.IHETools.SyslogBrowser.db;

import edu.wustl.mir.erl.IHETools.Util.DBUtil;
import edu.wustl.mir.erl.IHETools.Util.DataBaseConnection;
import edu.wustl.mir.erl.IHETools.Util.Query;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.Date;
/**
 * Implementation for filters table in admin DB. Includes:<ul>
 * <li/>Object representation of table row.
 * <li/>Constructor and method to build objects from JDBC
 * {@link java.sql.ResultSet ResultSet}
 * <li/>Methods to reset filters to default, and save current filters.
 * @author rmoult01
 */
public class Filters implements Serializable {
   static final long serialVersionUID = 1L;

   private int id = 0;   // This is user id of user
   private String address;
   private String port;
   private Date startTime;
   private Date endTime;
   private String eventType;
   private String eventId;
   private Integer recordLimit;

   private Filters() {
      defaultFilters();
   }

   /**
    * Constructor builds Filtersp object from the current row of the passed
    * ResultSet.
    * @param result ResultSet. Must be positioned at row for which Ip
    * object is desired.
    * @throws Exception on error.
    */
   private Filters(ResultSet r) throws Exception {
      id = r.getInt("id");
      address = r.getString("address");
      port = r.getString("port");
      eventType = r.getString("event_type");
      eventId = r.getString("event_id");
      startTime = r.getTimestamp("start_time");
      endTime = r.getTimestamp("end_time");
      recordLimit = r.getInt("record_limit");
   }

   /**
    * Returns the current Browser filter values for this user. On first access,
    * returns default values.
    * @param userId the users record dba for this user.
    * @param dbc DataBaseConnection object, or null
    * @return Filters object with current filter values for this user
    * @throws Exception on DB access error
    */
   public static Filters getFilters(int userId, DataBaseConnection dbc)
      throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
            dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      while (true) {
         //-------------------------------- Pull existing filters, if there
         ResultSet result = new Query(DBUtil.SFILTERS).set("id", userId).dbQuery(dbc);
         if (result.next()) return new Filters(result);
         //--------------- Create filter record, then loop to pull it out.
         new Query(DBUtil.FILTERS_INSERT).set("id", userId).dbUpdate(dbc);
      }
   }

   /**
    * saves current filter values for this user.
    * @param dbc DataBaseConnection object, or null
    * @throws Exception on DB access error
    */
   public void saveFilters(DataBaseConnection dbc) throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
            dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      new Query(DBUtil.FILTERS_UPDATE)
          .set("id", id)
          .set("address", address)
          .set("port", port)
          .set("eventType", eventType)
          .set("eventId", eventId)
          .set("startTime", startTime)
          .set("endTime", endTime)
          .set("recordLimit", recordLimit)
          .dbUpdate(dbc);
   }
   /**
    * Reset filter values to defaults
    */
   public final void defaultFilters() {
      address     = "Any";
      port        = "Any";
      eventType   = "Any";
      eventId     = "Any";
      startTime   = null;
      endTime     = null;
      recordLimit = 10;
   }

   public String getAddress() {
      return address;
   }

   public void setAddress(String address) {
      this.address = address;
   }

   public Date getEndTime() {
      return endTime;
   }

   public void setEndTime(Date endTime) {
      this.endTime = endTime;
   }

   public String getEventId() {
      return eventId;
   }

   public void setEventId(String eventId) {
      this.eventId = eventId;
   }

   public String getEventType() {
      return eventType;
   }

   public void setEventType(String eventType) {
      this.eventType = eventType;
   }

   public String getPort() {
      return port;
   }

   public void setPort(String port) {
      this.port = port;
   }

   public Integer getRecordLimit() {
      return recordLimit;
   }

   public void setRecordLimit(Integer recordLimit) {
      this.recordLimit = recordLimit;
   }

   public Date getStartTime() {
      return startTime;
   }

   public void setStartTime(Date startTime) {
      this.startTime = startTime;
   }

}  // EO Filters class
