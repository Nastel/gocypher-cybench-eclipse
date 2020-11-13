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
	
	private double executionScore = -1.0d;
	
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



	public double getExecutionScore() {
		return executionScore;
	}



	public void setExecutionScore(double executionScore) {
		this.executionScore = executionScore;
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
	

}
