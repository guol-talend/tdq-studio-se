// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.editor.composite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.talend.cwm.helper.ColumnHelper;
import org.talend.cwm.relational.TdColumn;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.PluginConstant;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.ui.dialog.ColumnsSelectionDialog;
import org.talend.dataprofiler.core.ui.editor.analysis.AbstractAnalysisMetadataPage;
import org.talend.dataprofiler.core.ui.editor.analysis.ColumnDependencyMasterDetailsPage;
import org.talend.dataprofiler.core.ui.views.DQRespositoryView;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.indicators.Indicator;
import org.talend.dataquality.indicators.columnset.RowMatchingIndicator;
import orgomg.cwm.resource.relational.Column;
import orgomg.cwm.resource.relational.ColumnSet;

/**
 * 
 * DOC mzhao 2009-06-17 feature 5887
 */
public class AnalysisColumnCompareTreeViewer extends AbstractPagePart {

    private static Logger log = Logger.getLogger(AnalysisColumnCompareTreeViewer.class);

    private AbstractAnalysisMetadataPage masterPage;

    private Composite parentComp;

    private ScrolledForm form = null;

    private List<Column> columnListA;

    // MOD mzhao 2009-06-17 feature 5887
    private SelectionListener selectionListener = null;

    // ADD mzhao 2009-02-03 Tableviewer creation stack that remember left or
    // right position.
    private List<TableViewer> tableViewerPosStack = null;

    private List<Column> columnListB;

    private Section columnsComparisonSection = null;

    private FormToolkit toolkit;

    private Button checkComputeButton;

    private TableViewer rightTable = null;

    private TableViewer leftTable = null;

    private String mainTitle;

    private String titleDescription;

    private Analysis analysis = null;

    private boolean showCheckButton = true;

    private boolean checkComputButton = false;

    private boolean allowColumnDupcation = false;

    private Button columnReverseButtion;

    public AnalysisColumnCompareTreeViewer(AbstractAnalysisMetadataPage masterPage, Composite topComp, List<Column> columnSetA,
            List<Column> columnSetB, String mainTitle, String description, boolean showCheckButton, boolean allowColumnDupcation) {
        this.masterPage = masterPage;
        this.analysis = masterPage.getAnalysis();
        form = masterPage.getScrolledForm();
        toolkit = masterPage.getEditor().getToolkit();
        this.parentComp = topComp;

        columnListA = new ArrayList<Column>();
        columnListB = new ArrayList<Column>();
        tableViewerPosStack = new ArrayList<TableViewer>();

        columnListA.addAll(columnSetA);
        columnListB.addAll(columnSetB);

        this.showCheckButton = showCheckButton;
        this.allowColumnDupcation = allowColumnDupcation;
        createAnalyzedColumnSetsSection(mainTitle, description);

    }

    public AnalysisColumnCompareTreeViewer(AbstractAnalysisMetadataPage masterPage, Composite topComp, Analysis analysis,
            boolean allowColumnDupcation) {

        this(masterPage, topComp, new ArrayList<Column>(), new ArrayList<Column>(), DefaultMessagesImpl
                .getString("ColumnsComparisonMasterDetailsPage.analyzedColumnSets"), DefaultMessagesImpl //$NON-NLS-1$
                .getString("ColumnsComparisonMasterDetailsPage.SelectTableOrColumnsCompare"), true, allowColumnDupcation); //$NON-NLS-1$

        if (analysis.getResults().getIndicators().size() > 0) {
            EList<Indicator> indicators = analysis.getResults().getIndicators();
            RowMatchingIndicator rowMatchingIndicatorA = (RowMatchingIndicator) indicators.get(0);
            columnListA.addAll(rowMatchingIndicatorA.getColumnSetA());
            RowMatchingIndicator rowMatchingIndicatorB = (RowMatchingIndicator) indicators.get(1);
            columnListB.addAll(rowMatchingIndicatorA.getColumnSetB());

        }

        this.analysis = analysis;
        checkComputButton = analysis.getParameters().getDeactivatedIndicators().size() != 0;

    }

