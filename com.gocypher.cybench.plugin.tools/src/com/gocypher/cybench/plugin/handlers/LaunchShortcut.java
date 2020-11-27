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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.model.RunSelectionEntry;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;

public class LaunchShortcut implements ILaunchShortcut {



	@Override
	public void launch(ISelection selection, String mode) {
		try {
			RunSelectionEntry selectionEntry = LauncherUtils.fillRunselectionData(selection);
	    	
			String pathToTempReportPlainFile = CybenchUtils.generatePlainReportFilename(selectionEntry.getProjectReportsPath(), true, "report") ;
			String pathToTempReportEncryptedFile = CybenchUtils.generateEncryptedReportFilename(selectionEntry.getProjectReportsPath(), true, "report") ;
	
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager() ;
			ILaunchConfigurationType launchType = manager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
			final ILaunchConfigurationWorkingCopy config = launchType.newInstance(null, "CyBench plugin");
			    
			setEnvironmentProperties(config, selectionEntry);

			config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
			List<String> classPaths = new ArrayList<String>();
			classPaths.addAll(Arrays.asList(selectionEntry.getOutputPath().split(",")));
			classPaths.add(LauncherUtils.resolveBundleLocation(Activator.PLUGIN_ID, true));
			classPaths.add(LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID,false) );
			List<String> classpathMementos = new ArrayList<String>();
			for (int i = 0; i < classPaths.size(); i++) {
			    IRuntimeClasspathEntry cpEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(classPaths.get(i)));
			    cpEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
			    try {
			        classpathMementos.add(cpEntry.getMemento());
			    } catch (CoreException e) {
			    	GuiUtils.logError ("Error during classpath add",e) ;
			    }
			}
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
		System.out.println(
				" -DREPORT_FOLDER=\""+selection.getProjectReportsPath()+"\""
				+ " -DREPORT_CLASSES=\""+selection.getClassPaths().toString()+"\"");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, 
				" -DREPORT_FOLDER=\""+selection.getProjectReportsPath()+"\" "
				+ " -DREPORT_CLASSES=\""+LauncherUtils.setToString(selection.getClassPaths())+"\"");
    }
    
   
    
	@Override
	public void launch(IEditorPart arg0, String arg1) {
				
	}
   


}