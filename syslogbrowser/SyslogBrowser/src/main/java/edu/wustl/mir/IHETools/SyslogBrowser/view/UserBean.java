/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.IHETools.SyslogBrowser.view;

import com.icesoft.faces.component.paneltabset.TabChangeEvent;
import edu.wustl.mir.erl.IHETools.SyslogBrowser.db.*;
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
 * Session bean for maintenance on user table in admin DB.
 * @author rmoult01
 */
@ManagedBean
@ViewScoped
public class UserBean implements Serializable  {
   static final long serialVersionUID = 1L;

   private Logger syslog = Util.getLog();
   private DataBaseConnection dbc = null;
   private static final String entity = "User";

   public static String getEntity() {
      return entity;
   }



    /* Creates a new instance of UserBean */
    public UserBean() throws Exception {
       selectedTab = ADD_TAB_SELECTED;
       dbc = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
       initializeAdd();
       initializeChg();
    }

   /****************************************************************************
    *********************************** panelTabSet Controls
    ***************************************************************************/

   private static final int ADD_TAB_SELECTED = 0;
   private static final int CHG_TAB_SELECTED = 1;
   private int selectedTab;
   public int getSelectedTab() {return selectedTab; }
   public void setSelectedTab(int s) {selectedTab = s; }

   public void tabChangeListener(TabChangeEvent tabChangeEvent) {
      switch (selectedTab) {
         case ADD_TAB_SELECTED:
            initializeAdd();
            break;
         case CHG_TAB_SELECTED:
            initializeChg();
            break;
         default:
            syslog.warn("xhtml returned invalid tab selected value: " +
                    selectedTab);
      }
   }

   /****************************************************************************
    *********************************** Add tab methods
    ***************************************************************************/

   private User newEntity;
   public User getNewEntity() {return newEntity;}
   public void setNewEntity(User newEntity) {this.newEntity = newEntity;}

   public final void initializeAdd() {
      newEntity = new User();
   }

   public void addOK() {
      Valid v = new Valid();
      try {
         v.startValidations();
         if (newEntity.getCompanyId() <= 0)
            v.error("Company", "No company selected");
         v.NB("User ID", newEntity.getUserId());
         ResultSet rslt = new Query(DBUtil.SUSER_USERID)
             .set("userId", newEntity.getUserId())
             .dbQuery(dbc);
         if (rslt.next()) 
            v.error("User ID", "already in use, choose another");
         v.NB("Password", newEntity.getPlainTextPassword());
         if (!newEntity.getPlainTextPassword().equals(newEntity.getPlainTextPasswordRepeat()))
            v.error("Passwords", "do not match");
         v.NB("User Name", newEntity.getUserName());
         v.Email("Email", newEntity.getUserEmail(), false);
         if (v.isErrors()) return;
         newEntity.setPassword(Util.hashPw(newEntity.getPlainTextPassword()));
         newEntity.insert(dbc);
         initializeAdd();
      } catch (Exception e) {
         String em = "Error during add " + entity + ": ";
         syslog.warn(em + e.getMessage());
         v.error(em, e.getMessage());
      }
   }

   public void addCancel() {
      initializeAdd();
   }


   /****************************************************************************
    *********************************** Change tab methods
    ***************************************************************************/

   //----------------------------------------------------- Column sort
   private String sortColumnName, oldSortColumnName;
   private boolean ascending, oldAscending;
   public String getSortColumnName() {return sortColumnName;}
   public void setSortColumnName(String sortColumnName) {this.sortColumnName = sortColumnName;}
   public boolean isAscending() {return ascending;}
   public void setAscending(boolean ascending) {this.ascending = ascending;}

   private User chgEntity = null;   // entity selected for changes
   public User getChg() {return chgEntity;}
   public void setChg(User chgEntity) {this.chgEntity = chgEntity;}

   private User[] allEntities;
   public User[] getAll() {
      if (!sortColumnName.equals(oldSortColumnName) ||
              ascending != oldAscending) {
         oldSortColumnName = sortColumnName;
         oldAscending = ascending;
         Comparator<User> comp = new User.Comp(sortColumnName, ascending);
         Arrays.sort(allEntities, comp);
      }
      return allEntities;
   }
   public void setAll(User[] all) {this.allEntities = all;}

   /*
    * 1 = begin; No entity selected
    * 2 = entity selected; No action to take selected
    * 3 = Modify selected entity
    * 4 = Delete selected entity
    * 5 = Change password for selected entity
    * 6 = Maintain ipv4 addresses for selected entity
    */
  private int currentChangeStep = 1;
  public boolean renderSelect()         { return currentChangeStep <= 2; }
  public boolean renderCommandButtons() { return currentChangeStep == 2; }
  public boolean renderMessage1()       { return currentChangeStep == 1; }
  public boolean renderMessage2()       { return currentChangeStep == 2; }
  public boolean renderModify()         { return currentChangeStep == 3; }
  public boolean renderDelete()         { return currentChangeStep == 4; }
  public boolean renderChgPassword()    { return currentChangeStep == 5; }
  public boolean renderMaintainIps()    { return currentChangeStep == 6; }
  public boolean disableAddIpButtons()  {
      return StringUtils.trimToEmpty(newIp.getAddress()).length() == 0;
   }
  public boolean disableDelIpButtons()  { return chgIp == null; }

