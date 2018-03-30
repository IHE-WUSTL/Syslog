package gov.nist.syslog;

import javax.xml.validation.Schema;
import net.sf.saxon.s9api.XsltExecutable;


/**
 * @author rmoult01
 * Package class for validator and its corresponding message name. Also used
 * during validation to store results.
 */
public class NamedValidator {
   
   private Object validator;
   private String antaMessageName;
   
   private int schematronValidate = 0;
   private String schematronValidateErrorMessage = "";
   private int schematronValidateLine = -1;
   private int schematronValidateColumn = -1;
   
   /**
    * @param validator Object, must be an instance of
    * {@link javax.xml.validation.Schema Schema} or
    * {@link net.sf.saxon.s9api.XsltExecutable XsltExecutable}
    * @param antaMessageName ATNA Audit message validated by this validator
    */
   public NamedValidator(Object validator, String antaMessageName) 
          throws IllegalArgumentException {
      if (!(validator instanceof Schema) && 
          !(validator instanceof XsltExecutable))
         throw new IllegalArgumentException("invalid validator class");
      this.validator = validator;
      this.antaMessageName = antaMessageName;
   }
   
   public NamedValidator(NamedValidator copy) {
      validator = copy.validator;
      antaMessageName = copy.antaMessageName;
   }

   public Object getValidator() {
      return validator;
   }

   public String getAtnaMessageName() {
      return antaMessageName;
   }

   public int getSchematronValidate() {
      return schematronValidate;
   }

   public void setSchematronValidate(int schematronValidate) {
      this.schematronValidate = schematronValidate;
   }

   public String getSchematronValidateErrorMessage() {
      return schematronValidateErrorMessage;
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

   public int getSchematronValidateColumn() {
      return schematronValidateColumn;
   }

   public void setSchematronValidateColumn(int schematronValidateColumn) {
      this.schematronValidateColumn = schematronValidateColumn;
   }
   

}
