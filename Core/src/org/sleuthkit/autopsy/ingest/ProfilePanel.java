/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011-2021 Basis Technology Corp.
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
package org.sleuthkit.autopsy.ingest;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.ingest.IngestProfiles.IngestProfile;

/**
 * Panel to display options for profile creation and editing.
 */
@SuppressWarnings("PMD.SingularField") // UI widgets cause lots of false positives
class ProfilePanel extends IngestModuleGlobalSettingsPanel {

    @NbBundle.Messages({"ProfilePanel.title.text=Profile",
        "ProfilePanel.profileDescLabel.text=Description:",
        "ProfilePanel.profileNameLabel.text=Profile Name:",
        "ProfilePanel.newProfileText=NewEmptyProfile",
        "ProfilePanel.messages.profilesMustBeNamed=Ingest profile must be named.",
        "ProfilePanel.messages.profileNameContainsIllegalCharacter=Profile name contains an illegal character. Only \nletters, digits, and underscore characters are allowed."})

    private final IngestJobSettingsPanel ingestSettingsPanel;
    private final IngestJobSettings settings;
    private IngestProfile profile;
    private final static String NEW_PROFILE_NAME = NbBundle.getMessage(ProfilePanel.class, "ProfilePanel.newProfileText");

    /**
     * Creates new form ProfilePanel
     */
    ProfilePanel() {
        initComponents();
        setName(org.openide.util.NbBundle.getMessage(ProfilePanel.class, "ProfilePanel.title.text"));
        settings = new IngestJobSettings(IngestProfiles.getExecutionContext(NEW_PROFILE_NAME));
        ingestSettingsPanel = new IngestJobSettingsPanel(settings);
        ingestSettingsPanel.setPastJobsButtonVisible(false);
        jPanel1.add(ingestSettingsPanel, 0);

    }

    ProfilePanel(IngestProfile selectedProfile) {
        initComponents();
        setName(org.openide.util.NbBundle.getMessage(ProfilePanel.class, "ProfilePanel.title.text"));
        profile = selectedProfile;
        profileDescArea.setText(profile.getDescription());
        profileNameField.setText(profile.getName());
        settings = new IngestJobSettings(IngestProfiles.getExecutionContext(selectedProfile.getName()));
        ingestSettingsPanel = new IngestJobSettingsPanel(settings);
        ingestSettingsPanel.setPastJobsButtonVisible(false);
        jPanel1.add(ingestSettingsPanel, 0);
    }

    /**
     * Get the name of the profile.
     *
     * The name will not contain any trailing or leading spaces.
     *
     * @return
     */
    String getProfileName() {
        return profileNameField.getText().trim();
    }

    String getProfileDesc() {
        return profileDescArea.getText();
    }

    IngestJobSettings getSettings() {
        return ingestSettingsPanel.getSettings();
    }
    
