USE [dynaTracePluginDB]
GO

/****** Object:  Table [dbo].[LogFileMonitor]    Script Date: 12/18/2013 9:10:17 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [dbo].[LogFileMonitor](
	[LogID] [numeric](18, 0) IDENTITY(1,1) NOT NULL,
	[Server] [varchar](max) NULL,
	[Line_Count] [numeric](18, 0) NULL,
	[Last_Line_Number] [numeric](18, 0) NULL,
	[Directory] [varchar](max) NOT NULL,
	[Search_Term] [varchar](max) NULL,
	[Log_Message] [varchar](max) NULL,
 CONSTRAINT [PK_LogFileMonitor] PRIMARY KEY CLUSTERED 
(
	[LogID] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO


