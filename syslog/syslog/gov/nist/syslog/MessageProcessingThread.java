
package gov.nist.syslog;

import gov.nist.syslog.util.DBUtil;
import gov.nist.syslog.util.Query;
import gov.nist.syslog.util.Util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * {@link java.lang.Runnable Runnable} class which processes a single syslog
 * message. {@link gov.nist.syslog.transports.UPDServerThread
 * UDPServerThread} and 
 * {@link gov.nist.syslog.transports.TCPConnectionThread 
 * TCPConnectionThread} each generate one of these objects to process each
 * syslog message which they receive. <b>NOTE:</b> The 
 * {@link gov.nist.syslog.MessageProcessingThread#run() run} method may
 * be called directly or via an Executor
 * {@link java.util.concurrent.Executor#execute(java.lang.Runnable) execute}
 * method.
 * @author rmoult01
 */
public class MessageProcessingThread implements Runnable {
   
   private static boolean useSyslogDB;
   private static boolean storeMessages;
   private static String messageDirectory;
   private static final String BOM = "\ufeff";
   
   private static final String n = System.getProperty("line.separator");
   private static Logger log;
   private static final String nl = System.getProperty("line.separator");
   private static boolean trace = false;
   
   /**
    * These properties represent the columns of the syslog table. They are in
    * the same order as the DB table definition for comparison. Some notes:
    * The *Parse and *Validate integer values are 0=not done, 1=done, success,
    * 2=done, failed. 
    */
   
   //-------- inet information, used to identify messages for web display.
   private String senderIp;
   private static String collectorIp = "127.0.0.1";
   private int collectorPort;
   private String xmitType;
   
   //---------- For errors which preclude any processing at all
   private String errorMessage = "";
   
   /*
    * rfc3164 and rfc5424 are ABNF parsing. On error, the parser returns a 
    * substring from the xmlMessage containing the error and the location, along
    * with an error xmlMessage.
    */
   private Status rfc3164Parse = Status.NOT_DONE;
   private String rfc3164ErrorMessage = "";
   private String rfc3164ErrorSubstring = "";
   private int rfc3164ErrorLocation = -1;
   
   private Status rfc5424Parse = Status.NOT_DONE;
   private String rfc5424ErrorMessage = "";
   private String rfc5424ErrorSubstring = "";
   private int rfc5424ErrorLocation = -1;
   
   /**
    * xml parsing is a DOM Document build. On failure, the parser returns
    * an error xmlMessage, line and column.
    */
   private Status xmlParse = Status.NOT_DONE;
   private String xmlParseErrorMessage = "";
   private int xmlParseLine = -1;
   private int xmlParseColumn = -1;
   
   /**
    * rfc3881 validation is xsd schema. On failure, the validator returns
    * an error xmlMessage, line and column.
    */
   private Status rfc3881Validate = Status.NOT_DONE;
   private String rfc3881ValidateErrorMessage = "";
   private int rfc3881ValidateLine = -1;
   private int rfc3881ValidateColumn = -1;
   
   /**
    * dicom validation is xsd schema. On failure, the validator returns
    * an error xmlMessage, line and column.
    */
   private Status dicomValidate = Status.NOT_DONE;
   private String dicomValidateErrorMessage = "";
   private int dicomValidateLine = -1;
   private int dicomValidateColumn = -1;
   
   MsgType msgType = null;
   
   
   //-------------- AuditMessage/EventIdentification/EventTypeCode@code
   private String eventType = "Unknown";
   //-------------- AuditMessage/EventIdentification/EventId@code
   private String eventId = "Unknown";
   
   /**
    * Used to store xml message name before schematron validation(s) take 
    * place.  If they do, this is ignored.
    */
   private String messageName = "unknown xmlMessage";
   
   /**
    * For TCP/TLS messages which did not have valid VLI message format and UDP
    * messages, this is the raw data received on the connection. For TCP/TLS
    * messages which did have a valid VLI message format, this is the message.
    */
   private String rawMessage = "";
   //------- The MSG segment of the rfc5424 packet.
   private String xmlMessage = "";
   
   private NamedValidator[] schematrons, schematronsD, schematronsR;
   
   private static Schema rfc3881Schema;
   private static Schema dicomSchema;
   DocumentBuilderFactory parserFactory = null;
   DocumentBuilder parser = null;
   Document doc = null, out = null;

   gov.nist.syslog.rfc3164Parser.Rule$SYSLOG_3164 syslog3164Msg = null;  
   gov.nist.syslog.rfc5424Parser.Rule$SYSLOG_MSG syslog5424Msg = null;   
   
   static {
      log = Util.getSyslog(); 
      String pfn = null;
      //-------------------------------------- Load local host ip address 
      try {
         collectorIp = InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e){
         log.error("Could not load local host ip address");
         Runtime.getRuntime().exit(1);
      }
      //----------------------------- load the use DB and store messages flags
      try {
         useSyslogDB = true;
         String str = Util.getParameterString("UseSyslogDB", "YES");
         if (str.equalsIgnoreCase("NO") || str.equalsIgnoreCase("FALSE"))
            useSyslogDB = false;
         storeMessages = false;
         str = Util.getParameterString("StoreMessages", "NO");
         if (str.equalsIgnoreCase("YES") || str.equalsIgnoreCase("TRUE"))
            storeMessages = true;    
         if (storeMessages) {
            messageDirectory = 
               Util.getParameterString("MessageDirectory", "messageDirectory");
            if (!messageDirectory.substring(0,1).equalsIgnoreCase(File.separator)) 
               messageDirectory = Util.getRunDirectoryPath() + File.separator + messageDirectory;
            File f = new File(messageDirectory);
            if (!f.exists()) 
               throw new Exception("message directory " + messageDirectory + "not found");
            if (!f.isDirectory())
               throw new Exception("message directory " + messageDirectory + "not a directory");
            if (!f.canRead() || !f.canWrite())
               throw new Exception("message directory " + messageDirectory + "not read/write");
         }
      } catch (Exception e) {
         log.error("DB/messages setting error: " + e.getMessage());
         Runtime.getRuntime().exit(1);
      }
      /*
       * Load the RFC 3881 xmlMessage validation schema. Schema is thread safe,
       * so this can be done once at class load.
       */
      Source src = null;
      try {
         pfn = Util.getParameterString("RFC3881SchemaFile", "rfc3881.xsd");
      } catch (Exception e) {}
         if (!pfn.substring(0, 1).equalsIgnoreCase(File.separator)) {
            pfn = Util.getRunDirectoryPath() + File.separator + pfn;
            src = new StreamSource(new File(pfn)); 
         }
      SchemaFactory sf = 
         SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      try {
         rfc3881Schema = sf.newSchema(src);
      } catch (SAXException e) {
         log.error("Could not load RFC3881 schema" + e.getMessage());
         Runtime.getRuntime().exit(1);
      }
      
      // Load dicom message validation schema
      try {
         pfn = Util.getParameterString("DICOMSchemaFile", "dicom.xsd");
      } catch (Exception e) {}
         if (!pfn.substring(0, 1).equalsIgnoreCase(File.separator)) {
            pfn = Util.getRunDirectoryPath() + File.separator + pfn;
            src = new StreamSource(new File(pfn)); 
         }
      try {
         dicomSchema = sf.newSchema(src);
      } catch (SAXException e) {
         log.error("Could not load dicom schema" + e.getMessage());
         Runtime.getRuntime().exit(1);
      }
      
   } // EO static initialization block
   
   
   //------------------------- Constructor used for UDP and normal TCP/TLS
   public MessageProcessingThread(String msg, String ipAddress, int port,
      String xmitType) {
      this.xmitType = xmitType;
      rawMessage = StringUtils.trimToEmpty(msg);
      senderIp = ipAddress;
      collectorPort = port;
   }
   
   //------------------------- TCP/TLS which errored out reading connection
   public MessageProcessingThread(String msg, String ipAddress, int port, 
         String xmitType, String errorMsg) {
      this.xmitType = xmitType;
      rawMessage = msg;
      senderIp = ipAddress;
      collectorPort = port;
      errorMessage = errorMsg;
   }

   /**
    * Run method validates one audit xmlMessage, passed in constructor.
    */
   public void run() {
      
      try {
      
      /** 
       * If error message is already set, this message failed during reading 
       * the connection (bad VLI or connection timeout).
       */
      if (errorMessage.length() > 0) {
         storeMessage();
         return;
      }
      
      if (rawMessage == null || rawMessage.length() == 0) {
         this.errorMessage = "empty message";
         rawMessage = "";
         storeMessage();
         return;
      }
      
      //----------------------- Cludge: Remove repetitive BOMs
      while (rawMessage.contains(BOM + BOM)) {
    	  StringUtils.replace(rawMessage, BOM + BOM, BOM);
      }
      
      //-------------------------------------------- RFC 5424 parsing
      try {
         syslog5424Msg = (gov.nist.syslog.rfc5424Parser.Rule$SYSLOG_MSG) 
         gov.nist.syslog.rfc5424Parser.Parser.parse("SYSLOG-MSG", rawMessage, trace);
         rfc5424Parse = Status.SUCCESSFUL;
         messageName = "Valid RFC5424 format xmlMessage";
      } catch (IllegalArgumentException e) {
         rfc5424Parse = Status.FAILED;
         rfc5424ErrorMessage = e.getMessage();
      } catch (gov.nist.syslog.rfc5424Parser.ParserException e) {
         rfc5424Parse = Status.FAILED;
         rfc5424ErrorMessage = e.getMessage();
         rfc5424ErrorSubstring = e.getSubstring();
         this.rfc5424ErrorLocation = e.getSubstringIndex();
      }
      
      //--- If RFC 5242 parsing fails, try RFC 3164 parsing
      if (rfc5424Parse == Status.FAILED) {
         try {
            syslog3164Msg = (gov.nist.syslog.rfc3164Parser.Rule$SYSLOG_3164)
             gov.nist.syslog.rfc3164Parser.Parser.parse("SYSLOG_3164", rawMessage, trace);
            rfc3164Parse = Status.SUCCESSFUL;
            messageName = "Valid RFC3164 format xmlMessage";
         } catch (IllegalArgumentException e) {
            rfc3164Parse = Status.FAILED;
            rfc3164ErrorMessage = e.getMessage();
         } catch (gov.nist.syslog.rfc3164Parser.ParserException e) {
            rfc3164Parse = Status.FAILED;
            rfc3164ErrorMessage = e.getMessage();
            rfc3164ErrorSubstring = e.getSubstring();
            rfc3164ErrorLocation = e.getSubstringIndex();
         }
         //--- Regardless of result of 3164 parsing, store message and done
         storeMessage();
         return;
      } // EO RFC 3164 parsing
      
      /*
       * Pull MSG tag value from 5424 xmlMessage
       * Each "node" in the parsed xmlMessage is represented by an extension of the
       * Rule class, and each Rule object contains an array of Rule objects for
       * the nodes within that node. This code finds SYSLOG-MSG => MSG =>
       * [MSG-ANY | MSG-UTF8 => UTF-8-STRING]. Both MSG-ANY and UTF-8-STRING
       * contain OCTET objects, which are strung together to produce the xmlMessage
       * content. Alternatively, syslog5424Msg could be converted to an XML
       * equivalent and then visited using XPath, but this approach avoids that
       * overhead, and should be much faster and lighter on memory.
       * see http://www.parse2.com for details on ABNF parsing.
       */
      
      gov.nist.syslog.rfc5424Parser.Rule holder = null;
      pull: for (gov.nist.syslog.rfc5424Parser.Rule rmsg : syslog5424Msg.rules) {
         if (rmsg instanceof gov.nist.syslog.rfc5424Parser.Rule$MSG) {
            for (gov.nist.syslog.rfc5424Parser.Rule rms : rmsg.rules) {
               if (rms instanceof gov.nist.syslog.rfc5424Parser.Rule$MSG_ANY) {
                  holder = rms;
                  break pull;
               } else {
                  if (rms instanceof gov.nist.syslog.rfc5424Parser.Rule$MSG_UTF8) {
                     for (gov.nist.syslog.rfc5424Parser.Rule rm8 : rms.rules) {
                        if (rm8 instanceof gov.nist.syslog.rfc5424Parser.Rule$UTF_8_STRING) {
                           holder = rm8;
                           break pull;
                        }
                     }
                  }
               }
            }
         }
      }
      if (holder != null) {
         StringBuilder s = new StringBuilder();
         for(gov.nist.syslog.rfc5424Parser.Rule o : holder.rules) {
            if (o instanceof gov.nist.syslog.rfc5424Parser.Rule$OCTET)
               s.append(o.spelling);
         }
         xmlMessage = s.toString();
      }
      if (xmlMessage.length() == 0) {
         xmlParse = Status.FAILED;
         xmlParseErrorMessage = "XML MSG missing";
         storeMessage();
         return;
      }

      //--------------------------------------------- XML parse xmlMessage
      try {
         parserFactory = DocumentBuilderFactory.newInstance();
         parserFactory.setNamespaceAware(true);
         parser = parserFactory.newDocumentBuilder();
      } catch (ParserConfigurationException e) {
         log.error("Could not create DocumentBuilder: " + e.getMessage());
         Runtime.getRuntime().exit(1);
      }
      
      try {
         doc = parser.parse(new ByteArrayInputStream(xmlMessage.getBytes()));
         xmlParse = Status.SUCCESSFUL;
         messageName = "Valid RFC5424 format xmlMessage with XML MSG payload";
         log.trace("xml parse OK");
      } catch (Exception e1) {
         if (e1 instanceof SAXParseException) {
            SAXParseException spe = (SAXParseException) e1;
            xmlParseLine = spe.getLineNumber();
            xmlParseColumn = spe.getColumnNumber();
         }
         xmlParseErrorMessage = "Error parsing MSG xml: " + e1.getMessage();
         log.debug(xmlParseErrorMessage);
         xmlParse = Status.FAILED;
         storeMessage();
         return;
      } 

      /*
       * Validate message against rfc3881 or dicom schema. Only one validation 
       * is done, so if the rfc3881 validation check OK, the dicom validation is
       * skipped. 
       */
      //------------------- rfc3881 schema validation
      try { 
         DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
         df.setValidating(false); 
         df.setSchema(rfc3881Schema);
         DocumentBuilder b = df.newDocumentBuilder(); 
         SchemaErrorHandler seh = new SchemaErrorHandler();
         b.setErrorHandler(seh);
         b.parse(new ByteArrayInputStream(xmlMessage.getBytes()));
         seh.validateMessage();
         rfc3881Validate = Status.SUCCESSFUL;
         messageName = "Valid RFC3881 format xmlMessage";
         log.trace("RFC 3881 validation OK");
         msgType = MsgType.RFC3881;
         //-- since rfc3881 passed, dicom is not applicable
         dicomValidate = Status.NA;
      } catch (Exception e1) {
         if (e1 instanceof SAXParseException) {
            SAXParseException spe = (SAXParseException) e1;
            rfc3881ValidateLine = spe.getLineNumber();
            rfc3881ValidateColumn = spe.getColumnNumber();
         }
         rfc3881ValidateErrorMessage = "RFC3881 Schema Error: " + e1.getMessage();
         log.debug(rfc3881ValidateErrorMessage);
         rfc3881Validate = Status.FAILED;
      } 
      
      //----------------------------------- dicom schema validation
      if (rfc3881Validate != Status.SUCCESSFUL) {
         try { 
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            df.setValidating(false); 
            df.setSchema(dicomSchema);
            DocumentBuilder b = df.newDocumentBuilder(); 
            SchemaErrorHandler seh = new SchemaErrorHandler();
            b.setErrorHandler(seh);
            b.parse(new ByteArrayInputStream(xmlMessage.getBytes()));
            seh.validateMessage();
            
            //--------- Additional validation of AuditSourceIdentification element
            Element asiElement = (Element) doc.getElementsByTagName("AuditSourceIdentification").item(0);
            boolean asiCodeIsDigit = asiElement.getAttribute("code").matches("[1-9]");
            boolean asiHasCodeSystemName = asiElement.hasAttribute("codeSystemName");
            boolean asiHasOriginalText = asiElement.hasAttribute("originalText");
            boolean asiHasDisplayName = asiElement.hasAttribute("displayname");
            if (!asiCodeIsDigit && !asiHasCodeSystemName) 
               throw new Exception("AuditSourceIdentification: code attribute value must be 1-9 unless codeSystemName attribute is present");
            if ((asiHasCodeSystemName || asiHasOriginalText || asiHasDisplayName) &&
               (!asiHasCodeSystemName || !asiHasOriginalText))
               throw new Exception("AuditSourceIdentification: codeSystemName and originalText are required if any csd-other-attributes are present");
            
            dicomValidate = Status.SUCCESSFUL;
            messageName = "Valid DICOM format xmlMessage";
            log.debug("DICOM validation OK");
            msgType = MsgType.DICOM;
            //--- if dicom passes, clear any rfc3881 error
            rfc3881Validate = Status.NA;
            rfc3881ValidateErrorMessage = "";
            rfc3881ValidateLine = -1;
            rfc3881ValidateColumn = -1;
         } catch (Exception e1) {
            if (e1 instanceof SAXParseException) {
               SAXParseException spe = (SAXParseException) e1;
               dicomValidateLine = spe.getLineNumber();
               dicomValidateColumn = spe.getColumnNumber();
            }
            dicomValidateErrorMessage = "DICOM Schema Error: " + e1.getMessage();
            log.debug(dicomValidateErrorMessage);
            dicomValidate = Status.FAILED;
            // If we get here, both rfc3881 and dicom have failed to validate
            storeMessage();
            return;
         } 
      } // EO DICOM schema validation

      /*
       * get AuditMessage/EventIdentification/EventTypeCode@code and
       * AuditMessage/EventIdentification/EventId@code values from the xml doc.
       * These should all work, because doc has passed schema validation. 
       */
      try {
         Element eventIdElement = (Element) doc.getElementsByTagName("EventID").item(0);
         eventId = eventIdElement.getAttribute(msgType.getCodeAttributeName());
      } catch (Exception e1) {}
      try {
         Element eventTypeElement = (Element) doc.getElementsByTagName("EventTypeCode").item(0);
         eventType = eventTypeElement.getAttribute(msgType.getCodeAttributeName());
      } catch (Exception e1) {}
      
      log.debug(eventType + "-" + eventId);
      
      //------------------------------- Retrieve validation objects
      schematronsR = 
         ValidationSchemaFactory.getValidators(eventType, eventId, "R");
      schematronsD = 
               ValidationSchemaFactory.getValidators(eventType, eventId, "D");
      // No validation available at all for this message 
      if (schematronsR == null && schematronsD == null) {
         messageName = "ATNA msg for " + eventType + ", " + eventId + 
            " schematron validation not available";
         String em = "No validators for " + eventType + "|" + eventId;
         log.debug(em);
         storeMessage();
         return;
      }
      //---------- load validations for appropriate schema type
      switch (msgType) {
         case RFC3881:
            schematrons = schematronsR;
            break;
         case DICOM:
            schematrons = schematronsD;
            break;
         default:
            log.warn("Invalid MsgType " + 
               ((msgType == null) ? "null" : msgType.toString()));
            schematrons = schematronsR;
      }
      if (schematrons == null) {
         messageName = "ATNA msg for " + eventType + ", " + eventId + 
                  " schematron validation not availablefor " + 
                  msgType.toString() + " schema messages";
         String em = "No validators for " + eventType + "|" + eventId +
                  "|" + msgType.toString();
         log.debug(em);
         storeMessage();
         return;
      }
      
      //----------------------------------- Process validations
      for (NamedValidator val : schematrons) {
         //-------------------------------- Schema validation
         if(val.getValidator() instanceof Schema) {
            val.setSchematronValidate(1);
            try {
               Schema s = (Schema) val.getValidator();
               Validator schemaValidator = s.newValidator();
               schemaValidator.setErrorHandler(new schematronErrorHandler());
               schemaValidator.validate(new DOMSource(doc));
            } catch (Exception e1) {
               if (e1 instanceof SAXParseException) {
                  SAXParseException spe = (SAXParseException) e1;
                  val.setSchematronValidateLine(spe.getLineNumber());
                  val.setSchematronValidateColumn(spe.getColumnNumber());
               }
               val.setSchematronValidateErrorMessage("Schematron Validation error: " + e1.getMessage());
               log.debug(val.getSchematronValidateErrorMessage());
               val.setSchematronValidate(2);
            }       
            continue;
         } // EO process Schema validation
         
         //------------------------------------------- XSLT validations
         if (val.getValidator() instanceof XsltExecutable) {
            val.setSchematronValidate(1);
            XsltExecutable exp = (XsltExecutable) val.getValidator();
            XsltTransformer trnx = exp.load();
            try {
               trnx.setSource(new DOMSource(doc));
            } catch (SaxonApiException e) {
               val.setSchematronValidate(2);
               val.setSchematronValidateErrorMessage(e.getMessage());
               log.debug(val.getSchematronValidateErrorMessage());
               continue;
            }
            out = parser.newDocument();
            trnx.setDestination(new DOMDestination(out));
            trnx.setMessageListener(new XSLTMessageListener(val));
            try {
               trnx.transform();
               // pull errors (if any) from svr
               NodeList errs = 
                  out.getElementsByTagNameNS("http://purl.oclc.org/dsdl/svrl", 
                        "failed-assert");
               if (errs.getLength() > 0) {
                  for (int nn=0; nn < errs.getLength(); nn++) {
                     Node node = errs.item(nn);
                     String c = StringUtils.trimToNull(node.getTextContent());
                     if (c != null) {
                        if (!StringUtils.containsIgnoreCase(c, "Informational:")) 
                           val.setSchematronValidate(2);
                        String em = val.getSchematronValidateErrorMessage();
                        if (em.length() > 0) em += nl;
                        em += c;
                        val.setSchematronValidateErrorMessage(em);
                     }
                  }
                  log.debug(val.getSchematronValidateErrorMessage());
               }
            } catch (SaxonApiException e) {
               val.setSchematronValidate(2);
               val.setSchematronValidateErrorMessage(e.getMessage());
               log.warn("Schematron Validation failure: " + e.getMessage());
            }
            continue;
         } // EO process XSLT validation
         
         log.warn("Invalid validator class: " +
               val.getValidator().getClass().getName());
         continue;
         
      } // EO process validations
      
      storeMessage();
      } catch (Exception e ) {
         log.warn("Exception in MessageProcessingThread.run:", e);
      }
   } // EO run() method
   
  private void storeMessage() {
     String hdr = rawMessage;
     if (hdr.contains("<?xml")) hdr = hdr.substring(0, hdr.indexOf("<?xml"));
     else if (hdr.length() > 35) hdr = hdr.substring(0,35);
     log.info(xmitType + " " + senderIp + ":" + collectorPort + " - Storing " +
          rawMessage.length() + " bytes, hdr= " + hdr);
     if (useSyslogDB) storeMessageDB();
     if (storeMessages) storeMessageFile();
  }
  
  private void storeMessageFile() {
     String path = messageDirectory + File.separator;
     String pfn = "";
     FileWriter fw;
     // ------------ basic file name
     SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmssSSS");
     String fname = senderIp + "-" + collectorPort + "-" + df.format(new Date());
     //                     raw message
     try {
        pfn = path + fname + ".msg";
        fw = new FileWriter(pfn);
        fw.write(rawMessage);
        fw.close();
     } catch (Exception e) {
        log.error("Error writing " + pfn + " : " + e.getMessage());
     }
     //                     xml payload
     try {
        pfn = path + fname + ".xml";
        fw = new FileWriter(pfn);
        fw.write(xmlMessage);
        fw.close();
     } catch (Exception e) {
        log.error("Error writing " + pfn + " : " + e.getMessage());
     }
     //                 build status message
     StringBuilder b = new StringBuilder();
     if (errorMessage.length() > 0) b.append(errorMessage).append(nl);
     b.append("RFC-5424 parse ")
      .append(result(rfc5424Parse, rfc5424ErrorMessage)).append(nl);
     b.append("RFC-3164 parse ")
      .append(result(rfc3164Parse, rfc3164ErrorMessage)).append(nl);
     b.append("XML parse ")
      .append(result(xmlParse, xmlParseErrorMessage)).append(nl);
     b.append("RFC-3881 Schema Validate ")
      .append(result(rfc3881Validate, rfc3881ValidateErrorMessage)).append(nl);
     b.append("DICOM Schema Validate ")
     .append(result(dicomValidate, dicomValidateErrorMessage)).append(nl);
     b.append("Event Type ").append(eventType).append(nl);
     b.append("Event Id ").append(eventId).append(nl);
     b.append("Message Name ").append(messageName).append(nl);
     if (schematrons == null) 
        b.append("No validations available for this msg").append(nl);
     if (schematrons != null) {
        for (NamedValidator v : schematrons) {
           if (v.getSchematronValidate() == 1) {
              b.append("ATNA msg: ").append(v.getAtnaMessageName())
               .append(" validation successful").append(nl);
              if (StringUtils.containsIgnoreCase(v.getSchematronValidateErrorMessage(), "Informational:" )) {
                 b.append(" with informational notes").append(nl)
                  .append(v.getSchematronValidateErrorMessage()).append(nl);
              }
           }
           if (v.getSchematronValidate() == 2)
              b.append("ATNA msg: ").append(v.getAtnaMessageName())
               .append(" validation failed: ")
               .append(v.getSchematronValidateErrorMessage()).append(nl);
        }
     }
     //                     write status report
     try {
        pfn = path + fname + ".rpt";
        fw = new FileWriter(pfn);
        fw.write(b.toString());
        fw.close();
     } catch (Exception e) {
        log.error("Error writing " + pfn + " : " + e.getMessage());
     }
  } // EO storeMessageFile
   
  private void storeMessageDB() {
     String leader = " syslog ";
     try {
        int syslogId = new Query(DBUtil.SYSLOG_INSERT)
        .set("senderIp", senderIp)
        .set("collectorIp", collectorIp)
        .set("collectorPort", collectorPort)
        .set("errorMessage", errorMessage)
        .set("rfc3164Parse", rfc3164Parse)
        .set("rfc3164ErrorMessage", rfc3164ErrorMessage)
        .set("rfc3164ErrorSubstring", rfc3164ErrorSubstring)
        .set("rfc3164ErrorLocation", rfc3164ErrorLocation)
        .set("rfc5424Parse", rfc5424Parse)
        .set("rfc5424ErrorMessage", rfc5424ErrorMessage)
        .set("rfc5424ErrorSubstring", rfc5424ErrorSubstring)
        .set("rfc5424ErrorLocation", rfc5424ErrorLocation)
        .set("xmlParse", xmlParse)
        .set("xmlParseErrorMessage", xmlParseErrorMessage)
        .set("xmlParseLine", xmlParseLine)
        .set("xmlParseColumn", xmlParseColumn)
        .set("rfc3881Validate", rfc3881Validate)
        .set("rfc3881ValidateErrorMessage", rfc3881ValidateErrorMessage)
        .set("rfc3881ValidateLine", rfc3881ValidateLine)
        .set("rfc3881ValidateColumn", rfc3881ValidateColumn)
        .set("dicomValidate", dicomValidate)
        .set("dicomValidateErrorMessage", dicomValidateErrorMessage)
        .set("dicomValidateLine", dicomValidateLine)
        .set("dicomValidateColumn", dicomValidateColumn)
        .set("eventType", eventType)
        .set("eventId", eventId)
        .set("messageName", messageName)
        .set("rawMessage", DBUtil.escape(rawMessage))
        .set("xmlMessage", DBUtil.escape(xmlMessage))
        .dbInsertSyslog();
        if (schematrons != null ) {
           for (NamedValidator v : schematrons) {
              leader = " shc - " + v.getAtnaMessageName() + " ";
              new Query(DBUtil.SCHEMATRON_INSERT)
              .set("syslogId", syslogId)
              .set("atnaMessageName", v.getAtnaMessageName())
              .set("schematronValidate", v.getSchematronValidate())
              .set("schematronValidateErrorMessage", v.getSchematronValidateErrorMessage())
              .set("schematronValidateLine", v.getSchematronValidateLine())
              .set("schematronValidateColumn", v.getSchematronValidateColumn())
              .dbUpdate();
           }
        }
        int schs = 0;
        if (schematrons != null) schs = schematrons.length;
        log.info("syslog insert, id=" + syslogId + " " + schs + " Schs");
     } catch (Exception e) {
        log.warn("Error storing: " + leader, e);
     }
  }
  
  public class schematronErrorHandler implements ErrorHandler {
     
     private Logger syslog = Util.getSyslog();

   public void error(SAXParseException e) throws SAXException {
      syslog.warn("error: " + e.getMessage());
      throw e;
   }

   public void fatalError(SAXParseException e) throws SAXException {
      syslog.warn("fatalError: " + e.getMessage());
      throw e;
   }

   public void warning(SAXParseException e) throws SAXException {
      syslog.warn("warning: " + e.getMessage());
      throw e;
   }
     
  }
  
  private class XSLTMessageListener implements MessageListener {
     
     NamedValidator val;
     
     public XSLTMessageListener(NamedValidator val) {
        this.val = val;
     }

   public void message(XdmNode content, boolean terminate, SourceLocator locator) {
      StringBuilder em = new StringBuilder("XSLT ");
      if (terminate) em.append("error: ");
      else em.append("xmlMessage: ");
      QName qn = content.getNodeName();
      if (qn != null) {
         em.append("node=").append(qn.getClarkName()).append(" ");
         val.setSchematronValidateLine(content.getLineNumber());
         val.setSchematronValidateColumn(content.getColumnNumber());
      }
      em.append("xsl=").append(locator.getPublicId())
         .append(" lin=").append(locator.getLineNumber())
         .append(" col=").append(locator.getColumnNumber());
      val.setSchematronValidateErrorMessage(em.toString());
      log.debug(em.toString());
   }
     
  } // EO XSLTMessageListener class
  
  private class SchemaErrorHandler extends DefaultHandler {

     private StringBuilder msg = new StringBuilder();
     private boolean messageValid = true;
     
     public void error(SAXParseException e) {        
        String m = e.getMessage();
        // TODO Replace kludge with fix
        if (m.contains("noNamespaceSchemaLocation' is not allowed to appear in element 'AuditMessage'")) return;
        if (m.contains("type' is not allowed to appear in element 'AuditMessage'")) return;
        if (m.contains("nil' is not allowed to appear in element 'AuditMessage'")) return;
        if (m.contains("schemaLocation' is not allowed to appear in element 'AuditMessage'")) return;
        messageValid = false;
        errorMessage("Error", e);
     }
     
     public void fatalError(SAXParseException e) {
        messageValid = false;
        errorMessage("Fatal", e);
     }
     
     private void errorMessage(String prefix, SAXParseException e) {
        msg.append(n)
           .append(prefix)
           .append(": Line=")
           .append(e.getLineNumber())
           .append(", Col=")
           .append(e.getColumnNumber())
           .append(": ")
           .append(e.getMessage());
     }
   
     public void validateMessage() throws Exception {
        if (!messageValid) throw new Exception(msg.toString());
     }
     
  }
  
  private enum MsgType {
     RFC3881 ("code"),
     DICOM   ("csd-code");
     
     private final String codeAttributeName;
     
     MsgType(String can) {
        codeAttributeName = can;
     }
     
     public String getCodeAttributeName() {
        return codeAttributeName;
     }
     
  } // EO enum MsgType
  
  public enum Status {
     NOT_DONE ("Not Done"), 
     SUCCESSFUL ("Successful"),
     FAILED ("Failed"), 
     NA ("Not Applicable");
     
     private final String statusName;
     
     Status(String name) {
        statusName = name;
     }
     
     @Override
     public String toString() {
        return statusName;
     }
     
  } // EO enum Status
  
  private String result(Status status, String em) {
     String result = status.toString();
     if (status == Status.FAILED)
        result = result.concat(" ").concat(em);
     return result;
  }
   

} // EO MessageProcessingThread class
