package com.gocypher.cybench.plugin.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.gocypher.cybench.plugin.model.BenchmarkMethodModel;

public class MessageDialogView  extends Dialog {

    private List<BenchmarkMethodModel> methodsExist;
    private List<BenchmarkMethodModel> methodsToGenerate = new ArrayList<BenchmarkMethodModel>();;
    List<Button> methodSelections =  new ArrayList<Button>();
    Button checkALL;
    Button uncheckAll;
    private Combo modeChoice;
    
    private ScrolledComposite scrolledComposite;
    Label hintText;
    private final String textForHint = "HINT: if no method is selected only the stub will be generated";

    public MessageDialogView(IShellProvider parentShell) {
		super(parentShell);
	}


	public MessageDialogView(Shell parentShell, List<BenchmarkMethodModel> methods) {
		super(parentShell);
	    this.methodsExist = methods;
	}

    @Override
    protected Control createDialogArea(Composite parent) {
	    Composite popupViewPort = (Composite) super.createDialogArea(parent);
	    popupViewPort.setLayout(new GridLayout(6, false));
	    popupViewPort.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
	     
    	String[] methodModeTypes = {"Throughput", "AverageTime", "SampleTime", "SingleShotTime"};
        modeChoice = new Combo(popupViewPort, SWT.BORDER); 
        modeChoice.setItems(methodModeTypes);
//         modeChoice.setLayout(new GridLayout(6, false));
        modeChoice.setText(methodModeTypes[0]);
        modeChoice.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1));
        
        checkALL = new Button(popupViewPort, SWT.BUTTON1);
        checkALL.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, true, false, 3,1)); 
        checkALL.setText("Select All");
        checkALL.addSelectionListener(selectAll);
 	        
        uncheckAll = new Button(popupViewPort, SWT.BUTTON1);
        uncheckAll.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false, 3,1)); 
        uncheckAll.setText("Select None");
        uncheckAll.addSelectionListener(selectNone);
         
        Composite content = (Composite) super.createDialogArea(parent);
        content.setLayout(new FillLayout());
        content.setSize(new Point(420, 300));
        
        scrolledComposite = new ScrolledComposite(content, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        
        Composite container = new Composite(scrolledComposite, SWT.MULTI);
        container.setLayout(new GridLayout(6, false));
        
        int i = 0;
        for(BenchmarkMethodModel method : methodsExist) {
 	        methodSelections.add(new Button(container, SWT.CHECK));
 	        methodSelections.get(i).setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 3,1)); 
 	        methodSelections.get(i).addSelectionListener(selectionListener);
 	        methodSelections.get(i).setText(method.getMethodName()+"() : "+method.getMethodType());
        	i++;
        }
        
        scrolledComposite.setContent(container);
        scrolledComposite.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        scrolledComposite.setExpandVertical( true );
        scrolledComposite.setExpandHorizontal( true );

        hintText = new Label(parent, SWT.NONE);
        hintText.setText(textForHint);
        return parent;
    }
    
    private SelectionListener selectAll = new SelectionListener() {
    	@Override
  		public void widgetSelected(SelectionEvent arg0) {
  	        for(Button checkBox : methodSelections) {
  	        	checkBox.setSelection(true);
  	        }
  		}
		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
		}
    };
    private SelectionListener selectNone = new SelectionListener() {
    	@Override
  		public void widgetSelected(SelectionEvent arg0) {
  	        for(Button checkBox : methodSelections) {
  	        	checkBox.setSelection(false);
  	        }
  		}
		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
		}
    };
    
    private SelectionListener selectionListener = new SelectionListener() {

  		@Override
  		public void widgetDefaultSelected(SelectionEvent arg0) {
  				
  		}

  		@Override
  		public void widgetSelected(SelectionEvent arg0) {
  			boolean buttonChecked = false;
  	        for(Button checkBox : methodSelections) {
  	        	if(checkBox.getSelection()) {
  	        		buttonChecked = checkBox.getSelection();
		  	 	    break;
  	        	}
  	        }
			Button ok = getButton(IDialogConstants.OK_ID);
			if (ok != null) {
			    if(buttonChecked) {
			    	hintText.setText("");
			    }else {
			    	hintText.setText(textForHint);
			    }
			}
  		}
  	};
	
    @Override
	public void create(){
    	super.create();
    }
    // overriding this methods allows you to set the
    // title of the custom dialog
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select Methods To Generate Benchmarks");
     
    }
    @Override
    protected boolean isResizable() {
        return true;
    }
    @Override
    protected Point getInitialSize() {
        return new Point(550, 380);
    }

	@Override
    public void okPressed() {
		int i = 0;
    	List<String> selectedMethodNames = new ArrayList<String>();
        for(Button checkBox : methodSelections) {
        	if(checkBox.getSelection()) {
        		methodsExist.get(i).setMethodBenchmarkMode(modeChoice.getText());
        		if(!selectedMethodNames.contains(methodsExist.get(i).getMethodName())) {
        			selectedMethodNames.add(methodsExist.get(i).getMethodName());
            		methodsExist.get(i).setMethodName(methodsExist.get(i).getMethodName()+"Benchmark");
        		}else {
        			String allParamsTypes = String.join("",  methodsExist.get(i).getParameterTypes());
        			String[] stringArray = allParamsTypes.split("\\W+");
        			allParamsTypes = String.join("",stringArray);
        			allParamsTypes =allParamsTypes.replace("Q", "");
            		methodsExist.get(i).setMethodName(methodsExist.get(i).getMethodName()+"Benchmark"+allParamsTypes);
        		}
        		methodsToGenerate.add(methodsExist.get(i));
        	}
        	i++;
        }
        this.setReturnCode(0);
		this.close();
	}
	
//    private static void doAnimation(Shell shell) {
//        Point shellArea = shell.getSize();
//        int x = shellArea.x;
//        int y = shellArea.y;
//        while (x != -200) {
//            try {
//                shell.setSize(x--, y--);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

    protected void fillDialogMenu(IMenuManager dialogMenu) {
        dialogMenu.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager arg0) {
                handleShellCloseEvent();
            }
        });
    }

    protected void handleShellCloseEvent() {
//        doAnimation(getShell());
        super.handleShellCloseEvent();
    }


	public String becnhmarkingMethodChoice() {
		return modeChoice.getText();
	}
	
	public List<BenchmarkMethodModel> getMethodsToGenerate() {
		return methodsToGenerate;
	}

}
