package com.dynatrace.diagnostics.plugins.logfile.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logfile.WP;

//import com.dynatrace.diagnostics.plugins.vmstat.PrimitiveCompressingLogger;

public abstract class ConnectionMethod {
	//protected final PrimitiveCompressingLogger log = PrimitiveCompressingLogger.getLogger(ConnectionMethod.class.getName());
	private static final Logger LOGGER = 
		      Logger.getLogger(WP.class.getName());
	
    public String executeCommand(String command, String env) throws Exception {
        if (LOGGER.isLoggable(Level.FINER))
            LOGGER.finer("Sending command string: " + command);
        LOGGER.info("Sending command string: " + command);
        String result = executeStringCommand(command, env);
        LOGGER.info("Received result to parse: " + result);
        if (LOGGER.isLoggable(Level.FINEST)) {
        	LOGGER.finest("Received result to parse: " + result);
        }
        return result;
    }

    public abstract Object openSession(String env) throws Exception;
    public abstract void closeSession(Object session) throws Exception;
    public abstract void executePersistentCommand(String cmd, String env, LineCallback callback, Object session) throws Exception;

	protected abstract String executeStringCommand(String command, String env) throws Exception;

	public void setup(String host, String user, String pass, int port) throws Exception {
	}

	public void teardown() throws Exception {
	}

	/**
	 * Validates the persistent connection (if any) and attempts to reconnect if it is down.
	 * @throws Exception may be thrown if reconnect fails.
	 */
	public void reconnectIfNecessary() throws Exception {
	}

	protected String readInputStream(InputStream is) throws IOException {
		StringBuilder strBuild = new StringBuilder();
		BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(is));

		while (true) {
			String line = stdoutReader.readLine();
			if (line == null)
				break;
            strBuild.append(line).append("\n");
		}
		int index = strBuild.lastIndexOf("\n");
		return (index >= 0) ? strBuild.substring(0, index) : "";
	}

    protected void readPersistentInputStream(InputStream is, LineCallback callback) throws IOException {
        BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(is));
        while (true) {
            String line = stdoutReader.readLine();
            if (line == null)
                break;
            callback.lineRead(line);
        }
    }

	public static ConnectionMethod getConnectionMethod(String method) {
		if (method.equals("LOCAL")) {
			return new LocalConnectionMethod();
		} else {
			return new SSHConnectionMethod();
		}
	}
}
