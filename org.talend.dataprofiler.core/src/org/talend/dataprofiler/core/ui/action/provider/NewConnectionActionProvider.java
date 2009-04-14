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
package org.talend.dataprofiler.core.ui.action.provider;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.talend.dataprofiler.core.manager.DQStructureManager;
import org.talend.dataprofiler.core.ui.action.actions.CreateConnectionAction;

/**
 * @author rli
 * 
 */
public class NewConnectionActionProvider extends CommonActionProvider {

    protected static Logger log = Logger.getLogger(NewConnectionActionProvider.class);

    /**
     * 
     */
    public NewConnectionActionProvider() {
    }

    /**
     * Adds a submenu to the given menu with the name "New Component".
     */
    public void fillContextMenu(IMenuManager menu) {
        Object obj = ((TreeSelection) this.getContext().getSelection()).getFirstElement();

        if (obj instanceof IFolder) {
            IFolder folder = (IFolder) obj;
            try {
                if (DQStructureManager.DBCONNECTION_FOLDER_PROPERTY.equals(folder
                        .getPersistentProperty(DQStructureManager.FOLDER_CLASSIFY_KEY))) {
                    CreateConnectionAction createConnectionAction = new CreateConnectionAction(folder);
                    menu.add(createConnectionAction);
                    // menu.insertBefore("group.edit", createConnectionAction);
                }
            } catch (CoreException e) {
                log.error(e, e);
            }
        }
    }
}
