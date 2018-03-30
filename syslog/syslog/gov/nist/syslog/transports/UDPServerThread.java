package gov.nist.syslog.transports;

import gov.nist.syslog.util.Util;
import gov.nist.syslog.MessageProcessingThread;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.Charset;
import java.util.concurrent.Executor;
import org.apache.log4j.Logger;
/**
 * {@link java.lang.Runnable Runnable} class implementing a UDP server
 * for syslog. {@link gov.nist.syslog.SyslogCollector SyslogCollector}
 * creates one of these threads for each UDP server, as indicated in the
 * SyslogCollector.ini file. Listens for UDP messages on the assigned port. When
 * a message is received, creates a 
 * {@link gov.nist.syslog.MessageProcessingThread
 * MessageProcessingThread} to process the message.
 * @author rmoult01
 */
public class UDPServerThread implements Runnable {
   
   //---------------------------------------------
   //              Properties
   //---------------------------------------------
   private static final int MAX_LENGTH = 32768;
   private Logger syslog;
   private int port;
   private DatagramSocket socket;
   private Executor exec;
   private boolean isRunning = true;
   
   //---------------------------------------------
   //              Constructors
   //---------------------------------------------
   
   public UDPServerThread(int port) {
      this.port = port;
      syslog = Util.getSyslog();
      exec = Util.getExec();
   }
   
   public void shutdown() {
      isRunning = false;
   }

   /** 
    * Creates listen socket. Listens for packets, creates new ProcessingThread
    * to process received packets.
    */
   public void run() {

      // --------------------------------------- Create listen socket
      try {
         socket = new DatagramSocket(port);
         syslog.debug("UDPServerThread listening on port: " + port);
      } catch (Exception e) {
         syslog.error("Could not create UPD server on port " + port + " - "
               + e.getMessage());
         return;
      }
      // ----------------------------------------------- Listen loop
      while (isRunning) {
         String ipAddress = "unknown";
         try {

            byte[] buf = new byte[MAX_LENGTH];
            DatagramPacket packet;

            //---------------------------------------- get message
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            ipAddress = packet.getAddress().getHostAddress();

            String msg = new String(packet.getData(), 0, packet.getLength(),
            	Charset.forName("UTF-8"));
            
            if (msg.length() == 0) throw new Exception("empty message packet");

            //--------------------- process message in thread
            syslog.info("UDP " + ipAddress + ":" + port + " - Message Received");
            MessageProcessingThread t = 
               new MessageProcessingThread(msg, ipAddress, port, "UDP");
            exec.execute(t);

         } catch (Exception e) {
            syslog.warn("Error: UPD port=" + port + " sender=" + ipAddress +
                  " - " + e.getMessage());
         }
      } // EO Listen loop
      
      if (socket != null) socket.close();
      syslog.debug("UDP server on port " + port + " shut down.");
      
   } // EO run()

}
