package com.gocypher.cybench.launcher;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openjdk.jmh.generators.annotations.APGeneratorDestinaton;
import org.openjdk.jmh.generators.annotations.APGeneratorSource;
import org.openjdk.jmh.generators.core.BenchmarkGenerator;
import org.openjdk.jmh.generators.core.GeneratorDestination;
import org.openjdk.jmh.generators.core.GeneratorSource;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotRuntimeProfiler;
import org.openjdk.jmh.profile.HotspotThreadProfiler;
import org.openjdk.jmh.profile.SafepointsProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.gocypher.cybench.LauncherConfiguration;
import com.gocypher.cybench.core.utils.JSONUtils;
import com.gocypher.cybench.launcher.environment.model.HardwareProperties;
import com.gocypher.cybench.launcher.environment.model.JVMProperties;
import com.gocypher.cybench.launcher.environment.services.CollectSystemInformation;
import com.gocypher.cybench.launcher.model.BenchmarkOverviewReport;
import com.gocypher.cybench.launcher.report.DeliveryService;
import com.gocypher.cybench.launcher.report.ReportingService;
import com.gocypher.cybench.launcher.utils.ComputationUtils;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.launcher.utils.SecurityBuilder;
import com.jcabi.manifests.Manifests;


//import com.gocypher.cybench.launcher.utils.ComputationUtils;

public class CyBenchLauncher {
	public static Map<String,String> resultsMap = new HashMap<>() ;
	
	public static void main(String[] args) throws Exception{
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("                                 Starting CyBench benchmarks                             ");
		System.out.println("-----------------------------------------------------------------------------------------");
//		System.out.println("Launcher classpath:"+System.getProperty("java.class.path"));
		long start = System.currentTimeMillis();
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
	        System.out.println("Collecting JVM properties...");
	        jvmProperties = CollectSystemInformation.getJavaVirtualMachineProperties();
		}
		
        SecurityBuilder securityBuilder = new SecurityBuilder();        
        Map<String, Object> benchmarkSettings = new HashMap<>();

        Map<String, Map<String, String>> customBenchmarksMetadata = ComputationUtils.parseCustomBenchmarkMetadata(launcherConfiguration.getUserBenchmarkMetadata());

        checkAndConfigureCustomProperties(securityBuilder, benchmarkSettings, customBenchmarksMetadata);

        benchmarkSettings.put("benchThreadCount", launcherConfiguration.getThreads());
        benchmarkSettings.put("benchReportName", launcherConfiguration.getReportName());

        System.out.println("Executing benchmarks...");
        
		
		OptionsBuilder optBuild = new OptionsBuilder();
		Options opt = optBuild
					.forks(launcherConfiguration.getForks())
					//.include(DemoBenchmarks.class.getName())
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
	private static void checkAndConfigureCustomProperties (SecurityBuilder securityBuilder
            ,Map<String,Object>benchmarkSettings
            ,Map<String,Map<String,String>>customBenchmarksMetadata){

		Reflections reflections = new Reflections("com.gocypher.cybench.", new SubTypesScanner(false));
		Set<Class<? extends Object>> allDefaultClasses = reflections.getSubTypesOf(Object.class);
		String tempBenchmark = null;
		for (Class<? extends Object> classObj : allDefaultClasses) {
			try {
			if (!classObj.getName().isEmpty() && classObj.getSimpleName().contains("Benchmarks")
						&& !classObj.getSimpleName().contains("_")) {
			// LOG.info("==>Default found:{}",classObj.getName());
			// We do not include any class, because then JMH will discover all benchmarks
			// automatically including custom ones.
			// optBuild.include(classObj.getName());
				tempBenchmark = classObj.getName();
				securityBuilder.generateSecurityHashForClasses(classObj);
			}
			}catch (Throwable t) {
				System.err.println ("Class not found:"+classObj) ;
			}
		}
		if (tempBenchmark != null) {
			String manifestData = null;
			if (Manifests.exists("customBenchmarkMetadata")) {
				manifestData = Manifests.read("customBenchmarkMetadata");
			}
			Map<String, Map<String, String>> benchmarksMetadata = ComputationUtils.parseCustomBenchmarkMetadata(manifestData);
			Map<String, String> benchProps;
			if (manifestData != null) {
				benchProps = ReportingService.getInstance().prepareBenchmarkSettings(tempBenchmark, benchmarksMetadata);
			} else {
				benchProps = ReportingService.getInstance().prepareBenchmarkSettings(tempBenchmark, customBenchmarksMetadata);
			}
			benchmarkSettings.putAll(benchProps);
		}

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
}
