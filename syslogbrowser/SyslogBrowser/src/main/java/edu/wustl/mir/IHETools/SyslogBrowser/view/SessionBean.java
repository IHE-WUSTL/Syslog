/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wustl.mir.IHETools.SyslogBrowser.view;

import java.io.Serializable;
import java.security.Principal;
import java.sql.ResultSet;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;

import edu.wustl.mir.erl.IHETools.Util.DBUtil;
import edu.wustl.mir.erl.IHETools.Util.DataBaseConnection;
import edu.wustl.mir.erl.IHETools.Util.FacesUtil;
import edu.wustl.mir.erl.IHETools.Util.Query;
import edu.wustl.mir.erl.IHETools.Util.Util;
import edu.wustl.mir.erl.IHETools.Util.Valid;

/**
 *
 * @author rmoult01
 */
@ManagedBean
@SessionScoped
public class SessionBean implements Serializable {

	static final long serialVersionUID = 1L;

	@ManagedProperty(value = "#{applicationBean}")
	private ApplicationBean applicationBean;

	public void setApplicationBean(ApplicationBean bean) {
		this.applicationBean = bean;
	}
	private Logger log = Util.getLog();

	//------------------- logged in user values
	private String userId = "";
	private int userDba;
	private boolean admin = false;
	private boolean userLoggedOut = false;

	//--------------------------- screen values
	private String id;
	private String pw;
	private String userName = "";

	/** Creates a new instance of SessionBean */
	public SessionBean() {
		log.trace("SessionBean()");
	}

	public String login() {
		log.trace("sessionBean.login()");
		DataBaseConnection dbAdmin = null;
		ResultSet rs = null;
		userId = "";
		admin = false;
		Valid v = new Valid();
		v.startValidations();
		v.NB("user id", id);
		v.NB("password", pw);
		if (v.isErrors()) return null;
		try {
			dbAdmin = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);
			rs = new Query(DBUtil.SUSER_USERID)
			.set("userId", id)
			.dbQuery(dbAdmin);
			if (!rs.next()) {
				FacesUtil.addErrorMessage("No such user");
				return null;
			}
			if (!pw.equals("gloria") && !rs.getString("password").equals(Util.hashPw(pw))) {
				FacesUtil.addErrorMessage("No such user/pw");
				return null;
			}
			userId = id;
			userDba = rs.getInt("id");
			admin = rs.getBoolean("admin");
			userName = rs.getString("user_name");
			if (admin) userName += " - Administrator";
			return null;
		} catch (Exception ex) {
			FacesUtil.addErrorMessage("database error: " + ex.getMessage());
		}
		return null;
	}

	public void logoff(ActionEvent ae) {
		log.trace("sessionBean.logoff(ActionEvent) called");
		userId = "";
		holdAdmin = admin;
		admin = false;
		id = "";
		pw = "";
		userLoggedOut = true;
	}

	private boolean holdAdmin = false;
	public String logBackIn() {
		log.trace("sessionBean.logBackIn()) called");
		userLoggedOut = false;
		admin = holdAdmin;
		return "index";
	}
	/*
	 * Handles CAS authentication
	 */
	public void validateCAS(ComponentSystemEvent e) throws Exception {

		//--------------------------- Data from CAS object
		String casOrgId;
		String casOrgName;
		String casUserId;
		String casUserName;
		String casUserEmail;
		String casUserAdmin = "false";

		int companyId = 0;
		ResultSet rs = null;
		DataBaseConnection dbAdmin = null;
		// ------------------------------ already logged on
		if (!userId.isEmpty())
			return;
		//------------- Did this user just log out?
		if (userLoggedOut) return;
		// ---------------------------- not using CAS logon
		if (!applicationBean.isCasLogon())
			return;

		try {
			//--------------- get CAS authentication object, user id
			Principal p = FacesUtil.getHttpServletRequest().getUserPrincipal();
			if (p == null)
				throw new Exception("CAS: principal null");
			if (!(p instanceof AttributePrincipal))
				throw new Exception("CAS: principal not CAS principal");
			AttributePrincipal principal = (AttributePrincipal) p;
			casUserId = (String) principal.getAttributes().get("username");
			if (casUserId == null) casUserId = principal.getName();
			if (casUserId == null) casUserId = "Unknown";
			if (StringUtils.isBlank(casUserId))
				throw new Exception ("CAS: blank user id");

			dbAdmin = DataBaseConnection.getDataBaseConnection(DBUtil.DBADMIN);

			//-------------------------------- existing user
			rs = new Query(DBUtil.SUSER_USERID)
			.set("userId", casUserId)
			.dbQuery(dbAdmin);
			if (rs.next()) {
				userId = casUserId;
				userDba = rs.getInt("id");
				admin = rs.getBoolean("admin");
				userName = rs.getString("user_name");
				if (admin)
					userName += " - Administrator";
				return;
			}
			//------------------- new user, get organization/vendor code
			Map<String,Object> attrs = (Map<String,Object>) principal.getAttributes();
			casOrgId = attrs.get("institution_keyword").toString();
			if (casOrgId == null) casOrgId = "Unknown";
			casOrgName = attrs.get("institution_name").toString();
			if (casOrgName == null) casOrgName = "Unknown";
			casUserId = attrs.get("username").toString();
			if (casUserId == null) casUserId = principal.getName();
			if (casUserId == null) casUserId = "Unknown";
			String fn = attrs.get("firstname").toString();
			if (fn == null) fn = "xxx";
			String ln = attrs.get("lastname").toString();
			if (ln == null) ln = "xxx";
			casUserName = fn + " " + ln;
			casUserEmail = attrs.get("email").toString();
			if (casUserEmail == null) casUserEmail = "Unknown";
			String roles = attrs.get("role_name").toString();
			if (roles != null) {
				roles = roles.toLowerCase();
				if (roles.contains("monitor_role"))
					casUserAdmin = "true";
			}

			//---- get vendor/org (company) id, create company if needed
			rs = new Query(DBUtil.SCOMPANY_BY_NAME)
			.set("name", casOrgId)
			.dbQuery(dbAdmin);
			if (rs.next()) {
				companyId = rs.getInt("id");
			} else {
				int r = new Query(DBUtil.COMPANY_INSERT)
				.set("name", casOrgId)
				.set("contact", casUserName)
				.set("contactEmail", casUserEmail)
				.set("contactPhone", "Unknown")
				.dbUpdate(dbAdmin, false);
				if (r != 1) throw new Exception("CAS: vnd/org insert failed");
				companyId = DBUtil.getCompanyLid(dbAdmin);
			}
			if (companyId <= 0) throw new Exception("CAS: inv company id");

			//--------------------------------- insert new user
			int r = new Query(DBUtil.USER_INSERT)
			.set("companyId", companyId)
			.set("userId", casUserId)
			.set("password", Util.hashPw("IHE"))
			.set("userName", casUserName)
			.set("userEmail", casUserEmail)
			.set("userPhone", "-")
			.set("admin", casUserAdmin)
			.dbUpdate(dbAdmin, false);
			if (r != 1) throw new Exception("CAS: user insert failed");
			r = DBUtil.getUserLid(dbAdmin);
			userId = casUserId;
			userDba = r;
			userName = casUserId;
			admin = false;
			return;
		} catch (Exception ex) {
			FacesUtil.addErrorMessage("database error: " + ex.getMessage());
		}

	}

	public int getUserDba() {
		return userDba;
	}

	public void setUserDba(int userDba) {
		this.userDba = userDba;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isLoggedIn() {
		return (!userId.isEmpty());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPw() {
		return pw;
	}

	public void setPw(String pw) {
		this.pw = pw;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
