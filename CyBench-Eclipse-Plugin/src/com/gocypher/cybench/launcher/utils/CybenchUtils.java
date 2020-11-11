package com.gocypher.cybench.launcher.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
			//FIXME handle logging in an eclipse way
			//LOG.error("Error on close: obj={}", obj,e);
			e.printStackTrace();
		}		
	}
	
	public static String loadFile (String pathToFile){
		
		String content = null ;
		try {
			content = new String ( Files.readAllBytes( Paths.get(pathToFile) ) );
		}catch (Exception e) {
			System.err.println("Error on reading file from:"+pathToFile+" ->"+e.getMessage());
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
	
	/*public static List<File> walkAndFindReports (List<String>pathsToDirectories) {
		List<File> reports = new ArrayList<>() ;
		for (String pathToDirectory:pathsToDirectories) {
			List<File>files = listFilesInDirectory(pathToDirectory) ;
			for (File file:files) {
				if (file.getName().endsWith(Constants.REPORT_FILE_EXTENSION)) {
					reports.add(file) ;
				}
			}
		}
		return reports;
	}
	*/
   
}
