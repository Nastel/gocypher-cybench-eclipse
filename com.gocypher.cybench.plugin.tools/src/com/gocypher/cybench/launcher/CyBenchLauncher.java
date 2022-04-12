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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.gocypher.cybench.core.utils.SecurityUtils;
import com.gocypher.cybench.launcher.model.BenchmarkReport;

import org.apache.commons.lang3.EnumUtils;
import org.codehaus.plexus.util.StringUtils;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.profile.HotspotRuntimeProfiler;
import org.openjdk.jmh.profile.HotspotThreadProfiler;
import org.openjdk.jmh.profile.SafepointsProfiler;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.gocypher.cybench.model.ComparisonConfig;
import com.gocypher.cybench.model.ComparisonConfig.Scope;
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
import com.gocypher.cybench.launcher.utils.ComputationUtils;
import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.launcher.utils.SecurityBuilder;

//import com.gocypher.cybench.launcher.utils.ComputationUtils;

public class CyBenchLauncher {
	public static Map<String,String> resultsMap = new HashMap<>() ;
	private static final  String benchSource = "Eclipse plugin (v0.3-beta)";
	private static Path userDir;
    static Properties cfg = new Properties();
    static ComparisonConfig automatedComparisonCfg;
    private static final Map<String, String> PROJECT_METADATA_MAP = new HashMap<>(5);
    
