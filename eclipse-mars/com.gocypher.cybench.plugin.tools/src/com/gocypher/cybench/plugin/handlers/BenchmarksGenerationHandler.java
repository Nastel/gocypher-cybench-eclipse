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
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
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

	boolean testProp = false;
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
				benchmarkMethods = new ArrayList<BenchmarkMethodModel>();
				if(shouldgenerateFile(fileExists, selectionEntry, outputPath, packagePath)) {
					chooseMethodsToGenerateBenchmarks(selection, selectionEntry, packagePath);
					if(generationMethodsSelected) {
		    			JDefinedClass generationClass;
			    		generationClass = codeModelInstance._class(packagePath+"Benchmarks");
			    		generationClass.annotate(codeModelInstance.ref(State.class)).param("value", Scope.Benchmark);
			        	/*---------------- SETUP METHODS -------------------------*/
			    		model.setMethodType(void.class);
			    		model.setMethodName("setup");
			    		model.setMethodHint("//TODO Trial level: write code to be executed before each run of the benchmark");
			    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 0);
			    		model.setMethodName("setupIteration");
			    		model.setMethodHint("//TODO Iteration level: write code to be executed before each iteration of the benchmark.");
			    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 2);
			    		
		
			        	/*---------------- BENCHMARK METHODS -------------------------*/
			        	for(BenchmarkMethodModel methodModelEntry : benchmarkMethods) {
				    		generateBenchmarkMethod(generationClass, codeModelInstance, methodModelEntry);
			        	}
		
			        	/*---------------- TEARDOWN METHODS -------------------------*/
			    		model.setMethodType(void.class);
			        	model.setMethodName("teardown");
			    		model.setMethodHint("//TODO Trial level: write code to be executed after each run of the benchmark");
			    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 1);
			    		model.setMethodName("teardownIteration");
			    		model.setMethodHint("//TODO Iteration level: write code to be executed after each iteration of the benchmark.");
			    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 3);
		
						codeModelInstance.build(file);
					}
	    		}
				
	    		if(fileExists!= null && fileExists.exists()) {
	    			IProject project = selectionEntry.getProjectSelected();
	  		        if(project != null) {
	  		        	IJavaProject javaProject = (IJavaProject)JavaCore.create((IProject)project);
	  		        	GuiUtils.refreshProject(javaProject);
	  		        }
					URI uri = fileExists.toURI();
					IEditorDescriptor desc = getEditorDescriptor(uri);
					String editorId = (desc == null) ? IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID : desc.getId();
					String projectPath = selectionEntry.getProjectPath().replace("/", "\\");
					String pathToOpen = fileExists.toString().replace(projectPath, "").substring(1);
	    			IPath path = new Path(pathToOpen);
	    			IFile fileToUse = selectionEntry.getProjectSelected().getFile(path);
					page.openEditor(new FileEditorInput(fileToUse), editorId);
	    		}
    		}
		  	
		} catch (Exception e) {
			GuiUtils.logError ("JAVA Code generation error",e);
		}
		return null;
	}
	
	@Override
	public boolean isEnabled() {
		return testProp;
	}
	
	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		try {
			if (evaluationContext instanceof IEvaluationContext) {
				IEvaluationContext appContext = (IEvaluationContext) evaluationContext;		
				ISelection selection = (ISelection) appContext .getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);	
//				GuiUtils.logInfo ("Selection: " + selection);
				RunSelectionEntry selectionEntry = LauncherUtils.fillRunselectionData(selection);
				String outputPath = LauncherUtils.getRawSourceFolderForBenchmarks(selectionEntry.getProjectSelected());
				for(String packagePath :  selectionEntry.getClassPaths()) {
					File fileExists = new File(outputPath+"/"+packagePath.replaceAll("\\.", "/")+"Benchmarks.java");
					testProp = shouldgenerateFile(fileExists, selectionEntry, outputPath, packagePath);
				}			
//		      GuiUtils.logInfo ("selectionEntry: " + selectionEntry); 
		   }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static IEditorDescriptor getEditorDescriptor(URI uri)
	{
		// NOTE: Moved from PHP's EditorUtils
		String uriPath = uri.getPath();
		if (uriPath.isEmpty()|| uriPath.equals("/")) //$NON-NLS-1$
		{
			return null;
		}
		IPath path = new Path(uriPath);
		return PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(path.lastSegment());
	}
	
	private JMethod generateBenchmarkMethod(JDefinedClass generationClass, JCodeModel codeModelInstance, BenchmarkMethodModel model) throws ClassNotFoundException, FileNotFoundException {
		JMethod benchmark = generationClass.method(1, model.getMethodType(), model.getMethodName());
		benchmark.param(Blackhole.class, "bh");
		benchmark.annotate(codeModelInstance.ref(Benchmark.class));
		benchmark.annotate(codeModelInstance.ref(BenchmarkMode.class)).param("value", Mode.deepValueOf(model.getMethodBenchmarkMode()));
		benchmark.annotate(codeModelInstance.ref(OutputTimeUnit.class)).param("value", TimeUnit.SECONDS);

		benchmark.annotate(codeModelInstance.ref(Fork.class)).param("value", 1);
		benchmark.annotate(codeModelInstance.ref(Threads.class)).param("value", 1);
		benchmark.annotate(codeModelInstance.ref(Measurement.class)).param("iterations", 2).param("time", 5).param("timeUnit", TimeUnit.SECONDS);
		benchmark.annotate(codeModelInstance.ref(Warmup.class)).param("iterations", 1).param("time", 5).param("timeUnit", TimeUnit.SECONDS);
		
		if(model.getExceptionTypes().length>0) {
			benchmark._throws(Exception.class);
		}else {
	//		for(String exceptionType : model.getExceptionTypes()) {
	//			JClass exceptionClass =codeModelInstance.ref(exceptionType.substring(1, exceptionType.length()-1));
	//			Class c = Class.forName(exceptionType.substring(1, exceptionType.length()-1));
	//			GuiUtils.logInfo("codeModelInstance.ref(exceptionType); :   "+exceptionClass);
	//			benchmark._throws(Mode.deepValueOf(exceptionType));
	//		}
		}
//		benchmark.annotate(codeModelInstance.ref(BenchmarkTag.class)).param("tag", UUID.randomUUID().toString());
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
			GuiUtils.logInfo("Method type not recognized, will skip");
			return null;
		}
		
		return method;
	} 

	
	private void methodDetection(IStructuredSelection selection, RunSelectionEntry selectionEntry, String classPath) {
		try {
	        IProject project = selectionEntry.getProjectSelected();
	        if(project != null) {
	        	IJavaProject javaProject = (IJavaProject)JavaCore.create((IProject)project);
		    	IType itype = javaProject.findType(classPath);
		    	if(itype != null && itype.getChildren() != null) {
			    	for(IMethod methodDataObject: itype.getMethods()) {
			    		int flags = methodDataObject.getFlags();
			    		if(Flags.isPublic(flags) || Flags.isProtected(flags) || Flags.isPackageDefault(flags)) {
			    			fillBenchmarkMethodInformation(methodDataObject);
			    		}
			    	}
		    	}
	        }
		} catch (JavaModelException e) {		
			GuiUtils.logError ("JAVA Code generation error, there was a problem while detecting methods to generate. ",e);
		}
	}
	
	private void fillBenchmarkMethodInformation(IMethod methodDataObject) throws JavaModelException {
		String[] exceptionTypes = methodDataObject.getExceptionTypes();
		String[] parameterTypes = methodDataObject.getParameterTypes();
//		String[] parameterNames = methodDataObject.getParameterNames();

		BenchmarkMethodModel model = new BenchmarkMethodModel();
		model.setExceptionTypes(exceptionTypes);
		model.setParameterTypes(parameterTypes);
		model.setMethodName(methodDataObject.getElementName());
		model.setMethodBenchmarkMode("Throughput");
		model.setMethodType(void.class);
		model.setMethodHint("//TODO fill up benchmark method with logic");
		benchmarkMethods.add(model);
	}
	

	private boolean shouldgenerateFile(File fileExists, RunSelectionEntry selectionEntry, String outputPath, String packagePath) {
		File fileExists2 = new File(outputPath+"/"+packagePath.replaceAll("\\.", "/")+".java");
		if(fileExists!= null && !fileExists.exists() && fileExists2 != null && !fileExists2.exists() && !isFileInJavaProjectGeneratedSource(fileExists, selectionEntry)) { 
			return true;
		}else {
			return false;
		}
	}
	private boolean isFileInJavaProjectGeneratedSource(File fileExists, RunSelectionEntry selectionEntry) {
		try {
			if(LauncherUtils.isJavaProject(selectionEntry.getProjectSelected())) {
				if(fileExists.toString().split(LauncherUtils.SRC_FOLDER_FOR_BENCHMARKS_JAVA.substring(1)).length > 2) {
					return  true;
				}
			}
		} catch (Exception e) {
    		GuiUtils.logError ("Error on view open",e) ;
		}
		return  false;
	}
    public void chooseMethodsToGenerateBenchmarks (IStructuredSelection selection, RunSelectionEntry selectionEntry, String classPath) {
    	methodDetection(selection, selectionEntry, classPath);
    	Display.getDefault().syncExec(new Runnable() {
		    public void run() {
		    	try {
		    		Shell generateMethods = new Shell();
		    		MessageDialogView pop = new MessageDialogView(generateMethods, benchmarkMethods);
					if (pop.open() == Window.OK) {
						generationMethodsSelected = true;
						benchmarkMethods = pop.getMethodsToGenerate();
					}else {
						generationMethodsSelected = false;
					}
		    	}catch (Exception e) {			    		
		    		GuiUtils.logError ("Error  on view open",e) ;
		    	}
		    }
		});	
    }
}