    String getIngestProfileName() {
        if (profile != null) {
            return profile.getName();
        } else {
            return NEW_PROFILE_NAME;
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        profileNameField = new javax.swing.JTextField();
        profileDescLabel = new javax.swing.JLabel();
        profileDescPane = new javax.swing.JScrollPane();
        profileDescArea = new javax.swing.JTextArea();
        profileNameLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        jScrollPane1.setPreferredSize(new java.awt.Dimension(650, 323));

        org.openide.awt.Mnemonics.setLocalizedText(profileDescLabel, org.openide.util.NbBundle.getMessage(ProfilePanel.class, "ProfilePanel.profileDescLabel.text")); // NOI18N

        profileDescArea.setColumns(20);
        profileDescArea.setLineWrap(true);
        profileDescArea.setRows(8);
        profileDescArea.setWrapStyleWord(true);
        profileDescArea.setMinimumSize(new java.awt.Dimension(164, 44));
        profileDescArea.setName(""); // NOI18N
        profileDescPane.setViewportView(profileDescArea);

        org.openide.awt.Mnemonics.setLocalizedText(profileNameLabel, org.openide.util.NbBundle.getMessage(ProfilePanel.class, "ProfilePanel.profileNameLabel.text")); // NOI18N

        jPanel1.setMinimumSize(new java.awt.Dimension(625, 450));
        jPanel1.setPreferredSize(new java.awt.Dimension(625, 450));
        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(profileDescLabel)
                    .addComponent(profileNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(profileDescPane)
                    .addComponent(profileNameField))
                .addGap(5, 5, 5))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(profileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(profileNameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(profileDescLabel)
                    .addComponent(profileDescPane, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(2, 2, 2))
        );

        jScrollPane1.setViewportView(jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
  @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        ingestSettingsPanel.removePropertyChangeListener(l);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        super.addPropertyChangeListener(l);
        /*
         * There is at least one look and feel library that follows the bad
         * practice of calling overrideable methods in a constructor, e.g.:
         *
         * at
         * javax.swing.plaf.synth.SynthPanelUI.installListeners(SynthPanelUI.java:83)
         * at
         * javax.swing.plaf.synth.SynthPanelUI.installUI(SynthPanelUI.java:63)
         * at javax.swing.JComponent.setUI(JComponent.java:666) at
         * javax.swing.JPanel.setUI(JPanel.java:153) at
         * javax.swing.JPanel.updateUI(JPanel.java:126) at
         * javax.swing.JPanel.<init>(JPanel.java:86) at
         * javax.swing.JPanel.<init>(JPanel.java:109) at
         * javax.swing.JPanel.<init>(JPanel.java:117)
         *
         * When this happens, the following child components of this JPanel
         * subclass have not been constructed yet, since this panel's
         * constructor has not been called yet.
         */
        if (null != ingestSettingsPanel) {
            ingestSettingsPanel.addPropertyChangeListener(l);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea profileDescArea;
    private javax.swing.JLabel profileDescLabel;
    private javax.swing.JScrollPane profileDescPane;
    private javax.swing.JTextField profileNameField;
    private javax.swing.JLabel profileNameLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Save a new or edited profile.
     */
    @Override
    public void saveSettings() {
        if (profile == null) {
            IngestProfile.renameProfile(settings.getExecutionContext(), getProfileName());
        } else if (!profile.getName().equals(getProfileName())) {
            IngestProfile.renameProfile(profile.getName(), getProfileName());
        }
        profile = new IngestProfile(getProfileName(), profileDescArea.getText(), ingestSettingsPanel.getSettings().getFileFilter().getName());
        IngestProfile.saveProfile(profile);
        ingestSettingsPanel.getSettings().saveAs(IngestProfiles.getExecutionContext(getProfileName()));
    }

    /**
     * Save a new or edited profile.
     */
    boolean store() {
        if (!isValidDefinition(false)) {
            return false;
        }
        saveSettings();
        return true;
    }

    void load() {
    }

    /**
     * Checks that information entered constitutes a valid ingest profile.
     * 
     * @param dispayWarnings boolean flag whether to display warnings if an error occurred.
     *
     * @return true for valid, false for invalid.
     */
    boolean isValidDefinition(boolean dispayWarnings) {
        String profileName = getProfileName();
        if (profileName.isEmpty()) {
            if (dispayWarnings) {
                NotifyDescriptor notifyDesc = new NotifyDescriptor.Message(
                        NbBundle.getMessage(ProfilePanel.class, "ProfilePanel.messages.profilesMustBeNamed"),
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(notifyDesc);
            }
            return false;
        }

        // check if the name contains illegal characters
        String sanitizedName = profileName.replaceAll("[^A-Za-z0-9_]", "");
        if (!(profileName.equals(sanitizedName))) {
            if (dispayWarnings) {
                NotifyDescriptor notifyDesc = new NotifyDescriptor.Message(
                        NbBundle.getMessage(ProfilePanel.class, "ProfilePanel.messages.profileNameContainsIllegalCharacter"),
                        NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(notifyDesc);
            }
            return false;
        }
        return true;
    }
}
