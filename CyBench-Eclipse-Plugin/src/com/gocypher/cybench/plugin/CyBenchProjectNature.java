package com.gocypher.cybench.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;

public class CyBenchProjectNature implements IProjectNature {
	public  static final String NATURE_ID = "CyBenchLauncherPlugin.cybenchnature";

	private static final String JMH_GROUP_ID="org.openjdk.jmh" ;
	private static final String JMH_CORE_ARTIFACT_ID="jmh-core" ;
	private static final String JMH_ANNOTATIONDS_ARTIFACT_ID="jmh-generator-annprocess" ;
	private static final String JMH_VERSION = "1.26" ;
	
	private IProject project;
	
	@Override
	public void configure() throws CoreException {
		System.out.println("-->Configuring CyBench nature for:"+this.project);
		
		IJavaProject javaProject = (IJavaProject)JavaCore.create(this.project);
		
		String cyBenchExternalsPath = LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID, false) ;
		System.out.println("Externals path:"+cyBenchExternalsPath);
		
		
		String fullPathHardcodedCore = "e:/benchmarks/eclipse_plugin/ext_libs/jmh-core-1.26.jar" ;
		String fullPathHardcodedAnnotations = "e:/benchmarks/eclipse_plugin/ext_libs/jmh-generator-annprocess-1.26.jar" ;
		
		try {
			this.updateDependenciesForNature(javaProject) ;
			
			if (!this.isMavenProject(javaProject)) {
				//Externals for real Eclipse test
				//this.addAndSaveClassPathEntry(javaProject, cyBenchExternalsPath);
				///Externals for local tests
				this.addAndSaveClassPathEntry(javaProject, fullPathHardcodedCore,fullPathHardcodedAnnotations);
			}
				
	
			//Externals for real Eclipse test
			//this.updateProjectAPTSettings (javaProject,cyBenchExternalsPath) ;
			//Externals for local tests
			this.updateProjectAPTSettings (javaProject,fullPathHardcodedCore,fullPathHardcodedAnnotations) ;
		}catch (Exception e) {
			System.err.println("Error during set of project nature:"+e.getMessage());
			e.printStackTrace();
			throw new CoreException(null);
		}
			
	}

	@Override
	public void deconfigure() throws CoreException {
		System.out.println("-->Deconfigure CyBench nature for project:"+this.project);
		
	}

	@Override
	public IProject getProject() {		
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
		
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
	private void updateProjectAPTSettings (IJavaProject javaProject, String ... pathToExternalJars) throws Exception {
		AptConfig.setEnabled(javaProject, true);	
		if (this.isMavenProject(javaProject)) {
			AptConfig.setGenSrcDir(javaProject, "target/jmh-generated");
			AptConfig.setGenTestSrcDir(javaProject, "target/jmh-generated-tests");
		}
		else {
			AptConfig.setGenSrcDir(javaProject, "jmh-generated");
			AptConfig.setGenTestSrcDir(javaProject, "jmh-generated-tests");
		}
		AptConfig.setProcessDuringReconcile(javaProject, true);
				
		IFactoryPath factoryPath= AptConfig.getFactoryPath(javaProject) ;
	
		for (String item : pathToExternalJars) {
			factoryPath.addExternalJar(new File (item));
		}
		
		AptConfig.setFactoryPath(javaProject, factoryPath);
		
	}
	
	private void updateDependenciesForNature (IJavaProject javaProject) throws Exception{
		
		if (this.isMavenProject(javaProject)) {
			String projectLocation = javaProject.getProject().getLocation().toPortableString() ;
			System.out.println("Selected project location:"+projectLocation);
			List<File> files = CybenchUtils.listFilesInDirectory(projectLocation) ;
			File pomXML = null ;
			for (File file:files) {
				if (!file.getAbsolutePath().contains("target") && "pom.xml".equals(file.getName())){
					pomXML = file ;
				}
			}
			if (pomXML != null) {
				System.out.println("POM file found:"+pomXML.getAbsolutePath());
				MavenXpp3Reader reader = new MavenXpp3Reader();
				Model model = reader.read(new FileReader(pomXML)) ;
				System.out.println("Pom model:"+model.getDependencies());
								
				for (Dependency dep:createCyBenchMvnDependencies(model.getDependencies())) {
					System.out.println("will add new dependency:"+dep);
					model.addDependency(dep);
				}
				
				System.out.println("Will write model to file:"+pomXML.getAbsolutePath());
				MavenXpp3Writer writer = new MavenXpp3Writer() ;
				writer.write(new FileOutputStream(pomXML), model);
				System.out.println("POM file updated successfully!");
			}
		}
	}
	
	private List<Dependency>createCyBenchMvnDependencies (List<Dependency>currentDependencies){
		List<Dependency>dependencies = new ArrayList<>() ;
		if (!hasJMHDependency(currentDependencies, JMH_GROUP_ID,JMH_CORE_ARTIFACT_ID)) {
		
			Dependency core = new Dependency() ;
			core.setGroupId(JMH_GROUP_ID);
			core.setArtifactId(JMH_CORE_ARTIFACT_ID);
			core.setVersion(JMH_VERSION);
			
			dependencies.add(core) ;
		}
		if (!hasJMHDependency(currentDependencies,JMH_GROUP_ID, JMH_ANNOTATIONDS_ARTIFACT_ID)) {
			Dependency annotations = new Dependency() ;
			annotations.setGroupId(JMH_GROUP_ID);
			annotations.setArtifactId(JMH_ANNOTATIONDS_ARTIFACT_ID);
			annotations.setVersion(JMH_VERSION);
			annotations.setScope("provided");
		dependencies.add(annotations) ;
		}
				
		return dependencies ;
	}
	private boolean hasJMHDependency (List<Dependency>dependencies , String groupId, String artifacId) {
		
		for (Dependency item:dependencies) {
			if (groupId.equals(item.getGroupId()) && artifacId.equals(item.getArtifactId())  ) {
				return true ;
			}
		}
		
		return false ;
	}
	private boolean isMavenProject (IJavaProject javaProject) throws Exception{		
		if (javaProject.getProject().hasNature("org.eclipse.m2e.core.maven2Nature")) {
			return true ;
		}		
		return false ;
	}
	protected boolean isJavaProject (IJavaProject javaProject) throws Exception{		
		if (javaProject.getProject().hasNature("org.eclipse.jdt.core.javanature")) {
			return true ;
		}		
		return false ;
	}  

}
