package com.logfile;




import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.dynatrace.diagnostics.pdk.Monitor;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.plugins.logfile.connection.ConnectionMethod;
import com.dynatrace.diagnostics.plugins.logfile.connection.SSHConnectionMethod;

import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.logfile.LogFile;





public class WP implements Monitor {
	
	private final Logger log = Logger.getLogger(WP.class.getName());
	private static final String METRIC_GROUP = "Log File Scraper";
	private static final String MSR_LINE = "Line Number";
	private static final String MSR_MESSAGE = "New Message";
	private static final String MSR_MESSAGE_NUM = "Number of Messages";
	private static final String CONFIG_METHOD = "method";
	private static final String CONFIG_AUTH_METHOD="authMethod";
	private ConnectionMethod connMethod;
	//ConnectionMethod connMethod;
	
	public Status setup(MonitorEnvironment env) throws Exception {
		return doSetup(env, null);
		//return new Status(Status.StatusCode.Success);
	}
	
	private Status doSetup(MonitorEnvironment env, ConnectionMethod connectionMethod) throws Exception {
		log.finer("Entering doSetup method...");
		if (env == null || env.getHost() == null) {
			Status stat = new Status();
			stat.setStatusCode(Status.StatusCode.ErrorInternalConfigurationProblem);
			stat.setShortMessage("Environment was not properly initialized. env.host must not be null.");
			stat.setMessage("Environment was not properly initialized. env.host must not be null.");
			Exception e = new IllegalArgumentException("Environment was not properly initialized. env.host must not be null.");
			stat.setException(e);
			if (log.isLoggable(Level.WARNING)){
				log.log(Level.WARNING, stat.getMessage(), e);
			}
			return stat;
		}

		//log.info("Connect Method = " + connectionMethod.toString());
		Status errorState = configureConnectionMethod(env, connectionMethod);
		if (errorState != null) {
			return errorState;
		}


		String unameoutput;
		try {
			unameoutput = connMethod.executeCommand("uname", "");
			log.fine("unameoutput: " + unameoutput);
			if ("".equals(unameoutput)) {
				throw new IOException("uname command did not produce any output.");
			}
		} catch (Exception e) {
			Status stat = new Status();
			stat.setStatusCode(Status.StatusCode.ErrorInfrastructure);
			stat.setShortMessage("uname command failed");
			stat.setException(e);

			if (log.isLoggable(Level.WARNING)){
				log.log(Level.WARNING, stat.getShortMessage(), e);
			}

			//connMethod.teardown();
			return stat;
		}
		log.finer("doSetup method connection ran");
		return new Status();
	}

	
	/** @return null on success, a Status with detailed error information on failure.
	 */
	private Status configureConnectionMethod(MonitorEnvironment env, ConnectionMethod preconfigured) throws Exception {
		log.finer("Entering configureConnectionMethod method...");
		if (preconfigured != null) {
			connMethod = preconfigured;
			return null;
		}
		
		String sshUsername = env.getConfigString("LUser");
	    String sshPassword = env.getConfigPassword("LPass");
	    String sshKey = env.getConfigString("Key");
	    String sshHost = env.getHost().getAddress();
	    int sshPort = 22;
	    
	    //BEGIN NEW CODE
	    String method = env.getConfigString(CONFIG_METHOD) == null ? "SSH" : env
	                .getConfigString(CONFIG_METHOD).toUpperCase();
	    log.fine("method variable = " + method);
	    String authMethod = env.getConfigString(CONFIG_AUTH_METHOD) == null ? "PASSWORD" : env
	                .getConfigString(CONFIG_AUTH_METHOD).toUpperCase();
	    log.fine("authMethod variable = " + authMethod);
	    log.fine("sshKey variable = " + sshKey);
	    
	    /*String port = (env.getConfigString(CONFIG_PORT) == null || env.getConfigString(CONFIG_PORT).isEmpty()) ? "22" : env.getConfigString(CONFIG_PORT);

		
		try{
			sshPort = Integer.parseInt(port);
		} catch(NumberFormatException ex){
            log.info("could not parse port: '" + port + "' falling back to default (22)");
		}*/
	    //SSHManager instance = new SSHManager(userName, password, connectionIP, "");
	     
	    connMethod = ConnectionMethod.getConnectionMethod(method);

		try {
			if (method.equalsIgnoreCase("SSH") && authMethod.equalsIgnoreCase("Public Key")){
				log.finer("Calling Public Key setup...");
				log.finer("sshKey variable = " + sshKey);
				((SSHConnectionMethod)connMethod).setup(sshHost, sshUsername, sshPassword, sshPort, sshKey);
			} else {
				connMethod.setup(sshHost, sshUsername, sshPassword, sshPort);
			}
		//END NEW CODE
		} catch (Exception e) {
			Status stat = new Status();
			stat.setStatusCode(Status.StatusCode.ErrorInfrastructure);
			stat.setShortMessage("Connecting failed");
			stat.setMessage("Connecting via " + method + " to " + sshHost + " failed");
			stat.setException(e);
	
			if (log.isLoggable(Level.WARNING)){
				log.log(Level.WARNING, stat.getMessage(), e);
			}
	
			return stat;
		}
		log.info("Connection setup successful");
	return null;
		
	}
	
