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
			GuiUtils.logInfo("--->Adding CyBench Nature");
					
			IJavaProject javaProject = GuiUtils.resolveJavaProject(selection) ;
		
			if (javaProject != null) {
				this.addCybenchNature(javaProject);
								
				GuiUtils.refreshProject(javaProject);
			}
			GuiUtils.logInfo("--->CyBench Nature finish");
		}catch (Exception e) {	
			GuiUtils.logError ("Error on project nature update",e);
		}
		return null;
	}
	
	
	private void addCybenchNature (IJavaProject javaProject) throws Exception{
		
		IProjectDescription description = javaProject.getProject().getDescription();
		String[] natures = description.getNatureIds();
		GuiUtils.logInfo("Natures of project found:");
		for (String nature:natures) {
			System.out.println("Nature:"+nature);
		}
		
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = CyBenchProjectNature.NATURE_ID;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus status = workspace.validateNatureSet(newNatures);
		
		
		if (status.getCode() == IStatus.OK) {
			GuiUtils.logInfo("CyBench nature will be added:"+status);
		    description.setNatureIds(newNatures);
		    javaProject.getProject().setDescription(description, null);
		   
		}
		else {
			GuiUtils.logError("CyBench nature won't be added:"+status);
		}
	}
	
	
	

}
