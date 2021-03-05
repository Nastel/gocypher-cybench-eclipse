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

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;

public class CyBenchUpdateDependencies extends AbstractHandler {

	public  static final String FACTORY_PATH = "/.factorypath";
	public  static final String FACTORY_ENTRY_NAME = "factorypathentry";
	
	private IProject project;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
			IJavaProject javaProject = GuiUtils.resolveJavaProject(selection);
			if(javaProject != null){
				this.project = javaProject.getProject();
				GuiUtils.logInfo("-->Updating CyBench Nature:"+this.project);
				String cyBenchExternalsPath = LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID, false) ;
				if (!LauncherUtils.isMavenProject(javaProject.getProject()) &&  !LauncherUtils.isGradleProject(javaProject.getProject())) {
					this.removeOldAndAddNewClassPathEntry(javaProject, cyBenchExternalsPath);
				}
				this.configureProjectAPTSettings (javaProject,cyBenchExternalsPath) ;
				if (javaProject != null) {
					GuiUtils.refreshProject(javaProject);
				}
				GuiUtils.logInfo("--->CyBench nature update finish");
			}
		}catch (Exception e) {	
			GuiUtils.logError ("Error on project nature update",e);
			throw new ExecutionException(null);
		}
		return null;
	}


	private void removeOldAndAddNewClassPathEntry (IJavaProject javaProject, String ...fullPathToExternalLibraries) throws Exception{
		try {
		List<IClasspathEntry>classPathEntries = new ArrayList<>() ;
		for (IClasspathEntry entry :javaProject.getRawClasspath()) {
			if (!entry.getPath().toPortableString().contains(Activator.EXTERNALS_PLUGIN_ID)) {
				classPathEntries.add (entry) ;
			}
		}
		int i = 0 ;
		IClasspathEntry[] classPathRaw = new IClasspathEntry[classPathEntries.size()] ;
		for (IClasspathEntry item: classPathEntries) {
			classPathRaw[i] = item ;
			i++ ;
		}	
		javaProject.setRawClasspath(classPathRaw, true, new NullProgressMonitor());
		GuiUtils.addAndSaveClassPathEntry(javaProject, fullPathToExternalLibraries);
		}catch(Exception e){
			GuiUtils.logError ("Error on configuring classpath settings for projectt",e);
			e.printStackTrace();
		}
	}
	
	private void configureProjectAPTSettings (IJavaProject javaProject, String ... pathToExternalJars){
		try {		
			AptConfig.setEnabled(javaProject, false);
			AptConfig.setProcessDuringReconcile(javaProject, false);
			
//			AptConfig.setEnabled(javaProject, true);	
			if (LauncherUtils.isMavenProject(javaProject.getProject())) {
				AptConfig.setGenSrcDir(javaProject, "jmh-generated-tests");
			}else if(LauncherUtils.isGradleProject(javaProject.getProject())) {
				AptConfig.setGenSrcDir(javaProject, "jmh-generated-tests");
			}
			else {
				AptConfig.setGenSrcDir(javaProject, "jmh-generated-tests");
			}	
			AptConfig.setProcessDuringReconcile(javaProject, true);
			IFactoryPath factoryPath= AptConfig.getFactoryPath(javaProject) ;
			List<String> externalJarFiles = getAListOfFactoryPaths();
			if(externalJarFiles != null) {
				for(String externalFactoryPath : externalJarFiles) {
					if(externalFactoryPath.contains(Activator.EXTERNALS_PLUGIN_ID)) {
						File tempExternalJarPath = new File(externalFactoryPath);
						factoryPath.removeExternalJar(tempExternalJarPath);
					}
				}
			}
			for (String item : pathToExternalJars) {
				factoryPath.addExternalJar(new File (item));
			}
			
			AptConfig.setFactoryPath(javaProject, factoryPath);
			AptConfig.setEnabled(javaProject, true);
			AptConfig.setProcessDuringReconcile(javaProject, true);
		}catch(Exception e){
			GuiUtils.logError ("Error on configuring APT settings for project",e);
			e.printStackTrace();
		}
		
	}

	private List<String> getAListOfFactoryPaths() {
		List<String> externalJarFiles =  new ArrayList<String>();
		if(project != null){
			File file = new File(project.getLocation().toOSString()+FACTORY_PATH);
			try {
				 SAXBuilder saxBuilder = new SAXBuilder();
		         Document document = saxBuilder.build(file);
		         Element classElement = document.getRootElement();
		         List<?> factoryPathList = classElement.getChildren();
		         for (int temp = 0; temp < factoryPathList.size(); temp++) {    
		             Element factoryPath = (Element) factoryPathList.get(temp);
		             if(factoryPath.getName().equals(FACTORY_ENTRY_NAME)) {
			             Attribute attribute =  factoryPath.getAttribute("id");
			             externalJarFiles.add(attribute.getValue());
		             }
		          }
			} catch (Exception e) {
				GuiUtils.logError ("Error on reading .factory file",e);
				e.printStackTrace();
			}
		}
		return externalJarFiles;
	}

}
