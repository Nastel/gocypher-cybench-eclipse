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


import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.widgets.DirectoryDialog;
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
import com.gocypher.cybench.plugin.utils.GuiUtils;
import com.gocypher.cybench.plugin.views.ReportsDisplayView.ViewLabelProvider;


public class CyBenchExplorerView extends ViewPart implements ICybenchPartView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.gocypher.cybench.plugin.views.CyBenchExplorerView";

	IWorkbench workbench = PlatformUI.getWorkbench();


	private TreeViewer projectsViewer ;

	
	private List<Node<ReportFileEntry>>treeOfReports = new ArrayList<>() ;
	
	private Action refreshAction;
	private Action openLocationView;
	private Action workspaceDirectory;
	private Action fileSystemDirectory;
	//private Action action2;
	private Action openSelectedReportAction;
	private Styler scoreStyler ;

	private String filesSystemSelectedPath = null;
	private boolean loadWorkspace = true;
	
	@PostConstruct
	public void init ( ) {
		this.loadData();
		
		this.scoreStyler = new Styler() {
			
			@Override
			public void applyStyles(TextStyle textStyle) {
				FontDescriptor boldDescriptor = FontDescriptor.createFrom(new FontData("Arial",8,SWT.BOLD));
		        Font boldFont = boldDescriptor.createFont(Display.getCurrent());		       
		        textStyle.font = boldFont;		
			}
		};
		this.refreshView();
	}
	
	private void loadData () {		
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
						if(checkForExistance(entry)) {
							Node<ReportFileEntry> reportNode = new Node<>(entry) ;
							projectNode.addChild(reportNode) ;
						}
					}
				}			
				treeOfReports.add(projectNode) ;
			}
		}
	}
	
	private void loadData (String pathToProjectDirectory) {		
		if(loadWorkspace) {
			loadData();
		}else if(pathToProjectDirectory != null){
			treeOfReports.clear();
			ReportFileEntry projectEntry = new ReportFileEntry() ;
			projectEntry.setFullPathToFile(pathToProjectDirectory);
			projectEntry.setName(pathToProjectDirectory.substring(pathToProjectDirectory.lastIndexOf('\\'),pathToProjectDirectory.length()));
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
	private boolean checkForExistance(ReportFileEntry entry) {
		if(treeOfReports.size()>1) {
			for(int i=0; i<treeOfReports.size(); i++) {
			List<Node<ReportFileEntry>> temp = treeOfReports.get(i).getChildren();
				for(int k=0; k<temp.size(); k++) {
				
					if(temp.get(k).getData().getTimestamp() == entry.getTimestamp()) {
						if(temp.get(k).getData().getFullPathToFile().length() == entry.getFullPathToFile().length()) {
							treeOfReports.get(i).getChildren().remove(k);
							return true;
						}else {
							return false;
						}
					}
				
				}
				
			}
		}
		return true;
	}
	@Override
	public void createPartControl(Composite parent) {
		// Create the help context id for the viewer's control
		
		projectsViewer = new TreeViewer (parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER|  SWT.FULL_SELECTION) ;
		projectsViewer.setContentProvider(new TreeViewContentProvider());
		projectsViewer.setAutoExpandLevel(2);
		projectsViewer.getTree().setHeaderVisible(true);
		
		TreeViewerColumn mainColumn = new TreeViewerColumn(projectsViewer, SWT.NONE);
		mainColumn.getColumn().setWidth(200);
        mainColumn.getColumn().setText("Name");
       
       
        mainColumn.setLabelProvider(
                new DelegatingStyledCellLabelProvider(
                        new TreeViewMainColumnLabelProvider(createImageDescriptor())));
		
        TreeViewerColumn scoreColumn = new TreeViewerColumn(projectsViewer, SWT.NONE);
        scoreColumn.getColumn().setText("Score");
        scoreColumn.getColumn().setAlignment(SWT.RIGHT);
        scoreColumn
                .setLabelProvider(new DelegatingStyledCellLabelProvider(
                        new ReportScoreLabelProvider(this.scoreStyler)));
		
        TreeViewerColumn modifiedColumn = new TreeViewerColumn(projectsViewer, SWT.NONE);
        modifiedColumn.getColumn().setText("Timestamp");
        modifiedColumn.getColumn().setAlignment(SWT.CENTER);
        modifiedColumn
                .setLabelProvider(new DelegatingStyledCellLabelProvider(
                        new ReportTimestampLabelProvider()));
        

		projectsViewer.setInput(treeOfReports);
//		mainColumn.getColumn().pack();
		
		mainColumn.getColumn().pack();
		modifiedColumn.getColumn().pack();
		scoreColumn.getColumn().pack();
		
		
		workbench.getHelpSystem().setHelp(projectsViewer.getControl(), "com.gocypher.cybench.plugin.tools.viewer");
		getSite().setSelectionProvider(projectsViewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		refreshView();
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
		manager.add(workspaceDirectory);
		manager.add(fileSystemDirectory);

	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(workspaceDirectory);
		manager.add(fileSystemDirectory);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(openLocationView);
		manager.add(refreshAction);
	}

	@SuppressWarnings("unchecked")
	private void makeActions() {
		workspaceDirectory = new Action() {
			public void run() {
             	loadWorkspace = true;
             	refreshView();
			}
		};
		workspaceDirectory.setText("Open Workspace");
		workspaceDirectory.setToolTipText("Open Workspace Reports");
//		workspaceDirectory.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		
		fileSystemDirectory = new Action() {
			public void run() {
             	loadWorkspace = false;
             	if(filesSystemSelectedPath == null || filesSystemSelectedPath.equals("")) {
             		setTheFilePathforFileSystemBrowsing();
             	}
             	refreshView();
			}
		};
		fileSystemDirectory.setText("Open File System");
		fileSystemDirectory.setToolTipText("Open File System Reports");
		
		
		refreshAction = new Action() {
			public void run() {
				refreshView();
			}
		};
		refreshAction.setText("Reload CyBench Explorer");
		refreshAction.setToolTipText("Reload CyBench Explorer");
		refreshAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		
		openLocationView = new Action() {
			public void run() {
				try {						
					setTheFilePathforFileSystemBrowsing();
					refreshView();
				} catch (Exception e) {
					GuiUtils.logError("Error on open report link",e);
				}
				
			}
		};
		openLocationView.setText("Select Directory And Open Reports");
		openLocationView.setToolTipText("Select Directory And Open Reports");
		openLocationView.setImageDescriptor(GuiUtils.getCustomImage("icons/open_file.png"));
		
		openSelectedReportAction = new Action() {
			public void run() {
						
				IStructuredSelection selection = projectsViewer.getStructuredSelection();
				Object obj = selection.getFirstElement();

				Node<ReportFileEntry> entry = (Node<ReportFileEntry> )obj ;
				
				try {
					if (entry.getData() != null 
							&& entry.getData().getReportIdentifier() != null 
							&& ! entry.getData().getReportIdentifier().isEmpty()) {
						IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage() ;				
						page.showView(ReportsDisplayView.ID, entry.getData().getReportIdentifier(), IWorkbenchPage.VIEW_ACTIVATE);
					}
					
				}catch (Exception e) {
					GuiUtils.logError ("Error on report open",e) ;
				}
							
			}
		};
	}

	private void setTheFilePathforFileSystemBrowsing() {
		DirectoryDialog dialog = new DirectoryDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.NULL);
			filesSystemSelectedPath = dialog.open();
         if (filesSystemSelectedPath != null && filesSystemSelectedPath != "") {
         	loadWorkspace = false;
         	loadData(filesSystemSelectedPath);
         }
	}
	
	private void hookDoubleClickAction() {
		
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
		GuiUtils.logInfo("filesSystemSelectedPath: "+filesSystemSelectedPath);
		this.loadData(filesSystemSelectedPath);
		
		this.projectsViewer.setInput(this.treeOfReports);
		
		for (TreeColumn col:this.projectsViewer.getTree().getColumns()) {
			col.pack();
		}
		this.projectsViewer.refresh();
	}
	
	class TreeViewContentProvider implements ITreeContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object inputElement) {        
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
	        		            	           
        	}
        	return new StyledString("");
        	
        }

        @Override
        public Image getImage(Object element) {
            if(element instanceof Node) {
                if(((Node<?>) element).getChildren().size()>0  ||((Node<?>) element).getParent() == null ) {                    
                	return workbench.getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
                }
            }

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
