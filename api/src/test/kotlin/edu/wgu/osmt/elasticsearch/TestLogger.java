package edu.wgu.osmt.elasticsearch;

import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.List;

public class TestLogger implements Logger {

    private boolean traceEnabled;
    private boolean debugEnabled;
    private boolean infoEnabled;
    private boolean warnEnabled;
    private boolean errorEnabled;

    private final List<String> log = new ArrayList<>();

    public TestLogger(String level) {
        switch (level.toUpperCase().charAt(0)) {
            case 'T': traceEnabled = true;
            case 'D': debugEnabled = true;
            case 'I': infoEnabled = true;
            case 'W': warnEnabled = true;
            case 'E': errorEnabled = true;
        }
    }

    public List<String> getLog() {
        return log;
    }

    protected void doLog(String level, Throwable throwable, Marker marker, Object message, Object[] parameters) {
        String text = level + ": ";
        if (marker != null) {
            text += "Marker: " + marker.getName() + " ";
        }

        if (throwable != null) {
            text += "Throwable: " + throwable.getMessage() + " ";
        }

        if (parameters == null) {
            text += "Message: " + message;
        } else {
            final String arg = "\\Q{}\\E";
            String format = String.valueOf(message);
            for (Object parameter : parameters) {
                if (parameter instanceof Long) {
                    format = format.replaceFirst(arg, "%d");
                } else if (parameter instanceof String) {
                    format = format.replaceFirst(arg, "%s");
                }
            }

            text += "Message: " + String.format(format, parameters);
        }
        log.add(text);
    }

    @Override
    public String getName() {
        return "testLogger";
    }

    /*** Trace ***/
    @Override public boolean isTraceEnabled() { return traceEnabled; }
    @Override public boolean isTraceEnabled(Marker marker) { return traceEnabled; }
    @Override public void trace(String s) { doLog("trace", null, null, s, null); }
    @Override public void trace(String s, Object... objects) { doLog("trace", null, null, s, objects); }
    @Override public void trace(String s, Object o) { doLog("trace", null, null, s, new Object[]{o}); }
    @Override public void trace(String s, Object o, Object o1) { doLog("trace", null, null, s, new Object[]{o, o1}); }
    @Override public void trace(String s, Throwable throwable) { doLog("trace", throwable, null, s, null); }
    @Override public void trace(Marker marker, String s) { doLog("trace", null, marker, s, null);}
    @Override public void trace(Marker marker, String s, Object o) { doLog("trace", null, marker, s, new Object[]{o});}
    @Override public void trace(Marker marker, String s, Object o, Object o1) { doLog("trace", null, marker, s, new Object[]{o, o1}); }
    @Override public void trace(Marker marker, String s, Object... objects) { doLog("trace", null, marker, s, objects);}
    @Override public void trace(Marker marker, String s, Throwable throwable) { doLog("trace", throwable, marker, s, null); }

    /*** Debug ***/
    @Override public boolean isDebugEnabled() { return debugEnabled; }
    @Override public boolean isDebugEnabled(Marker marker) { return debugEnabled; }
    @Override public void debug(String s) { doLog("debug", null, null, s, null); }
    @Override public void debug(String s, Object... objects) { doLog("debug", null, null, s, objects); }
    @Override public void debug(String s, Object o) { doLog("debug", null, null, s, new Object[]{o}); }
    @Override public void debug(String s, Object o, Object o1) { doLog("debug", null, null, s, new Object[]{o, o1}); }
    @Override public void debug(String s, Throwable throwable) { doLog("debug", throwable, null, s, null); }
    @Override public void debug(Marker marker, String s) { doLog("debug", null, marker, s, null);}
    @Override public void debug(Marker marker, String s, Object o) { doLog("debug", null, marker, s, new Object[]{o});}
    @Override public void debug(Marker marker, String s, Object o, Object o1) { doLog("debug", null, marker, s, new Object[]{o, o1}); }
    @Override public void debug(Marker marker, String s, Object... objects) { doLog("debug", null, marker, s, objects);}
    @Override public void debug(Marker marker, String s, Throwable throwable) { doLog("debug", throwable, marker, s, null); }
    
