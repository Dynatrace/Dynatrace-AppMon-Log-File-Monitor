--Oracle Log File Monitor Tables

CREATE TABLE LogFileMonitor
(
	LogID NUMBER(10) NOT NULL,
	Server varchar (75) NULL,
	Line_Count numeric(18, 0) NULL,
	Last_Line_Number numeric(18, 0) NULL,
	Directory varchar(75) NOT NULL,
	Search_Term varchar(75) NULL,
	Log_Message varchar(500) NULL,
 CONSTRAINT PK_LogFileMonitor PRIMARY KEY (LogID)
 );
 
 CREATE SEQUENCE logFile_seq
 START WITH     1
 INCREMENT BY   1
 NOCACHE
 NOCYCLE;

 
 CREATE TABLE LogRecords(
	LogID numeric(18, 0) NOT NULL,
	OS varchar(75) NULL,
	Timestamp numeric(18, 0) NULL,
	Directory varchar(75) NULL,
	SearchTerm varchar(75) NULL,
	Server varchar(75) NULL,
	LogMessage varchar(500) NULL
);