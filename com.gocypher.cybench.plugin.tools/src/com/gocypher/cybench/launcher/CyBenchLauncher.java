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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.gocypher.cybench.core.utils.SecurityUtils;
import com.gocypher.cybench.launcher.model.BenchmarkReport;

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
import com.gocypher.cybench.core.annotation.BenchmarkMetaData;
import com.gocypher.cybench.core.annotation.CyBenchMetadataList;
import com.gocypher.cybench.core.utils.JMHUtils;
import com.gocypher.cybench.core.utils.JSONUtils;
import com.gocypher.cybench.launcher.environment.model.HardwareProperties;
import com.gocypher.cybench.launcher.environment.model.JVMProperties;
import com.gocypher.cybench.launcher.environment.services.CollectSystemInformation;
import com.gocypher.cybench.launcher.model.BenchmarkOverviewReport;
import com.gocypher.cybench.launcher.report.DeliveryService;
import com.gocypher.cybench.launcher.report.ReportingService;
import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.launcher.utils.SecurityBuilder;


//import com.gocypher.cybench.launcher.utils.ComputationUtils;

public class CyBenchLauncher {
	public static Map<String,String> resultsMap = new HashMap<>() ;
	private static final  String benchSource = "Eclipse plugin";
    static Properties cfg = new Properties();
    
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
		OptionsBuilder optBuild = new OptionsBuilder();    
        Map<String, Object> benchmarkSettings = new HashMap<>();


        System.out.println("_______________________ BENCHMARK TESTS FOUND _________________________________");
        
        benchmarkSettings.put("benchSource", benchSource);
        benchmarkSettings.put("benchWarmUpIteration", launcherConfiguration.getWarmUpIterations());
        benchmarkSettings.put("benchWarmUpSeconds", launcherConfiguration.getWarmUpSeconds());
        benchmarkSettings.put("benchMeasurementIteration", launcherConfiguration.getMeasurementIterations());
        benchmarkSettings.put("benchMeasurementSeconds", launcherConfiguration.getMeasurmentSeconds());
        benchmarkSettings.put("benchForkCount", launcherConfiguration.getForks());
        benchmarkSettings.put("benchThreadCount", launcherConfiguration.getThreads());
        benchmarkSettings.put("benchReportName", launcherConfiguration.getReportName());

        System.out.println("Executing benchmarks...");
        
		

		if(launcherConfiguration.getClassCalled().size() > 0) {
			for(String classname : launcherConfiguration.getClassCalled()) {
				System.out.println("Classes selected to run: "+ classname);
				optBuild.include(classname+"\\b");
			}
		}
		Options opt;
		if(launcherConfiguration.isUseCyBenchBenchmarkSettings()){
			opt = optBuild
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
		}else {
			opt = optBuild
					.shouldDoGC(true)
					.detectJvmArgs()
					.addProfiler(GCProfiler.class)
                    .addProfiler(HotspotThreadProfiler.class)
                    .addProfiler(HotspotRuntimeProfiler.class)
                    .addProfiler(SafepointsProfiler.class)
					.build();
		}
	
		Runner runner = new Runner(opt);

        Map<String, String> generatedFingerprints = new HashMap<>();
        Map<String, String> manualFingerprints = new HashMap<>();
        Map<String, String> classFingerprints = new HashMap<>();

        List<String> benchmarkNames = JMHUtils.getAllBenchmarkClasses();
        for (String benchmarkClass : benchmarkNames) {
            try {
                Class<?> classObj = Class.forName(benchmarkClass);
                SecurityUtils.generateMethodFingerprints(classObj, manualFingerprints, classFingerprints);
                SecurityUtils.computeClassHashForMethods(classObj, generatedFingerprints);
            } catch (ClassNotFoundException exc) {
            	System.out.println("Class not found in the classpath for execution");
            	exc.printStackTrace();
            }


        }
	
		Collection<RunResult> results = runner.run() ;

//      Map<String, Map<String, String>> customBenchmarksMetadata = CybenchUtils.parseCustomBenchmarkMetadata(launcherConfiguration.getUserBenchmarkMetadata());
		Map<String, Map<String, String>> customBenchmarksMetadata = new HashMap<String, Map<String, String>>();
		BenchmarkOverviewReport report = ReportingService.getInstance().createBenchmarkReport(results, customBenchmarksMetadata);

