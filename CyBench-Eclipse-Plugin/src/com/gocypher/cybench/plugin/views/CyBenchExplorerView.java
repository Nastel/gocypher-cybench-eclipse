package com.gocypher.cybench.plugin.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.model.ICybenchPartView;
import com.gocypher.cybench.plugin.model.NameValueEntry;
import com.gocypher.cybench.plugin.model.NameValueModelProvider;
import com.gocypher.cybench.plugin.model.Node;
import com.gocypher.cybench.plugin.model.ReportFileEntry;
import com.gocypher.cybench.plugin.model.ReportFileEntryComparator;
import com.gocypher.cybench.plugin.views.ReportsDisplayView.ViewLabelProvider;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class CyBenchExplorerView extends ViewPart implements ICybenchPartView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.gocypher.cybench.plugin.views.CyBenchExplorerView";

	@Inject IWorkbench workbench;
	@Inject ESelectionService selectionService ;
	
	//private TableViewer reportsListViewer;
	private TreeViewer projectsViewer ;
	
	
	//private List<ReportFileEntry> listOfFiles = new ArrayList<>();
	
	private List<Node<ReportFileEntry>>treeOfReports = new ArrayList<>() ;
	
	private Action refreshAction;
	//private Action action2;
	private Action openSelectedReportAction;
	
	@PostConstruct
	public void init ( ) {
		//System.out.println("--->Explorer Init called:"+selectionService);
		this.loadData();
	}
	
	private void loadData () {
		/*String pathToPluginLocalStateDirectory = Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID)).toPortableString() ;
		//System.out.println("Reports default directory:"+pathToPluginLocalStateDirectory);
		this.listOfFiles.clear();
		List<File>reportsFiles = CybenchUtils.listFilesInDirectory(pathToPluginLocalStateDirectory) ;
		
		for (File file:reportsFiles) {
			if (file.getName().endsWith(Constants.REPORT_FILE_EXTENSION)) {
				ReportFileEntry entry = new ReportFileEntry() ;
				entry.create(file);
				listOfFiles.add(entry) ;
			}
		}
		Collections.sort(listOfFiles, new ReportFileEntryComparator ());
		*/
		
		treeOfReports.clear();
		
				
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects() ;
		for (IProject item:projects) {
			String pathToProjectDirectory = item.getLocation().toPortableString() ;
			if (pathToProjectDirectory != null && !pathToProjectDirectory.isEmpty()) {
				ReportFileEntry projectEntry = new ReportFileEntry() ;
				projectEntry.setFullPathToFile(pathToProjectDirectory);
				projectEntry.setName(item.getName());
				Node<ReportFileEntry> projectNode = new Node<>(projectEntry) ;
				
				List<File>projectReportsFiles = CybenchUtils.listFilesInDirectory(pathToProjectDirectory) ;
				
				for (File file:projectReportsFiles) {
					if (file.getName().endsWith(Constants.REPORT_FILE_EXTENSION)) {
						ReportFileEntry entry = new ReportFileEntry() ;
						entry.create(file);
						Node<ReportFileEntry> reportNode = new Node<>(entry) ;
						projectNode.addChild(reportNode) ;
					}
				}			
				treeOfReports.add(projectNode) ;
						
			}
			
		}
		
		//System.out.println("Reports:"+listOfFiles);
	}
	
	/*public ReportFileEntry findEntryByIdentifier (String identifier) {
		
		for (ReportFileEntry item:listOfFiles) {
			System.out.println("Report entry identifier:"+item.getReportIdentifier());
			if (item.getReportIdentifier() != null 
					&& item.getReportIdentifier().equals(identifier)) {
				return item ;
			}
		}
		return null ;
	}
	*/

	@Override
	public void createPartControl(Composite parent) {
		/*reportsListViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
		//viewer.getTable().setLinesVisible(true);
		reportsListViewer.setContentProvider(ArrayContentProvider.getInstance());
		reportsListViewer.setInput(this.listOfFiles);
		reportsListViewer.setLabelProvider(new ViewLabelProvider());
		reportsListViewer.setSelection(new StructuredSelection(reportsListViewer.getElementAt(0)),true);
		
		workbench.getHelpSystem().setHelp(reportsListViewer.getControl(), "CyBenchLauncherPlugin.viewer");
		getSite().setSelectionProvider(reportsListViewer);
		*/

		// Create the help context id for the viewer's control
		
		projectsViewer = new TreeViewer (parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER) ;
		projectsViewer.setContentProvider(new TreeViewContentProvider());
		projectsViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
                new TreeViewLabelProvider(createImageDescriptor())));
		projectsViewer.setAutoExpandLevel(2);
		
		/*ReportFileEntry folder1 = new ReportFileEntry() ;
		folder1.setName("Folder1");
		
		Node<ReportFileEntry> n1 = new Node<> (folder1) ;
		
		ReportFileEntry rep1 = new ReportFileEntry() ;
		rep1.setName("report1.json");
				
		n1.addChild(new Node<ReportFileEntry>(rep1));
		
		ReportFileEntry folder2 = new ReportFileEntry() ;
		folder2.setName("Folder2");
		
		Node<ReportFileEntry> n2 = new Node<ReportFileEntry> (folder2) ;
		
		List<Node<ReportFileEntry>>nodes = new ArrayList<>() ;
		nodes.add(n1) ;
		nodes.add(n2) ;
		*/
		projectsViewer.setInput(treeOfReports);
		
		
		workbench.getHelpSystem().setHelp(projectsViewer.getControl(), "CyBenchLauncherPlugin.viewer");
		getSite().setSelectionProvider(projectsViewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CyBenchExplorerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(projectsViewer.getControl());
		projectsViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, projectsViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
		//manager.add(new Separator());
		//manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
		//manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		//manager.add(action2);
	}

	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				refreshView();
			}
		};
		refreshAction.setText("Reload CyBench Explorer");
		refreshAction.setToolTipText("Reload CyBench Explorer");
		refreshAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		
		/*action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(workbench.getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		*/
		openSelectedReportAction = new Action() {
			public void run() {
				
				//System.out.println("Selection service:"+selectionService);
				
				IStructuredSelection selection = projectsViewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				//System.out.println("Selected report:"+obj.getClass());
				
				
				
				Node<ReportFileEntry> entry = (Node<ReportFileEntry> )obj ;
				//System.out.println("Will open:"+entry.getName());
				
				
				try {
					if (entry.getData() != null 
							&& entry.getData().getReportIdentifier() != null 
							&& ! entry.getData().getReportIdentifier().isEmpty()) {
						IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage() ;				
						page.showView(ReportsDisplayView.ID, entry.getData().getReportIdentifier(), IWorkbenchPage.VIEW_ACTIVATE);
					}
					//selectionService.setSelection(obj);
				}catch (Exception e) {
					e.printStackTrace();
				}
				
				
				
				
				
				//showMessage("Double-click detected on "+obj.getClass());
				
			}
		};
	}

	private void hookDoubleClickAction() {
		/*reportsListViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openSelectedReportAction.run();
			}
		});
		*/
		
		projectsViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openSelectedReportAction.run();
			}
		});
	}

	@Override
	public void setFocus() {
		projectsViewer.getControl().setFocus();
	}

	@Override
	public void refreshView() {
		this.loadData();
		this.projectsViewer.setInput(this.treeOfReports);
		this.projectsViewer.refresh();
		
	}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		@Override
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		@Override
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		@Override
		public Image getImage(Object obj) {
			return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		}
	}
	class TreeViewContentProvider implements ITreeContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object inputElement) {
        	//System.out.println("!--->Class:"+inputElement.getClass());
            return ((List)inputElement).toArray();
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            Node node = (Node) parentElement;
            return node.getChildren().toArray();
        }

        @Override
        public Object getParent(Object element) {
        	Node node = (Node) element;
            return node.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
        	Node node = (Node) element;
            if (node.getChildren().size()>0) {
                return true;
            }
            return false;
        }
	}
	class TreeViewLabelProvider extends LabelProvider implements IStyledLabelProvider {
		private ImageDescriptor directoryImage;
        private ResourceManager resourceManager;

        public TreeViewLabelProvider(ImageDescriptor directoryImage) {
            this.directoryImage = directoryImage;
        }

        @Override
        public StyledString getStyledText(Object element) {
        	
        	Node node = (Node) element;
        	StyledString styledString = new StyledString(getNodeName(node));
        	
            /*if(element instanceof Node) {
                Node node = (Node) element;
                StyledString styledString = new StyledString(getNodeName(node));
                String[] files = file.list();
                if (files != null) {
                    styledString.append(" ( " + files.length + " ) ",
                            StyledString.COUNTER_STYLER);
                }
                return styledString;
            }
            */
            return styledString;
        }

        @Override
        public Image getImage(Object element) {
            if(element instanceof Node) {
                if(((Node) element).getChildren().size()>0  ||((Node) element).getParent() == null ) {
                    //return getResourceManager().createImage(directoryImage);
                	return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_PROJECT);
                }
            }

            //return super.getImage(element);
            return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        }

        @Override
        public void dispose() {
            // garbage collect system resources
            if(resourceManager != null) {
                resourceManager.dispose();
                resourceManager = null;
            }
        }

        protected ResourceManager getResourceManager() {
            if(resourceManager == null) {
                resourceManager = new LocalResourceManager(JFaceResources.getResources());
            }
            return resourceManager;
        }

        private String getNodeName(Node node) {
            String name = node.toString();
            return name;
        }
	}
	private ImageDescriptor createImageDescriptor() {
        Bundle bundle = FrameworkUtil.getBundle(ViewLabelProvider.class);
        URL url = FileLocator.find(bundle, new Path("icons/sample.png"), null);
        return ImageDescriptor.createFromURL(url);
    }
}
