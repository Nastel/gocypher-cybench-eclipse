package com.gocypher.cybench.plugin.model;

import java.io.File;
import java.io.Serializable;


import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.utils.GuiUtils;

public class ReportFileEntry implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1337514991330925494L;
	
	private String fullPathToFile ;
	private String name ;
	private long timestamp ;
	private String timeStampStr ;
	private String reportIdentifier ;
	private double score ;
	private String scoreStr ;
	
	public ReportFileEntry () {
		
	}

	public void create (File file) {
		//this.name = file.getName() ;
		this.fullPathToFile = file.toString() ;
		this.reportIdentifier = GuiUtils.encodeBase64(this.fullPathToFile) ;
		
		this.extractMetaDataFromFileName(file.getName());
		
		/*String timeInMilis = this.name.substring(this.name.lastIndexOf("-")+1,this.name.lastIndexOf(".")) ;
		if (timeInMilis != null && ! timeInMilis.isEmpty()) {
			this.timestamp = Long.parseLong(timeInMilis) ;
			this.timeStampStr = CybenchUtils.formatTimestamp (this.timestamp) ;
		}
		*/
		
	}
	private void extractMetaDataFromFileName (String fileName) {
		String meta = fileName.substring(0,fileName.lastIndexOf(".")) ;
		
		String[] arr = meta.split("-") ;
		if (arr != null && arr.length >=3) {		
			try {
				
				this.score = Double.parseDouble(arr[arr.length -1]) ;
				this.scoreStr = GuiUtils.convertNumToStringByLength(arr[arr.length -1]) ;
				
				this.timestamp = Long.parseLong(arr[arr.length -2]) ;
				this.timeStampStr = CybenchUtils.formatTimestamp (this.timestamp) ;
				this.name = "" ;
				for (int i = 0; i < arr.length -2;i++) {
					
					this.name += arr[i] ;
					
				}
				this.name = this.name.replaceAll("_"," ");
				
			}catch (Exception e) {
				this.timestamp = 0 ;
				this.timeStampStr = null ;
				this.score = 0 ;
				this.scoreStr = null ;
				this.name = fileName ;
			}
		
		}
		else {
			this.name = fileName ;
		}
	}
	public String getFullPathToFile() {
		return fullPathToFile;
	}

	public void setFullPathToFile(String fullPathToFile) {
		this.fullPathToFile = fullPathToFile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTimeStampStr() {
		return timeStampStr;
	}

	public void setTimeStampStr(String timeStampStr) {
		this.timeStampStr = timeStampStr;
	}

	@Override
	public String toString() {
		/*if (this.timeStampStr != null ) {
			return this.name+" ("+this.timeStampStr+")";
		}
		*/
		return this.name ;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getReportIdentifier() {
		return reportIdentifier;
	}

	public void setReportIdentifier(String reportIdentifier) {
		this.reportIdentifier = reportIdentifier;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getScoreStr() {
		return scoreStr;
	}

	public void setScoreStr(String scoreStr) {
		this.scoreStr = scoreStr;
	}
	
	
	
	
	
}
