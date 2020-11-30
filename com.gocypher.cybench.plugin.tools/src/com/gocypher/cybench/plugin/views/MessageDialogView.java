package com.gocypher.cybench.plugin.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.gocypher.cybench.plugin.model.BenchmarkMethodModel;
import com.gocypher.cybench.plugin.utils.GuiUtils;

public class MessageDialogView  extends Dialog {

    private List<BenchmarkMethodModel> methodsExist;
    private List<BenchmarkMethodModel> methodsToGenerate = new ArrayList<BenchmarkMethodModel>();;
	List<Label> methodLabels = new ArrayList<Label>();
    List<Button> methodSelections =  new ArrayList<Button>();

    public MessageDialogView(IShellProvider parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}


	public MessageDialogView(Shell parentShell, List<BenchmarkMethodModel> methods) {
		super(parentShell);
		
		this.methodsExist = methods;
		// TODO Auto-generated constructor stub
	}

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(10, false));
        int i = 0;
        for(BenchmarkMethodModel method : methodsExist) {
        	GuiUtils.logInfo("method.getMethodBenchmarkMode: "+method.getMethodBenchmarkMode());
        	GuiUtils.logInfo("method.getMethodHint: "+method.getMethodHint());
        	GuiUtils.logInfo("method.getMethodName: "+method.getMethodName());
        	GuiUtils.logInfo("method.getMethodType: "+method.getMethodType());
        	
        	methodLabels.add(new Label(container, SWT.NONE));
        	methodLabels.get(i).setText(method.getMethodName()+"() : "+method.getMethodType());
        	methodLabels.get(i).setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 7, 1)); 
 	        
 	        methodSelections.add(new Button(container, SWT.CHECK));
 	        methodSelections.get(i).setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false, 3, 1)); 

        	i++;
        }
        return container;
    }
    
    // overriding this methods allows you to set the
    // title of the custom dialog
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select Methods To Generate Benchmarks");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(450, 300);
    }

	@Override
    public void okPressed() {
		int i = 0;
        for(Button checkBox : methodSelections) {
        	if(checkBox.getSelection()) {
        		methodsToGenerate.add(methodsExist.get(i));
        	}
        	i++;
        }
        this.setReturnCode(0);
		this.close();
	}
	
    private static void doAnimation(Shell shell) {
        Point shellArea = shell.getSize();
        int x = shellArea.x;
        int y = shellArea.y;
        while (x != -200) {
            try {
                shell.setSize(x--, y--);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void fillDialogMenu(IMenuManager dialogMenu) {
        dialogMenu.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager arg0) {
                handleShellCloseEvent();
            }
        });
    }

    protected void handleShellCloseEvent() {
        doAnimation(getShell());
        super.handleShellCloseEvent();
    }


	public List<BenchmarkMethodModel> getMethodsToGenerate() {
		return methodsToGenerate;
	}

}
