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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

import com.gocypher.cybench.plugin.model.LaunchConfiguration;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;
//import com.gocypher.cybench.plugin.utils.MultiCheckSelectionCombo;


public class CybenchTabView extends AbstractLaunchConfigurationTab {
	
	private Group configuration;

    private Text reportName;
    private Combo launchPath;
//    private Combo onlySelectedLaunch;

    private Text reportsFolder;
    private Button browse;

    private Text accessToken;
    
    private Button shouldSendReportToCyBench;
    private Button shouldDoHardwareSnapshot;
   
    private List leftList;
    private List rightList;
    private Button moveLeft;
    private Button moveRight;
    private Label hintText;
    
    private Button createNew;
    private Label createNewValueLable;
    private Text addBenchmarksAvailable;
    
    private final String textForHint = ">>> If no benchmark classes will be"
    		+ "selected all project benchmarks will be executed. <<<";
//    private MultiCheckSelectionCombo classesToExecute;
    
//    private Text onlySelectedLaunch;
//    private Combo reportUploadStatus;
    
//    private Spinner forks;
//    private Spinner threads;
//    private Spinner measurmentIterations;
//    private Spinner warmupIterations;
//    private Spinner warmupSeconds;
//    private Spinner measurmentSeconds;
    
//    private Spinner expectedScore;

//    private Text userProperties;
//    private Button useCyBenchBenchmarkSettings;
    
//    private Button shouldStoreReportToFileSystem;

//    private Text classPathProperties;

    
	private Map<String, String> paths =  new HashMap<>();
	
	private ArrayList<String> projectSelectedPaths = new ArrayList<String>();

    @Override
    public void createControl(Composite parent) {
    	paths = getProjectPaths(true);
    	String[] itemsArray =  paths.keySet().toArray(new String[ paths.keySet().size()]);
    	
        Composite comp = new Group(parent, SWT.BORDER);
        setControl(comp);
        GridLayoutFactory.swtDefaults().numColumns(10).applyTo(comp); 

//    	benchmarking = new Group(comp, SWT.NONE);
//        benchmarking.setText("Benchmarking settings");
//        benchmarking.setLayout(new GridLayout(10, false));
//
//        
//        Label useCyBenchBenchmarkSettingsLabel = new Label(benchmarking, SWT.NONE);
//        useCyBenchBenchmarkSettingsLabel.setText("Use CyBench Benchmark Settings:");
//        useCyBenchBenchmarkSettings = new Button(benchmarking, SWT.CHECK);
//        useCyBenchBenchmarkSettings.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 8, 1)); 

              
        /* Configuration GROUP */
        configuration = prepareCyBenchConfigurations(comp, itemsArray);
	           
    }
    
