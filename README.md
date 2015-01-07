## Log File Monitor

Previously called "Windows Log File Monitor" but updated to now support both Windows and Linux

The monitor searches a windows or Linux log file for text or regex expression and returns if a new line was found, and the last line number to contain the regex.

It stores the last result and position in the monitored file in a SQL database so that it knows where it ended the last time it ran so it's not reading the same lines over and over. If the log file
rolls over the monitor is smart enough to see that and starts from the beginning of the file again. The table in the database can be created by running the attached script or you can use the
screenshot from the Design view in the table to manually enter the values. The script will create the table on a database called dynaTracePluginDB. Change this name if you desire.

Find further information in the [dynaTrace community](https://community.compuwareapm.com/community/display/DL/Log+File+Monitor)


