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
	
	public ReportFileEntry () {
		
	}

	public void create (File file) {
		this.name = file.getName() ;
		this.fullPathToFile = file.toString() ;
		this.reportIdentifier = GuiUtils.encodeBase64(this.fullPathToFile) ;
		
		String timeInMilis = this.name.substring(this.name.lastIndexOf("-")+1,this.name.lastIndexOf(".")) ;
		if (timeInMilis != null && ! timeInMilis.isEmpty()) {
			this.timestamp = Long.parseLong(timeInMilis) ;
			this.timeStampStr = CybenchUtils.formatTimestamp (this.timestamp) ;
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
		if (this.timeStampStr != null ) {
			return this.name+" ("+this.timeStampStr+")";
		}
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
	
	
	
	
}
