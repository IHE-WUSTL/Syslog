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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation for users table in admin DB. Includes:<ul>
 * <li/>Object representation of table row.
 * <li/>Constructor and methods to build objects from JDBC
 * {@link java.sql.ResultSet ResultSet}
 * <li/>Insert, delete, and update methods.
 * <li/>Comparator for sorting arrays of User objects
 * <li/>toString method useful for inserting object in log.</ul>
 * @author rmoult01
 */
public class User implements Serializable {
   static final long serialVersionUID = 1L;

   private int id = 0;
   private int companyId = 0;
   private String userId = "";
   private String plainTextPassword = "";
   private String plainTextPasswordRepeat = "";
   private String password = "";
   private String userName = "";
   private String userEmail = "";
   private String userPhone = "";
   private boolean admin = false;

   private boolean selected = false;
   private Company company = null;
   private Ip[] ips = null;
   private Filters filters = null;

   public User() {}

   /**
    * Constructor builds User object from the current row of the passed
    * ResultSet.
    * @param result ResultSet. Must be positioned at row for which user
    * object is desired.
    * @param companyEager boolean. If true, {@link gov.nist.syslog.db.Company
      Company} object for this User is loaded also.
    * @param ipEager boolean. If true, {@link gov.nist.syslog.db.IP Ip} objects
    * for this User are loaded also.
    * @param filtersEager boolean. If true, {@link gov.nist.syslog.db.Filter
      Filter} objects for this User are loaded also.
    * @param dbc {@link gov.nist.syslog.util.DataBaseConnection DataBaseConnection}
    * object
    * @throws Exception on error.
    */
   public User(ResultSet result, boolean companyEager, boolean ipEager,
           boolean filtersEager,
           DataBaseConnection dbc) throws Exception {
      id = result.getInt("id");
      companyId = result.getInt("company_id");
      userId = result.getString("user_id");
      password = result.getString("password");
      userName = result.getString("user_name");
      userEmail = result.getString("user_email");
      userPhone = result.getString("user_phone");
      admin = result.getBoolean("admin");
      if (companyEager) loadCompany(dbc);
      if (ipEager) loadIps(dbc);
      if (filtersEager) filters = Filters.getFilters(id, dbc);
   }
   /**
    * loads {@link gov.nist.syslog.db.Company Company} for this User, if not
    * already loaded, using the passed DataBasConnection object
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @throws Exception on error.
    */
   public final void loadCompany(DataBaseConnection dbc) throws Exception {
      if (company != null) return;
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
            dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      ResultSet rslt = new Query(DBUtil.SCOMPANY_BY_ID)
         .set("id", companyId)
         .dbQuery(dbc);
      rslt.next();
      company = new Company(rslt);
   }

   public boolean isAdmin() {
      return admin;
   }

   public void setAdmin(boolean admin) {
      this.admin = admin;
   }

   public String getAdministrator() {
      if (admin) return "True";
      else return "False";
   }

   public Company getCompany() {
      if (company == null) {
         try {
            loadCompany(null);
         } catch (Exception e) {
            Util.getLog().warn("error loading user company: " + e.getMessage());
         }
      }
      return company;
   }

   public void setCompany(Company company) {
      this.company = company;
   }

   public int getCompanyId() {
      return companyId;
   }

   public void setCompanyId(int companyId) {
      this.companyId = companyId;
   }

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public Ip[] getIps() {
      if (ips == null) {
         try {
            loadIps(null);
         } catch (Exception e) {
            Util.getLog().warn("error loading user ips: " + e.getMessage());
         }
      }
      return ips;
   }

   public void setIps(Ip[] ips) {
      this.ips = ips;
   }

   public String getPassword() {
      return password;
   }

   public void setPassword(String password) {
      this.password = password;
   }

   public String getPlainTextPassword() {
      return plainTextPassword;
   }

   public void setPlainTextPassword(String plainTextPassword) {
      this.plainTextPassword = plainTextPassword;
   }

