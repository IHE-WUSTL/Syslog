package gov.nist.syslog;

import gov.nist.syslog.util.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This class loads all the validation schemas and XSLT files at startup, 
 * and provides them for validation to the MessageProcessingThread. <b>Note:</b> 
 * {@link java.xml.validation.Schema Schema} objects are thread safe.
 * @author rmoult01
 */
public class ValidationSchemaFactory {
   
   private static Map<String, NamedValidator[]> validators;
   private static Processor proc;
   private static DocumentBuilder builder;
   
   /**
    * The validation schema files for the various IHE transactions are located
    * in the IHEValidation directory. Each IHE transaction is represented by a
    * sub directory whose name begins with the EventTypeCode code, for example,
    * "ITI-42 Register Document Set-b". Each audit event type within that
    * EventType is represented by a sub directory within the EventType
    * directory, whose name is the EventId code value, for example, "110106".
    * The validation file(s) for this EventType code & EventID code combination
    * are placed in this later sub directory. Because of the differences between
    * the schemas specified in RFC-3881 and DICOM PS 3.15, schematrons for these
    * schemas contain a trailing R or D in their name indicating which schema
    * they are for. mt01R.sch indicates a schematron for an RFC-3881 format
    * message, mt01D.sch for a DICOM format message. In many cases the
    * differences are minor, and the build process can generate the schematron
    * for one format from the schematron for the other format. These generated
    * schematrons are designated with a trailing G in their name. For example,
    * if mt01R.sch exists, but mt01D.sch does not, the build process with create
    * an mt01DG.sch. Generated .sch files are deleted during a clean. If more
    * than one validation file is present for a given format, the files will be
    * applied in alpha order by the file name. Schematron files have the file
    * name extension .sch. Relax NG files have extension .rnc.
    */
   static {
      validators = new HashMap<String, NamedValidator[]>();
      Logger log = Util.getSyslog();
      XsltCompiler comp = null;
      List<NamedValidator> namedValidatorsR, namedValidatorsD;
      try {
         proc = new Processor(false);
         builder = proc.newDocumentBuilder();
         comp = proc.newXsltCompiler();
         //----------- Get, validate Schematron directory
         String pfn = Util.getParameterString("IHEValidationDirectory", 
               "IHEValidation");
         if (!pfn.startsWith(File.separator))
            pfn = Util.getRunDirectoryPath() + File.separator + pfn;
         File validationDirectory = new File(pfn);
         if (!validationDirectory.exists())
            throw new Exception("Directory " + pfn + " not found");
         if (!validationDirectory.isDirectory())
            throw new Exception("Directory " + pfn + " not a directory");
         if (!validationDirectory.canRead())
            throw new Exception("Directory " + pfn + " not readable");
         log.debug("Processing validation directory: " + pfn);
         
         //------------ Get and process Event Type sub directories
         String [] eventTypeDirectoryPfns = validationDirectory.list();
         if (eventTypeDirectoryPfns == null || eventTypeDirectoryPfns.length == 0)
            throw new Exception ("No EventType sub directories found in " + pfn);
         Arrays.sort(eventTypeDirectoryPfns, Collator.getInstance());
         for (String eventTypeDirectoryPfn : eventTypeDirectoryPfns) {
            if (eventTypeDirectoryPfn.startsWith(".")) continue;
            String tfn = pfn + File.separator + eventTypeDirectoryPfn;
            File eventTypeDirectory = new File(tfn);
            if (!eventTypeDirectory.exists() || !eventTypeDirectory.isDirectory())
               continue;
            if (!eventTypeDirectory.canRead()) {
               log.warn("Can't read event type directory " + eventTypeDirectoryPfn);
               continue;
            }
            log.debug("Processing event type " + eventTypeDirectoryPfn);
            
            //---------- Get and process Event ID sub directories
            String[] eventIdDirectoryPfns = eventTypeDirectory.list();
            if (eventIdDirectoryPfns == null || eventIdDirectoryPfns.length == 0) {
               log.warn("No event id directories found in " + eventTypeDirectory);
               continue;
            }
            Arrays.sort(eventIdDirectoryPfns, Collator.getInstance());
            for (String eventIdDirectoryPfn : eventIdDirectoryPfns) {
               if (eventIdDirectoryPfn.startsWith(".")) continue;
               String ifn = tfn + File.separator + eventIdDirectoryPfn;
               File eventIdDirectory = new File(ifn);
               if (!eventIdDirectory.exists() || !eventIdDirectory.isDirectory())
                  continue;
               if (!eventIdDirectory.canRead()) {
                  log.warn("Can't read event Id directory " + eventIdDirectoryPfn);
                  continue;
               }
               log.debug("   Processing event Id   " + eventIdDirectoryPfn);
               
               //--------- Get and process validation files for Event Type / Id
               File[] validationFiles = eventIdDirectory.listFiles(
                  new FilenameFilter() {
                     public boolean accept(File dir, String name) {
                        return name.endsWith(".xsl");
                     }
                  });
               
               if (validationFiles == null || validationFiles.length == 0) {
                  log.warn("No schematron files for " + 
                        eventTypeDirectoryPfn + "/" + eventIdDirectoryPfn);
                  continue;
               }
               Arrays.sort(validationFiles, new Comparator<File>() {
                  public int compare(File o1, File o2) {
                     return o1.getName().compareToIgnoreCase(o2.getName());
                  }
               });
               namedValidatorsR = new ArrayList<NamedValidator>();
               namedValidatorsD = new ArrayList<NamedValidator>();
               String str;
               for (File validationFile : validationFiles) {
                  String validationFilePfn = validationFile.getName();
                  //------------------ general file validity checks
                  if (!validationFile.exists() || !validationFile.isFile()) continue;
                  if (!validationFile.canRead()) {
                     log.warn("Can't read " + validationFilePfn);
                     continue;
                  }
                  str = eventTypeDirectoryPfn + File.separator +
                     eventIdDirectoryPfn + File.separator +
                     validationFilePfn;
                  
                  log.debug("      Processing validation " + validationFilePfn);
                  
                                  
                  String nam = getMessageName(ifn, validationFilePfn);
                  try {
                     NamedValidator nv = new NamedValidator(comp.compile(new StreamSource(validationFile)), nam);
                     switch(getSchemaType(validationFilePfn)) {
                        case "D":
                           namedValidatorsD.add(nv);
                           break;
                        case "R":
                           namedValidatorsR.add(nv);
                           break;
                        default:
                           log.warn(validationFilePfn + " invalid/missing schema type code");
                     }
                  } catch (SaxonApiException e1) {
                     log.warn("Could not load " + str + ": " + e1.getMessage());
                  }
                  
               } // EO process schematron files

               String eventType = StringUtils.substringBefore(eventTypeDirectoryPfn, " ");
               eventType = StringUtils.replaceOnce(eventType, "-0", "-");
               String key = StringUtils.trimToEmpty(eventType) + 
                  "|" + StringUtils.trimToEmpty(eventIdDirectoryPfn);
               if (!namedValidatorsD.isEmpty()) {
                  ValidationSchemaFactory.validators.put(key + "|D", 
                     namedValidatorsD.toArray(new NamedValidator[0]));
               }
               if (!namedValidatorsR.isEmpty()) {
                  ValidationSchemaFactory.validators.put(key + "|R", 
                     namedValidatorsR.toArray(new NamedValidator[0]));
               }
               
            } // EO process Event Id sub directories
               
         } // EO process Event Type sub directories
            
      } catch (Exception e) {
         Util.getSyslog().warn("Error loading validation schemas: " + e.getMessage());
      }
   } // EO static block
   
