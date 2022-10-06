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

import org.apache.commons.validator.routines.EmailValidator;
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.model.LaunchConfiguration;
import com.gocypher.cybench.plugin.tools.preferences.PreferenceConstants;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;


public class CybenchTabView extends AbstractLaunchConfigurationTab {
	
	private Group configuration;

    private Text reportName;
    private Combo launchPath;

    private Text reportsFolder;
    private Button browse;

    private Text accessToken;
	private Text queryToken;
    private Text userEmail;
    private Label emailLabel;
    
    private Button shouldSendReportToCyBench;
    private Button shouldDoHardwareSnapshot;
    private Button shouldSendReportToPublicRepo;
    private Label repoHintText;
    
    private List leftList;
    private List rightList;
    private Button moveLeft;
    private Button moveRight;
    private Label hintText;
    
    private String benchmarkClassInput;
   
    
    private final String textForHint = ">>> If no benchmark classes will be "
    		+ "selected all project benchmarks will be executed. <<<";
    
    private final String publicRepoHint = ">>> Note: Reports sent to the public repository will be viewable by all, and you will "
    		+ "not have the ability to make comparisons or organize results. <<<";

    
	private Map<String, String> paths =  new HashMap<>();
	private ArrayList<String> projectSelectedPaths = new ArrayList<String>();

    @Override
    public void createControl(Composite parent) {
    	paths = getProjectPaths(true);
    	String[] itemsArray =  paths.keySet().toArray(new String[ paths.keySet().size()]);
    	
        Composite comp = new Group(parent, SWT.BORDER);
        setControl(comp);
        GridLayoutFactory.swtDefaults().numColumns(10).applyTo(comp); 
      
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
        
        shouldSendReportToCyBench.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	shouldSendReportEnableDisable();
            }
        });
        

        
        
        /* Empty field 3 */
        Label emptyField3 = new Label(configuration, SWT.NONE);
		GridDataFactory.swtDefaults().span(2,1).applyTo(emptyField3);
		
		
        Label shouldSendReportToPublicRepoLabel = new Label(configuration, SWT.NONE);
        shouldSendReportToPublicRepoLabel.setText("Send Report to the Public workspace:");
        shouldSendReportToPublicRepo = new Button(configuration, SWT.CHECK);
        shouldSendReportToPublicRepo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 3, 1));
        
        shouldSendReportToPublicRepo.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		shouldSendReportPublicEnableDisable();
        	}
        });
        
        shouldSendReportEnableDisable();

        
        repoHintText = new Label(configuration, SWT.NONE);
        FontData[] fD = repoHintText.getFont().getFontData();
        repoHintText.setText(publicRepoHint);
        fD[0].setHeight(8);
        Font tempFont = new Font(configuration.getDisplay(), fD[0]);
        repoHintText.setFont(tempFont);
        GridDataFactory.swtDefaults().span(2,1).applyTo(repoHintText);
        tempFont.dispose();

        
 //       Label emptyField5 = new Label(configuration, SWT.NONE);
