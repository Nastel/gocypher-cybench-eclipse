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
			RunSelectionEntry selectionEntry = fillRunselectionData(selection);
		    System.out.println("Execution Mode: " + mode);
	    	System.out.println(System.getProperty("line.separator"));
			System.out.println("project path: "+selectionEntry.getProjectPath());
			System.out.println("output path: "+selectionEntry.getOutputPath());
			System.out.println("classes paths: "+selectionEntry.getClassPaths());
			System.out.println("project reports path: "+selectionEntry.getProjectReportsPath());
	    	System.out.println(System.getProperty("line.separator"));
	    	
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
					  
					    //out.println("Waiting for CyBench to finish...");
					    
					    while (!launchedBenchmarks.isTerminated()) {
					    	try {
					    		Thread.sleep(1000);
					    	}catch (Exception e) {
					    		
					    	}
					    }
					    /*out.println("Finished CyBench tests:"+launchedBenchmarks.isTerminated());
						String results = CybenchUtils.loadFile(pathToTempReportPlainFile) ;
						if (results != null && !results.isEmpty()) {
								out.println("Results from tests:"+JSONUtils.parseJsonIntoMap(results));
						}
						out.println("-----------------------------------------------------------------------------------------");
						out.println("                                 Finished CyBench benchmarks                             ");
						out.println("-----------------------------------------------------------------------------------------");
						*/

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
					} catch (Exception e) {
					    System.err.println(e.getMessage());
					}
				}
			}).start();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private RunSelectionEntry fillRunselectionData(ISelection selection) {
	    String reportsDirectory = "/reports";
		RunSelectionEntry selectionEntry = new RunSelectionEntry();
		try {
		 if (selection instanceof IStructuredSelection) {
	    		IStructuredSelection ss = (IStructuredSelection) selection;
		    	for (Object elem : ss.toList()) {
		    		String selectedPath = "";
		    		System.out.println("elem: "+elem.toString());
	    			IJavaProject javaProject = null;
 				if (elem instanceof IProject) {
	    				javaProject = (IJavaProject)JavaCore.create((IProject)elem);
	    				IProject iproject = (IProject) elem;
	    				selectionEntry.setProjectPath(iproject.getLocation().toString());
	    				System.out.println("selectedPath IProject: "+selectionEntry.getProjectPath());
	    			}
	    			else if (elem instanceof IFolder) {
	    				IAdaptable adaptable = (IAdaptable) elem;
	    				IResource res = (IResource) adaptable.getAdapter(IResource.class);
	    		        IFolder folder = (IFolder)  adaptable.getAdapter(IFolder.class);
	    		        System.out.println(folder);
	    		        IProject project = res.getProject();
	    		        selectedPath = res.getLocation().toString();
	    				selectionEntry.setProjectPath(selectedPath.replace("/"+res.getProjectRelativePath().toPortableString(), ""));
	    				javaProject = (IJavaProject)JavaCore.create((IProject)project);
	    				selectionEntry.setClassPaths(LauncherUtils.addClasses(folder.members(), selectionEntry.getClassPaths()));
	    			}
	    			else if (elem instanceof IFile) {
	    				IAdaptable adaptable = (IAdaptable) elem;
	    				IResource res = (IResource) adaptable.getAdapter(IResource.class);
	    		        IProject project = res.getProject();
	    		        
	    		        selectedPath = res.getLocation().toString();
	    		        String benchmarkClass = res.getFullPath().toPortableString().replace(".java", "");
	    		        selectionEntry.addClassPaths(benchmarkClass);
	    				selectionEntry.setProjectPath(selectedPath.replace("/"+res.getProjectRelativePath().toPortableString(), ""));
	    				javaProject = (IJavaProject)JavaCore.create((IProject)project);
	    				System.out.println("selectedPath IFile: "+selectionEntry.getProjectPath());
	    			}
	    			else if (elem instanceof IAdaptable) {
	    				IAdaptable adaptable = (IAdaptable) elem;
	    				IResource res = (IResource) adaptable.getAdapter(IResource.class);
	    		        IProject project = res.getProject();
	    		        selectedPath = res.getLocation().toString();
	    				selectionEntry.setProjectPath(selectedPath.replace("/"+res.getProjectRelativePath().toPortableString(), ""));
	    				javaProject = (IJavaProject)JavaCore.create((IProject)project);
	    			} else {
	    				System.err.println("The run selection was not recognized: "+ selection);
	    			}

	    			if(javaProject!=null && javaProject.getOutputLocation()!=null) {
	    				selectionEntry.setOutputPath(selectionEntry.getProjectPath().substring(0, selectionEntry.getProjectPath().lastIndexOf('/')) + javaProject.getOutputLocation().toPortableString());
	    				IPackageFragmentRoot[] fragmetnRootsTest = javaProject.getAllPackageFragmentRoots();
	    				Set<String> tempClassSet = new HashSet<String>();
	    				for(IPackageFragmentRoot root : fragmetnRootsTest) {
	    					if(root.getKind() == IPackageFragmentRoot.K_SOURCE) {
//    							System.out.println("selectionEntry.getClassPaths() ==  "+selectionEntry.getClassPaths());
	    						for(String classPath : selectionEntry.getClassPaths()) {
		    						if(classPath.contains(root.getPath().toPortableString())){
		    							tempClassSet.add(classPath.replace(root.getPath().toPortableString()+"/", "").replace("/", "."));
		    						}
	    						}

//	    						System.out.println("root.toString() ==  "+root.toString());
//	    						System.out.println("root.getPath() ==  "+root.getPath().toPortableString().replace("/", "."));
	    					}
	    				}
	    				selectionEntry.setClassPaths(tempClassSet);
	    			}  
 				selectionEntry.setProjectReportsPath(selectionEntry.getProjectPath()+reportsDirectory);
		    	}
		    }
		}catch(Exception e){
			System.err.println("Problem on Selected paths collection: "+e.getStackTrace());
		}
		return selectionEntry;
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