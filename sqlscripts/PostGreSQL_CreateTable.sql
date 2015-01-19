CREATE TABLE LogFileMonitor(
	LogID serial NOT NULL,
	Server varchar NULL,
	Line_Count numeric(18, 0) NULL,
	Last_Line_Number numeric(18, 0) NULL,
	Directory varchar NOT NULL,
	Search_Term varchar NULL,
	Log_Message varchar NULL,
 CONSTRAINT PK_LogFileMonitor PRIMARY KEY (LogID));
 
 CREATE TABLE LogRecords(
	LogID numeric(18, 0) NOT NULL,
	OS varchar NULL,
	Timestamp numeric(18, 0) NULL,
	Directory varchar NULL,
	SearchTerm varchar NULL,
	Server varchar NULL,
	LogMessage varchar NULL
);