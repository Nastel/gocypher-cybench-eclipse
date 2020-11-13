package com.gocypher.cybench.plugin.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import com.gocypher.cybench.plugin.model.LaunchConfiguration;

public class CybenchTabView extends AbstractLaunchConfigurationTab {

	private Group benchmarking;
	private Group configuration;
	private Group conditions;
	
    private Text reportsFolder;
    private Button browse;

    private String buildPath;
    private Combo launchPath;
    private Text reportName;
    private Combo reportUploadStatus;
    
    private Spinner forks;
    private Spinner threads;
    private Spinner measurmentIterations;
    private Spinner warmupIterations;
    private Spinner warmupSeconds;
    private Spinner measurmentSeconds;
    
    private Spinner expectedScore;
    
    private Text userProperties;
    
//    private Button shouldStoreReportToFileSystem;
    private Button shouldSendReportToCyBench;
    private Button shouldDoHardwareSnapshot;


	Map<String, String> paths =  new HashMap<>();

    @Override
    public void createControl(Composite parent) {
    	paths = getProjectPaths(true);
        Composite comp = new Group(parent, SWT.BORDER);
        setControl(comp);
    	String[] itemsArray =  paths.keySet().toArray(new String[ paths.keySet().size()]);
        GridLayoutFactory.swtDefaults().numColumns(10).applyTo(comp); 
        
        /* Benchmarking settings GROUP */
        benchmarking = prepareBenchmarkConfigurationGroup(comp);
        
        /* Configuration GROUP */
        configuration = prepareCyBenchConfigurations(comp, itemsArray);
	        
        /* Execution Conditions GROUP */
        conditions = prepareConditionGroup(comp);
     
    }
    
    private Group  prepareBenchmarkConfigurationGroup(Composite comp) {
    	benchmarking = new Group(comp, SWT.NONE);
        benchmarking.setText("Benchmarking settings");
        benchmarking.setLayout(new GridLayout(10, false));
        
	        /* Report execution number of forks */
	        Label executionForkCountLabel = new Label(benchmarking, SWT.NONE);
	        executionForkCountLabel.setText("Forks:");
	        forks = new Spinner(benchmarking, SWT.BORDER);
	        
	        /* Report execution number of threads */
	        Label executionThreadsCountLabel = new Label(benchmarking, SWT.NONE);
	        executionThreadsCountLabel.setText("Threads:");
	        threads = new Spinner(benchmarking, SWT.BORDER);
	        
	        /* Report execution number of measurement iterations */
	        Label executionMeasurmentIterationCountLabel = new Label(benchmarking, SWT.NONE);
	        executionMeasurmentIterationCountLabel.setText("Measurment Iterations:");
	        measurmentIterations = new Spinner(benchmarking, SWT.BORDER);
	        
	        /* Report execution number of warm-up iterations */
	        Label executionWarmupIterationCountLabel = new Label(benchmarking, SWT.NONE);
	        executionWarmupIterationCountLabel.setText("Warmup Iterations:");
	        warmupIterations = new Spinner(benchmarking, SWT.BORDER);
	        
	        /* Report execution number of warm-up seconds */
	        Label executionWarmupSecondsLabel = new Label(benchmarking, SWT.NONE);
	        executionWarmupSecondsLabel.setText("Warmup time (s):");
	        warmupSeconds = new Spinner(benchmarking, SWT.BORDER);
	        
	        /* Report execution number of measurement seconds */
	        Label executionMeasurmentsSecondsLabel = new Label(benchmarking, SWT.NONE);
	        executionMeasurmentsSecondsLabel.setText("Measurment time (s):");
	        measurmentSeconds = new Spinner(benchmarking, SWT.BORDER);

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

    private Group  prepareCyBenchConfigurations(Composite comp, String[] itemsArray) {
    	  
    	 configuration = new Group(comp, SWT.NONE);
         configuration.setText("Configuration");
         configuration.setLayout(new GridLayout(10, false));
 	        /* Report name input field */
 	        Label reportNameLabel = new Label(configuration, SWT.NONE);
 	        reportNameLabel.setText("Report Name:");
 	        reportName = new Text(configuration, SWT.BORDER);

 	        /* Report launch path input field */
 	        Label reportlaunchPathLabel = new Label(configuration, SWT.NONE);
 	        launchPath = new Combo(configuration, SWT.BORDER); 
 	        launchPath.setItems(itemsArray);
 	        launchPath.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 	        reportlaunchPathLabel.setText("Run Project:");
 	        launchPath.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	reportsFolder.setText(launchPath.getText()+"/reports");
	            }
	
	        });
 	        
 	        /* Report status input field */
 	        Label benchmarkUploadStatusLabel = new Label(configuration, SWT.NONE);
 	        benchmarkUploadStatusLabel.setText("Report Upload Status:");
 	        reportUploadStatus = new Combo(configuration, SWT.BORDER); 
 	        reportUploadStatus.setItems(new String [] {"Public", "Private"});
 	        reportUploadStatus.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 	        
 	        /* Report save folder path choice*/
 	        Label reportFolderLabel = new Label(configuration, SWT.NONE);
 	        reportFolderLabel.setText("Reports Folder:");
 	        reportsFolder = new Text(configuration, SWT.BORDER);
 	        
 	        browse = new Button(configuration, SWT.PUSH);
 	        browse.setText("Browse ...");
 	        browse.setSize(1, 1);
 	        browse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
 	        browse.addSelectionListener(new SelectionAdapter() {
 	            public void widgetSelected(SelectionEvent e) {
 					DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NULL);
 	                String path = dialog.open();
 	                if (path != null) {
 	                	reportsFolder.setText(path);
 	                }
 	            }
 	
 	        });
 	        
 	        /* User properties input */
 	        Label userPropertiesLabel = new Label(configuration, SWT.NONE);
 	        userPropertiesLabel.setText("User Properties:");
 	        userProperties = new Text(configuration, SWT.BORDER);
 	        
 	        /* User save to file send t CyBench choice buttons */