    private Group  prepareCyBenchConfigurations(Composite comp, String[] itemsArray) {
    	  
    	 configuration = new Group(comp, SWT.NONE);
         configuration.setText("Configuration");
         configuration.setLayout(new GridLayout(10, false));
         
         	Label sendReportsToCybenchLabel = new Label(configuration, SWT.NONE);
	        sendReportsToCybenchLabel.setText("Send Report To CyBench:");
	        shouldSendReportToCyBench = new Button(configuration, SWT.CHECK);
	        shouldSendReportToCyBench.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 3, 1)); 


	        Label doHardwarePropertiesSnapshotLabel = new Label(configuration, SWT.NONE);
	        doHardwarePropertiesSnapshotLabel.setText("Include Hardware Propeties");
	        shouldDoHardwareSnapshot = new Button(configuration, SWT.CHECK);
	        shouldDoHardwareSnapshot.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 3, 1)); 
	        shouldSendReportEnableDisable();
	        
	        
	        
	        shouldSendReportToCyBench.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	shouldSendReportEnableDisable();
	            }
	
	        });
	        
	        /* Empty field 3 */
 	        Label emptyField3 = new Label(configuration, SWT.NONE);
			GridDataFactory.swtDefaults().span(2,1).applyTo(emptyField3);
 	        
 	        /* Report launch path input field */
 	        Label reportlaunchPathLabel = new Label(configuration, SWT.NONE);
 	        launchPath = new Combo(configuration, SWT.BORDER); 
 	        launchPath.setItems(itemsArray);
 	        launchPath.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 	        reportlaunchPathLabel.setText("Run Project:");
 	       
           
 	        launchPath.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {
	            	setAvailbaleBenchmarksChoice(launchPath.getText());
	            	reportsFolder.setText(launchPath.getText()+"/reports");
	            }
	
	        });
 	         
 	        
 	        /* Report save folder path choice*/
 	        Label reportFolderLabel = new Label(configuration, SWT.NONE);
 	        reportFolderLabel.setText("Reports Folder:");
 	        reportsFolder = new Text(configuration, SWT.BORDER);
 	        
 	        browse = new Button(configuration, SWT.PUSH);
 	        browse.setText("Browse ...");
 	        browse.addSelectionListener(new SelectionAdapter() {
 	            public void widgetSelected(SelectionEvent e) {
 					DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NULL);
 	                String path = dialog.open();
 	                if (path != null) {
 	                	reportsFolder.setText(path);
 	                }
 	            }
 	
 	        });     


 	        /* Report name input field */
 	        Label reportNameLabel = new Label(configuration, SWT.NONE);
 	        reportNameLabel.setText("Report Name:");
 	        reportName = new Text(configuration, SWT.BORDER);
 	        
	        Label  accessTokenLabel = new Label(configuration, SWT.NONE);
	        accessTokenLabel.setText("Bench Access Token:");
	        accessToken = new Text(configuration, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
	        
 	        /* Report selection field Component */
 	        createBenchmarkClassesSelection(configuration);	      
 	        
			GridDataFactory.swtDefaults().span(2,1).applyTo(reportFolderLabel);
			GridDataFactory.swtDefaults().span(2,1).applyTo(reportNameLabel);
			GridDataFactory.swtDefaults().span(2,1).applyTo(reportlaunchPathLabel);
			GridDataFactory.swtDefaults().span(2,1).applyTo(accessTokenLabel);    
			GridDataFactory.fillDefaults().grab(true, false).span(7,1).applyTo(reportsFolder);
			GridDataFactory.swtDefaults().span(1,1).applyTo(browse);
			GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(reportName);
			GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(launchPath);
			GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(accessToken);
 	       

 	        
 	        /* Report selection field */
// 	        Label runOnlySelectedLabel = new Label(configuration, SWT.NONE);
// 	        runOnlySelectedLabel.setText("Execute:");
//
// 	        onlySelectedLaunch = new Combo(configuration, SWT.BORDER); 
// 	        onlySelectedLaunch.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

 	        /* User save to file send t CyBench choice buttons */
// 	        Label storeReportsToFileSystemLabel = new Label(configuration, SWT.NONE);
// 	        storeReportsToFileSystemLabel.setText("Store Report In File System:");
// 	        shouldStoreReportToFileSystem = new Button(configuration, SWT.CHECK);
// 	        shouldStoreReportToFileSystem.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1)); 	   
	        
//	        Label  classPathPropertiesLabel = new Label(configuration, SWT.NONE);
//	        classPathPropertiesLabel.setText("Classpath arguments:");
//	        classPathProperties = new Text(configuration, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
			
// 	        onlySelectedLaunch = new Text(configuration, SWT.BORDER);
 	        /* User properties input */
// 	        Label userPropertiesLabel = new Label(configuration, SWT.NONE);
// 	        userPropertiesLabel.setText("User Properties:");
// 	        userProperties = new Text(configuration, SWT.BORDER);
 	        
// 	        Label  jvmPropertiesLabel = new Label(configuration, SWT.NONE);
// 	        jvmPropertiesLabel.setText("JVM Properties:");
//	        jvmProperties = new Text(configuration, SWT.BORDER);
 	        /* User save to file send t CyBench choice buttons */
// 	        Label storeReportsToFileSystemLabel = new Label(configuration, SWT.NONE);
// 	        storeReportsToFileSystemLabel.setText("Store Report In File System:");
// 	        shouldStoreReportToFileSystem = new Button(configuration, SWT.CHECK);
// 	        shouldStoreReportToFileSystem.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 4, 1));

//	        Label  classPathPropertiesLabel = new Label(configuration, SWT.NONE);
//	        classPathPropertiesLabel.setText("Classpath arguments:");
//	        classPathProperties = new Text(configuration, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
 	        
 	        /* Report status input field */
// 	        Label benchmarkUploadStatusLabel = new Label(configuration, SWT.NONE);
// 	        benchmarkUploadStatusLabel.setText("Report Upload Status:");
// 	        reportUploadStatus = new Combo(configuration, SWT.BORDER); 
// 	        reportUploadStatus.setItems(new String [] {"Public", "Private"});
// 	        reportUploadStatus.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
 	        
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(benchmarkUploadStatusLabel);
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(userPropertiesLabel);
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(jvmPropertiesLabel);
// 	        GridDataFactory.swtDefaults().span(2,1).applyTo(classPathPropertiesLabel);
 	        
// 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(userProperties);
// 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(reportUploadStatus);
// 	        GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(jvmProperties);
 	        
 	       
 	        // Class-paths layout
	        GridData classPathGrid = new GridData();
	        classPathGrid.horizontalSpan = 10;
	        classPathGrid.widthHint = GridData.FILL_HORIZONTAL;
 	        classPathGrid.heightHint = 100;
// 	       	GridDataFactory.createFrom(classPathGrid).applyTo(classPathProperties);
 	        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(configuration);
 	        
			return configuration;
 	        
    }
    private void shouldSendReportEnableDisable() {
    	if(shouldSendReportToCyBench.getSelection()) {
    		shouldDoHardwareSnapshot.setSelection(true);
    		shouldDoHardwareSnapshot.setEnabled(false);
    	}else {
    		shouldDoHardwareSnapshot.setEnabled(true);
    	}
    }