    private void createAnalyzedColumnSetsSection(String mainTitle, String description) {
        columnsComparisonSection = masterPage.createSection(form, parentComp, mainTitle, description); //$NON-NLS-1$ //$NON-NLS-2$
        Composite sectionClient = toolkit.createComposite(columnsComparisonSection);
        sectionClient.setLayout(new GridLayout());
        // sectionClient.setLayout(new GridLayout(2, true));
        // this.createSectionPart(form, sectionClient, "left Columns");
        // this.createSectionPart(form, sectionClient, "Right Columns");

        if (showCheckButton) {
            checkComputeButton = new Button(sectionClient, SWT.CHECK);
            GridData layoutData = new GridData(GridData.FILL_BOTH);
            layoutData.horizontalAlignment = SWT.CENTER;
            checkComputeButton.setLayoutData(layoutData);
            checkComputeButton.setText(DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.Compute")); //$NON-NLS-1$
            checkComputeButton.setToolTipText(DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.WhenUnchecked")); //$NON-NLS-1$
            checkComputeButton.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    masterPage.setDirty(true);
                }

            });
            checkComputeButton.setSelection(checkComputButton);
        }

        Composite columnComp = toolkit.createComposite(sectionClient);
        columnComp.setLayoutData(new GridData(GridData.FILL_BOTH));
        columnComp.setLayout(new GridLayout());

