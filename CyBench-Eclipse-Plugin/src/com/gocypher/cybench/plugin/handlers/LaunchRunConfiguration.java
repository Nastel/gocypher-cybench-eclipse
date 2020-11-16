package com.gocypher.cybench.plugin.handlers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;
import org.openjdk.jmh.generators.annotations.APGeneratorDestinaton;
import org.openjdk.jmh.generators.annotations.APGeneratorSource;
import org.openjdk.jmh.generators.core.BenchmarkGenerator;
import org.openjdk.jmh.generators.core.GeneratorDestination;
import org.openjdk.jmh.generators.core.GeneratorSource;

import com.gocypher.cybench.core.utils.JSONUtils;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.model.LaunchConfiguration;
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.utils.LauncherUtils;

public class LaunchRunConfiguration extends org.eclipse.debug.core.model.LaunchConfigurationDelegate {
	
	private String reportFolder;
	private String reportName;
	private String launchPath;
	private String reportUploadStatus;
	private int thread;
	private int forks;
	private int warmupIterations;
	private int measurmentIterations;
	private int warmupSeconds;
	private int mesurmentSeconds;
	private boolean sendReportCybnech; 
	private boolean includeHardware;
	private String userProperties;
	private int excutionScoreBoundary ;
	String selectionFolderPath;
	Set<String> classPaths = new LinkedHashSet<>();
    
	public static String resolveBundleLocation (String bundleSymbolicName, boolean shouldAddBin) {
		try {
			URL pluginURL = FileLocator.resolve(Platform.getBundle(bundleSymbolicName).getEntry("/"));
			String pluginInstallDir = pluginURL.getPath().trim().replace("!/", "").replace("jar:", "").replace("file:", "");
			if( Platform.getOS().compareTo(Platform.OS_WIN32) == 0 ) {
				if (shouldAddBin && !pluginInstallDir.endsWith("jar")) {
					return  pluginInstallDir.substring(1)+"bin" ;
				}
				if (pluginInstallDir.endsWith("/")) {
					return  pluginInstallDir.substring(1,pluginInstallDir.length()-1) ;
				}
				return  pluginInstallDir.substring(1) ;
			}
			return pluginInstallDir ; 
			
		}catch (Exception ex) {
			ex.printStackTrace();
		}
		return "" ;
	}
	
