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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.model.RunSelectionEntry;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;

public class LaunchShortcut implements ILaunchShortcut {

	/**
	* @component
	*/
//	private ArtifactResolver artifactResolver;
//	/**
//	*
//	* @component
//	*/
//	private ArtifactFactory artifactFactory;
//	/**
//	*
//	* @component
//	*/
//	private ArtifactMetadataSource metadataSource;
//	/**
//	*
//	* @parameter expression="${localRepository}"
//	*/
//	private ArtifactRepository localRepository;
//	/**
//	*
//	* @parameter expression="${project.remoteArtifactRepositories}"
//	*/
//	private List remoteRepositories;

	IProgressMonitor monitor = new NullProgressMonitor(); 
	private String reportName = null;
	
    @Override
	public void launch(ISelection selection, String mode) {
		try {
			reportName = null;
			RunSelectionEntry selectionEntry = LauncherUtils.fillRunselectionData(selection);
	    	
		
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager() ;
			ILaunchConfigurationType launchType = manager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
			final ILaunchConfigurationWorkingCopy config = launchType.newInstance(null, "CyBench plugin");
	
			if(reportName == null || reportName.equals("")) {
				String temp = selectionEntry.getClassPaths().toString();
				if(temp != null && temp.contains(".")) {
					reportName = LauncherUtils.getProjectNameConstruction(selectionEntry.getJavaProjectSelected(), temp.substring(temp.lastIndexOf('.'), temp.length()-1));
				}else{
					reportName = LauncherUtils.getProjectNameConstruction(selectionEntry.getJavaProjectSelected(), "");
				}
			}
			String pathToTempReportPlainFile = CybenchUtils.generatePlainReportFilename(selectionEntry.getProjectReportsPath(), true, reportName) ;
			String pathToTempReportEncryptedFile = CybenchUtils.generateEncryptedReportFilename(selectionEntry.getProjectReportsPath(), true, reportName) ;
	
			setEnvironmentProperties(config, selectionEntry);
			List<String> classPaths = new ArrayList<String>();

			if(LauncherUtils.isJavaProject(selectionEntry.getProjectSelected()) && !LauncherUtils.isGradleProject(selectionEntry.getProjectSelected())) {
				IJavaProject javaProject = selectionEntry.getJavaProjectSelected();
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
			config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
			classPaths.addAll(Arrays.asList(selectionEntry.getOutputPath().split(",")));
			classPaths.add(LauncherUtils.resolveBundleLocation(Activator.PLUGIN_ID, true));
			classPaths.add(LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID,false) );
			List<String> classpathMementos = LauncherUtils.getNeededClassPaths(selectionEntry.getProjectSelected(), classPaths);
//	    	GuiUtils.logInfo("Class-path: "+classpathMementos) ;
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

					} catch (Exception e) {
						GuiUtils.logError("Error during launch",e);
					}
				}
			}).start();
		}catch (Exception e) {
			GuiUtils.logError("Error during launch",e);
		}
		
	}
	
    private void setEnvironmentProperties(ILaunchConfigurationWorkingCopy config, RunSelectionEntry selection) {
    	String userHome = System.getProperty("user.home");
     	String start = " -D";
     	String classPaths = "";
     	if(!selection.getClassPaths().isEmpty()) {
     		classPaths = String.join(", ", selection.getClassPaths());
     	}
		System.out.println(
				start+Constants.BENCHMARK_REPORT_NAME+"=\""+reportName+"\""+
    			start+Constants.SELECTED_CLASS_PATHS+"=\""+classPaths+"\"");
//				" -DREPORT_FOLDER=\""+selection.getProjectReportsPath()+"\""
//				+ " -DREPORT_NAME=\""+reportName+"\""
//				+ " -DREPORT_CLASSES=\""+selection.getClassPaths().toString()+"\"");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, 
				start+Constants.BENCHMARK_REPORT_NAME+"=\""+reportName+"\""+
    			start+Constants.SELECTED_CLASS_PATHS+"=\""+classPaths+"\""+
    			start+Constants.AUTO_SHOULD_RUN_COMPARISON+"=\"false\""+
				start+"log4j.logs.root.path=" + userHome + "\\cybenchLogs\"");
//				" -DREPORT_FOLDER=\""+selection.getProjectReportsPath()+"\" "
//				+ " -DREPORT_NAME=\""+reportName+"\""
//				+ " -DREPORT_CLASSES=\""+LauncherUtils.setToString(selection.getClassPaths())+"\"");
		
		
    }
    
   
    
	@Override
	public void launch(IEditorPart arg0, String arg1) {
				
	}
   


}