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

package com.gocypher.cybench.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.buildship.core.BuildConfiguration;
import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.GradleCore;
import org.eclipse.buildship.core.GradleWorkspace;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;

public class CyBenchProjectNature implements IProjectNature {
	public  static final String NATURE_ID = "com.gocypher.cybench.plugin.tools.cybenchnature";

	private static final String JMH_GROUP_ID="org.openjdk.jmh" ;
	private static final String JMH_CORE_ARTIFACT_ID="jmh-core" ;
	private static final String JMH_ANNOTATIONDS_ARTIFACT_ID="jmh-generator-annprocess" ;
	private static final String JMH_VERSION = "1.26" ;
	
	//private static final String fullPathHardcodedCore = "e:/benchmarks/eclipse_plugin/ext_libs/jmh-core-1.26.jar" ;
	//private static final String fullPathHardcodedAnnotations = "e:/benchmarks/eclipse_plugin/ext_libs/jmh-generator-annprocess-1.26.jar" ;
	
	private IProject project;
	
	@Override
	public void configure() throws CoreException {
		GuiUtils.logInfo("-->Configuring CyBench nature for:"+this.project);
		
		IJavaProject javaProject = (IJavaProject)JavaCore.create(this.project);
	
		String cyBenchExternalsPath = LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID, false) ;
		GuiUtils.logInfo("Externals path:"+cyBenchExternalsPath);
		