	public static void main(String[] args) throws Exception{
		System.out.println("-----------------------------------------------------------------------------------------");
		System.out.println("                                 Starting CyBench benchmarks                             ");
		System.out.println("-----------------------------------------------------------------------------------------");
		LauncherConfiguration launcherConfiguration = new LauncherConfiguration () ;
		fillLaunchConfigurations(launcherConfiguration);
		automatedComparisonCfg = checkConfigValidity(launcherConfiguration);
				
		if (args != null && args.length > 0) {
			System.out.println("Launcher program arguments:"+args[0]);
			launcherConfiguration.setPathToPlainReportFile(args[0]);
			if (args.length > 1) {
				launcherConfiguration.setPathToEncryptedReportFile(args[1]);
			}
		}
		
		userDir = Paths.get(launcherConfiguration.getPathToPlainReportFile()).getParent().getParent();
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
        
        

		try {
            checkProjectMetadataExists();
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


            if (automatedComparisonCfg != null) {
                if (automatedComparisonCfg.getScope().equals(Scope.WITHIN)) {
                    automatedComparisonCfg.setCompareVersion(PROJECT_METADATA_MAP.get(Constants.PROJECT_VERSION));
                }
                automatedComparisonCfg.setRange(String.valueOf(automatedComparisonCfg.getCompareLatestReports()));
                automatedComparisonCfg.setProjectName(PROJECT_METADATA_MAP.get(Constants.PROJECT_NAME));
                automatedComparisonCfg.setProjectVersion(PROJECT_METADATA_MAP.get(Constants.PROJECT_VERSION));
                report.setAutomatedComparisonConfig(automatedComparisonCfg);
            }
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
                        System.out.println("Adding metadata for benchmark: " + clazz + " test: " + method);
                        Class<?> aClass = Class.forName(clazz);
                        Optional<Method> benchmarkMethod = JMHUtils.getBenchmarkMethod(method, aClass);
                        appendMetadataFromMethod(benchmarkMethod, benchmarkReport);
                        appendMetadataFromClass(aClass, benchmarkReport);
                        syncReportsMetadata(report, benchmarkReport);
                        System.out.println(report);

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
    //        getReportUploadStatus(report);
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
                String tokenAndEmail = ComputationUtils.getRequestHeader(launcherConfiguration.getRemoteAccessToken(), launcherConfiguration.getEmailAddress());
                String queryToken = launcherConfiguration.getRemoteQueryToken();
                responseWithUrl = DeliveryService.getInstance().sendReportForStoring(reportEncrypted, tokenAndEmail, queryToken);

                if (StringUtils.isNotEmpty(responseWithUrl)) {
                    response = JSONUtils.parseJsonIntoMap(responseWithUrl);
                }
                if (!response.isEmpty() && !isErrorResponse(response)) {
                    deviceReports = String.valueOf(response.get(Constants.REPORT_USER_URL));
                    resultURL = String.valueOf(response.get(Constants.REPORT_URL));
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

            if (!response.isEmpty() && !isErrorResponse(response)) {
                System.out.println("Benchmark report submitted successfully to "+ Constants.REPORT_URL);
                System.out.println("You can find all device benchmarks on "+ deviceReports);
                System.out.println("Your report is available at "+ resultURL);
                System.out.println("NOTE: It may take a few minutes for your report to appear online");
                
                if (response.containsKey("automatedComparisons")) {
                    List<Map<String, Object>> automatedComparisons = (List<Map<String, Object>>) response
                            .get("automatedComparisons");
                    if (tooManyAnomalies(automatedComparisons)) {
                        System.exit(1);
                    }
                }               
            } else {
                String errMsg = getErrorResponseMessage(response);
                if (errMsg != null) {
                    System.out.println("CyBench backend service sent error response: "+ errMsg);
                }
                System.out.println("You may submit your report manually at "+Constants.CYB_UPLOAD_URL);
            }
        } catch (MissingResourceException exc) {
        }
        
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("                                 Finished CyBench benchmarks                             ");
        System.out.println("-----------------------------------------------------------------------------------------");
	}

    public static boolean isErrorResponse(Map<?, ?> response) {
        return response != null && (response.containsKey("error") || response.containsKey("ERROR"));
    }

    public static String getErrorResponseMessage(Map<?, ?> response) {
        if (response == null) {
            return null;
        }
        String errMsg = (String) response.get("error");
        if (errMsg == null) {
            errMsg = (String) response.get("ERROR");
        }
        return errMsg;
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
                System.out.println("added metadata " + annot.key() + "=" + annot.value());
            });
        }
        BenchmarkMetaData singleAnnotation = aClass.getDeclaredAnnotation(BenchmarkMetaData.class);
        if (singleAnnotation != null) {
            checkSetOldMetadataProps(singleAnnotation.key(), singleAnnotation.value(), benchmarkReport);
            benchmarkReport.addMetadata(singleAnnotation.key(), singleAnnotation.value());
            System.out.println("added metadata " + singleAnnotation.key() + "=" + singleAnnotation.value());
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
	 public static ComparisonConfig checkConfigValidity(LauncherConfiguration launcherConfiguration) throws Exception {
	        ComparisonConfig verifiedComparisonConfig = new ComparisonConfig();

	        String SCOPE_STR = launcherConfiguration.getScope();
	        if (StringUtils.isBlank(SCOPE_STR)) {
	            throw new Exception("Scope is not specified!");
	        } else {
	            SCOPE_STR = SCOPE_STR.toUpperCase();
	        }
	        ComparisonConfig.Scope SCOPE;
	        String COMPARE_VERSION = launcherConfiguration.getCompareVersion();
	        Integer NUM_LATEST_REPORTS = launcherConfiguration.getLatestReports();
	        Integer ANOMALIES_ALLOWED = launcherConfiguration.getAnomaliesAllowed();
	        String METHOD_STR = launcherConfiguration.getMethod();
	        if (StringUtils.isBlank(METHOD_STR)) {
	            throw new Exception("Method is not specified!");
	        } else {
	            METHOD_STR = METHOD_STR.toUpperCase();
	        }
	        ComparisonConfig.Method METHOD;
	        String THRESHOLD_STR = launcherConfiguration.getThreshold();
	        if (StringUtils.isNotBlank(THRESHOLD_STR)) {
	            THRESHOLD_STR = THRESHOLD_STR.toUpperCase();
	        }
	        ComparisonConfig.Threshold THRESHOLD;
	        Double PERCENT_CHANGE_ALLOWED = (double) launcherConfiguration.getPercentChange();
	        Double DEVIATIONS_ALLOWED = (double) launcherConfiguration.getDeviationsAllowed();

	        if (NUM_LATEST_REPORTS != null) {
	            if (NUM_LATEST_REPORTS < 1) {
	                throw new Exception("Not enough latest reports specified to compare to!");
	            }
	            verifiedComparisonConfig.setCompareLatestReports(NUM_LATEST_REPORTS);
	        } else {
	            throw new Exception("Number of latest reports to compare to was not specified!");
	        }
	        if (ANOMALIES_ALLOWED != null) {
	            if (ANOMALIES_ALLOWED < 1) {
	                throw new Exception("Not enough anomalies allowed specified!");
	            }
	            verifiedComparisonConfig.setAnomaliesAllowed(ANOMALIES_ALLOWED);
	        } else {
	            throw new Exception("Anomalies allowed was not specified!");
	        }

	        if (!EnumUtils.isValidEnum(ComparisonConfig.Scope.class, SCOPE_STR)) {
	            throw new Exception("Scope is invalid!");
	        } else {
	            SCOPE = ComparisonConfig.Scope.valueOf(SCOPE_STR);
	            verifiedComparisonConfig.setScope(SCOPE);
	        }
	        if (!EnumUtils.isValidEnum(ComparisonConfig.Method.class, METHOD_STR)) {
	            throw new Exception("Method is invalid!");
	        } else {
	            METHOD = ComparisonConfig.Method.valueOf(METHOD_STR);
	            verifiedComparisonConfig.setMethod(METHOD);
	        }

	        if (SCOPE.equals(ComparisonConfig.Scope.WITHIN) && StringUtils.isNotEmpty(COMPARE_VERSION)) {
	            COMPARE_VERSION = "";
	            System.out.println("Automated comparison config scoped specified as WITHIN"
	            		+ " but compare version was also specified, will compare WITHIN the currently tested version.");
	        } else if (SCOPE.equals(ComparisonConfig.Scope.BETWEEN) && StringUtils.isBlank(COMPARE_VERSION)) {
	            throw new Exception("Scope specified as BETWEEN but no compare version specified!");
	        } else if (SCOPE.equals(ComparisonConfig.Scope.BETWEEN)) {
	            verifiedComparisonConfig.setCompareVersion(COMPARE_VERSION);
	        }

	        if (METHOD.equals(ComparisonConfig.Method.SD)) {
	            if (DEVIATIONS_ALLOWED != null) {
	                if (DEVIATIONS_ALLOWED <= 0) {
	                    throw new Exception("Method specified as SD but not enough deviations allowed were specified!");
	                }
	                verifiedComparisonConfig.setDeviationsAllowed((DEVIATIONS_ALLOWED/Math.pow(10, 2.0)));
	            } else {
	                throw new Exception("Method specified as SD but deviations allowed was not specified!");
	            }
	        } else if (METHOD.equals(ComparisonConfig.Method.DELTA)) {
	            if (!EnumUtils.isValidEnum(ComparisonConfig.Threshold.class, THRESHOLD_STR) || StringUtils.isBlank(THRESHOLD_STR)) {
	                throw new Exception(
	                        "Method specified as DELTA but no threshold specified or threshold is invalid!");
	            } else {
	                THRESHOLD = ComparisonConfig.Threshold.valueOf(THRESHOLD_STR);
	                verifiedComparisonConfig.setThreshold(THRESHOLD);
	            }

	            if (THRESHOLD.equals(ComparisonConfig.Threshold.PERCENT_CHANGE)) {
	                if (PERCENT_CHANGE_ALLOWED != null) {
	                    if (PERCENT_CHANGE_ALLOWED <= 0) {
	                        throw new Exception(
	                                "Threshold specified as PERCENT_CHANGE but percent change is not high enough!");
	                    }
	                    verifiedComparisonConfig.setPercentChangeAllowed((PERCENT_CHANGE_ALLOWED/Math.pow(10, 2.0)));
	                } else {
	                    throw new Exception(
	                            "Threshold specified as PERCENT_CHANGE but percent change allowed was not specified!");
	                }
	            }
	        }

	        return verifiedComparisonConfig;
	    }
	
	
	private static void fillLaunchConfigurations(LauncherConfiguration launcherConfiguration) {
		
		launcherConfiguration.setReportName(checkNullAndReturnString(Constants.BENCHMARK_REPORT_NAME));
//		launcherConfiguration.setReportUploadStatus(checkNullAndReturnString(Constants.REPORT_UPLOAD_STATUS));

		launcherConfiguration.setThreads(checkNullAndReturnInt(Constants.RUN_THREAD_COUNT));
		launcherConfiguration.setForks(checkNullAndReturnInt(Constants.NUMBER_OF_FORKS));
		launcherConfiguration.setWarmUpIterations(checkNullAndReturnInt(Constants.WARM_UP_ITERATIONS));
		launcherConfiguration.setMeasurementIterations(checkNullAndReturnInt(Constants.MEASUREMENT_ITERATIONS));
		launcherConfiguration.setWarmUpSeconds(checkNullAndReturnInt(Constants.WARM_UP_SECONDS));
		launcherConfiguration.setMeasurmentSeconds(checkNullAndReturnInt(Constants.MEASUREMENT_SECONDS));

		launcherConfiguration.setIncludeHardware(checkNullAndReturnBoolean(Constants.COLLECT_HARDWARE_PROPS));
		launcherConfiguration.setShouldSendReportToCyBench(checkNullAndReturnBoolean(Constants.SEND_REPORT));
//		launcherConfiguration.setUserProperties(checkNullAndReturnString("CUSTOM_USER_PROPERTIES"));
		   

		launcherConfiguration.setUseCyBenchBenchmarkSettings(checkNullAndReturnBoolean(Constants.USE_CYBENCH_CONFIGURATION));
		launcherConfiguration.setClassCalled(checkNullAndReturnSet(Constants.SELECTED_CLASS_PATHS));

		launcherConfiguration.setRemoteAccessToken(checkNullAndReturnString(Constants.USER_REPORT_TOKEN));
        launcherConfiguration.setRemoteQueryToken(checkNullAndReturnString(Constants.USER_QUERY_TOKEN));
		launcherConfiguration.setEmailAddress(checkNullAndReturnString(Constants.USER_EMAIL_ADDRESS));
		
    	if(launcherConfiguration.getRemoteAccessToken() != null && !launcherConfiguration.getRemoteAccessToken().equals("")){
    		launcherConfiguration.setReportUploadStatus("private");
		}else{
			launcherConfiguration.setReportUploadStatus("public");
		}
    	 // grab user auto comparison configs
		 launcherConfiguration.setAnomaliesAllowed(checkNullAndReturnInt(Constants.AUTO_ANOMALIES_ALLOWED));
		 launcherConfiguration.setMethod(checkNullAndReturnString(Constants.AUTO_METHOD));
		 launcherConfiguration.setLatestReports(checkNullAndReturnInt(Constants.AUTO_LATEST_REPORTS));
		 launcherConfiguration.setPercentChange(checkNullAndReturnDouble(Constants.AUTO_PERCENT_CHANGE));
		 launcherConfiguration.setThreshold(checkNullAndReturnString(Constants.AUTO_THRESHOLD));
		 launcherConfiguration.setScope(checkNullAndReturnString(Constants.AUTO_SCOPE));
		 launcherConfiguration.setDeviationsAllowed(checkNullAndReturnDouble(Constants.AUTO_DEVIATIONS_ALLOWED));
		 launcherConfiguration.setCompareVersion(checkNullAndReturnString(Constants.AUTO_COMPARE_VERSION));
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
	
	private static double checkNullAndReturnDouble(String propertyName)  {
		if(System.getProperty(propertyName)!= null) {
			return Double.parseDouble(System.getProperty(propertyName));
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
	
    @SuppressWarnings("unchecked")
    public static boolean tooManyAnomalies(List<Map<String, Object>> automatedComparisons) {
        for (Map<String, Object> automatedComparison : automatedComparisons) {
            Integer totalFailedBenchmarks = (Integer) automatedComparison.get("totalFailedBenchmarks");
            Map<String, Object> config = (Map<String, Object>) automatedComparison.get("config");
            if (config.containsKey("anomaliesAllowed")) {
                Integer anomaliesAllowed = (Integer) config.get("anomaliesAllowed");
                if (totalFailedBenchmarks != null && totalFailedBenchmarks > anomaliesAllowed) {
                    System.out.println(
                            "There were more anomaly benchmarks than your specified anomalies allowed in one of your automated comparison configurations!");
                    return true;
                }
            }
        }
        return false;
    }
	
    /**
     * Synchronizes overview and benchmark reports metadata.
     * 
     * @param report
     *            overview report object
     * @param benchmarkReport
     *            report data object
     */
    public static void syncReportsMetadata(BenchmarkOverviewReport report, BenchmarkReport benchmarkReport) {
        try {
            String projectVersion = PROJECT_METADATA_MAP.get("version");
            String projectArtifactId = PROJECT_METADATA_MAP.get("artifactId");

            if (StringUtils.isNotEmpty(benchmarkReport.getProject())) {
                report.setProject(benchmarkReport.getProject());
            } else {
                System.out.println("* Project name metadata not defined, grabbing it from build files...");
                report.setProject(projectArtifactId);
                benchmarkReport.setProject(projectArtifactId);
            }

            if (StringUtils.isNotEmpty(benchmarkReport.getProjectVersion())) {
                report.setProjectVersion(benchmarkReport.getProjectVersion());
            } else {
                System.out.println("* Project version metadata not defined, grabbing it from build files...");
                report.setProjectVersion(projectVersion); // default
                benchmarkReport.setProjectVersion(projectVersion);
            }

            if (StringUtils.isEmpty(benchmarkReport.getVersion())) {
                benchmarkReport.setVersion(projectVersion);
            }

            if (StringUtils.isEmpty(report.getBenchmarkSessionId())) {
                Map<String, String> bMetadata = benchmarkReport.getMetadata();
                if (bMetadata != null) {
                    String sessionId = bMetadata.get("benchSession");
                    if (StringUtils.isNotEmpty(sessionId)) {
                        report.setBenchmarkSessionId(sessionId);
                    }
                }
            }

            if (benchmarkReport.getCategory().equals("CUSTOM")) {
                int classIndex = benchmarkReport.getName().lastIndexOf(".");
                if (classIndex > 0) {
                    String pckgAndClass = benchmarkReport.getName().substring(0, classIndex);
                    int pckgIndex = pckgAndClass.lastIndexOf(".");
                    if (pckgIndex > 0) {
                        String pckg = pckgAndClass.substring(0, pckgIndex);
                        benchmarkReport.setCategory(pckg);
                    } else {
                        benchmarkReport.setCategory(pckgAndClass);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error while attempting to synchronize benchmark metadata from runner: ");
            e.printStackTrace();
        }
    }

    /**
     * A method needed in order to support the previous data model. Setting the needed values from annotation to a
     * previously defined data model value
     * 
     * @param key
     *            property key
     * @param value
     *            value to set for the key found
     * @param benchmarkReport
     *            report data object
     */
    public static void checkSetOldMetadataProps(String key, String value, BenchmarkReport benchmarkReport) {
        if ("api".equals(key)) {
            benchmarkReport.setCategory(value);
        }
        if ("context".equals(key)) {
            benchmarkReport.setContext(value);
        }
        if ("version".equals(key)) {
            benchmarkReport.setVersion(value);
        }
        if ("project".equals(key)) {
            benchmarkReport.setProject(value);
        }
        if ("projectVersion".equals(key)) {
            benchmarkReport.setProjectVersion(value);
        }
    }

    public static void checkProjectMetadataExists() throws Exception {
        try {
            PROJECT_METADATA_MAP.put("artifactId", getMetadataFromBuildFile("artifactId"));
            PROJECT_METADATA_MAP.put("version", getMetadataFromBuildFile("version"));
            // make sure gradle metadata can be parsed BEFORE benchmarks are run
            String metaProp = PROJECT_METADATA_MAP.get("artifactId");
            if (StringUtils.isEmpty(metaProp)) {
                failBuildFromMissingMetadata("Project");
            } else {
                System.out.println("MetaData - Project name:    "+ metaProp);
            }
            metaProp = PROJECT_METADATA_MAP.get("version");
            if (StringUtils.isEmpty(metaProp)) {
                failBuildFromMissingMetadata("Version");
            } else {
                System.out.println("MetaData - Project version: "+ metaProp);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Resolved metadata property value from set of project configuration files: pom.xml, build.gradle, etc.
     * 
     * @param prop
     *            metadata property name
     * @return metadata property value
     */
    public static String getMetadataFromBuildFile(String prop) throws MissingResourceException {
        String property = "";
        File gradle = new File(userDir + "/build.gradle");
        File gradleKTS = new File(userDir + "/build.gradle.kts");
        File pom = new File(userDir + "/pom.xml");
        File projectProps = new File(userDir + "/config/project.properties");

        boolean pomAvailable = pom.exists();
        boolean gradleAvailable = gradle.exists() || gradleKTS.exists();
        boolean propsAvailable = projectProps.exists();

        if (gradleAvailable && pomAvailable) {
            System.out.println("Multiple build instructions detected, resolving to pom.xml...");
            property = getMetadataFromMaven(prop);
        } else if (gradleAvailable) {
            property = getMetadataFromGradle(prop);
        } else if (pomAvailable) {
            property = getMetadataFromMaven(prop);
        } else if (propsAvailable) {
            property = getMetadataFromProjectProperties(prop, projectProps.getPath());
        }
        return property;
    }

    private static String getMetadataFromMaven(String prop) throws MissingResourceException {
        String property = "";
        File pom = new File(userDir + "/pom.xml");
        System.out.println("* Maven project detected, grabbing missing metadata from pom.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(pom);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("project");

            for (int i = 0; i < nList.getLength(); i++) {
                Node nNode = nList.item(i);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    property = eElement.getElementsByTagName(prop).item(0).getTextContent();
                }
            }
        } catch (ParserConfigurationException e) {
            System.out.println("Error creating DocumentBuilder");
            e.printStackTrace();
            failBuildFromMissingMavenMetadata();
        } catch (SAXException e) {
            System.out.println("SAX error");
            e.printStackTrace();
            failBuildFromMissingMavenMetadata();
        } catch (IOException e) {
            System.out.println("Failed to read project file: "+ pom);
            e.printStackTrace();
            failBuildFromMissingMavenMetadata();
        }
        return property;
    }

    private static String getMetadataFromGradle(String prop) throws MissingResourceException {
        // System.out.println("* Gradle project detected, grabbing missing metadata from gradle build files");
        // System.out.println("* Checking for Groovy or Kotlin style build instructions");
        String property = "";
        String dir = System.getProperty("user.dir");
        String switcher;
        File buildFile = new File(dir + "/settings.gradle");

        if (buildFile.exists()) {
            switcher = "groovy";
        } else {
            switcher = "kotlin";
        }

        // System.out.println("Prop is currently: {}", prop);
        switch (switcher) {
        case "groovy":
            // System.out.println("* Regular (groovy) build file detected, looking for possible metadata...");
            property = getGradleProperty(prop, dir, "/config/project.properties", "/settings.gradle",
                    "/version.gradle");
            break;
        case "kotlin":
            // System.out.println("* Kotlin style build file detected, looking for possible metadata...");
            property = getGradleProperty(prop, dir, "/config/project.properties", "/settings.gradle.kts",
                    "/version.gradle.kts");
            break;
        }

        return property;
    }

    private static String getMetadataFromProjectProperties(String prop, String propsFile) {
        if (prop == "artifactId") {
            prop = "PROJECT_ARTIFACT";
        } else {
            prop = "PROJECT_VERSION";
        }
        Properties props = loadProperties(propsFile);

        return props.getProperty(prop);
    }

    private static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        File buildFile = new File(fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(buildFile))) {
            props.load(reader);
        } catch (IOException e) {
            System.out.println("Failed to read project properties file: "+ buildFile);
            e.printStackTrace();
        }
        return props;
    }

    private static String getGradleProperty(String prop, String dir, String... cfgFiles)
            throws MissingResourceException {
        if (prop == "artifactId") {
            prop = "PROJECT_ARTIFACT";
        } else {
            prop = "PROJECT_VERSION";
        }
        Properties props = loadProperties(dir + cfgFiles[0]);
        String gradleProp = props.getProperty(prop);
        if (prop == "PROJECT_ARTIFACT" && !isPropUnspecified("PROJECT_ROOT")) { // for subprojects
            String parent = props.getProperty("PROJECT_ROOT");
            parent = parent.replaceAll("\\s", "").split("'")[1];
            if (parent.equals(gradleProp)) {
                return gradleProp;
            } else {
                return parent + "/" + gradleProp;
            }
        }
        if (prop == "PROJECT_ARTIFACT" && isPropUnspecified(gradleProp)) {
            String property = "";
            File buildFile = new File(dir + cfgFiles[1]);
            try (BufferedReader reader = new BufferedReader(new FileReader(buildFile))) {
                String line;
                prop = "rootProject.name";
                while ((line = reader.readLine()) != null) {
                    if (line.contains(prop)) {
                        // System.out.println("Found relevant metadata: {}", line);
                        line = line.replaceAll("\\s", "");
                        property = line.split("'")[1];
                    }
                }
            } catch (IOException e) {
                failBuildFromMissingMetadata("Project");
                System.out.println("Failed to read project file: "+ buildFile);
                e.printStackTrace();
            }
            return property;
        }

        if (prop == "PROJECT_VERSION" && isPropUnspecified(gradleProp)) {
            String property = "";
            File buildFile = new File(dir + cfgFiles[2]);
            try (BufferedReader reader = new BufferedReader(new FileReader(buildFile))) {
                String line;
                prop = "version =";
                while ((line = reader.readLine()) != null) {
                    if (line.contains(prop)) {
                        System.out.println("Found relevant metadata: "+ line);
                        line = line.replaceAll("\\s", "");
                        property = line.split("'")[1];
                    }
                }
            } catch (IOException e) {
                failBuildFromMissingMetadata("Version");
                System.out.println("Failed to read project file: "+ buildFile);
                e.printStackTrace();
            }
            return property;
        }
        return gradleProp;
    }

    private static boolean isPropUnspecified(String prop) {
        return StringUtils.isBlank(prop) || "unspecified".equals(prop);
    }

    public static void failBuildFromMissingMetadata(String metadata) throws MissingResourceException {
        System.out.println("* ===[Build failed from lack of metadata: (" + metadata + ")]===");
        System.out.println("* CyBench runner is unable to continue due to missing crucial metadata.");
        if (metadata.contains("Version")) {
            System.out.println("* Project version metadata was unable to be processed.");
            System.out.println("* Project version can be set or parsed dynamically a few different ways: \n");
            System.out.println("*** The quickest and easiest (Gradle) solution is by adding an Ant task to 'build.gradle'"
                    + " to generate 'project.properties' file.");
            System.out.println("*** This Ant task can be found in the README for CyBench Gradle Plugin"
                    + " (https://github.com/K2NIO/gocypher-cybench-gradle/blob/master/README.md) \n");
            System.out.println("*** For Gradle (groovy) projects, please set 'version = \"<yourProjectVersionNumber>\"' in either "
                            + "'build.gradle' or 'version.gradle'.");
            System.out.println("*** For Gradle (kotlin) projects, please set 'version = \"<yourProjectVersionNumber>\"' in either "
                            + "'build.gradle.kts' or 'version.gradle.kts'.");
            System.out.println("*** For Maven projects, please make sure '<version>' tag is set correctly.\n");
            System.out.println("* If running benchmarks from a class you compiled/generated yourself via IDE plugin (Eclipse, Intellij, etc..),");
            System.out.println("* please set the @BenchmarkMetaData projectVersion tag at the class level");
            System.out.println("* e.g.: '@BenchmarkMetaData(key = \"projectVersion\", value = \"1.6.0\")'");
            System.out.println("* Project version can also be detected from 'metadata.properties' in your project's 'config' folder.");
            System.out.println("* If setting project version via 'metadata.properties', please add the following: ");
            System.out.println("* 'class.version=<yourProjectVersionNumber>'\n");
            System.out.println("* For more information and instructions on this process, please visit the CyBench wiki at "
                    + "https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-CyBench-annotations");

            throw new MissingResourceException("Missing project metadata configuration", null, null);
        } else if (metadata.contains("Project")) {
            System.out.println("* Project name metadata was unable to be processed.");
            System.out.println("* Project name can be set or parsed dynamically a few different ways: \n");
            System.out.println("*** The quickest and easiest (Gradle) solution is by adding an Ant task to 'build.gradle'"
                    + " to generate 'project.properties' file.");
            System.out.println("*** This Ant task can be found in the README for CyBench Gradle Plugin"
                    + " (https://github.com/K2NIO/gocypher-cybench-gradle/blob/master/README.md) \n");
            System.out.println("*** For Gradle (groovy) projects, please set 'rootProject.name = \"<yourProjectName>\"' in 'settings.gradle'.");
            System.out.println("*** For Gradle (kotlin) projects, please set 'rootProject.name = \"<yourProjectName>\"' in 'settings.gradle.kts'.");
            System.out.println("**** Important note regarding Gradle project's name: This value is read-only in 'build.gradle(.kts)'. This value *MUST*"
                            + " be set in 'settings.gradle(.kts)' if the project name isn't able to be dynamically parsed.");
            System.out.println("*** For Maven projects, please make sure '<artifactId>' tag is set correctly.\n");
            System.out.println("*** If running benchmarks from a class you compiled/generated yourself via IDE plugin (Eclipse, Intellij, etc..), "
                            + "please set the @BenchmarkMetaData project tag at the class level");
            System.out.println("**** e.g.: '@BenchmarkMetaData(key = \"project\", value = \"myTestProject\")'");
            System.out.println("*** Project version can also be detected from 'metadata.properties' in your project's 'config' folder.");
            System.out.println("*** If setting project version via 'metadata.properties', please add the following: ");
            System.out.println("*** 'class.project=<yourProjectName>'\n");
            System.out.println("* For more information and instructions on this process, please visit the CyBench wiki at "
                    + "https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-CyBench-annotations");

            throw new MissingResourceException("Missing project metadata configuration", null, null);
        }
    }

    public static void failBuildFromMissingMavenMetadata() throws MissingResourceException {
        System.out.println("* ===[Build failed from lack of metadata]===");
        System.out.println("* CyBench runner is unable to continue due to missing crucial metadata.");
        System.out.println("* Error while parsing Maven project's 'pom.xml' file.");
        System.out.println("* 'artifactId' or 'version' tag was unable to be parsed. ");
        System.out.println("* Refer to the exception thrown for reasons why the .xml file was unable to be parsed.");
        System.out.println("* For more information on CyBench metadata (setting it, how it is used, etc.), please visit the CyBench wiki at "
                        + "https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-CyBench-annotations");

        throw new MissingResourceException("Missing project metadata configuration", null, null);
    }
}
