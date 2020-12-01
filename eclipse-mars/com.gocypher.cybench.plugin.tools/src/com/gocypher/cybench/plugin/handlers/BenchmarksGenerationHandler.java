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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import com.gocypher.cybench.plugin.model.BenchmarkMethodModel;
import com.gocypher.cybench.plugin.model.RunSelectionEntry;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;
import com.gocypher.cybench.plugin.views.MessageDialogView;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;


public class BenchmarksGenerationHandler extends AbstractHandler {
	
	List<BenchmarkMethodModel> benchmarkMethods = new ArrayList<BenchmarkMethodModel>();
	boolean generationMethodsSelected = false;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		JCodeModel codeModelInstance = new JCodeModel();
		BenchmarkMethodModel model =  new BenchmarkMethodModel();
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
		RunSelectionEntry selectionEntry = LauncherUtils.fillRunselectionData(selection);
	
    	try {
        	String outputPath= LauncherUtils.getRawSourceFolderForBenchmarks(selectionEntry.getProjectSelected());
    		for(String packagePath :  selectionEntry.getClassPaths()) {
    			IWorkbenchPage page = window.getActivePage();
	    		File file = new File(outputPath);
				File fileExists = new File(outputPath+"/"+packagePath.replaceAll("\\.", "/")+"Benchmarks.java");
				File fileExists2 = new File(outputPath+"/"+packagePath.replaceAll("\\.", "/")+".java");
				benchmarkMethods = new ArrayList<BenchmarkMethodModel>();
				chooseMethodsToGenerateBenchmarks(selection, selectionEntry, packagePath);
	    		if(fileExists!= null && !fileExists.exists() && fileExists2 != null && !fileExists2.exists() && generationMethodsSelected) { 
	    			JDefinedClass generationClass;
		    		generationClass = codeModelInstance._class(packagePath+"Benchmarks");
		    		generationClass.annotate(codeModelInstance.ref(State.class)).param("value", Scope.Benchmark);
		        	/*---------------- SET UP METHODS -------------------------*/
		    		model.setMethodType(void.class);
		    		model.setMethodName("setUp");
		    		model.setMethodHint("//TODO Trial level: write code to be executed before each run of the benchmark");
		    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 0);
		    		model.setMethodName("setUpIteration");
		    		model.setMethodHint("//TODO Iteration level: write code to be executed before each iteration of the benchmark.");
		    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 2);
		    		
	
		        	/*---------------- BENCHMARK METHODS -------------------------*/
		        	for(BenchmarkMethodModel methodModelEntry : benchmarkMethods) {
			    		generateBenchmarkMethod(generationClass, codeModelInstance, methodModelEntry);
		        	}
	
		        	/*---------------- TEAR DOWN METHODS -------------------------*/
		    		model.setMethodType(void.class);
		        	model.setMethodName("cleanUp");
		    		model.setMethodHint("//TODO Trial level: write code to be executed after each run of the benchmark");
		    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 1);
		    		model.setMethodName("cleanUpIteration");
		    		model.setMethodHint("//TODO Iteration level: write code to be executed after each iteration of the benchmark.");
		    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 3);
	
					codeModelInstance.build(file);
	    		}
	    		if(fileExists!= null && fileExists.exists()) {
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileExists.toURI());
					IDE.openEditorOnFileStore(page, fileStore);
	    		}
    		}
		    IProject project = selectionEntry.getProjectSelected();
	        if(project != null) {
	        	IJavaProject javaProject = (IJavaProject)JavaCore.create((IProject)project);
	        	GuiUtils.refreshProject(javaProject);
	        }		
		} catch (Exception e) {
			GuiUtils.logError ("JAVA Code generation error",e);
		}
		return null;
	}
	
	private JMethod generateBenchmarkMethod(JDefinedClass generationClass, JCodeModel codeModelInstance, BenchmarkMethodModel model) {
		JMethod benchmark = generationClass.method(1, model.getMethodType(), model.getMethodName());
		benchmark.param(Blackhole.class, "bh");
		benchmark.annotate(codeModelInstance.ref(Benchmark.class));
		benchmark.annotate(codeModelInstance.ref(BenchmarkMode.class)).param("value", Mode.deepValueOf(model.getMethodBenchmarkMode()));
		benchmark.annotate(codeModelInstance.ref(OutputTimeUnit.class)).param("value", TimeUnit.SECONDS);
		benchmark.body().directStatement(model.getMethodHint());
		return benchmark;
	}
	
	/**
	 * 
	 * @param generationClass
	 * @param codeModelInstance
	 * @param model
	 * @param methodType - Represents the methods by number from 0 to 3. 0 - set up,  1 -tear down
	 * @return
	 */
	private JMethod generateGeneralBenchmarkMethods(JDefinedClass generationClass, JCodeModel codeModelInstance, BenchmarkMethodModel model, int methodType) {
		JMethod method = generationClass.method(1, model.getMethodType(), model.getMethodName()); 
		method.body().directStatement(model.getMethodHint());
		switch(methodType) {
		case 0:
			method.annotate(codeModelInstance.ref(Setup.class)).param("value", Level.Trial);
		break;
		case 1:
			method.annotate(codeModelInstance.ref(TearDown.class)).param("value", Level.Trial);
		break;
		case 2:
			method.annotate(codeModelInstance.ref(Setup.class)).param("value", Level.Iteration);
		break;
		case 3:
			method.annotate(codeModelInstance.ref(TearDown.class)).param("value", Level.Iteration);
		break;
		default:
			System.out.println("Method type not recognized, will skip");
			return null;
		}
		
		return method;
	} 

	
	private List<BenchmarkMethodModel> methodDetection(IStructuredSelection selection, RunSelectionEntry selectionEntry, String classPath) {
		try {
	        IProject project = selectionEntry.getProjectSelected();
	        if(project != null) {
	        	IJavaProject javaProject = (IJavaProject)JavaCore.create((IProject)project);
		    	IType itype = javaProject.findType(classPath);
		    	if(itype != null) {
			    	IMethod[] allMethods = itype.getMethods();
			    	Set<String> methodNames = new HashSet<String>();
			    	for(IMethod methodDataObject: allMethods) {
			    		int flags = methodDataObject.getFlags();
			    		if(Flags.isPublic(flags) || Flags.isProtected(flags) || Flags.isPackageDefault(flags)) {
				    		methodNames.add(methodDataObject.getElementName()+"Benchmark");
			    		}
//			    		GuiUtils.logInfo(String.valueOf("methodDataObject.getElementType(): "+methodDataObject.getElementType()));
//			    		GuiUtils.logInfo(String.valueOf("methodDataObject.isReadOnly(): "+methodDataObject.isReadOnly()));
//			    		GuiUtils.logInfo(String.valueOf("methodDataObject.getDeclaringType(): "+methodDataObject.getDeclaringType()));
//			    		GuiUtils.logInfo(String.valueOf("methodDataObject.getElementType(): "+methodDataObject.getFlags()));
//			    		GuiUtils.logInfo(String.valueOf("methodDataObject.getElementType(): "+methodDataObject.getParameters()));
			    	}
			    	for(String name : methodNames) {
			    		BenchmarkMethodModel model = new BenchmarkMethodModel();
			    		model.setMethodName(name);
			    		model.setMethodBenchmarkMode("Throughput");
			    		model.setMethodType(void.class);
			    		model.setMethodHint("//TODO fill up benchmark method with logic");
			    		benchmarkMethods.add(model);
			    	}
		    	}
	        }
			
		} catch (JavaModelException e) {		
			GuiUtils.logError ("JAVA Code generation error, method detection problem",e);
		}
		return benchmarkMethods; 
	}

	
    public void chooseMethodsToGenerateBenchmarks (IStructuredSelection selection, RunSelectionEntry selectionEntry, String classPath) {
    	benchmarkMethods = methodDetection(selection, selectionEntry, classPath);
    	Display.getDefault().syncExec(new Runnable() {
		    public void run() {
		    	try {
		    		Shell generateMethods = new Shell();
		    		MessageDialogView pop = new MessageDialogView(generateMethods, benchmarkMethods);
					if (pop.open() == Window.OK) {
						GuiUtils.logInfo("Popup Return CODE: "+0);
						generationMethodsSelected = true;
						benchmarkMethods = pop.getMethodsToGenerate();
					}else {
						GuiUtils.logInfo("Popup Return CODE: "+1);
						generationMethodsSelected = false;
					}
		    	}catch (Exception e) {			    		
		    		GuiUtils.logError ("Error  on view open",e) ;
		    	}
		    }
		});	
    }
}
