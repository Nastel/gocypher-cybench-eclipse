/*
 * Copyright (C) 2020, K2N.IO.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package com.gocypher.cybench.launcher.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.gocypher.cybench.plugin.utils.Constants;
import com.gocypher.cybench.plugin.utils.GuiUtils;

public class CybenchUtils {
	
	private static SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss") ;
	
	
	
	public static String generatePlainReportFilename (String path,  boolean shouldIncludeSlash, String reportName) {
		return generateReportFileName (path,shouldIncludeSlash,"cybench", reportName) ;
	}
	public static String generateEncryptedReportFilename (String path, boolean shouldIncludeSlash, String reportName) {
		return generateReportFileName (path,shouldIncludeSlash,"cyb", reportName) ;
	}
	private static String generateReportFileName (String path, boolean shouldIncludeSlash, String extension, String reportName) {
		if(reportName==null || reportName.equals("")) {
			reportName = "CyBench Report";
		}
		String fileName = reportName+"-"+System.currentTimeMillis()+"-" ;
		if (shouldIncludeSlash) {
			return path+"/"+fileName ;
		}
		return path + fileName ;
	}
	
	public static String readReportFile (String fileName) {
		String result = "" ;
		
		return result ;
	}
	
	
	public static void storeResultsToFile(String pathToFile, String content) {
		FileWriter file = null;
		try {
	        System.out.println("pathToFile: "+pathToFile);
			File cFile = new File(pathToFile);
			File pFile = cFile.getParentFile();
			boolean exists = pFile.exists();
			if (!exists) {
				if (!pFile.mkdir()) {
					throw new IOException("Could not create folder=" + pFile);
				}
			}
			file = new FileWriter(pathToFile);
			file.write(content);
			file.flush();
		} catch (Exception e) {
			//FIXME handle logging in an eclipse way
			//LOG.error("Error on saving to file={}", fileName, e);
			e.printStackTrace();
		} finally {
			close(file);
		}
	}
	private static void close(Closeable obj) {
		try {
			if (obj != null) {
				obj.close();
			}
		} catch (Throwable e) {
			GuiUtils.logError("Error on close",e);
		}		
	}
	
	public static String loadFile (String pathToFile){
		
		String content = null ;
		try {
			content = new String ( Files.readAllBytes( Paths.get(pathToFile) ) );
		}catch (Exception e) {
			System.err.println("Error on reading file from:"+pathToFile+" ->"+e.getMessage());
			GuiUtils.logError("Error on reading file from:"+pathToFile+" ->"+e.getMessage(),e);
			
		}
		return content ;
	}
	
	public static List<File> listFilesInDirectory (String pathToDirectory) {
		try {
			List<File> filesInFolder = Files.walk(Paths.get(pathToDirectory))
	                .filter(Files::isRegularFile)
	                .map(Path::toFile)
	                .collect(Collectors.toList());
			return filesInFolder ;
		}catch (Exception e) {
			System.err.println ("Directory not opened:"+pathToDirectory+" Error:"+e.getMessage()) ;
			GuiUtils.logError("Directory not opened:"+pathToDirectory+" Error:"+e.getMessage(),e);
		}
		return new ArrayList<>() ;
	}
	public static String formatTimestamp (long timestamp) {
		return sdf.format(new Date (timestamp)) ;		
	}
	
	public static String findPathToFileByPrefix (String partialPathToFile) {
		//E:\benchmarks\runtime-EclipseApplication\demo-jmh-tests\reports/first_real_launch-1605086893862
		
		String filePath = null ;
		if (partialPathToFile != null) {
			String directory = partialPathToFile.substring(0, partialPathToFile.lastIndexOf("/")) ;
			String prefix = partialPathToFile.substring(partialPathToFile.lastIndexOf("/")+1) ;
			List<File> files = listFilesInDirectory(directory) ;
			for (File file:files) {
				if (file.getName().endsWith(Constants.REPORT_FILE_EXTENSION)) {
					if (file.getName().startsWith(prefix)) {
						filePath = file.getAbsolutePath() ;
					}
				}
			}
		}
		
		
		return filePath ;
	}
	public static String formatInterval(final long l) {
        final long hr = TimeUnit.MILLISECONDS.toHours(l);
        final long min = TimeUnit.MILLISECONDS.toMinutes(l - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(l - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }
	
	public static Map<String, Map<String, String>> parseCustomBenchmarkMetadata(String configuration) {
        Map<String, Map<String, String>> benchConfiguration = new HashMap<>();
        if (configuration != null && !configuration.isEmpty()) {
            for (String item : configuration.split(";")) {
                String[] testCfg = item.split("=");
                if (testCfg != null && testCfg.length == 2) {
                    String name = testCfg[0];
                    if (benchConfiguration.get(name) == null) {
                        benchConfiguration.put(name, new HashMap<>());
                    }
                    String value = testCfg[1];
                    for (String cfgItem : value.split(",")) {
                        String[] values = cfgItem.split(":");
                        if (values != null && values.length == 2) {
                            String key = values[0];
                            String val = values[1];
                            benchConfiguration.get(name).put(key, val);
                        }
                    }
                }
            }
        }
        return benchConfiguration;
    }
	   
}