//    private Group prepareConditionGroup(Composite comp) {
//    	  conditions = new Group(comp, SWT.NONE);
//          conditions.setText("Execution Conditions");
//          conditions.setLayout(new GridLayout(10, false));
//  	        /* Report expected score input */
//  	        Label expectedScoreLabel = new Label(conditions, SWT.NONE);
//  	        expectedScoreLabel.setText("Score Less Then:");
//  	        expectedScore = new Spinner(conditions, SWT.BORDER);
//  	        GridDataFactory.swtDefaults().span(2,1).applyTo(expectedScoreLabel);
//  	        GridDataFactory.fillDefaults().grab(true, false).span(7,1).applyTo(expectedScore);
//  	        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(conditions);
//			return conditions;
//    }
    
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    }
    
    private ModifyListener modifyListener = new ModifyListener() {
    	public void modifyText(ModifyEvent e) {
			setChangeHappened();
    	}
	};
	
    private SelectionListener selectionListener = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
				
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			setChangeHappened();
		}
	};
	
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
    	    Map.Entry<String,String> entry = paths.entrySet().iterator().next();
        	String launchPathDef = "";
        	String reportFolderDef = "";
        	if(entry!= null) {
        		scanElements(entry.getKey());
            	reportFolderDef = configuration.getAttribute(LaunchConfiguration.REPORT_FOLDER, entry.getKey()+"/reports");
            	launchPathDef = configuration.getAttribute(LaunchConfiguration.LAUNCH_PATH, entry.getKey());
                
            }
            String reportNameDef = configuration.getAttribute(LaunchConfiguration.REPORT_NAME, "");
            String pathToSourceSelectedDef = configuration.getAttribute(LaunchConfiguration.LAUNCH_SELECTED_PATH, "");
            
            String accessTokenDef = configuration.getAttribute(LaunchConfiguration.REMOTE_CYBENCH_ACCESS_TOKEN, "");
            boolean sendReportCybnech = configuration.getAttribute(LaunchConfiguration.SHOULD_SEND_REPORT_CYBENCH, true);
            boolean includehardwarePropeties = configuration.getAttribute(LaunchConfiguration.INCLUDE_HARDWARE_PROPERTIES, true);
            
            
            reportsFolder.setText(reportFolderDef);
            reportsFolder.addModifyListener(modifyListener);
            reportName.setText(reportNameDef);
            reportName.addModifyListener(modifyListener);
            launchPath.setText(launchPathDef);
            launchPath.addModifyListener(modifyListener);

            accessToken.setText(accessTokenDef);
            accessToken.addModifyListener(modifyListener);
            