   /**
    * Returns NamedValidator[] of validators for the passed ATNA EventTypeCode,
    * Event ID, and message schema type code ((D)ICOM or (R)FC-3881). See
    * transactions in ITI Vol2a and 2b for details. Validators can be
    * {@link net.sf.saxon.s9api.XsltExecutable XsltExecutable} objects, or
    * {@link javas.xml.validation.Schema Schema} objects.
    * <b>NOTE:</b> The namedValidators returned are copies of those stored in
    * the SchemaValidationFactory at class loading time. They should be used and
    * discarded, and are thread-safe.
    * @param eventTypeCode /AuditMessage/EventIdentificationEventTypeCode@code
    * @param eventId /AuditMessage/EventIdentificationEventId@code
    * @param schemaTypeCode indicates what schema the message is validated for,
    * (D)ICOM or (R)FC-3881.
    * @return NamedValidator[] of validators, or null if no validations exist
    *         for this combination of EventTypeCode and EventId.
    */
   public static NamedValidator[] getValidators(String eventTypeCode, 
      String eventId, String schemaTypeCode) {
      String key = StringUtils.trimToEmpty(eventTypeCode) + "|" +
         StringUtils.trimToEmpty(eventId) + "|" +
         StringUtils.trimToEmpty(schemaTypeCode);
      NamedValidator[] nvs = validators.get(key);
      if (nvs == null) return null;
      NamedValidator[] copy = new NamedValidator[nvs.length];
      for (int i = 0; i < nvs.length; i ++)
         copy[i] = new NamedValidator(nvs[i]);
      return copy;
   }
    
