package com.gocypher.cybench.plugin.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import com.gocypher.cybench.core.utils.JSONUtils;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.model.ICybenchPartView;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;
import com.gocypher.cybench.plugin.views.ReportsDisplayView;

public class LaunchShortcut implements ILaunchShortcut {

	@Inject
	ESelectionService selectionService ;
	
	@Inject 
	UISynchronize sync;
	
	String selectedPath;
	
//    @Override 
//    public void launch(ISelection selection, String mode) {
//    
//        System.out.println("Execution Mode: " + mode);
//
//    	System.out.println(System.getProperty("line.separator"));
//    	 if (selection instanceof IStructuredSelection) {
//    		 IStructuredSelection ss = (IStructuredSelection) selection;
//    		 Object element = ss.getFirstElement();
//    		  if (element instanceof IResource) {
//    	         IResource resElement = (IResource) element;
//    			 System.out.println("Resource element:"+ (IResource) element+System.getProperty("line.separator"));
//				 System.out.println("Full File Path: "+resElement.getFullPath()+System.getProperty("line.separator"));
//				 selectedPath = resElement.getFullPath()+System.getProperty("line.separator");
//    		  } else if (element instanceof IAdaptable) {
//			     IAdaptable adaptable = (IAdaptable)element;
//			     Object adapter = adaptable.getAdapter(IResource.class);
//			     IResource res = (IResource) adapter;
//				 System.out.println("Resource adapter:"+ (IResource) adapter+System.getProperty("line.separator"));
//				 System.out.println("Full File Path: "+res.getFullPath()+System.getProperty("line.separator"));
//				 selectedPath = res.getFullPath()+System.getProperty("line.separator");
//		      }
//		 }
//    	String selectedProjectRun = selection.toString();
//
//    	String path = selectedProjectRun.substring(3, selectedProjectRun.length()-1)+System.getProperty("line.separator");
//    	switch(selectedProjectRun.charAt(1)) {
//    	case 'P':
//            System.out.println("Selected project name: " + path);
//    		break;
//    	case 'F':
//            System.out.println("Selected folder name: " + path);
//    		break;
//    	case 'L':
//            System.out.println("Selected class file run name: " + path);
//    		break;
//		default:
//			System.out.println("The selected value was not recognized as a Folder, Project or Class");
//    	
//    	}
//    }

	@Override
	public void launch(ISelection selection, String mode) {
		try {
			
			   System.out.println("Execution Mode: " + mode);

		    	System.out.println(System.getProperty("line.separator"));
		    	 if (selection instanceof IStructuredSelection) {
		    		 IStructuredSelection ss = (IStructuredSelection) selection;
		    		 Object element = ss.getFirstElement();
		    		  if (element instanceof IProject) {
		    	         IProject iproject = (IProject) element;
		    			 String real_file_path = iproject.getLocation().toString();
		    			 System.out.println("real_file_path: "+ real_file_path);
			    		 System.out.println("Resource element file: "+ iproject);
						 selectedPath = iproject.getLocation().toString();
		    		  }
		    		  else if (element instanceof IResource) {
		    	         IResource resElement = (IResource) element;
		    			 System.out.println("Resource element:"+ (IResource) element);
						 System.out.println("Full File Path: "+resElement.getFullPath().toString());
						 System.out.println("Full File getLocation: "+resElement.getLocation().toString());
						 selectedPath = resElement.getLocation().toString();
		    		  } else if (element instanceof IAdaptable) {
					     IAdaptable adaptable = (IAdaptable)element;
					     Object adapter = adaptable.getAdapter(IResource.class);
					     IResource res = (IResource) adapter;
						 System.out.println("Resource adapter:"+ (IResource) adapter);
						 System.out.println("Full File Path: "+res.getFullPath().toString());
						 System.out.println("Full File getLocation: "+res.getLocation().toString());
						 selectedPath = res.getLocation().toString();
				      }
				 }
	    	String selectedProjectRun = selection.toString();
	     	String path = selectedProjectRun.substring(3, selectedProjectRun.length()-1)+System.getProperty("line.separator");
	    	switch(selectedProjectRun.charAt(1)) {
	    	case 'P':
	    		selectedPath = selectedPath + "/target/classes";
	    		break;
	    	case 'F':
	    		selectedPath = selectedPath + "/target/classes";
	    		break;
	    	case 'L':
//	    		selectedPath = selectedPath;
	    		break;
			default:
				System.out.println("The selection was not recognized as a Folder, Project or Class");
	    	}
	    	
			System.out.println("selectedPath: "+selectedPath);
			
//    	    File exeFile=new File(".",selectedPath);
//			System.out.println("Path: "+exeFile.getAbsolutePath());
			System.out.println("Sync service:"+sync+";"+selectionService);
	    	System.out.println(System.getProperty("line.separator"));
			String msg = "" ;
			MessageConsole cyBenchConsole = LauncherUtils.findConsole("CyBench Console");
			cyBenchConsole.clearConsole();
			cyBenchConsole.activate();
			MessageConsoleStream out = cyBenchConsole.newMessageStream();
				
			out.println("-----------------------------------------------------------------------------------------");
			out.println("                                 Starting CyBench benchmarks                             ");
			out.println("-----------------------------------------------------------------------------------------");
			
			List<String>programArguments = new ArrayList<>() ;
			
			String bundlePaths = ""
			//+Platform.getBundle("CyBenchLauncherPlugin").getLocation() +";"
			//+Platform.getBundle("com.gocypher.cybench.externals").getLocation()
			;
			
			bundlePaths += LauncherUtils.resolveBundleLocation("CyBenchLauncherPlugin", true) ;
			bundlePaths += ";" ;
			bundlePaths += LauncherUtils.resolveBundleLocation("com.gocypher.cybench.externals", false) ;
		
			
			System.out.println(bundlePaths);
			String classPath= System.getProperty("java.class.path") ;
			System.out.println("Classpath found:"+classPath);
			System.out.println("selectedPath: "+selectedPath);
			
			
			classPath += ";"+selectedPath ;
			//System.setProperty("java.class.path", classPath) ;
			
			System.out.println("Location of workspace:"+ResourcesPlugin.getWorkspace().getRoot().getRawLocationURI().toASCIIString() );
		
			String pathToPluginLocalStateDirectory = Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID)).toPortableString() ;
			System.out.println("Location of bundle state:"+pathToPluginLocalStateDirectory) ;
			String pathToTempReportPlainFile = CybenchUtils.generatePlainReportFilename(pathToPluginLocalStateDirectory, true) ;
			String pathToTempReportEncryptedFile = CybenchUtils.generateEncryptedReportFilename(pathToPluginLocalStateDirectory, true) ;
					
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects() ;
			
