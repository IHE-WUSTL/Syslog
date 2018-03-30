/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.erl.IHETools.SyslogBrowser.db;

import java.io.Serializable;
import java.sql.ResultSet;

/**
 * Implementation for the Schematron table in syslog DB. This is a simple
 * implementation because Schematron records are created, read, and deleted
 * along with the corresponding syslog record.
 * @author rmoult01
 */
public class Schematron implements Serializable {
   static final long serialVersionUID = 1L;

   private String auditMessageName;
   private int    schematronValidate;
   private String schematronValidateErrorMessage;
   private int    schematronValidateLine;
   private int    schematronValidateColumn;

   public static String newline = System.getProperty("line.separator");

    /**
    * Constructor builds Syslog object from the current row of the passed
    * ResultSet.
    * @param result ResultSet. Must be positioned at row for which user
    * object is desired.
    * @throws Exception on error.
    */
   public Schematron(ResultSet result) throws Exception {
      auditMessageName = result.getString("audit_message_name");
      schematronValidate = result.getInt("schematron_validate");
      schematronValidateErrorMessage = result.getString("schematron_validate_error_message");
      schematronValidateLine = result.getInt("schematron_validate_line");
      schematronValidateColumn = result.getInt("schematron_validate_column");
   }

   public String getAuditMessageName() {
      return auditMessageName;
   }

   public void setAuditMessageName(String auditMessageName) {
      this.auditMessageName = auditMessageName;
   }

   public int getSchematronValidate() {
      return schematronValidate;
   }

   public void setSchematronValidate(int schematronValidate) {
      this.schematronValidate = schematronValidate;
   }

   public int getSchematronValidateColumn() {
      return schematronValidateColumn;
   }

   public void setSchematronValidateColumn(int schematronValidateColumn) {
      this.schematronValidateColumn = schematronValidateColumn;
   }

   public String getSchematronValidateErrorMessage() {
      return newline + schematronValidateErrorMessage;
   }

   public void setSchematronValidateErrorMessage(String schematronValidateErrorMessage) {
      this.schematronValidateErrorMessage = schematronValidateErrorMessage;
   }

   public int getSchematronValidateLine() {
      return schematronValidateLine;
   }

   public void setSchematronValidateLine(int schematronValidateLine) {
      this.schematronValidateLine = schematronValidateLine;
   }



} // EO Schematron class