    /*** Info ***/
    @Override public boolean isInfoEnabled() { return infoEnabled; }
    @Override public boolean isInfoEnabled(Marker marker) { return infoEnabled; }
    @Override public void info(String s) { doLog("info", null, null, s, null); }
    @Override public void info(String s, Object... objects) { doLog("info", null, null, s, objects); }
    @Override public void info(String s, Object o) { doLog("info", null, null, s, new Object[]{o}); }
    @Override public void info(String s, Object o, Object o1) { doLog("info", null, null, s, new Object[]{o, o1}); }
    @Override public void info(String s, Throwable throwable) { doLog("info", throwable, null, s, null); }
    @Override public void info(Marker marker, String s) { doLog("info", null, marker, s, null);}
    @Override public void info(Marker marker, String s, Object o) { doLog("info", null, marker, s, new Object[]{o});}
    @Override public void info(Marker marker, String s, Object o, Object o1) { doLog("info", null, marker, s, new Object[]{o, o1}); }
    @Override public void info(Marker marker, String s, Object... objects) { doLog("info", null, marker, s, objects);}
    @Override public void info(Marker marker, String s, Throwable throwable) { doLog("info", throwable, marker, s, null); }

    /*** Warn ***/
    @Override public boolean isWarnEnabled() { return warnEnabled; }
    @Override public boolean isWarnEnabled(Marker marker) { return warnEnabled; }
    @Override public void warn(String s) { doLog("warn", null, null, s, null); }
    @Override public void warn(String s, Object... objects) { doLog("warn", null, null, s, objects); }
    @Override public void warn(String s, Object o) { doLog("warn", null, null, s, new Object[]{o}); }
    @Override public void warn(String s, Object o, Object o1) { doLog("warn", null, null, s, new Object[]{o, o1}); }
    @Override public void warn(String s, Throwable throwable) { doLog("warn", throwable, null, s, null); }
    @Override public void warn(Marker marker, String s) { doLog("warn", null, marker, s, null);}
    @Override public void warn(Marker marker, String s, Object o) { doLog("warn", null, marker, s, new Object[]{o});}
    @Override public void warn(Marker marker, String s, Object o, Object o1) { doLog("warn", null, marker, s, new Object[]{o, o1}); }
    @Override public void warn(Marker marker, String s, Object... objects) { doLog("warn", null, marker, s, objects);}
    @Override public void warn(Marker marker, String s, Throwable throwable) { doLog("warn", throwable, marker, s, null); }

    /*** Error ***/
    @Override public boolean isErrorEnabled() { return errorEnabled; }
    @Override public boolean isErrorEnabled(Marker marker) { return errorEnabled; }
    @Override public void error(String s) { doLog("error", null, null, s, null); }
    @Override public void error(String s, Object... objects) { doLog("error", null, null, s, objects); }
    @Override public void error(String s, Object o) { doLog("error", null, null, s, new Object[]{o}); }
    @Override public void error(String s, Object o, Object o1) { doLog("error", null, null, s, new Object[]{o, o1}); }
    @Override public void error(String s, Throwable throwable) { doLog("error", throwable, null, s, null); }
    @Override public void error(Marker marker, String s) { doLog("error", null, marker, s, null);}
    @Override public void error(Marker marker, String s, Object o) { doLog("error", null, marker, s, new Object[]{o});}
    @Override public void error(Marker marker, String s, Object o, Object o1) { doLog("error", null, marker, s, new Object[]{o, o1}); }
    @Override public void error(Marker marker, String s, Object... objects) { doLog("error", null, marker, s, objects);}
    @Override public void error(Marker marker, String s, Throwable throwable) { doLog("error", throwable, marker, s, null); }
}