	@Override
	public Status execute(MonitorEnvironment env) throws Exception {
	     Status resultstat = new Status(Status.StatusCode.Success);
	     String fileHost = env.getHost().getAddress();
	     String OS = env.getConfigString("OS");
	     String sshConnMethod = env.getConfigString("authMethod");
	     String directory = env.getConfigString("Directory");
	     String file = env.getConfigString("File");
	     String realfile = directory + file;
	     String search = env.getConfigString("SearchTerm");
	     String dbType = env.getConfigString("dbType");
	     String SQLServer = env.getConfigString("SQLServer");
	     String Database = env.getConfigString("Database");
	     String sqlPort = env.getConfigString("SQLPort");
	     String sqlUsername = env.getConfigString("Username");
	     String sqlPassword = env.getConfigPassword("Password");
	     double additionallines = env.getConfigDouble("AddLines");
	     boolean FRegex = env.getConfigBoolean("FileRegex");
	     boolean History = env.getConfigBoolean("Record");
	     boolean skiprec = env.getConfigBoolean("SkipRec");
	     Status stat = new Status();
	     
	     int newmes = 0;
	     int line = 0;
	     String result = null;
	     String[] lines = null;
	     log.info("Connecting to " + realfile + " on " + fileHost +"...");
	     if(OS.equals("Linux"))
	     {
	    	 //String sshUserName = env.getConfigString("LUser");
		     //String sshPassword = env.getConfigPassword("LPass");
		     
		     /*BEGIN NEW CODE
		     String method = env.getConfigString(CONFIG_METHOD) == null ? "SSH" : env
		                .getConfigString(CONFIG_METHOD).toUpperCase();
		     String authMethod = env.getConfigString(CONFIG_AUTH_METHOD) == null ? "PASSWORD" : env
		                .getConfigString(CONFIG_AUTH_METHOD).toUpperCase();
	    	 //SSHManager instance = new SSHManager(userName, password, connectionIP, "");
		     
		     connMethod = ConnectionMethod.getConnectionMethod(method);

				try {
					if (method.equalsIgnoreCase("SSH") && authMethod.equalsIgnoreCase("PUBLICKEY")){
						((SSHConnectionMethod)connMethod).setup(connectionIP, SUsername, SPassword, Port, SKey);
					} else {
						connMethod.setup(connectionIP, SUsername, SPassword, Port);
					}*/
		     //REMOVED CODE FOR TESTING
	    	 /*try {
					connMethod.reconnectIfNecessary();
				} catch (Exception e) {
					String msg = "Connection seems to be unavailable and reconnect failed.";
					log.log(Level.WARNING, msg, e);
					stat.setException(e);
					stat.setExceptionMessage(msg);
					stat.setStatusCode(Status.StatusCode.ErrorInfrastructureUnreachable);
					return stat;
				}*/
		     //END NEW CODE
		     
		     //OLD CODE 
		     /*String errorMessage = instance.connect();
		     if(errorMessage != null)
		     {
		        log.info(errorMessage);
		        stat.setStatusCode (Status.StatusCode.ErrorInfrastructureUnauthorized);
   			 	stat.setMessage("The provided account was not allowed to log on to the server to retrieve the file.  Please verify that SSH traffic is allowed from the collector to the server and that the username/password provided are correct.");
   			 	return stat;
		     }*/
		     if(FRegex)
		     {
		    	 String findcommand = "ls " + realfile + " -ltc | awk \'{print $9}\'";
		    	 //OLD CODE 
		    	 //String filereturn = instance.sendCommand(findcommand);
		    	 //NEW CODE
		    	 String filereturn = ConnectionMethod.getConnectionMethod(sshConnMethod).executeCommand(findcommand, "");
		    	 if(filereturn.equals(""))
	    		 {
	    			 stat.setStatusCode(Status.StatusCode.ErrorInfrastructureUnreachable);
	    			 stat.setMessage("No file matching the Regex was found");
	    			 return stat;
	    		 }
		    	 String[] filelist = filereturn.split("(\\n|\\r)");
		    	 realfile = filelist[0];
		     }
		     /*Random rand = new Random();
		     long epoch = System.currentTimeMillis() + rand.nextInt(10000000);
		     String storefile = "/home/" + userName + "/" + Long.toString(epoch) + "_" + file;*/
		     String command = "cat " + realfile;
		     //OLD CODE 
		     //result = instance.sendCommand(command);
		     //NEW CODE
		     log.fine("Connection Method: " + sshConnMethod);
		     log.fine("Read File Command: " + command);
		     log.finer("Trying to get ConnectionMethod...");
		     ConnectionMethod.getConnectionMethod(sshConnMethod);
		     log.finer("Trying to get the command result...");
		     //result = ConnectionMethod.getConnectionMethod(sshConnMethod).executeCommand(command, "");
		     result = connMethod.executeCommand(command, "");
		     log.info("File Read");
		     // close only after all commands are sent
		     //OLD CODE 
		     //instance.close();
		     lines = result.split("(\\n|\\r)");
	     }
	     else if(OS.equals("Windows"))
	     {
	    	 File thefile = null; 
	    	 if(FRegex)
	    	 {
	    		 thefile = WinFileRegex.lastFileModified(("\\\\" + fileHost + directory), file);
	    		 if(thefile == null)
	    		 {
	    			 stat.setStatusCode(Status.StatusCode.ErrorInfrastructureUnreachable);
	    			 stat.setMessage("No file matching the Regex was found");
	    			 return stat;
	    		 }
	    	 }
	    	 else
	    	 {
	    		 thefile = new File("\\\\" + fileHost + directory + file);
	    	 }
	    	 try {
	    		List<String> lineList = Files.readLines(thefile, Charsets.UTF_8);
	    	 	lines = lineList.toArray(new String[lineList.size()]);;
	    	 }
	    	 catch (FileNotFoundException e) {
				  StringWriter sw = new StringWriter();
				  PrintWriter pw = new PrintWriter(sw);
				  e.printStackTrace(pw);
				  log.warning(sw.toString());
				  stat.setStatusCode(Status.StatusCode.ErrorInfrastructureUnreachable);
	    		  stat.setMessage("No file found, please ensure that the file is shared and that access to the file is available from the user running the collector.");
	    		  return stat;
	    	 }
	    	 catch (Exception f) {
	    		  StringWriter sw = new StringWriter();
				  PrintWriter pw = new PrintWriter(sw);
				  f.printStackTrace(pw);
				  log.warning(sw.toString());
				  stat.setStatusCode(Status.StatusCode.ErrorInternalConfigurationProblem);
	    		  stat.setMessage("Plugin configuration issue, please ensure the correct settings are entered into the monitor configuration.");
	    		  return stat;
	    	 }
	     }
	     
	     if(lines != null)
	     {
	    	 
	    	 LogFile linlog = new LogFile(fileHost,directory,file,search,dbType,SQLServer,Database,sqlPort,sqlUsername,sqlPassword);
	    	 linlog.getCurrentValues();
	    	 line = linlog.checkFile(lines, (int) additionallines, skiprec);
	    	 lines = null;
	    	 if(line != 0)
	    	 {
	    		 log.info("Log ID " + linlog.UUID + ": New Message Found!");
	    		 newmes = 1;
	    		 resultstat.setMessage("New Message Found!");
	    	 }
	    	 else
	    	 {
	    		 log.info("Log ID " +linlog.UUID + ": No New Message");
	    		 newmes = 0;
	    		 resultstat.setMessage("No New Message Found");
	    	 }
	    	 linlog.updateData(History, OS);
	    	 Collection<MonitorMeasure> measures;
	 		if ((measures = env.getMonitorMeasures(METRIC_GROUP, MSR_LINE)) != null) {
	 			for (MonitorMeasure measure : measures)
	 				measure.setValue(line);
	 		}
	 		if ((measures = env.getMonitorMeasures(METRIC_GROUP, MSR_MESSAGE)) != null) {
	 			for (MonitorMeasure measure : measures)
	 				measure.setValue(newmes);
	 		}
	 		if ((measures = env.getMonitorMeasures(METRIC_GROUP, MSR_MESSAGE_NUM)) != null) {
	 			for (MonitorMeasure measure : measures)
	 			{
	 				String Name = measure.getParameter("Error Code");
	 				if(Name.equals("*"))
	 				{
	 					measure.setValue(linlog.nummessages);
	 				}
	 				else
	 				{
	 					measure.setValue(linlog.codeLookup(Name));
	 				}
	 			}
	 		}
	 		resultstat.setMessage(resultstat + "\nLogID: " + linlog.UUID);
	    	return resultstat;
	     }
	     else
	     {
	    	 log.info("An auth issue has occurred.");
	    	 stat.setStatusCode(Status.StatusCode.ErrorInternalConfigurationProblem);
	    	 stat.setMessage("An authorization issue has occurred.");
	    	 return stat;
	     }
	}
	
	@Override
	public void teardown(MonitorEnvironment env) throws Exception {
		// Complete
	}
}