   public String getPlainTextPasswordRepeat() {
      return plainTextPasswordRepeat;
   }

   public void setPlainTextPasswordRepeat(String plainTextPasswordRepeat) {
      this.plainTextPasswordRepeat = plainTextPasswordRepeat;
   }

   

   public boolean isSelected() {
      return selected;
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
   }

   public String getUserEmail() {
      return userEmail;
   }

   public void setUserEmail(String userEmail) {
      this.userEmail = userEmail;
   }

   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getUserName() {
      return userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public String getUserPhone() {
      return userPhone;
   }

   public void setUserPhone(String userPhone) {
      this.userPhone = userPhone;
   }

   public Filters getFilters() {
      try {
         if (filters == null) Filters.getFilters(id, null);
      } catch (Exception e) {
         Util.getLog().warn("error getting filters " + e.getMessage());
      }
      return filters;
   }

   public void setFilters(Filters filters) {
      this.filters = filters;
   }

   /**
    * loads {@link gov.nist.syslog.db.IP IP}s for this User, if not
    * already loaded, using the passed DataBasConnection object
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @throws Exception on error.
    */
   public final void loadIps(DataBaseConnection dbc) throws Exception {
      if (ips != null) return;
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
            dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      ResultSet rslt = new Query(DBUtil.SIP_BY_USER_ID)
         .set("userId", id)
         .dbQuery(dbc);
      ips = Ip.load(rslt);
   }

   public void addIp(DataBaseConnection dbc, Ip ip) throws Exception {
      if (id == 0) throw new Exception("must add user first");
      loadIps(dbc);
      new Query(DBUtil.IP_INSERT)
          .set("userId", id)
          .set("address", ip.getAddress())
          .set("description", ip.getDescription())
          .dbUpdate(dbc, false);
      ip.setId(DBUtil.getIpLid(dbc));
      Ip[] n = new Ip[ips.length + 1];
      System.arraycopy(ips, 0, n, 0, ips.length);
      n[ips.length] = ip;
      ips = n;
   }

   public void deleteIp(DataBaseConnection dbc, Ip ip) throws Exception {
      if (id == 0) throw new Exception("user not added yet");
      if (ip.getId() == 0) throw new Exception("ip not added yet");
      if (ip.getUserId() != id) throw new Exception("ip not for this user");
      new Query(DBUtil.IP_DELETE).set("id", ip.getId()).dbUpdate(dbc);
      loadIps(dbc);
      Ip[] n = new Ip[ips.length -1];
      int ni = 0;
      for (int i = 0; i < ips.length; i++) {
         if (ips[i].getId() != ip.getId()) {
            n[ni] = ips[i];
            ni++;
         }
      }
      ips = n;
   }

    /**
    * Loads the users in a JDBC ResultSet into User objects and returns
    * them as an array.
    * @param result ResultSet, assumed to contain zero or more rows from the
    * Company table, using their default column names. The ResultSet is assumed
    * to be positioned beforeFirst.
    * @param companyEager boolean. If true, {@link gov.nist.syslog.db.Company
      Company} object for this User is loaded also.
    * @param ipEager boolean. If true, {@link gov.nist.syslog.db.IP Ip} objects
    * for this User are loaded also.
    * @param dbc {@link gov.nist.syslog.util.DataBaseConnection DataBaseConnection}
    * object
    * @return Array of User objects
    * @throws Exception on error
    */
   public static User[] load(ResultSet result, boolean companyEager,
           boolean ipEager, DataBaseConnection dbc) throws Exception {
      List<User> users = new ArrayList<User>();
      while (result.next()) {
         users.add(new User(result, companyEager, ipEager, false, dbc));
      }
      return users.toArray(new User[0]);
   }

    /**
    * Inserts this {@link gov.nist.syslog.db.User User} object into the
    * company table, using the passed
    * {@link gov.nist.syslog.util.DataBaseConnection DataBaseConnection}
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @return int id assigned by the database to the new row.
    * @throws Exception on error.
    */
   public int insert(DataBaseConnection dbc) throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
         dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      new Query(DBUtil.USER_INSERT)
           .set("companyId", companyId)
           .set("userId", userId)
           .set("password", password)
           .set("userName", userName)
           .set("userEmail", userEmail)
           .set("userPhone", userPhone)
           .set("admin", admin)
           .dbUpdate(dbc, false);
      id = DBUtil.getUserLid(dbc);
      return id;
   }