		try {
			this.updateDependenciesForNature(javaProject) ;
			
			if (!LauncherUtils.isMavenProject(javaProject.getProject()) &&  !LauncherUtils.isGradleProject(javaProject.getProject())) {
				//Externals for real Eclipse test
				GuiUtils.addAndSaveClassPathEntry(javaProject, cyBenchExternalsPath);
				//Externals for local tests
				//this.addAndSaveClassPathEntry(javaProject, fullPathHardcodedCore,fullPathHardcodedAnnotations);
			}
			this.createBenchmarksSrcFolder(javaProject);	
	
			//Externals for real Eclipse test
			this.configureProjectAPTSettings (javaProject,cyBenchExternalsPath) ;
			//Externals for local tests
			//this.configureProjectAPTSettings (javaProject,fullPathHardcodedCore,fullPathHardcodedAnnotations) ;
		}catch (Exception e) {
			GuiUtils.logError ("Error during configure of CyBench nature:",e) ;
		}
			
	}

	@Override
	public void deconfigure() throws CoreException {
		GuiUtils.logInfo("-->Deconfigure CyBench nature for project:"+this.project);
		IJavaProject javaProject = (IJavaProject)JavaCore.create(this.project);
		
		String cyBenchExternalsPath = LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID, false) ;
		GuiUtils.logInfo("Externals path:"+cyBenchExternalsPath);
		
		
		//String fullPathHardcodedCore = "e:/benchmarks/eclipse_plugin/ext_libs/jmh-core-1.26.jar" ;
		//String fullPathHardcodedAnnotations = "e:/benchmarks/eclipse_plugin/ext_libs/jmh-generator-annprocess-1.26.jar" ;
		try {
			//FIXME uncomment this for production usage Externals for real Eclipse test
			this.deconfigureAptSettings (javaProject,cyBenchExternalsPath) ;
			//FIXME comment this for production usage Externals for local tests
			//this.deconfigureAptSettings(javaProject, fullPathHardcodedCore,fullPathHardcodedAnnotations);
			if (!LauncherUtils.isMavenProject(javaProject.getProject()) && !LauncherUtils.isGradleProject(javaProject.getProject())) {
				//FIXME uncomment this for production usage
				this.removeAndSaveClassPathEntry(javaProject, cyBenchExternalsPath);
				//FIXME comment this for production usage 
				//this.removeAndSaveClassPathEntry(javaProject, fullPathHardcodedCore,fullPathHardcodedAnnotations);
			}
			GuiUtils.refreshProject(javaProject);
			
		}catch (Exception e) {			
			GuiUtils.logError("Error during deconfigure of CyBench nature", e);
			throw new CoreException(null);
		}
	}

	@Override
	public IProject getProject() {		
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
		
	}
	
	private void createBenchmarksSrcFolder (IJavaProject javaProject) throws Exception {
		
		IClasspathEntry srcFolder = JavaCore.newSourceEntry(LauncherUtils.getSourceFolderForBenchmarks(javaProject.getProject())) ;
		java.nio.file.Path path = FileSystems.getDefault().getPath(srcFolder.getPath().toPortableString());

		if (LauncherUtils.isMavenProject(javaProject.getProject()) || LauncherUtils.isGradleProject(javaProject.getProject())) {
			path = FileSystems.getDefault().getPath(javaProject.getProject().getLocation().toPortableString()+LauncherUtils.SRC_FOLDER_FOR_BENCHMARKS_MVN);
		}
		if (!Files.exists(path)) {
			GuiUtils.logInfo("SRC folder for benchmarks does not exist, will add new one.");
			boolean occurenceFound = false;
			List<IClasspathEntry>classPathEntries = new ArrayList<>() ;
			for (IClasspathEntry entry :javaProject.getRawClasspath()) {	
				classPathEntries.add (entry) ;
				if(entry.getPath().equals(srcFolder.getPath())){
					occurenceFound = true;
				}
			}
			if(!occurenceFound){
				classPathEntries.add(srcFolder);
			}
			int i = 0 ;
			IClasspathEntry[] classPathRaw = new IClasspathEntry[classPathEntries.size()] ;
			for (IClasspathEntry item: classPathEntries) {
				classPathRaw[i] = item ;
				i++ ;
			}
			javaProject.setRawClasspath(classPathRaw, true, new NullProgressMonitor());
		}
		else {
			GuiUtils.logInfo("SRC folder for benchmarks exist.");
		}
		
		IPath projectPath = javaProject.getProject().getLocation() ;
		if (LauncherUtils.isMavenProject(javaProject.getProject()) || LauncherUtils.isGradleProject(javaProject.getProject())) {
			projectPath = projectPath.append(LauncherUtils.SRC_FOLDER_FOR_BENCHMARKS_MVN) ;
		}
		else {
			projectPath = projectPath.append(LauncherUtils.SRC_FOLDER_FOR_BENCHMARKS_JAVA) ;
		}
		File rawFolder = new File(projectPath.toPortableString()) ;
		GuiUtils.logInfo("-->Raw path on FS:"+projectPath.toPortableString()+"; Existence on FS:"+rawFolder.exists());
		if (!rawFolder.exists()) {
			rawFolder.mkdirs();
		}
		
	}
	private void removeAndSaveClassPathEntry (IJavaProject javaProject, String ...fullPathToExternalLibraries) throws Exception{
		List<IClasspathEntry>classPathEntries = new ArrayList<>() ;
		for (IClasspathEntry entry :javaProject.getRawClasspath()) {
			boolean found = false ;
			for (String externalItem:fullPathToExternalLibraries) {
				//System.out.println("Classpath:"+entry.getPath().toPortableString()+";"+externalItem);
				if (entry.getPath().toPortableString().equalsIgnoreCase(externalItem)) {
					found = true ;
				}
			}
			if (!found) {
				classPathEntries.add (entry) ;
			}
		}
		

		IClasspathEntry[] classPathRaw = classPathEntries.toArray(new IClasspathEntry[0]);
//		int i = 0 ;
//		IClasspathEntry[] classPathRaw = new IClasspathEntry[classPathEntries.size()] ;
//		for (IClasspathEntry item: classPathEntries) {
//			classPathRaw[i] = item ;
//			i++ ;
//		}
					
		javaProject.setRawClasspath(classPathRaw, true, new NullProgressMonitor());
		
	}
	
	private void configureProjectAPTSettings (IJavaProject javaProject, String ... pathToExternalJars) throws Exception {
		AptConfig.setEnabled(javaProject, true);	
		if (LauncherUtils.isMavenProject(javaProject.getProject())) {
			AptConfig.setGenSrcDir(javaProject, "jmh-generated");
			AptConfig.setGenTestSrcDir(javaProject, "jmh-generated-tests");
		}else if(LauncherUtils.isGradleProject(javaProject.getProject())) {
			AptConfig.setGenSrcDir(javaProject, "jmh-generated");
			AptConfig.setGenTestSrcDir(javaProject, "jmh-generated-tests");
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
	private void deconfigureAptSettings (IJavaProject javaProject,String ... pathToExternalJars)throws Exception {
		AptConfig.setEnabled(javaProject, false);
		AptConfig.setProcessDuringReconcile(javaProject, false);
		//AptConfig.setGenSrcDir(javaProject, null);
		//AptConfig.setGenTestSrcDir(javaProject, null);
		
		IFactoryPath factoryPath= AptConfig.getFactoryPath(javaProject) ;
		
		for (String item : pathToExternalJars) {
			factoryPath.removeExternalJar(new File (item));
		}
		AptConfig.setFactoryPath(javaProject, factoryPath);
		
		
	}
	
	private void updateDependenciesForNature (IJavaProject javaProject) throws Exception{
		
		if (LauncherUtils.isMavenProject(javaProject.getProject())) {
			String projectLocation = javaProject.getProject().getLocation().toPortableString() ;
			GuiUtils.logInfo("Selected maven project location:"+projectLocation);
			List<File> files = CybenchUtils.listFilesInDirectory(projectLocation) ;
			File pomXML = null ;
			for (File file:files) {
				if (!file.getAbsolutePath().contains("target") && "pom.xml".equals(file.getName())){
					pomXML = file ;
				}
			}
			if (pomXML != null) {
				GuiUtils.logInfo("POM file found:"+pomXML.getAbsolutePath());
				MavenXpp3Reader reader = new MavenXpp3Reader();
				Model model = reader.read(new FileReader(pomXML)) ;
				GuiUtils.logInfo("Pom model:"+model.getDependencies());
								
				for (Dependency dep:createCyBenchMvnDependencies(model.getDependencies())) {
					GuiUtils.logInfo("will add new dependency:"+dep);
					model.addDependency(dep);
				}
				
				GuiUtils.logInfo("Will write model to file:"+pomXML.getAbsolutePath());
				MavenXpp3Writer writer = new MavenXpp3Writer() ;
				writer.write(new FileOutputStream(pomXML), model);
				GuiUtils.logInfo("POM file updated successfully!");
			}
		} else if(LauncherUtils.isGradleProject(javaProject.getProject())) {
		    String jmhDependency = LauncherUtils.GRADLE_JMH_DEPENDENCY;
		    String jmhAnnotationDependency = LauncherUtils.GRADLE_JMH_ANNOTATION_DEPENDENCY;
		    String projectLocation = javaProject.getProject().getLocation().toPortableString() ;
			GuiUtils.logInfo("Selected gradle project location:"+projectLocation);
			List<File> files = CybenchUtils.listFilesInDirectory(projectLocation) ;
			File gradleBuild = null ;
			String fileName ="build.gradle";
			for (File file:files) {
				if (fileName.equals(file.getName())){
					gradleBuild = file ;
				}
			}
			if (gradleBuild != null) { 
				GuiUtils.logInfo("Gradle build file found:"+gradleBuild.getAbsolutePath());
				String newBuildFile = "";
			    Scanner myReader = new Scanner(gradleBuild);
			    while (myReader.hasNextLine()) {
					String data = myReader.nextLine();
					if (data.contains("org.openjdk.jmh") && data.contains("jmh-core")){
						jmhDependency = "";
					}
					if (data.contains("org.openjdk.jmh") && data.contains("jmh-generator-annprocess")){
						jmhAnnotationDependency = "";
					}
			    }
				myReader.close();
			    myReader = new Scanner(gradleBuild);
			    while (myReader.hasNextLine()) {
					String data = myReader.nextLine();
					newBuildFile += data + "\n";
					if (data.contains("dependencies {")){
						newBuildFile += jmhDependency + jmhAnnotationDependency;
				    }
				}
				myReader.close();
				FileWriter fw_build = new FileWriter(projectLocation+"\\"+fileName);
				fw_build.write(newBuildFile);
				fw_build.close();
				Job job = new Job("Gradle dependency refresh") {
				    @Override
				    protected IStatus run(IProgressMonitor monitor) {
			            BuildConfiguration configuration = BuildConfiguration
								.forRootProjectDirectory(new File(projectLocation))
							    .overrideWorkspaceConfiguration(true)
							    .autoSync(true)
							    .build();
							GradleWorkspace workspace = GradleCore.getWorkspace();
							GradleBuild newBuild = workspace.createBuild(configuration);
							//TODO: get the progress monitor and set instead of null
						newBuild.synchronize(monitor);
				        return Status.OK_STATUS;
				    }

				};
				job.schedule();
			}
			GuiUtils.logInfo("gradle.build file updated successfully!");
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
	

}
