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


import java.net.URL;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.model.ICybenchPartView;
import com.gocypher.cybench.plugin.model.NameValueEntry;
import com.gocypher.cybench.plugin.model.ReportFileEntry;
import com.gocypher.cybench.plugin.model.ReportHandlerService;
import com.gocypher.cybench.plugin.model.ReportUIModel;
import com.gocypher.cybench.plugin.utils.Constants;
import com.gocypher.cybench.plugin.utils.GuiUtils;

public class ReportsDisplayView extends ViewPart implements ICybenchPartView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.gocypher.cybench.plugin.views.ReportsDisplayView";


	IWorkbench workbench = PlatformUI.getWorkbench();
	@Inject ReportHandlerService reportService;
	
	private CTabFolder reportTabs ;
	private TableViewer reportsListViewer;
	private TableViewer jvmAttributesViewer;
	private TableViewer hwAttributesViewer;
	private TableViewer overviewAttributesViewer;
	
	private Action refreshAction;
	private Action openReportLinkAction;
	
	private Action doubleClickAction;
		
	private TableViewer reportDetailsViewer ;
	
	
	private ReportUIModel reportUIModel = new ReportUIModel();
	
	//private static Color colorGray= new Color (Display.getCurrent(),232,232,232) ;//Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
	
	private Styler valueStyler ;
