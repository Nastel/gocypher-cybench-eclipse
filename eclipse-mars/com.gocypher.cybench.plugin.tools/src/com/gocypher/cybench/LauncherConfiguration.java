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

package com.gocypher.cybench;

import java.util.LinkedHashSet;
import java.util.Set;

public class LauncherConfiguration {
	
	private String reportName = "CyBench Report" ;
	
	private String pathToPlainReportFile ;
	private String pathToEncryptedReportFile ;
	
	private int forks = 1 ;
	private int measurementIterations = 1 ;
	private int warmUpIterations = 1 ;
	private int warmUpSeconds = 5 ;
	private int measurmentSeconds = 5 ;
	private int threads = 1 ;
	
	private String userBenchmarkMetadata = "";
	private String userProperties = "";
	private String reportUploadStatus = "public";
	
	private boolean shouldSendReportToCyBench = false;
	private boolean includeHardware = true;
	private Set<String> classCalled = new LinkedHashSet<String>();
	
	private boolean useCyBenchBenchmarkSettings = false;
	
	private String remoteAccessToken = "";
	private String emailAddress = "";
	
	public LauncherConfiguration() {
	
	}

	

	public int getForks() {
		return forks;
	}

	public void setForks(int forks) {
		this.forks = forks;
	}

	public int getMeasurementIterations() {
		return measurementIterations;
	}

	public void setMeasurementIterations(int measurementIterations) {
		this.measurementIterations = measurementIterations;
	}

	public int getWarmUpIterations() {
		return warmUpIterations;
	}

	public void setWarmUpIterations(int warmUpIterations) {
		this.warmUpIterations = warmUpIterations;
	}

	public int getWarmUpSeconds() {
		return warmUpSeconds;
	}

	public void setWarmUpSeconds(int warmUpSeconds) {
		this.warmUpSeconds = warmUpSeconds;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public String getUserBenchmarkMetadata() {
		return userBenchmarkMetadata;
	}

	public void setUserBenchmarkMetadata(String userBenchmarkMetadata) {
		this.userBenchmarkMetadata = userBenchmarkMetadata;
	}

	public String getUserProperties() {
		return userProperties;
	}

	public void setUserProperties(String userProperties) {
		this.userProperties = userProperties;
	}

	public String getReportUploadStatus() {
		return reportUploadStatus;
	}

	public void setReportUploadStatus(String reportUploadStatus) {
		this.reportUploadStatus = reportUploadStatus;
	}

	public boolean isShouldSendReportToCyBench() {
		return shouldSendReportToCyBench;
	}

	public void setShouldSendReportToCyBench(boolean shouldSendReportToCyBench) {
		this.shouldSendReportToCyBench = shouldSendReportToCyBench;
	}



	public String getPathToPlainReportFile() {
		return pathToPlainReportFile;
	}



	public void setPathToPlainReportFile(String pathToPlainReportFile) {
		this.pathToPlainReportFile = pathToPlainReportFile;
	}



	public String getPathToEncryptedReportFile() {
		return pathToEncryptedReportFile;
	}



	public void setPathToEncryptedReportFile(String pathToEncryptedReportFile) {
		this.pathToEncryptedReportFile = pathToEncryptedReportFile;
	}


	public int getMeasurmentSeconds() {
		return measurmentSeconds;
	}



	public void setMeasurmentSeconds(int measurmentSeconds) {
		this.measurmentSeconds = measurmentSeconds;
	}



	public boolean isIncludeHardware() {
		return includeHardware;
	}



	public void setIncludeHardware(boolean includeHardware) {
		this.includeHardware = includeHardware;
	}



	public Set<String> getClassCalled() {
		return classCalled;
	}



	public void setClassCalled(Set<String> classCalled) {
		this.classCalled = classCalled;
	}

	public boolean isUseCyBenchBenchmarkSettings() {
		return useCyBenchBenchmarkSettings;
	}



	public void setUseCyBenchBenchmarkSettings(boolean useCyBenchBenchmarkSettings) {
		this.useCyBenchBenchmarkSettings = useCyBenchBenchmarkSettings;
	}



	public String getRemoteAccessToken() {
		return remoteAccessToken;
	}



	public void setRemoteAccessToken(String remoteAccessToken) {
		this.remoteAccessToken = remoteAccessToken;
	}



	public String getEmailAddress() {
		return emailAddress;
	}



	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

}
