IHE ATNA Test Syslog Collector and Syslog Sender

For support: Ralph Moulton moulton@mir.wustl.edu

Need Sun Java 1.7+ postgresql (I used 8.4.11)

I run this on Ubuntu. Checked it out briefly on Windows.

INSTALLATION

Unzip to a directory of your choice. The unzip will create a directory matching
the current version of the software (for example, syslog-1.4)

Control files, scripts, and other related data are contained in the
run/SyslogCollector directory (the "run" directory)

There are currently three "Profiles" supported for running the Syslog Collector:
   Production: Started using run.sh.
               Refers to SyslogCollector.ini and log4j.properties in run
               directory.
               Meant to run at Connectathons, or for supporting between
               connectathon testing. 
               Saves messages to postgresql database.
               generates daily rolling logs in the logs
               directory, at the INFO level
   Development: Started using run-dev.sh.
                Refers to SyslogCollector.dev.ini and log4j.dev.properties in 
                run directory.
                Meant for development and testing. Saves messages to db,
                generates console logs at the DEBUG level.
   FreeStanding: Started using run-fs.sh. 
                 Refers to SyslogCollector.fs.ini and log4j.fs.properties in run
                 directory.
                 Meant for users who wish to run the Syslog Collector on their 
                 own systems for private testing, write messages and results to
                 text files, generates console logs at the DEBUG level.

To run Syslog Collector:

1. Add JAVA_HOME to your environment and JAVA_HOME/bin to your PATH.

2. Decide which profile you want to run.

3. Examine SyslogCollector.ini file and the log4j.properties file which are 
referred to by your profile to see if you need to make any changes. The .ini
files are commented, so read about the options there.

4. cd to SyslogCollector and ./run.sh (or run-dev.sh or run-fs.sh) for Linux, 
run.bat for Windows. You may need to add execute permission, as not all unzips 
preserve these. Have fun.

To run Syslog Collector as an auto-start service on Ubuntu (and similar) Linux:

1. Install as above, steps 1 thru 3. 

2. Edit syslog-collector (in run/SyslogCollector) to have the correct JAVA_HOME,
PATH, and to cd to the correct run/SyslogCollector directory.

3. Copy syslog-collector to /etc/init.d, chown it to root:root and chmod to 755.

4. Create a link to it in rc2.d called S20syslog-collector. Create a link to it
in rc1.d called K20syslog-collector.

5. cmod the run.sh file (in run/SyslogCollector) to 4755.

To run Syslog Sender

1. Examine the SyslogSender/SyslogSender.ini file to see if you need to 
make any changes. Read the comments there.

As delivered, Syslog Sender will send an ITI-2 user authentication login audit 
message to port 3001 on localhost using UDP. Change the .ini file parameters to 
pick your server port, transmission protocol, and point to the message you want 
to send.

2. As with the SyslogCollector, you can mess with the log4j.properties file
if you want.

3. When you are ready to send, cd to SyslogSender and ./run.sh for linux, 
run.bat for Windows.

