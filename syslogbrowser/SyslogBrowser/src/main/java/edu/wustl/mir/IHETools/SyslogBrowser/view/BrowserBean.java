/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.IHETools.SyslogBrowser.view;

import edu.wustl.mir.erl.IHETools.SyslogBrowser.db.Ip;
import edu.wustl.mir.erl.IHETools.SyslogBrowser.db.Syslog;
import edu.wustl.mir.erl.IHETools.SyslogBrowser.db.User;
import edu.wustl.mir.erl.IHETools.Util.*;
import java.io.Serializable;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TimeZone;
import java.util.TreeSet;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

/**
 * Backing session bean for Browser.xhtml
 * @author rmoult01
 */
@ManagedBean
@ViewScoped
public class BrowserBean implements Serializable {
   static final long serialVersionUID = 1L;

   private Logger syslog = Util.getLog();
   private SessionBean sessionBean =
           (SessionBean) FacesUtil.getManagedBean("sessionBean");
   private DataBaseConnection admin = null, msg = null;
   private boolean expandListPanel = false;

   private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

   /** 
    * Creates a new instance of BrowserBean
    * @throws Exception on error
    */
    public BrowserBean() throws Exception {
       userDba = sessionBean.getUserDba();
       admin = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
       msg = DataBaseConnection.getDataBaseConnection(DBUtil.DBNAME);
       initialize();
    }

    private void initialize() throws Exception {
       initializeFilters();
       initializeNoList();
       allEntities = new Syslog[0];
       expandFilterPanel = true;
       expandListPanel = false;
       expandDetailPanel = false;
   }

   //***********************************************************************
   //***********************************************************************
   //*************** Filter panel processing
   //***********************************************************************
   //***********************************************************************

    private int userDba = 0;
   User user = null;
   private boolean expandFilterPanel = true;

   private SelectItem[] ipsSelections;

   private SelectItem[] eventTypeSelections;
   private SelectItem[] eventIdSelections;

   public boolean isIps() {
      return user.getIps().length != 0;
   }
   Ip[] ips;

   public boolean isExpandFilterPanel() {
      return expandFilterPanel;
   }

   public void setExpandFilterPanel(boolean expandFilterPanel) {
      this.expandFilterPanel = expandFilterPanel;
   }

   private void initializeFilters() throws Exception {

       //------------------ load current user incl company, ips, filters
       ResultSet rslt = new Query(DBUtil.SUSER_ID)
           .set("id", userDba)
           .dbQuery(admin);
       rslt.next();
       user = new User(rslt, true, true, true, admin);
       //------- If no Ips set up, can't browse.
       if (user.getIps().length == 0) return;

       //-------------------------- set up IP SelectItems array and selection
       ips = user.getIps();
       Comparator<Ip> comp = new Ip.Comp("address", true);
       Arrays.sort(ips, comp);
       List<SelectItem> items = new ArrayList<>();
       items.add(new SelectItem("Any", "Any"));
       for (Ip ip : ips) {
          items.add(new SelectItem(ip.getAddress(), ip.getAddress()));
       }
       ipsSelections = items.toArray(new SelectItem[0]);

       
       //---------- Set up event type and event id selections
       StringBuilder q =
          new StringBuilder("SELECT DISTINCT event_type, event_id FROM syslog WHERE sender_ip IN (");
       for (int i = 0; i < ips.length; i++) {
          q.append("'").append(ips[i].getAddress()).append("'");
          if ((i + 1) < ips.length) q.append(", ");
       }
       q.append(");");
       ResultSet result = new Query(q).dbQuery(msg);
       NavigableSet<String> types = new TreeSet<>();
       NavigableSet<String> ids   = new TreeSet<>();
       while (result.next()) {
          types.add(result.getString("event_type"));
          ids.add(result.getString("event_id"));
       }
       List<SelectItem> stypes = new ArrayList<>();
       stypes.add(new SelectItem("Any", "Any"));
       Iterator<String> itypes = types.iterator();
       while (itypes.hasNext()) {
          String n = itypes.next();
          stypes.add(new SelectItem(n, n));
       }
       eventTypeSelections = stypes.toArray(new SelectItem[0]);

       List<SelectItem> sids = new ArrayList<>();
       sids.add(new SelectItem("Any", "Any"));
       Iterator<String> iids = ids.iterator();
       while (iids.hasNext()) {
          String n = iids.next();
          sids.add(new SelectItem(n, n));
       }
       eventIdSelections = sids.toArray(new SelectItem[0]);

   } // EO initialize filters method

