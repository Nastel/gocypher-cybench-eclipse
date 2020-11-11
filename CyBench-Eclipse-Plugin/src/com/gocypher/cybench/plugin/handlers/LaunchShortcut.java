package com.gocypher.cybench.plugin.handlers;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.gocypher.cybench.core.utils.JSONUtils;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;

public class LaunchShortcut implements ILaunchShortcut {



	@Override
	public void launch(ISelection selection, String mode) {
		try {
		    String selectedPath = "";
		    String selectedRaportPath = "";
		    String reportsDirectory = "/reports";
		    System.out.println("Execution Mode: " + mode);

	    	System.out.println(System.getProperty("line.separator"));
	    	 if (selection instanceof IStructuredSelection) {
	    		 IStructuredSelection ss = (IStructuredSelection) selection;
	    		 Object element = ss.getFirstElement();
	    		  if (element instanceof IProject) {
	    	         IProject iproject = (IProject) element;
	    			 String real_file_path = iproject.getLocation().toString();
					 selectedPath = real_file_path;
					 selectedRaportPath = real_file_path+reportsDirectory;
	    		  }
	    		  else if (element instanceof IResource) {
	    	         IResource resElement = (IResource) element;
					 selectedPath = resElement.getLocation().toString();
					 selectedRaportPath = resElement.getLocation().toString()+reportsDirectory;
	    		  } else if (element instanceof IAdaptable) {
				     IAdaptable adaptable = (IAdaptable)element;
				     Object adapter = adaptable.getAdapter(IResource.class);
				     IResource res = (IResource) adapter;
					 selectedPath = res.getLocation().toString();
					 selectedRaportPath = res.getLocation().toString()+reportsDirectory;
			      }

		    	 for (Object elem : ss.toList()) {
		    		    IJavaProject javaProject = null;

		    		    if (elem instanceof IJavaProject) {
		    		    	javaProject = (IJavaProject)elem;
		    		    } else if (elem instanceof IProject) {
		    		    	javaProject = (IJavaProject)JavaCore.create((IProject)elem);
		    		    }
		    		    
		    			if(javaProject.getOutputLocation()!=null) {
		    				selectedPath = selectedPath.substring(0, selectedPath.lastIndexOf('/')) + javaProject.getOutputLocation().toPortableString();
		    			}       
		    		}
			 }
			System.out.println("selectedPath: "+selectedPath);
	    	System.out.println(System.getProperty("line.separator"));
	    	
			MessageConsole cyBenchConsole = LauncherUtils.findConsole("CyBench Console");
			cyBenchConsole.clearConsole();
			cyBenchConsole.activate();
			MessageConsoleStream out = cyBenchConsole.newMessageStream();
				
			out.println("-----------------------------------------------------------------------------------------");
			out.println("                                 Starting CyBench benchmarks                             ");
			out.println("-----------------------------------------------------------------------------------------");
			
//			System.out.println("Location of workspace:"+ResourcesPlugin.getWorkspace().getRoot().getRawLocationURI().toASCIIString() );
//			String pathToPluginLocalStateDirectory = Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID)).toPortableString() ;
//			System.out.println("Location of bundle state:"+pathToPluginLocalStateDirectory) ;
			String pathToTempReportPlainFile = CybenchUtils.generatePlainReportFilename(selectedRaportPath, true, "report") ;
			String pathToTempReportEncryptedFile = CybenchUtils.generateEncryptedReportFilename(selectedRaportPath, true, "report") ;
	
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager() ;
			ILaunchConfigurationType launchType = manager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
			final ILaunchConfigurationWorkingCopy config = launchType.newInstance(null, "CyBench plugin");
			    
			setEnvironmentProperties(config, selectedRaportPath);

			config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
			String[] classpath = new String[] { selectedPath
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
//			System.out.println("Classpath:"+classpathMementos);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpathMementos);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, pathToTempReportPlainFile+" "+pathToTempReportEncryptedFile);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.gocypher.cybench.launcher.CyBenchLauncher");
			
		
			new Thread ( new Runnable() {
				
				@Override
				public void run() {
					try {
					    ILaunch launchedBenchmarks = config.launch(ILaunchManager.RUN_MODE, null);
					  
					    out.println("Waiting for CyBench to finish...");
					    
					    while (!launchedBenchmarks.isTerminated()) {
					    	try {
					    		Thread.sleep(1000);
					    	}catch (Exception e) {
					    		
					    	}
					    }
					    out.println("Finished CyBench tests:"+launchedBenchmarks.isTerminated());
						String results = CybenchUtils.loadFile(pathToTempReportPlainFile) ;
						if (results != null && !results.isEmpty()) {
								out.println("Results from tests:"+JSONUtils.parseJsonIntoMap(results));
						}
						out.println("-----------------------------------------------------------------------------------------");
						out.println("                                 Finished CyBench benchmarks                             ");
						out.println("-----------------------------------------------------------------------------------------");
						
						
						GuiUtils.refreshCybenchExplorer();
						GuiUtils.openReportDisplayView(pathToTempReportPlainFile);					
						
						/*Display.getDefault().asyncExec(new Runnable() {
						    public void run() {
						    	try {
						    		System.out.println("Will open part for reports");
						    		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						    		page.showView(ReportsDisplayView.ID) ;
									IViewPart view = page.findView(ReportsDisplayView.ID) ;
						    		if (view instanceof ICybenchPartView) {
					    			((ICybenchPartView)view).refreshView();
					    		}
//							    	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ReportsDisplayView.ID) ; 
						    	}catch (Exception e) {
						    		e.printStackTrace();
						    	}
						    }
						});
						*/	
					} catch (CoreException e) {
					    System.err.println(e.getMessage());
					}
				}
			}).start();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
    private void setEnvironmentProperties(ILaunchConfigurationWorkingCopy config, String property) {
		System.out.println(" -DREPORT_FOLDER=\""+property+"\"");
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, " -DREPORT_FOLDER=\""+property+"\"");
    }
    
	@Override
	public void launch(IEditorPart arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
   


}