   public final void initializeChg() {
      if (chgEntity != null) chgEntity.setSelected(false);
      try {
         ResultSet rslt = new Query(DBUtil.SUSER_ALL).dbQuery(dbc);
         allEntities = User.load(rslt, true, false, dbc);
         sortColumnName = oldSortColumnName = "name";
         ascending = oldAscending = true;
         currentChangeStep = 1;
      } catch (Exception e) {
         String em = "initializeChg() " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
      }
   }

   public void Selected() {
      for (User s : allEntities) {
         if (s.isSelected()) {
            chgEntity = s;
            currentChangeStep = 2;
            return;
         }
      }
      currentChangeStep = 1;
   }

   public void clearSelections() {
      for (User s : allEntities) s.setSelected(false);
   }

   //---------- Action to take selections on main chg screen

   public void chg() {
      currentChangeStep = 3;
   }

   public void del() {
      currentChangeStep = 4;
   }

   public void chgPassword() {
      currentChangeStep = 5;
   }

   public void maintIps() {
      currentChangeStep = 6;
      newIp = new Ip();
      try {chgEntity.loadIps(dbc);
      } catch (Exception e) {
         syslog.warn("unable to load ips for user " + chgEntity.getUserId() +
                 " " + e.getMessage());
      }
   }

   /****************************************************************************
    *********************************** Change tab - Modify selected Entity
    ***************************************************************************/

   public void chgOk() {
      Valid v = new Valid();
      try {
         v.startValidations();
         v.NB("User ID", chgEntity.getUserId());
         ResultSet rslt = new Query(DBUtil.SUSER_DUPID)
             .set("id", chgEntity.getId())
             .set("userId", chgEntity.getUserId())
             .dbQuery(dbc);
         if (rslt.next()) 
            v.error("User ID", "already in use, choose another");
         v.NB("User Name", chgEntity.getUserName());
         v.Email("Email", chgEntity.getUserEmail(), false);
         if (v.isErrors()) return;
         chgEntity.update(dbc);
         initializeChg();
      } catch (Exception e) {
         String em = "chgOK() " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
     }
   }

   public void chgDelCancel() {
       initializeChg();
   }

   /****************************************************************************
    *********************************** Change tab - Delete selected Entity
    ***************************************************************************/

   public void delOk() {
      try {
         chgEntity.delete(dbc);
         initializeChg();
      } catch (Exception e) {
         String em = "delOk() " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
      }
   }

   /****************************************************************************
    *********************************** Change tab - change pw selected Entity
    ***************************************************************************/
   public void chgPwOk() {
      Valid v = new Valid();
      try {
         v.startValidations();
         v.NB("Password", chgEntity.getPlainTextPassword());
         if (!chgEntity.getPlainTextPassword().equals(chgEntity.getPlainTextPasswordRepeat()))
            v.error("Passwords", "do not match");
         if (v.isErrors()) return;
         chgEntity.setPassword(Util.hashPw(chgEntity.getPlainTextPassword()));
         chgEntity.update(dbc);
         initializeChg();
      } catch (Exception e) {
         String em = "chgPwOk() " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
     }
   }

   /****************************************************************************
    *********************************** Change tab - Maint ipv4 selected Entity
    ***************************************************************************/
   //----------------------------------------------------- Column sort
   private String sortColName = "address", oldSortColName = "Not sorted";
   private boolean ascend = true, oldAscend = false;
   public String getSortColName() {return sortColName;}
   public void setSortColName(String sortColName) {this.sortColName = sortColName;}
   public boolean isAscend() {return ascend;}
   public void setAscend(boolean ascend) {this.ascend = ascend;}

   private Ip[] ips = null;
   public Ip[] getIps() {
      if (ips == null) {
         ips = chgEntity.getIps();
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

   private Ip newIp;
   private Ip chgIp;

   public Ip getNewIp() {
      return newIp;
   }

   public void setNewIp(Ip newIp) {
      this.newIp = newIp;
   }

   public Ip getChgIp() {
      return chgIp;
   }

   public void setChgIp(Ip chgIp) {
      this.chgIp = chgIp;
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

   public void ipDone() {
      initializeChg();
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
         Ip[] curr = chgEntity.getIps();
         for (Ip ip : curr) {
            if (Util.compareIp(n, ip.getAddress()) == 0)
               v.error("IP address", "already on file");
         }
         //------------------------ not in use by another company
         ResultSet rslt = new Query(DBUtil.SIP_ADD_OK)
             .set("ipAddress", newIp.getAddress())
             .set("companyId", chgEntity.getCompanyId())
             .dbQuery(dbc);
         if (rslt.next())
            v.error("IP address", "dup of address for company" +
                    rslt.getString("name"));
         if (v.isErrors()) return;
         //------------------------------------- store new ip
         newIp.setAddress(n);
         chgEntity.addIp(dbc, newIp);
         newIp = new Ip();
         ips = null;
      } catch (Exception e) {
         syslog.warn("Add ip failure: " + e.getMessage());
         v.error("Add ip failure: ", e.getMessage());
      }
   }

   public void addIpCancel() {
      newIp = new Ip();
   }

   /**
    * Deletes selected Ip from list for this user.
    */
   public void delIpOk() {
      try {
         chgEntity.deleteIp(dbc, chgIp);
      } catch (Exception e) {
         String em = "error deleting ip: " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
      }
      ips = null;
      chgIp = null;
   }


   static final String[] messages = {
      "Select User to Modify",
      "Choose action to take for selected User ",
      "<b>Warning:</b> Delete of a User cannot be undone.<br/>" +
      "Deleting a user automatically and permanently deletes all " +
      "IP address information for that user."
   };
   public String message(int messageNumber) {
      return messages[messageNumber];
   }

} // EO bean class
