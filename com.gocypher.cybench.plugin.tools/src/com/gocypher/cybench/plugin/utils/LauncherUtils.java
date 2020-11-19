package com.gocypher.cybench.plugin.utils;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.handlers.HandlerUtil;

import com.gocypher.cybench.plugin.CyBenchProjectNature;
import com.gocypher.cybench.plugin.model.RunSelectionEntry;

public class LauncherUtils {
	public static String SRC_FOLDER_FOR_BENCHMARKS_JAVA="/src-benchmarks" ;
	public static String SRC_FOLDER_FOR_BENCHMARKS_MVN="/src/test/java" ;
	
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
			ex.printStackTrace();
		}
		return "" ;
	}
	public static MessageConsole findConsole(String name) {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (name.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      //no console found, so create a new one
	      MessageConsole myConsole = new MessageConsole(name, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	   }
	public static void showMsgBox (String msg,ExecutionEvent event) {
		try {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openInformation(
					window.getShell(),
					"CyBench plugin",
					msg);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 public static String setToString(Set<String> setData) {
    	String result ="";
    	for(String data : setData) {
    		result += data + ",";
    	}
    	return result;
    }
	public static Set<String> addClasses(IResource[] files, Set<String> selectionEntry) {
	   try {
			for(IResource file : files) {
				  if (file.getType() == IResource.FOLDER) {
					  IFolder tempFolder = (IFolder) file;
					  addClasses(tempFolder.members(), selectionEntry);
				  }else {
					 String benchmarkClass = file.getFullPath().toPortableString().replace(".java", "");
	    				System.out.println("selectedPath IProject: "+benchmarkClass);
					 selectionEntry.add(benchmarkClass);
				  }
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	   return selectionEntry; 
	}
	
	public static RunSelectionEntry fillRunselectionData(ISelection selection) {
	    String reportsDirectory = "/reports";
		RunSelectionEntry selectionEntry = new RunSelectionEntry();
		try {
		 if (selection instanceof IStructuredSelection) {
	    		IStructuredSelection ss = (IStructuredSelection) selection;
		    	for (Object elem : ss.toList()) {
		    		IProject project = null;
		    		String selectedPath = "";
//		    		System.out.println("elem: "+elem.toString());
	    			IJavaProject javaProject = null;
 				if (elem instanceof IProject) {
 						IAdaptable adaptable = (IAdaptable) elem;
	    		        IFolder folder = (IFolder)  adaptable.getAdapter(IFolder.class);
	    				project = (IProject) elem;
	    				selectionEntry.setProjectPath(project.getLocation().toString());
	    			}
	    			else if (elem instanceof IFolder) {
	    				IAdaptable adaptable = (IAdaptable) elem;
	    				IResource res = (IResource) adaptable.getAdapter(IResource.class);
	    		        IFolder folder = (IFolder)  adaptable.getAdapter(IFolder.class);
	    		        System.out.println(folder);
	    		        project = res.getProject();
	    		        selectedPath = res.getLocation().toString();
	    				selectionEntry.setProjectPath(selectedPath.replace("/"+res.getProjectRelativePath().toPortableString(), ""));
	    				selectionEntry.setClassPaths(LauncherUtils.addClasses(folder.members(), selectionEntry.getClassPaths()));
	    				System.out.println("selectedPath IFolder: "+selectionEntry.getProjectPath());
	    			}
	    			else if (elem instanceof IFile) {
	    				IAdaptable adaptable = (IAdaptable) elem;
	    				IResource res = (IResource) adaptable.getAdapter(IResource.class);
	    		        project = res.getProject();
	    		        
	    		        selectedPath = res.getLocation().toString();
	    		        String benchmarkClass = res.getFullPath().toPortableString().replace(".java", "");
	    		        selectionEntry.addClassPaths(benchmarkClass);
	    				selectionEntry.setProjectPath(selectedPath.replace("/"+res.getProjectRelativePath().toPortableString(), ""));
	    				System.out.println("selectedPath IFile: "+selectionEntry.getProjectPath());
	    			}
	    			else if (elem instanceof IAdaptable) {
	    				IAdaptable adaptable = (IAdaptable) elem;
	    				IResource res = (IResource) adaptable.getAdapter(IResource.class);
	    				if(res.getFullPath().toPortableString().endsWith(".java")) {
		    		        String benchmarkClass = res.getFullPath().toPortableString().replace(".java", "");
		    		        selectionEntry.addClassPaths(benchmarkClass);
	    				}
	    		        project = res.getProject();
	    		        selectedPath = res.getLocation().toString();
	    				selectionEntry.setProjectPath(selectedPath.replace("/"+res.getProjectRelativePath().toPortableString(), ""));
	    				System.out.println("selectedPath IAdaptable: "+selectionEntry.getProjectPath());
	    			} else {
	    				System.err.println("The run selection was not recognized: "+ selection);
	    			}

 					javaProject = (IJavaProject)JavaCore.create((IProject)project);
					selectionEntry.setProjectSelected(project);
	 				runSelectionClassesInformation(javaProject, selectionEntry);
		    		selectionEntry.setProjectName(selectionEntry.getProjectPath().substring(selectionEntry.getProjectPath().lastIndexOf('/') + 1));
	 				selectionEntry.setProjectReportsPath(selectionEntry.getProjectPath()+reportsDirectory);
		    	}
		    }
		}catch(Exception e){
			System.err.println("Problem on Selected paths collection");
			e.printStackTrace();
		}
		return selectionEntry;
	}
	
	public static void runSelectionClassesInformation(IJavaProject javaProject, RunSelectionEntry selectionEntry ) {
		try {
	    			if(javaProject!=null && javaProject.getOutputLocation()!=null) {
	    				selectionEntry.setOutputPath(selectionEntry.getProjectPath().substring(0, selectionEntry.getProjectPath().lastIndexOf('/')) + javaProject.getOutputLocation().toPortableString());
	    				IPackageFragmentRoot[] fragmetnRootsTest = javaProject.getAllPackageFragmentRoots();
	    				Set<String> tempClassSet = new HashSet<String>();
	    				for(IPackageFragmentRoot root : fragmetnRootsTest) {
	    					if(root.getKind() == IPackageFragmentRoot.K_SOURCE) {
	    						for(String classPath : selectionEntry.getClassPaths()) {
		    						if(classPath.contains(root.getPath().toPortableString())){
		    							selectionEntry.addSourcePathsWithClasses(root.getPath().toPortableString());
		    							tempClassSet.add(classPath.replace(root.getPath().toPortableString()+"/", "").replace("/", "."));
		    						}
	    						}
	    					}
	    				}
	    				selectionEntry.setClassPaths(tempClassSet);
	    			}  

		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    	}
		    }
	
	public static IPath getSourceFolderForBenchmarks (IProject project) throws Exception{
	    	if (isMavenProject(project)) {
	    		IFolder folder = project.getFolder(SRC_FOLDER_FOR_BENCHMARKS_MVN) ;
	    		return folder.getFullPath();
	    	}
	    	else {
	    		IFolder folder = project.getFolder(SRC_FOLDER_FOR_BENCHMARKS_JAVA) ;
	    		return folder.getFullPath();
	    	}
	}
	public static String getRawSourceFolderForBenchmarks (IProject project) throws Exception{
    	if (isMavenProject(project)) {
    		return project.getLocation().append(SRC_FOLDER_FOR_BENCHMARKS_MVN).toPortableString() ;
    	}
    	else {    		
    		return project.getLocation().append(SRC_FOLDER_FOR_BENCHMARKS_JAVA).toPortableString() ;
    	}
}
	
	public static boolean isMavenProject (IProject project) throws Exception{		
			if (project.hasNature("org.eclipse.m2e.core.maven2Nature")) {
				return true ;
			}		
			return false ;
	}
	public static boolean isJavaProject (IProject project) throws Exception{		
			if (project.hasNature("org.eclipse.jdt.core.javanature")) {
				return true ;
			}		
			return false ;
		}
	public static boolean isCyBenchProject (IProject project) throws Exception{		
			if (project.hasNature(CyBenchProjectNature.NATURE_ID)) {
				return true ;
			}		
			return false ;
	}
//	
}