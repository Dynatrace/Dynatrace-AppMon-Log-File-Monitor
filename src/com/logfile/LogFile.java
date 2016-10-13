package com.logfile;



import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;
import java.sql.*;


public class LogFile{

	private final Logger log = Logger.getLogger(WP.class.getName());
	
	// Garbage used to define the search terms and the file to check
		String server = null;
		String director = null;
		String thefile = null;
		String lookfor = null;
		String sqlfilename = null;
		String filename = null;
		String finalline = null;
		int lasttotal = 0;
		int linenumber = 1;
		int startline = 1;
		int newmessage = 0;
		int current;
		int UUID = 0;
		//long UUID = 0;
		public int nummessages = 0;
		boolean newFile = false;
		
		// Define the connection to the database
		String DatabaseType = null;
		String SQLServer = null;
		String Port = "1433";
		String SQLDriver = null;
		String Database = null;
		String Username = null;
		String Password = null;
		String connectionUrl = null;
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		boolean hasRows = false;
		Collection<String> collection = new ArrayList<String>();
	
	public LogFile(String server2, String directory, String file, String SearchTerm, String DBType, String sql, String database, String SPort, String suser, String spass)
	{
		newmessage = 0;
		hasRows = false;
		server = server2;
		director = "\\\\" + server + directory;
		thefile = file;
		lookfor = SearchTerm;
		sqlfilename = director + thefile;
		filename = director + thefile;
		DatabaseType = DBType;
		SQLServer = sql;
		Port = SPort;
		Database = database;
		Username = suser;
		Password = spass;
		
		//Create connection string based on DB Type, using if statement due to Java 1.6
		if (DatabaseType.equals("SQL Server")) {
			connectionUrl = "jdbc:sqlserver://" + SQLServer + ":" + Port + ";" + "databaseName=" + Database + ";username=" + Username + ";password=" + Password + ";";
			SQLDriver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		}
		else if (DatabaseType.equals("PostGreSQL")) {
			connectionUrl = "jdbc:postgresql://" + SQLServer + ":" + Port + "/" + Database + "?user=" + Username + "&password=" + Password;
			SQLDriver = "org.postgresql.Driver";
		}
		else if (DatabaseType.equals("Oracle")) {
			connectionUrl = "jdbc:oracle:thin:" + Username + "/" + Password + "@" + SQLServer + ":" + Port + ":" + Database;
			SQLDriver = "oracle.jdbc.driver.OracleDriver";
		}
	}
	
