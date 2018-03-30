/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.IHETools.SyslogBrowser.view;

import com.icesoft.faces.component.paneltabset.TabChangeEvent;
import edu.wustl.mir.erl.IHETools.SyslogBrowser.db.Company;
import edu.wustl.mir.erl.IHETools.Util.*;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Comparator;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.apache.log4j.Logger;

/**
 * Session bean for maintenance on company table in admin DB.
 * @author rmoult01
 */
@ManagedBean
@ViewScoped
public class CompanyBean implements Serializable  {
   static final long serialVersionUID = 1L;

   private Logger syslog = Util.getLog();
   private ApplicationBean applicationBean =
           (ApplicationBean) FacesUtil.getManagedBean("applicationBean");
   private DataBaseConnection dbc = null;
   private static final String entity = "Company";

   public static String getEntity() {
      return entity;
   }



    /* Creates a new instance of CompanyBean */
    public CompanyBean() throws Exception {
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

   private Company newEntity;
   public Company getNewEntity() {return newEntity;}
   public void setNewEntity(Company newEntity) {this.newEntity = newEntity;}

   public final void initializeAdd() {
      newEntity = new Company();
   }

   public void addOK() {
      Valid v = new Valid();
      try {
         v.startValidations();
         v.NB(entity + " name", newEntity.getName());
         v.NB("Contact", newEntity.getContact());
         v.Email("Contact Email", newEntity.getContactEmail(), false);
         if (v.isErrors()) return;
         newEntity.insert(dbc);
         applicationBean.setCompaniesStale();
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

   private Company chgEntity = null;   // entity selected for changes
   public Company getChg() {return chgEntity;}
   public void setChg(Company chgEntity) {this.chgEntity = chgEntity;}

   private Company[] allEntities;
   public Company[] getAll() {
      if (!sortColumnName.equals(oldSortColumnName) ||
              ascending != oldAscending) {
         oldSortColumnName = sortColumnName;
         oldAscending = ascending;
         Comparator<Company> comp = new Company.Comp(sortColumnName, ascending);
         Arrays.sort(allEntities, comp);
      }
      return allEntities;
   }
   public void setAll(Company[] all) {this.allEntities = all;}

    /*
   * 1 = begin; No entity selected
   * 2 = entity selected; No action to take selected
   * 3 = Modify selected entity
   * 4 = Delete selected entity
   */
  private int currentChangeStep = 1;
  public boolean renderSelect()         { return currentChangeStep <= 2; }
  public boolean renderCommandButtons() { return currentChangeStep == 2; }
  public boolean renderMessage1()       { return currentChangeStep == 1; }
  public boolean renderMessage2()       { return currentChangeStep == 2; }
  public boolean renderModify()         { return currentChangeStep == 3; }
  public boolean renderDelete()         { return currentChangeStep == 4; }

   public final void initializeChg() {
      if (chgEntity != null) chgEntity.setSelected(false);
      try {
         ResultSet rslt = new Query(DBUtil.SCOMPANY_ALL).dbQuery(dbc);
         allEntities = Company.load(rslt);
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
      for (Company s : allEntities) {
         if (s.isSelected()) {
            chgEntity = s;
            currentChangeStep = 2;
            return;
         }
      }
      currentChangeStep = 1;
   }

   public void clearSelections() {
      for (Company s : allEntities) s.setSelected(false);
   }

   //---------- Action to take selections on main chg screen

   public void chg() {
      currentChangeStep = 3;
   }

   public void del() {
      currentChangeStep = 4;
   }

   //----------------- Actions from modify screen

   public void chgOk() {
      Valid v = new Valid();
      try {
         v.startValidations();
         v.NB("Company name", chgEntity.getName());
         v.NB("Contact", chgEntity.getContact());
         if (v.isErrors()) return;
         chgEntity.update(dbc);
         applicationBean.setCompaniesStale();
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

   public void delOk() {
      try {
         chgEntity.delete(dbc);
         applicationBean.setCompaniesStale();
         initializeChg();
      } catch (Exception e) {
         String em = "delOk() " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
      }
   }

   static final String[] messages = {
      "Select Company to Modify",
      "Choose action to take for Company ",
      "<b>Warning:</b> Delete of a Company cannot be undone.<br/>" +
      "Deleting a company automatically and permanently deletes all" +
      "users and Ip address information for that company."
   };
   public String message(int messageNumber) {
      return messages[messageNumber];
   }

} // EO bean class
