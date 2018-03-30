package gov.nist.syslog;

import gov.nist.syslog.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.log4j.Logger;

public class SyslogSender implements Serializable {

   private static final long serialVersionUID = 1L;

   private static final String BOM = "\ufeff";
   private static final Charset UTF8 = Charset.forName("UTF-8");

   private static String sender = "-";
   private static final String name = "SyslogSender";
   private static String pid = "-";
   private static String nl = System.getProperty("line.separator");
   private static boolean initialized = false;

   private Logger log;
   private SecureRandom rand;
   private KeyStore keyStore;
   private KeyManagerFactory kmf;
   private TrustManagerFactory tmf;
   private SSLContext sslContext;
   private SSLSocketFactory ssFactory;

   private String lastMessage = null;

   public String getLastMessage() {
      return lastMessage;
   }

   public void setLastMessage(String lastMessage) {
      this.lastMessage = lastMessage;
   }

   private enum Xtype {
      UDP, TCP, TLS
   };

   static {
      try {
         InetAddress s = InetAddress.getLocalHost();
         sender = s.getCanonicalHostName();
         pid = Util.getPid();
      } catch (Exception e) {
      }
   }

   SSLSocket sslSocket;

   /**
    * Main method for IHE Syslog Sender. Generates test syslog message and
    * sends them to a SyslogCollector using UPD, TCP, or TLS as specified in
    * SyslogSender.ini. Uses standard
    * {@link gov.nist.registry.syslog.util.Util Util} .ini configuration, see:
    * {@link gov.nist.registry.syslog.util.Util#processMainArguments(String, String[], Object[])
    * processMainArguments} for details.<br/>
    */
   public static void main(String[] args) {

      // ----------- Default command line parameter values
      String ip = "127.0.0.1";
      int port = 3201;
      String xmitType = "TLS";
      String messageFileName = "messageFile.txt";
      String certificateName = "";

      Object[] addOns = new Object[]{
         (Object) new Option("s", "ServerName", true,
                  "SyslogCollector host/ip"),
                  (Object) new Option("p", "Port", true,
                        "SyslogCollector port number"),
                        (Object) new Option("x", "XmitType", true, "UDP|TCP|TLS"),
                        (Object) new Option("m", "MessageFile", true,
                              "test message file"),
                              (Object) new Option("c", "CertificateName", true,
                                    "Chicago2012|Bern2012")};

      // -------- Process command line arguments; initialize Util.
      try {
         if (Util.processMainArguments(name, args, addOns))
            return;

         // ------------------------------------- load parameter values
         ip = Util.getParameterString("ServerName", ip);
         port = Util.getParameterInt("Port", port);
         xmitType = Util.getParameterString("XmitType", xmitType);
         messageFileName = Util.getParameterString("MessageFile",
               messageFileName);
         // ----- Validate certificate name (only for TLS)
         if (xmitType.equalsIgnoreCase("TLS")) {
            certificateName = Util.getParameterString("CertificateName",
                  certificateName);
            String[] names = Util
                  .getParameterStringArray("CertificateNames");
            if (names.length == 0)
               throw new Exception(
                     "No valid certificate names in .ini file");
            if (StringUtils.isBlank(certificateName))
               certificateName = names[0];
            boolean found = false;
            for (int i = 0; i < names.length; i++) {
               if (certificateName.equalsIgnoreCase(names[i])) {
                  found = true;
                  break;
               }
            }
            if (!found)
               throw new Exception("Unknown certificate name: "
                     + certificateName);
         } // EO if TLS

         SyslogSender sender = new SyslogSender();
         sender.log = Util.getSyslog();
         sender.syslogSend(ip, port, xmitType, messageFileName,
               certificateName);
      } catch (Exception e) {
         System.out.println(e.getMessage());
      }
      return;
   } // EO main method

   public SyslogSender() {
   }

   public SyslogSender(String runDir, Map<String, String> pmap)
         throws Exception {
      if (!initialized) {
         initialized = true;
         Util.initialize(runDir, "SyslogSender", pmap);
      }
      log = Util.getSyslog();
   }

