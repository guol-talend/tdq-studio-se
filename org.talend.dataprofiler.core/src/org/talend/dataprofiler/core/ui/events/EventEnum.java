// ============================================================================
//
// Copyright (C) 2006-2013 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.events;

/**
 * DOC yyin class global comment. Detailled comment
 */
public enum EventEnum {
    DQ_JRXML_RENAME,
    DQ_JRXML_MOVE,
    DQ_JRXML_DELETE,
    DQ_ANALYSIS_RUN_FROM_MENU,
    DQ_ANALYSIS_CHECK_BEFORERUN,
    // Update the software system when : new connection created; existing connection altered, exsiting connection
    // reloaded, connections checkedout from svn repository.
    DQ_SOFTWARESYSTEM_UPDATE,
    DQ_MATCH_ANALYSIS_AFTER_CREATE_CONNECTION,
    DQ_MATCH_ANALYSIS_REFRESH_WITH_RESULT;

}
