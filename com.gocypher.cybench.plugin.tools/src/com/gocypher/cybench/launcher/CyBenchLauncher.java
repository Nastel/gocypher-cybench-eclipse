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

package com.gocypher.cybench.launcher;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotRuntimeProfiler;
import org.openjdk.jmh.profile.HotspotThreadProfiler;
import org.openjdk.jmh.profile.SafepointsProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import com.gocypher.cybench.LauncherConfiguration;
import com.gocypher.cybench.core.utils.JSONUtils;
import com.gocypher.cybench.launcher.environment.model.HardwareProperties;
import com.gocypher.cybench.launcher.environment.model.JVMProperties;
import com.gocypher.cybench.launcher.environment.services.CollectSystemInformation;
import com.gocypher.cybench.launcher.model.BenchmarkOverviewReport;
import com.gocypher.cybench.launcher.report.DeliveryService;
import com.gocypher.cybench.launcher.report.ReportingService;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.launcher.utils.SecurityBuilder;


//import com.gocypher.cybench.launcher.utils.ComputationUtils;

public class CyBenchLauncher {
	public static Map<String,String> resultsMap = new HashMap<>() ;
	
	public static void main(String[] args) throws Exception{
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("                                 Starting CyBench benchmarks                             ");
		System.out.println("-----------------------------------------------------------------------------------------");
		LauncherConfiguration launcherConfiguration = new LauncherConfiguration () ;
		fillLaunchConfigurations(launcherConfiguration);
		
		if (args != null && args.length > 0) {
			System.out.println("Launcher program arguments:"+args[0]);
			launcherConfiguration.setPathToPlainReportFile(args[0]);
			if (args.length > 1) {
				launcherConfiguration.setPathToEncryptedReportFile(args[1]);
			}
		}
		//FIXME implement loading of custom benchmark meta data

    	System.out.println(System.getProperty("line.separator"));
    	HardwareProperties hwProperties = new HardwareProperties();
    	JVMProperties jvmProperties = new JVMProperties();
		if(launcherConfiguration.isIncludeHardware()) {
			System.out.println("Collecting hardware, software information...");
	        hwProperties = CollectSystemInformation.getEnvironmentProperties();
		}

        System.out.println("Collecting JVM properties...");
        jvmProperties = CollectSystemInformation.getJavaVirtualMachineProperties();
        SecurityBuilder securityBuilder = new SecurityBuilder();        
        Map<String, Object> benchmarkSettings = new HashMap<>();

        Map<String, Map<String, String>> customBenchmarksMetadata = CybenchUtils.parseCustomBenchmarkMetadata(launcherConfiguration.getUserBenchmarkMetadata());

        benchmarkSettings.put("benchThreadCount", launcherConfiguration.getThreads());
        benchmarkSettings.put("benchReportName", launcherConfiguration.getReportName());

        System.out.println("Executing benchmarks...");
        
		
		OptionsBuilder optBuild = new OptionsBuilder();

		if(launcherConfiguration.getClassCalled().size() > 0) {
			for(String classname : launcherConfiguration.getClassCalled()) {
				System.out.println("Classes sellected to run: "+ classname);
				optBuild.include(classname+"\\b");
			}
		}
		
		Options opt = optBuild
					.forks(launcherConfiguration.getForks())
					.measurementIterations(launcherConfiguration.getMeasurementIterations())
					.warmupIterations(launcherConfiguration.getWarmUpIterations())
					.warmupTime(TimeValue.seconds(launcherConfiguration.getWarmUpSeconds()))
					.threads(launcherConfiguration.getThreads())
					.measurementTime(TimeValue.seconds(launcherConfiguration.getMeasurmentSeconds()))
					.shouldDoGC(true)
					.detectJvmArgs()
					.addProfiler(GCProfiler.class)
                    .addProfiler(HotspotThreadProfiler.class)
                    .addProfiler(HotspotRuntimeProfiler.class)
                    .addProfiler(SafepointsProfiler.class)
					.build();
	
		Runner runner = new Runner(opt);
	
		Collection<RunResult> results = runner.run() ;
		
		BenchmarkOverviewReport report = ReportingService.getInstance().createBenchmarkReport(results, customBenchmarksMetadata);

		report.updateUploadStatus(launcherConfiguration.getReportUploadStatus());
		if(launcherConfiguration.isIncludeHardware()) {
	        report.getEnvironmentSettings().put("environment", hwProperties);
	        report.getEnvironmentSettings().put("jvmEnvironment", jvmProperties);
		}
        report.getEnvironmentSettings().put("unclassifiedProperties", CollectSystemInformation.getUnclassifiedProperties());
        report.getEnvironmentSettings().put("userDefinedProperties", customUserDefinedProperties(launcherConfiguration.getUserProperties()));
        report.setBenchmarkSettings(benchmarkSettings);

        //FIXME add all missing custom properties including public/private flag

        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("Report score - " + report.getTotalScore());
        System.out.println("-----------------------------------------------------------------------------------------");
		
        String reportEncrypted = ReportingService.getInstance().prepareReportForDelivery(securityBuilder, report);
        
        String responseWithUrl = null;
        
        if (report.isEligibleForStoringExternally() && launcherConfiguration.isShouldSendReportToCyBench()) {
            responseWithUrl = DeliveryService.getInstance().sendReportForStoring(reportEncrypted);
            report.setReportURL(responseWithUrl);
        }
        BigDecimal reportScore = report.getTotalScore();
        if(reportScore == null) {
        	reportScore = new BigDecimal(0);
        }
        String reportJSON = JSONUtils.marshalToPrettyJson(report);
        System.out.println(reportJSON);
        String pathToReportFile = launcherConfiguration.getPathToPlainReportFile();
        System.out.println("Store file at: "+pathToReportFile+reportScore+".cybench");
        CybenchUtils.storeResultsToFile(pathToReportFile+reportScore+".cybench", reportJSON);
        CybenchUtils.storeResultsToFile(pathToReportFile+reportScore+".cyb", reportEncrypted);
        
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("                                 Finished CyBench benchmarks                             ");
        System.out.println("-----------------------------------------------------------------------------------------");
	}
	
