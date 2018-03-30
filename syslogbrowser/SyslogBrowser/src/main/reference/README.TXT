Currently three profiles:
    dev = For development. 
          Does not use CAS Authentication.
          Expects postgresql server on localhost with root pw development.
          Uses TRACE logging to console.
          Context SyslogBrowser
          
    na  = For North American users.
          Uses CAS on ihe.wustl.edu
          Expects postgresql server on localhost with root pw syslog.
          Context SyslogBrowser-na
          
    eu  = For European Union users.
          Uses CAS on gazelle.ihe.net
          Expects postgresql server on localhost with root pw syslog.
          syslog_admin db is named syslog_admin_eu
          Context SyslogBrowser-eu