	private void setCorrectClassRelativePath() {
		 try {
			Files.list(new File(selectionFolderPath).toPath())
			    .limit(10)
			    .forEach(path -> {
			        System.out.println(path);
			    });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    private void setClasspathSelectionEntries() {
	    try {
	    	IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects() ;
	    	for(IProject proj : projects) {
	    		if(proj.isAccessible()) {
		    		String projectPackageFullPath = "";
		    		String projectOutputPath = "";
		    		if(proj.getLocation()!=null) {
		    			projectPackageFullPath = proj.getLocation().toPortableString();
		    		}
		    		IJavaProject javaProject = JavaCore.create(proj);
	    			if(javaProject.getOutputLocation()!=null) {
	    				projectOutputPath = projectPackageFullPath.substring(0, projectPackageFullPath.lastIndexOf('/')) + javaProject.getOutputLocation().toPortableString();
	    			}
	    			System.out.println(projectPackageFullPath +" : "+ projectOutputPath);
	    		}
	    	}
	    	IPath stringPath = new Path(selectionFolderPath);
    	    System.out.println("IPath: "+stringPath);
	    	IFolder res = ResourcesPlugin.getWorkspace().getRoot().getFolder(stringPath);
    	    System.out.println("IFolder: "+res);
		    classPaths = LauncherUtils.addClasses(res.members(), classPaths);
		} catch (CoreException e) {
			e.printStackTrace();
		}
    }
	
	@Override
	public void launch(ILaunchConfiguration configuration, String arg1, ILaunch arg2, IProgressMonitor arg3)
			throws CoreException {
		try {
			
	    	setRunConfigurationProperties(configuration);
	    	selectionFolderPath = selectionFolderPath.replaceAll("\\s+","");
//	    	setCorrectClassRelativePath();
	    	
			String pathToTempReportPlainFile = CybenchUtils.generatePlainReportFilename(reportFolder, true, reportName.replaceAll(" ", "_")) ;
			String pathToTempReportEncryptedFile = CybenchUtils.generateEncryptedReportFilename(reportFolder, true, reportName.replaceAll(" ", "_")) ;
	
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager() ;
			ILaunchConfigurationType launchType = manager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
			final ILaunchConfigurationWorkingCopy config = launchType.newInstance(null, "CyBench plugin");
			    
			setEnvironmentProperties(config);
			
			config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
			String[] classpath = new String[] { launchPath
					,LauncherUtils.resolveBundleLocation(Activator.PLUGIN_ID, true)
					,LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID,false) 
					};
			
		
			List<String> classpathMementos = new ArrayList<String>();
			for (int i = 0; i < classpath.length; i++) {
			    IRuntimeClasspathEntry cpEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(classpath[i]));
			    cpEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
			    try {
			        classpathMementos.add(cpEntry.getMemento());
			    } catch (CoreException e) {
			        System.err.println(e.getMessage());
			    }
			}
			System.out.println("Classpath:"+classpathMementos);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpathMementos);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "\""+pathToTempReportPlainFile+"\" \""+pathToTempReportEncryptedFile+"\"");
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.gocypher.cybench.launcher.CyBenchLauncher");
			
			
			new Thread ( new Runnable() {
				
				@Override
				public void run() {
					try {
					    ILaunch launchedBenchmarks = config.launch(ILaunchManager.RUN_MODE, null);
					    while (!launchedBenchmarks.isTerminated()) {
					    	try {
					    		Thread.sleep(1000);
					    	}catch (Exception e) {
					    		
					    	}
					    }
						GuiUtils.refreshCybenchExplorer();			
						GuiUtils.openReportDisplayView(pathToTempReportPlainFile);		
					} catch (CoreException e) {
					    System.err.println(e.getMessage());
					}
				}
			}).start();
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
    private void setEnvironmentProperties(ILaunchConfigurationWorkingCopy config) {
		System.out.println("-DFORKS_COUNT="+forks+
				" -DTHREADS_COUNT="+thread+
				" -DREPORT_NAME=\""+reportName+"\""+
				" -DBENCHMARK_REPORT_STATUS=\""+reportUploadStatus+"\""+
				" -DWARMUP_ITERATION="+warmupIterations+
				" -DMEASURMENT_ITERATIONS="+measurmentIterations+
				" -DWARMUP_SECONDS="+warmupSeconds+
				" -DMEASURMENT_SECONDS="+mesurmentSeconds+
//				" -DSHOULD_SAVE_REPOT_TO_FILE="+storeReportInFile+
				" -DSHOULD_SEND_REPORT_CYBENCH="+sendReportCybnech+
				" -DINCLUDE_HARDWARE_PROPERTIES="+includeHardware+
				" -DEXECUTION_SCORE="+excutionScoreBoundary+
				" -DCUSTOM_USER_PROPERTIES=\""+userProperties+"\""+
				" -DREPORT_CLASSES=\""+selectionFolderPath+"\"");
		
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "-DFORKS_COUNT="+forks+
				" -DTHREADS_COUNT="+thread+
				" -DREPORT_NAME=\""+reportName+"\""+
				" -DBENCHMARK_REPORT_STATUS=\""+reportUploadStatus+"\""+
				" -DWARMUP_ITERATION="+warmupIterations+
				" -DMEASURMENT_ITERATIONS="+measurmentIterations+
				" -DWARMUP_SECONDS="+warmupSeconds+
				" -DMEASURMENT_SECONDS="+mesurmentSeconds+
//				" -DSHOULD_SAVE_REPOT_TO_FILE="+storeReportInFile+
				" -DSHOULD_SEND_REPORT_CYBENCH="+sendReportCybnech+
				" -DINCLUDE_HARDWARE_PROPERTIES="+includeHardware+
				" -DEXECUTION_SCORE="+excutionScoreBoundary+
				" -DCUSTOM_USER_PROPERTIES=\""+userProperties+"\""+
				" -DREPORT_CLASSES=\""+selectionFolderPath+"\"");
		
    }
    private void setRunConfigurationProperties(ILaunchConfiguration configuration) throws CoreException {
	   reportFolder = configuration.getAttribute(LaunchConfiguration.REPORT_FOLDER, "/report");
       reportName = configuration.getAttribute(LaunchConfiguration.REPORT_NAME, "CyBench Report");
       reportUploadStatus = configuration.getAttribute(LaunchConfiguration.BENCHMARK_REPORT_STATUS, "public");
      
       thread = configuration.getAttribute(LaunchConfiguration.TREADS_COUNT, 1);
       forks  = configuration.getAttribute(LaunchConfiguration.FORKS_COUNT, 1);
       warmupIterations  = configuration.getAttribute(LaunchConfiguration.WARMUP_ITERATION, 1);
       measurmentIterations = configuration.getAttribute(LaunchConfiguration.MEASURMENT_ITERATIONS, 5);
       warmupSeconds = configuration.getAttribute(LaunchConfiguration.WARMUP_SECONDS, 10);
       mesurmentSeconds = configuration.getAttribute(LaunchConfiguration.MEASURMENT_SECONDS, 10);
       
//       storeReportInFile = configuration.getAttribute(LaunchConfiguration.SHOULD_SAVE_REPOT_TO_FILE, true);
       sendReportCybnech = configuration.getAttribute(LaunchConfiguration.SHOULD_SEND_REPORT_CYBENCH, true);
       includeHardware = configuration.getAttribute(LaunchConfiguration.INCLUDE_HARDWARE_PROPERTIES, true);
       
       
       userProperties = configuration.getAttribute(LaunchConfiguration.CUSTOM_USER_PROPERTIES, "");
   	   excutionScoreBoundary = configuration.getAttribute(LaunchConfiguration.EXECUTION_SCORE, -1);
   	   
   	   launchPath = configuration.getAttribute(LaunchConfiguration.BUILD_PATH, "");
   	   selectionFolderPath =configuration.getAttribute(LaunchConfiguration.LAUNCH_SELECTED_PATH, "");
    }
    
    
//	addClasses(folder.members(), selectionEntry);
}