   /**
    * Updates this {@link gov.nist.syslog.db.User User} object in the
    * users table, using the passed DataBaseConnection object.
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @throws Exception on error.
    */
   public void update(DataBaseConnection dbc) throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
         dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      new Query(DBUtil.USER_UPDATE)
           .set("id", id)
           .set("companyId", companyId)
           .set("userId", userId)
           .set("password", password)
           .set("userName", userName)
           .set("userEmail", userEmail)
           .set("userPhone", userPhone)
           .set("admin", admin)
           .dbUpdate(dbc);
   }

   /**
    * Deletes this {@link gov.nist.syslog.db.User User} object from the
    * Company table, using the passed DataBaseConnection
    * @param dbc DataBaseConnection. if null or not for admin db one is created.
    * @throws Exception on error.
    */
   public void delete(DataBaseConnection dbc) throws Exception {
      if (dbc == null || !dbc.getDbName().equalsIgnoreCase(DBUtil.DBADMIN))
         dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
      new Query(DBUtil.USER_DELETE)
              .set("id", id)
              .dbUpdate(dbc);
   }

   public static class Comp implements Comparator<User> {

      private enum Comparators { USER, COMPANY }
      private enum Comparisons {
         UID          ("userId",      "userId",   Comparators.USER),
         UNAME        ("userName",    "userName", Comparators.USER),
         ADMIN        ("admin",       "admin",    Comparators.USER),
         CONAME       ("companyName", "name",     Comparators.COMPANY);

         public final String externalColumnName;
         public final String internalColumnName;
         public final Comparators comparatorToUse;

         private Comparisons (String external, String internal, Comparators comparator) {
            externalColumnName = external;
            internalColumnName = internal;
            comparatorToUse = comparator;
         }
         public static Comparisons getComparisonForExternalColumnName(String external) {
            for (Comparisons c : Comparisons.values()) {
               if (c.externalColumnName.equalsIgnoreCase(external)) return c;
            }
            return null;
         }
      } // EO Comparisons enum

      private String col;
      private boolean asc;
      Comparisons comparison;
      private Comparator<Company> companyComparator = null;

      public Comp(String columnName, boolean ascending) {
         asc = ascending;
         comparison = Comparisons.getComparisonForExternalColumnName(columnName);
         if (comparison != null) {
            col = comparison.internalColumnName;
            switch (comparison.comparatorToUse) {
               case COMPANY:
                  companyComparator = new Company.Comp(col, asc);
                  break;
               default:
            }
         }
      }

      @Override
      public int compare(User one, User two) {
         if (col == null) return 0;

         switch (comparison.comparatorToUse) {
            case COMPANY:
               return companyComparator.compare(one.getCompany(), two.getCompany());
            case USER:
               if (col.equalsIgnoreCase(Comparisons.UID.internalColumnName)) {
                  return asc
                          ? one.getUserId().compareToIgnoreCase(two.getUserId())
                          : two.getUserId().compareToIgnoreCase(one.getUserId());
               } else if (col.equalsIgnoreCase(Comparisons.UNAME.internalColumnName)) {
                  return asc
                          ? one.getUserName().compareToIgnoreCase(two.getUserName())
                          : two.getUserName().compareToIgnoreCase(one.getUserName());
               } else if (col.equalsIgnoreCase(Comparisons.ADMIN.internalColumnName)) {
                  return asc
                          ? Util.compareBoolean(one.isAdmin(), two.isAdmin())
                          : Util.compareBoolean(two.isAdmin(), one.isAdmin());
               } else return 0;
            default:
               return 0;
         }
      }
   }

} // EO User class