//            onlySelectedLaunch.setText(pathToSourceSelectedDef);
            initializeSelectedBenchmarks(pathToSourceSelectedDef);
            moveLeft.addSelectionListener(selectionListener);
            moveRight.addSelectionListener(selectionListener);

            shouldSendReportToCyBench.setSelection(sendReportCybnech);
            shouldSendReportToCyBench.addSelectionListener(selectionListener);
            
            shouldDoHardwareSnapshot.setSelection(includehardwarePropeties);
            shouldDoHardwareSnapshot.addSelectionListener(selectionListener);
            shouldSendReportEnableDisable();


			checkIfThereAreSelectedBenchmarks();
//          String reportUploadStatusDef = configuration.getAttribute(LaunchConfiguration.BENCHMARK_REPORT_STATUS, "private");
//        String userArguments = configuration.getAttribute(LaunchConfiguration.CUSTOM_USER_PROPERTIES, "");
//        String jvmArguments = configuration.getAttribute(LaunchConfiguration.CUSTOM_JVM_PROPERTIES, "");
            
//          int threadDef = configuration.getAttribute(LaunchConfiguration.TREADS_COUNT, 1);
//          int forksDef  = configuration.getAttribute(LaunchConfiguration.FORKS_COUNT, 1);
//          int warmupIterationsDef  = configuration.getAttribute(LaunchConfiguration.WARMUP_ITERATION, 1);
//          int measurmentIterationsDef = configuration.getAttribute(LaunchConfiguration.MEASURMENT_ITERATIONS, 5);
//          int warmupSecondsDef = configuration.getAttribute(LaunchConfiguration.WARMUP_SECONDS, 10);
//          int measurmentSecondsDef = configuration.getAttribute(LaunchConfiguration.MEASURMENT_SECONDS, 10);
          
//          boolean storeReportInFile = configuration.getAttribute(LaunchConfiguration.SHOULD_SAVE_REPOT_TO_FILE, true);
//          boolean useCybenchbenchmarkSettingsDef = configuration.getAttribute(LaunchConfiguration.USE_CYBNECH_BENCHMARK_SETTINGS, true);
        
//          String classPathArguments = configuration.getAttribute(LaunchConfiguration.ADD_CUSTOM_CLASS_PATH, "");
//          reportUploadStatus.setText(reportUploadStatusDef);
//          reportUploadStatus.addModifyListener(modifyListener);
            
//            forks.setValues(forksDef, 1, 10000, 0, 1, 1);
//            forks.addModifyListener(modifyListener);
//            threads.setValues(threadDef, 1, 100, 0, 1, 1);
//            threads.addModifyListener(modifyListener);
//            measurmentIterations.setValues(measurmentIterationsDef, 1, 10000, 0, 1, 1);
//            measurmentIterations.addModifyListener(modifyListener);
//            warmupIterations.setValues(warmupIterationsDef, 1, 10000, 0, 1, 1);
//            warmupIterations.addModifyListener(modifyListener);
//            warmupSeconds.setValues(warmupSecondsDef, 1, 10000, 0, 1, 1);
//            warmupSeconds.addModifyListener(modifyListener);
//            measurmentSeconds.setValues(measurmentSecondsDef, 1, 10000, 0, 1, 1);
//            measurmentSeconds.addModifyListener(modifyListener);
//            expectedScore.setValues(-1, -1, 10000, 2, 1, 1);
//            expectedScore.addModifyListener(modifyListener);