//	private Styler keyStyler ;
	 
	
	@PostConstruct
	public void init ( ) {
		this.valueStyler = new Styler() {
			
			@Override
			public void applyStyles(TextStyle textStyle) {
				FontDescriptor boldDescriptor = FontDescriptor.createFrom(new FontData("Arial",8,SWT.BOLD));
		        Font boldFont = boldDescriptor.createFont(Display.getCurrent());		       
		        textStyle.font = boldFont;		
			}
		};
		this.loadData();
		
	}
	
	private void loadData () {
		//String pathToPluginLocalStateDirectory = Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID)).toPortableString() ;
		//GuiUtils.logInfo("Reports default directory:"+pathToPluginLocalStateDirectory);
//		IViewPart explorerView = workbench.getActiveWorkbenchWindow().getActivePage().findView(ReportsDisplayView.ID) ;
		
		if (
			 this.getViewSite().getSecondaryId() != null 
				&& !this.getViewSite().getSecondaryId().isEmpty()) {
			
				//GuiUtils.logInfo("Explorer:"+explorerView);
				//CyBenchExplorerView cybenchExplorerView = (CyBenchExplorerView)explorerView ;
				
				//String reportIdentifier = this.getViewSite().getSecondaryId() ;	
				//ReportFileEntry entry = cybenchExplorerView.findEntryByIdentifier(reportIdentifier);
				
				String fullPathToPatialFile = GuiUtils.decodeBase64(this.getViewSite().getSecondaryId()) ;
			
				ReportFileEntry entry = new ReportFileEntry() ;
				
				if (fullPathToPatialFile.endsWith(Constants.REPORT_FILE_EXTENSION)) {
					entry.setFullPathToFile(fullPathToPatialFile);
				}
				else {					
					entry.setFullPathToFile(CybenchUtils.findPathToFileByPrefix(fullPathToPatialFile));
				}
				reportUIModel = reportService.prepareReportDisplayModel(entry) ;
				
				this.setPartName(reportUIModel.getReportTitle());
		}
		
				
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

	@Override
	public void createPartControl(Composite parent) {
		ContextInjectionFactory.inject(this, EclipseContextFactory.create());
		//GuiUtils.logInfo("-->Creating part view for reports");
		parent.setLayout(new FillLayout());
		SashForm sash = new SashForm(parent, SWT.HORIZONTAL) ;
		
	
		this.createLeftSide(sash);
		this.createRightSide(sash);
		
		sash.setWeights(new int [] {1,2});
		
	
	}
	
	private void createLeftSide (SashForm sash) {
		Group leftGroup = new Group(sash, SWT.NONE) ;
		leftGroup.setText("Available Benchmarks");
		leftGroup.setLayout(new FillLayout());
		
		reportsListViewer = new TableViewer(leftGroup, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
		//viewer.getTable().setLinesVisible(true);
		reportsListViewer.setContentProvider(ArrayContentProvider.getInstance());
		reportsListViewer.setInput(this.reportUIModel.getListOfBenchmarks());
		reportsListViewer.setLabelProvider(new ViewLabelProvider());
		/*if (this.reportUIModel.getListOfBenchmarks().size() > 0) {
			reportsListViewer.setSelection(new StructuredSelection(reportsListViewer.getElementAt(0)),true);
		}*/
		if(workbench != null){
			workbench.getHelpSystem().setHelp(reportsListViewer.getControl(), "com.gocypher.cybench.plugin.tools.viewer");
		}
			getSite().setSelectionProvider(reportsListViewer);
			makeActions();
			hookContextMenu();
			hookDoubleClickAction();
			contributeToActionBars();
		
	}
	private void createRightSide (SashForm sash) {
		Group rightGroup = new Group(sash, SWT.NONE);
		rightGroup.setText( "Details");
		rightGroup.setLayout(new FillLayout());
	
		reportTabs = new CTabFolder(rightGroup, SWT.BOTTOM);
		GridData data = new GridData(SWT.FILL,
                SWT.FILL, true, true,
                2, 1);
		reportTabs.setLayoutData(data);
        
  
		CTabItem summaryTab = new CTabItem(reportTabs, SWT.NONE);       
        reportTabs.setSelection(0);
        summaryTab.setText("Summary");
        this.createSummaryDetailsViewer(reportTabs);
        summaryTab.setControl(overviewAttributesViewer.getControl());
        
		
        CTabItem benchmarkDetailsTab = new CTabItem(reportTabs, SWT.NONE);               
        benchmarkDetailsTab.setText("Benchmark Details");
		this.createReportDetailsViewer(reportTabs);
		benchmarkDetailsTab.setControl(reportDetailsViewer.getControl());
		
		CTabItem jvmPropertiesTab = new CTabItem(reportTabs, SWT.NONE);       		
		jvmPropertiesTab.setText("JVM Properties");
		this.createJVMAttributesViewer(reportTabs);
		jvmPropertiesTab.setControl(jvmAttributesViewer.getControl());
		
		CTabItem hwPropertiesTab = new CTabItem(reportTabs, SWT.NONE);       		
		hwPropertiesTab.setText("HW Properties");
		this.createHWAttributesViewer(reportTabs);
		hwPropertiesTab.setControl(hwAttributesViewer.getControl());
		
		
		setDataForSecondaryDisplayElements() ;
		
		
		
	}
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ReportsDisplayView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(reportsListViewer.getControl());
		reportsListViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, reportsListViewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(new Separator());
		manager.add(openReportLinkAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(openReportLinkAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(openReportLinkAction);
	}

	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				refreshView();
				
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Reload report from the file system.");
		refreshAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		
		openReportLinkAction = new Action() {
			public void run() {
				//showMessage("Navigate executed:" + reportUIModel.getBaseProperties().get("reportURL"));
				if (reportUIModel.getReportExternalUrl() != null) {
					try {						
					    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(reportUIModel.getReportExternalUrl()));
					} catch (Exception e) {
					    e.printStackTrace();
					}
				}
				
			}
		};
		openReportLinkAction.setText("Open in CyBench Web");
		openReportLinkAction.setToolTipText("Open Report in CyBench WebSite.");
		openReportLinkAction.setImageDescriptor(GuiUtils.getCustomImage("icons/chrome.png"));
		
				
		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = reportsListViewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				//GuiUtils.logInfo("Selected on the left:"+obj + ";"+obj.getClass());
				if (obj instanceof NameValueEntry) {
					NameValueEntry selEntry = (NameValueEntry)obj ;
					if (reportUIModel.getBenchmarksAttributes().get(selEntry.getName()) != null){
																
						reportDetailsViewer.setInput(reportUIModel.getBenchmarksAttributes().get(selEntry.getName()));
						reportDetailsViewer.refresh();
					}
					reportTabs.setSelection(1);				
				}			
			}
		};
	}

	private void hookDoubleClickAction() {
		reportsListViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	
	@Override
	public void setFocus() {
		reportsListViewer.getControl().setFocus();
	}
	
	@Override
	public void refreshView () {
		loadData();
		reportsListViewer.setInput(this.reportUIModel.getListOfBenchmarks());
		//reportsListViewer.setSelection(new StructuredSelection(reportsListViewer.getElementAt(0)),true);
		reportsListViewer.refresh();
		setDataForSecondaryDisplayElements();
		
	}
	
	private void setDataForSecondaryDisplayElements () {
		if (this.reportUIModel.getListOfBenchmarks().size()>0) {
			reportTabs.setSelection(0);
			NameValueEntry selEntry = this.reportUIModel.getListOfBenchmarks().get(0) ;
			if (reportUIModel.getBenchmarksAttributes().get(selEntry.getName()) != null){
			
				overviewAttributesViewer.setInput(reportUIModel.getListOfOverviewProperties());
				overviewAttributesViewer.refresh();
				
				reportDetailsViewer.setInput(reportUIModel.getBenchmarksAttributes().get(selEntry.getName()));
				reportDetailsViewer.refresh();
				
				jvmAttributesViewer.setInput(reportUIModel.getListOfJVMProperties());
				jvmAttributesViewer.refresh();
				
				hwAttributesViewer.setInput(reportUIModel.getListOfHwProperties());
				hwAttributesViewer.refresh();
			}
		}
	}
	private void createSummaryDetailsViewer (Composite parent) {
		this.overviewAttributesViewer =  new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION  );
		
		createColumns(parent, overviewAttributesViewer);
		
		final Table table = overviewAttributesViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
//        table.setForeground(colorGray);       
        
        overviewAttributesViewer.setContentProvider(new ArrayContentProvider());
		
        
     // Layout the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        overviewAttributesViewer.getControl().setLayoutData(gridData);
		
		/*GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		summaryDetailsViewer.setLayout(layout);
		GridData rightGroupData = new GridData(GridData.FILL_BOTH);
		rightGroupData.horizontalSpan = 2;
		summaryDetailsViewer.setLayoutData(rightGroupData);
		    
		
		Label label = new Label (summaryDetailsViewer,SWT.LEFT) ;
		label.setText("Report Name - My First CyBench report");
		Label label2 = new Label (summaryDetailsViewer,SWT.LEFT) ;
		label2.setText("Total Score - 1,200.99");
		
		Label label3 = new Label (summaryDetailsViewer,SWT.LEFT) ;
		label3.setText("Link on CyBench Web - https://www.gocypher.com/cybench");
		*/
	}
	private void createReportDetailsViewer (Composite parent) {
		reportDetailsViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION  );
		createColumns(parent, reportDetailsViewer);
		
		final Table table = reportDetailsViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
