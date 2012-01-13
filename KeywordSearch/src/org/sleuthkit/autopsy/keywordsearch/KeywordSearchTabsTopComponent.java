/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011 Basis Technology Corp.
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
package org.sleuthkit.autopsy.keywordsearch;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;

/**
 * Keyword Search explorer top component, container for specific Keyword Search tabs
 */
@ConvertAsProperties(dtd = "-//org.sleuthkit.autopsy.keywordsearch//KeywordSearchTabsTopComponent//EN",
autostore = false)
@TopComponent.Description(preferredID = "KeywordSearchTabsTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "org.sleuthkit.autopsy.keywordsearch.KeywordSearchTabsTopComponentTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_KeywordSearchTabsTopComponentAction",
preferredID = "KeywordSearchTabsTopComponent")
public final class KeywordSearchTabsTopComponent extends TopComponent implements KeywordSearchTopComponentInterface {

    private Logger logger = Logger.getLogger(KeywordSearchTabsTopComponent.class.getName());
    private PropertyChangeListener serverChangeListener;
    
    public enum TABS{Simple, List, Lists};

    public KeywordSearchTabsTopComponent() {
        initComponents();
        initTabs();
        setName(NbBundle.getMessage(KeywordSearchTabsTopComponent.class, "CTL_KeywordSearchTabsTopComponentTopComponent"));
        setToolTipText(NbBundle.getMessage(KeywordSearchTabsTopComponent.class, "HINT_KeywordSearchTabsTopComponentTopComponent"));


        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);

        //register with server Actions
        serverChangeListener = new KeywordSearchServerListener();
        KeywordSearch.getServer().addServerActionListener(serverChangeListener);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabs = new javax.swing.JTabbedPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables

    private void initTabs() {
        tabs.addTab(TABS.Simple.name(), null, new KeywordSearchSimpleTopComponent(), "Single keyword or regex search");
        tabs.addTab(TABS.List.name(), null, new KeywordSearchListTopComponent(), "Search for or load a saved list of keywords.");
        tabs.addTab(TABS.Lists.name(), null, new KeywordSearchListImportExportTopComponent(), "Manage (import, export, delete) lists of keywords.");
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }
    
    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // read your settings according to their version
    }

    @Override
    public boolean isMultiwordQuery() {
        KeywordSearchTopComponentInterface selected = (KeywordSearchTopComponentInterface) tabs.getSelectedComponent();
        if (selected == null) {
            return false;
        }
        return selected.isMultiwordQuery();
    }

    @Override
    public void addSearchButtonListener(ActionListener l) {
        final int tabsCount = tabs.getTabCount();
        for (int i = 0; i < tabsCount; ++i) {
            KeywordSearchTopComponentInterface ks = (KeywordSearchTopComponentInterface) tabs.getComponentAt(i);
            ks.addSearchButtonListener(l);
        }
    }

    @Override
    public String getQueryText() {
        KeywordSearchTopComponentInterface selected = (KeywordSearchTopComponentInterface) tabs.getSelectedComponent();
        if (selected == null) {
            return "";
        }
        return selected.getQueryText();
    }

    @Override
    public Map<String, Boolean> getQueryList() {
        KeywordSearchTopComponentInterface selected = (KeywordSearchTopComponentInterface) tabs.getSelectedComponent();
        if (selected == null) {
            return null;
        }
        return selected.getQueryList();
    }
    
    

    @Override
    public boolean isLuceneQuerySelected() {
        KeywordSearchTopComponentInterface selected = (KeywordSearchTopComponentInterface) tabs.getSelectedComponent();
        if (selected == null) {
            return false;
        }
        return selected.isLuceneQuerySelected();
    }

    @Override
    public boolean isRegexQuerySelected() {
        KeywordSearchTopComponentInterface selected = (KeywordSearchTopComponentInterface) tabs.getSelectedComponent();
        if (selected == null) {
            return false;
        }
        return selected.isRegexQuerySelected();
    }

    @Override
    public void setFilesIndexed(int filesIndexed) {
        final int tabsCount = tabs.getTabCount();
        for (int i = 0; i < tabsCount; ++i) {
            KeywordSearchTopComponentInterface ks = (KeywordSearchTopComponentInterface) tabs.getComponentAt(i);
            ks.setFilesIndexed(filesIndexed);
        }

    }

    class KeywordSearchServerListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String eventType = evt.getPropertyName();

            if (eventType.equals(Server.CORE_EVT)) {
                final Server.CORE_EVT_STATES state = (Server.CORE_EVT_STATES) evt.getNewValue();
                switch (state) {
                    case STARTED:
                        try {
                            final int numIndexedFiles = KeywordSearch.getServer().getCore().queryNumIndexedFiles();
                            KeywordSearch.changeSupport.firePropertyChange(KeywordSearch.NUM_FILES_CHANGE_EVT, null, new Integer(numIndexedFiles));
                            //setFilesIndexed(numIndexedFiles);
                        } catch (SolrServerException se) {
                            logger.log(Level.SEVERE, "Error executing Solr query, " + se.getMessage());
                        }
                        break;
                    case STOPPED:
                        break;
                    default:

                }
            }
        }
    }
}
