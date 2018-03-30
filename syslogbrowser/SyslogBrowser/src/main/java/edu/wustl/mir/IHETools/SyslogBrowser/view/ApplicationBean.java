/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.IHETools.SyslogBrowser.view;

import java.io.Serializable;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;

import edu.wustl.mir.erl.IHETools.Util.DBUtil;
import edu.wustl.mir.erl.IHETools.Util.DataBaseConnection;
import edu.wustl.mir.erl.IHETools.Util.FacesUtil;
import edu.wustl.mir.erl.IHETools.Util.Query;
import edu.wustl.mir.erl.IHETools.Util.Util;

/**
 *
 * @author rmoult01
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class ApplicationBean implements Serializable {
	static final long serialVersionUID = 1L;

	private static Logger log;
	private static String DBADMIN = DBUtil.DBADMIN;
	private DataBaseConnection dbc = null;
	
	private static final String APPLICATION_NAME = "SyslogBrowser";
	
	public enum Profiles { DEV, NA, EU }
	public static Profiles profile = null;
	
	public boolean isWiki() {
		return !Util.getWiki().isEmpty();
	}
	public String getWikiURL() {
		return Util.getWiki();
	}


	private static String casLogoutURL = "";

	public String casLogoff() {
		FacesUtil.getHttpSession(false).invalidate();
		return casLogoutURL;
	}

	/** Creates a new instance of ApplicationBean */
	public ApplicationBean() throws Exception {
		try {
			Util.initializeApplication(APPLICATION_NAME);
			log = Util.getLog();
			log.trace("Initializing ApplicationBean");

			try {
				profile = Profiles.valueOf(Util.getProfileString().toUpperCase());
			} catch (IllegalArgumentException | NullPointerException ex) {
				throw new Exception("Unsupported profile " + 
					Util.getProfileString());
			}

			casLogoutURL = Util.getParameterString("CASLogoutURL", "");
			
			initializeDB();
			loadCompanies();
			loadServerPorts();
			
			log.trace("ApplicationBean Initialized");
			
		} catch (Exception e) {
			System.err.println("Could not instatiate ApplicationBean: "
					+ e.getMessage());
			throw e;
		}
	}

	private void initializeDB() throws Exception {
		dbc = DataBaseConnection.getDataBaseConnection(DBADMIN);

		if (!DBUtil.userExists(dbc))
			DBUtil.createUser(dbc, true);

		if (!DBUtil.databaseExists(dbc)) {
			DBUtil.createDatabase(dbc);
			new Query(DBUtil.CREATE_SYSLOG_ADMIN_TABLES).dbUpdates(dbc);
			//--------------- create master admin user
			new Query(DBUtil.COMPANY_INSERT)
			.set("name", "Monitors")
			.set("contact", "Moulton, Ralph")
			.set("contactEmail", "moultonr@mir.wustl.edu")
			.set("contactPhone", "314-747-1733")
			.dbUpdate(dbc, false);
			new Query(DBUtil.USER_INSERT)
			.set("companyId", DBUtil.getCompanyLid(dbc))
			.set("userId", "moulton")
			.set("password", Util.hashPw("gloria"))
			.set("userName", "Moulton, Ralph")
			.set("userEmail", "moultonr@mir.wustl.edu")
			.set("userPhone", "314-747-1733")
			.set("admin", "true")
			.dbUpdate(dbc);
		}
	}

	public String getTimeZone() {
		TimeZone tz = TimeZone.getDefault();
		return tz.getID();
	}

	public boolean isCasLogon() {
		return profile != Profiles.DEV;
	}
	public boolean isInternalLogon() {
		return profile == Profiles.DEV;
	}

	/*
	 * Handling for companies select list. This is loaded at application startup.
	 * When any change is made to the company table, the list is set stale. When
	 * the list is requested, it is reloaded if stale.
	 */
	private SelectItem[] companies;
	private AtomicBoolean companiesFresh = new AtomicBoolean();

	public void setCompaniesStale() {
		companiesFresh.compareAndSet(true, false);
	}

	public SelectItem[] getCompanies() {
		if (companiesFresh.get() == false) 
			try {
				loadCompanies();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		return companies;
	}

	private synchronized void loadCompanies() throws Exception {
		List<SelectItem> items = new ArrayList<>();
		try {
			if (companiesFresh.get() == true) return;
			if (dbc == null) dbc = DataBaseConnection.getDataBaseConnection(DBADMIN);
			ResultSet rslt = new Query(DBUtil.SCOMPANY_ALL).dbQuery(dbc);
			while (rslt.next()) {
				items.add(new SelectItem(rslt.getInt("id"), rslt.getString("name")));
			}
		} catch (Exception e) {
			throw new Exception("loadCompanies() error " + e.getMessage());
		}
		companies = items.toArray(new SelectItem[0]);
		companiesFresh.compareAndSet(false, true);
	}

	/*
	 * Handling for users select list. This is loaded at application startup.
	 * When any change is made to the company table, the list is set stale. When
	 * the list is requested, it is reloaded if stale.
	 */
	private SelectItem[] users;
	private AtomicBoolean usersFresh = new AtomicBoolean();

	public void setUsersStale() {
		usersFresh.compareAndSet(true, false);
	}

	public SelectItem[] getUsers() {
		if (usersFresh.get() == false)
			try {
				loadUsers();
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		return users;
	}

	private synchronized void loadUsers() throws Exception {
		List<SelectItem> items = new ArrayList<>();
		if (usersFresh.get() == true) return;
		try {
			if (dbc == null) dbc = DataBaseConnection.getDataBaseConnection(DBADMIN);
			ResultSet rslt = new Query(DBUtil.SUSER_ALL).dbQuery(dbc);
			while (rslt.next()) {
				items.add(new SelectItem(rslt.getInt("id"), rslt
						.getString("user_id")
						+ ": "
						+ rslt.getString("user_name")));
			}
			users = items.toArray(new SelectItem[0]);
			usersFresh.compareAndSet(false, true);
		} catch (Exception e) {
			throw new Exception("loadUsers() error " + e.getMessage());
		}
	}

	/**
	 * Handling for the list of ports presented by the SyslogCollector simulator.
	 * This is taken from the SyslogBrowser.ini file in which the [Servers]
	 * section should duplicate the one in the SyslogCollector.ini.  If any
	 * changes are made in the latter, the former should be changed to match, and
	 * the SyslogBrowser application restarted.
	 */

	private SelectItem[] ports;

	/**
	 * Gets list of ports supported by SyslogCollector simulator, suitable for
	 * a select one component.
	 * @return Array of SelectItem
	 */
	public SelectItem[] getPorts() {
		return ports;
	}

	private String[] protocols = { "TLS", "TCP", "UDP" };

	private void loadServerPorts() throws Exception {
		List<SelectItem> items = new ArrayList<SelectItem>();
		items.add(new SelectItem("Any", "Any"));
		for (String protocol : protocols) {
			String[] prts = Util.getParameterStringArray("Servers." + protocol + "Ports");
			for (String prt: prts) {
				items.add(new SelectItem(prt, prt + "-" + protocol));
			}
		}
		ports = items.toArray(new SelectItem[0]);
	}
	
	public String getDisplayName() {
		return Util.getDisplayName();
	}
	public String getApplicationName() {
		return APPLICATION_NAME;
	}
	
} // EO ApplicationBean class
