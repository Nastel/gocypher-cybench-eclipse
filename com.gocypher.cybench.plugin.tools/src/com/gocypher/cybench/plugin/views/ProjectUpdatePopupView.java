package com.gocypher.cybench.plugin.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ProjectUpdatePopupView extends Dialog  {
	
		//	private final String messageNotific = "NOTICE: While adding JMH dependencies the comments and spaces from pom.xml will be removed, no dependencies \n"
		//	+ "or other instances get deleted, but the file structure may be reorganized. If you choose to cancel the process\n"
		//	+ "the nature will be updated but you will need to add the JMH dependencies yourself.";
		
		private final String messageNotific = "NOTICE: Your project pom.xml will be updated and JMH dependencies will be added.";
		private final String questionNotific = "Are you sure you want to update pom.xml file?";
		
		
		private boolean natureUpdateContinue = false;
	    public ProjectUpdatePopupView(Shell parentShell) {
	        super(parentShell);
	    }
	    
	    @Override
	    protected void createButtonsForButtonBar(Composite parent) {
	        super.createButtonsForButtonBar(parent);

	        Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
	        cancelButton.setText("No");
	        

	    	Button okButton = getButton(IDialogConstants.OK_ID);
	    	okButton.setText("Yes");
	    	
	    }
	    
	    @Override
	    public void create() {
	        super.create();
	    }

	    @Override
	    protected void configureShell(Shell newShell) {
	        super.configureShell(newShell);
	        newShell.setText("Update pom.xml");
	     
	    }
	    @Override
	    protected Point getInitialSize() {
	        return new Point(700, 200);
	    }
	    
		@Override
	    protected Control createDialogArea(Composite parent) {
	        Composite area = (Composite) super.createDialogArea(parent);
	        Composite container = new Composite(area, SWT.NONE);
	        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	        GridLayout layout = new GridLayout(10, false);
	        container.setLayout(layout);

	        createFirstName(container);

	        return area;
	    }

	    private void createFirstName(Composite container) {
	        Label notificationMessage = new Label(container, SWT.NONE | SWT.WRAP);
	        notificationMessage.setText(messageNotific);
	        
	        Label questionMessage = new Label(container, SWT.NONE);
	        questionMessage.setText(questionNotific);
	        
			GridDataFactory.fillDefaults().grab(false, false).span(10,3).applyTo(notificationMessage);
			GridDataFactory.fillDefaults().grab(false, false).span(10,3).applyTo(questionMessage);
			GridDataFactory.fillDefaults().grab(true, true).span(10,10).applyTo(container);
	    }
	    
	    @Override
	    protected boolean isResizable() {
	        return true;
	    }
	    
	    @Override
	    protected void okPressed() {
	        saveInput();
	        super.okPressed();
	    }
	    
	    private void saveInput() {
	    	natureUpdateContinue = true;
	    }
	    
	    public boolean continueUpdate() {
	    	return natureUpdateContinue;
	    }

}
