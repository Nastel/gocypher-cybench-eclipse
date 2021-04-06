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

package com.gocypher.cybench.plugin.handlers;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.model.LaunchConfiguration;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;

public class LaunchRunConfiguration extends org.eclipse.debug.core.model.LaunchConfigurationDelegate {
	
	private String reportFolder;
	private String reportName;
	private String launchPath;
	private String classPathFromUser;
	private String selectionFolderPath;
	private String jvmProperties;
	
	private String accessToken;
	private String  reportUploadStatus;
	
	private Integer thread;
	private Integer forks;
	private Integer warmupIterations;
	private Integer measurmentIterations;
	private Integer warmupSeconds;
	private Integer mesurmentSeconds;
    
	private Boolean sendReportCybnech;
	private Boolean includeHardware;
	private  Boolean useCyBenchBenchmarkSettings;
    
	public static String resolveBundleLocation (String bundleSymbolicName, boolean shouldAddBin) {
		try {
			URL pluginURL = FileLocator.resolve(Platform.getBundle(bundleSymbolicName).getEntry("/"));
			String pluginInstallDir = pluginURL.getPath().trim().replace("!/", "").replace("jar:", "").replace("file:", "");
			if( Platform.getOS().compareTo(Platform.OS_WIN32) == 0 ) {
				if (shouldAddBin && !pluginInstallDir.endsWith("jar")) {
					return  pluginInstallDir.substring(1)+"bin" ;
				}
				if (pluginInstallDir.endsWith("/")) {
					return  pluginInstallDir.substring(1,pluginInstallDir.length()-1) ;
				}
				return  pluginInstallDir.substring(1) ;
			}
			return pluginInstallDir ; 
			
		}catch (Exception ex) {
			GuiUtils.logError("Error during bundle location resolve",ex);
		}
		return "" ;
	}
	
