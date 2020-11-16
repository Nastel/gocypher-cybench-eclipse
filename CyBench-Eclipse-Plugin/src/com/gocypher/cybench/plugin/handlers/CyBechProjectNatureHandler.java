package com.gocypher.cybench.plugin.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
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

import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;

public class CyBechProjectNatureHandler implements ILaunchShortcut{

	@Override
	public void launch(ISelection selection, String mode) {
		try {
			System.out.println("--->Add CyBench Nature");
			
			System.out.println("Location of workspace:"+ResourcesPlugin.getWorkspace().getRoot().getRawLocationURI().toASCIIString() );
			
			//System.out.println("Selection:"+selection.getClass());
			IJavaProject javaProject = this.resolveJavaProject(selection) ;
			
			String cyBenchExternalsPath = LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID, false) ;
			
			
			
			String fullPathHardcodedCore = "e:/benchmarks/eclipse_plugin/ext_libs/jmh-core-1.26.jar" ;
			String fullPathHardcodedAnnotations = "e:/benchmarks/eclipse_plugin/ext_libs/jmh-generator-annprocess-1.26.jar" ;
			
			
			//FIXME Code for adding external library , shall use path to "cyBenchExternalsPath" variable
			this.addAndSaveClassPathEntry(javaProject, fullPathHardcodedCore);
			this.addAndSaveClassPathEntry(javaProject, fullPathHardcodedAnnotations);
			
			System.out.println("Externals path:"+cyBenchExternalsPath);
			
			//FIXME code for updated of project preferences in order to enable Annotation Processing
			//this.attachAptBasedPreferences(javaProject) ;
			
			
			//FIXME code for update of factory path for the project
			Map<String, String> props = AptConfig.getProcessorOptions(javaProject, false) ;
			System.out.println("APT props:"+props);
			
			AptConfig.setEnabled(javaProject, true);
			AptConfig.setGenSrcDir(javaProject, "target/jmh-generated");
			AptConfig.setGenTestSrcDir(javaProject, "target/jmh-generated-tests");
			AptConfig.setProcessDuringReconcile(javaProject, true);
			
			
			System.out.println("Raw Processor options:"+AptConfig.getRawProcessorOptions(javaProject)) ;
			IFactoryPath factoryPath= AptConfig.getFactoryPath(javaProject) ;
		
			
			factoryPath.addExternalJar(new File (fullPathHardcodedCore));
			factoryPath.addExternalJar(new File (fullPathHardcodedAnnotations));
			
			AptConfig.setFactoryPath(javaProject, factoryPath);
			System.out.println("Factory path:"+factoryPath);
			
			
			
			//GuiUtils.logMessage("Externals path:"+cyBenchExternalsPath);
			
			
			
			
			
			
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
	}
	
	
	@Override
	public void launch(IEditorPart arg0, String arg1) {
		// TODO Auto-generated method stub
		
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
	public void addAndSaveClassPathEntry (IJavaProject javaProject, String fullPathToExternalLibrary) throws Exception {
		
		IClasspathEntry externalJar = JavaCore.newLibraryEntry(new Path(fullPathToExternalLibrary), null, null) ;
		
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
			System.err.println("Class path entry pointing to external lib exists:"+fullPathToExternalLibrary);
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
	
	public void attachAptBasedPreferences (IJavaProject javaProject) throws Exception {
		ProjectScope projectScope = new ProjectScope(javaProject.getProject()) ;
		IEclipsePreferences prefs = projectScope.getNode("org.eclipse.jdt.apt.core") ;
		prefs.putBoolean("org.eclipse.jdt.apt.aptEnabled", true);
		prefs.putBoolean("org.eclipse.jdt.apt.reconcileEnabled", true);
		
		prefs.put("org.eclipse.jdt.apt.genSrcDir", "target/jmh-generated");
		prefs.put("org.eclipse.jdt.apt.genTestSrcDir", "target/jmh-generated-tests");
		prefs.flush();
		
	}
   

}
