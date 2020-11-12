package com.gocypher.cybench.plugin.handlers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
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
	public static MessageConsole findConsole(String name) {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (name.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      //no console found, so create a new one
	      MessageConsole myConsole = new MessageConsole(name, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	   }
	public static void showMsgBox (String msg,ExecutionEvent event) {
		try {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openInformation(
					window.getShell(),
					"CyBench plugin",
					msg);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String arg1, ILaunch arg2, IProgressMonitor arg3)
			throws CoreException {
		try {
	    	setRunConfigurationProperties(configuration);

			//MessageConsole cyBenchConsole = LauncherUtils.findConsole("CyBench Console");
			//cyBenchConsole.clearConsole();
			//cyBenchConsole.activate();
			/*MessageConsoleStream out = cyBenchConsole.newMessageStream();
				
			out.println("-----------------------------------------------------------------------------------------");
			out.println("                                 Starting CyBench benchmarks                             ");
			out.println("-----------------------------------------------------------------------------------------");
			*/
//			System.out.println("Location of workspace:"+ResourcesPlugin.getWorkspace().getRoot().getRawLocationURI().toASCIIString() );
//			String pathToPluginLocalStateDirectory = Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID)).toPortableString() ;
//			System.out.println("Location of bundle state:"+pathToPluginLocalStateDirectory) ;
//			String pathToTempReportPlainFile = CybenchUtils.generatePlainReportFilename(pathToPluginLocalStateDirectory, true, reportName) ;
//			String pathToTempReportEncryptedFile = CybenchUtils.generateEncryptedReportFilename(pathToPluginLocalStateDirectory, true, reportName);
			String pathToTempReportPlainFile = CybenchUtils.generatePlainReportFilename(reportFolder, true, reportName.replaceAll(" ", "_").toLowerCase()) ;
			String pathToTempReportEncryptedFile = CybenchUtils.generateEncryptedReportFilename(reportFolder, true, reportName.replaceAll(" ", "_").toLowerCase()) ;
	
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
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, pathToTempReportPlainFile+" "+pathToTempReportEncryptedFile);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.gocypher.cybench.launcher.CyBenchLauncher");
			
		
			new Thread ( new Runnable() {
				
				@Override
				public void run() {
					try {
					    ILaunch launchedBenchmarks = config.launch(ILaunchManager.RUN_MODE, null);
					  
					    //out.println("Waiting for CyBench to finish...");
					    
					    while (!launchedBenchmarks.isTerminated()) {
					    	try {
					    		Thread.sleep(1000);
					    	}catch (Exception e) {
					    		
					    	}
					    }
					    //out.println("Finished CyBench tests:"+launchedBenchmarks.isTerminated());
						/*String results = CybenchUtils.loadFile(pathToTempReportPlainFile) ;
						if (results != null && !results.isEmpty()) {
								out.println("Results from tests:"+JSONUtils.parseJsonIntoMap(results));
						}
						out.println("-----------------------------------------------------------------------------------------");
						out.println("                                 Finished CyBench benchmarks                             ");
						out.println("-----------------------------------------------------------------------------------------");
						*/
						
						GuiUtils.refreshCybenchExplorer();
						GuiUtils.openReportDisplayView(pathToTempReportPlainFile);					
						
						/*Display.getDefault().asyncExec(new Runnable() {
						    public void run() {
						    	try {
						    		System.out.println("Will open part for reports");
						    		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						    		page.showView(ReportsDisplayView.ID) ;
									IViewPart view = page.findView(ReportsDisplayView.ID) ;
						    		if (view instanceof ICybenchPartView) {
					    			((ICybenchPartView)view).refreshView();
					    		}
//							    	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ReportsDisplayView.ID) ; 
						    	}catch (Exception e) {
						    		e.printStackTrace();
						    	}
						    }
						});
						*/	
					} catch (CoreException e) {
					    System.err.println(e.getMessage());
					    //this.showMsgBox(e.getMessage(),event);
					}
				}
			}).start();
		//this.showMsgBox("Reality:"+LauncherDemo.resultsMap, event);
		//this.launchCyBenchLauncher();
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
				" -DCUSTOM_USER_PROPERTIES=\""+userProperties+"\"");
		
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
				" -DCUSTOM_USER_PROPERTIES=\""+userProperties+"\"");
		
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
   	   
    }
}