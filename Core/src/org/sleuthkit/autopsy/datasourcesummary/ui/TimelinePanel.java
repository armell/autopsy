/*
 * Autopsy Forensic Browser
 *
 * Copyright 2020 Basis Technology Corp.
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
package org.sleuthkit.autopsy.datasourcesummary.ui;

import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.collections.CollectionUtils;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.datasourcesummary.datamodel.TimelineSummary;
import org.sleuthkit.autopsy.datasourcesummary.datamodel.TimelineSummary.DailyActivityAmount;
import org.sleuthkit.autopsy.datasourcesummary.datamodel.TimelineSummary.TimelineSummaryData;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.BarChartPanel;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.BarChartPanel.BarChartItem;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.BarChartPanel.BarChartSeries;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.DataFetchResult;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.DataFetchWorker;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.DataFetchWorker.DataFetchComponents;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.IngestRunningLabel;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.LoadableComponent;
import org.sleuthkit.autopsy.datasourcesummary.uiutils.LoadableLabel;
import org.sleuthkit.datamodel.DataSource;

/**
 * A tab shown in data source summary displaying information about a data
 * source's timeline events.
 */
@Messages({
    "TimelinePanel_earliestLabel_title=Earliest",
    "TimelinePanel_latestLabel_title=Latest",
    "TimlinePanel_last30DaysChart_title=Last 30 Days"
})
public class TimelinePanel extends BaseDataSourceSummaryPanel {

    private static final long serialVersionUID = 1L;
    private static final DateFormat EARLIEST_LATEST_FORMAT = getUtcFormat("MMM d, yyyy");
    private static final DateFormat CHART_FORMAT = getUtcFormat("MMM d");
    private static final Color CHART_COLOR = Color.BLUE;
    private static final int MOST_RECENT_DAYS_COUNT = 30;
    
    /**
     * Creates a DateFormat formatter that uses UTC for time zone.
     *
     * @param formatString The date format string.
     * @return The data format.
     */
    private static DateFormat getUtcFormat(String formatString) {
        return new SimpleDateFormat(formatString, Locale.getDefault());
    }

    // components displayed in the tab
    private final IngestRunningLabel ingestRunningLabel = new IngestRunningLabel();
    private final LoadableLabel earliestLabel = new LoadableLabel(Bundle.TimelinePanel_earliestLabel_title());
    private final LoadableLabel latestLabel = new LoadableLabel(Bundle.TimelinePanel_latestLabel_title());
    private final BarChartPanel last30DaysChart = new BarChartPanel(Bundle.TimlinePanel_last30DaysChart_title(), "", "");

    // all loadable components on this tab
    private final List<LoadableComponent<?>> loadableComponents = Arrays.asList(earliestLabel, latestLabel, last30DaysChart);

    // actions to load data for this tab
    private final List<DataFetchComponents<DataSource, ?>> dataFetchComponents;

    public TimelinePanel() {
        this(new TimelineSummary());
    }

    /**
     * Creates new form PastCasesPanel
     */
    public TimelinePanel(TimelineSummary timelineData) {
        // set up data acquisition methods
        dataFetchComponents = Arrays.asList(
                new DataFetchWorker.DataFetchComponents<>(
                        (dataSource) -> timelineData.getData(dataSource, MOST_RECENT_DAYS_COUNT),
                        (result) -> handleResult(result))
        );

        initComponents();
    }

    /**
     * Formats a date using a DateFormat. In the event that the date is null,
     * returns a null string.
     *
     * @param date The date to format.
     * @param formatter The DateFormat to use to format the date.
     * @return The formatted string generated from the formatter or null if the
     * date is null.
     */
    private static String formatDate(Date date, DateFormat formatter) {
        return date == null ? null : formatter.format(date);
    }

    /**
     * Converts DailyActivityAmount data retrieved from TimelineSummary into
     * data to be displayed as a bar chart.
     *
     * @param recentDaysActivity The data retrieved from TimelineSummary.
     * @return The data to be displayed in the BarChart.
     */
    private BarChartSeries parseChartData(List<DailyActivityAmount> recentDaysActivity) {
        // if no data, return null indicating no result.
        if (CollectionUtils.isEmpty(recentDaysActivity)) {
            return null;
        }

        // Create a bar chart item for each recent days activity item
        List<BarChartItem> items = new ArrayList<>();
        for (int i = 0; i < recentDaysActivity.size(); i++) {
            DailyActivityAmount curItem = recentDaysActivity.get(i);
            long amount = curItem.getArtifactActivityCount() * 1000 + curItem.getFileActivityCount();

            if (i == 0 || i == recentDaysActivity.size() - 1) {
                String formattedDate = formatDate(curItem.getDay(), CHART_FORMAT);
                items.add(new BarChartItem(formattedDate, amount));
            } else {
                items.add(new BarChartItem("", amount));
            }
        }

        return new BarChartSeries(CHART_COLOR, items);
    }

