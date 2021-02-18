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

public interface LaunchConfiguration {
	public static final String REPORT_NAME = "com.cybench.reportName";
	public static final String REPORT_FOLDER = "com.cybench.reportSaveFolder";
	public static final String BENCHMARK_REPORT_STATUS = "com.cybench.benchmarkReportStatus";
	

	public static final String FORKS_COUNT = "com.cybench.forkCount";
	public static final String TREADS_COUNT = "com.cybench.threadCount";
	public static final String MEASURMENT_ITERATIONS = "com.cybench.measurmentIterations";
	public static final String WARMUP_ITERATION = "com.cybench.warmupIterations";
	public static final String WARMUP_SECONDS = "com.cybench.warmupSeconds";
	public static final String MEASURMENT_SECONDS = "com.cybench.measurmentSeconds";

//	public static final String EXECUTION_SCORE = "com.cybench.executionScore";
	public static final String SHOULD_SEND_REPORT_CYBENCH = "com.cybench.shouldSendReportToCyBench";
	public static final String INCLUDE_HARDWARE_PROPERTIES = "com.cybench.includeHardwareProperties";
	public static final String SHOULD_SAVE_REPOT_TO_FILE = "com.cybench.ShouldSaveReportToFile";
//	public static final String CUSTOM_USER_PROPERTIES = "com.cybench.customUserProperties";
	public static final String CUSTOM_JVM_PROPERTIES = "com.cybench.customJVMProperties";
	

	public static final String LAUNCH_SELECTED_PATH = "com.cybench.launchPathSetFolderFileSelection";
	public static final String LAUNCH_PATH = "com.cybench.launchPathSet";
	public static final String BUILD_PATH = "com.cybench.buildhPathSet";
	
	public static final String USE_CYBNECH_BENCHMARK_SETTINGS = "com.cybench.useCyBenchBenchmarkSettings";
	
	public static final String ADD_CUSTOM_CLASS_PATH ="com.cybench.useCustomClassPath";
}
