package com.gocypher.cybench.plugin.handlers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

import com.gocypher.cybench.core.utils.JSONUtils;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.model.ICybenchPartView;
import com.gocypher.cybench.plugin.utils.LauncherUtils;
import com.gocypher.cybench.plugin.views.ReportsDisplayView;

public class BenchmarkLauncherHandler extends AbstractHandler {

	@Inject
	ESelectionService selectionService ;
	
	@Inject UISynchronize sync;
	
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		/*
		*/
		try {
			String launchPath = "";
			launchPath = "C:/streams/tests/cybench-eclipse-backup-2020-11-05/cybench-eclipse-backup-2020-11-05/demo-jmh-tests/target/classes";
			System.out.println("Sync service:"+sync+";"+selectionService);
			String msg = "" ;
			//MessageConsole cyBenchConsole = LauncherUtils.findConsole("CyBench Console");
			//cyBenchConsole.clearConsole();
			//cyBenchConsole.activate();
			/*MessageConsoleStream out = cyBenchConsole.newMessageStream();
				
			out.println("-----------------------------------------------------------------------------------------");
			out.println("                                 Starting CyBench benchmarks                             ");
			out.println("-----------------------------------------------------------------------------------------");
			*/
			List<String> programArguments = new ArrayList<>() ;
			
			String bundlePaths = ""
			//+Platform.getBundle("CyBenchLauncherPlugin").getLocation() +";"
			//+Platform.getBundle("com.gocypher.cybench.externals").getLocation()
			;
			
			bundlePaths += LauncherUtils.resolveBundleLocation("CyBenchLauncherPlugin", true) ;
			bundlePaths += ";" ;
			bundlePaths += LauncherUtils.resolveBundleLocation("com.gocypher.cybench.externals", false) ;
		
			
			System.out.println(bundlePaths);
			String classPath= System.getProperty("java.class.path") ;
			System.out.println("Classpath found:"+classPath);
			
			classPath += ";"+launchPath ;
			//System.setProperty("java.class.path", classPath) ;
			
			System.out.println("Location of workspace:"+ResourcesPlugin.getWorkspace().getRoot().getRawLocationURI().toASCIIString() );
		
			String pathToPluginLocalStateDirectory = Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID)).toPortableString() ;
			System.out.println("Location of bundle state:"+pathToPluginLocalStateDirectory) ;
			String pathToTempReportPlainFile = CybenchUtils.generatePlainReportFilename(pathToPluginLocalStateDirectory, true,"") ;
			String pathToTempReportEncryptedFile = CybenchUtils.generateEncryptedReportFilename(pathToPluginLocalStateDirectory, true,"") ;
					
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects() ;
			
			IPath pluginPath = ResourcesPlugin.getPlugin().getStateLocation() ;
			System.out.println("Plugin path:"+pluginPath.toOSString());
			
				
			if (projects != null) {
				for (IProject project:projects) {
					System.out.println(project.getFullPath());
				}
			}
			
			
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager() ;
			ILaunchConfigurationType launchType = manager.getLaunchConfigurationType("org.eclipse.jdt.launching.localJavaApplication");
			final ILaunchConfigurationWorkingCopy config = launchType.newInstance(null, "CyBench plugin");
			    
			    
			
			config.setAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, "org.eclipse.jdt.launching.sourceLocator.JavaSourceLookupDirector");
			String[] classpath = new String[] { 
					launchPath
					//,"e:/benchmarks/eclipse_plugin_ws/DemoBenchmarksProject/lib/jmh-core-1.26.jar"
					//,"e:/benchmarks/eclipse_plugin_ws/DemoBenchmarksProject/lib/jmh-generator-annprocess-1.26.jar"
					//,"e:/benchmarks/eclipse_plugin_ws/DemoBenchmarksProject/lib/jopt-simple-4.6.jar"
					//,"e:/benchmarks/eclipse_plugin_ws/DemoBenchmarksProject/lib/commons-math3-3.2.jar"
					//,"e:/benchmarks/eclipse_plugin/plugins/CyBenchLauncherPlugin_1.0.0.jar"
					//,"e:/benchmarks/eclipse_plugin/plugins/com.gocypher.cybench.externals_1.0.0.jar"
					//,"e:/benchmarks/eclipse_plugin/dependencies/gocypher-cybench-client.jar"
					,LauncherUtils.resolveBundleLocation(Activator.PLUGIN_ID, true)
					,LauncherUtils.resolveBundleLocation(Activator.EXTERNALS_PLUGIN_ID,false) 
					//,"E:/benchmarks/eclipse_plugin_ws/com.gocypher.cybench.externals"
					};
			//e:\benchmarks\eclipse_plugin_ws\demo-jmh-tests\target\classes\
			
			
			List classpathMementos = new ArrayList();
			for (int i = 0; i < classpath.length; i++) {
			    IRuntimeClasspathEntry cpEntry = JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(classpath[i]));
			    cpEntry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
			    try {
			        classpathMementos.add(cpEntry.getMemento());
			    } catch (CoreException e) {
			        System.err.println(e.getMessage());
			        LauncherUtils.showMsgBox(e.getMessage(),event);
			    }
			}
			System.out.println("Classpath:"+classpathMementos);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpathMementos);
			
			
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, pathToTempReportPlainFile+" "+pathToTempReportEncryptedFile);
			config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.gocypher.cybench.launcher.CyBenchLauncher");
			
			//config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.local.demo.DemoRunner");
			//config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "com.gocypher.cybench.launcher.BenchmarkRunner");
			
			new Thread ( new Runnable() {
				
				@Override
				public void run() {
					try {
						//IProgressMonitor monitor =  //PlatformUI.getWorkbench().getProgressService() ;
					    ILaunch launchedBenchmarks = config.launch(ILaunchManager.RUN_MODE, null);
					  
					    //out.println("Waiting for CyBench to finish...");
					    
					    while (!launchedBenchmarks.isTerminated()) {
					    	//out.println("Waiting for CyBench to finish:"+launchedBenchmarks.isTerminated());
					    	try {
					    		Thread.sleep(1000);
					    	}catch (Exception e) {
					    		
					    	}
					    }
					    //out.println("Finished CyBench tests:"+launchedBenchmarks.isTerminated());
					    
						String results = CybenchUtils.loadFile(pathToTempReportPlainFile) ;
						if (results != null && !results.isEmpty()) {
								//out.println("Results from tests:"+JSONUtils.parseJsonIntoMap(results));
						}
						//this.showMsgBox("Results from tests:"+results, event);				
						
						
						/*out.println("-----------------------------------------------------------------------------------------");
						out.println("                                 Finished CyBench benchmarks                             ");
						out.println("-----------------------------------------------------------------------------------------");
						*/
						//cyBenchConsole.activate();
											
						Display.getDefault().asyncExec(new Runnable() {
						    public void run() {
						    	try {
						    		System.out.println("Will open part for reports");
						    		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
						    		page.showView(ReportsDisplayView.ID) ;
						    		
						    		IViewPart view = page.findView(ReportsDisplayView.ID) ;
						    		if (view instanceof ICybenchPartView) {
						    			((ICybenchPartView)view).refreshView();
						    		}
						    		
						    		
						    	}catch (Exception e) {
						    		e.printStackTrace();
						    	}
						    }
						});
											
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
		return null;
	}
	
	/*private void launchCyBenchLauncher () {
		int forks = 1 ;
		int measurementIterations = 1 ;
		int warmUpIterations = 1 ;
		int warmUpSeconds = 5 ;
		int threads = 1 ;
		
		try {
			OptionsBuilder optBuild = new OptionsBuilder();
			Options opt = optBuild
					.forks(forks)				
					.measurementIterations(measurementIterations)
					.warmupIterations(warmUpIterations)
					.warmupTime(TimeValue.seconds(warmUpSeconds))
					.threads(threads)
					.shouldDoGC(true)
					.detectJvmArgs()
					// .addProfiler(StackProfiler.class)
					// .addProfiler(HotspotMemoryProfiler.class)
					// .addProfiler(HotspotRuntimeProfiler.class)
					// .addProfiler(JavaFlightRecorderProfiler.class)
					
					.build();
	
			Runner runner = new Runner(opt);
		
			Collection<RunResult> results = runner.run() ;
			
			System.out.println("Cybench launch result items:"+results.size());
		}catch (Throwable t) {
			t.printStackTrace();
		}
		
		
	}
	*/

}
