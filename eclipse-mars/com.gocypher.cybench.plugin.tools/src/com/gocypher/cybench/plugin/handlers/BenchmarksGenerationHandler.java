package com.gocypher.cybench.plugin.handlers;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredSelection;
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

import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.model.BenchmarkMethodModel;
import com.gocypher.cybench.plugin.model.RunSelectionEntry;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;


public class BenchmarksGenerationHandler extends AbstractHandler {


	ILog log = Activator.getDefault().getLog();
	
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

	    		if(fileExists!= null && !fileExists.exists() && fileExists2 != null && !fileExists2.exists()) {
		    		GuiUtils.logInfo("-->Starting the generation of new file: "+file.getPath().toString()); 
	    			List<BenchmarkMethodModel> benchmarkMethods = methodDetection(selection, selectionEntry, packagePath);
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
    		GuiUtils.logError("Encounter a problem while generating the benchmarks file:", e);
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
    		GuiUtils.logInfo("Method type not recognized, will skip");
			return null;
		}
		return method;
	} 

	
	private List<BenchmarkMethodModel> methodDetection(IStructuredSelection selection, RunSelectionEntry selectionEntry, String classPath) {
		LinkedList<BenchmarkMethodModel> benchmarkMethods = new LinkedList<BenchmarkMethodModel>();
		try {
	        IProject project = selectionEntry.getProjectSelected();
	        if(project != null) {
	        	IJavaProject javaProject = (IJavaProject)JavaCore.create((IProject)project);
		    	IType itype = javaProject.findType(classPath);
		    	if(itype != null) {
			    	IMethod[] allMethods = itype.getMethods();
			    	Set<String> methodNames = new HashSet<String>();
			    	for(IMethod methodDataObject: allMethods) {
			    		methodNames.add(methodDataObject.getElementName()+"Benchmark");
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
    		GuiUtils.logError("Method type not recognized, will skip", e);
		}
		return benchmarkMethods; 
	}

}
