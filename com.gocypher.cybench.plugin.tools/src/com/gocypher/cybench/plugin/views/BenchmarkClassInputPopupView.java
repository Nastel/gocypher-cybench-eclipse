package com.gocypher.cybench.plugin.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class BenchmarkClassInputPopupView extends Dialog {

    private Text txtBenchmarkClass;
    private String benchmarkClass;

    public BenchmarkClassInputPopupView(Shell parentShell) {
        super(parentShell);
    }
    
    @Override
    public void create() {
        super.create();
//        setTitle("This is my first custom dialog");
//        setMessage("This is a TitleAreaDialog", IMessageProvider.INFORMATION);
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Add new Benchmark Class path");
     
    }
    @Override
    protected Point getInitialSize() {
        return new Point(550, 380);
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
        Label lbtFirstName = new Label(container, SWT.NONE);
        lbtFirstName.setText("Benchmark Class:");
        txtBenchmarkClass = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(false, false).span(2,2).applyTo(lbtFirstName);
		GridDataFactory.fillDefaults().grab(true, false).span(8,2).applyTo(txtBenchmarkClass);
		GridDataFactory.fillDefaults().hint(600, 50).grab(true, true).span(10,10).applyTo(container);
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
    	benchmarkClass = txtBenchmarkClass.getText();

    }

    public String getBenchmarkClassInput() {
        return benchmarkClass;
    }

}