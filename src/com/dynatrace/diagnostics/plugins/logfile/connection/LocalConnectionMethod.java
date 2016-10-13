package com.dynatrace.diagnostics.plugins.logfile.connection;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.logfile.WP;

public class LocalConnectionMethod extends ConnectionMethod {

	private static final Logger LOGGER = 
		      Logger.getLogger(WP.class.getName());
	private class StreamGobbler extends Thread {

		InputStream is;
		String output;

		StreamGobbler(InputStream is) {
			this.is = is;
		}

		@Override
		public void run() {
			try {
				output = readInputStream(is);
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Error reading outputstream from process: ", e);
			}
		}

		public String getOutput() {
			return output;
		}
	}

	@Override
	public Object openSession(String env) throws Exception {
		return null;
	}

	@Override
	public void closeSession(Object session) throws Exception {
		// nothing to do
	}

	@Override
	public void executePersistentCommand(String cmd, String env, LineCallback callback, Object session) throws Exception {
		Process child = Runtime.getRuntime().exec(
				cmd,
				(env.equals("")) ? new String[] { "LANG=C" }
						: new String[] { "LANG=C", env });
		readPersistentInputStream(child.getInputStream(), callback);
	}

	@Override
	public String executeStringCommand(String command, String env) throws Exception {
		Process child = Runtime.getRuntime().exec(
				command,
				(env.equals("")) ? new String[] { "LANG=C" }
						: new String[] { "LANG=C", env });
		// handling stderr and stdout in threads as both buffers can fill up
		// and will block the process so child.waitFor() will never return
		StreamGobbler outputGobbler = new StreamGobbler(child.getInputStream());
		StreamGobbler errorGobbler = new StreamGobbler(child.getErrorStream());
		outputGobbler.start();
		errorGobbler.start();

		int exitValue = child.waitFor();
		// important to handle scenarios where the process is finishing before the threads are finished
		errorGobbler.join();
		outputGobbler.join();

		String output = outputGobbler.getOutput();
		String error = errorGobbler.getOutput();
		if (output == null || output.equals("")) {
			throw new IOException("Command \"" + command + "\" did not produce any output. stderr was: " + error +
					";exitValue = " + exitValue);
		}
		return output;
	}
}
