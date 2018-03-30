@ECHO OFF
rem ************************************************
rem SyslogCollector run                    R Moulton
rem This directory must be current to run
rem ************************************************
SET ROOT=..\..\
SET CLASS=gov.nist.syslog.SyslogCollector
SET CP=%ROOT%dist\SyslogCollector.jar;%ROOT%lib\*
java -cp %CP% %CLASS%