//            userProperties.setText(userArguments);
//            userProperties.addModifyListener(modifyListener);
//            jvmProperties.setText(jvmArguments);
//            jvmProperties.addModifyListener(modifyListener);
//            shouldStoreReportToFileSystem.setSelection(storeReportInFile);
//            shouldStoreReportToFileSystem.addSelectionListener(selectionListener);
            
//            useCyBenchBenchmarkSettings.setSelection(useCybenchbenchmarkSettingsDef);
//            useCyBenchBenchmarkSettings.addSelectionListener(selectionListener);
            
//            classPathProperties.setText(classPathArguments);
//            classPathProperties.addModifyListener(modifyListener);
            
//            useCybenchEnableDisable();
            
        } catch (CoreException e) {
        	GuiUtils.logError("There was a problem on the run configuration initialization: ", e);
        }
    }
    
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(LaunchConfiguration.REPORT_NAME, reportName.getText());
        configuration.setAttribute(LaunchConfiguration.LAUNCH_PATH, launchPath.getText());
        
        String selectedBenchmarkLaunchClasses = getSelectedBenchmarksFormList();
        configuration.setAttribute(LaunchConfiguration.LAUNCH_SELECTED_PATH, selectedBenchmarkLaunchClasses);
        
        configuration.setAttribute(LaunchConfiguration.REPORT_FOLDER, reportsFolder.getText());
        
        configuration.setAttribute(LaunchConfiguration.REMOTE_CYBENCH_ACCESS_TOKEN, accessToken.getText());
        configuration.setAttribute(LaunchConfiguration.SHOULD_SEND_REPORT_CYBENCH, shouldSendReportToCyBench.getSelection());
        configuration.setAttribute(LaunchConfiguration.INCLUDE_HARDWARE_PROPERTIES, shouldDoHardwareSnapshot.getSelection());
        String buildPath = getBuildPath(launchPath.getText());
    	configuration.setAttribute(LaunchConfiguration.BUILD_PATH,  buildPath);

		checkIfThereAreSelectedBenchmarks();
//      configuration.setAttribute(LaunchConfiguration.CUSTOM_JVM_PROPERTIES, jvmProperties.getText());
//        configuration.setAttribute(LaunchConfiguration.USE_CYBNECH_BENCHMARK_SETTINGS, useCyBenchBenchmarkSettings.getSelection());
//        configuration.setAttribute(LaunchConfiguration.ADD_CUSTOM_CLASS_PATH,  classPathProperties.getText());
        
    	
//        configuration.setAttribute(LaunchConfiguration.BENCHMARK_REPORT_STATUS, reportUploadStatus.getText());
        
//        configuration.setAttribute(LaunchConfiguration.TREADS_COUNT, threads.getSelection());
//        configuration.setAttribute(LaunchConfiguration.FORKS_COUNT, forks.getSelection());
//        configuration.setAttribute(LaunchConfiguration.WARMUP_ITERATION, warmupIterations.getSelection());
//        configuration.setAttribute(LaunchConfiguration.MEASURMENT_ITERATIONS, measurmentIterations.getSelection());
//        configuration.setAttribute(LaunchConfiguration.WARMUP_SECONDS, warmupSeconds.getSelection());
//        configuration.setAttribute(LaunchConfiguration.MEASURMENT_SECONDS, measurmentSeconds.getSelection());