// 	        Label storeReportsToFileSystemLabel = new Label(configuration, SWT.NONE);
// 	        storeReportsToFileSystemLabel.setText("Store Report In File System:");
// 	        shouldStoreReportToFileSystem = new Button(configuration, SWT.CHECK);
// 	        shouldStoreReportToFileSystem.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1));
 	        
 	        Label sendReportsToCybenchLabel = new Label(configuration, SWT.NONE);
 	        sendReportsToCybenchLabel.setText("Send Report To CyBench:");
 	        shouldSendReportToCyBench = new Button(configuration, SWT.CHECK);
 	        shouldSendReportToCyBench.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1)); 

 	       

 	        Label doHardwarePropertiesSnapshotLabel = new Label(configuration, SWT.NONE);
 	        doHardwarePropertiesSnapshotLabel.setText("Include Hardware Propeties");
 	        shouldDoHardwareSnapshot = new Button(configuration, SWT.CHECK);
 	        shouldDoHardwareSnapshot.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1)); 
    		shouldDoHardwareSnapshot.setEnabled(false);
//    		shouldDoHardwareSnapshot.setVisible(false);
//    		doHardwarePropertiesSnapshotLabel.setVisible(false);
 	        
 	        shouldSendReportToCyBench.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	if(shouldSendReportToCyBench.getSelection()) {
	            		shouldDoHardwareSnapshot.setSelection(true);
	            		shouldDoHardwareSnapshot.setEnabled(false);
//	            		shouldDoHardwareSnapshot.setVisible(false);
//	            		doHardwarePropertiesSnapshotLabel.setVisible(false);
	            	}else {
	            		shouldDoHardwareSnapshot.setEnabled(true);
//	            		shouldDoHardwareSnapshot.setVisible(true);
//	            		doHardwarePropertiesSnapshotLabel.setVisible(true);
	            	}
	            }
	
	        });
	        
 	        GridDataFactory.swtDefaults().span(2,1).applyTo(reportFolderLabel);
 	        GridDataFactory.swtDefaults().span(2,1).applyTo(reportNameLabel);
 	        GridDataFactory.swtDefaults().span(2,1).applyTo(reportlaunchPathLabel);
 	        GridDataFactory.swtDefaults().span(2,1).applyTo(benchmarkUploadStatusLabel);
 	        GridDataFactory.swtDefaults().span(2,1).applyTo(userPropertiesLabel);
 	        
 	        GridDataFactory.fillDefaults().grab(true, false).span(7,1).applyTo(reportsFolder);
 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(userProperties);
 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(reportName);
 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(launchPath);
 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(reportUploadStatus);
 	        
 	        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(configuration);
 	        
			return configuration;
 	        
    }
    
    private Group prepareConditionGroup(Composite comp) {
    	  conditions = new Group(comp, SWT.NONE);
          conditions.setText("Execution Conditions");
          conditions.setLayout(new GridLayout(10, false));
  	        /* Report expected score input */
  	        Label expectedScoreLabel = new Label(conditions, SWT.NONE);
  	        expectedScoreLabel.setText("Score Less Then:");
  	        expectedScore = new Spinner(conditions, SWT.BORDER);
  	        GridDataFactory.swtDefaults().span(2,1).applyTo(expectedScoreLabel);
  	        GridDataFactory.fillDefaults().grab(true, false).span(7,1).applyTo(expectedScore);
  	        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(conditions);
			return conditions;
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			// TODO Auto-generated method stub
			setDirty(true);
	    	updateLaunchConfigurationDialog();
		}
	};
	
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
    	    Map.Entry<String,String> entry = paths.entrySet().iterator().next();
        	String launchPathDef = "";
        	String reportFolderDef = "";
        	String buildPathDef = "";
            if(entry!= null) {
            	reportFolderDef = configuration.getAttribute(LaunchConfiguration.REPORT_FOLDER, entry.getKey()+"/reports");
            	launchPathDef = configuration.getAttribute(LaunchConfiguration.LAUNCH_PATH, entry.getKey());
            	buildPathDef = configuration.getAttribute(LaunchConfiguration.BUILD_PATH, entry.getValue());
            }
            String reportNameDef = configuration.getAttribute(LaunchConfiguration.REPORT_NAME, "CyBench Report");
            String reportUploadStatusDef = configuration.getAttribute(LaunchConfiguration.BENCHMARK_REPORT_STATUS, "public");
         
            int threadDef = configuration.getAttribute(LaunchConfiguration.TREADS_COUNT, 1);
            int forksDef  = configuration.getAttribute(LaunchConfiguration.FORKS_COUNT, 1);
            int warmupIterationsDef  = configuration.getAttribute(LaunchConfiguration.WARMUP_ITERATION, 1);
            int measurmentIterationsDef = configuration.getAttribute(LaunchConfiguration.MEASURMENT_ITERATIONS, 5);
            int warmupSecondsDef = configuration.getAttribute(LaunchConfiguration.WARMUP_SECONDS, 10);
            int measurmentSecondsDef = configuration.getAttribute(LaunchConfiguration.MEASURMENT_SECONDS, 10);
            
