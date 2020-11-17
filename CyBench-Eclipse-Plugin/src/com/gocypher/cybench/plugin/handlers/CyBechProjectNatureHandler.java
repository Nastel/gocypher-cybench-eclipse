package com.gocypher.cybench.plugin.handlers;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.apt.core.internal.util.AptCorePreferenceInitializer;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;

public class CyBechProjectNatureHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
			System.out.println("--->Add CyBench Nature");
			
			System.out.println("Location of workspace:"+ResourcesPlugin.getWorkspace().getRoot().getRawLocationURI().toASCIIString() );
			
			//System.out.println("Selection:"+selection.getClass());
			IJavaProject javaProject = this.resolveJavaProject(selection) ;
			
			//IProjectNature projectNature = 
			/*boolean hasJavaNature = javaProject.getProject().hasNature("org.eclipse.jdt.core.javanature") ;
			
			boolean hasGradleNature = javaProject.getProject().hasNature("org.eclipse.buildship.core.gradleprojectnature") ;
			
			
			System.out.println("Project natures:"+hasJavaNature +";"+hasMavenNature);
			*/
			
			String cyBenchExternalsPath = LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID, false) ;
			System.out.println("Externals path:"+cyBenchExternalsPath);
			
			
			String fullPathHardcodedCore = "e:/benchmarks/eclipse_plugin/ext_libs/jmh-core-1.26.jar" ;
			String fullPathHardcodedAnnotations = "e:/benchmarks/eclipse_plugin/ext_libs/jmh-generator-annprocess-1.26.jar" ;
			
			
			this.updateDependenciesForNature(javaProject) ;
			/*
			
			//FIXME Code for adding external library , shall use path to "cyBenchExternalsPath" variable
			//Externals for real Eclipse test
			this.addAndSaveClassPathEntry(javaProject, cyBenchExternalsPath);
			
			//Externals for local tests
			//this.addAndSaveClassPathEntry(javaProject, fullPathHardcodedCore,fullPathHardcodedAnnotations);
			
			//FIXME code for update of factory path for the project
			//Externals for real Eclipse test
			this.updateProjectAPTSettings (javaProject,cyBenchExternalsPath) ;
			//Externals for local tests
			//this.updateProjectAPTSettings (javaProject,fullPathHardcodedCore,fullPathHardcodedAnnotations) ;
			
			
			
			//FIXME code for updated of project preferences in order to enable Annotation Processing
			//this.attachAptBasedPreferences(javaProject) ;
			
			
			//GuiUtils.logMessage("Externals path:"+cyBenchExternalsPath);
			*/
			
			
			
			
			
			/*System.out.println("Java Project:"+javaProject);
			for (IPackageFragmentRoot root :javaProject.getAllPackageFragmentRoots()) {
				System.out.println("Fragment root:"+root.getElementName());
				IClasspathEntry rawEntry= getClasspathEntry(root);
				System.out.println("\tClass path:"+rawEntry.getPath());
				
			}
			*/
			System.out.println("--->CyBench Nature finish");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public IClasspathEntry getClasspathEntry(IPackageFragmentRoot root) throws JavaModelException {
		IClasspathEntry rawEntry= root.getRawClasspathEntry();
		int rawEntryKind= rawEntry.getEntryKind();
		switch (rawEntryKind) {
			case IClasspathEntry.CPE_LIBRARY:
			case IClasspathEntry.CPE_VARIABLE:
			case IClasspathEntry.CPE_CONTAINER: // should not happen, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=305037
				if (root.isArchive() && root.getKind() == IPackageFragmentRoot.K_BINARY) {
					IClasspathEntry resolvedEntry= root.getResolvedClasspathEntry();
					if (resolvedEntry.getReferencingEntry() != null)
						return resolvedEntry;
					else
						return rawEntry;
				}
		}
		return rawEntry;
	}
	
	private IJavaProject resolveJavaProject (ISelection selection) {
		IJavaProject javaProject = null ;
		if (selection instanceof IStructuredSelection) {
    		IStructuredSelection ss = (IStructuredSelection) selection;
    		System.out.println(ss.getFirstElement());
    		for (Object elem : ss.toList()) {
    			if (elem instanceof IProject) {
    				javaProject = (IJavaProject)JavaCore.create((IProject)elem);
    				//IProject iproject = (IProject) elem;
    			}
    		}
		}
		return javaProject;
	}
	public void addAndSaveClassPathEntry (IJavaProject javaProject, String ...fullPathToExternalLibraries) throws Exception {
		for (String pathToExternalLib:fullPathToExternalLibraries) {
			IClasspathEntry externalJar = JavaCore.newLibraryEntry(new Path(pathToExternalLib), null, null) ;
			
			if (javaProject.getClasspathEntryFor(externalJar.getPath()) == null) { 
			
				List<IClasspathEntry>classPathEntries = new ArrayList<>() ;
				
				for (IClasspathEntry entry :javaProject.getRawClasspath()) {			
					classPathEntries.add (entry) ;
				}
				classPathEntries.add(externalJar);
				
				int i = 0 ;
				IClasspathEntry[] classPathRaw = new IClasspathEntry[classPathEntries.size()] ;
				for (IClasspathEntry item: classPathEntries) {
					classPathRaw[i] = item ;
					i++ ;
				}
							
				javaProject.setRawClasspath(classPathRaw, true, new NullProgressMonitor());
										
			}
			else {
				System.err.println("Class path entry pointing to external lib exists:"+pathToExternalLib);
			}
		}
		
	}
	public void refreshProject (IJavaProject javaProject) {
		if (javaProject != null) {
			try {
				javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			}catch (Exception e) {
				System.err.println("Error on proejct refresh:"+e.getMessage());
				e.printStackTrace();
			}
		}
		else {
			System.err.println("Unable to frefresh null project!");
		}
		
	}
	
	private void attachAptBasedPreferences (IJavaProject javaProject) throws Exception {
		ProjectScope projectScope = new ProjectScope(javaProject.getProject()) ;
		IEclipsePreferences prefs = projectScope.getNode("org.eclipse.jdt.apt.core") ;
		prefs.putBoolean("org.eclipse.jdt.apt.aptEnabled", true);
		prefs.putBoolean("org.eclipse.jdt.apt.reconcileEnabled", true);
		
		prefs.put("org.eclipse.jdt.apt.genSrcDir", "target/jmh-generated");
		prefs.put("org.eclipse.jdt.apt.genTestSrcDir", "target/jmh-generated-tests");
		prefs.flush();
		
	}
	
	private void updateProjectAPTSettings (IJavaProject javaProject, String ... pathToExternalJars) {
		AptConfig.setEnabled(javaProject, true);
		AptConfig.setGenSrcDir(javaProject, "target/jmh-generated");
		AptConfig.setGenTestSrcDir(javaProject, "target/jmh-generated-tests");
		AptConfig.setProcessDuringReconcile(javaProject, true);
		
		
		IFactoryPath factoryPath= AptConfig.getFactoryPath(javaProject) ;
	
		for (String item : pathToExternalJars) {
			factoryPath.addExternalJar(new File (item));
		}
		try {
			AptConfig.setFactoryPath(javaProject, factoryPath);
		}catch (Exception e) {
			System.err.println ("Error on update of APT factory classpath:" + e.getMessage()) ;
			e.printStackTrace();
		}
	}
	private void registerCybenchNature (IJavaProject javaProject) throws Exception{
		String cybenchNature = "com.gocypher.cybench.cybenchnature";
		IProjectDescription description = javaProject.getProject().getDescription();
		String[] natures = description.getNatureIds();
		for (String nature:natures) {
			System.out.println("Nature:"+nature);
		}
		
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = cybenchNature;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus status = workspace.validateNatureSet(newNatures);
		
		System.out.println("Nature status:"+status);
		if (status.getCode() == IStatus.OK) {
		    description.setNatureIds(newNatures);
		    javaProject.getProject().setDescription(description, null);
		}		
	}
	private void updateDependenciesForNature (IJavaProject javaProject) throws Exception{
		boolean hasMavenNature = javaProject.getProject().hasNature("org.eclipse.m2e.core.maven2Nature") ;
		if (hasMavenNature) {
			String projectLocation = javaProject.getProject().getLocation().toPortableString() ;
			System.out.println("Selected project location:"+projectLocation);
			List<File> files = CybenchUtils.listFilesInDirectory(projectLocation) ;
			File pomXML = null ;
			for (File file:files) {
				if ("pom.xml".equals(file.getName())){
					pomXML = file ;
				}
			}
			if (pomXML != null) {
				System.out.println("POM file found:"+pomXML.getAbsolutePath());
				MavenXpp3Reader reader = new MavenXpp3Reader();
				Model model = reader.read(new FileReader(pomXML)) ;
				System.out.println("Pom model:"+model.getDependencies());
			}
			
			
			
		}
	}
   

}
