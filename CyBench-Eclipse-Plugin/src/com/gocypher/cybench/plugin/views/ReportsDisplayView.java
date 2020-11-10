package com.gocypher.cybench.plugin.views;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.part.*;

import com.gocypher.cybench.core.utils.JSONUtils;
import com.gocypher.cybench.launcher.utils.Constants;
import com.gocypher.cybench.launcher.utils.CybenchUtils;
import com.gocypher.cybench.plugin.Activator;
import com.gocypher.cybench.plugin.model.ICybenchPartView;
import com.gocypher.cybench.plugin.model.NameValueEntry;
import com.gocypher.cybench.plugin.model.NameValueModelProvider;
import com.gocypher.cybench.plugin.model.ReportFileEntry;
import com.gocypher.cybench.plugin.model.ReportFileEntryComparator;
import com.gocypher.cybench.plugin.model.ReportHandlerService;
import com.gocypher.cybench.plugin.model.ReportUIModel;
import com.gocypher.cybench.plugin.utils.GuiUtils;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

public class ReportsDisplayView extends ViewPart implements ICybenchPartView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.gocypher.cybench.plugin.views.ReportsDisplayView";

	@Inject IWorkbench workbench;
	@Inject ESelectionService selectionService ;
	@Inject ReportHandlerService reportService ;
	
	private CTabFolder reportTabs ;
	private TableViewer reportsListViewer;
	private TableViewer jvmAttributesViewer;
	private TableViewer hwAttributesViewer;
	
	private Text reportTextArea ;
	
	
	private Action refreshAction;
	private Action action2;
	private Action doubleClickAction;
	
	//private List<ReportFileEntry> listOfFiles = new ArrayList<>();
	private String reportRawData = "" ;
	
	private ReportFileEntry selectedReport ;
	
	private TableViewer reportDetailsViewer ;
	private ReportUIModel reportUIModel = new ReportUIModel();
	
	private static Color colorGray= Display.getCurrent().getSystemColor(
            SWT.COLOR_GRAY);
	 
	
	@PostConstruct
	public void init ( ) {
		//System.out.println("--->Init called:"+reportService);
		//System.out.println("Titles:"+CybenchUtils.titles);
		//System.out.println("Part properties:"+this.getViewSite().getSecondaryId());
		
		
		
		/*this.selectionService.addSelectionListener(new ISelectionListener() {
			
			@Override
			public void selectionChanged(MPart sourcePart, Object selection) {
				if (selection instanceof ReportFileEntry) {
					System.out.println("Selection service:"+sourcePart.getElementId() +";"+selection);
					//selectedReport = (ReportFileEntry)selection ;
					//reportUIModel = reportService.prepareReportDisplayModel(selectedReport) ;
					//refreshView();
					
					
				}
				
			}
		});
		*/
		//this.reportService.prepareReportDisplayModel();
		this.loadData();
		
	}
	
	private void loadData () {
		//String pathToPluginLocalStateDirectory = Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID)).toPortableString() ;
		//System.out.println("Reports default directory:"+pathToPluginLocalStateDirectory);
		
		IViewPart explorerView = workbench.getActiveWorkbenchWindow().getActivePage().findView(CyBenchExplorerView.ID) ;
		if (explorerView instanceof CyBenchExplorerView 
				&& this.getViewSite().getSecondaryId() != null 
				&& !this.getViewSite().getSecondaryId().isEmpty()) {
			
				System.out.println("Explorer:"+explorerView);
				CyBenchExplorerView cybenchExplorerView = (CyBenchExplorerView)explorerView ;
				
				//String reportIdentifier = this.getViewSite().getSecondaryId() ;	
				//ReportFileEntry entry = cybenchExplorerView.findEntryByIdentifier(reportIdentifier);
				
				ReportFileEntry entry = new ReportFileEntry() ;
				entry.setFullPathToFile(GuiUtils.decodeBase64(this.getViewSite().getSecondaryId()));
				
				reportUIModel = reportService.prepareReportDisplayModel(entry) ;
				
				this.setPartName(reportUIModel.getReportTitle());
		}
		
		
		//this.listOfFiles.clear();
		/*List<File>reportsFiles = CybenchUtils.listFilesInDirectory(pathToPluginLocalStateDirectory) ;
		
		for (File file:reportsFiles) {
			if (file.getName().endsWith(Constants.REPORT_FILE_EXTENSION)) {
				ReportFileEntry entry = new ReportFileEntry() ;
				entry.create(file);
				listOfFiles.add(entry) ;
			}
		}
		Collections.sort(listOfFiles, new ReportFileEntryComparator ());
		if (listOfFiles.size() > 0) {
			this.reportRawData =  CybenchUtils.loadFile(listOfFiles.get(0).getFullPathToFile()) ;	
			this.extractReportProperties(this.reportRawData, NameValueModelProvider.INSTANCE.getEntries());
		}
		*/
		
		
		//System.out.println("Reports:"+listOfFiles);
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
		//System.out.println("-->Creating part view for reports");
	
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
		reportsListViewer.setSelection(new StructuredSelection(reportsListViewer.getElementAt(0)),true);
		
		workbench.getHelpSystem().setHelp(reportsListViewer.getControl(), "CyBenchLauncherPlugin.viewer");
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
        
  
        CTabItem benchmarkDetailsTab = new CTabItem(reportTabs, SWT.NONE);       
        reportTabs.setSelection(0);
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
		
		/*reportTextArea = new Text (rightGroup, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL ) ;
		reportTextArea.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL  | GridData.VERTICAL_ALIGN_FILL));
		reportTextArea.setEditable(false);
		reportTextArea.setText(this.reportRawData);
		*/
		
		/*
		Table table = new Table(rightGroup, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = 200;
        table.setLayoutData(data);

        String[] titles = { "First Name", "Last Name", "Age" };
        for (int i = 0; i < titles.length; i++) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(titles[i]);
            table.getColumn(i).pack();
        }

        for (int i = 0 ; i<= 50 ; i++){
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText (0, "Person " +i );
            item.setText (1, "LastName " +i );
            item.setText (2, String.valueOf(i));
        }

        for (int i=0; i<titles.length; i++) {
            table.getColumn (i).pack ();
        }
        */
		/*CTabFolder folder = new CTabFolder(rightGroup, SWT.LEFT);
		GridData data = new GridData(SWT.FILL,
                SWT.FILL, true, true,
                2, 1);
        folder.setLayoutData(data);
        
        
        
        CTabItem cTabItem1 = new CTabItem(folder, SWT.NONE);
        
        folder.setSelection(0);
        
        cTabItem1.setText("Raw report");
        
        
		
		
		cTabItem1.setControl(reportTextArea);
		
		
		CTabItem cTabItem2 = new CTabItem(folder, SWT.NONE);
		cTabItem2.setText("Report");
		
		TableViewer localViewer = new TableViewer(folder, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
		
		localViewer.setContentProvider(ArrayContentProvider.getInstance());
		localViewer.setInput(this.listOfFiles);
		localViewer.setLabelProvider(new ViewLabelProvider());
		
		cTabItem2.setControl(localViewer.getControl());
		*/
		
		/*GridData areaData = new GridData();
	    areaData.grabExcessHorizontalSpace = true;
	    areaData.grabExcessVerticalSpace = true;
	    areaData.horizontalAlignment = GridData.FILL;
	    areaData.verticalAlignment = GridData.FILL;
	    //areaData.widthHint = 200;
	    areaData.heightHint = 80;
	    reportTextArea.setLayoutData(areaData);
	    */
		
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
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(action2);
	}

	private void makeActions() {
		refreshAction = new Action() {
			public void run() {
				refreshView();
				
			}
		};
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refresh list of report files");
		refreshAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(workbench.getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = reportsListViewer.getStructuredSelection();
				Object obj = selection.getFirstElement();
				//System.out.println("Selected on the left:"+obj + ";"+obj.getClass());
				if (obj instanceof NameValueEntry) {
					NameValueEntry selEntry = (NameValueEntry)obj ;
					if (reportUIModel.getBenchmarksAttributes().get(selEntry.getName()) != null){
											
						//ameValueModelProvider.INSTANCE.getEntries().clear();
						//NameValueModelProvider.INSTANCE.getEntries().addAll(reportUIModel.getBenchmarksAttributes().get(selEntry.getName()) ) ;
						//reportDetailsViewer.setInput(NameValueModelProvider.INSTANCE.getEntries());
						reportDetailsViewer.setInput(reportUIModel.getBenchmarksAttributes().get(selEntry.getName()));
						reportDetailsViewer.refresh();
					}
					reportTabs.setSelection(0);
						
				}
				
				if (obj instanceof ReportFileEntry) {
					ReportFileEntry file = (ReportFileEntry)obj ;
					//reportRawData = CybenchUtils.loadFile(file.getFullPathToFile()) ;
					
					//extractReportProperties(reportRawData, NameValueModelProvider.INSTANCE.getEntries());
					//reportDetailsViewer.setInput(NameValueModelProvider.INSTANCE.getEntries());
					//reportDetailsViewer.refresh();
					
					//System.out.println("Report Raw data:"+reportRawData);
					/*reportTextArea.setText(reportRawData);
					reportTextArea.redraw();
					reportTextArea.update();
					*/
				}
				
				//showMessage("Double-click detected on "+obj.getClass());
				
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
	private void showMessage(String message) {
		MessageDialog.openInformation(
				reportsListViewer.getControl().getShell(),
			"Cybench Reports",
			message);
	}

	@Override
	public void setFocus() {
		reportsListViewer.getControl().setFocus();
	}
	
	@Override
	public void refreshView () {
		loadData();
		reportsListViewer.setInput(this.reportUIModel.getListOfBenchmarks());
		reportsListViewer.setSelection(new StructuredSelection(reportsListViewer.getElementAt(0)),true);
		reportsListViewer.refresh();
		setDataForSecondaryDisplayElements();
		
			
		/*reportTextArea.setText(reportRawData);
		reportTextArea.redraw();
		reportTextArea.update();
		*/
	}
	
	private void setDataForSecondaryDisplayElements () {
		if (this.reportUIModel.getListOfBenchmarks().size()>0) {
			reportTabs.setSelection(0);
			NameValueEntry selEntry = this.reportUIModel.getListOfBenchmarks().get(0) ;
			if (reportUIModel.getBenchmarksAttributes().get(selEntry.getName()) != null){
			
				reportDetailsViewer.setInput(reportUIModel.getBenchmarksAttributes().get(selEntry.getName()));
				reportDetailsViewer.refresh();
				
				jvmAttributesViewer.setInput(reportUIModel.getListOfJVMProperties());
				jvmAttributesViewer.refresh();
				
				hwAttributesViewer.setInput(reportUIModel.getListOfHwProperties());
				hwAttributesViewer.refresh();
			}
		}
	}
	private void createReportDetailsViewer (Composite parent) {
		reportDetailsViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION  );
		createColumns(parent, reportDetailsViewer);
		
		final Table table = reportDetailsViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setHeaderBackground(colorGray);
        
        
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
        table.setHeaderBackground(colorGray);
                
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
        table.setHeaderBackground(colorGray);
                
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
        int[] bounds = { 400, 400 };
        
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
        col.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
            	NameValueEntry p = (NameValueEntry) element;
                return p.getValue();
            }
        });
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
	
}