		report.updateUploadStatus(launcherConfiguration.getReportUploadStatus());
		if(launcherConfiguration.isIncludeHardware()) {
	        report.getEnvironmentSettings().put("environment", hwProperties);
	        report.getEnvironmentSettings().put("jvmEnvironment", jvmProperties);
		}
        report.getEnvironmentSettings().put("unclassifiedProperties", CollectSystemInformation.getUnclassifiedProperties());
        report.getEnvironmentSettings().put("userDefinedProperties", customUserDefinedProperties(launcherConfiguration.getUserProperties()));
        report.setBenchmarkSettings(benchmarkSettings);

        Iterator<String>it = report.getBenchmarks().keySet().iterator() ;

        while (it.hasNext()) {
            List<BenchmarkReport> custom = report.getBenchmarks().get(it.next()).stream().collect(Collectors.toList());
            custom.stream().forEach(benchmarkReport -> {
                String name = benchmarkReport.getName();
                benchmarkReport.setClassFingerprint(classFingerprints.get(name));
                benchmarkReport.setGeneratedFingerprint(generatedFingerprints.get(name));
                benchmarkReport.setManualFingerprint(manualFingerprints.get(name));
                try {
                	JMHUtils.ClassAndMethod classAndMethod = new JMHUtils.ClassAndMethod(name).invoke();
                    String clazz = classAndMethod.getClazz();
                    String method = classAndMethod.getMethod();
                    System.out.println("Adding metadata for benchamrk: " + clazz + " test: " + method);
                    Class<?> aClass = Class.forName(clazz);
                    Optional<Method> benchmarkMethod = JMHUtils.getBenchmarkMethod(method, aClass);
                    appendMetadataFromMethod(benchmarkMethod, benchmarkReport);
                    appendMetadataFromClass(aClass, benchmarkReport);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            });
        }
        
        List<BenchmarkReport> customBenchmarksCategoryCheck = report.getBenchmarks().get("CUSTOM");
        report.getBenchmarks().remove("CUSTOM");
        if(customBenchmarksCategoryCheck != null) {
	        for(BenchmarkReport benchReport : customBenchmarksCategoryCheck) {
	            report.addToBenchmarks(benchReport);
	        }
        }
        report.computeScores();
        getReportUploadStatus(report);
        //FIXME add all missing custom properties including public/private flag

        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("Report score - " + report.getTotalScore());
        System.out.println("-----------------------------------------------------------------------------------------");
		
        String reportEncrypted = ReportingService.getInstance().prepareReportForDelivery(securityBuilder, report);
        
        String responseWithUrl = null;
        String deviceReports = null;
        String resultURL = null;
        Map<?, ?> response = new HashMap<>();
        if (report.isEligibleForStoringExternally() && launcherConfiguration.isShouldSendReportToCyBench()) {
            responseWithUrl = DeliveryService.getInstance().sendReportForStoring(reportEncrypted, launcherConfiguration.getRemoteAccessToken());
            response = JSONUtils.parseJsonIntoMap(responseWithUrl);
            if(!response.containsKey("ERROR") && responseWithUrl != null && !responseWithUrl.isEmpty()) {
                deviceReports = response.get(Constants.REPORT_USER_URL).toString() ;
                resultURL = response.get(Constants.REPORT_URL).toString();
                report.setDeviceReportsURL(deviceReports);
                report.setReportURL(resultURL);
            }
  
        } else {
 			System.out.println("You may submit your report manually at "+Constants.CYB_UPLOAD_URL);
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
        
        if(!response.containsKey("ERROR") && responseWithUrl != null && !responseWithUrl.isEmpty()) {
            System.out.println("Benchmark report submitted successfully to "+ Constants.REPORT_URL);
            System.out.println("You can find all device benchmarks on "+ deviceReports);
            System.out.println("Your report is available at "+ resultURL);
            System.out.println("NOTE: It may take a few minutes for your report to appear online");
        }else{
        	System.out.println((String) response.get("ERROR"));
 			System.out.println("You may submit your report manually at "+Constants.CYB_UPLOAD_URL);
        }
        
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("                                 Finished CyBench benchmarks                             ");
        System.out.println("-----------------------------------------------------------------------------------------");
	}
    private static void getReportUploadStatus(BenchmarkOverviewReport report) {
        String reportUploadStatus = getProperty(Constants.REPORT_UPLOAD_STATUS);
        if (Constants.REPORT_PUBLIC.equals(reportUploadStatus)) {
            report.setUploadStatus(reportUploadStatus);
        } else if (Constants.REPORT_PRIVATE.equals(reportUploadStatus)) {
            report.setUploadStatus(reportUploadStatus);
        } else {
            report.setUploadStatus(Constants.REPORT_PUBLIC);
        }
    }
    public static String getProperty(String key) {
        return System.getProperty(key, cfg.getProperty(key));
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
	private static void appendMetadataFromClass(Class<?> aClass, BenchmarkReport benchmarkReport) {
        CyBenchMetadataList annotation = aClass.getDeclaredAnnotation(CyBenchMetadataList.class);
        if (annotation != null) {
            Arrays.stream(annotation.value()).forEach(annot -> {
                checkSetOldMetadataProps(annot.key(), annot.value(), benchmarkReport);
                benchmarkReport.addMetadata(annot.key(), annot.value());
//                System.out.println("added metadata " + annot.key() + "=" + annot.value());
            });
        }
        BenchmarkMetaData singleAnnotation = aClass.getDeclaredAnnotation(BenchmarkMetaData.class);
        if (singleAnnotation != null) {
            checkSetOldMetadataProps(singleAnnotation.key(), singleAnnotation.value(), benchmarkReport);
            benchmarkReport.addMetadata(singleAnnotation.key(), singleAnnotation.value());
//            System.out.println("added metadata " + singleAnnotation.key() + "=" + singleAnnotation.value());
        }
    }
	 private static void appendMetadataFromMethod(Optional<Method> benchmarkMethod, BenchmarkReport benchmarkReport) {
	        CyBenchMetadataList annotation = benchmarkMethod.get().getDeclaredAnnotation(CyBenchMetadataList.class);
	        if (annotation != null) {
	            Arrays.stream(annotation.value()).forEach(annot -> {
	                checkSetOldMetadataProps(annot.key(), annot.value(), benchmarkReport);
	                benchmarkReport.addMetadata(annot.key(), annot.value());
//	                System.out.println("added metadata " + annot.key() + "=" + annot.value());
	            });
	        }
	        BenchmarkMetaData singleAnnotation = benchmarkMethod.get().getDeclaredAnnotation(BenchmarkMetaData.class);
	        if (singleAnnotation != null) {
	            checkSetOldMetadataProps(singleAnnotation.key(), singleAnnotation.value(), benchmarkReport);
	            benchmarkReport.addMetadata(singleAnnotation.key(), singleAnnotation.value());
//	            System.out.println("added metadata " + singleAnnotation.key() + "=" + singleAnnotation.value());
	        }

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
		   

		launcherConfiguration.setUseCyBenchBenchmarkSettings(checkNullAndReturnBoolean("USE_CYBNECH_BENCHMARK_SETTINGS"));
		launcherConfiguration.setClassCalled(checkNullAndReturnSet("REPORT_CLASSES"));
		launcherConfiguration.setMeasurmentSeconds(checkNullAndReturnInt("MEASURMENT_SECONDS"));

		launcherConfiguration.setRemoteAccessToken(checkNullAndReturnString("REMOTE_CYBENCH_ACCESS_TOKEN"));
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
	
    private static void checkSetOldMetadataProps(String key,String value, BenchmarkReport benchmarkReport){
        if(key.equals("api")){
            benchmarkReport.setCategory(value);
        }
        if(key.equals("context")){
            benchmarkReport.setContext(value);
        }
        if(key.equals("version")){
            benchmarkReport.setVersion(value);
        }
    }
}
