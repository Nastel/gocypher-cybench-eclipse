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
//	private Group conditions;
	
//    private Text reportsFolder;
//    private Button browse;

//    private Combo launchPath;
//    private Text reportName;
//    private Text onlySelectedLaunch;
//    private Combo onlySelectedLaunch;
//    private Combo reportUploadStatus;
//    
    private Spinner forks;
    private Spinner threads;
    private Spinner measurmentIterations;
    private Spinner warmupIterations;
    private Spinner warmupSeconds;
    private Spinner measurmentSeconds;
    
//    private Spinner expectedScore;

    private Text jvmProperties;
//    private Text userProperties;
    private Button useCyBenchBenchmarkSettings;
    
//    private Button shouldStoreReportToFileSystem;
//    private Button shouldSendReportToCyBench;
//    private Button shouldDoHardwareSnapshot;

    private Text classPathProperties;

//    private Text accessToken;
    
//	private Map<String, String> paths =  new HashMap<>();
//	
//	private ArrayList<String> projectSelectedPaths = new ArrayList<String>();

    @Override
    public void createControl(Composite parent) {
//    	paths = getProjectPaths(true);
//    	String[] itemsArray =  paths.keySet().toArray(new String[ paths.keySet().size()]);
        Composite comp = new Group(parent, SWT.BORDER);
        setControl(comp);
        
        GridLayoutFactory.swtDefaults().numColumns(10).applyTo(comp); 
        
        /* Benchmarking settings GROUP */
        benchmarking = prepareBenchmarkConfigurationGroup(comp);
        
        /* Configuration GROUP */
        configuration = prepareCyBenchConfigurations(comp);
	        
        /* Execution Conditions GROUP */
//        conditions = prepareConditionGroup(comp);
     
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
    	  
 	        /* Report name input field */
// 	        Label reportNameLabel = new Label(configuration, SWT.NONE);
// 	        reportNameLabel.setText("Report Name:");
// 	        reportName = new Text(configuration, SWT.BORDER);
 	        
 	        /* Report launch path input field */
// 	        Label reportlaunchPathLabel = new Label(configuration, SWT.NONE);
// 	        launchPath = new Combo(configuration, SWT.BORDER); 
// 	        launchPath.setItems(itemsArray);
// 	        launchPath.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
// 	        reportlaunchPathLabel.setText("Run Project:");
 	       
           
// 	        launchPath.addSelectionListener(new SelectionAdapter() {
//	            public void widgetSelected(SelectionEvent e) {
//	            	setAvailbaleBenchmarksChoice(launchPath.getText());
//	            	reportsFolder.setText(launchPath.getText()+"/reports");
//	            }
//	
//	        });
 	        
 	        /* Report selection field */
// 	        Label runOnlySelectedLabel = new Label(configuration, SWT.NONE);
// 	        runOnlySelectedLabel.setText("Execute:");

// 	        onlySelectedLaunch = new Combo(configuration, SWT.BORDER); 
// 	        onlySelectedLaunch.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 	        
// 	        onlySelectedLaunch = new Text(configuration, SWT.BORDER);

	        /* Empty field */
// 	        Label emptyField = new Label(configuration, SWT.NONE);
//	        /* Report selection field Explanation*/
// 	        Label runOnlySelectedExplained = new Label(configuration, SWT.NONE);
// 	        FontData[] fD = runOnlySelectedExplained.getFont().getFontData();
// 	        fD[0].setHeight(8);
// 	        runOnlySelectedExplained.setFont( new Font(configuration.getDisplay(),fD[0]));
// 	        runOnlySelectedExplained.setText("Syntax:   	org.test.jmh.Benchmark, org.test.jmh.StringBenchmarks. Leave empty to run all project benchmarks.");
 	        
 	        
 	        /* Report status input field */
// 	        Label benchmarkUploadStatusLabel = new Label(configuration, SWT.NONE);
// 	        benchmarkUploadStatusLabel.setText("Report Upload Status:");
// 	        reportUploadStatus = new Combo(configuration, SWT.BORDER); 
// 	        reportUploadStatus.setItems(new String [] {"Public", "Private"});
// 	        reportUploadStatus.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
// 	        
 	        /* Report save folder path choice*/
// 	        Label reportFolderLabel = new Label(configuration, SWT.NONE);
// 	        reportFolderLabel.setText("Reports Folder:");
// 	        reportsFolder = new Text(configuration, SWT.BORDER);
// 	        
// 	        browse = new Button(configuration, SWT.PUSH);
// 	        browse.setText("Browse ...");
// 	        browse.setSize(1, 1);
// 	        browse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
// 	        browse.addSelectionListener(new SelectionAdapter() {
// 	            public void widgetSelected(SelectionEvent e) {
// 					DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NULL);
// 	                String path = dialog.open();
// 	                if (path != null) {
// 	                	reportsFolder.setText(path);
// 	                }
// 	            }
// 	
// 	        });
 	        
 	        /* User properties input */
// 	        Label userPropertiesLabel = new Label(configuration, SWT.NONE);
// 	        userPropertiesLabel.setText("User Properties:");
// 	        userProperties = new Text(configuration, SWT.BORDER);
 	        
 	        /* User save to file send t CyBench choice buttons */
// 	        Label storeReportsToFileSystemLabel = new Label(configuration, SWT.NONE);
// 	        storeReportsToFileSystemLabel.setText("Store Report In File System:");
// 	        shouldStoreReportToFileSystem = new Button(configuration, SWT.CHECK);
// 	        shouldStoreReportToFileSystem.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1));
 	        

//	        Label  accessTokenLabel = new Label(configuration, SWT.NONE);
//	        accessTokenLabel.setText("Bench Access Token:");
//	        accessToken = new Text(configuration, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
	        
	        
	        
// 	        Label sendReportsToCybenchLabel = new Label(configuration, SWT.NONE);
// 	        sendReportsToCybenchLabel.setText("Send Report To CyBench:");
// 	        shouldSendReportToCyBench = new Button(configuration, SWT.CHECK);
// 	        shouldSendReportToCyBench.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1)); 


// 	        Label doHardwarePropertiesSnapshotLabel = new Label(configuration, SWT.NONE);
// 	        doHardwarePropertiesSnapshotLabel.setText("Include Hardware Propeties");
// 	        shouldDoHardwareSnapshot = new Button(configuration, SWT.CHECK);
// 	        shouldDoHardwareSnapshot.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1)); 
//        	shouldSendReportEnableDisable();
// 	        
// 	        shouldSendReportToCyBench.addSelectionListener(new SelectionAdapter() {
//	            public void widgetSelected(SelectionEvent e) {
//	            	shouldSendReportEnableDisable();
//	            }
//	
//	        });
	        
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(reportFolderLabel);
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(reportNameLabel);
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(reportlaunchPathLabel);
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(benchmarkUploadStatusLabel);
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(runOnlySelectedLabel);
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(userPropertiesLabel);
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(accessTokenLabel);
 	        
 	       
//	        GridDataFactory.swtDefaults().span(2,1).applyTo(emptyField);
// 	        GridDataFactory.swtDefaults().span(8,1).applyTo(runOnlySelectedExplained);
 	       
 	        
// 	        GridDataFactory.fillDefaults().grab(true, false).span(7,1).applyTo(reportsFolder);
// 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(userProperties);
// 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(reportName);
// 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(launchPath);
// 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(reportUploadStatus);
// 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(onlySelectedLaunch);
// 	        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(jvmProperties);
// 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(accessToken);

	   	 	configuration = new Group(comp, SWT.NONE);
	        configuration.setText("Configuration");
	        configuration.setLayout(new GridLayout(10, false));

	        Label  jvmPropertiesLabel = new Label(configuration, SWT.NONE);
	        jvmPropertiesLabel.setText("JVM Properties:");
	        jvmProperties = new Text(configuration, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	        
	        Label  classPathPropertiesLabel = new Label(configuration, SWT.NONE);
	        classPathPropertiesLabel.setText("Classpath arguments:");
	        classPathProperties = new Text(configuration, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
	        
 	        GridDataFactory.swtDefaults().span(2,1).applyTo(jvmPropertiesLabel);
 	        GridDataFactory.swtDefaults().span(2,1).applyTo(classPathPropertiesLabel);
 	        // Class-paths layout
	        GridData classPathGrid = new GridData();
	        classPathGrid.horizontalSpan = 10;
	        classPathGrid.widthHint = GridData.FILL_HORIZONTAL;
 	        classPathGrid.heightHint = 100;
 	        
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
