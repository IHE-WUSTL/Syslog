/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.wustl.mir.erl.IHETools.SyslogSender.view;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import edu.wustl.mir.erl.IHETools.Util.FacesUtil;
import edu.wustl.mir.erl.IHETools.Util.Util;
import edu.wustl.mir.erl.IHETools.Util.Valid;
import gov.nist.syslog.SyslogSender;

/**
 *
 * @author rmoult01
 */
@ManagedBean
@ViewScoped
public class SenderBean implements Serializable {

   static final long serialVersionUID = 1L;
   private ApplicationBean applicationBean;

   SyslogSender sender;

   private String collectorIP;
   private String port;
   private String xmitType;
   private String sampleMessageFileName;
   private String certificateName;

   private boolean messageSent;
   private String message;
   private boolean displayXml;

    /**
     * Creates a new instance of SenderBean
     * @throws Exception on initialization error
     */
    public SenderBean() throws Exception {
       applicationBean = (ApplicationBean) FacesUtil.getManagedBean("applicationBean");
       initialize();
    }

   public String getCollectorIP() {
      return collectorIP;
   }

   public void setCollectorIP(String collectorIP) {
      this.collectorIP = collectorIP;
   }

   public String getPort() {
      return port;
   }

   public void setPort(String port) {
      this.port = port;
   }

   public String getXmitType() {
      return xmitType;
   }

   public void setXmitType(String xmitType) {
      this.xmitType = xmitType;
   }

   public String getSampleMessageFileName() {
      return sampleMessageFileName;
   }

   public void setSampleMessageFileName(String sampleMessageFileName) {
      this.sampleMessageFileName = sampleMessageFileName;
   }
   
   public boolean isTLS() {
      return xmitType.equalsIgnoreCase("TLS");
   }

   public String getCertificateName() {
      return certificateName;
   }

   public void setCertificateName(String certificateName) {
      this.certificateName = certificateName;
   }
   
   

   public String getMessage() {
      return message;
   }


   public boolean isMessageSent() {
      return messageSent;
   }

   public void setMessageSent(boolean messageSent) {
      this.messageSent = messageSent;
   }

   public String getXml() {
      return Util.prettyPrintXML(message);
   }

   public String getMessageButtonValue() {
      return displayXml ? "Show message" : "show Xml";
   }

   public boolean isDisplayXml() {
      return displayXml;
   }

   public void setDisplayXml(boolean displayXml) {
      this.displayXml = displayXml;
   }
   

   private void initialize() throws Exception {
      sender = new SyslogSender(applicationBean.getRunSyslogSender().toString(), null);
      clear();
   }

   public String send() {
      int iPort = 0;
      Valid v = new Valid();
      v.startValidations();
      v.Ip("IP address", collectorIP, true);
      try {
         iPort = Integer.parseInt(port);
         v.Port("Port #", iPort, true);
      } catch (NumberFormatException nfe) {
         v.error("Port #", nfe.getMessage());
      }
      if (v.isErrors()) return null;

      try {
         String pfn = 
        	applicationBean.getSampleMessageDirectory()
        	.resolve(sampleMessageFileName).toString();
         sender.syslogSend(collectorIP, iPort, xmitType, pfn, certificateName);
      } catch (Exception e) {
         v.error(e.toString());
      }
      message = sender.getLastMessage();
      messageSent = true;
      displayXml = true;
      return null;
   }

   public void clear() {
      setCollectorIP("");
      setPort("");
      setXmitType("TLS");
      setCertificateName((String)applicationBean.getCertificateNames()[0].getValue());
      String d = applicationBean.getSampleMessages()[0].getDescription();
      setSampleMessageFileName(d);
      messageSent = false;
      message = "";
   }

   public void flipMessage() {
      displayXml = !displayXml;
   }

}