   public User getUser() {
      return user;
   }

   public void setUser(User user) {
      this.user = user;
   }

	public int getUserDba() {
		return userDba;
	}

	public void setUserDba(int userDba) {
		this.userDba = userDba;
	}

	public void userChange(ValueChangeEvent e) {
		int id = (Integer) e.getNewValue();
		try {
			ResultSet r = new Query(DBUtil.COUNT_IPS_FOR_USER_ID).set("userId",
					id).dbQuery(admin);
			r.next();
			if (r.getInt("count") <= 0)
				throw new Exception("User has no IP addresses entered");
			userDba = id;
		} catch (Exception er) {
			FacesUtil.addErrorMessage(er.getMessage());
			userDba = (Integer) e.getOldValue();
		}
		try {
			initialize();
		} catch (Exception er) {
			FacesUtil.addErrorMessage(er.getMessage());
		}
	}

	public void resetUserSelection() {
		userDba = sessionBean.getUserDba();
		try {
			initialize();
		} catch (Exception er) {
			FacesUtil.addErrorMessage(er.getMessage());
		}

	}

   

   public int getSelectedIp() {
      String addr = user.getFilters().getAddress();
      if (addr == null) return 0;
      for (SelectItem si : ipsSelections) {
         if (Util.compareIp(addr, si.getLabel()) == 0)
            return (Integer) si.getValue();
      }
      user.getFilters().setAddress(null);
      return 0;
   }

   public void setSelectedIp(int selectedIp) {
      String addr = null;
      for (SelectItem si : ipsSelections)
         if ((Integer) si.getValue() == selectedIp) {
            addr = si.getLabel();
            if (addr.equals("Any")) addr = null;
            break;
         }
      user.getFilters().setAddress(addr);
   }

   public SelectItem[] getIpsSelections() {
      return ipsSelections;
   }

   public void setIpsSelections(SelectItem[] ipsSelections) {
      this.ipsSelections = ipsSelections;
   }

   public void clearIpSelection() {
      user.getFilters().setAddress(null);
   }

   public String getSelectedPort() {
      return user.getFilters().getPort();
   }

   public void setSelectedPort(String selectedPort) {
      user.getFilters().setPort(selectedPort);
   }

   public void clearPortSelection() {
      user.getFilters().setPort(null);
   }

   public TimeZone getTimeZone() {
      return java.util.TimeZone.getDefault();
   }

   public void setStartTime() {
      user.getFilters().setStartTime(DateUtils.truncate(new Date(),
              Calendar.DAY_OF_MONTH));
   }
   public void clearStartTime() {
      user.getFilters().setStartTime(null);
   }

   public boolean isDefaultStart() {
      return user.getFilters().getStartTime() == null;
   }

   public void setEndTime() {
      user.getFilters().setEndTime(DateUtils.truncate(new Date(),
              Calendar.DAY_OF_MONTH));
   }
   public void clearEndTime() {
      user.getFilters().setEndTime(null);
   }

   public boolean isDefaultEnd() {
      return user.getFilters().getEndTime() == null;
   }

   public SelectItem[] getEventIdSelections() {
      return eventIdSelections;
   }

   public SelectItem[] getEventTypeSelections() {
      return eventTypeSelections;
   }

   public void clearEventTypeSelection() {
      user.getFilters().setEventType(null);
   }

   public void clearEventIdSelection() {
      user.getFilters().setEventId(null);
   }

   public void resetRecordLimit() {
      user.getFilters().setRecordLimit(10);
   }

   //---------------- Action buttons

   public void saveFilters() {
      try {
         user.getFilters().saveFilters(admin);
      } catch (Exception e) {
         String em = "Error saving filters: " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
      }
   }

   public void resetFilters() {
      user.getFilters().defaultFilters();
   }