	public void getCurrentValues()
	{
		finalline = "";
		hasRows = false;
		String SQL = null;
		try {
			// Establish the connection to the DB
			Class.forName(SQLDriver);
			con = DriverManager.getConnection(connectionUrl);
		     
		    // Check to see if the current item has been searched for before
			if (DatabaseType.equals("Oracle")) {
				SQL = "select ALL LogFileMonitor.* from LogFileMonitor where LogFileMonitor.Server='" + server + "' and LogFileMonitor.Search_Term='" + lookfor +  "'";
				//SQL = "select LOGFILEMONITOR.LogID from System.LOGFILEMONITOR";
			} else {
				SQL = "select * from LogFileMonitor where Server='" + server + "' and Directory='" + sqlfilename + "' and Search_Term='" + lookfor +  "';";
			}
			try {
		     stmt = con.createStatement();
		     // log.info(SQL);
		     rs = stmt.executeQuery(SQL);
			}
			catch(SQLException s){
				StringWriter sw = new StringWriter();
				  PrintWriter pw = new PrintWriter(sw);
				  s.printStackTrace(pw);
				  log.warning(sw.toString());
			}

		     // If there is a previous search, iterate through the data in the result set and display it.
		     while (rs.next()) {
		    	 hasRows = true;
		    	 lasttotal =  rs.getInt("Line_Count");
		    	 linenumber = rs.getInt("Last_Line_Number");
		    	 startline = rs.getInt("Last_Line_Number");
		    	 //UUID = rs.getLong("LogID");
		    	 UUID = rs.getInt("LogID");
		    	 //String test = "Last Line Count:" + lasttotal + ", Last Line Number:" + startline;
		    	 //log.info("Log ID " + UUID + ": " + test);
		     }
		     
		     // If there is not a previous search, create a new record within the DB
		     if(!hasRows) {
		    	 if (DatabaseType.equals("Oracle")) {	 
		    		 SQL = "Insert into LogFileMonitor (LogID,Server,Line_Count,Last_Line_Number,Directory,Search_Term,Log_Message) VALUES (logFile_seq.NEXTVAL,'" + server + "',0,0,'" + sqlfilename + "','" + lookfor + "',' ')";
		    	 } else {
		    		 SQL = "Insert into LogFileMonitor (Server,Line_Count,Last_Line_Number,Directory,Search_Term,Log_Message) VALUES ('" + server + "',0,0,'" + sqlfilename + "','" + lookfor + "',' ');";
		    	 }
		    	 int up = stmt.executeUpdate(SQL);
		    	 linenumber = 0;
		    	 startline = 0;
		    	 
		    	 // Retrieve the automatically generated LogID
		    	 if (DatabaseType.equals("Oracle")) {
		    		 SQL = "select LogID from LogFileMonitor where LogFileMonitor.Server='" + server + "' and LogFileMonitor.Search_Term='" + lookfor +  "'";
		    		 //SQL = "select * from LogFileMonitor where Server = \"" + server + "\" and Directory = \"" + sqlfilename + "\" and Search_Term = \"" + lookfor +  "\"";
		    	 } else {
		    		 SQL = "select * from LogFileMonitor where Server = '" + server + "' and Directory = '" + sqlfilename + "' and Search_Term = '" + lookfor +  "';";
		    	 }
		    	 stmt = con.createStatement();
			     // log.info(SQL);
		    	 rs = stmt.executeQuery(SQL);
			     while (rs.next()) {
			    	 UUID = rs.getInt("LogID");
			    	 //UUID = rs.getLong("LogID");
			     }
			     newFile = true;
			     log.info("Log ID " + UUID + ": No Record for current search in database.  A new record was created for " + lookfor + " on " + sqlfilename);
		     }
		  }
		// Handle any errors that may have occurred.
		  catch (Exception e) {
			  StringWriter sw = new StringWriter();
			  PrintWriter pw = new PrintWriter(sw);
			  e.printStackTrace(pw);
			  log.warning(sw.toString());
		  }
		  finally {
		          if (rs != null) try { rs.close(); } catch(Exception e) {}
		          if (stmt != null) try { stmt.close(); } catch(Exception e) {}
		          if (con != null) try { con.close(); } catch(Exception e) {}
		  }
	}
	
	public int checkFile(String[] read, int additional, boolean skipper)
	{
		String line;
		current = read.length-1;
		linenumber = 0;
		newmessage = 0;
		//log.info("Log ID " + UUID + ": Last Total = " + lasttotal + ", Current = " + current);
		//Compares current file length to Line_Count entry in database to determine if it is a new file
		//if(current <= lasttotal && newFile)
		if(newFile)
		{
			lasttotal = -1;
			log.info("Log ID " + UUID + ": New File Found!");
		}
		for(int x=lasttotal+1; x<read.length; x++)
		{
			line = read[x];
			String recordline = "";
			// Check if each line of the file matches and is a new line
			if(line.matches(lookfor))
			{
				// Removed complicated finalline logic
				//if(newmessage == 1)
				//{
					//finalline += "<hr>";
				//}
				recordline = line + "\n\r";
				// Removed complicated finalline logic 
				//finalline += line.replace("'","") + "<br>";
				// Replaced finalline logic within line below
				finalline = line;
				newmessage = 1;
				nummessages++;
				int b = x + additional;
				if(additional != 0)
				{
					if(b >= read.length)
					{
						b = read.length-1;
					}
					for(int z=x+1; z<=b; z++)
					{
						// Removed complicated finalline logic
						//finalline += read[z].replace("'","") + "<br>\r";
						recordline += read[z] + "\n\r";
					}
				}
				if(skipper)
				{
					x = b;
				}
				//log.info("Final Line: " + finalline);
				collection.add(recordline.replace("'",""));
				linenumber = x+1;
			}
		}
		return linenumber;
	}
	
