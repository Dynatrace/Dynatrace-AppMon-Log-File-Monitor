package com.logfile;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class WinFileRegex
{
	public static File lastFileModified(String dirt, String reg) {
    	File dir = new File(dirt);
        File[] files = dir.listFiles();
        if(files != null)
        {
        	Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f2.lastModified()).compareTo(
                            f1.lastModified());
                }
            });
            for(int x=0; x<files.length; x++)
            {
            	String fin = files[x].getName();
            	if(fin.matches(reg))
            	{
            		return files[x];
            	}
            }
        }
        return null;
	}
}

