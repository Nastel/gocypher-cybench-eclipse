package com.gocypher.cybench.plugin.handlers;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Level;

import com.gocypher.cybench.plugin.model.BenchmarkMethodModel;
import com.gocypher.cybench.plugin.model.RunSelectionEntry;
import com.gocypher.cybench.plugin.utils.LauncherUtils;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;


public class BenchmarksGenerationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		JCodeModel codeModelInstance = new JCodeModel();
		BenchmarkMethodModel model =  new BenchmarkMethodModel();
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
		RunSelectionEntry selectionEntry = LauncherUtils.fillRunselectionData(selection);
		
    	System.out.println("getOutputPath: "+ selectionEntry.getOutputPath());
    	System.out.println("getProjectPath: "+ selectionEntry.getProjectPath());
    	System.out.println("getProjectReportsPath: "+ selectionEntry.getProjectReportsPath());
    	System.out.println("getSourcePathsWithClasses: "+ selectionEntry.getSourcePathsWithClasses());
    	System.out.println("getClassPaths: "+ selectionEntry.getClassPaths());
    	System.out.println(System.getProperty("line.separator"));
    	try {
    		for(String packagePath :  selectionEntry.getClassPaths()) {
    			String outputPath = "";
	    		JDefinedClass generationClass;
	    		generationClass = codeModelInstance._class("cybench."+packagePath);
	    		generationClass.annotate(codeModelInstance.ref(State.class)).param("value", Scope.Benchmark);
	    		model.setMethodType(void.class);
	        	System.out.println("Class annotation correct");
	        	
	        	/*---------------- SET UP METHODS -------------------------*/
	    		model.setMethodName("setUp");
	    		model.setMethodHint("//TODO Trial level: write code to be executed before each run of the benchmark");
	    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 0);
	    		model.setMethodName("setUpIteration");
	    		model.setMethodHint("//TODO Iteration level: write code to be executed before each iteration of the benchmark.");
	    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 2);
	        	System.out.println("Set up method creation correct");
	        	
	    		

	        	/*---------------- BENCHMARK METHODS -------------------------*/
	    		model.setMethodBenchmarkMode("Throughput");
	    		model.setMethodName("customBenchmark");
	    		model.setMethodHint("//TODO fill up benchmark method with logic");
	    		generateBenchmarkMethod(generationClass, codeModelInstance, model);
	        	System.out.println("First custom benchmark method creation correct");
	        	

	    		model.setMethodBenchmarkMode("Throughput");
	    		model.setMethodName("customBenchmark1");
	    		model.setMethodHint("//TODO fill up benchmark method with logic");
	    		generateBenchmarkMethod(generationClass, codeModelInstance, model);
	        	System.out.println("Second custom benchmark method creation correct");
	    		
	    		model.setMethodBenchmarkMode("Throughput");
	    		model.setMethodName("customBenchmark2");
	    		model.setMethodHint("//TODO fill up benchmark method with logic");
	    		generateBenchmarkMethod(generationClass, codeModelInstance, model);
	        	System.out.println("Third custom benchmark method creation correct");
	        	

	        	/*---------------- TEAR DOWN METHODS -------------------------*/
	        	model.setMethodName("cleanUp");
	    		model.setMethodHint("//TODO Trial level: write code to be executed after each run of the benchmark");
	    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 1);
	    		model.setMethodName("cleanUpIteration");
	    		model.setMethodHint("//TODO Iteration level: write code to be executed after each iteration of the benchmark.");
	    		generateGeneralBenchmarkMethods(generationClass, codeModelInstance, model, 3);
	        	System.out.println("Clean up method creation correct");
	        	

	        	System.out.println("selectionEntry.getSourcePathsWithClasses().get(0): "+selectionEntry.getSourcePathsWithClasses().get(0));
	        	System.out.println("selectionEntry.getProjectName(): "+selectionEntry.getProjectName());
				if(selectionEntry.getSourcePathsWithClasses().get(0) != null) {
					outputPath = selectionEntry.getProjectPath() + selectionEntry.getSourcePathsWithClasses().get(0).replace(selectionEntry.getProjectName(), "");
				}else {
					outputPath = selectionEntry.getProjectPath();
				}
		    	System.out.println(System.getProperty("line.separator"));
	        	System.out.println("outputPath: "+outputPath);
				File file = new File(outputPath);
				file.mkdirs();

	        	System.out.println("Buildin the benchamrks class");
				codeModelInstance.build(file);
	        	System.out.println("Building was completed succesfully");
    		}
		} catch (JClassAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	private JMethod generateBenchmarkMethod(JDefinedClass generationClass, JCodeModel codeModelInstance, BenchmarkMethodModel model) {
		JMethod benchmark = generationClass.method(1, model.getMethodType(), model.getMethodName());
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

}
