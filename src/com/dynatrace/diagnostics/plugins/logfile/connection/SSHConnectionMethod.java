package com.dynatrace.diagnostics.plugins.logfile.connection;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logfile.WP;
//import com.dynatrace.diagnostics.plugins.vmstat.PrimitiveCompressingLogger;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.trilead.ssh2.StreamGobbler;

public class SSHConnectionMethod extends ConnectionMethod {
	//private final PrimitiveCompressingLogger log = PrimitiveCompressingLogger.getLogger(SSHConnectionMethod.class.getName());
	private static final Logger LOGGER = 
		      Logger.getLogger(WP.class.getName());
	private Connection conn;
	private Session session;

	private String host, user, pass;
	private int port;

	private String keyFile;

    @Override
    public Object openSession(String env) throws Exception {
        return conn.openSession();
    }

    @Override
    public void closeSession(Object session) throws Exception {
        this.session.close();
        if (session instanceof Session) {
            ((Session)session).close();
        }
    }

    @Override
	public void executePersistentCommand(String cmd, String env, LineCallback callback, Object session) throws Exception {
    	Session s = ((Session)session);
        s.execCommand(((env.isEmpty()) ? "" : (env + " ")) + "LANG=C " + cmd);
        readPersistentInputStream(new StreamGobbler(s.getStdout()), callback);
    }


    @Override
	public String executeStringCommand(String cmd, String env) throws Exception {
		String output = "";
		String errors = "";

		try {
			session = conn.openSession();
			session.execCommand(((env.isEmpty()) ? "" : (env + " "))
				+ "LANG=C " + cmd);
			output = readInputStream(new StreamGobbler(session.getStdout()));
			errors = readInputStream(new StreamGobbler(session.getStderr()));
		} catch (Exception e) {
			LOGGER.warning("Cannot establish a connection...");
			//LOGGER.warning(e.toString());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			LOGGER.warning(sw.toString());
		}finally {
			session.close();
		}
		if (output.equals(""))
			throw new IOException("Command \"" + cmd + "\" did not produce any output. stderr was: " + errors);

		return output;
	}

	@Override
	public void reconnectIfNecessary() throws IOException {
		try {
			Session testSession = conn.openSession();
			testSession.close();
		} catch (Exception e) {
			LOGGER.info("Connection seems to be down, trying to reconnect..");
			connect();
		}
	}

	private void connect() throws IOException {
		boolean isAuthenticated;
		try {
			if (conn != null) {
				conn.close();
			}
			conn = new Connection(host, port);
			conn.connect();

			if (keyFile != null) {
				if(LOGGER.isLoggable(Level.INFO)){
					LOGGER.info("SSH Publickey authentication");
				}
				boolean available = conn.isAuthMethodAvailable(user, "publickey");
				if (!available) {
					throw new IOException("Authentication-Method publickey not available");
				}
				File pemFile = new File(keyFile);
				isAuthenticated = conn.authenticateWithPublicKey(user, pemFile, pass);
				LOGGER.info("Authenticated with Public Key");
			} else {
				boolean available = conn.isAuthMethodAvailable(user, "password");
				if (!available) {
					throw new IOException("Authentication-Method password is not available");
				}
				isAuthenticated = conn.authenticateWithPassword(user, pass);
				LOGGER.info("Authenticated with SSH Password");
			}
			if (!isAuthenticated) {
				throw new IOException("Authentication failed.");
			}
			else {
				LOGGER.info("Authentication Successful");
			}
		} catch (IOException ex) {
			if (conn != null) {
				conn.close();
			}
			throw ex;
		}
	}

	@Override
	public void setup(String host, String user, String pass, int port) throws Exception {
		setup(host, user, pass, port, null);
	}

	public void setup(String host, String user, String pass, int port, String keyFile) throws Exception {
		LOGGER.finer("Authenticating with Key File...");
		LOGGER.finer("keyFile Variable = " + keyFile);
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.port = port;
		this.keyFile = keyFile;
		connect();
	}

	@Override
	public void teardown() {
		conn.close();
	}
}
