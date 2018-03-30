Relies on syslog, which is not a maven project. Current version of syslog 
needs to be added to the maven repository. For example

mvn install:install-file -Dfile=/home/rmoult01/syslog/syslog/dist/SyslogCollector.jar \
    -DgroupId=gov.nist.syslog -DartifactId=SyslogCollector -Dversion=2.1 \
    -Dpackaging=jar
    
Relies on wildcard 1.03, which needs to be added to the maven repository.

mvn install:install-file -Dfile=/opt/wildcard-1.03/wildcard-1.03.jar \
    -DgroupId=com.esotericsoftware.wildcard -DartifactId=wildcard -Dversion=1.03 \
    -Dpackaging=jar
    

Currently 2 profiles:

   dev = for development
         looks for SyslogSender at /home/rmoult01/syslog/syslog/run/SyslogSender
         logs at TRACE level to console
         
   prod = for production
          Rolling logs.
          looks for SyslogSender at /opt/syslog*/run/SyslogSender
          Context SyslogSender
          NOTE: Uses SyslogSender-prod.war. This should be renamed to
          SyslogSender.war before being copied to webapps.
         