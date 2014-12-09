USE [dynaTracePluginDB]
GO

/****** Object:  Table [dbo].[LogRecords]    Script Date: 12/18/2013 9:09:27 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

SET ANSI_PADDING ON
GO

CREATE TABLE [dbo].[LogRecords](
	[LogID] [numeric](18, 0) NOT NULL,
	[OS] [varchar](max) NULL,
	[Timestamp] [numeric](18, 0) NULL,
	[Directory] [varchar](max) NULL,
	[SearchTerm] [varchar](max) NULL,
	[Server] [varchar](max) NULL,
	[LogMessage] [varchar](max) NULL
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO

SET ANSI_PADDING OFF
GO


