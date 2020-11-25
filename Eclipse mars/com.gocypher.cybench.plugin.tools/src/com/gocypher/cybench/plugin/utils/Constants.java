package com.gocypher.cybench.plugin.utils;

public class Constants {
	public static final String CYB_REPORT_JSON_FILE = "report.json";
	public static final String CYB_REPORT_CYB_FILE =  "report.cyb";

	public static final String CYB_UPLOAD_URL = System.getProperty("cybench.manual.upload.url",	"https://www.gocypher.com/cybench/upload");

	public static final String SHOULD_SEND_REPORT = "sendReport";
	public static final String URL_LINK_TO_GOCYPHER_REPORT = "reportUrl";
	public static final String BENCHMARK_REPORT_NAME = "reportName";
	public static final String BENCHMARK_RUN_CLASSES = "benchmarkClasses";

//--------------------------- Properties that configure the runner execution ------------------------------------------
	public static final String NUMBER_OF_FORKS = "numberOfBenchmarkForks";
	public static final String MEASUREMENT_ITERATIONS = "measurementIterations";
	public static final String WARM_UP_ITERATIONS = "warmUpIterations";
	public static final String WARM_UP_SECONDS = "warmUpSeconds";
	public static final String BENCHMARK_RUN_THREAD_COUNT = "runThreadCount";

	public static final String REPORT_UPLOAD_STATUS = "reportUploadStatus";
	public static final String CUSTOM_BENCHMARK_METADATA = "customBenchmarkMetadata";
	
	public static final String REPORT_FILE_EXTENSION = ".cybench" ;


	
	
	
}