        Composite compareToplevelComp = toolkit.createComposite(columnComp);
        GridLayout compareToplevelLayout = new GridLayout();
        compareToplevelLayout.numColumns = 2;
        compareToplevelComp.setLayout(compareToplevelLayout);
        // ~ MOD mzhao 2009-05-05,Bug 6587.
        masterPage.createConnBindWidget(compareToplevelComp);
        // ~
        // !MOD mzhao 2010-03-08,Feature 11387. Add reverse action to make it easy for columns comparing on opposite
        // way.
        columnReverseButtion = new Button(compareToplevelComp, SWT.NONE);
        // GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).applyTo(compareToplevelComp);
        columnReverseButtion.setText("Reverse columns");
        columnReverseButtion.addMouseListener(new MouseListener() {

            public void mouseDoubleClick(MouseEvent e) {

            }

            public void mouseDown(MouseEvent e) {
                handleColumnReverseAction();
            }

            public void mouseUp(MouseEvent e) {

            }

        });
        columnReverseButtion.addKeyListener(new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.keyCode == 13) {
                    handleColumnReverseAction();
                }
            }

            public void keyReleased(KeyEvent e) {

            }

        });
        // ~

        SashForm sashForm = new SashForm(sectionClient, SWT.NULL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        String hyperlinkTextLeft = null;
        String hyperlinkTextRight = null;
        if (masterPage instanceof ColumnDependencyMasterDetailsPage) {
            hyperlinkTextLeft = DefaultMessagesImpl.getString("AnalysisColumnCompareTreeViewer.DeterminantCol"); //$NON-NLS-1$
            hyperlinkTextRight = DefaultMessagesImpl.getString("AnalysisColumnCompareTreeViewer.DependentCol"); //$NON-NLS-1$
        } else {
            hyperlinkTextLeft = DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.selectColumnsForASet"); //$NON-NLS-1$
            hyperlinkTextRight = DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.selectColumnsForBSet"); //$NON-NLS-1$
        }
        Composite leftComp = toolkit.createComposite(sashForm);
        leftComp.setLayoutData(new GridData(GridData.FILL_BOTH));
        leftComp.setLayout(new GridLayout());
        leftTable = this.createSectionPart(leftComp, columnListA, DefaultMessagesImpl
                .getString("ColumnsComparisonMasterDetailsPage.leftColumns"), hyperlinkTextLeft); //$NON-NLS-1$ 

        Composite rightComp = toolkit.createComposite(sashForm);
        rightComp.setLayoutData(new GridData(GridData.FILL_BOTH));
        rightComp.setLayout(new GridLayout());
        rightTable = this.createSectionPart(rightComp, columnListB, DefaultMessagesImpl
                .getString("ColumnsComparisonMasterDetailsPage.rightColumns"), hyperlinkTextRight); //$NON-NLS-1$ 
        // MOD mzhao 2009-05-05 bug:6587.
        updateBindConnection(masterPage, tableViewerPosStack);
        columnsComparisonSection.setClient(sectionClient);
    }

    /**
     * DOC jet Comment method "refreash". redraw selected content.
     */
    public void refreash() {
        rightTable.refresh();
        leftTable.refresh();
    }

    private TableViewer createSectionPart(Composite parentComp, final List<Column> columnList, String title, String hyperlinkText) {
        Section columnSetElementSection = masterPage.createSection(form, parentComp, title, null);
        Composite sectionComp = toolkit.createComposite(columnSetElementSection);
        sectionComp.setLayout(new GridLayout());

        Hyperlink selectColumnBtn = toolkit.createHyperlink(sectionComp, hyperlinkText, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(selectColumnBtn);

        Composite columsComp = toolkit.createComposite(sectionComp, SWT.NULL);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, true).applyTo(columsComp);
        columsComp.setLayout(new GridLayout());
        final TableViewer columnsElementViewer = createTreeViewer(columnList, columsComp);
        // ~ADD mzhao for cheat sheets later open column selection dialog.
        tableViewerPosStack.add(columnsElementViewer);
        // ~
        // DragAndDropDecorate decorate = new DragAndDropDecorate();
        // decorate.toDecorateDragAndDrop(columnsElementViewer);
        TableViewerDNDDecorate dndDecorate = new TableViewerDNDDecorate(allowColumnDupcation);
        dndDecorate.installDND(columnsElementViewer, true, TableViewerDNDDecorate.COLUMN_VALIDATETYPE);

        Composite buttonsComp = toolkit.createComposite(columsComp, SWT.NULL);
        buttonsComp.setLayout(new GridLayout(4, true));
        buttonsComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final Button delButton = new Button(buttonsComp, SWT.NULL);
        delButton.setImage(ImageLib.getImage(ImageLib.DELETE_ACTION));
        GridData buttonGridData = new GridData(GridData.FILL_BOTH);
        delButton.setLayoutData(buttonGridData);
        final Button moveUpButton = new Button(buttonsComp, SWT.NULL);
        moveUpButton.setText(DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.moveUp")); //$NON-NLS-1$
        moveUpButton.setLayoutData(buttonGridData);
        final Button moveDownButton = new Button(buttonsComp, SWT.NULL);
        moveDownButton.setText(DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.moveDown")); //$NON-NLS-1$
        moveDownButton.setLayoutData(buttonGridData);

        Button sortButton = new Button(buttonsComp, SWT.NULL);
        sortButton.setText(DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.sort")); //$NON-NLS-1$
        sortButton.setLayoutData(buttonGridData);

        final Button[] buttons = new Button[] { delButton, moveUpButton, moveDownButton };
        columnsElementViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                enabledButtons(buttons, event.getSelection() != null);

            }
        });

        // ADD 2009-01-07 mzhao for feature:0005664
        createTableViewerMenu(columnsElementViewer, columnList, buttons);
        delButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                columnList.removeAll(((IStructuredSelection) columnsElementViewer.getSelection()).toList());
                columnsElementViewer.setInput(columnList);
                enabledButtons(buttons, false);
                masterPage.setDirty(true);
                // MOD mzhao 2009-05-05 bug:6587.
                // MOD mzhao feature 5887 2009-06-17
                // updateBindConnection(masterPage);
            }

        });

        moveUpButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                moveElement(columnList, columnsElementViewer, false);

            }

        });
        moveDownButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                moveElement(columnList, columnsElementViewer, true);

            }

        });
        sortButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                // MOD xqliu 2009-01-17, bug 5940: achieve the function of sort
                // button
                sortElement(columnList, columnsElementViewer);
            }

        });
        this.enabledButtons(new Button[] { delButton, moveUpButton, moveDownButton }, false);
        final List<Column> columnsOfSectionPart = columnList;
        selectColumnBtn.addHyperlinkListener(new HyperlinkAdapter() {

            public void linkActivated(HyperlinkEvent e) {
                openColumnsSelectionDialog(columnsElementViewer, columnsOfSectionPart);
                // Object input = columnsElementViewer.getInput();
                // List<Object> columnSet = (List<Object>) input;
                enabledButtons(buttons, false);
            }

        });

        columnSetElementSection.setClient(sectionComp);
        return columnsElementViewer;

    }

    /**
     * MOD mzhao 2009-02-03,remove the first parameter, extract it to class property filed for the convenience of
     * invoking this method from cheat sheets.
     */
    public void openColumnsSelectionDialog(TableViewer columnsElementViewer, List<Column> columnsOfSectionPart) {
        ColumnsSelectionDialog dialog = new ColumnsSelectionDialog(masterPage, null, DefaultMessagesImpl
                .getString("ColumnMasterDetailsPage.columnSelection"), columnsOfSectionPart, //$NON-NLS-1$
                DefaultMessagesImpl.getString("ColumnMasterDetailsPage.columnSelections")); //$NON-NLS-1$
        if (dialog.open() == Window.OK) {
            Object[] columns = dialog.getResult();
            List<Column> columnSet = new ArrayList<Column>();
            for (Object obj : columns) {
                columnSet.add((Column) obj);
            }
            columnsElementViewer.setInput(columnSet);
            columnsOfSectionPart.clear();
            columnsOfSectionPart.addAll(columnSet);
            if (columnSet.size() != 0) {
                String tableName = ColumnHelper.getColumnSetOwner((TdColumn) columnSet.get(0)).getName();
                columnsElementViewer.getTable().getColumn(0).setText(
                        DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.elements", tableName)); //$NON-NLS-1$
            }
            updateBindConnection(masterPage, tableViewerPosStack);
        }
    }

    /**
     * DOC rli Comment method "moveElement".
     * 
     * @param columnList
     * @param columnsElementViewer
     */
    private void moveElement(List<Column> columnList, TableViewer columnsElementViewer, boolean isDown) {
        // Object firstElement = ((IStructuredSelection) columnsElementViewer.getSelection()).getFirstElement();
        // int index = columnList.indexOf(firstElement);
        // if (isDown) {
        // if ((index + 1) < columnList.size()) {
        // columnList.remove(firstElement);
        // columnList.add((index + 1), (Column) firstElement);
        // }
        // } else {
        // if ((index - 1) >= 0) {
        // columnList.remove(firstElement);
        // columnList.add((index - 1), (Column) firstElement);
        // }
        //
        // }
        // columnsElementViewer.setInput(columnList);
        Object[] elementArray = ((IStructuredSelection) columnsElementViewer.getSelection()).toArray();
        if (isDown) {
            for (int i = elementArray.length - 1; i >= 0; i--) {
                Column currentElement = (Column) elementArray[i];
                int index = columnList.indexOf(currentElement);
                if ((index + 1) < columnList.size()) {
                    columnList.remove(currentElement);
                    columnList.add((index + 1), currentElement);
                } else {
                    break;
                }
            }
        } else {
            for (int i = 0; i < elementArray.length; i++) {
                Column currentElement = (Column) elementArray[i];
                int index = columnList.indexOf(currentElement);
                if ((index - 1) >= 0) {
                    columnList.remove(currentElement);
                    columnList.add((index - 1), currentElement);
                } else {
                    break;
                }
            }
        }
        columnsElementViewer.setInput(columnList);
    }

    /**
     * 
     * DOC xqliu Comment method "sortElement".
     * 
     * @param columnList
     * @param columnsElementViewer
     * @param asc
     */
    private void sortElement(List<Column> columnList, TableViewer columnsElementViewer) {
        Collections.sort(columnList, new CaseInsensitiveComparator());
        columnsElementViewer.setInput(columnList);
    }

    /**
     * 
     * DOC xqliu ColumnsComparisonMasterDetailsPage class global comment. Detailled comment
     */
    private class CaseInsensitiveComparator implements Comparator {

        public int compare(Object element1, Object element2) {
            Column col1 = (Column) element1;
            Column col2 = (Column) element2;
            String lower1 = col1.getName().toLowerCase();
            String lower2 = col2.getName().toLowerCase();
            return lower1.compareTo(lower2);
        }
    }

    /**
     * DOC rli Comment method "setColumnAB".
     */
    public void setColumnABForMatchingIndicator(RowMatchingIndicator rowMatchingIndicator, List<Column> columnsA,
            List<Column> columnsB) {
        if (columnsA.size() != 0) {
            ColumnSet columnSetOwner = ColumnHelper.getColumnSetOwner((TdColumn) columnsA.get(0));
            rowMatchingIndicator.setAnalyzedElement(columnSetOwner);
        }
        rowMatchingIndicator.getColumnSetA().clear();
        rowMatchingIndicator.getColumnSetA().addAll(columnsA);
        rowMatchingIndicator.getColumnSetB().clear();
        rowMatchingIndicator.getColumnSetB().addAll(columnsB);
    }

    private TableViewer createTreeViewer(final List<Column> columnList, Composite columsComp) {
        TableViewer columnsElementViewer = new TableViewer(columsComp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);

        Table table = columnsElementViewer.getTable();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(table);
        ((GridData) table.getLayoutData()).heightHint = 280;
        table.setHeaderVisible(true);
        table.setDragDetect(true);
        table.setToolTipText(DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.reorderElementsByDragAnddrop")); //$NON-NLS-1$
        final TableColumn columnHeader = new TableColumn(table, SWT.CENTER);

        columnHeader.setWidth(260);
        columnHeader.setAlignment(SWT.CENTER);
        if (columnList.size() > 0) {
            String tableName = ColumnHelper.getColumnSetOwner((TdColumn) columnList.get(0)).getName();
            columnHeader.setText(DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.element", tableName)); //$NON-NLS-1$
        }

        ColumnsElementViewerProvider provider = new ColumnsElementViewerProvider();
        columnsElementViewer.setContentProvider(provider);
        columnsElementViewer.setLabelProvider(provider);
        columnsElementViewer.setInput(columnList);
        return columnsElementViewer;

    }

    private void createTableViewerMenu(final TableViewer columnsElementViewer, final List<Column> columnList,
            final Button[] buttons) {
        Table table = columnsElementViewer.getTable();

        Menu menu = new Menu(table);
        MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setImage(ImageLib.getImage(ImageLib.DELETE_ACTION));
        menuItem.setText(DefaultMessagesImpl.getString("ColumnsComparisonMasterDetailsPage.removeElement")); //$NON-NLS-1$

        menuItem.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                if (columnList.remove(((IStructuredSelection) columnsElementViewer.getSelection()).getFirstElement())) {
                    columnsElementViewer.setInput(columnList);
                    enabledButtons(buttons, false);
                    masterPage.setDirty(true);
                    // MOD mzhao 2009-05-05 bug:6587.
                    // MOD mzhao 2009-06-17 remove the connection bind here feature
                    // 5887
                    // updateBindConnection();
                }
            }
        });

        MenuItem showMenuItem = new MenuItem(menu, SWT.CASCADE);
        showMenuItem.setText(DefaultMessagesImpl.getString("AnalysisColumnTreeViewer.showDQElement")); //$NON-NLS-1$
        showMenuItem.setImage(ImageLib.getImage(ImageLib.EXPLORE_IMAGE));
        showMenuItem.addSelectionListener(new SelectionAdapter() {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse .swt.events.SelectionEvent)
             */
            @Override
            public void widgetSelected(SelectionEvent e) {
                showSelectedElements(columnsElementViewer);
            }

        });

        table.setMenu(menu);
    }

    /**
     * 
     * DOC mzhao Comment method "showSelectedElements".
     * 
     * @param newTree
     */
    private void showSelectedElements(TableViewer tableView) {
        TableItem[] selection = tableView.getTable().getSelection();

        DQRespositoryView dqview = CorePlugin.getDefault().getRepositoryView();
        if (selection.length == 1) {
            try {
                Column column = (Column) selection[0].getData();
                dqview.showSelectedElements(column);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }

    private void enabledButtons(Button[] buttons, boolean enabled) {
        for (Button button : buttons) {
            button.setEnabled(enabled);
        }
    }

    /**
     * The provider for ColumnsElementViewer.
     */
    class ColumnsElementViewerProvider extends LabelProvider implements IStructuredContentProvider {

        @SuppressWarnings("unchecked")
        public Object[] getElements(Object inputElement) {
            List<Object> columnSet = (List<Object>) inputElement;
            return columnSet.toArray();
        }

        @SuppressWarnings("unchecked")
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (oldInput != null && newInput != null) {
                if (!((List) newInput).isEmpty()) {
                    masterPage.setDirty(true);
                }
            }
        }

        public Image getImage(Object element) {
            if (element instanceof TdColumn) {
                return ImageLib.getImage(ImageLib.TD_COLUMN);
            }
            return null;
        }

        public String getText(Object element) {
            if (element instanceof Column) {
                return ((Column) element).getName();
            }
            return PluginConstant.EMPTY_STRING;
        }

    }

    /**
     * 
     * DOC mzhao Open column selection dialog for left column set. this method is intended to use from cheat sheets.
     */
    public void openColumnsSetASelectionDialog() {
        openColumnsSelectionDialog(tableViewerPosStack.get(0), columnListA);
    }

    /**
     * 
     * DOC mzhao Open column selection dialog for right column set. this method is intended to use from cheat sheets.
     */
    public void openColumnsSetBSelectionDialog() {
        openColumnsSelectionDialog(tableViewerPosStack.get(1), columnListB);
    }

    @Override
    public void updateModelViewer() {
        if (analysis.getResults().getIndicators().size() != 0) {
            EList<Indicator> indicators = analysis.getResults().getIndicators();
            RowMatchingIndicator rowMatchingIndicatorA = (RowMatchingIndicator) indicators.get(0);
            columnListA.clear();
            columnListA.addAll(rowMatchingIndicatorA.getColumnSetA());
            tableViewerPosStack.get(0).setInput(columnListA);
            columnListB.clear();
            columnListB.addAll(rowMatchingIndicatorA.getColumnSetB());
            tableViewerPosStack.get(1).setInput(columnListB);
        }

    }

    public Section getColumnsComparisonSection() {
        return columnsComparisonSection;
    }

    public Button getCheckComputeButton() {
        return checkComputeButton;
    }

    public List<Column> getColumnListA() {
        return columnListA;
    }

    public List<Column> getColumnListB() {
        return columnListB;
    }

    /**
     * 
     * DOC mzhao feature 11387, 2010-03-08, AnalysisColumnCompareTreeViewer class global comment. Detailled comment
     */
    private void handleColumnReverseAction() {
        if (columnListA.size() != columnListB.size()) {
            return;
        }
        if (columnListA != null && columnListA.size() > 0) {
            int idx = 0;
            List<Integer> needToReverseIndex = new ArrayList<Integer>();
            for (Column column : columnListA) {
                if (canReverse(column, columnListB.get(idx))) {
                    needToReverseIndex.add(idx);
                }
                idx++;
            }
            for (Integer index : needToReverseIndex) {
                columnListB.add(columnListA.get(index));
                columnListA.add(columnListB.get(index));
                leftTable.add(columnListB.get(index));
                rightTable.add(columnListA.get(index));
                // Show on tree view
                masterPage.setDirty(true);
            }

        }

    }

    private Boolean canReverse(Column colA, Column colB) {
        int idx = 0;
        for (Column col : columnListA) {
            if (col == colB) {
                if (idx > columnListB.size() - 1) {
                    return false;
                } else if (columnListB.get(idx) == colA) {
                    return false;
                } else {
                    return true;
                }
            }
            idx++;
        }
        return true;
    }
}
