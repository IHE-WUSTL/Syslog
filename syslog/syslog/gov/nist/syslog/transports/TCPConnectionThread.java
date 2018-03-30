package gov.nist.syslog.transports;

import gov.nist.syslog.util.Util;
import gov.nist.syslog.MessageProcessingThread;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

/**
 * {@link java.lang.Runnable Runnable} class which processes a connection from a
 * TCP or TLS Server for syslog.
 * {@link gov.nist.syslog.transports.TCPServerThread TCPServerThread}, which
 * instantiates both TCP and TLS servers, creates one of these threads to
 * process each connection it receives.
 * 
 * @author rmoult01
 */
public class TCPConnectionThread implements Runnable {

   private static final Charset UTF8 = Charset.forName("UTF-8");
   private static final int cutoffMinutes = 5;
   private int bufferLength = 0;
   private Socket socket;
   private String ipAddress;
   private int port;
   private boolean secure;
   private Logger log;
   private static final int BUFF_SIZE = 10240;
   private char[] buffer = new char[BUFF_SIZE];

   public TCPConnectionThread(Socket socket, String ipAddress, int port, boolean secure) {
      this.socket = socket;
      this.ipAddress = ipAddress;
      this.port = port;
      log = Util.getSyslog();
      this.secure = secure;
   }

   public void run() {
      int messageLength = 0;
      StringBuilder ml = null;
      try {
         // ---------------------------------------------- initialization
         
         socket.setSoTimeout(60000 * cutoffMinutes);

         BufferedReader br = new BufferedReader(new InputStreamReader(
               socket.getInputStream(), UTF8));

         StringBuilder buff = new StringBuilder();

         // ---------------------------- Read / process buffer loop
         int num = 0;
         while (true) {
            // -------- get current length of buffer (data we have)
            bufferLength = buff.length();
            // ------------------------------ read buffer until timeout or close
            while (true) {
               try {
                  num = br.read(buffer);
               } catch (SocketTimeoutException toe) {
                  String em = srv() + "Connection idle > " + cutoffMinutes + 
                     " minutes. closing.";
                  log.warn(em);
                  MessageProcessingThread mpt = new MessageProcessingThread(
                     buff.toString(), ipAddress, port, 
                     (secure ? "TLS" : "TCP"), em);
                  mpt.run();
                  br.close();
                  socket.close();
                  return;
               }
               if (num > 0) buff.append(new String(buffer, 0, num));
               // ------------ if more data in buffer, process it
               if (buff.length() > bufferLength || num == -1) break;
               
            } // EO read/wait loop

            // -------------------------- process complete message(s) in buffer
            while (buff.length() > 0) {
               // -------------- look for message length followed by space
               try {
                  ml = new StringBuilder();
                  for (int n = 0; n < buff.length(); n++) {
                     char c = buff.charAt(n);
                     if (n == 0)
                        if (isNonZeroDigit(c)) {
                           ml.append(c);
                           continue;
                        } else throw new Exception(
                              "first char not nonzero digit");
                     if (isDigit(c)) {
                        ml.append(c);
                        continue;
                     }
                     if (c == ' ') break;
                     throw new Exception("non digit characters");
                  }
                  messageLength = Integer.parseInt(ml.toString());
                  // ----------- Catch errors in message VLI parsing
               } catch (Exception e) {
                  log.warn(srv() + " parsing error invalid msg length "
                        + e.getMessage());
                  // ---------------------------- Process buffer
                  String em = "invalid message length, " + e.getMessage();
                  MessageProcessingThread mpt = new MessageProcessingThread(
                        buff.toString(), ipAddress, port, 
                        (secure ? "TLS" : "TCP"), em);
                  mpt.run();
                  // -------------------------- kill connection
                  br.close();
                  socket.close();
                  return;
               }
               // --------- Do we have the whole message yet?
               
               //-- Starting point of message in buffer
               int start = Math.max(ml.length() + 1, 0); 
               //--- Portion of message received in utf8 bytes 
               byte[] mBytes;
               if (start >= buff.length()) mBytes = new byte[0];
               else mBytes = buff.substring(start).toString().getBytes(UTF8);
               
               //--- Are there 'messageLength' or more bytes?
               if (mBytes.length >= messageLength) {
            	  // First messageLength bytes (utf8) are message
            	  String message = new String(mBytes, 0, messageLength, UTF8);
            	  if (mBytes.length > messageLength)
            		  buff = new StringBuilder(new String(mBytes, messageLength, 
            			 mBytes.length - messageLength, UTF8));
            	  else buff = new StringBuilder();
                  /*
                   * NOTE: MessageProcessingThread is run in place, not exec'd,
                   * because each TCP/TLS connection processes messages from one
                   * source in a serial manner.
                   */
                  MessageProcessingThread mpt = new MessageProcessingThread(
                        message, ipAddress, port, secure ? "TLS" : "TCP");
                  mpt.run();
               } else break;

            } // EO process messages loop
            if (num == -1) break;
         } // EO Read / process buffer loop

      } catch (IOException e) {
         log.warn(srv() + " error reading connection - " + e.getMessage());
         return;
      }

   } // EO run() method

   private String srv() {
      return (secure ? "TLS" : "TCP") + " " + ipAddress + ":" + port + " - ";
   }

   public boolean isDigit(char c) {
      return Character.getType(c) == Character.DECIMAL_DIGIT_NUMBER;
   }

   public boolean isNonZeroDigit(char c) {
      return isDigit(c) && (c != '0');
   }
} // TCP ConnectionThread

