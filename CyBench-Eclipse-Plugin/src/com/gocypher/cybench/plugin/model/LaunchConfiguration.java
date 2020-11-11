package com.gocypher.cybench.plugin.model;

public interface LaunchConfiguration {
	public static final String REPORT_NAME = "com.cybench.reportName";
	public static final String REPORT_FOLDER = "com.cybench.reportSaveFolder";
	public static final String BENCHMARK_REPORT_STATUS = "com.cybench.benchmarkReportStatus";
	

	public static final String FORKS_COUNT = "com.cybench.forkCount";
	public static final String TREADS_COUNT = "com.cybench.threadCount";
	public static final String MEASURMENT_ITERATIONS = "com.cybench.measurmentIterations";
	public static final String WARMUP_ITERATION = "com.cybench.warmupIterations";
	public static final String WARMUP_SECONDS = "com.cybench.warmupSeconds";

	public static final String EXECUTION_SCORE = "com.cybench.executionScore";
	public static final String SHOULD_SEND_REPORT_CYBENCH = "com.cybench.shouldSendReportToFile";
	public static final String SHOULD_SAVE_REPOT_TO_FILE = "com.cybench.ShouldSaveReportToFile";
	public static final String CUSTOM_USER_PROPERTIES = "com.cybench.customUserProperties";
	

	public static final String LAUNCH_PATH = "com.cybench.launchPathSet";
	public static final String BUILD_PATH = "com.cybench.launchPathSet";
	
}
