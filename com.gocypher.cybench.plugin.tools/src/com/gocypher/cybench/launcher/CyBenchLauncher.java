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
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.gocypher.cybench.core.utils.SecurityUtils;
import com.gocypher.cybench.launcher.model.BenchmarkReport;

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
	private static final  String benchSource = "Eclipse plugin";
	private static String filePath;
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
		
		filePath = launcherConfiguration.getPathToPlainReportFile();
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
                    System.out.println(report);
                    try {
                        if (StringUtils.isNotEmpty(benchmarkReport.getProject())) {
                            report.setProject(benchmarkReport.getProject());
                        } else {
                            report.setProject(getMetadataFromBuildFile("artifactId"));
                            benchmarkReport.setProject(getMetadataFromBuildFile("artifactId"));
                        }

                        if (StringUtils.isNotEmpty(benchmarkReport.getProjectVersion())) {
                            report.setProjectVersion(benchmarkReport.getProjectVersion());
                        } else {
                            report.setProjectVersion(getMetadataFromBuildFile("version")); // default
                            benchmarkReport.setProjectVersion(getMetadataFromBuildFile("version"));
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
                    } catch (Exception e) {
                    	System.out.println("Error grabbing metadata: " + e);
                    }                    
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
            responseWithUrl = DeliveryService.getInstance().sendReportForStoring(reportEncrypted, tokenAndEmail);
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
		launcherConfiguration.setEmailAddress(checkNullAndReturnString(Constants.USER_EMAIL_ADDRESS));
		
    	if(launcherConfiguration.getRemoteAccessToken() != null && !launcherConfiguration.getRemoteAccessToken().equals("")){
    		launcherConfiguration.setReportUploadStatus("private");
		}else{
			launcherConfiguration.setReportUploadStatus("public");
		}
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
        if(key.equals("project")){
        	benchmarkReport.setProject(value);
        }
        if(key.equals("projectVersion")) {
        	benchmarkReport.setProjectVersion(value);
        }
    }
    public static String getMetadataFromBuildFile(String prop) {
        String property = "";

        Path tempPath = Paths.get(filePath);
        tempPath = tempPath.getParent().getParent();
        System.out.println("Real Path: " + tempPath.toString());
        
        File gradle = new File(tempPath + "/build.gradle");
        File gradleKTS = new File(tempPath + "/build.gradle.kts");
        File pom = new File(tempPath + "/pom.xml");
       
        if (gradle.exists() && pom.exists()) {
            System.out.println("Multiple build instructions detected, resolving to pom.xml..");
            property = getMetaDataFromMaven(prop);
        } else if (gradle.exists() || gradleKTS.exists()) {
            property = getMetaDataFromGradle(prop);
        } else if (pom.exists()) {
            property = getMetaDataFromMaven(prop);
        }
        return property;
    }

    private static String getMetaDataFromMaven(String prop) {
        String property = "";
        Path tempPath = Paths.get(filePath);
        tempPath = tempPath.getParent().getParent();
        File pom = new File(tempPath + "/pom.xml");
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
            failBuildFromMissingMetaData();
        } catch (SAXException e) {
            System.out.println("SAX error");
            e.printStackTrace();
            failBuildFromMissingMetaData();
        } catch (IOException e) {
            System.out.println("IO Error");
            e.printStackTrace();
            failBuildFromMissingMetaData();
        }
        return property;
    }

    private static String getMetaDataFromGradle(String prop) {
        System.out.println("* Gradle project detected, grabbing missing metadata from gradle build files");
        System.out.println("* Checking for Groovy or Kotlin style build instructions");
        String property = "";
        String dir = System.getProperty("user.dir");
        String switcher;
        File buildFile = new File(dir + "/settings.gradle");

        if (buildFile.exists()) {
            switcher = "groovy";
        } else {
            switcher = "kotlin";
        }

        switch (switcher) {
        case "groovy":
             System.out.println("* Regular (groovy) build file detected, looking for possible metadata..");
            property = getGradleProperty(prop, dir,
                    new String[] { "/config/project.properties", "/settings.gradle", "/version.gradle" });
            break;
        case "kotlin":
             System.out.println("* Kotlin style build file detected, looking for possible metadata..");
            property = getGradleProperty(prop, dir,
                    new String[] { "/config/project.properties", "/settings.gradle.kts", "/version.gradle.kts" });
            break;
        }

        return property;
    }

    private static String getGradleProperty(String prop, String dir, String[] cfgFiles) {
        String property = "";
        try {
            String temp;
            if (prop == "artifactId") {
                prop = "PROJECT_ARTIFACT";
            } else {
                prop = "PROJECT_VERSION";
            }
            Properties props = new Properties();
            File buildFile = new File(dir + cfgFiles[0]);
            try (BufferedReader reader = new BufferedReader(new FileReader(buildFile))) {
                props.load(reader);
            }
            String tempProp = props.getProperty(prop);
            if (prop == "PROJECT_ARTIFACT" && !isPropUnspecified("PROJECT_ROOT")) { // for subprojects
                String parent = props.getProperty("PROJECT_ROOT");
                parent = parent.replaceAll("\\s", "").split("'")[1];
                if (parent.equals(tempProp)) {
                    return tempProp;
                } else {
                    return parent + "/" + tempProp;
                }
            }
            if (prop == "PROJECT_ARTIFACT" && isPropUnspecified(tempProp)) {
                buildFile = new File(dir + cfgFiles[1]);
                try (BufferedReader reader = new BufferedReader(new FileReader(buildFile))) {
                    prop = "rootProject.name";
                    while ((temp = reader.readLine()) != null) {
                        if (temp.contains(prop)) {
                            temp = temp.replaceAll("\\s", "");
                            property = temp.split("'")[1];
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    failBuildFromMissingMetaData("Project");
                }
                return property;
            }

            if (prop == "PROJECT_VERSION" && isPropUnspecified(tempProp)) {
                buildFile = new File(dir + cfgFiles[2]);
                try (BufferedReader reader = new BufferedReader(new FileReader(buildFile))) {
                    prop = "version =";
                    while ((temp = reader.readLine()) != null) {
                        if (temp.contains(prop)) {
                            System.out.println("Found relevant metadata: " + temp);
                            temp = temp.replaceAll("\\s", "");
                            property = temp.split("'")[1];
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    failBuildFromMissingMetaData("Version");
                }
                return property;
            }
            return tempProp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return property;
    }

    private static boolean isPropUnspecified(String prop) {
        return StringUtils.isBlank(prop) || "unspecified".equals(prop);
    }

    private static void failBuildFromMissingMetaData(String metadata) {
        System.out.println("* ===[Build failed from lack of metadata: ("+ metadata + ")]===");
        System.out.println("* CyBench runner is unable to continue due to missing crucial metadata.");
        if (metadata.contains("Version")) {
            System.out.println("* Project version metadata was unable to be processed.");
            System.out.println("* Project version can be set or parsed dynamically a few different ways: \n");
            System.out.println("*** For Gradle (groovy) projects, please set 'version = \"<yourProjectVersionNumber>\"' in either "
                            + "'build.gradle' or 'version.gradle'.");
            System.out.println("*** For Gradle (kotlin) projects, please set 'version = \"<yourProjectVersionNumber>\"' in either "
                            + "'build.gradle.kts' or 'version.gradle.kts'.");
            System.out.println("*** For Maven projects, please make sure '<version>' tag is set correctly.\n");
            System.out.println("*** If running benchmarks from a class you compiled/generated yourself via IDE plugin (Eclipse, Intellij, etc..),");
            System.out.println("*** please set the @BenchmarkMetaData projectVersion tag at the class level");
            System.out.println("**** e.g.: '@BenchmarkMetaData(key = \"projectVersion\", value = \"1.6.0\")'");
            System.out.println("*** Project version can also be detected from 'metadata.properties' in your project's 'config' folder.");
            System.out.println("*** If setting project version via 'metadata.properties', please add the following: ");
            System.out.println("*** 'class.version=<yourProjectVersionNumber>'\n");
            System.out.println("* For more information and instructions on this process, please visit the CyBench wiki at "
                    + "https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-CyBench-annotations");
            System.exit(1);
        } else if (metadata.contains("Project")) {
            System.out.println("* Project name metadata was unable to be processed.");
            System.out.println("* Project name can be set or parsed dynamically a few different ways: \n");
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
            System.exit(1);
        }
    }

    private static void failBuildFromMissingMetaData() {
        System.out.println("* ===[Build failed from lack of metadata]===");
        System.out.println("* CyBench runner is unable to continue due to missing crucial metadata.");
        System.out.println("* Error while parsing Maven project's 'pom.xml' file.");
        System.out.println("* 'artifactId' or 'version' tag was unable to be parsed. ");
        System.out.println("* Refer to the exception thrown for reasons why the .xml file was unable to be parsed.");
        System.out.println("* For more information on CyBench metadata (setting it, how it is used, etc.), please visit the CyBench wiki at "
                        + "https://github.com/K2NIO/gocypher-cybench-java/wiki/Getting-started-with-CyBench-annotations");
        System.exit(1);
    }
}
