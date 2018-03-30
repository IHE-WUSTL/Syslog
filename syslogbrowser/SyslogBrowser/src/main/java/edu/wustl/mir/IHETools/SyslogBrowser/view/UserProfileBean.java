/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.IHETools.SyslogBrowser.view;

import edu.wustl.mir.erl.IHETools.SyslogBrowser.db.Ip;
import edu.wustl.mir.erl.IHETools.SyslogBrowser.db.User;
import edu.wustl.mir.erl.IHETools.Util.*;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Comparator;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * backing bean for user profile mainteance: UserProfile.xhtml
 * @author rmoult01
 */
@ManagedBean
@ViewScoped
public class UserProfileBean implements Serializable {
   static final long serialVersionUID = 1L;

   private Logger syslog = Util.getLog();
   private SessionBean sessionBean =
           (SessionBean) FacesUtil.getManagedBean("sessionBean");
   private DataBaseConnection dbc = null;
   private static final String entity = "User";

   public static String getEntity() {
      return entity;
   }

    /** 
     * Creates a new instance of UserProfileBean
     * @throws Exception on error
     */
    public UserProfileBean() throws Exception {
       dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
       ResultSet rslt = new Query(DBUtil.SUSER_ID)
           .set("id", sessionBean.getUserDba())
           .dbQuery(dbc);
       rslt.next();
       user = new User(rslt, true, true, false, dbc);
    }

    public void del() { currentChangeStep = 2; }
    public void chgPw() { currentChangeStep = 3; }
    public void mntIps() { currentChangeStep = 4; }
    

  private int currentChangeStep = 1;
  public boolean renderModify()         { return currentChangeStep == 1; }
  public boolean renderDelete()         { return currentChangeStep == 2; }
  public boolean renderChgPassword()    { return currentChangeStep == 3; }
  public boolean renderMaintainIps()    { return currentChangeStep == 4; }
  public boolean disableAddIpButtons()  {
      return StringUtils.trimToEmpty(newIp.getAddress()).length() == 0;
   }
  public boolean disableDelIpButtons()  { return chgIp == null; }

  private User user;
  private Ip newIp = new Ip();
  private Ip chgIp = null;
  private Ip[] ips = null;

   //----------------------------------------------------- Column sort
   private String sortColName = "address", oldSortColName = "Not sorted";
   private boolean ascend = true, oldAscend = false;
   public String getSortColName() {return sortColName;}
   public void setSortColName(String sortColName) {this.sortColName = sortColName;}
   public boolean isAscend() {return ascend;}
   public void setAscend(boolean ascend) {this.ascend = ascend;}

   public Ip[] getIps() {
      if (ips == null) {
         ips = user.getIps();
         oldSortColName = "Not sorted";
      }
      if (!sortColName.equals(oldSortColName) ||
           ascend != oldAscend) {
         oldSortColName = sortColName;
         oldAscend = ascend;
         Comparator<Ip> comp = new Ip.Comp(sortColName, ascend);
         Arrays.sort(ips, comp);
      }
      return ips;
   }
   public void setIps(Ip[] ips) {
      this.ips = ips;
   }

   public Ip getChgIp() {
      return chgIp;
   }

   public void setChgIp(Ip chgIp) {
      this.chgIp = chgIp;
   }

   public Ip getNewIp() {
      return newIp;
   }

   public void setNewIp(Ip newIp) {
      this.newIp = newIp;
   }

   public User getUser() {
      return user;
   }

   public void setUser(User user) {
      this.user = user;
   }


  
  public void chgOk() {
      Valid v = new Valid();
      try {
         v.startValidations();
         v.NB("User ID", user.getUserId());
         ResultSet rslt = new Query(DBUtil.SUSER_DUPID)
             .set("id", user.getId())
             .set("userId", user.getUserId())
             .dbQuery(dbc);
         if (rslt.next())
            v.error("User ID", "already in use, choose another");
         v.NB("User Name", user.getUserName());
         v.Email("Email", user.getUserEmail(), false);
         if (v.isErrors()) return;
         user.update(dbc);
         currentChangeStep = 1;
      } catch (Exception e) {
         String em = "chgOK() " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
     }
   }
  
  public void delOk() {
      try {
         user.delete(dbc);
         sessionBean.logoff(null);
      } catch (Exception e) {
         String em = "delOk() " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
      }
   }
  
  public void chgPwOk() {
      Valid v = new Valid();
      try {
         v.startValidations();
         v.NB("Password", user.getPlainTextPassword());
         if (!user.getPlainTextPassword().equals(user.getPlainTextPasswordRepeat()))
            v.error("Passwords", "do not match");
         if (v.isErrors()) return;
         user.setPassword(Util.hashPw(user.getPlainTextPassword()));
         user.update(dbc);
         currentChangeStep = 1;
      } catch (Exception e) {
         String em = "chgPwOk() " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
     }
   }
  
  public void chgDelCancel() {
     currentChangeStep = 1;
  }
  
  public void addIpCancel() {
     newIp = new Ip();
  }
  
  public void ipDone() {
     currentChangeStep = 1;
  }
  
  public void Select() {
      for (Ip ip : ips) {
         if (ip.isSelected()) {
            chgIp = ip;
            return;
         }
      }
   }

   public void clearSelect() {
      for (Ip ip: ips) ip.setSelected(false);
      chgIp = null;
   }
  
   /**
    * action listener for add new ipv4 to user. Validates that new ip is not
    * blank or just whitespace, that it is a valid ipv4 address in dot format,
    * that it does not duplicate an address already on file for this user, and
    * that it does not duplicate an address already assigned to one or more
    * users belonging to a different company than this user. Also normalizes the
    * address by removing any excess whitespace or unneeded leading zeros.
    */
   public void addIpOk() {
      String n = null;
      //--------------- Validations
      Valid v = new Valid();
      try {
         //------------------- can't be just blank, whitespace
         v.NB("IP Address", newIp.getAddress());
         try {
            //-------------------- Valid ipv4 (also normalizes)
            n = Util.validateFormatIpv4(newIp.getAddress());
         } catch (Exception e) {
            v.error("IP Address", e.getMessage());
            return;
         }
         //-------------------------------------------- not AOF
         Ip[] curr = user.getIps();
         for (Ip ip : curr) {
            if (Util.compareIp(n, ip.getAddress()) == 0)
               v.error("IP address already on file");
         }
         //------------------------ not in use by another company
         ResultSet rslt = new Query(DBUtil.SIP_ADD_OK)
             .set("ipAddress", newIp.getAddress())
             .set("companyId", user.getCompanyId())
             .dbQuery(dbc);
         if (rslt.next())
            v.error("IP address dup of address for company" +
                    rslt.getString("name"));
         if (v.isErrors()) return;
         //------------------------------------- store new ip
         newIp.setAddress(n);
         user.addIp(dbc, newIp);
         newIp = new Ip();
         ips = null;
      } catch (Exception e) {
         String em = "Add ip failure: " + e.getMessage();
         syslog.warn(em);
         v.error(em);
      }
   }

  /**
    * Deletes selected Ip from list for this user.
    */
   public void delIpOk() {
      try {
         user.deleteIp(dbc, chgIp);
      } catch (Exception e) {
         String em = "error deleting ip: " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
      }
      ips = null;
      chgIp = null;
   }
   static final String[] messages = {
      "<b>Warning:</b> Delete of a User cannot be undone.<br/>" +
      "Deleting a user automatically and permanently deletes all " +
      "IP address information for that user."
   };
   public String message(int messageNumber) {
      return messages[messageNumber];
   }

} // EO UserProfileBean class
