/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.erl.IHETools.SyslogBrowser.db;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import edu.wustl.mir.erl.IHETools.Util.DBUtil;
import edu.wustl.mir.erl.IHETools.Util.DataBaseConnection;
import edu.wustl.mir.erl.IHETools.Util.Query;

/**
 * Implementation for company table in admin DB. Includes:<ul>
 * <li/>Object representation of table row.
 * <li/>Constructor and methods to build objects from JDBC
 * {@link java.sql.ResultSet ResultSet}
 * <li/>Insert, delete, and update methods.
 * <li/>Comparator for sorting arrays of Company objects
 * <li/>toString method useful for inserting object in log.</ul>
 * @author rmoult01
 */
public class Company implements Serializable {
   static final long serialVersionUID = 1L;

   private int id = 0;
   private String name = "";
   private String contact = "";
   private String contactEmail = "";
   private String contactPhone = "";
   
   private boolean selected = false;
   
   public Company() {}

   /**
    * Constructor builds Company object from the current row of the passed
    * ResultSet.
    * @param result ResultSet. Must be positioned at row for which Company
    * object is desired.
    * @throws SQLException on error.
    */
   public Company(ResultSet result) throws SQLException {
      id = result.getInt("id");
      name = result.getString("name");
      contact = result.getString("contact");
      contactEmail = result.getString("contact_email");
      contactPhone = result.getString("contact_phone");
   }

   public String getContact() {
      return contact;
   }

   public void setContact(String contact) {
      this.contact = contact;
   }

   public String getContactEmail() {
      return contactEmail;
   }

   public void setContactEmail(String contactEmail) {
      this.contactEmail = contactEmail;
   }

   public String getContactPhone() {
      return contactPhone;
   }

   public void setContactPhone(String contactPhone) {
      this.contactPhone = contactPhone;
   }

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }



   public boolean isSelected() {
      return selected;
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
   }
   
   public String toString() {
      return "[Company: id=" + id + " name=" + name + " contact=" + contact + 
              " contact_email=" + contactEmail + 
              " contact_phone=" + contactPhone + 
              "]";
   }
   
   /**
    * Loads the companies in a JDBC ResultSet into Company objects and returns
    * them as an array.
    * @param result ResultSet, assumed to contain zero or more rows from the
    * Company table, using their default column names. The ResultSet is assumed
    * to be positioned beforeFirst.
    * @return Array of Company objects
    * @throws SQLException on error
    */
   public static Company[] load(ResultSet result) throws SQLException {
      List<Company> companies = new ArrayList<Company>();
      while (result.next()) {
         companies.add(new Company(result));
      }
      return companies.toArray(new Company[0]);
   }
   /**
    * Inserts this {@link gov.nist.syslog.db.Company Company} object into the
    * company table, using the passed 
    * {@link gov.nist.syslog.util.DataBaseConnection DataBaseConnection} 
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @return int id assigned by the database to the new row.
    * @throws Exception on error.
    */
   public int insert(DataBaseConnection dbc) throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
         dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      new Query(DBUtil.COMPANY_INSERT)
           .set("name", name)
           .set("contact", contact)
           .set("contactEmail", contactEmail)
           .set("contactPhone", contactPhone)
           .dbUpdate(dbc, false);
      id = DBUtil.getCompanyLid(dbc);
      return id;
   }
   /**
    * Updates this {@link gov.nist.syslog.db.Company Company} object in the
    * company table, using the passed DataBaseConnection object.
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @throws Exception on error.
    */
   public void update(DataBaseConnection dbc) throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
         dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      new Query(DBUtil.COMPANY_UPDATE)
           .set("id", id)
           .set("name", name)
           .set("contact", contact)
           .set("contactEmail", contactEmail)
           .set("contactPhone", contactPhone)
           .dbUpdate(dbc);
   }

   /**
    * Deletes this {@link gov.nist.syslog.db.Company Company} object from the
    * Company table, using the passed
    * {@link gov.nist.syslog.util.DataBaseConnection DataBaseConnection}
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @throws Exception on error.
    */
   public void delete(DataBaseConnection dbc) throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
         dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      new Query(DBUtil.COMPANY_DELETE)
              .set("id", id)
              .dbUpdate(dbc);
   }

   /**
    * Comparator class; implements compare for "name" and "contact"
    */
   public static class Comp implements Comparator<Company> {

      private String col;
      private boolean asc;
      public Comp(String columnName, boolean ascending) {
         col = columnName;
         asc = ascending;
      }

      @Override
      public int compare(Company one, Company two) {
         if (col == null) return 0;
         if (col.equalsIgnoreCase("name")) {
            return asc ?
               one.getName().compareToIgnoreCase(two.getName()) :
               two.getName().compareToIgnoreCase(one.getName()) ;
         } else if (col.equalsIgnoreCase("contact")) {
            return asc ?
               one.getContact().compareToIgnoreCase(two.getContact()) :
               two.getContact().compareToIgnoreCase(one.getContact()) ;
         } else return 0;
      }

   } // EO Company Comparator inner class

} // EO Company class
