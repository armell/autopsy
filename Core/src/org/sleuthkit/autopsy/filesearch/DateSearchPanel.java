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
package org.sleuthkit.autopsy.filesearch;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.optionalusertools.PickerUtilities;
import com.github.lgooddatepicker.components.DatePickerSettings;        
import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;

/**
 * Subpanel with controls for file data filtering.
 */
class DateSearchPanel extends javax.swing.JPanel {

    private final DatePickerSettings fromDateSettings = new DatePickerSettings();
    private final DatePickerSettings toDateSettings = new DatePickerSettings();
    DateFormat dateFormat;
    List<String> timeZones;

    DateSearchPanel(DateFormat dateFormat, List<String> timeZones) {
        this.dateFormat = dateFormat;
        this.timeZones = timeZones;

        initComponents();
        customizeComponents();
    }

    private void customizeComponents() {
        fromDateSettings.setFormatForDatesCommonEra(PickerUtilities.createFormatterFromPatternString("MM/dd/yyyy", fromDateSettings.getLocale()));
        toDateSettings.setFormatForDatesCommonEra(PickerUtilities.createFormatterFromPatternString("MM/dd/yyyy", toDateSettings.getLocale()));
        fromDateSettings.setAllowKeyboardEditing(false);
        toDateSettings.setAllowKeyboardEditing(false);
        
        ActionListener actList = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem jmi = (JMenuItem) e.getSource();
                /*
                 * Because there are two text fields, we have to determine which
                 * invoked the popupmenu
                 */
                JFormattedTextField jftf = (JFormattedTextField) ((JPopupMenu) jmi.getParent()).getInvoker();
                if (jmi.equals(cutMenuItem)) {
                    jftf.cut();
                } else if (jmi.equals(copyMenuItem)) {
                    jftf.copy();
                } else if (jmi.equals(pasteMenuItem)) {
                    jftf.paste();
                } else if (jmi.equals(selectAllMenuItem)) {
                    jftf.selectAll();
                }
            }
        };
        cutMenuItem.addActionListener(actList);
        copyMenuItem.addActionListener(actList);
        pasteMenuItem.addActionListener(actList);
        selectAllMenuItem.addActionListener(actList);
                
        this.setComponentsEnabled();
    }

    JCheckBox getAccessedCheckBox() {
        return accessedCheckBox;
    }

    JCheckBox getChangedCheckBox() {
        return changedCheckBox;
    }

    JCheckBox getCreatedCheckBox() {
        return createdCheckBox;
    }

    JCheckBox getDateCheckBox() {
        return dateCheckBox;
    }

    String getFromDate() {
        return fromDatePicker.getText();
    }

    String getToDate() {
        return toDatePicker.getText();
    }

    JCheckBox getModifiedCheckBox() {
        return modifiedCheckBox;
    }

    JComboBox<String> getTimeZoneComboBox() {
        return timeZoneComboBox;
    }

    void setTimeZones(List<String> newTimeZones) {
        this.timeZones = newTimeZones;
        this.timeZoneComboBox.removeAllItems();
        for (String tz : newTimeZones) {
            this.timeZoneComboBox.addItem(tz);
        }
    }

    private void setComponentsEnabled() {
        boolean enable = this.dateCheckBox.isSelected();
        this.fromDatePicker.setEnabled(enable);
        this.jLabel1.setEnabled(enable);
        this.toDatePicker.setEnabled(enable);
        this.jLabel2.setEnabled(enable);
        this.jLabel3.setEnabled(enable);
        this.jLabel4.setEnabled(enable);
        this.timeZoneComboBox.setEnabled(enable);
        this.modifiedCheckBox.setEnabled(enable);
        this.accessedCheckBox.setEnabled(enable);
        this.changedCheckBox.setEnabled(enable);
        this.createdCheckBox.setEnabled(enable);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rightClickMenu = new javax.swing.JPopupMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        selectAllMenuItem = new javax.swing.JMenuItem();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        dateCheckBox = new javax.swing.JCheckBox();
        timeZoneComboBox = new JComboBox<>(this.timeZones.toArray(new String[this.timeZones.size()]));
        timeZoneComboBox.setRenderer(new DateSearchFilter.ComboBoxRenderer());
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        modifiedCheckBox = new javax.swing.JCheckBox();
        changedCheckBox = new javax.swing.JCheckBox();
        accessedCheckBox = new javax.swing.JCheckBox();
        createdCheckBox = new javax.swing.JCheckBox();
        fromDatePicker = new DatePicker(fromDateSettings);
        toDatePicker = new DatePicker(toDateSettings);

        cutMenuItem.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.cutMenuItem.text")); // NOI18N
        rightClickMenu.add(cutMenuItem);

        copyMenuItem.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.copyMenuItem.text")); // NOI18N
        rightClickMenu.add(copyMenuItem);

        pasteMenuItem.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.pasteMenuItem.text")); // NOI18N
        rightClickMenu.add(pasteMenuItem);

        selectAllMenuItem.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.selectAllMenuItem.text")); // NOI18N
        rightClickMenu.add(selectAllMenuItem);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.jLabel1.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.jLabel4.text")); // NOI18N

        dateCheckBox.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.dateCheckBox.text")); // NOI18N
        dateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateCheckBoxActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel3.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.jLabel3.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
        jLabel2.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.jLabel2.text")); // NOI18N

        modifiedCheckBox.setSelected(true);
        modifiedCheckBox.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.modifiedCheckBox.text")); // NOI18N
        modifiedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifiedCheckBoxActionPerformed(evt);
            }
        });

        changedCheckBox.setSelected(true);
        changedCheckBox.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.changedCheckBox.text")); // NOI18N
        changedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changedCheckBoxActionPerformed(evt);
            }
        });

        accessedCheckBox.setSelected(true);
        accessedCheckBox.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.accessedCheckBox.text")); // NOI18N
        accessedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accessedCheckBoxActionPerformed(evt);
            }
        });

        createdCheckBox.setSelected(true);
        createdCheckBox.setText(org.openide.util.NbBundle.getMessage(DateSearchPanel.class, "DateSearchPanel.createdCheckBox.text")); // NOI18N
        createdCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createdCheckBoxActionPerformed(evt);
            }
        });

        fromDatePicker.setAutoscrolls(true);

        toDatePicker.setAutoscrolls(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 193, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(modifiedCheckBox)
                        .addGap(6, 6, 6)
                        .addComponent(accessedCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createdCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changedCheckBox)))
                .addGap(33, 33, 33))
            .addGroup(layout.createSequentialGroup()
                .addComponent(dateCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(fromDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(toDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dateCheckBox)
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(fromDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(toDatePicker, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(timeZoneComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modifiedCheckBox, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(accessedCheckBox)
                        .addComponent(createdCheckBox)
                        .addComponent(changedCheckBox)))
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void dateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateCheckBoxActionPerformed
        this.setComponentsEnabled();
        firePropertyChange(FileSearchPanel.EVENT.CHECKED.toString(), null, null);
    }//GEN-LAST:event_dateCheckBoxActionPerformed

    private void modifiedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifiedCheckBoxActionPerformed
        firePropertyChange(FileSearchPanel.EVENT.CHECKED.toString(), null, null);
    }//GEN-LAST:event_modifiedCheckBoxActionPerformed

    private void accessedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accessedCheckBoxActionPerformed
        firePropertyChange(FileSearchPanel.EVENT.CHECKED.toString(), null, null);
    }//GEN-LAST:event_accessedCheckBoxActionPerformed

    private void createdCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createdCheckBoxActionPerformed
        firePropertyChange(FileSearchPanel.EVENT.CHECKED.toString(), null, null);
    }//GEN-LAST:event_createdCheckBoxActionPerformed

    private void changedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changedCheckBoxActionPerformed
        firePropertyChange(FileSearchPanel.EVENT.CHECKED.toString(), null, null);
    }//GEN-LAST:event_changedCheckBoxActionPerformed
    
    boolean isValidSearch() {
        return this.accessedCheckBox.isSelected() ||
                this.changedCheckBox.isSelected() ||
                this.createdCheckBox.isSelected() ||
                this.modifiedCheckBox.isSelected();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox accessedCheckBox;
    private javax.swing.JCheckBox changedCheckBox;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JCheckBox createdCheckBox;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JCheckBox dateCheckBox;
    private com.github.lgooddatepicker.components.DatePicker fromDatePicker;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JCheckBox modifiedCheckBox;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JPopupMenu rightClickMenu;
    private javax.swing.JMenuItem selectAllMenuItem;
    private javax.swing.JComboBox<String> timeZoneComboBox;
    private com.github.lgooddatepicker.components.DatePicker toDatePicker;
    // End of variables declaration//GEN-END:variables

    void addDateChangeListener() {
        DateChangeListener dcl = (DateChangeEvent event) -> {
            firePropertyChange(FileSearchPanel.EVENT.CHECKED.toString(), null, null);            
        };
        
        fromDatePicker.addDateChangeListener(dcl);
        toDatePicker.addDateChangeListener(dcl);
    }

}

