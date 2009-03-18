// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.action.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.talend.commons.emf.EMFUtil;
import org.talend.cwm.dependencies.DependenciesHandler;
import org.talend.dataprofiler.core.CorePlugin;
import org.talend.dataprofiler.core.ImageLib;
import org.talend.dataprofiler.core.i18n.internal.DefaultMessagesImpl;
import org.talend.dataprofiler.core.manager.DQStructureManager;
import org.talend.dataprofiler.core.ui.views.DQRespositoryView;
import org.talend.dataquality.analysis.Analysis;
import org.talend.dataquality.helpers.ReportHelper;
import org.talend.dataquality.reports.TdReport;
import org.talend.dq.helper.resourcehelper.RepResourceFileHelper;
import org.talend.dq.nodes.foldernode.IFolderNode;

/**
 * DOC rli RemoveAnalysisActionProvider class global comment. Detailled comment
 */
public class RemoveAnalysisAction extends Action {

	public RemoveAnalysisAction() {
		super(DefaultMessagesImpl
				.getString("RemoveAnalysisAction.removeAnalysis")); //$NON-NLS-1$
		setImageDescriptor(ImageLib.getImageDescriptor(ImageLib.DELETE_ACTION));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		DQRespositoryView findView = (DQRespositoryView) CorePlugin
				.getDefault().findView(DQRespositoryView.ID);
		TreeSelection treeSelection = (TreeSelection) findView
				.getCommonViewer().getSelection();
		TreePath[] paths = treeSelection.getPaths();
		TdReport parentReport;
		List<Analysis> analysisList;
		Analysis analysisObj = null;
		Map<TdReport, List<Analysis>> removeMap = new HashMap<TdReport, List<Analysis>>();
		for (int i = 0; i < paths.length; i++) {
			Object lastSegment = paths[i].getLastSegment();
			if (!(lastSegment instanceof Analysis)) {
				return;
			}
			analysisObj = (Analysis) lastSegment;
			IFolderNode folderNode = (IFolderNode) paths[i].getSegment(paths[i]
					.getSegmentCount() - 2);
			parentReport = (TdReport) folderNode.getParent();
			analysisList = removeMap.get(parentReport);
			if (analysisList == null) {
				analysisList = new ArrayList<Analysis>();
				analysisList.add(analysisObj);
				removeMap.put(parentReport, analysisList);
			} else {
				analysisList.add(analysisObj);
			}
		}
		if (analysisObj == null) {
			return;
		}
		String message = paths.length > 1 ? DefaultMessagesImpl.getString(
				"RemoveAnalysisAction.areYouDeleteElement0", paths.length) //$NON-NLS-1$ //$NON-NLS-2$
				: DefaultMessagesImpl
						.getString(
								"RemoveAnalysisAction.areYouDeleteElement2", analysisObj.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		boolean openConfirm = MessageDialog
				.openConfirm(
						null,
						DefaultMessagesImpl
								.getString("RemoveAnalysisAction.confirmResourceDelete"), message); //$NON-NLS-1$

		if (openConfirm) {
			Iterator<TdReport> iterator = removeMap.keySet().iterator();
			while (iterator.hasNext()) {
				TdReport report = iterator.next();
				ReportHelper.removeAnalyses(report, removeMap.get(report));

				// save now modified resources (that contain the Dependency
				// objects)
				List<Resource> modifiedResources = DependenciesHandler
						.getInstance().removeDependenciesBetweenModels(report,
								removeMap.get(report));
				for (int i = 0; i < modifiedResources.size(); i++) {
					EMFUtil.saveSingleResource(modifiedResources.get(i));
				}

				RepResourceFileHelper.getInstance().save(report);
			}
			// MOD mzhao 2009-03-13 Feature 6066 Move all folders into one
			// project.
			IFolder reportsFolder = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(
							org.talend.dataquality.PluginConstant
									.getRootProjectName()).getFolder(
							DQStructureManager.getDataProfiling()).getFolder(
							DQStructureManager.REPORTS);
			try {
				reportsFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			findView.getCommonViewer().refresh();
		}
	}
}