   /*
    * This method queries using the filters on Browser.xhtml, setting up the
    * messages table. It is also used to clear messages by Clear.xhtml. Look for
    * the boolean clear flag to find the differences.
    */
   public void queryUsingFilters() {
      ResultSet result = null;
      String p;
      //--------------------------------------------- Validations
      Valid v = new Valid();
      v.startValidations();
      Date s = user.getFilters().getStartTime();
      Date e = user.getFilters().getEndTime();
      if (s != null && e != null && s.after(e))
         v.error("Start time after end time");
      if (!clear && user.getFilters().getRecordLimit() <= 0)
         v.error("Max rows must be positive integer");
      if (v.isErrors()) return;

      //----------------------------- build the query using filters
      StringBuilder query;
      if (clear) query = new StringBuilder("DELETE from syslog WHERE ");
      else query = new StringBuilder("SELECT * from syslog WHERE ");

      //............................... IP Address(es)
      p = user.getFilters().getAddress();
      if (!p.equals("Any")) {
         query.append("sender_ip = '").append(p).append("' ");
      } else {
         query.append("sender_ip IN (");
         for (int i = 0; i < ips.length; i++) {
            query.append("'").append(ips[i].getAddress()).append("'");
            if ((i + 1) < ips.length) query.append(", ");
         }
         query.append(") ");
      }

      //................................ port
      String port = user.getFilters().getPort();
      if (!port.equals("Any")) {
         query.append("AND collector_port = ").append(port).append(" ");
      }

      //.......................... start time
      Date stime = user.getFilters().getStartTime();
      if (stime != null) {
         query.append("AND arrival_time >= '")
              .append(df.format(stime))
              .append("' ");
      }

      //.......................... end time
      Date etime = user.getFilters().getEndTime();
      if (etime != null) {
         query.append("AND arrival_time <= '")
              .append(df.format(etime))
              .append("' ");
      }

      //............................ event type
      String et = user.getFilters().getEventType();
      if (!et.equals("Any")) {
         query.append("AND event_type = '")
              .append(et)
              .append("' ");
      }

      //............................ event id
      String eid = user.getFilters().getEventId();
      if (!eid.equals("Any")) {
         query.append("AND event_id = '")
              .append(eid)
              .append("' ");
      }

      if (clear) {
         query.append(";");
      }
      else {

         query.append("ORDER BY arrival_time DESC ");

         Integer rl = user.getFilters().getRecordLimit();
         if (rl == null) rl = 10;
         query.append("LIMIT ")
            .append(rl)
            .append(";");
      }

      try {
         Query q = new Query(query);
         if (clear) { 
            int numDeletes = q.dbUpdate(msg);
            String m = numDeletes + " messages deleted.";
            syslog.info(m);
            v.error(m);
         }
         else result = new Query(query).dbQuery(msg);
      } catch (Exception ex) {
         String em = "Database error: " + ex.getMessage();
         syslog.warn(em);
         v.error(em);
         return;
      }

      if (clear) return;

      /*
       * Now we generate a second query to retrieve the schematron records
       * corresponding to the syslog records we have already selected.
       */
      String sub = StringUtils.chomp(query.toString(), ";");
      sub = StringUtils.replaceOnce(sub, "*", "id");
      String squery = "SELECT * FROM schematron WHERE syslog_id IN (" + sub + ") ORDER BY syslog_id;";
      ResultSet sresult;
      try {
         sresult = new Query(squery).dbQuery(msg);
      } catch (Exception ex) {
         String em = "Database error: " + ex.getMessage();
         syslog.warn(em);
         v.error(em);
         return;
      }

      initializeList(result, sresult);


   } // EO QueryUsingFilters method

   private boolean clear = false;
   /*
    * Action listener for Clear messages button on Clear.xhtml. Sets clear flag
    * and uses queryUsingFilters.
    */
   public void clearMessages() {
      clear = true;
      queryUsingFilters();
      clear = false;
   }

   

   //***********************************************************************
   //***********************************************************************
   //*************** Message list processing
   //***********************************************************************
   //***********************************************************************

   private void initializeNoList() {
         sortColumnName = oldSortColumnName = "arrivalTime";
         ascending = oldAscending = false;
         detail = null;
   }

   private void initializeList(ResultSet result, ResultSet schema) {
      try {
         allEntities = Syslog.load(result, schema);
         sortColumnName = oldSortColumnName = "arrivalTime";
         ascending = oldAscending = false;
         detail = null;
         expandFilterPanel = false;
         expandListPanel = true;
         expandDetailPanel = false;
      } catch (Exception e) {
         String em = "Error loading message list: " + e.getMessage();
         syslog.warn(em);
         FacesUtil.addErrorMessage(em);
      }
   }