	public void updateData(boolean doHistory, String OS)
	{
		try {	
			// Establish the connection to the DB
			Class.forName(SQLDriver);
			con = DriverManager.getConnection(connectionUrl);
			int rows=0;
			
	    	// Update the DB to include the current line number and total line count
	    	stmt = con.createStatement();
	    	String monitorUpdate = null;
	    	if (DatabaseType.equals("Oracle")) {
	    		monitorUpdate = "update LogFileMonitor set Line_Count = " +  current + ",Last_Line_Number = " + linenumber + " where LogID = "+ UUID;
	    		//rows = stmt.executeUpdate( "update LogFileMonitor set Line_Count = " +  current + ",Last_Line_Number = " + linenumber + " where LogID = "+ UUID);
	    	} else {
	    		monitorUpdate = "update LogFileMonitor set Line_Count = " +  current + ",Last_Line_Number = " + linenumber + " where LogID = "+ UUID + ";";
	    		//rows = stmt.executeUpdate( "update LogFileMonitor set Line_Count = " +  current + ",Last_Line_Number = " + linenumber + " where LogID = "+ UUID + ";");
	    	}
	    	log.info("Log ID " + UUID + ": Updated summary data within the LogFileMonitor table");
	    	//log.info("Log ID " + UUID + ": " + monitorUpdate);
	    	rows = stmt.executeUpdate(monitorUpdate);
	    	//rows = stmt.executeUpdate( "update LogFileMonitor set Last_Line_Number = " + linenumber + " where server = '" + server + "' and directory = '" + sqlfilename + "' and search_term = '" + lookfor +  "';");
	    	
	    	// If a new message is found, update the Log Message field and record the Log Messages
	    	if(newmessage == 1)
	    	{
	    		// Trim the message if necessary
	    		String lastMessage = null;
	    		int finallineLength = finalline.length();
	    		if(finallineLength > 300) {
	    			int beginIndex = finallineLength - 299;
	    			lastMessage = finalline.substring(beginIndex, finallineLength);
	    		} else {
	    			lastMessage = finalline;
	    		}
	    		
	    		// Insert the message into the DB
	    		if (DatabaseType.equals("Oracle")) {
	    			monitorUpdate = "update LogFileMonitor set Log_Message = '" + lastMessage + "' where LogID = "+ UUID;
	    			//monitorUpdate = "update LogFileMonitor set Log_Message = '" + finalline + "' where LogID = "+ UUID;
	    		} else {
	    			monitorUpdate = "update LogFileMonitor set Log_Message = '" + lastMessage + "' where LogID = "+ UUID + ";";
	    			//monitorUpdate = "update LogFileMonitor set Log_Message = '" + finalline + "' where LogID = "+ UUID + ";";
	    		}
	    		// log.info(monitorUpdate);	
	    		rows = stmt.executeUpdate(monitorUpdate);
	    		log.info("Log ID " + UUID + ": Final Line Found = '" + finalline + "'" );
	    		
	    		// Record individual messages if "Keep Historical Record" is checked
	    		if(doHistory)
	    		{
	    			String epoch = Long.toString(System.currentTimeMillis()/1000);
	    			int RecordCount = 0;
	    			for (String s : collection)
	    			{
	    				RecordCount = RecordCount + 1;
	    				String HistorySQL = null;
	    				if (DatabaseType.equals("Oracle")) {
	    					HistorySQL = "Insert into LogRecords VALUES (" + UUID + ",'" + OS + "'," + epoch + ",'" + sqlfilename + "','" + lookfor + "','" + server + "','" + s + "')";
	    				}	else {
	    					HistorySQL = "Insert into LogRecords VALUES (" + UUID + ",'" + OS + "'," + epoch + ",'" + sqlfilename + "','" + lookfor + "','" + server + "','" + s + "');";	
	    				}
	    				// log.info(HistorySQL);
	    				int dofinish = stmt.executeUpdate(HistorySQL);
	    			}
	    			log.info("Log ID " + UUID + ": Inserted " + RecordCount + " historical records into LogRecords table");
	    		}
	    	}
	    }

	    // Handle any errors that may have occurred.
	    catch (Exception e) {
	    	StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			log.warning(sw.toString());
	    }
	    finally {
	    	if (stmt != null) try { stmt.close(); } catch(Exception e) {}
	    	if (con != null) try { con.close(); } catch(Exception e) {}
	    }
	}
	
	
	public int codeLookup(String Code)
	{
		int codeCount = 0;
		for (String s : collection)
		{
			if(s.contains(Code))
			{
				codeCount++;
			}
		}
		return codeCount;
	}
	
}
