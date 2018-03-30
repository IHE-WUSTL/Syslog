/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.erl.IHETools.SyslogBrowser.db;

import edu.wustl.mir.erl.IHETools.Util.Util;
import java.io.Serializable;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for syslog message table in syslog DB. Includes:
 * <ul>
 * <li/>Object representation of table row.
 * <li/>Constructor and method to build objects from JDBC
 * {@link java.sql.ResultSet ResultSet}
 * <li/>Comparator for sorting arrays of Syslog objects
 * </ul>
 * 
 * @author rmoult01
 */
public class Syslog implements Serializable {
	static final long serialVersionUID = 1L;

	/** columns from the syslog table */
	private int id;
	private String senderIp;
	private String collectorIp;
	private int collectorPort;
	private Date arrivalTime;
	private String errorMessage;
	private int rfc3164Parse;
	private String rfc3164ErrorMessage;
	private String rfc3164ErrorSubstring;
	private int rfc3164ErrorLocation;
	private int rfc5424Parse;
	private String rfc5424ErrorMessage;
	private String rfc5424ErrorSubstring;
	private int rfc5424ErrorLocation;
	private int xmlParse;
	private String xmlParseErrorMessage;
	private int xmlParseLine;
	private int xmlParseColumn;
	private int rfc3881Validate;
	private String rfc3881ValidateErrorMessage;
	private int rfc3881ValidateLine;
	private int rfc3881ValidateColumn;
	private int dicomValidate;
	private String dicomValidateErrorMessage;
	private int dicomValidateLine;
	private int dicomValidateColumn;
	private String eventType;
	private String eventId;
	private String messageName;
	private String rawMessage;
	private String xmlMessage;

	private Schematron[] schematrons = new Schematron[0];

	public int numberSchematrons() {
		if (schematrons == null)
			return 0;
		return schematrons.length;
	}

	/** boolean, has this object been selected in table? */
	private boolean selected = false;