//            boolean storeReportInFile = configuration.getAttribute(LaunchConfiguration.SHOULD_SAVE_REPOT_TO_FILE, true);
            boolean sendReportCybnech = configuration.getAttribute(LaunchConfiguration.SHOULD_SEND_REPORT_CYBENCH, true);
            boolean includehardwarePropeties = configuration.getAttribute(LaunchConfiguration.INCLUDE_HARDWARE_PROPERTIES, true);
            
            reportsFolder.setText(reportFolderDef);
            reportsFolder.addModifyListener(modifyListener);
            reportName.setText(reportNameDef);
            reportName.addModifyListener(modifyListener);
            reportUploadStatus.setText(reportUploadStatusDef);
            reportUploadStatus.addModifyListener(modifyListener);
            launchPath.setText(launchPathDef);
            launchPath.addModifyListener(modifyListener);
            
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
            expectedScore.setValues(-1, -1, 10000, 2, 1, 1);
            expectedScore.addModifyListener(modifyListener);

            userProperties.addModifyListener(modifyListener);
//            shouldStoreReportToFileSystem.setSelection(storeReportInFile);
//            shouldStoreReportToFileSystem.addSelectionListener(selectionListener);
            shouldSendReportToCyBench.setSelection(sendReportCybnech);
            shouldSendReportToCyBench.addSelectionListener(selectionListener);
            
            shouldDoHardwareSnapshot.setSelection(includehardwarePropeties);
            shouldDoHardwareSnapshot.addSelectionListener(selectionListener);
            
        } catch (CoreException e) {
        	System.out.println("There was a problem on the run configuration initialization: "+ e);
        }
    }
    
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(LaunchConfiguration.REPORT_FOLDER, reportsFolder.getText());
        configuration.setAttribute(LaunchConfiguration.REPORT_NAME, reportName.getText());
        configuration.setAttribute(LaunchConfiguration.LAUNCH_PATH, launchPath.getText());
        configuration.setAttribute(LaunchConfiguration.BENCHMARK_REPORT_STATUS, reportUploadStatus.getText());
        
        configuration.setAttribute(LaunchConfiguration.TREADS_COUNT, threads.getSelection());
        configuration.setAttribute(LaunchConfiguration.FORKS_COUNT, forks.getSelection());
        configuration.setAttribute(LaunchConfiguration.WARMUP_ITERATION, warmupIterations.getSelection());
        configuration.setAttribute(LaunchConfiguration.MEASURMENT_ITERATIONS, measurmentIterations.getSelection());
        configuration.setAttribute(LaunchConfiguration.WARMUP_SECONDS, warmupSeconds.getSelection());
        configuration.setAttribute(LaunchConfiguration.MEASURMENT_SECONDS, measurmentSeconds.getSelection());

        configuration.setAttribute(LaunchConfiguration.CUSTOM_USER_PROPERTIES, userProperties.getText());
