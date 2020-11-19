package com.gocypher.cybench.plugin.handlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
//		    System.out.println("Execution Mode: " + mode);
//	    	System.out.println(System.getProperty("line.separator"));
//			System.out.println("project path: "+selectionEntry.getProjectPath());
//			System.out.println("output path: "+selectionEntry.getOutputPath());
//			System.out.println("classes paths: "+selectionEntry.getClassPaths());
//			System.out.println("project reports path: "+selectionEntry.getProjectReportsPath());
//	    	System.out.println(System.getProperty("line.separator"));
	    	
			String pathToTempReportPlainFile = CybenchUtils.generatePlainReportFilename(selectionEntry.getProjectReportsPath(), true, "report") ;
			String pathToTempReportEncryptedFile = CybenchUtils.generateEncryptedReportFilename(selectionEntry.getProjectReportsPath(), true, "report") ;
	
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager() ;
			ILaunchConfigurationType launchType = manager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
			final ILaunchConfigurationWorkingCopy config = launchType.newInstance(null, "CyBench plugin");
			    
			setEnvironmentProperties(config, selectionEntry);

			config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
			String[] classpath = new String[] { selectionEntry.getOutputPath()
					,LauncherUtils.resolveBundleLocation(Activator.PLUGIN_ID, true)
					,LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID,false) 
					};
			
		
			List<String> classpathMementos = new ArrayList<String>();
			for (int i = 0; i < classpath.length; i++) {
			    IRuntimeClasspathEntry cpEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(classpath[i]));
			    cpEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
			    try {
			        classpathMementos.add(cpEntry.getMemento());
			    } catch (CoreException e) {
			        System.err.println(e.getMessage());
			    }
			}
			
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
					    System.err.println(e.getMessage());
					}
				}
			}).start();
		}catch (Exception e) {
			e.printStackTrace();
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
		// TODO Auto-generated method stub
		
	}
   


}