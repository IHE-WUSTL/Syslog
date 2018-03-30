package gov.nist.syslog;

import java.io.File;
import java.util.concurrent.Executor;
import org.apache.log4j.Logger;
import gov.nist.syslog.util.DBUtil;
import gov.nist.syslog.util.DataBaseConnection;
import gov.nist.syslog.util.Query;
import gov.nist.syslog.transports.*;
import gov.nist.syslog.util.Util;

public class SyslogCollector {

   private static final String name = "SyslogCollector";
   private static Logger log;
   @SuppressWarnings("unused")
   private static ValidationSchemaFactory validationSchemaFactory = null;

   /**
    * Main method for IHE Syslog Collector. Generates UPD, TCP, and TLS server
    * threads as specified in SyslogCollector.ini. Uses standard
    * {@link gov.nist.syslog.util.Util Util} .ini configuration, see:
    * {@link gov.nist.syslog.util.Util#processMainArguments(String, String[], Object[])
    * processMainArguments} for details.<br/>
    * 
    * @param args
    *           command line arguments.
    */
   public static void main(String[] args) {

      Executor exec;
      String[] ports;
      int port;

      // -------- Process command line arguments; initialize Util.
      try {
         if (Util.processMainArguments(name, args, null)) return;
      } catch (Exception e) {
         System.out.println(e.getMessage());
         return;
      }
      log = Util.getSyslog();
      exec = Util.getExec();

      // ----------------------- check for syslog DB, create if needed
      try {
         boolean useSyslogDB = true;
         String str = Util.getParameterString("UseSyslogDB", "YES");
         if (str.equalsIgnoreCase("NO") || str.equalsIgnoreCase("FALSE"))
            useSyslogDB = false;
         if (useSyslogDB) {
            DataBaseConnection dbc = DataBaseConnection
                  .getDataBaseConnection(DBUtil.DBNAME);

            if (!DBUtil.userExists(dbc.getUser()))
               DBUtil.createUser(dbc.getUser(), dbc.getPassword(), true);

            if (!DBUtil.databaseExists(DBUtil.DBNAME)) {
               DBUtil.createDatabase(DBUtil.DBNAME);
               new Query(DBUtil.CREATE_SYSLOG_TABLE).dbUpdates();
            }
         }

      } catch (Exception e2) {
         log.error("Startup database check failed: " + e2.getMessage());
         return;
      }

      /*
       * This is not used here, but it instantiated at this point so that its
       * static block (which builds all the validation schema objects) will run
       * before the servers are started.
       */
      validationSchemaFactory = new ValidationSchemaFactory();

      // ------------------------------------- Set up UDP Servers
      try {
         ports = Util.getParameterStringArray("Servers", "UDPPorts");
         for (int i = 0; i < ports.length; i++) {
            try {
               port = Integer.parseInt(ports[i]);
               if (port < 0 || port > 65536)
                  throw new NumberFormatException(port + " not valid port");
            } catch (NumberFormatException e) {
               log.warn("UPD port " + ports[i] + " skipped: "
                     + e.getMessage());
               continue;
            }
            UDPServerThread t = new UDPServerThread(port);
            exec.execute(t);
            log.debug("UDP Server Thread started on port " + port);
         }
      } catch (Exception e) {
         log.warn("Error reading [Servers] UDPPorts. UDP servers not started:"
               + e.getMessage());
      } // EO Set up UDP Servers

      // --------------------------------------- Set up TCP Servers
      try {
         ports = Util.getParameterStringArray("Servers", "TCPPorts");
         for (int i = 0; i < ports.length; i++) {
            try {
               port = Integer.parseInt(ports[i]);
               if (port < 0 || port > 65536)
                  throw new NumberFormatException(port + " not valid port");
            } catch (NumberFormatException e) {
               log.warn("TCP port " + ports[i] + " skipped: "
                     + e.getMessage());
               continue;
            }
            TCPServerThread t = new TCPServerThread(port);
            exec.execute(t);
            log.debug("TCP Server Thread started on port " + port);
         }

      } catch (Exception e) {
         log.warn("Error reading [Servers] TCPPorts. TCP servers not started:"
               + e.getMessage());
      } // EO Set up TCP Servers

      // --------------------------------------- Set up TLS Servers
      String keystore = "", password = "";
      try {
         try {
            keystore = Util.getParameterString("Servers", "Keystore",
                  "server.keystore");
            if (!keystore.startsWith(File.separator)) {
               keystore = Util.getRunDirectoryPath() + File.separator
                     + keystore;
            }
            File ks = new File(keystore);
            if (!ks.exists())
               throw new Exception("keystore file not found: " + keystore);
            if (!ks.isFile())
               throw new Exception("keystore not a file: " + keystore);
            if (!ks.canRead())
               throw new Exception("keystore not readable: " + keystore);

            password = Util.getParameterString("Servers", "Password",
                  "password");
         } catch (Exception e1) {
            throw new Exception("Error reading Keystore/password "
                  + e1.getMessage());
         }
         try {
            ports = Util.getParameterStringArray("Servers", "TLSPorts");
         } catch (Exception e1) {
            throw new Exception("Error reading TLSPorts " + e1.getMessage());
         }
         for (int i = 0; i < ports.length; i++) {
            try {
               port = Integer.parseInt(ports[i]);
               if (port < 0 || port > 65536)
                  throw new NumberFormatException(port + " not valid port");
            } catch (NumberFormatException e) {
               log.warn("TLS port " + ports[i] + " skipped: "
                     + e.getMessage());
               continue;
            }
            TCPServerThread t = new TCPServerThread(port, keystore, password);
            exec.execute(t);
            log.debug("TLS Server Thread started on port " + port);
         }
         log.info("SyslogCollector startup complete");

      } catch (Exception e) {
         log.warn(e.getMessage());
      } // EO Set up TCP Servers

   } // EO main(String[] args) method

} // EO SyslogCollector class