   public static Processor getSaxonProcessor() {
      return proc;
   }
   public static DocumentBuilder getSaxonDocumentBuilder() {
      return builder;
   }
   
   /**
    * Returns message name for a given validation file. The message name is 
    * found in a file in dir named fileName.dat, or failing that Name.dat. If
    * neither of these files is found, the name is "Unknown message name".
    * @param dir String the directory the validation file is in
    * @param pfn String the name of the validation file. fileName is derived 
    * from pfn by first removing any extensions, then removing a trailing "G"
    * (for Generated), then removing a trailing "D" or "R" (for DICOM or RFC).
    * @return string, the message name.
    */
   private static String getMessageName(String dir, String pfn) {
      String fileName = getFileName(pfn);
      String returnValue = "Unknown message name";
      String s = getFileContents(dir, "Name.dat");
      if (s != null) returnValue = s;
      s = getFileContents(dir, fileName + ".dat");
      if (s != null) returnValue = s;
      return returnValue;
   }
   private static String getFileName(String pfn) {
      String s = StringUtils.substringBefore(pfn,".");
      if (s.endsWith("G")) s = s.substring(0, s.length() -1);
      if (s.endsWith("D")) s = s.substring(0, s.length() -1);
      else if (s.endsWith("R")) s = s.substring(0, s.length() -1);
      return s;
   }
   private static String getSchemaType(String pfn) {
      String s = StringUtils.substringBefore(pfn,".");
      if (s.endsWith("G")) s = s.substring(0, s.length() -1);
      if (s.endsWith("D")) return "D";
      if (s.endsWith("R")) return "R";
      return "";
   }
   /**
    * Returns the contents of the file fileName in directory dir. Returns null
    * if the file does not exists, cannot be read, is zero length, or consists
    * entirely of whitespace.
    * @param dir String directory
    * @param fileName String file name.
    * @return file contents or null.
    */
   private static String getFileContents(String dir, String fileName) {
      String returnValue = null;
      File file = new File(dir + File.separator + fileName);
      if (file.exists() && file.isFile() && file.canRead() && file.length() > 0) {
         byte[] buff = new byte[(int) file.length()];
         FileInputStream f = null;
         try {
            f = new FileInputStream(file);
            f.read(buff);
            returnValue = new String(buff);
         } catch (Exception e) {
            Util.getSyslog().warn("Error reading " + fileName + " " + e.getMessage());
         } finally {
            if (f != null) try {
               f.close();
            } catch (IOException i) {
            }
         }
      }
      return StringUtils.trimToNull(returnValue);
   }
   
}  // ValidationSchemaFactor class