	/**
	 * Constructor builds Syslog object from the current row of the passed
	 * ResultSet.
	 * 
	 * @param result
	 *            ResultSet. Must be positioned at row for which user object is
	 *            desired.
	 * @throws Exception
	 *             on error.
	 */
	public Syslog(ResultSet result) throws Exception {
		id = result.getInt("id");
		senderIp = result.getString("sender_ip");
		collectorIp = result.getString("collector_ip");
		collectorPort = result.getInt("collector_port");
		arrivalTime = result.getTimestamp("arrival_time");
		errorMessage = result.getString("error_message");
		rfc3164Parse = result.getInt("rfc3164_parse");
		rfc3164ErrorMessage = result.getString("rfc3164_error_message");
		rfc3164ErrorSubstring = result.getString("rfc3164_error_substring");
		rfc3164ErrorLocation = result.getInt("rfc3164_error_location");
		rfc5424Parse = result.getInt("rfc5424_parse");
		rfc5424ErrorMessage = result.getString("rfc5424_error_message");
		rfc5424ErrorSubstring = result.getString("rfc5424_error_substring");
		rfc5424ErrorLocation = result.getInt("rfc5424_error_location");
		xmlParse = result.getInt("xml_parse");
		xmlParseErrorMessage = result.getString("xml_parse_error_message");
		xmlParseLine = result.getInt("xml_parse_line");
		xmlParseColumn = result.getInt("xml_parse_column");
		rfc3881Validate = result.getInt("rfc3881_validate");
		rfc3881ValidateErrorMessage = result
				.getString("rfc3881_validate_error_message");
		rfc3881ValidateLine = result.getInt("rfc3881_validate_line");
		rfc3881ValidateColumn = result.getInt("rfc3881_validate_column");
		dicomValidate = result.getInt("dicom_validate");
		dicomValidateErrorMessage = result
				.getString("dicom_validate_error_message");
		dicomValidateLine = result.getInt("dicom_validate_line");
		dicomValidateColumn = result.getInt("dicom_validate_column");
		eventType = result.getString("event_type");
		eventId = result.getString("event_id");
		messageName = result.getString("message_name");
		rawMessage = result.getString("raw_message");
		xmlMessage = result.getString("xml_message");
	}

	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String auditMessageName) {
		this.messageName = auditMessageName;
	}

	public String getCollectorIp() {
		return collectorIp;
	}

	public void setCollectorIp(String collectorIp) {
		this.collectorIp = collectorIp;
	}

	public int getCollectorPort() {
		return collectorPort;
	}

	public void setCollectorPort(int collectorPort) {
		this.collectorPort = collectorPort;
	}

	public Date getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd HH:mm");

	public String getAT() {
		return df.format(arrivalTime);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRawMessage() {
		return rawMessage;
	}

	public void setRawMessage(String rawMessage) {
		this.rawMessage = rawMessage;
	}

	public String getXmlMessage() {
		return xmlMessage;
	}

	public void setXmlMessage(String xmlMessage) {
		this.xmlMessage = xmlMessage;
	}

	public int getRfc3164ErrorLocation() {
		return rfc3164ErrorLocation;
	}

	public void setRfc3164ErrorLocation(int rfc3164ErrorLocation) {
		this.rfc3164ErrorLocation = rfc3164ErrorLocation;
	}

	public String getRfc3164ErrorMessage() {
		return rfc3164ErrorMessage;
	}

	public void setRfc3164ErrorMessage(String rfc3164ErrorMessage) {
		this.rfc3164ErrorMessage = rfc3164ErrorMessage;
	}

	public String getRfc3164ErrorSubstring() {
		return rfc3164ErrorSubstring;
	}

	public void setRfc3164ErrorSubstring(String rfc3164ErrorSubstring) {
		this.rfc3164ErrorSubstring = rfc3164ErrorSubstring;
	}

	public int getRfc3164Parse() {
		return rfc3164Parse;
	}

	public void setRfc3164Parse(int rfc3164Parse) {
		this.rfc3164Parse = rfc3164Parse;
	}

	public int getXmlParse() {
		return xmlParse;
	}

	public void setXmlParse(int xmlParse) {
		this.xmlParse = xmlParse;
	}

	public int getXmlParseColumn() {
		return xmlParseColumn;
	}

	public void setXmlParseColumn(int xmlParseColumn) {
		this.xmlParseColumn = xmlParseColumn;
	}

	public String getXmlParseErrorMessage() {
		return xmlParseErrorMessage;
	}

	public void setXmlParseErrorMessage(String xmlParseErrorMessage) {
		this.xmlParseErrorMessage = xmlParseErrorMessage;
	}

	public int getXmlParseLine() {
		return xmlParseLine;
	}

	public void setXmlParseLine(int xmlParseLine) {
		this.xmlParseLine = xmlParseLine;
	}

	public int getRfc3881Validate() {
		return rfc3881Validate;
	}

	public void setRfc3881Validate(int rfc3881Validate) {
		this.rfc3881Validate = rfc3881Validate;
	}

	public int getRfc3881ValidateColumn() {
		return rfc3881ValidateColumn;
	}

	public void setRfc3881ValidateColumn(int rfc3881ValidateColumn) {
		this.rfc3881ValidateColumn = rfc3881ValidateColumn;
	}

	public String getRfc3881ValidateErrorMessage() {
		return rfc3881ValidateErrorMessage;
	}

	public void setRfc3881ValidateErrorMessage(
			String rfc3881ValidateErrorMessage) {
		this.rfc3881ValidateErrorMessage = rfc3881ValidateErrorMessage;
	}

	public int getRfc3881ValidateLine() {
		return rfc3881ValidateLine;
	}

	public void setRfc3881ValidateLine(int rfc3881ValidateLine) {
		this.rfc3881ValidateLine = rfc3881ValidateLine;
	}

	public int getDicomValidate() {
		return dicomValidate;
	}

	public String getDicomValidateErrorMessage() {
		return dicomValidateErrorMessage;
	}

	public int getDicomValidateLine() {
		return dicomValidateLine;
	}

	public int getDicomValidateColumn() {
		return dicomValidateColumn;
	}

	public int getRfc5424ErrorLocation() {
		return rfc5424ErrorLocation;
	}

	public void setRfc5424ErrorLocation(int rfc5424ErrorLocation) {
		this.rfc5424ErrorLocation = rfc5424ErrorLocation;
	}

	public String getRfc5424ErrorMessage() {
		return rfc5424ErrorMessage;
	}

	public void setRfc5424ErrorMessage(String rfc5424ErrorMessage) {
		this.rfc5424ErrorMessage = rfc5424ErrorMessage;
	}

	public String getRfc5424ErrorSubstring() {
		return rfc5424ErrorSubstring;
	}

	public void setRfc5424ErrorSubstring(String rfc5424ErrorSubstring) {
		this.rfc5424ErrorSubstring = rfc5424ErrorSubstring;
	}

	public int getRfc5424Parse() {
		return rfc5424Parse;
	}

	public void setRfc5424Parse(int rfc5424Parse) {
		this.rfc5424Parse = rfc5424Parse;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getSenderIp() {
		return senderIp;
	}

	public void setSenderIp(String senderIp) {
		this.senderIp = senderIp;
	}

	public Schematron[] getSchematrons() {
		return schematrons;
	}

	public void setSchematrons(Schematron[] schematrons) {
		this.schematrons = schematrons;
	}

	private boolean summarized = false;
	private String sumResult = "";
	private String sumMessage = "";
	private String resultStyle = "erlCellErr";

	private void summarizeResult() {
		if (summarized)
			return;
		summarized = true;

		if (errorMessage.length() > 0) {
			sumResult = "Inv Message";
			sumMessage = errorMessage;
			return;
		}

		if (rfc5424Parse == 2) {
			if (rfc3164Parse == 1) {
				sumResult = "Valid RFC3164 Msg";
				resultStyle = "erlCellOk";
				return;
			} else {
				sumResult = "RFC5424-3164 Parse";
				sumMessage = rfc5424ErrorMessage;
				return;
			}
		}

		if (xmlParse == 2) {
			sumResult = "RFC3881 Parse";
			sumMessage = xmlParseErrorMessage;
			return;
		}

		if (rfc3881Validate == 2) {
			sumResult = "RFC3881 Validate";
			sumMessage = rfc3881ValidateErrorMessage;
			return;
		}

		if (schematrons == null || schematrons.length == 0) {
			sumResult = "ATNA msg";
			sumMessage = "Validation not available";
			resultStyle = "erlCellOk";
			return;
		}

		if (schematrons.length == 1) {
			switch (schematrons[0].getSchematronValidate()) {
			case 0:
				sumResult = "ATNA msg";
				sumMessage = "Validation not available";
				resultStyle = "erlCellOk";
				return;
			case 1:
				sumResult = "Valid ATNA msg";
				resultStyle = "erlCellOk";
				return;
			case 2:
			default:
				sumResult = "ATNA msg Invalid";
				sumMessage = schematrons[0].getSchematronValidateErrorMessage();
				return;
			}
		}

		sumResult = "Multiple ATNA msg types";
		sumMessage = "See detail for more information";
		resultStyle = "erlCellOk";

	} // EO summarizeResult method

	/**
	 * Returns overall 'result' of message processing, indicating if the message
	 * was processed successfully, or at what point the error occurred.
	 * 
	 * @return
	 */
	public String getResult() {
		summarizeResult();
		return sumResult;
	}

	public String getResultMessage() {
		summarizeResult();
		return sumMessage;
	}

	public String getResultStyle() {
		summarizeResult();
		return resultStyle;
	}

	/**
	 * Loads the syslog in a JDBC ResultSet into Syslog objects, loads the
	 * schematron objects into the corresponding syslog objects, then returns
	 * the syslog objects as an array.
	 * 
	 * @param result
	 *            ResultSet, assumed to contain zero or more rows from the
	 *            syslog table, using their default column names. The ResultSet
	 *            is assumed to be positioned beforeFirst.
	 * @param schemas
	 *            ResultSet, assumed to contain zero or more rows from the
	 *            schematron table, using default column names. The ResultSet is
	 *            assumed to be positioned beforeFirst, and sorted by syslogId.
	 * @return Array of Syslog objects
	 * @throws Exception
	 *             on error
	 */
	public static Syslog[] load(ResultSet result, ResultSet schemas)
			throws Exception {
		// -------- load syslog objects into arraylist and map (same object in
		// both)
		List<Syslog> messages = new ArrayList<Syslog>();
		Map<Integer, Syslog> map = new HashMap<Integer, Syslog>();
		while (result.next()) {
			Syslog sys = new Syslog(result);
			messages.add(sys);
			map.put(sys.getId(), sys);
		}
		/*
		 * pass schematron rows, grouping them by syslogId and putting them in
		 * syslog object when id changes
		 */
		int syslogId = -1;
		List<Schematron> lst = null;
		while (schemas.next()) {
			int id = schemas.getInt("syslog_id");
			if (syslogId != id) {
				if (syslogId != -1) {
					if (map.containsKey(syslogId)) {
						map.get(syslogId).setSchematrons(
								lst.toArray(new Schematron[0]));
					}
				}
				syslogId = id;
				lst = new ArrayList<Schematron>();
			}
			lst.add(new Schematron(schemas));
		}
		if (syslogId != -1) {
			if (map.containsKey(syslogId)) {
				map.get(syslogId)
						.setSchematrons(lst.toArray(new Schematron[0]));
			}
		}
		return messages.toArray(new Syslog[0]);
	}

	/**
	 * Comparator class;
	 */
	public static class Comp implements Comparator<Syslog> {

		private String col;
		private boolean asc;

		public Comp(String columnName, boolean ascending) {
			col = columnName;
			asc = ascending;
		}

		@Override
		public int compare(Syslog one, Syslog two) {
			if (col == null)
				return 0;
			if (col.equalsIgnoreCase("senderIp")) {
				return asc ? Util.compareIp(one.getSenderIp(),
						two.getSenderIp()) : Util.compareIp(two.getSenderIp(),
						one.getSenderIp());
			} else if (col.equalsIgnoreCase("collectorIp")) {
				return asc ? Util.compareIp(one.getCollectorIp(),
						two.getCollectorIp()) : Util.compareIp(
						two.getCollectorIp(), one.getCollectorIp());
			} else if (col.equalsIgnoreCase("collectorPort")) {
				return asc ? two.getCollectorPort() - one.getCollectorPort()
						: one.getCollectorPort() - two.getCollectorPort();
			} else if (col.equalsIgnoreCase("arrivalTime")) {
				return asc ? one.getArrivalTime().compareTo(
						two.getArrivalTime()) : two.getArrivalTime().compareTo(
						one.getArrivalTime());
			} else if (col.equalsIgnoreCase("rfc3164Parse")) {
				return asc ? two.getRfc3164Parse() - one.getRfc3164Parse()
						: one.getRfc3164Parse() - two.getRfc3164Parse();
			} else if (col.equalsIgnoreCase("rfc5424Parse")) {
				return asc ? two.getRfc5424Parse() - one.getRfc5424Parse()
						: one.getRfc5424Parse() - two.getRfc5424Parse();
			} else if (col.equalsIgnoreCase("rfc3881Parse")) {
				return asc ? two.getXmlParse() - one.getXmlParse()
						: one.getXmlParse() - two.getXmlParse();
			} else if (col.equalsIgnoreCase("rfc3881Validate")) {
				return asc ? two.getRfc3881Validate()
						- one.getRfc3881Validate() : one.getRfc3881Validate()
						- two.getRfc3881Validate();
			} else if (col.equalsIgnoreCase("event")) {
				String a = one.getEventType() + one.getEventId();
				String b = two.getEventType() + two.getEventId();
				return asc ? a.compareToIgnoreCase(b) : b
						.compareToIgnoreCase(a);
			} else if (col.equalsIgnoreCase("eventType")) {
				return asc ? one.getEventType().compareToIgnoreCase(
						two.getEventType()) : two.getEventType()
						.compareToIgnoreCase(one.getEventType());
			} else if (col.equalsIgnoreCase("eventId")) {
				return asc ? one.getEventId().compareToIgnoreCase(
						two.getEventId()) : two.getEventId()
						.compareToIgnoreCase(one.getEventId());
			} else
				return 0;
		}

	} // EO Company Comparator inner class

} // EO Syslog class