    /**
     * Handles displaying the result for each displayable item in the
     * TimelinePanel by breaking the TimelineSummaryData result into its
     * constituent parts and then sending each data item to the pertinent
     * component.
     *
     * @param result The result to be displayed on this tab.
     */
    private void handleResult(DataFetchResult<TimelineSummaryData> result) {
        earliestLabel.showDataFetchResult(DataFetchResult.getSubResult(result, r -> formatDate(r.getMinDate(), EARLIEST_LATEST_FORMAT)));
        latestLabel.showDataFetchResult(DataFetchResult.getSubResult(result, r -> formatDate(r.getMaxDate(), EARLIEST_LATEST_FORMAT)));
        last30DaysChart.showDataFetchResult(DataFetchResult.getSubResult(result, r -> parseChartData(r.getMostRecentDaysActivity())));
    }

    @Override
    protected void fetchInformation(DataSource dataSource) {
        fetchInformation(dataFetchComponents, dataSource);
    }

    @Override
    protected void onNewDataSource(DataSource dataSource) {
        onNewDataSource(dataFetchComponents, loadableComponents, dataSource);
    }

    @Override
    public void close() {
        ingestRunningLabel.unregister();
        super.close();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JScrollPane mainScrollPane = new javax.swing.JScrollPane();
        javax.swing.JPanel mainContentPanel = new javax.swing.JPanel();
        javax.swing.JPanel ingestRunningPanel = ingestRunningLabel;
        javax.swing.JLabel activityRangeLabel = new javax.swing.JLabel();
        javax.swing.Box.Filler filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 2), new java.awt.Dimension(0, 2), new java.awt.Dimension(0, 2));
        javax.swing.JPanel earliestLabelPanel = earliestLabel;
        javax.swing.JPanel latestLabelPanel = latestLabel;
        javax.swing.Box.Filler filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20), new java.awt.Dimension(0, 20));
        javax.swing.JPanel sameIdPanel = last30DaysChart;
        javax.swing.Box.Filler filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));

        mainContentPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainContentPanel.setLayout(new javax.swing.BoxLayout(mainContentPanel, javax.swing.BoxLayout.PAGE_AXIS));

        ingestRunningPanel.setAlignmentX(0.0F);
        ingestRunningPanel.setMaximumSize(new java.awt.Dimension(32767, 25));
        ingestRunningPanel.setMinimumSize(new java.awt.Dimension(10, 25));
        ingestRunningPanel.setPreferredSize(new java.awt.Dimension(10, 25));
        mainContentPanel.add(ingestRunningPanel);

        activityRangeLabel.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(activityRangeLabel, org.openide.util.NbBundle.getMessage(TimelinePanel.class, "TimelinePanel.activityRangeLabel.text")); // NOI18N
        mainContentPanel.add(activityRangeLabel);
        activityRangeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TimelinePanel.class, "PastCasesPanel.notableFileLabel.text")); // NOI18N

        filler1.setAlignmentX(0.0F);
        mainContentPanel.add(filler1);

        earliestLabelPanel.setAlignmentX(0.0F);
        earliestLabelPanel.setMaximumSize(new java.awt.Dimension(32767, 20));
        earliestLabelPanel.setMinimumSize(new java.awt.Dimension(100, 20));
        earliestLabelPanel.setPreferredSize(new java.awt.Dimension(100, 20));
        mainContentPanel.add(earliestLabelPanel);

        latestLabelPanel.setAlignmentX(0.0F);
        latestLabelPanel.setMaximumSize(new java.awt.Dimension(32767, 20));
        latestLabelPanel.setMinimumSize(new java.awt.Dimension(100, 20));
        latestLabelPanel.setPreferredSize(new java.awt.Dimension(100, 20));
        mainContentPanel.add(latestLabelPanel);

        filler2.setAlignmentX(0.0F);
        mainContentPanel.add(filler2);

        sameIdPanel.setAlignmentX(0.0F);
        sameIdPanel.setMaximumSize(new java.awt.Dimension(600, 300));
        sameIdPanel.setMinimumSize(new java.awt.Dimension(600, 300));
        sameIdPanel.setPreferredSize(new java.awt.Dimension(600, 300));
        sameIdPanel.setVerifyInputWhenFocusTarget(false);
        mainContentPanel.add(sameIdPanel);

        filler5.setAlignmentX(0.0F);
        mainContentPanel.add(filler5);

        mainScrollPane.setViewportView(mainContentPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