//		GridDataFactory.swtDefaults().span(2,1).applyTo(emptyField5);
		
        Label emptyField4 = new Label(configuration, SWT.NONE);
		GridDataFactory.swtDefaults().span(2,1).applyTo(emptyField4);
		
        Label emptyField6 = new Label(configuration, SWT.NONE);
		GridDataFactory.swtDefaults().span(2,1).applyTo(emptyField6);
        
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

		Label  queryTokenLabel = new Label(configuration, SWT.NONE);
        queryTokenLabel.setText("Bench Query Token:");
        queryToken = new Text(configuration, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);

        Label  userEmailLabel = new Label(configuration, SWT.NONE);
        userEmailLabel.setText("Email Address:");
        userEmail = new Text(configuration, SWT.SINGLE | SWT.BORDER );
        

        Label emptyLabel = new Label(configuration, SWT.NONE);
        emptyLabel.setText("");
        emailLabel = new Label(configuration, SWT.NONE);
        emailLabel.setText("Only by providing correct email address the report will be associated with the user");
        emailLabel.setVisible(false);
        /* Report selection field Component */
        createBenchmarkClassesSelection(configuration);	      
        
		GridDataFactory.swtDefaults().span(2,1).applyTo(reportFolderLabel);
		GridDataFactory.swtDefaults().span(2,1).applyTo(reportNameLabel);
		GridDataFactory.swtDefaults().span(2,1).applyTo(reportlaunchPathLabel);
		GridDataFactory.swtDefaults().span(2,1).applyTo(accessTokenLabel);   
		GridDataFactory.swtDefaults().span(2,1).applyTo(queryTokenLabel);  
		GridDataFactory.swtDefaults().span(2,1).applyTo(userEmailLabel);   
		GridDataFactory.swtDefaults().span(2,1).applyTo(emptyLabel);   
		GridDataFactory.fillDefaults().grab(true, false).span(7,1).applyTo(reportsFolder);
		GridDataFactory.swtDefaults().span(1,1).applyTo(browse);
		GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(reportName);
		GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(launchPath);
		GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(accessToken);
		GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(queryToken);	
		GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(userEmail);  	
		GridDataFactory.fillDefaults().grab(true, false).span(8,1).applyTo(emailLabel);      
       
        // Class-paths layout
        GridData classPathGrid = new GridData();
        classPathGrid.horizontalSpan = 10;
        classPathGrid.widthHint = GridData.FILL_HORIZONTAL;
        classPathGrid.heightHint = 100;
        GridDataFactory.fillDefaults().grab(true, false).span(10,1).applyTo(configuration);
        
		return configuration;
 	        
    }
    private void shouldSendReportEnableDisable() {
    	if(shouldSendReportToCyBench.getSelection()) {
    		shouldDoHardwareSnapshot.setSelection(true);
    		shouldSendReportToPublicRepo.setSelection(false);
			accessToken.setEnabled(true);
			queryToken.setEnabled(true);

    		shouldDoHardwareSnapshot.setEnabled(false);
    		shouldSendReportToPublicRepo.setEnabled(true);
    	}else {
    		shouldDoHardwareSnapshot.setEnabled(true);
    		shouldSendReportToPublicRepo.setSelection(false);
    		shouldSendReportToPublicRepo.setEnabled(false);

    	}
    }
    
    private void shouldSendReportPublicEnableDisable() {
    	if (shouldSendReportToCyBench.getSelection()) {
    		if (shouldSendReportToPublicRepo.getSelection()) {
    			accessToken.setEnabled(false);
    			repoHintText.setVisible(true);
    			queryToken.setEnabled(false);
    		} else {
    			accessToken.setEnabled(true);
    			repoHintText.setVisible(false);
    			queryToken.setEnabled(true);
    		}
    	}
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    }
    
    private ModifyListener modifyListener = new ModifyListener() {
    	public void modifyText(ModifyEvent e) {
			setChangeHappened();
    	}
	};
    private ModifyListener modifyEmailListener = new ModifyListener() {
    	public void modifyText(ModifyEvent e) {
			String email = userEmail.getText();
//			GuiUtils.logInfo("userEmail.getText(): "+userEmail.getText());
	        EmailValidator validator = EmailValidator.getInstance();
	        if(email == null || email.equals("")) {
	        	emailLabel.setVisible(false);
	        }
	        else if(validator.isValid(email)) {
//				GuiUtils.logInfo("validator.isValid(email): "+validator.isValid(email));
				setChangeHappened();
	        	emailLabel.setVisible(false);
	        }else {
	        	emailLabel.setVisible(true);
	        }
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
            	reportFolderDef = configuration.getAttribute(LaunchConfiguration.REPORT_FOLDER, entry.getKey()+"/reports");
            	launchPathDef = configuration.getAttribute(LaunchConfiguration.LAUNCH_PATH, entry.getKey());
        		scanElements(launchPathDef);
                
            }
        	
        	IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        	String bToken = store.getString(PreferenceConstants.P_AUTH_TOKEN);
        	String qToken = store.getString(PreferenceConstants.P_QUERY_TOKEN);
        	String email = store.getString(PreferenceConstants.P_EMAIL);
        	
            String reportNameDef = configuration.getAttribute(LaunchConfiguration.REPORT_NAME, "");
            String pathToSourceSelectedDef = configuration.getAttribute(LaunchConfiguration.LAUNCH_SELECTED_PATH, "");
            String pathToSourceNotSelectedDef = configuration.getAttribute(LaunchConfiguration.LAUNCH_NOT_SELECTED_PATH, "");
            
            String accessTokenDef = configuration.getAttribute(LaunchConfiguration.REMOTE_CYBENCH_ACCESS_TOKEN, bToken);
            String queryTokenDef = configuration.getAttribute(LaunchConfiguration.REMOTE_CYBENCH_QUERY_TOKEN, qToken);
            String userEmailDef = configuration.getAttribute(LaunchConfiguration.USER_EMAIL_ADDRESS, email);
            boolean sendReportCybnech = configuration.getAttribute(LaunchConfiguration.SHOULD_SEND_REPORT_CYBENCH, true);
            boolean includehardwarePropeties = configuration.getAttribute(LaunchConfiguration.INCLUDE_HARDWARE_PROPERTIES, true);
            
            boolean shouldPublicUpload = configuration.getAttribute(LaunchConfiguration.SHOULD_SEND_PUBLIC_WORKSPACE, true);
            

            userEmail.setText(userEmailDef);
            userEmail.addModifyListener(modifyEmailListener);
            reportsFolder.setText(reportFolderDef);
            reportsFolder.addModifyListener(modifyListener);
            reportName.setText(reportNameDef);
            reportName.addModifyListener(modifyListener);
            launchPath.setText(launchPathDef);
            launchPath.addModifyListener(modifyListener);

            accessToken.setText(accessTokenDef);
            accessToken.addModifyListener(modifyListener);
			queryToken.setText(queryTokenDef);
			queryToken.addModifyListener(modifyListener);
            
//            onlySelectedLaunch.setText(pathToSourceSelectedDef);
            initializeSelectedBenchmarks(pathToSourceSelectedDef, pathToSourceNotSelectedDef);
            moveLeft.addSelectionListener(selectionListener);
            moveRight.addSelectionListener(selectionListener);

            shouldSendReportToCyBench.setSelection(sendReportCybnech);
            shouldSendReportToCyBench.addSelectionListener(selectionListener);
            
            shouldDoHardwareSnapshot.setSelection(includehardwarePropeties);
            shouldDoHardwareSnapshot.addSelectionListener(selectionListener);
            
            shouldSendReportToPublicRepo.setSelection(shouldPublicUpload);
            shouldSendReportToPublicRepo.addSelectionListener(selectionListener);
            
            shouldSendReportEnableDisable();
            checkIfThereAreSelectedBenchmarks();
            checkIfPublicUploadSelected();
            
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

        String notSelectedBenchmarkLaunchClasses = getNotSelectedBenchmarksFormList();
        configuration.setAttribute(LaunchConfiguration.LAUNCH_NOT_SELECTED_PATH, notSelectedBenchmarkLaunchClasses);
        
        
        configuration.setAttribute(LaunchConfiguration.REPORT_FOLDER, reportsFolder.getText());
        
        configuration.setAttribute(LaunchConfiguration.REMOTE_CYBENCH_ACCESS_TOKEN, accessToken.getText());
		configuration.setAttribute(LaunchConfiguration.REMOTE_CYBENCH_QUERY_TOKEN, queryToken.getText());
        configuration.setAttribute(LaunchConfiguration.USER_EMAIL_ADDRESS,userEmail.getText());
        configuration.setAttribute(LaunchConfiguration.SHOULD_SEND_REPORT_CYBENCH, shouldSendReportToCyBench.getSelection());
        configuration.setAttribute(LaunchConfiguration.INCLUDE_HARDWARE_PROPERTIES, shouldDoHardwareSnapshot.getSelection());
        String uploadStatus;
        
        if (shouldSendReportToPublicRepo.getSelection()) {
        	uploadStatus = "public";
        } else {
        	uploadStatus = "private";
        }
        configuration.setAttribute(LaunchConfiguration.BENCHMARK_REPORT_STATUS, uploadStatus);
  
        String buildPath = getBuildPath(launchPath.getText());
    	configuration.setAttribute(LaunchConfiguration.BUILD_PATH,  buildPath);

    	checkIfThereAreSelectedBenchmarks();
    	checkIfPublicUploadSelected();
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
    	String tempName = fullfilePath.replace(fileType, "").replace(projectName, "").replace("\\", ".");
    	tempName =  tempName.replace("src-benchmarks", "").replace("src", "").replace("test.java", "");
    	return tempName;
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
    private void initializeSelectedBenchmarks(String benchmarkClassesString, String benchmarkClassesNotSelectedString) {
      	try {
            java.util.List<String> selectedExecutionClasses = Arrays.asList(benchmarkClassesString.split(","));
            java.util.List<String> notSelectedExecutionClasses = Arrays.asList(benchmarkClassesNotSelectedString.split(","));
      		leftList.removeAll();
      		rightList.removeAll();
	        for(String prop : projectSelectedPaths) {
        	    if(!selectedExecutionClasses.contains(prop) && prop != null && !prop.equals("")) {
    	    		leftList.add(prop);
        	    }
	        }
	        for(String prop : notSelectedExecutionClasses) {
        	    if(!projectSelectedPaths.contains(prop)  && prop != null && !prop.equals("")) {
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
        middle.setLayout(new GridLayout(11, false));
        middle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridData classPathGrid = new GridData();
        classPathGrid.grabExcessHorizontalSpace = true;
        classPathGrid.heightHint = 200;
        classPathGrid.minimumHeight = 200;
        classPathGrid.minimumWidth = 420;
        classPathGrid.widthHint = 420;
	        
        
		GridDataFactory.swtDefaults().span(10,1).applyTo(middle);

        Group leftGroup = new Group(middle, SWT.FILL  | SWT.WRAP);
        leftGroup.setText("Benchmarks Class Available");
        leftGroup.setLayout(new GridLayout(10, false));
        leftGroup.setLayoutData(classPathGrid);
        
        leftList = new List(leftGroup, SWT.BORDER  | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
//        leftList.setLayoutData(listGrid);
//        ((Control) leftList).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

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
        rightGroup.setText("Benchmarks Class Selected");
        rightGroup.setLayout(new GridLayout(10, false));
        rightGroup.setLayoutData(classPathGrid);

        rightList = new List(rightGroup, SWT.BORDER  | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
//        rightList.setLayoutData(listGrid);
//        rightList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        /* Report selection field Explanation*/
        hintText = new Label(middle, SWT.NONE);
        FontData[] fD = hintText.getFont().getFontData();
    	hintText.setText(textForHint);
        fD[0].setHeight(8);
        Font tempFont = new Font(middle.getDisplay(),fD[0]);
        hintText.setFont(tempFont);
		GridDataFactory.swtDefaults().span(9,1).applyTo(hintText);
		tempFont.dispose();
		
		GridDataFactory.fillDefaults().grab(true, true).span(8,1).applyTo(leftList);	     
		GridDataFactory.fillDefaults().grab(true, true).span(8,1).applyTo(rightList);	       
		       
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
			checkIfPublicUploadSelected();
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
			checkIfPublicUploadSelected();
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
			checkIfPublicUploadSelected();
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
			checkIfPublicUploadSelected();
			checkIfThereAreSelectedBenchmarks();
		}
	};
    
    private SelectionListener showAddNewValue = new SelectionListener() {

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
				
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			openAddNewBenchmarkClassPopup();
		}
	};
	
	
	
	private String getSelectedBenchmarksFormList() {
		String[] selectedItems = new String[rightList.getItemCount()];
		for(int i = 0; i<rightList.getItemCount(); i++) {
			selectedItems[i]= rightList.getItem(i);
		}
		return String.join(",",selectedItems);
	}
	
	private String getNotSelectedBenchmarksFormList() {
		String[] selectedItems = new String[leftList.getItemCount()];
		for(int i = 0; i<leftList.getItemCount(); i++) {
			selectedItems[i]= leftList.getItem(i);
		}
		return String.join(",",selectedItems);
	}
	
	private void checkIfPublicUploadSelected() {
		if (shouldSendReportToPublicRepo.getSelection()) {
			repoHintText.setVisible(true);
		} else {
			repoHintText.setVisible(false);
		}
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
	
	private void openAddNewBenchmarkClassPopup() {
		Display.getDefault().syncExec(new Runnable() {
		    public void run() {
		    	try {
		    		Shell addNewBenhcmarkClass = new Shell();
		    		BenchmarkClassInputPopupView pop = new BenchmarkClassInputPopupView(addNewBenhcmarkClass);
					if (pop.open() == Window.OK) {
						benchmarkClassInput = pop.getBenchmarkClassInput();
						rightList.add(benchmarkClassInput);
						setChangeHappened();
					}
		    	}catch (Exception e) {			    		
		    		GuiUtils.logError ("Error  on view open",e) ;
		    	}
		    }
		});	
	}

	
}
