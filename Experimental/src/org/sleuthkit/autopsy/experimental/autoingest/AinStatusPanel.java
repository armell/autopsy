/*
 * Autopsy Forensic Browser
 *
 * Copyright 2018 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.experimental.autoingest;

import java.awt.Dimension;
import java.beans.PropertyVetoException;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.sleuthkit.autopsy.experimental.autoingest.AutoIngestJobsNode.JobNode;

/**
 * A panel which displays an outline view with all auto ingest nodes and their
 * status.
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
final class AinStatusPanel extends javax.swing.JPanel implements ExplorerManager.Provider {

    private static final long serialVersionUID = 1L;
    private final org.openide.explorer.view.OutlineView outlineView;
    private final Outline outline;
    private ExplorerManager explorerManager;

    /**
     * Creates a new AinStatusPanel
     */
    AinStatusPanel() {
        initComponents();
        outlineView = new org.openide.explorer.view.OutlineView();
        outline = outlineView.getOutline();
        customize();
    }

    /**
     * Set up the AinStatusPanel's so that its outlineView is displaying the
     * host name and the node status.
     */
    void customize() {
        ((DefaultOutlineModel) outline.getOutlineModel()).setNodesColumnLabel(Bundle.AinStatusNode_hostName_title());
        outline.setRowSelectionAllowed(false); //rows will be made selectable after table has been populated
        outline.setFocusable(false);  //table will be made focusable after table has been populated
        if (null == explorerManager) {
            explorerManager = new ExplorerManager();
        }
        outlineView.setPropertyColumns(
                Bundle.AinStatusNode_status_title(), Bundle.AinStatusNode_status_title());
        outline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outline.setRootVisible(false);
        add(outlineView, java.awt.BorderLayout.CENTER);
    }

    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
        outlineView.setMaximumSize(new Dimension(400, 100));
        outline.setPreferredScrollableViewportSize(new Dimension(400, 100));
    }

    /**
     * Add a list selection listener to the selection model of the outline being
     * used in this panel.
     *
     * @param listener the ListSelectionListener to add
     */
    void addListSelectionListener(ListSelectionListener listener) {
        outline.getSelectionModel().addListSelectionListener(listener);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    /**
     * Update the contents of this AinStatusPanel while retaining currently
     * selected node.
     *
     * @param monitor - the AutoIngestMonitor which will provide the new
     *                contents
     */
    void refresh(AutoIngestMonitor monitor) {
        outline.setRowSelectionAllowed(false);
        Node[] selectedNodes = explorerManager.getSelectedNodes();
        AinStatusNode ainStatusNode = new AinStatusNode(monitor);
        explorerManager.setRootContext(ainStatusNode);
        outline.setRowSelectionAllowed(true);
        if (selectedNodes.length > 0 && ainStatusNode.getChildren().findChild(selectedNodes[0].getName()) != null && outline.isFocusable()) {  //don't allow saved selections of empty nodes to be restored
            try {
                explorerManager.setSelectedNodes(new Node[]{ainStatusNode.getChildren().findChild(selectedNodes[0].getName())});
            } catch (PropertyVetoException ignore) {
                //Unable to select previously selected node
            }
        }
        outline.setFocusable(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
