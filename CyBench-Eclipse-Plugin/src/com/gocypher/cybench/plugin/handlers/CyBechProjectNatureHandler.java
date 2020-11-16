package com.gocypher.cybench.plugin.handlers;

import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

public class CyBechProjectNatureHandler implements ILaunchShortcut{

	@Override
	public void launch(ISelection selection, String mode) {
		try {
			System.out.println("--->Add CyBench Nature");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void launch(IEditorPart arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
   

}