	private static Map<String, Object> customUserDefinedProperties(String customPropertiesStr) {
        Map<String, Object> customUserProperties = new HashMap<>();
        if (customPropertiesStr != null && !customPropertiesStr.isEmpty()){
            String [] pairs = customPropertiesStr.split(";") ;
            for (String pair:pairs){
                String [] kv = pair.split("=");
                if (kv.length == 2){
                    customUserProperties.put(kv[0],kv[1]) ;
                }
            }
        }
        return customUserProperties;
    }
	

	
	private static void fillLaunchConfigurations(LauncherConfiguration launcherConfiguration) {
		launcherConfiguration.setReportName(checkNullAndReturnString("REPORT_NAME"));
		launcherConfiguration.setReportUploadStatus(checkNullAndReturnString("BENCHMARK_REPORT_STATUS"));

		launcherConfiguration.setThreads(checkNullAndReturnInt("TREADS_COUNT"));
		launcherConfiguration.setForks(checkNullAndReturnInt("FORKS_COUNT"));
		launcherConfiguration.setWarmUpIterations(checkNullAndReturnInt("WARMUP_ITERATION"));
		launcherConfiguration.setMeasurementIterations(checkNullAndReturnInt("MEASURMENT_ITERATIONS"));
		launcherConfiguration.setWarmUpSeconds(checkNullAndReturnInt("WARMUP_SECONDS"));

		launcherConfiguration.setIncludeHardware(checkNullAndReturnBoolean("INCLUDE_HARDWARE_PROPERTIES"));
		launcherConfiguration.setShouldSendReportToCyBench(checkNullAndReturnBoolean("SHOULD_SEND_REPORT_CYBENCH"));
		launcherConfiguration.setUserProperties(checkNullAndReturnString("CUSTOM_USER_PROPERTIES"));
		   

		launcherConfiguration.setClassCalled(checkNullAndReturnSet("REPORT_CLASSES"));
		launcherConfiguration.setMeasurmentSeconds(checkNullAndReturnInt("MEASURMENT_SECONDS"));
		launcherConfiguration.setExecutionScore(checkNullAndReturnInt("DEXECUTION_SCORE"));
	}
	
	private static String checkNullAndReturnString(String propertyName)  {
		if(System.getProperty(propertyName)!= null) {
			return System.getProperty(propertyName);
		}
		return "";
	}
	
	private static int checkNullAndReturnInt(String propertyName)  {
		if(System.getProperty(propertyName)!= null) {
			return Integer.parseInt(System.getProperty(propertyName));
		}
		return 1;
	}
	
	private static boolean checkNullAndReturnBoolean(String propertyName)  {
		if(System.getProperty(propertyName)!= null) {
			return Boolean.parseBoolean(System.getProperty(propertyName));
		}
		return false;
	}
	
	private static Set<String> checkNullAndReturnSet(String propertyName)  {
		Set<String>  classesToInclude= new HashSet<String>();
		if(System.getProperty(propertyName)!= null) {
			classesToInclude.addAll(Arrays.asList(System.getProperty(propertyName).split(",")));
			return classesToInclude;
		}
		return classesToInclude;
	}
}
