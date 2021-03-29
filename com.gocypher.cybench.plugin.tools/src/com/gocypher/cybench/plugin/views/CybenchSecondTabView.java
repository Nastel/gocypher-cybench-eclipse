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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.gocypher.cybench.plugin.model.LaunchConfiguration;
import com.gocypher.cybench.plugin.utils.GuiUtils;


public class CybenchSecondTabView extends AbstractLaunchConfigurationTab {
	
	private Group benchmarking;
	private Group configuration;
	
    private Spinner forks;
    private Spinner threads;
    private Spinner measurmentIterations;
    private Spinner warmupIterations;
    private Spinner warmupSeconds;
    private Spinner measurmentSeconds;
    
    private Text jvmProperties;
    private Button useCyBenchBenchmarkSettings;
    private Text classPathProperties;

    @Override
    public void createControl(Composite parent) {
        Composite comp = new Group(parent, SWT.BORDER);
        setControl(comp);
        
        GridLayoutFactory.swtDefaults().numColumns(10).applyTo(comp); 
        
        /* Benchmarking settings GROUP */
        benchmarking = prepareBenchmarkConfigurationGroup(comp);
        
        /* Configuration GROUP */
        configuration = prepareCyBenchConfigurations(comp);
     
    }
    
    private Group  prepareBenchmarkConfigurationGroup(Composite comp) {
    	benchmarking = new Group(comp, SWT.NONE);
        benchmarking.setText("Benchmarking settings");
        benchmarking.setLayout(new GridLayout(10, false));
        
	        Label useCyBenchBenchmarkSettingsLabel = new Label(benchmarking, SWT.NONE);
	        useCyBenchBenchmarkSettingsLabel.setText("Use CyBench Benchmark Settings:");
	        useCyBenchBenchmarkSettings = new Button(benchmarking, SWT.CHECK);
	        useCyBenchBenchmarkSettings.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 8, 1)); 

	        /* Report execution number of forks */
	        Label executionForkCountLabel = new Label(benchmarking, SWT.NONE);
	        executionForkCountLabel.setText("Forks:");
	        forks = new Spinner(benchmarking, SWT.BORDER);
	        
	        /* Report execution number of threads */
	        Label executionThreadsCountLabel = new Label(benchmarking, SWT.NONE);
	        executionThreadsCountLabel.setText("Threads:");
	        threads = new Spinner(benchmarking, SWT.BORDER);
	        
	        /* Report execution number of warm-up iterations */
	        Label executionWarmupIterationCountLabel = new Label(benchmarking, SWT.NONE);
	        executionWarmupIterationCountLabel.setText("Warmup Iterations:");
	        warmupIterations = new Spinner(benchmarking, SWT.BORDER);
	        
	        /* Report execution number of warm-up seconds */
	        Label executionWarmupSecondsLabel = new Label(benchmarking, SWT.NONE);
	        executionWarmupSecondsLabel.setText("Warmup time (s):");
	        warmupSeconds = new Spinner(benchmarking, SWT.BORDER);
	        
	        /* Report execution number of measurement iterations */
	        Label executionMeasurmentIterationCountLabel = new Label(benchmarking, SWT.NONE);
	        executionMeasurmentIterationCountLabel.setText("Measurment Iterations:");
	        measurmentIterations = new Spinner(benchmarking, SWT.BORDER);	        
	      
	        /* Report execution number of measurement seconds */
	        Label executionMeasurmentsSecondsLabel = new Label(benchmarking, SWT.NONE);
	        executionMeasurmentsSecondsLabel.setText("Measurment time (s):");
	        measurmentSeconds = new Spinner(benchmarking, SWT.BORDER);
	        
 	        useCyBenchBenchmarkSettings.addSelectionListener(new SelectionAdapter() {
 	            public void widgetSelected(SelectionEvent e) {
 	            	useCybenchEnableDisable();
 	            }
	        });
 	    

	        GridDataFactory.swtDefaults().span(2,1).applyTo(executionForkCountLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(executionThreadsCountLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(executionMeasurmentIterationCountLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(executionWarmupIterationCountLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(executionWarmupSecondsLabel);
	        GridDataFactory.swtDefaults().span(2,1).applyTo(executionMeasurmentsSecondsLabel);
	        
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(forks);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(threads);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(measurmentIterations);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(warmupIterations);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(warmupSeconds);
	        GridDataFactory.fillDefaults().grab(true, false).span(3,1).applyTo(measurmentSeconds);
	        
	        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(benchmarking);
	        
			return benchmarking;
    }
    private void useCybenchEnableDisable() {
    	if(!useCyBenchBenchmarkSettings.getSelection()) {
    		forks.setEnabled(false);
    		threads.setEnabled(false);
    		warmupIterations.setEnabled(false);
    		warmupSeconds.setEnabled(false);
    		measurmentIterations.setEnabled(false);
    		measurmentSeconds.setEnabled(false);
    	}else {
    		forks.setEnabled(true);
    		threads.setEnabled(true);
    		warmupIterations.setEnabled(true);
    		warmupSeconds.setEnabled(true);
    		measurmentIterations.setEnabled(true);
    		measurmentSeconds.setEnabled(true);
    	}
    }
    private Group  prepareCyBenchConfigurations(Composite comp) {

	   	 	configuration = new Group(comp, SWT.FILL | SWT.V_SCROLL);
	        configuration.setText("Configuration");
	        configuration.setLayout(new GridLayout(10, false));

	        Label  jvmPropertiesLabel = new Label(configuration, SWT.NONE);
	        jvmPropertiesLabel.setText("JVM Properties:");
	        jvmProperties = new Text(configuration, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
//	        jvmProperties.setLayoutData(new GridData(GridData.FILL_BOTH));
	        
	        Label  classPathPropertiesLabel = new Label(configuration, SWT.NONE);
	        classPathPropertiesLabel.setText("Classpath arguments:");
	        classPathProperties = new Text(configuration, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
//	        classPathProperties.setLayoutData(new GridData(GridData.FILL_BOTH));
	        
 	        GridDataFactory.swtDefaults().span(2,1).applyTo(jvmPropertiesLabel);
 	        GridDataFactory.swtDefaults().span(2,1).applyTo(classPathPropertiesLabel);
 	        // Class-paths layout
	        GridData classPathGrid = new GridData();
	        classPathGrid.horizontalSpan = 10;
	        classPathGrid.widthHint = GridData.FILL_HORIZONTAL;
 	        classPathGrid.heightHint = 120;

//	        GridDataFactory.fillDefaults().minSize(700, 200).grab(false, false).span(10,3).applyTo(jvmProperties);
//	        GridDataFactory.fillDefaults().minSize(700, 200).grab(false, false).span(10,3).applyTo(classPathProperties);
//	        GridDataFactory.fillDefaults().minSize(700, 600).grab(false, false).span(10,10).applyTo(configuration);
	        
 	       	GridDataFactory.createFrom(classPathGrid).applyTo(jvmProperties);
 	        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(configuration);
 	        
 	       	GridDataFactory.createFrom(classPathGrid).applyTo(classPathProperties);
 	        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(configuration);
 	        
			return configuration;
 	        
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
//    	    Map.Entry<String,String> entry = paths.entrySet().iterator().next();
//        	String launchPathDef = "";
//        	String reportFolderDef = "";
//        	if(entry!= null) {
//            	reportFolderDef = configuration.getAttribute(LaunchConfiguration.REPORT_FOLDER, entry.getKey()+"/reports");
//            	launchPathDef = configuration.getAttribute(LaunchConfiguration.LAUNCH_PATH, entry.getKey());
//            	setAvailbaleBenchmarksChoice(entry.getKey());
//            }
//            String reportNameDef = configuration.getAttribute(LaunchConfiguration.REPORT_NAME, "");
//            String reportUploadStatusDef = configuration.getAttribute(LaunchConfiguration.BENCHMARK_REPORT_STATUS, "public");
//            String pathToSourceSelectedDef = configuration.getAttribute(LaunchConfiguration.LAUNCH_SELECTED_PATH, "");
            
//            String userArguments = configuration.getAttribute(LaunchConfiguration.CUSTOM_USER_PROPERTIES, "");
//            String accessTokenDef = configuration.getAttribute(LaunchConfiguration.REMOTE_CYBENCH_ACCESS_TOKEN, "");
            
//            boolean storeReportInFile = configuration.getAttribute(LaunchConfiguration.SHOULD_SAVE_REPOT_TO_FILE, true);
//            boolean sendRepohardwarePropeties = configuration.getAttribute(LaunchConfiguration.INCLUDE_HARDWARE_PROPERTIES, true);
            
//            reportsFolder.setText(reportFolderDef);
//            reportsFolder.addModifyListener(modifyListener);
//            reportName.setText(reportNameDef);
//            reportName.addModifyListener(modifyListener);
//            reportUploadStatus.setText(reportUploadStatusDef);
//            reportUploadStatus.addModifyListener(modifyListener);
//            launchPath.setText(launchPathDef);
//            launchPath.addModifyListener(modifyListener);
        	
	//        expectedScore.setValues(-1, -1, 10000, 2, 1, 1);
	//        expectedScore.addModifyListener(modifyListener);
	
	//        userProperties.setText(userArguments);
	//        userProperties.addModifyListener(modifyListener);
	
	//        accessToken.setText(accessTokenDef);
	//        accessToken.addModifyListener(modifyListener);
	          
	//        onlySelectedLaunch.setText(pathToSourceSelectedDef);
	//        onlySelectedLaunch.addModifyListener(modifyListener);
	          
	//        shouldStoreReportToFileSystem.setSelection(storeReportInFile);
	//        shouldStoreReportToFileSystem.addSelectionListener(selectionListener);
	//        shouldSendReportToCyBench.setSelection(sendReportCybnech);
	//        shouldSendReportToCyBench.addSelectionListener(selectionListener);
	          
	//        shouldDoHardwareSnapshot.setSelection(includehardwarePropeties);
	//        shouldDoHardwareSnapshot.addSelectionListener(selectionListener);
	          
	//        shouldSendReportEnableDisable();

            String jvmArguments = configuration.getAttribute(LaunchConfiguration.CUSTOM_JVM_PROPERTIES, "");
            int threadDef = configuration.getAttribute(LaunchConfiguration.TREADS_COUNT, 1);
            int forksDef  = configuration.getAttribute(LaunchConfiguration.FORKS_COUNT, 1);
            int warmupIterationsDef  = configuration.getAttribute(LaunchConfiguration.WARMUP_ITERATION, 1);
            int measurmentIterationsDef = configuration.getAttribute(LaunchConfiguration.MEASURMENT_ITERATIONS, 5);
            int warmupSecondsDef = configuration.getAttribute(LaunchConfiguration.WARMUP_SECONDS, 10);
            int measurmentSecondsDef = configuration.getAttribute(LaunchConfiguration.MEASURMENT_SECONDS, 10);

            boolean useCybenchbenchmarkSettingsDef = configuration.getAttribute(LaunchConfiguration.USE_CYBNECH_BENCHMARK_SETTINGS, true);
          
            String classPathArguments = configuration.getAttribute(LaunchConfiguration.ADD_CUSTOM_CLASS_PATH, "");
            
            forks.setValues(forksDef, 1, 10000, 0, 1, 1);
            forks.addModifyListener(modifyListener);
            threads.setValues(threadDef, 1, 100, 0, 1, 1);
            threads.addModifyListener(modifyListener);
            measurmentIterations.setValues(measurmentIterationsDef, 1, 10000, 0, 1, 1);
            measurmentIterations.addModifyListener(modifyListener);
            warmupIterations.setValues(warmupIterationsDef, 1, 10000, 0, 1, 1);
            warmupIterations.addModifyListener(modifyListener);
            warmupSeconds.setValues(warmupSecondsDef, 1, 10000, 0, 1, 1);
            warmupSeconds.addModifyListener(modifyListener);
            measurmentSeconds.setValues(measurmentSecondsDef, 1, 10000, 0, 1, 1);
            measurmentSeconds.addModifyListener(modifyListener);           
            
            useCyBenchBenchmarkSettings.setSelection(useCybenchbenchmarkSettingsDef);
            useCyBenchBenchmarkSettings.addSelectionListener(selectionListener);
            
            classPathProperties.setText(classPathArguments);
            classPathProperties.addModifyListener(modifyListener);
            
            jvmProperties.setText(jvmArguments);
            jvmProperties.addModifyListener(modifyListener);
            
            useCybenchEnableDisable();
                       
        } catch (CoreException e) {
        	GuiUtils.logError("There was a problem on the run configuration initialization: ", e);
        }
    }
    
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(LaunchConfiguration.TREADS_COUNT, threads.getSelection());
        configuration.setAttribute(LaunchConfiguration.FORKS_COUNT, forks.getSelection());
        configuration.setAttribute(LaunchConfiguration.WARMUP_ITERATION, warmupIterations.getSelection());
        configuration.setAttribute(LaunchConfiguration.MEASURMENT_ITERATIONS, measurmentIterations.getSelection());
        configuration.setAttribute(LaunchConfiguration.WARMUP_SECONDS, warmupSeconds.getSelection());
        configuration.setAttribute(LaunchConfiguration.MEASURMENT_SECONDS, measurmentSeconds.getSelection());
        configuration.setAttribute(LaunchConfiguration.CUSTOM_JVM_PROPERTIES, jvmProperties.getText());
        configuration.setAttribute(LaunchConfiguration.USE_CYBNECH_BENCHMARK_SETTINGS, useCyBenchBenchmarkSettings.getSelection());
        configuration.setAttribute(LaunchConfiguration.ADD_CUSTOM_CLASS_PATH,  classPathProperties.getText());
        
//        configuration.setAttribute(LaunchConfiguration.REPORT_FOLDER, reportsFolder.getText());
//        configuration.setAttribute(LaunchConfiguration.REPORT_NAME, reportName.getText());
//        configuration.setAttribute(LaunchConfiguration.LAUNCH_PATH, launchPath.getText());
//        configuration.setAttribute(LaunchConfiguration.BENCHMARK_REPORT_STATUS, reportUploadStatus.getText());
//        configuration.setAttribute(LaunchConfiguration.LAUNCH_SELECTED_PATH, onlySelectedLaunch.getText());
        
//        configuration.setAttribute(LaunchConfiguration.CUSTOM_USER_PROPERTIES, userProperties.getText());
//        configuration.setAttribute(LaunchConfiguration.REMOTE_CYBENCH_ACCESS_TOKEN, accessToken.getText());
//        configuration.setAttribute(LaunchConfiguration.SHOULD_SAVE_REPOT_TO_FILE, shouldStoreReportToFileSystem.getSelection());
//        configuration.setAttribute(LaunchConfiguration.SHOULD_SEND_REPORT_CYBENCH, shouldSendReportToCyBench.getSelection());
//        configuration.setAttribute(LaunchConfiguration.INCLUDE_HARDWARE_PROPERTIES, shouldDoHardwareSnapshot.getSelection());      
        
//        configuration.setAttribute(LaunchConfiguration.EXECUTION_SCORE, expectedScore.getSelection());
        
//        String buildPath = getBuildPath(launchPath.getText());
//    	configuration.setAttribute(LaunchConfiguration.BUILD_PATH,  buildPath);
    }
    

    @Override
    public String getName() {
        return "Execution Settings";
    }

}