			IPath pluginPath = ResourcesPlugin.getPlugin().getStateLocation() ;
			System.out.println("Plugin path:"+pluginPath.toOSString());
			
				
			if (projects != null) {
				for (IProject project:projects) {
					System.out.println(project.getFullPath());
				}
			}
			
			
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager() ;
			ILaunchConfigurationType launchType = manager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
			final ILaunchConfigurationWorkingCopy config = launchType.newInstance(null, "CyBench plugin");
			    
			    

			System.out.println("selectedPath: "+selectedPath);
			
			config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
			String[] classpath = new String[] { selectedPath
					,LauncherUtils.resolveBundleLocation(Activator.PLUGIN_ID, true)
					,LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID,false) 
					};
			
		
			List classpathMementos = new ArrayList();
			for (int i = 0; i < classpath.length; i++) {
			    IRuntimeClasspathEntry cpEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(classpath[i]));
			    cpEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
			    try {
			        classpathMementos.add(cpEntry.getMemento());
			    } catch (CoreException e) {
			        System.err.println(e.getMessage());
//			        this.showMsgBox(e.getMessage(),event);
			    }
			}
			System.out.println("Classpath:"+classpathMementos);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpathMementos);
			
			
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, pathToTempReportPlainFile+" "+pathToTempReportEncryptedFile);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.gocypher.cybench.launcher.CyBenchLauncher");
			
			//config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.local.demo.DemoRunner");
			//config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.gocypher.cybench.launcher.BenchmarkRunner");
		
			new Thread ( new Runnable() {
				
				@Override
				public void run() {
					try {
						//IProgressMonitor monitor =  //PlatformUI.getWorkbench().getProgressService() ;
					    ILaunch launchedBenchmarks = config.launch(ILaunchManager.RUN_MODE, null);
					  
					    out.println("Waiting for CyBench to finish...");
					    
					    while (!launchedBenchmarks.isTerminated()) {
					    	//out.println("Waiting for CyBench to finish:"+launchedBenchmarks.isTerminated());
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
						//this.showMsgBox("Results from tests:"+results, event);				
						
						
						out.println("-----------------------------------------------------------------------------------------");
						out.println("                                 Finished CyBench benchmarks                             ");
						out.println("-----------------------------------------------------------------------------------------");
						//cyBenchConsole.activate();
						
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
					    //this.showMsgBox(e.getMessage(),event);
					}
				}
			}).start();
		//this.showMsgBox("Reality:"+LauncherDemo.resultsMap, event);
		//this.launchCyBenchLauncher();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
    
	@Override
	public void launch(IEditorPart arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
   


}