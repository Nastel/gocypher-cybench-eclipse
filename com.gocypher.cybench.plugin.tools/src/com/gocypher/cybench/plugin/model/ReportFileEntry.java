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
		this.fullPathToFile = file.toString() ;
		this.reportIdentifier = GuiUtils.encodeBase64(this.fullPathToFile) ;
		
		this.extractMetaDataFromFileName(file.getName());
		
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
