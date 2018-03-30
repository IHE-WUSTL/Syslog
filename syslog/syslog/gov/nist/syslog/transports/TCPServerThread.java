package gov.nist.syslog.transports;

import gov.nist.syslog.util.Util;
import java.io.FileInputStream;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.Executor;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import org.apache.log4j.Logger;

/**
 * {@link java.lang.Runnable Runnable} class implementing a TCP or TLS server
 * for syslog. {@link gov.nist.syslog.SyslogCollector SyslogCollector}
 * creates one of these threads for each TCP and each TLS server, as indicated 
 * in the SyslogCollector.ini file. Listens for connections on the assigned 
 * port. When a connection is received, creates a 
 * {@link gov.nist.syslog.transports.TCPConnectionThread
 * TCPConnectionThread} to handle the connection.
 * @author rmoult01
 */
public class TCPServerThread implements Runnable {
   
   //---------------------------------------------
   //              Properties
   //---------------------------------------------
   private final int timeout = 300000;
   private Logger log;
   private int port;
   private Executor exec;
   private boolean isRunning = true;
   private boolean secure = false;
   private String ks = null;
   private String pw = null;
   
   private SecureRandom rand;
   private KeyStore keyStore;
   private KeyManagerFactory kmf;
   private TrustManagerFactory tmf;
   private SSLContext sslContext;
   private SSLServerSocketFactory ssFactory;
   private SSLServerSocket tlsss;
   private ServerSocket ss;
   private static final String[] cyphersuites = {"TLS_RSA_WITH_AES_128_CBC_SHA"};
   
   //---------------------------------------------
   //              Constructors
   //---------------------------------------------
   
   public TCPServerThread(int port) {
      this.port = port;
      log = Util.getSyslog();
      exec = Util.getExec();
   }
   
   public TCPServerThread(int port, String keystore, String password) {
      this.port = port;
      log = Util.getSyslog();
      exec = Util.getExec();
      ks = keystore;
      pw = password;
      secure = true;
   }
   
   public void shutdown() {
      isRunning = false;
   }

   /** 
    * Creates listen socket. Listens for connections. Creates new Thread to
    * process connections.
    */
   public void run() {
      //------------------------------------ Create listen socket
      try { 
         if (secure) {
            rand = new SecureRandom();
            rand.nextInt();

            //-------------------------------------- keystore
            try {
               keyStore = KeyStore.getInstance("JKS");
               keyStore.load(new FileInputStream(ks), pw.toCharArray());
               kmf = KeyManagerFactory.getInstance("SunX509");
               kmf.init(keyStore, pw.toCharArray());

               //------------------------------------ truststore
               tmf = TrustManagerFactory.getInstance("SunX509");
               tmf.init(keyStore);
               log.debug("server certificates initialized");

               //-------------------------------- Context and socket factory
               sslContext = SSLContext.getInstance("TLS");
               sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), rand);
               log.debug("TLS trust context initialized");
            } catch (Exception e) {
               log.warn(srv() + " certificate error " + e.getMessage());
               return;
            }

            ssFactory = sslContext.getServerSocketFactory();
            tlsss = (SSLServerSocket) ssFactory.createServerSocket(port);
            tlsss.setEnabledCipherSuites(cyphersuites);
            tlsss.setNeedClientAuth(true);
            tlsss.setSoTimeout(timeout);
            ss = (ServerSocket) tlsss;

         } else {
            ss = new ServerSocket(port);
            ss.setSoTimeout(timeout);
         }
         log.debug(srv() + " listening");
      } catch (java.io.IOException e) {
         ss = null;
         log.error("Could not create " + srv() + " - " + e.getMessage());
         return;
      }
      //-------------------------------------------- Listen loop
      while (isRunning) {
         try {
            Socket socket = ss.accept();
            String ip = socket.getInetAddress().getHostAddress();
            log.info(con(ip) + "Connection");
            TCPConnectionThread t = new TCPConnectionThread(socket, ip, port, secure);
            exec.execute(t);
         } catch (InterruptedIOException ie) {
            log.debug(srv() + " timer");
         } catch (Exception e) {
            log.warn(srv() + 
                  " Error accepting connections " + e.getMessage());
         }
      } // EO is running loop
         
   } // EO run() method
   
   private String srv() {
      return (secure ? "TLS" : "TCP") + " on " + port;
   }
   private String con(String ip) {
      return (secure ? "TLS" : "TCP") + " " + ip + ":" + port + " - ";
   }

} // EO TCPServerThread class