//        table.setForeground(colorGray);       
        
        
        reportDetailsViewer.setContentProvider(new ArrayContentProvider());
        // Get the content for the viewer, setInput will call getElements in the
        // contentProvider
        //reportDetailsViewer.setInput(NameValueModelProvider.INSTANCE.getEntries());
		
        
     // Layout the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        reportDetailsViewer.getControl().setLayoutData(gridData);
        
	}
	
	private void createJVMAttributesViewer (Composite parent) {
		jvmAttributesViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION  );
		createColumns(parent, jvmAttributesViewer);
		
		final Table table = jvmAttributesViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
//        table.setForeground(colorGray);       
                
        jvmAttributesViewer.setContentProvider(new ArrayContentProvider());
        // Layout the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        
        jvmAttributesViewer.getControl().setLayoutData(gridData);
        
	}
	
	private void createHWAttributesViewer (Composite parent) {
		hwAttributesViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION  );
		createColumns(parent, hwAttributesViewer);
		
		final Table table = hwAttributesViewer.getTable();
		
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
//        table.setForeground(colorGray);       
                
        hwAttributesViewer.setContentProvider(new ArrayContentProvider());
        // Layout the viewer
        GridData gridData = new GridData();
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        
        hwAttributesViewer.getControl().setLayoutData(gridData);
		
	}
	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "Attribute Name", "Attribute Value"};
        int[] bounds = { 400, 500 };
        
        // First column is for the first name
        TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0, viewer);
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                NameValueEntry p = (NameValueEntry) element;
                return p.getName();
            }
        });

        // Second column is for the last name
        col = createTableViewerColumn(titles[1], bounds[1], 1,viewer);
        col.getColumn().setAlignment(SWT.RIGHT);
        col.setLabelProvider(new DelegatingStyledCellLabelProvider(
                new ValueLabelProvider()));
	}
	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber,final TableViewer viewer) {
        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
                SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(title);
        column.setWidth(bound);
        column.setResizable(true);
        column.setMoveable(true);
        
        return viewerColumn;
    }
	class ValueLabelProvider extends LabelProvider implements IStyledLabelProvider {
        public ValueLabelProvider() {
           
        }
        @Override
        public StyledString getStyledText(Object element) {
            if (element instanceof NameValueEntry) {
            	NameValueEntry p = (NameValueEntry) element;
            	if (p.getValue() != null) {
            		return new StyledString(p.getValue(),valueStyler);
            	}
                       
            }
            return new StyledString("");
        }
    }
	
}
