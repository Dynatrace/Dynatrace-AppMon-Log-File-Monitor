package com.logfile;




import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.dynatrace.diagnostics.pdk.Monitor;
import com.dynatrace.diagnostics.pdk.MonitorEnvironment;
import com.dynatrace.diagnostics.pdk.MonitorMeasure;
import com.dynatrace.diagnostics.pdk.Status;

import java.util.List;
import java.util.Random;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.logging.Logger;

import com.logfile.SSHManager;
import com.logfile.LogFile;





public class WP implements Monitor {
	
	private final Logger log = Logger.getLogger(WP.class.getName());
	private static final String METRIC_GROUP = "Log File Scraper";
	private static final String MSR_LINE = "Line Number";
	private static final String MSR_MESSAGE = "New Message";
	private static final String MSR_MESSAGE_NUM = "Number of Messages";
	
	public Status setup(MonitorEnvironment env) throws Exception {
		// TODO
		return new Status(Status.StatusCode.Success);
	}
	
	@Override
	public Status execute(MonitorEnvironment env) throws Exception {
	     Status resultstat = new Status(Status.StatusCode.Success);
		 String connectionIP = env.getHost().getAddress();
	     String OS = env.getConfigString("OS");
	     String directory = env.getConfigString("Directory");
	     String file = env.getConfigString("File");
	     String realfile = directory + file;
	     String search = env.getConfigString("SearchTerm");
	     String dbType = env.getConfigString("dbType");
	     String SQLServer = env.getConfigString("SQLServer");
	     String Database = env.getConfigString("Database");
	     String Port = env.getConfigString("SQLPort");
	     String SUsername = env.getConfigString("Username");
	     String SPassword = env.getConfigPassword("Password");
	     double additionallines = env.getConfigDouble("AddLines");
	     boolean FRegex = env.getConfigBoolean("FileRegex");
	     boolean History = env.getConfigBoolean("Record");
	     boolean skiprec = env.getConfigBoolean("SkipRec");
	     int newmes = 0;
	     int line = 0;
	     String result = null;
	     String[] lines = null;
	     log.info("Connecting to " + realfile + " on " + connectionIP +"...");
	     if(OS.equals("Linux"))
	     {
	    	 String userName = env.getConfigString("LUser");
		     String password = env.getConfigPassword("LPass");
	    	 SSHManager instance = new SSHManager(userName, password, connectionIP, "");
		     String errorMessage = instance.connect();
		     if(errorMessage != null)
		     {
		        log.info(errorMessage);
		        Status error = new Status(Status.StatusCode.ErrorInfrastructureUnauthorized);
   			 	error.setMessage("The provided account was not allowed to log on to the server to retrieve the file.  Please verify that SSH traffic is allowed from the collector to the server and that the username/password provided are correct.");
   			 	return error;
		     }
		     if(FRegex)
		     {
		    	 String findcommand = "ls " + realfile + " -ltc | awk \'{print $9}\'";
		    	 String filereturn = instance.sendCommand(findcommand);
		    	 if(filereturn.equals(""))
	    		 {
	    			 Status error = new Status(Status.StatusCode.ErrorInfrastructureUnreachable);
	    			 error.setMessage("No file matching the Regex was found");
	    			 return error;
	    		 }
		    	 String[] filelist = filereturn.split("(\\n|\\r)");
		    	 realfile = filelist[0];
		     }
		     /*Random rand = new Random();
		     long epoch = System.currentTimeMillis() + rand.nextInt(10000000);
		     String storefile = "/home/" + userName + "/" + Long.toString(epoch) + "_" + file;*/
		     String command = "cat " + realfile;
		     result = instance.sendCommand(command);
		     log.info("File Read");
		     // close only after all commands are sent
		     instance.close();
		     lines = result.split("(\\n|\\r)");
	     }
	     else if(OS.equals("Windows"))
	     {
	    	 File thefile = null; 
	    	 if(FRegex)
	    	 {
	    		 thefile = WinFileRegex.lastFileModified(("\\\\" + connectionIP + directory), file);
	    		 if(thefile == null)
	    		 {
	    			 Status error = new Status(Status.StatusCode.ErrorInfrastructureUnreachable);
	    			 error.setMessage("No file matching the Regex was found");
	    			 return error;
	    		 }
	    	 }
	    	 else
	    	 {
	    		 thefile = new File("\\\\" + connectionIP + directory + file);
	    	 }
	    	 List<String> lineList = Files.readLines(thefile, Charsets.UTF_8);
	    	 lines = lineList.toArray(new String[lineList.size()]);;
	     }
	     
	     if(lines != null)
	     {
	    	 
	    	 LogFile linlog = new LogFile(connectionIP,directory,file,search,dbType,SQLServer,Database,Port,SUsername,SPassword);
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
	    	 return new Status(Status.StatusCode.ErrorInternalConfigurationProblem);
	     }
	}
	
	@Override
	public void teardown(MonitorEnvironment env) throws Exception {
		// TODO
	}
}
