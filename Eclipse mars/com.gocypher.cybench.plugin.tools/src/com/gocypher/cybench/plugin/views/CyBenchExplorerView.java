package com.gocypher.cybench.plugin.views;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.model.ICybenchPartView;
import com.gocypher.cybench.plugin.model.Node;
import com.gocypher.cybench.plugin.model.ReportFileEntry;
import com.gocypher.cybench.plugin.utils.Constants;
import com.gocypher.cybench.plugin.views.ReportsDisplayView.ViewLabelProvider;


public class CyBenchExplorerView extends ViewPart implements ICybenchPartView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.gocypher.cybench.plugin.views.CyBenchExplorerView";

	IWorkbench workbench = PlatformUI.getWorkbench();
	
	//private TableViewer reportsListViewer;
	private TreeViewer projectsViewer ;
	
	
	//private List<ReportFileEntry> listOfFiles = new ArrayList<>();
	
	private List<Node<ReportFileEntry>>treeOfReports = new ArrayList<>() ;
	
	private Action refreshAction;
	//private Action action2;
	private Action openSelectedReportAction;
	private Styler scoreStyler ;
	
	@PostConstruct
	public void init ( ) {
//		GuiUtils.logInfo("--->Explorer Init called:"+selectionService);
		this.loadData();
		
		this.scoreStyler = new Styler() {
			
			@Override
			public void applyStyles(TextStyle textStyle) {
				FontDescriptor boldDescriptor = FontDescriptor.createFrom(new FontData("Arial",8,SWT.BOLD));
		        Font boldFont = boldDescriptor.createFont(Display.getCurrent());		       
		        textStyle.font = boldFont;		
			}
		};
	}
	
	private void loadData () {
		/*String pathToPluginLocalStateDirectory = Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID)).toPortableString() ;
		//GuiUtils.logInfo("Reports default directory:"+pathToPluginLocalStateDirectory);
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
		
		//GuiUtils.logInfo("Reports:"+listOfFiles);
	}
	
	/*public ReportFileEntry findEntryByIdentifier (String identifier) {
		
		for (ReportFileEntry item:listOfFiles) {
			GuiUtils.logInfo("Report entry identifier:"+item.getReportIdentifier());
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
		ContextInjectionFactory.inject(this, EclipseContextFactory.create());

		
		/*reportsListViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
		//viewer.getTable().setLinesVisible(true);
		reportsListViewer.setContentProvider(ArrayContentProvider.getInstance());
		reportsListViewer.setInput(this.listOfFiles);
		reportsListViewer.setLabelProvider(new ViewLabelProvider());
		reportsListViewer.setSelection(new StructuredSelection(reportsListViewer.getElementAt(0)),true);
		
		workbench.getHelpSystem().setHelp(reportsListViewer.getControl(), "com.gocypher.cybench.plugin.tools.viewer");
		getSite().setSelectionProvider(reportsListViewer);
		*/

		// Create the help context id for the viewer's control
		
		projectsViewer = new TreeViewer (parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER|  SWT.FULL_SELECTION) ;
		projectsViewer.setContentProvider(new TreeViewContentProvider());
		//projectsViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(
        //        new TreeViewLabelProvider(createImageDescriptor())));
		projectsViewer.setAutoExpandLevel(2);
		projectsViewer.getTree().setHeaderVisible(true);
		
		
		/*projectsViewer.getTree().addListener(SWT.Resize, new Listener() {

	          @Override
	          public void handleEvent(Event event) {

	        	 GuiUtils.logInfo("Resize:"+event);
	        	 Tree table = (Tree)event.widget;
	        	 table.redraw();
	        	 table.update();	            
	          }
	        });
		*/
		/*TreeViewerColumn fakeColumn = new TreeViewerColumn(projectsViewer, SWT.NONE);
		fakeColumn.getColumn().setWidth(20);
		fakeColumn.setLabelProvider(new CellLabelProvider() {
			
			@Override
			public void update(ViewerCell arg0) {
				arg0.setText("");				
			}
		});
		
		fakeColumn.getColumn().dispose();
		*/
		
		
		
		
		TreeViewerColumn mainColumn = new TreeViewerColumn(projectsViewer, SWT.NONE);
        mainColumn.getColumn().setText("Name");
       // mainColumn.getColumn().setResizable(false);
        //mainColumn.getColumn().setWidth(200);
       
        mainColumn.setLabelProvider(
                new DelegatingStyledCellLabelProvider(
                        new TreeViewMainColumnLabelProvider(createImageDescriptor())));
		
        TreeViewerColumn scoreColumn = new TreeViewerColumn(projectsViewer, SWT.NONE);
        scoreColumn.getColumn().setText("Score");
        //modifiedColumn.getColumn().setWidth(120);
        scoreColumn.getColumn().setAlignment(SWT.RIGHT);
        scoreColumn
                .setLabelProvider(new DelegatingStyledCellLabelProvider(
                        new ReportScoreLabelProvider(this.scoreStyler)));
		
        TreeViewerColumn modifiedColumn = new TreeViewerColumn(projectsViewer, SWT.NONE);
        modifiedColumn.getColumn().setText("Timestamp");
        //modifiedColumn.getColumn().setWidth(120);
        modifiedColumn.getColumn().setAlignment(SWT.CENTER);
        modifiedColumn
                .setLabelProvider(new DelegatingStyledCellLabelProvider(
                        new ReportTimestampLabelProvider()));
        
        
       
        
		projectsViewer.setInput(treeOfReports);
		
		//fakeColumn.getColumn().pack();
		mainColumn.getColumn().pack();
		modifiedColumn.getColumn().pack();
		scoreColumn.getColumn().pack();
		
		
		
		/*projectsViewer.getControl().addControlListener(new ControlListener() {

	        @Override
	        public void controlResized(ControlEvent arg0) {
	            Rectangle rect = projectsViewer.getTree().getClientArea();
	            if(rect.width>0){
	                int extraSpace=rect.width/3;
	                mainColumn.getColumn().setWidth(extraSpace);
	                modifiedColumn.getColumn().setWidth(extraSpace);
	                scoreColumn.getColumn().setWidth(extraSpace);
	                //col4.getColumn().setWidth(extraSpace);
	            }
	        }

	        @Override
	        public void controlMoved(ControlEvent arg0) {
	            // TODO Auto-generated method stub

	        }
	    });
	    */
		
		workbench.getHelpSystem().setHelp(projectsViewer.getControl(), "com.gocypher.cybench.plugin.tools.viewer");
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

	@SuppressWarnings("unchecked")
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
				
				//GuiUtils.logInfo("Selection service:"+selectionService);
				
				IStructuredSelection selection = projectsViewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				//GuiUtils.logInfo("Selected report:"+obj.getClass());
				
				
				
				Node<ReportFileEntry> entry = (Node<ReportFileEntry> )obj ;
				//GuiUtils.logInfo("Will open:"+entry.getName());
				
				
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
		
		for (TreeColumn col:this.projectsViewer.getTree().getColumns()) {
			col.pack();
		}
		this.projectsViewer.refresh();
		
	}
	
	/*class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
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
	*/
	class TreeViewContentProvider implements ITreeContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object inputElement) {
        	//GuiUtils.logInfo("!--->Class:"+inputElement.getClass());
            return ((List<?>)inputElement).toArray();
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            Node<?> node = (Node<?>) parentElement;
            return node.getChildren().toArray();
        }

        @Override
        public Object getParent(Object element) {
        	Node<?> node = (Node<?>) element;
            return node.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
        	Node<?> node = (Node<?>) element;
            if (node.getChildren().size()>0) {
                return true;
            }
            return false;
        }
	}
	@SuppressWarnings("unchecked")
	class TreeViewMainColumnLabelProvider extends LabelProvider implements IStyledLabelProvider {
		public TreeViewMainColumnLabelProvider(ImageDescriptor directoryImage) {
        }

        @Override
        public StyledString getStyledText(Object element) {
        	if (element instanceof Node) {
				Node<ReportFileEntry> node = (Node<ReportFileEntry>) element;
            	if (node.getData() != null && node.getData().getName() != null) {
            		return new StyledString(node.getData().getName());
            	}
	        	
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
	           
        	}
        	return new StyledString("");
        	
        }

        @Override
        public Image getImage(Object element) {
            if(element instanceof Node) {
                if(((Node<?>) element).getChildren().size()>0  ||((Node<?>) element).getParent() == null ) {
                    //return getResourceManager().createImage(directoryImage);
                	return workbench.getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
                }
            }

            //return super.getImage(element);
            return workbench.getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        }

	}
	@SuppressWarnings("unchecked")
	class ReportTimestampLabelProvider extends LabelProvider implements IStyledLabelProvider {
        public ReportTimestampLabelProvider() {
           
        }
        @Override
        public StyledString getStyledText(Object element) {
            if (element instanceof Node) {
            	Node<ReportFileEntry> node = (Node<ReportFileEntry>) element;
            	if (node.getData() != null && node.getData().getTimeStampStr() != null) {
            		return new StyledString(node.getData().getTimeStampStr(),StyledString.DECORATIONS_STYLER);
            	}
                       
            }
            return new StyledString("");
        }
    }
	@SuppressWarnings("unchecked")
	class ReportScoreLabelProvider extends LabelProvider implements IStyledLabelProvider {
		private Styler styler ;
        public ReportScoreLabelProvider(Styler styler ) {
           this.styler = styler;
        }
        @Override
        public StyledString getStyledText(Object element) {
            if (element instanceof Node) {
            	Node<ReportFileEntry> node = (Node<ReportFileEntry>) element;
            	if (node.getData() != null && node.getData().getScoreStr() != null) {
            		            		
            		if (styler != null) {
            			return new StyledString(node.getData().getScoreStr(), styler);
            		}
            		return new StyledString(node.getData().getScoreStr()) ; 
            	
            	}
                       
            }
            return new StyledString("");
        }
    }
	
	private ImageDescriptor createImageDescriptor() {
        Bundle bundle = FrameworkUtil.getBundle(ViewLabelProvider.class);
        URL url = FileLocator.find(bundle, new Path("icons/cybench_symbol.png"), null);
        return ImageDescriptor.createFromURL(url);
    }
}