//        configuration.setAttribute(LaunchConfiguration.CUSTOM_USER_PROPERTIES, userProperties.getText());
//        configuration.setAttribute(LaunchConfiguration.SHOULD_SAVE_REPOT_TO_FILE, shouldStoreReportToFileSystem.getSelection());
        
//        configuration.setAttribute(LaunchConfiguration.EXECUTION_SCORE, expectedScore.getSelection());
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
        return "CyBench Configuration";
    }
    

    private Map<String, String> getProjectPaths(boolean addBuildPath) {
    	Map<String, String> projectPaths = new HashMap<>();

    	try {
    		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects() ;
	    	for(IProject proj : projects) {
	    		if(proj.isAccessible()) {
		    		String projectPackageFullPath = "";
		    		String projectOutputPath = "";
		    		if(proj.getLocation()!=null && LauncherUtils.isJavaProject(proj)) {
		    			projectPackageFullPath = proj.getLocation().toPortableString();
		    			if(addBuildPath) {
				    		IJavaProject javaProject = JavaCore.create(proj);
			    			if(javaProject.getOutputLocation()!=null) {
			    				projectOutputPath = projectPackageFullPath.substring(0, projectPackageFullPath.lastIndexOf('/')) + javaProject.getOutputLocation().toPortableString();
			    			}
			    		}
		    			projectPaths.put(projectPackageFullPath, projectOutputPath);
		    		}
		    		
	    		}
	    	}		
		} catch (Exception e) {
			GuiUtils.logError("Error on get project paths",e);
		}
    	return projectPaths;
    }
    