	@Override
	public void launch(ILaunchConfiguration configuration, String arg1, ILaunch arg2, IProgressMonitor arg3)
			throws CoreException {
		try {
			setRunConfigurationProperties(configuration);
			
			String projectPath = reportFolder.substring(0, reportFolder.lastIndexOf('/'));
	    	GuiUtils.logInfo("reportFolder: "+projectPath) ;
	    	IProject project = LauncherUtils.getProjectFromPath(projectPath);
	    	
	
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager() ;
			ILaunchConfigurationType launchType = manager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
			final ILaunchConfigurationWorkingCopy config = launchType.newInstance(null, "CyBench plugin");
			    
			List<String> classPaths = new ArrayList<String>();
			if(LauncherUtils.isJavaProject(project) && !LauncherUtils.isGradleProject(project)) {
				IJavaProject javaProject = (IJavaProject)JavaCore.create((IProject)project);
				IClasspathEntry[] resolvedClasspath= javaProject.getResolvedClasspath(false);
				for(IClasspathEntry classPathTest : resolvedClasspath) {
					String tempPathVariable = classPathTest.getPath().toOSString();
					String referenceToTargetClassesForMavenModules = LauncherUtils.addReferenceToTragetClassesForMavenModules(tempPathVariable);
					if(referenceToTargetClassesForMavenModules != ""){
						classPaths.add(referenceToTargetClassesForMavenModules);
					}else{
						classPaths.add(tempPathVariable);
					}
				}
			}
			if(LauncherUtils.isJavaProject(project)) {
				IJavaProject javaProject = (IJavaProject)JavaCore.create((IProject)project);
				if(reportName == null || reportName.equals("")) {
					reportName = LauncherUtils.getProjectNameConstruction(javaProject, "");
				}
			}
	    	selectionFolderPath = selectionFolderPath.replaceAll("\\s+","");
			String pathToTempReportPlainFile = CybenchUtils.generatePlainReportFilename(reportFolder, true, reportName.replaceAll(" ", "_")) ;
			String pathToTempReportEncryptedFile = CybenchUtils.generateEncryptedReportFilename(reportFolder, true, reportName.replaceAll(" ", "_")) ;
			
			setEnvironmentProperties(config);
			
			config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
	    
	    	if(project != null){
//		    	GuiUtils.logInfo("project: "+project.getLocation().toPortableString()) ;
				if((LauncherUtils.isMavenProject(project))){
					String testPath = launchPath.substring(0, launchPath.lastIndexOf('/'))+ "/test-"+launchPath.substring(launchPath.lastIndexOf('/') + 1);
					launchPath = launchPath +","+ testPath;
				}else if(LauncherUtils.isGradleProject(project)){
					String testPath = launchPath.substring(0, launchPath.lastIndexOf('/'))+ "/test";
					launchPath = launchPath +","+ testPath;
				}
	    	}
			if(classPathFromUser != null && !classPathFromUser.equals("")){
				classPaths.addAll(Arrays.asList(classPathFromUser.split(",")));
	    	}
			classPaths.addAll(Arrays.asList(launchPath.split(",")));
			classPaths.add(LauncherUtils.resolveBundleLocation(Activator.PLUGIN_ID, true));
			classPaths.add(LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID,false) );
		
			List<String> classpathMementos = LauncherUtils.getNeededClassPaths(project, classPaths);
	    	//GuiUtils.logInfo("Classpath: "+classpathMementos) ;
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpathMementos);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "\""+pathToTempReportPlainFile+"\" \""+pathToTempReportEncryptedFile+"\"");
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.gocypher.cybench.launcher.CyBenchLauncher");
			
			new Thread ( new Runnable() {
				
				@Override
				public void run() {
					try {
					    ILaunch launchedBenchmarks = config.launch(ILaunchManager.RUN_MODE, null);
					    while (!launchedBenchmarks.isTerminated()) {
					    	try {
					    		Thread.sleep(1000);
					    	}catch (Exception e) {
					    		
					    	}
					    }
						GuiUtils.refreshCybenchExplorer();			
						GuiUtils.openReportDisplayView(pathToTempReportPlainFile);		
					} catch (CoreException e) {
						GuiUtils.logError("Error during launch",e);
					}
				}
			}).start();
		}catch (Exception e) {
			GuiUtils.logError("Error during launch",e);
		}
		
	}
    private void setEnvironmentProperties(ILaunchConfigurationWorkingCopy config) {
    	String start = " -D";
    	GuiUtils.logInfo(
    			start+Constants.NUMBER_OF_FORKS+"="+forks+
    			start+Constants.RUN_THREAD_COUNT+"="+thread+
    			start+Constants.BENCHMARK_REPORT_NAME+"=\""+reportName+"\""+
    			start+Constants.REPORT_UPLOAD_STATUS+"=\""+reportUploadStatus+"\""+

    			start+Constants.WARM_UP_ITERATIONS+"="+warmupIterations+
    			start+Constants.MEASUREMENT_ITERATIONS+"="+measurmentIterations+
    			start+Constants.WARM_UP_SECONDS+"="+warmupSeconds+
    			start+Constants.MEASUREMENT_SECONDS+"="+mesurmentSeconds+
    			start+Constants.SEND_REPORT+"="+sendReportCybnech+
    			
    			start+Constants.COLLECT_HARDWARE_PROPS+"="+includeHardware+
    			start+Constants.USE_CYBENCH_CONFIGURATION+"="+useCyBenchBenchmarkSettings+
    			
				"  "+jvmProperties+
				
    			start+Constants.USER_REPORT_TOKEN+"="+accessToken+
    			start+Constants.SELECTED_CLASS_PATHS+"=\""+selectionFolderPath+"\"");
    			
//				" -DTHREADS_COUNT="+thread+
//				" -DREPORT_NAME=\""+reportName+"\""+
//				" -DBENCHMARK_REPORT_STATUS=\""+reportUploadStatus+"\""+
//				" -DWARMUP_ITERATION="+warmupIterations+
//				" -DMEASURMENT_ITERATIONS="+measurmentIterations+
//				" -DWARMUP_SECONDS="+warmupSeconds+
//				" -DMEASURMENT_SECONDS="+mesurmentSeconds+
//				" -DSHOULD_SAVE_REPOT_TO_FILE="+storeReportInFile+
//				" -DSHOULD_SEND_REPORT_CYBENCH="+sendReportCybnech+
//				" -DINCLUDE_HARDWARE_PROPERTIES="+includeHardware+
//				" -DUSE_CYBNECH_BENCHMARK_SETTINGS="+useCyBenchBenchmarkSettings+
//				" -DEXECUTION_SCORE="+excutionScoreBoundary+
//				"  "+jvmProperties+
//				" -DCUSTOM_USER_PROPERTIES=\""+userProperties+"\""+
//				" -DREMOTE_CYBENCH_ACCESS_TOKEN="+accessToken+
//				" -DREPORT_CLASSES=\""+selectionFolderPath+"\"");
		
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, 
    			start+Constants.NUMBER_OF_FORKS+"="+forks+
    			start+Constants.RUN_THREAD_COUNT+"="+thread+
    			start+Constants.BENCHMARK_REPORT_NAME+"=\""+reportName+"\""+
    			start+Constants.REPORT_UPLOAD_STATUS+"=\""+reportUploadStatus+"\""+

    			start+Constants.WARM_UP_ITERATIONS+"="+warmupIterations+
    			start+Constants.MEASUREMENT_ITERATIONS+"="+measurmentIterations+
    			start+Constants.WARM_UP_SECONDS+"="+warmupSeconds+
    			start+Constants.MEASUREMENT_SECONDS+"="+mesurmentSeconds+
    			start+Constants.SEND_REPORT+"="+sendReportCybnech+
    			
    			start+Constants.COLLECT_HARDWARE_PROPS+"="+includeHardware+
    			start+Constants.USE_CYBENCH_CONFIGURATION+"="+useCyBenchBenchmarkSettings+
    			
				"  "+jvmProperties+
				
    			start+Constants.USER_REPORT_TOKEN+"="+accessToken+
    			start+Constants.SELECTED_CLASS_PATHS+"=\""+selectionFolderPath+"\"");
		
    }
    
    private void setRunConfigurationProperties(ILaunchConfiguration configuration) throws CoreException {
	   reportFolder = configuration.getAttribute(LaunchConfiguration.REPORT_FOLDER, "/report");
       reportName = configuration.getAttribute(LaunchConfiguration.REPORT_NAME, "CyBench Report");
   	   launchPath = configuration.getAttribute(LaunchConfiguration.BUILD_PATH, "");
   	   selectionFolderPath =configuration.getAttribute(LaunchConfiguration.LAUNCH_SELECTED_PATH, "");  
   	   classPathFromUser = configuration.getAttribute(LaunchConfiguration.ADD_CUSTOM_CLASS_PATH, "");
	   jvmProperties = configuration.getAttribute(LaunchConfiguration.CUSTOM_JVM_PROPERTIES, "");
//     storeReportInFile = configuration.getAttribute(LaunchConfiguration.SHOULD_SAVE_REPOT_TO_FILE, true);
//     userProperties = configuration.getAttribute(LaunchConfiguration.CUSTOM_USER_PROPERTIES, "");
// 	   excutionScoreBoundary = configuration.getAttribute(LaunchConfiguration.EXECUTION_SCORE, -1);
	   
	   	reportUploadStatus = configuration.getAttribute(LaunchConfiguration.BENCHMARK_REPORT_STATUS, "public");
		
	    thread = configuration.getAttribute(LaunchConfiguration.TREADS_COUNT, 1);
	    forks  = configuration.getAttribute(LaunchConfiguration.FORKS_COUNT, 1);
	    warmupIterations  = configuration.getAttribute(LaunchConfiguration.WARMUP_ITERATION, 1);
	    measurmentIterations = configuration.getAttribute(LaunchConfiguration.MEASURMENT_ITERATIONS, 5);
	    warmupSeconds = configuration.getAttribute(LaunchConfiguration.WARMUP_SECONDS, 10);
	    mesurmentSeconds = configuration.getAttribute(LaunchConfiguration.MEASURMENT_SECONDS, 10);
	    
	    sendReportCybnech = configuration.getAttribute(LaunchConfiguration.SHOULD_SEND_REPORT_CYBENCH, true);
	    includeHardware = configuration.getAttribute(LaunchConfiguration.INCLUDE_HARDWARE_PROPERTIES, true);
	    useCyBenchBenchmarkSettings = configuration.getAttribute(LaunchConfiguration.USE_CYBNECH_BENCHMARK_SETTINGS, false);

	    accessToken =  configuration.getAttribute(LaunchConfiguration.REMOTE_CYBENCH_ACCESS_TOKEN, "");
//   	   launchConfigurationMemento = configuration.getMemento();
    }
}