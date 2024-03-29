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

package com.gocypher.cybench.plugin.views;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import java.io.File;

import org.apache.commons.lang3.*;

import com.gocypher.cybench.plugin.model.LaunchConfiguration;
import com.gocypher.cybench.plugin.utils.GuiUtils;


public class CyBenchAutoTabView extends AbstractLaunchConfigurationTab {
	
	private Group config;
	private Group scopeGroup;
	private Group methodGroup;
	
	private Combo scope;
	private Combo method;
	private Combo threshold;
	
	private Text compareVersion;
	
	private Spinner latestReports;
	private Spinner anomaliesAllowed;
	private Spinner percentChange;
	private Spinner deviationsAllowed;
	
	private Button runAutoComparison;
	
    @Override
    public void createControl(Composite parent) {
        Composite comp = new Group(parent, SWT.BORDER);
        setControl(comp);
        
        GridLayoutFactory.swtDefaults().numColumns(10).applyTo(comp); 
        
        config = prepareBenchmarkConfigurationGroup(comp);
        
        scopeGroup = prepareScopeGroup(comp);
        
        methodGroup = prepareMethodGroup(comp);  
                
      
     
    }
    
    private Group prepareBenchmarkConfigurationGroup(Composite comp) {
    	config = new Group(comp, SWT.NONE);
    	config.setText("Automated Comparison Config");
        config.setLayout(new GridLayout(10, false));
        
        Label runAutoComparisonLabel = new Label(config, SWT.NONE);
        runAutoComparisonLabel.setText("Run Automatic Comparison");
        runAutoComparison = new Button(config, SWT.CHECK);
        runAutoComparison.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 8, 1)); 
        
        	Label scopeLabel = new Label(config, SWT.None);
        	scopeLabel.setText("Scope: ");
        	scope = new Combo(config, SWT.READ_ONLY);
        	String[] scopeOptions = {"WITHIN", "BETWEEN"};
        	scope.setItems(scopeOptions);
        	
        	Label compareVersionLabel = new Label(config, SWT.NONE);
        	compareVersionLabel.setText("Compare Version: ");
        	compareVersion = new Text(config, SWT.BORDER);
        	
        	Label latestReportsLabel = new Label(config, SWT.NONE);
        	latestReportsLabel.setText("# of Latest Reports: ");
        	latestReports = new Spinner(config, SWT.BORDER);
        	
        	Label anomaliesAllowedLabel = new Label(config, SWT.NONE);
        	anomaliesAllowedLabel.setText("# of Anomalies Allowed: ");
        	anomaliesAllowed = new Spinner(config, SWT.BORDER);
        	
        	Label methodLabel = new Label(config, SWT.NONE);
        	methodLabel.setText("Method: ");
        	method = new Combo(config, SWT.READ_ONLY);
        	String[] methodOptions = {"DELTA", "SD"};
        	method.setItems(methodOptions);
        	
        	Label thresholdLabel = new Label(config, SWT.NONE);
        	thresholdLabel.setText("Threshold: ");
        	threshold = new Combo(config, SWT.READ_ONLY);
        	String[] thresholdOptions = {"PERCENT_CHANGE", "GREATER"};
        	threshold.setItems(thresholdOptions);        	
        	
        	Label percentChangeLabel = new Label(config, SWT.NONE);
        	percentChangeLabel.setText("Percent Change Allowed: ");
        	percentChange = new Spinner(config, SWT.BORDER);
        	
        	Label deviationsAllowedLabel = new Label(config, SWT.NONE);
        	deviationsAllowedLabel.setText("# of Deviations Allowed: ");
        	deviationsAllowed = new Spinner(config, SWT.BORDER);
        	
        	runAutoComparison.addSelectionListener(new SelectionAdapter() {
        		public void widgetSelected(SelectionEvent e) {
        			runAutoComparison();
        		}
        	});
        	
        	method.addSelectionListener(new SelectionAdapter() {
        		public void widgetSelected(SelectionEvent e) {
        			disableDeltaWidgets();
        		}
        	});
        	
        	threshold.addSelectionListener(new SelectionAdapter() {
        		public void widgetSelected(SelectionEvent e) {
        			disableDeltaWidgets();
        		}
        	});
        	
        	scope.addSelectionListener(new SelectionAdapter() {
        		public void widgetSelected(SelectionEvent e) {
        			disableScopeWidgets();
        		}
        	});
        	
	        GridDataFactory.swtDefaults().span(2,1).applyTo(scopeLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(compareVersionLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(thresholdLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(latestReportsLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(anomaliesAllowedLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(methodLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(percentChangeLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(deviationsAllowedLabel);
	        
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(scope);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(compareVersion);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(threshold);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(latestReports);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(anomaliesAllowed);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(method);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(percentChange);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(deviationsAllowed);
	        
	        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(config);
	        
			return config;
    }
    
    
    private Group prepareScopeGroup(Composite comp) {
    	scopeGroup = new Group(comp, SWT.NONE);
    	scopeGroup.setText("Scope Options Info");
    	scopeGroup.setLayout(new GridLayout(1, false));
    	
    	Label scopeHelp = new Label(scopeGroup, SWT.None);
    	scopeHelp.setText("Scope: Determines which version of your project to compare to. Options are WITHIN and BETWEEN");
    	
    	Label withinHelp = new Label(scopeGroup, SWT.None);
    	withinHelp.setText("   - WITHIN: Comparison will be made against reports with the same version. Compare Version field is disabled.");
    	
    	Label betweenHelp = new Label(scopeGroup, SWT.None);
    	betweenHelp.setText("   - BETWEEN: Comparison will be made against reports with a previous version. Compare Version field is enabled, and is required.");
    	
    	Label compareVersionHelp = new Label(scopeGroup, SWT.None);
    	compareVersionHelp.setText("Compare Version: Version of your project to compare to, if using the BETWEEN scope.");
    	
    	Label numOfReportsHelp = new Label(scopeGroup, SWT.None);
    	numOfReportsHelp.setText("# of Latest Reports: Set the number of previous reports to compare to. Setting this to"
    			+ " 1 would compare to the most recent report, 5 would check the last 5, etc.");
    	
    	Label numOfAnomaliesAllowedHelp = new Label(scopeGroup, SWT.None);
    	numOfAnomaliesAllowedHelp.setText("# of Anomalies Allowed: Set the number of anomalies allowed in the comparison before failing CI/CD pipeline builds.");
    	

    	GridDataFactory.swtDefaults().span(1,4).applyTo(numOfReportsHelp);
    	GridDataFactory.swtDefaults().span(1,4).applyTo(numOfAnomaliesAllowedHelp);
    	GridDataFactory.swtDefaults().span(1,4).applyTo(scopeHelp);
    	GridDataFactory.swtDefaults().span(1,4).applyTo(withinHelp);
    	GridDataFactory.swtDefaults().span(1,4).applyTo(betweenHelp);
    	GridDataFactory.swtDefaults().span(1,4).applyTo(compareVersionHelp);
    	
        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(scopeGroup);
        
        return scopeGroup;
    	
    }
    
    private Group prepareMethodGroup(Composite comp) {
    	methodGroup = new Group(comp, SWT.NONE);
    	methodGroup.setText("Method Options Info");
    	methodGroup.setLayout(new GridLayout(1, false));
    	
    	Label methodHelp = new Label(methodGroup, SWT.None);
    	methodHelp.setText("Method: Determines comparison method. Options are DELTA and SD.");
    	
    	Label deltaHelp = new Label(methodGroup, SWT.None);
    	deltaHelp.setText("   - DELTA: Compares the overall change in score. When selected, deviations allowed is disabled, and threshold is enabled.");
    	
    	Label sdHelp = new Label(methodGroup, SWT.NONE);
    	sdHelp.setText("   - SD: Tests for Standard Deviation of new score, compared to the average of previous N scores, where N is # of Latest Reports.");
    	
    	Label thresholdHelp = new Label(methodGroup, SWT.NONE);
    	thresholdHelp.setText("Threshold: Used when the DELTA method is selected. Options are GREATER and PERCENT_CHANGE. Disabled when SD method is selected.");
    	
    	Label greaterHelp = new Label(methodGroup, SWT.NONE);
    	greaterHelp.setText("   - GREATER: Finds the change in score between reports, if # of Latest Reports is higher than 1,"
    			+ " will compare against the average score of those reports.");
    	
    	Label pcHelp = new Label(methodGroup, SWT.NONE);
    	pcHelp.setText("   - PERCENT_CHANGE: Finds the percent change in score between reports. Percent Change Allowed field is enabled and required.");
    	
    	Label pcaHelp = new Label(methodGroup, SWT.NONE);
    	pcaHelp.setText("Percent Change Allowed: Set the percent change allowed value. Required if threshold PERCENT_CHANGE is selected, otherwise disabled.");
    	
    	Label deviationsHelp = new Label(methodGroup, SWT.None);
    	deviationsHelp.setText("Deviations Allowed: Set the deviations allowed value, benchmarks beyond this value will report as an anomaly."
    			+ " Required if method SD is selected, otherwise disabled.");
    	
    	GridDataFactory.swtDefaults().span(5,5).applyTo(methodHelp);
    	GridDataFactory.swtDefaults().span(5,5).applyTo(deltaHelp);
    	GridDataFactory.swtDefaults().span(5,5).applyTo(sdHelp);
    	GridDataFactory.swtDefaults().span(5,5).applyTo(thresholdHelp);
    	GridDataFactory.swtDefaults().span(5,5).applyTo(greaterHelp);
    	GridDataFactory.swtDefaults().span(5,5).applyTo(pcHelp);
    	GridDataFactory.swtDefaults().span(5,5).applyTo(pcaHelp);
    	GridDataFactory.swtDefaults().span(5,5).applyTo(deviationsHelp);
    	
    	GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(methodGroup);
    	
    	return methodGroup;
    	
    }
    
    // handling to disable irrelevant fields depending on selection
    private void disableDeltaWidgets() {
    	if (method.getSelectionIndex() == 0) {
    		threshold.setEnabled(true);
    		if (threshold.getSelectionIndex() == 0) {
    			percentChange.setEnabled(true);
    		} else {
    			percentChange.setEnabled(false);
    		}
    		deviationsAllowed.setEnabled(false);
    	} else {
    		threshold.setEnabled(false);
    		percentChange.setEnabled(false);
    		deviationsAllowed.setEnabled(true);
    	}
    	
    	if (scope.getSelectionIndex() == 0) {
    	}
    }
    
    private void disableScopeWidgets() {
    	if (scope.getSelectionIndex() == 0) {
    		compareVersion.setEnabled(false);
    	} else {
    		compareVersion.setEnabled(true);
    	}
    }
    
    private void runAutoComparison() {
    	if(!runAutoComparison.getSelection()) {
    		scope.setEnabled(false);
    		compareVersion.setEnabled(false);
    		threshold.setEnabled(false);
    		latestReports.setEnabled(false);
    		anomaliesAllowed.setEnabled(false);
    		method.setEnabled(false);
    		percentChange.setEnabled(false);
    		deviationsAllowed.setEnabled(false);
    	}else {
    		scope.setEnabled(true);
    		compareVersion.setEnabled(false);
    		threshold.setEnabled(true);
    		latestReports.setEnabled(true);
    		anomaliesAllowed.setEnabled(true);
    		method.setEnabled(true);
    		percentChange.setEnabled(false);
    		deviationsAllowed.setEnabled(false);
    	}
    }
    
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    }
    
    private ModifyListener modifyListener = new ModifyListener() {
    	public void modifyText(ModifyEvent e) {
    	setDirty(true);
    	updateLaunchConfigurationDialog();
    	}
	};
	
    private SelectionListener selectionListener = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
				
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			setDirty(true);
	    	updateLaunchConfigurationDialog();
		}
	};
	
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {

            String scopeDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_SCOPE, "WITHIN");
            String compareVersionDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_COMPAREVERSION, "");
            String methodDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_METHOD, "DELTA");
            String thresholdDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_THRESHOLD, "GREATER");
            
            scope.setText(scopeDef);
            scope.addModifyListener(modifyListener);
            compareVersion.addModifyListener(modifyListener);
            method.setText(methodDef);
            method.addModifyListener(modifyListener);
            threshold.setText(thresholdDef);
            threshold.addModifyListener(modifyListener);
            
            
            
            int latestReportsDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_LATESTREPORTS, 0);
            int anomaliesAllowedDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_ANOMALIES_ALLOWED, 0);
            int percentChangeDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_PERCENTCHANGE, 0);
            int deviationsDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_DEVIATIONSALLOWED, 0);
                        
            latestReports.setValues(latestReportsDef, 1, 100, 0, 1, 1);
            latestReports.addModifyListener(modifyListener);
            anomaliesAllowed.setValues(anomaliesAllowedDef, 0, 99, 0, 1, 1);
            anomaliesAllowed.addModifyListener(modifyListener);
            percentChange.setValues(percentChangeDef, 0, 9999, 2, 10, 100);
            percentChange.addModifyListener(modifyListener);
            deviationsAllowed.setValues(deviationsDef, 0, 9999, 2, 10, 100);
            deviationsAllowed.addModifyListener(modifyListener);   
            
            boolean runAutoComparisonDef = configuration.getAttribute(LaunchConfiguration.AUTO_USE_AUTO_COMP, true);
            
            runAutoComparison.setSelection(runAutoComparisonDef);
            runAutoComparison.addSelectionListener(selectionListener);
            
            runAutoComparison();
            disableDeltaWidgets();
            disableScopeWidgets();
            
                       
        } catch (CoreException e) {
        	GuiUtils.logError("There was a problem on the run configuration initialization: ", e);
        }
    }
    
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    	
    	configuration.setAttribute(LaunchConfiguration.AUTO_COMPARE_ANOMALIES_ALLOWED, anomaliesAllowed.getSelection());
    	configuration.setAttribute(LaunchConfiguration.AUTO_COMPARE_COMPAREVERSION, compareVersion.getText()); //?
    	configuration.setAttribute(LaunchConfiguration.AUTO_COMPARE_DEVIATIONSALLOWED, deviationsAllowed.getSelection());
    	configuration.setAttribute(LaunchConfiguration.AUTO_COMPARE_LATESTREPORTS, latestReports.getSelection());
    	configuration.setAttribute(LaunchConfiguration.AUTO_COMPARE_METHOD, method.getText());
    	configuration.setAttribute(LaunchConfiguration.AUTO_COMPARE_PERCENTCHANGE, percentChange.getSelection());
    	configuration.setAttribute(LaunchConfiguration.AUTO_COMPARE_SCOPE, scope.getText());
    	configuration.setAttribute(LaunchConfiguration.AUTO_COMPARE_THRESHOLD, threshold.getText());
    	configuration.setAttribute(LaunchConfiguration.AUTO_USE_AUTO_COMP, runAutoComparison.getSelection());
    }
    

    @Override
    public String getName() {
        return "Automated Comparison";
    }

}