// --------------------------- Classes for execution -----------------------------
    /**
     *  Scans project classpath searching for files that contain benchmarks
     * @param filePath
     */
    public void scanElements(String filePath){
    	projectSelectedPaths = new ArrayList<String>();
        try (Stream<Path> paths = Files.walk(Paths.get(filePath))) {
        	ArrayList<Path> javaFiles = new ArrayList<Path>();
        	paths.filter(Files::isRegularFile).forEach(a -> javaFiles.add(a));
        	for(Path file : javaFiles){
        		 if(file.getFileName().toString().endsWith(".java")) {
        			 if(searchStringFromFile(file, "@Benchmark")) {
        				 String packageFilePath = prepareFullPathForPackage(filePath, ".java", file.toString());
        				 packageFilePath = replaceLeadingDots(packageFilePath);
        				 projectSelectedPaths.add(packageFilePath);
        			 }
        		 }
        	}
        
        } catch (Exception e) {
			 GuiUtils.logError("Problem reading project benchmark files: ",e);
        }
    }

    /**
     * 
     * @param Try to find provided string inside the provided File
     * @param searchTerm
     * @return
     */
    private boolean searchStringFromFile(Path path, String searchTerm) {
		 try(Stream <String> streamOfLines = Files.lines(path)) {
		  Optional <String> line = streamOfLines.filter(l -> l.contains(searchTerm)).findFirst();
		  if(line.isPresent()){
		   return true;
		  }
		 }catch(Exception e) {
			 GuiUtils.logError("Problem reading project benchmark files: ",e);
		 }
	   return false;
	}
    
    /**
     * Replace the file path with the correct package path for benchmark class
     * @param projectName
     * @param fileType
     * @param fullfilePath
     * @return
     */
    private String prepareFullPathForPackage(String projectName, String fileType, String fullfilePath) {
    	projectName = projectName.replace("/", "\\");
    	return fullfilePath.replace(fileType, "").replace(projectName, "").replace("\\", ".").replace("src-benchmarks", "").replace("src", "");
    }
    
    /**
     * Set the available choices into the left List field
     * @param path
     */
    private void setAvailbaleBenchmarksChoice(String path) {
      	try {
      		leftList.removeAll();
      		rightList.removeAll();
	   	    scanElements(path);
	        String[] benchmarksArray = new String[projectSelectedPaths.size()];
	        projectSelectedPaths.toArray(benchmarksArray);
	        for(int i=0; i< benchmarksArray.length; i++) {
		        leftList.add(benchmarksArray[i]);
	        }
	    }catch(Exception ex) {
	    	GuiUtils.logError("Problem while trying to update the benchmark classes available: ",ex);
	    }
    }
    /**
     * Set the available choices into the left List field
     * @param path
     */
    private void initializeSelectedBenchmarks(String benchmarkClassesString) {
      	try {
            java.util.List<String> selectedExecutionClasses = Arrays.asList(benchmarkClassesString.split(","));
      		leftList.removeAll();
      		rightList.removeAll();
	        for(String prop : projectSelectedPaths) {
        	    if(!selectedExecutionClasses.contains(prop)) {
        	    	leftList.add(prop);
        	    }
	        }
	        for(String prop : selectedExecutionClasses) {
	        	if(prop != null && !prop.equals("")) {
	        		rightList.add(prop);
	        	}
	        }
	    }catch(Exception ex) {
	    	GuiUtils.logError("Problem while trying to update the benchmark classes available: ",ex);
	    }
    }
    
    
    /**
     * Removing leading dots from strings
     * @param input
     * @return
     */
    private String replaceLeadingDots(String input) {
    	String val = input;
    	for (int i = 0; i < input.length(); i++){   
    	    if(input.charAt(i) == '.') {
    	    	val = val.substring(1);
    	    }else {
    	    	break;
    	    }
    	}
		return val;
    }
    
    
    /**
     * The component to select the project benchmark classes to be executed during the run
     * @param composite
     */
    private void createBenchmarkClassesSelection(Composite composite)
    {
        Composite middle = new Composite(composite, SWT.FILL);
        middle.setLayout(new GridLayout(9, false));
        middle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        
        GridData classPathGrid = new GridData();
//        classPathGrid.horizontalSpan = 5;
        classPathGrid.grabExcessHorizontalSpace = true;
        classPathGrid.heightHint = 200;
        classPathGrid.widthHint = 450;
//        classPathGrid.minimumWidth = 200;
	        
        

		GridDataFactory.swtDefaults().span(10,1).applyTo(middle);

        Group leftGroup = new Group(middle, SWT.FILL  | SWT.WRAP);
        leftGroup.setText("Benchmarks Available");
        leftGroup.setLayout(new GridLayout(4, false));
        leftGroup.setLayoutData(classPathGrid);
//       	GridDataFactory.createFrom(classPathGrid).applyTo(leftGroup);
        
        leftList = new List(leftGroup, SWT.BORDER  | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
        ((Control) leftList).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Composite buttons = new Composite(middle, SWT.FILL);
        buttons.setLayout(new GridLayout(1, false));

        moveRight = new Button(buttons, SWT.PUSH);
        moveRight.setText(">");
        moveRight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        moveRight.addSelectionListener(moveBenchmarkClassToRightSideListener);
        
        moveLeft = new Button(buttons, SWT.PUSH);
        moveLeft.setText("<");
        moveLeft.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        moveLeft.addSelectionListener(moveBenchmarkClassToLeftSideListener);
               
        Button moveAllRight = new Button(buttons, SWT.PUSH);
        moveAllRight.setText(">>");
        moveAllRight.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        moveAllRight.addSelectionListener(moveAllToRightSide);
        
        Button moveAllLeft = new Button(buttons, SWT.PUSH);
        moveAllLeft.setText("<<");
        moveAllLeft.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        moveAllLeft.addSelectionListener(moveAllToLeftSide);
        
        Button addNew = new Button(buttons, SWT.PUSH);
        addNew.setText("Create");
        addNew.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
        addNew.addSelectionListener(showAddNewValue);

        Group rightGroup = new Group(middle, SWT.FILL | SWT.WRAP );
        rightGroup.setText("Benchmarks Selected");
        rightGroup.setLayout(new GridLayout(4, false));
        rightGroup.setLayoutData(classPathGrid);
//       	GridDataFactory.createFrom(classPathGrid).applyTo(rightGroup);

        rightList = new List(rightGroup, SWT.BORDER  | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
        rightList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        /* Report selection field Explanation*/
        hintText = new Label(middle, SWT.NONE);
        FontData[] fD = hintText.getFont().getFontData();
    	hintText.setText(textForHint);
        fD[0].setHeight(8);
        hintText.setFont( new Font(middle.getDisplay(),fD[0]));
		GridDataFactory.swtDefaults().span(9,1).applyTo(hintText);
		
		createNewValueLable = new Label(configuration, SWT.NONE);
		createNewValueLable.setText("Add new benchmark:");
		createNewValueLable.setVisible(false);
        addBenchmarksAvailable = new Text(configuration, SWT.SINGLE | SWT.BORDER);	      
		addBenchmarksAvailable.setVisible(false);
		
	    createNew = new Button(configuration, SWT.PUSH);
	    createNew.setText("Add New");  
	    createNew.addSelectionListener(addNewValue);
	    createNew.setVisible(false);

		GridDataFactory.swtDefaults().span(2,1).applyTo(createNewValueLable); 
		GridDataFactory.fillDefaults().grab(true, false).span(6,1).applyTo(addBenchmarksAvailable);
		GridDataFactory.swtDefaults().grab(true, false).span(1,1).applyTo(createNew);    

    }
   
    
    private SelectionListener moveBenchmarkClassToRightSideListener = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
				
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			int selectedField = leftList.getSelectionIndex();
			if(selectedField != -1) {
				rightList.add(leftList.getItem(selectedField));
				leftList.remove(selectedField);
			}
			checkIfThereAreSelectedBenchmarks();
		}
	};
	
    private SelectionListener moveBenchmarkClassToLeftSideListener = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
				
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			int selectedField = rightList.getSelectionIndex();
			if(selectedField != -1) {
				leftList.add(rightList.getItem(selectedField));
				rightList.remove(selectedField);
			}
			checkIfThereAreSelectedBenchmarks();
		}
	};
	
    private SelectionListener moveAllToRightSide = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
				
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			for(int i = 0; i< leftList.getItemCount(); i++) {
				rightList.add(leftList.getItem(i));
			}
			leftList.removeAll();
			setChangeHappened();
			checkIfThereAreSelectedBenchmarks();
		}
	};
	
	
    private SelectionListener moveAllToLeftSide = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
				
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			for(int i = 0; i< rightList.getItemCount(); i++) {
				leftList.add(rightList.getItem(i));
			}
			rightList.removeAll();
			setChangeHappened();
			checkIfThereAreSelectedBenchmarks();
		}
	};
    
    private SelectionListener showAddNewValue = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
				
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			createNewValueLable.setVisible(true);
			addBenchmarksAvailable.setVisible(true);
			createNew.setVisible(true);
		}
	};
	
	private SelectionListener addNewValue = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
				
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			String inputText = addBenchmarksAvailable.getText();
			if(inputText != null && inputText != "") {
				rightList.add(inputText);
				checkIfThereAreSelectedBenchmarks();
				createNewValueLable.setVisible(false);
				addBenchmarksAvailable.setVisible(false);
				createNew.setVisible(false);
				setChangeHappened();
			}
		}
	};
	
	private String getSelectedBenchmarksFormList() {
		String[] selectedItems = new String[rightList.getItemCount()];
		for(int i = 0; i<rightList.getItemCount(); i++) {
			selectedItems[i]= rightList.getItem(i);
		}
		return String.join(",",selectedItems);
	}
	
	private void checkIfThereAreSelectedBenchmarks() {
		 if(rightList.getItemCount()>0) {
			hintText.setVisible(false);
	    }else {
			hintText.setVisible(true);
	    }
	}
	
	private void setChangeHappened() {
		setDirty(true);
    	updateLaunchConfigurationDialog();
	}

}