//        configuration.setAttribute(LaunchConfiguration.SHOULD_SAVE_REPOT_TO_FILE, shouldStoreReportToFileSystem.getSelection());
        configuration.setAttribute(LaunchConfiguration.SHOULD_SEND_REPORT_CYBENCH, shouldSendReportToCyBench.getSelection());
        configuration.setAttribute(LaunchConfiguration.INCLUDE_HARDWARE_PROPERTIES, shouldDoHardwareSnapshot.getSelection());
        
        configuration.setAttribute(LaunchConfiguration.EXECUTION_SCORE, expectedScore.getSelection());
        
        String buildPath = getBuildPath(launchPath.getText());
    	configuration.setAttribute(LaunchConfiguration.BUILD_PATH,  buildPath);
    }
    
    private String getBuildPath(String buildProjectPath) {
		if(paths.get(buildProjectPath) == null) {
			return buildProjectPath;
		}else {
			return paths.get(buildProjectPath);
		}
    }

    @Override
    public String getName() {
        return "CyBench properties";
    }
    
 
    
    private Map<String, String> getProjectPaths(boolean addBuildPath) {
    	Map<String, String> projectPaths = new HashMap<>();
    	try {
    		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects() ;
	    	for(IProject proj : projects) {
	    		if(proj.isAccessible()) {
		    		String projectPackageFullPath = "";
		    		String projectOutputPath = "";
		    		if(proj.getLocation()!=null) {
		    			projectPackageFullPath = proj.getLocation().toPortableString();
		    		}
		    		if(addBuildPath) {
			    		IJavaProject javaProject = JavaCore.create(proj);
		    			if(javaProject.getOutputLocation()!=null) {
		    				projectOutputPath = projectPackageFullPath.substring(0, projectPackageFullPath.lastIndexOf('/')) + javaProject.getOutputLocation().toPortableString();
		    			}
		    		}
	    			projectPaths.put(projectPackageFullPath, projectOutputPath);
	    		}
	    	}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
    	return projectPaths;
    }

}