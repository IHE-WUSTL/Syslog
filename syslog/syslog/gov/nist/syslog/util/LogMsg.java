package gov.nist.syslog.util;

import gov.nist.syslog.util.Util.LogType;

public class LogMsg {
    public Util.LogType logType;
    public String message;
    public LogMsg(LogType t, String m) {
        logType = t;
        message = m;
    }
}
