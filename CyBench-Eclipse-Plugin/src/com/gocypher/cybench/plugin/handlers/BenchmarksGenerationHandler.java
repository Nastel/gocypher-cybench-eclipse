package com.gocypher.cybench.plugin.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JMethod;

import org.openjdk.jmh.annotations.Benchmark;


public class BenchmarksGenerationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {

		JCodeModel cm = new JCodeModel();
		JDefinedClass dc;
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
		String projectselectionLocation = "";
    	for (Object elem : selection.toList()) {
			IAdaptable adaptable = (IAdaptable) elem;
			IResource res = (IResource) adaptable.getAdapter(IResource.class);
			projectselectionLocation = res.getLocation().toPortableString();
			

    	}
    	System.out.println("GenerationPath: "+ projectselectionLocation);
    	try {
			dc = cm._class("com.local.demo.CyBenchBenchmarks");
			
			JMethod m = dc.method(0, void.class, "customBenchmark");
			m.annotate(cm.ref(Benchmark.class));
			

			JMethod m2 = dc.method(0, void.class, "generatedSecondBenchmark");
			m2.annotate(cm.ref(Benchmark.class));
			
			File file = new File(projectselectionLocation+"/cybench");
			file.mkdirs();
			
			cm.build(file);
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
	
//	 IProject project = res.getProject();
//	 IJavaProject javaProject = (IJavaProject)JavaCore.create((IProject)project);
//	 
//	 if(javaProject!=null && javaProject.getOutputLocation()!=null) {
//		//selectionEntry.setOutputPath(selectionEntry.getProjectPath().substring(0, selectionEntry.getProjectPath().lastIndexOf('/')) + javaProject.getOutputLocation().toPortableString());
//		IPackageFragmentRoot[] fragmetnRootsTest = javaProject.getAllPackageFragmentRoots();
//		Set<String> tempClassSet = new HashSet<String>();
//		for(IPackageFragmentRoot root : fragmetnRootsTest) {
//			if(root.getKind() == IPackageFragmentRoot.K_SOURCE) {
////					System.out.println("selectionEntry.getClassPaths() ==  "+selectionEntry.getClassPaths());
//				for(String classPath : selectionEntry.getClassPaths()) {
//						if(classPath.contains(root.getPath().toPortableString())){
//							tempClassSet.add(classPath.replace(root.getPath().toPortableString()+"/", "").replace("/", "."));
//						}
//				}
//
////				System.out.println("root.toString() ==  "+root.toString());
////				System.out.println("root.getPath() ==  "+root.getPath().toPortableString().replace("/", "."));
//			}
//		}
//		//selectionEntry.setClassPaths(tempClassSet);
//	}  

}