   public boolean isExpandListPanel() {
      return expandListPanel;
   }

   public void setExpandListPanel(boolean expandListPanel) {
      this.expandListPanel = expandListPanel;
   }

   //----------------------------------------------------- Column sort
   private String sortColumnName, oldSortColumnName;
   private boolean ascending, oldAscending;
   public String getSortColumnName() {return sortColumnName;}
   public void setSortColumnName(String sortColumnName) {this.sortColumnName = sortColumnName;}
   public boolean isAscending() {return ascending;}
   public void setAscending(boolean ascending) {this.ascending = ascending;}

   private Syslog[] allEntities;
   public Syslog[] getAll() {
      if (!sortColumnName.equals(oldSortColumnName) ||
              ascending != oldAscending) {
         oldSortColumnName = sortColumnName;
         oldAscending = ascending;
         Comparator<Syslog> comp = new Syslog.Comp(sortColumnName, ascending);
         Arrays.sort(allEntities, comp);
      }
      return allEntities;
   }
   public void setAll(Syslog[] all) {this.allEntities = all;}

      public void Selected() {
      for (Syslog s : allEntities) {
         if (s.isSelected()) {
            detail = s;
            displayXml = !detail.getXmlMessage().isEmpty();
            expandListPanel = false;
            expandDetailPanel = true;
            return;
         }
      }
   }

   public void clearSelections() {
      for (Syslog s : allEntities) {
         s.setSelected(false);
      }
         expandListPanel = true;
         expandDetailPanel = false;
   }

   //***********************************************************************
   //***********************************************************************
   //*************** Detail panel processing
   //***********************************************************************
   //***********************************************************************

   private boolean expandDetailPanel = false;
   private boolean displayXml = true;
   public boolean isDisplayXml() { return displayXml; }
   public String buttonValue() {
      if (displayXml) return "Show entire message";
      return "Show XML packet only";
   }
   public void flipMessage() { displayXml = !displayXml; }

   /** Syslog message to show detail on in detail panel */
   private Syslog detail = null;

   public String getMSG() {
      return Util.prettyPrintXML(detail.getXmlMessage());
   }

   /*
    * Deletes the detail message user is looking at.
    */
   public void clearMessage() throws Exception {
      new Query(DBUtil.SYSLOG_DELETE)
          .set("id", detail.getId()).dbUpdate(msg);
      List<Syslog> list = new ArrayList<Syslog>(Arrays.asList(allEntities));
      for (Syslog s : list) {
         if (s.getId() == detail.getId()) {
            list.remove(s);
            break;
         }
      }
      allEntities = list.toArray(new Syslog[0]);
      detail = null;
      expandDetailPanel = false;
      if (allEntities.length == 0) {
         expandListPanel = false;
         expandFilterPanel = true;
      } else {
         expandListPanel = true;
         expandFilterPanel = false;
      }
   }


   public boolean isExpandDetailPanel() {
      return expandDetailPanel;
   }

   public void setExpandDetailPanel(boolean expandDetailPanel) {
      this.expandDetailPanel = expandDetailPanel;
   }

   public Syslog getDetail() {
      return detail;
   }

   public void setDetail(Syslog detail) {
      this.detail = detail;
   }

   public String getParseResult(int result, String em) {
      switch (result) {
         case 0:
            return "Not Attempted";
         case 1:
            String ret = "Successful";
            if (StringUtils.containsIgnoreCase(em, "Informational:"))
               ret += ", with Informational notes";
            return ret;
         case 2:
            return "Failed";
         case 3:
        	 return "Not Applicable";
         default:
            return "Invalid code";
      }
   }

   public boolean prtEm(int result, String em) {
      if (result > 1) return true;
      if (StringUtils.containsIgnoreCase(em, "Informational:")) return true;
      return false;
   }

   public String getResultStyle(int result, String em) {
      switch (result) {
         case 0:
            return "erlPnlGrd";
         case 1:
            String ret = "erlPnlGrd";
            if (StringUtils.containsIgnoreCase(em, "Informational:"))
               ret = "erlPnlGrdInfo";
            return ret;
         case 2:
             return "erlPnlGrdErr";
         case 3:
        	 return "erlPnlGrdInfo";
         default:
             return "erlPnlGrdErr";
      }
   }


} // EO BrowserBean class
