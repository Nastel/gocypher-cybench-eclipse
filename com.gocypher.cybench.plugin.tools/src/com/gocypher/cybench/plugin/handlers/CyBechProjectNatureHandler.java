package com.gocypher.cybench.plugin.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.gocypher.cybench.plugin.CyBenchProjectNature;
import com.gocypher.cybench.plugin.utils.GuiUtils;

public class CyBechProjectNatureHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
			System.out.println("--->Adding CyBench Nature");
					
			//System.out.println("Selection:"+selection.getClass());
			IJavaProject javaProject = GuiUtils.resolveJavaProject(selection) ;
		
			if (javaProject != null) {
				this.addCybenchNature(javaProject);
								
				GuiUtils.refreshProject(javaProject);
			}
			System.out.println("--->CyBench Nature finish");
		}catch (Exception e) {
			System.err.println("Error on project nature update:"+e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	
	private void addCybenchNature (IJavaProject javaProject) throws Exception{
		
		IProjectDescription description = javaProject.getProject().getDescription();
		String[] natures = description.getNatureIds();
		System.out.println("Natures of project found:");
		for (String nature:natures) {
			System.out.println("Nature:"+nature);
		}
		
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = CyBenchProjectNature.NATURE_ID;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus status = workspace.validateNatureSet(newNatures);
		
		
		if (status.getCode() == IStatus.OK) {
			 System.out.println("CyBench nature will be added:"+status);
		    description.setNatureIds(newNatures);
		    javaProject.getProject().setDescription(description, null);
		   
		}
		else {
			System.err.println("CyBench nature won't be added:"+status);
		}
	}
	
	
	

}