   public void syslogSend(String ip, int port, String xmitType,
         String messageFileName, String cName) throws Exception {

      try {

         InetAddress collector = null;
         Xtype xtype = null;
         String msgPfn = null;
         File messageFile = null;
         lastMessage = "Not available";

         // *****************************************************************
         // Validate parameter values
         // *****************************************************************

         // ---------------------------------------- Syslog Collector name/ip
         ip = StringUtils.trimToEmpty(ip);
         if (ip.length() == 0)
            throw new Exception("null or empty Syslog Collector name/ip");
         try {
            collector = InetAddress.getByName(ip);
         } catch (UnknownHostException unkHost) {
            throw new Exception("unknown Syslog Collector name/ip " + ip);
         }

         // ----------------------------------- Syslog Collector port number
         if (port <= 0)
            throw new Exception("invalid port number " + port);

         // ---------------------------------------- xmit type [UPD|TCP|TLS]
         xmitType = StringUtils.trimToEmpty(xmitType);
         if (xmitType.length() == 0)
            throw new Exception("null or empty xmit type");
         try {
            xtype = Xtype.valueOf(xmitType);
         } catch (IllegalArgumentException iae) {
            throw new Exception("no such xmit type as " + xmitType);
         }

         // ---------------------------------------------- message file name
         msgPfn = StringUtils.trimToEmpty(messageFileName);
         if (msgPfn.length() == 0)
            throw new Exception("null or empty message file name");
         if (!msgPfn.startsWith(File.separator))
            msgPfn = Util.getRunDirectoryPath() + File.separator + msgPfn;
         messageFile = new File(msgPfn);
         if (!messageFile.exists())
            throw new Exception("Message file " + messageFileName
                  + " not found");
         if (!messageFile.isFile())
            throw new Exception(messageFileName + " not a valid file");
         if (!messageFile.canRead())
            throw new Exception(messageFileName + " not readable");

         // *****************************************************************
         // build message to send
         // *****************************************************************

         /*
          * parameter substitutions in stock messages are used to make
          * message more realistic.
          */
         Map<String, String> vals = new HashMap<String, String>();
         vals.put("hostName", sender);
         vals.put("timeStamp", Util.getRFC3339TimeStamp());
         vals.put("procId", pid);
         vals.put("appName", name);
         StrSubstitutor sub = new StrSubstitutor(vals);

         /*
          * msg is the message which is going to be sent. prt is the message
          * xml, which will be printed to log. If the message file extension
          * is ".xml", it is understood that the file contains only the xml
          * payload, and that an RFC-5424 header will be added. If the
          * extension is not ".xml", it is understood that the header is
          * already present.
          */

         // ------------------------------ message RFC-5424 ABNF header
         StringBuilder msg = new StringBuilder();
         boolean skip = false;
         if (StringUtils.endsWith(msgPfn, ".xml")) {
            skip = true;
            msg.append("<85>1 ${timeStamp} ${hostName} ${appName} ${procId} IHE+RFC-3881 - ");
            msg.append(BOM);
         }
         StringBuilder prt = new StringBuilder();

         // --- Read message file lines and build msg and prt messages.
         String line, trimmed;
         BufferedReader in = new BufferedReader(new FileReader(messageFile));
         while ((line = in.readLine()) != null) {
            trimmed = myTrim(line);
            if (trimmed == null || trimmed.length() == 0)
               continue;
            if (skip) {
               if (trimmed.startsWith("<?xml")
                     || trimmed.startsWith("<AuditMessage"))
                  skip = false;
               else
                  continue;
            }
            msg.append(trimmed);
            prt.append(line).append(nl);
         }
         in.close();
         if (prt.length() == 0)
            throw new Exception("message is empty");

         // ---------------------------------- parameter substitution
         msg = new StringBuilder(sub.replace(msg.toString()));
         lastMessage = msg.toString();
         prt = new StringBuilder(sub.replace(prt.toString()));

         log.info("header: " + msg.substring(0, 120));

         log.info(messageFileName + ": " + nl + prt.toString());

         switch (xtype) {

         case UDP :
            DatagramSocket dgsocket = null;
            dgsocket = new DatagramSocket();
            byte[] buf = new byte[msg.toString().getBytes(UTF8).length];
            DatagramPacket packet = new DatagramPacket(buf, buf.length,
                  collector, port);
            packet.setData(msg.toString().getBytes(UTF8));
            dgsocket.send(packet);

            dgsocket.close();
            break;

         case TCP :
            wrap(msg);
            Socket sock;
            sock = new Socket(collector, port);
            sock.setSendBufferSize(100);
            OutputStream out = sock.getOutputStream();
            out.write(msg.toString().getBytes(UTF8));
            out.flush();
            out.close();
            sock.close();
            break;
         case TLS :
            wrap(msg);
            String keystoreFileName,
            keystoreFilePfn,
            pw;
            File keystoreFile;
            pw = Util.getParameterString(cName + ".Password",
                  "clientpw");
            keystoreFileName = Util.getParameterString(cName
                  + ".Keystore", "client.keystore");
            keystoreFilePfn = keystoreFileName;
            if (!keystoreFileName.startsWith(File.separator))
               keystoreFilePfn = Util.getRunDirectoryPath()
               + File.separator + keystoreFileName;
            keystoreFile = new File(keystoreFilePfn);
            if (!keystoreFile.exists())
               throw new Exception(keystoreFileName + " not found");
            if (!keystoreFile.isFile())
               throw new Exception(keystoreFileName
                     + " not valid file");
            if (!keystoreFile.canRead())
               throw new Exception(keystoreFileName + " not readable");

            rand = new SecureRandom();
            rand.nextInt();
            // -------------------------------- keystore / truststore
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keystoreFile),
                  pw.toCharArray());
            kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, pw.toCharArray());

            tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);
            log.debug("keystore/truststore established");

            // --------------------------------------------- SSL context
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(),
                  tmf.getTrustManagers(), rand);
            // -------------------------------------------------- socket
            ssFactory = sslContext.getSocketFactory();
            sslSocket = (SSLSocket) ssFactory.createSocket(ip, port);
            log.debug("secure socket connection established");

            OutputStream sout = sslSocket.getOutputStream();
            log.debug("PrintWriter created");
            sout.write(msg.toString().getBytes(UTF8));
            log.debug("write completed");
            sout.flush();
            log.debug("flush completed");
            sout.close();
            log.debug("PrintWrite closed");
            sslSocket.close();
            log.debug("Socket closed");

            break;
         default :
            log.error("switch defaulted");
            return;
         } // EO switch(x) block

         log.info("message sent");

      } catch (Exception exc) {
         log.warn(exc.toString());
         exc.printStackTrace();
         throw exc;
      }

   } // syslogSend() method

   private static void wrap(StringBuilder msg) {
      int ml = msg.toString().getBytes(UTF8).length;
      msg.insert(0, (Integer.toString(ml) + " "));
   }

   private static String myTrim(String in) {
      if (StringUtils.trimToEmpty(in).length() == 0)
         return "";
      String s = in;
      while (Character.isWhitespace(s.charAt(0))) {
         s = s.substring(1);
      }
      while (true) {
         char a = s.charAt(s.length() - 1);
         if (Character.isWhitespace(a) && a != ' ') {
            s = s.substring(0, s.length() - 1);
            continue;
         }
         break;
      }
      return s;
   }

} // EO SyslogSender class
