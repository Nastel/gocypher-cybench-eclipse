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
	
	
	private Combo scope;
	private Combo method;
	private Combo threshold;
	private Text compareVersion;
	
	private Spinner latestReports;
	private Spinner anomaliesAllowed;
	private Spinner percentChange;
	private Spinner deviationsAllowed;
		
//    private File myFile = SystemUtils.getUserHome();
//    private String myFileHome = myFile.getAbsolutePath(); <-- this also works if you don't use System.getProperty("user.home")

    @Override
    public void createControl(Composite parent) {
        Composite comp = new Group(parent, SWT.BORDER);
        setControl(comp);
        
        GridLayoutFactory.swtDefaults().numColumns(10).applyTo(comp); 
        
        /* Benchmarking settings GROUP */
        config = prepareBenchmarkConfigurationGroup(comp);       
     
    }
    
    private Group  prepareBenchmarkConfigurationGroup(Composite comp) {
    	config = new Group(comp, SWT.NONE);
    	config.setText("Automated Comparison Config");
        config.setLayout(new GridLayout(10, false));
        
        	Label scopeLabel = new Label(config, SWT.None);
        	scopeLabel.setText("Scope: ");
        	scope = new Combo(config, SWT.READ_ONLY);
        	scope.setItems("WITHIN", "BETWEEN");
        	
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
        	method.setItems("DELTA", "SD");
        	
        	Label thresholdLabel = new Label(config, SWT.NONE);
        	thresholdLabel.setText("Threshold: ");
        	threshold = new Combo(config, SWT.READ_ONLY);
        	threshold.setItems("PERCENT_CHANGE", "GREATER");        	
        	
        	Label percentChangeLabel = new Label(config, SWT.NONE);
        	percentChangeLabel.setText("Percent Change Allowed: ");
        	percentChange = new Spinner(config, SWT.BORDER);
        	
        	Label deviationsAllowedLabel = new Label(config, SWT.NONE);
        	deviationsAllowedLabel.setText("# of Deviations Allowed: ");
        	deviationsAllowed = new Spinner(config, SWT.BORDER);
        	            	
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
            
            int latestReportsDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_LATESTREPORTS, 1);
            int anomaliesAllowedDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_ANOMALIES_ALLOWED, 1);
            int percentChangeDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_PERCENTCHANGE, 15);
            int deviationsDef = configuration.getAttribute(LaunchConfiguration.AUTO_COMPARE_DEVIATIONSALLOWED, 1);
                        
            latestReports.setValues(latestReportsDef, 1, 100, 0, 1, 1);
            latestReports.addModifyListener(modifyListener);
            anomaliesAllowed.setValues(anomaliesAllowedDef, 1, 1000, 0, 1, 1);
            anomaliesAllowed.addModifyListener(modifyListener);
            percentChange.setValues(percentChangeDef, 1, 99, 0, 1, 1);
            percentChange.addModifyListener(modifyListener);
            deviationsAllowed.setValues(deviationsDef, 1, 1000, 0, 1, 1);
            deviationsAllowed.addModifyListener(modifyListener);                 
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
    }
    

    @Override
    public String getName() {
        return "Automated Comparison";
    }

}
