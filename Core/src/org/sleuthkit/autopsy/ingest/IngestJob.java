/*
 * Autopsy Forensic Browser
 *
 * Copyright 2014-2021 Basis Technology Corp.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import org.openide.util.NbBundle;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Content;

/**
 * Analyzes one or more data sources using a set of ingest modules specified via
 * ingest job settings.
 */
public final class IngestJob {

    /**
     * An ingest job can be cancelled for various reasons.
     */
    public enum CancellationReason {

        NOT_CANCELLED(NbBundle.getMessage(IngestJob.class, "IngestJob.cancelReason.notCancelled.text")),
        USER_CANCELLED(NbBundle.getMessage(IngestJob.class, "IngestJob.cancelReason.cancelledByUser.text")),
        INGEST_MODULES_STARTUP_FAILED(NbBundle.getMessage(IngestJob.class, "IngestJob.cancelReason.ingestModStartFail.text")),
        OUT_OF_DISK_SPACE(NbBundle.getMessage(IngestJob.class, "IngestJob.cancelReason.outOfDiskSpace.text")),
        SERVICES_DOWN(NbBundle.getMessage(IngestJob.class, "IngestJob.cancelReason.servicesDown.text")),
        CASE_CLOSED(NbBundle.getMessage(IngestJob.class, "IngestJob.cancelReason.caseClosed.text"));

        private final String displayName;

        private CancellationReason(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Ingest job modes.
     */
    enum Mode {
        BATCH,
        STREAMING
    }

    private static final Logger logger = Logger.getLogger(IngestJob.class.getName());
    private final static AtomicLong nextId = new AtomicLong(0L);
    private final long id;
    private final Content dataSource;
    private final List<AbstractFile> files = new ArrayList<>();
    private final Mode ingestMode;
    private final IngestJobSettings settings;
    private volatile IngestJobPipeline ingestJobPipeline;
    private volatile CancellationReason cancellationReason;

    /**
     * Constructs a batch mode ingest job that analyzes a data source using a
     * set of ingest modules specified via ingest job settings. Either all of
     * the files in the data source or a given subset of the files will be
     * analyzed.
     *
     * @param dataSource The data source to be analyzed.
     * @param files      A subset of the files from the data source.
     * @param settings   The ingest job settings.
     */
    IngestJob(Content dataSource, List<AbstractFile> files, IngestJobSettings settings) {
        this(dataSource, Mode.BATCH, settings);
        this.files.addAll(files);
    }

    /**
     * Constructs an ingest job that analyzes a data source using a set of
     * ingest modules specified via ingest job settings, possibly using an
     * ingest stream.
     *
     * @param settings The ingest job settings.
     */
    /**
     * Constructs an ingest job that analyzes a data source using a set of
     * ingest modules specified via ingest job settings, possibly using an
     * ingest stream.
     *
     * @param dataSource The data source to be analyzed.
     * @param ingestMode The ingest job mode.
     * @param settings   The ingest job settings.
     */
    IngestJob(Content dataSource, Mode ingestMode, IngestJobSettings settings) {
        this.id = IngestJob.nextId.getAndIncrement();
        this.dataSource = dataSource;
        this.settings = settings;
        this.ingestMode = ingestMode;
        cancellationReason = CancellationReason.NOT_CANCELLED;
    }

    /**
     * Gets the unique identifier assigned to this ingest job. For a multi-user
     * case, this ID is only unique on the host where the ingest job is running.
     *
     * @return The job identifier.
     */
    public long getId() {
        return this.id;
    }

    /**
     * Checks to see if this ingest job has at least one non-empty ingest module
     * pipeline.
     *
     * @return True or false.
     */
    boolean hasIngestPipeline() {
        return (!settings.getEnabledIngestModuleTemplates().isEmpty());
    }

    /**
     * Adds a set of files to this ingest job if it is running in streaming
     * ingest mode.
     *
     * @param fileObjIds The object IDs of the files.
     */
    void addStreamingIngestFiles(List<Long> fileObjIds) {
        if (ingestMode == Mode.STREAMING) {
            if (ingestJobPipeline != null) {
                ingestJobPipeline.addStreamedFiles(fileObjIds);
            } else {
                logger.log(Level.SEVERE, "Attempted to add streamed ingest files with no ingest pipeline");
            }
        } else {
            logger.log(Level.SEVERE, "Attempted to add streamed ingest files to batch ingest job");
        }
    }

    /**
     * Starts data source level analysis for this job if it is running in
     * streaming ingest mode.
     */
    void processStreamingIngestDataSource() {
        if (ingestMode == Mode.STREAMING) {
            if (ingestJobPipeline != null) {
                ingestJobPipeline.addStreamedDataSource();
            } else {
                logger.log(Level.SEVERE, "Attempted to start data source analaysis with no ingest pipeline");
            }
        } else {
            logger.log(Level.SEVERE, "Attempted to add streamed ingest files to batch ingest job");
        }
    }

    /**
     * Starts this ingest job by starting its ingest module pipelines and
     * scheduling the ingest tasks that make up the job.
     *
     * @return A collection of ingest module start up errors, empty on success.
     */
    synchronized List<IngestModuleError> start() throws InterruptedException {
        if (ingestJobPipeline != null) {
            logger.log(Level.SEVERE, "Attempt to start ingest job that has already been started");
            return Collections.emptyList();
        }

        /*
         * Try to start up the ingest pipeline.
         */
        if (files.isEmpty()) {
            ingestJobPipeline = new IngestJobPipeline(this, dataSource, settings);
        } else {
            ingestJobPipeline = new IngestJobPipeline(this, dataSource, files, settings);
        }
        List<IngestModuleError> errors = new ArrayList<>();
        errors.addAll(ingestJobPipeline.startUp());
        if (errors.isEmpty()) {
            IngestManager.getInstance().fireDataSourceAnalysisStarted(id, ingestJobPipeline.getDataSource());
        } else {
            cancel(CancellationReason.INGEST_MODULES_STARTUP_FAILED);
        }
        return errors;
    }

    /**
     * Get the ingest mode for this job (batch or streaming).
     *
     * @return the ingest mode.
     */
    Mode getIngestMode() {
        return ingestMode;
    }

    /**
     * Gets a snapshot of the progress of this ingest job.
     *
     * @return The snapshot, will be null if the job is not started yet.
     */
    public ProgressSnapshot getSnapshot() {
        return getSnapshot(true);
    }

    /**
     * Gets a snapshot of the progress of this ingest job.
     *
     * @param includeIngestTasksSnapshot Whether or not to include ingest task
     *                                   stats in the snapshot.
     *
     * @return The snapshot, will be null if the job is not started yet.
     */
    public ProgressSnapshot getSnapshot(boolean includeIngestTasksSnapshot) {
        ProgressSnapshot snapshot = null;
        if (ingestJobPipeline != null) {
            return new ProgressSnapshot(includeIngestTasksSnapshot);
        }
        return snapshot;
    }

    /**
     * Gets a snapshot of some basic diagnostic statistics for this ingest job.
     *
     * @return The snapshot, will be null if the job is not started yet.
     */
    Snapshot getDiagnosticStatsSnapshot() {
        Snapshot snapshot = null;
        if (ingestJobPipeline != null) {
            snapshot = ingestJobPipeline.getDiagnosticStatsSnapshot(true);
        }
        return snapshot;
    }

    /**
     * Requests cancellation of this ingest job, which means discarding
     * unfinished tasks and stopping the ingest module pipelines. Returns
     * immediately, but there may be a delay before all of the ingest modules in
     * the pipelines respond by stopping processing.
     *
     * @deprecated Use cancel(CancellationReason reason) instead
     */
    @Deprecated
    public void cancel() {
        cancel(CancellationReason.USER_CANCELLED);
    }

    /**
     * Requests cancellation of this ingest job, which means discarding
     * unfinished tasks and stopping the ingest module pipelines. Returns
     * immediately, but there may be a delay before all of the ingest modules in
     * the pipelines respond by stopping processing.
     *
     * @param reason The reason for cancellation.
     */
    public void cancel(CancellationReason reason) {
        cancellationReason = reason;
        /*
         * Cancels the running of the ingest module pipelines. This is done in a
         * separate thread to avoid a potential deadlock. The deadlock is
         * possible because this method can be called in a thread that acquires
         * the ingest manager's ingest jobs list lock and then tries to acquire
         * the ingest pipeline stage transition lock, while an ingest thread
         * that has acquired the stage transition lock is trying to acquire the
         * ingest manager's ingest jobs list lock.
         */
        new Thread(() -> {
            if (ingestJobPipeline != null) {
                ingestJobPipeline.cancel(reason);
            }
        }).start();
    }

    /**
     * Gets the reason this job was cancelled.
     *
     * @return The cancellation reason, may be not cancelled.
     */
    public CancellationReason getCancellationReason() {
        return this.cancellationReason;
    }

    /**
     * Queries whether or not cancellation of this ingest job has been
     * requested.
     *
     * @return True or false.
     */
    public boolean isCancelled() {
        return (CancellationReason.NOT_CANCELLED != this.cancellationReason);
    }

    /**
     * A snapshot of the progress of an ingest job.
     */
    public final class ProgressSnapshot {

        private final DataSourceProcessingSnapshot dataSourceProcessingSnapshot;
        private final boolean jobCancellationRequested;
        private final CancellationReason jobCancellationReason;

        /**
         * A snapshot of the state of the ingest pipeline of an ingest job. This
         * class is an artifact of a time when an ingest job could involve the
         * analysis of multiple data sources, each of which had its own
         * dedictaed ingest pipeline. It is currently nothing more than a
         * wrapper around a Snapshot object, another type of legacy partial
         * snaphot.
         */
        public final class DataSourceProcessingSnapshot {

            private final Snapshot snapshot;

            /**
             * Constructs a partial snapshot of the progress of an ingest job.
             * The partial snapshot is an artifact of a time when an ingest job
             * could involve the analysis of multiple data sources, each of
             * which had its own dedictaed ingest pipleine. It is currently
             * nothing more than a wrapper around a Snapshot object, another
             * type of legacy partial snaphot.
             */
            private DataSourceProcessingSnapshot(Snapshot snapshot) {
                this.snapshot = snapshot;
            }

            /**
             * Gets the name of the data source that is the subject of this
             * snapshot.
             *
             * @return A data source name string.
             */
            public String getDataSource() {
                return snapshot.getDataSource();
            }

            /**
             * Queries whether or not file level ingest was running at the time
             * the snapshot was taken.
             *
             * @return True or false.
             */
            public boolean fileIngestIsRunning() {
                return snapshot.getFileIngestIsRunning();
            }

            /**
             * Gets the time that file level ingest started.
             *
             * @return The start time, may be null.
             */
            public Date fileIngestStartTime() {
                return new Date(snapshot.getFileIngestStartTime().getTime());
            }

            /**
             * Gets the currently running data source level ingest module at the
             * time the snapshot was taken.
             *
             * @return The module.
             */
            DataSourceIngestPipeline.DataSourcePipelineModule getDataSourceLevelIngestModule() {
                return snapshot.getDataSourceLevelIngestModule();
            }

            /**
             * Indicates whether or not the processing of the data source that
             * is the subject of this snapshot was canceled.
             *
             * @return True or false.
             */
            public boolean isCancelled() {
                return snapshot.isCancelled();
            }

            /**
             * Gets the reason this job was cancelled.
             *
             * @return The cancellation reason, may be not cancelled.
             */
            public CancellationReason getCancellationReason() {
                return snapshot.getCancellationReason();
            }

            /**
             * Gets a list of the display names of any canceled data source
             * level ingest modules.
             *
             * @return A list of canceled data source level ingest module
             *         display names, possibly empty.
             */
            public List<String> getCancelledDataSourceIngestModules() {
                return snapshot.getCancelledDataSourceIngestModules();
            }

        }

        /**
         * Constructs a snapshot of the progress of an ingest job.
         *
         * @param includeIngestTasksSnapshot Whether or not to include ingest
         *                                   task stats in the snapshot.
         */
        private ProgressSnapshot(boolean includeIngestTasksSnapshot) {
            /*
             * Note that the getSnapshot() will not construct a ProgressSnapshot
             * if ingestJobPipeline is null.
             */
            Snapshot snapshot = ingestJobPipeline.getDiagnosticStatsSnapshot(includeIngestTasksSnapshot);
            dataSourceProcessingSnapshot = new DataSourceProcessingSnapshot(snapshot);
            jobCancellationRequested = IngestJob.this.isCancelled();
            jobCancellationReason = IngestJob.this.getCancellationReason();
        }

        /**
         * Gets a handle to the currently running data source level ingest
         * module at the time the snapshot was taken. This handle can be used to
         * cancel the module, if it is still running.
         *
         * @return The handle, may be null.
         */
        public DataSourceIngestModuleHandle runningDataSourceIngestModule() {
            return new DataSourceIngestModuleHandle(ingestJobPipeline, dataSourceProcessingSnapshot.getDataSourceLevelIngestModule());
        }

        /**
         * Queries whether or not file level ingest was running at the time the
         * snapshot was taken.
         *
         * @return True or false.
         */
        public boolean fileIngestIsRunning() {
            return dataSourceProcessingSnapshot.fileIngestIsRunning();
        }

        /**
         * Gets the time that file level ingest started.
         *
         * @return The start time, may be null.
         */
        public Date fileIngestStartTime() {
            return new Date(dataSourceProcessingSnapshot.fileIngestStartTime().getTime());
        }

        /**
         * Queries whether or not an ingest job cancellation request had been
         * issued at the time the snapshot was taken.
         *
         * @return True or false.
         */
        public boolean isCancelled() {
            return jobCancellationRequested;
        }

        /**
         * Gets the reason the ingest job was cancelled.
         *
         * @return The cancellation reason, which may indicate that the job had
         *         not been cancelled at the time the snapshot was taken.
         */
        public CancellationReason getCancellationReason() {
            return jobCancellationReason;
        }

        /**
         * Gets a snapshot of the state of the ingest pipeline of an ingest job.
         *
         * @return The ingest pipeline state snapshot.
         */
        public DataSourceProcessingSnapshot getDataSourceProcessingSnapshot() {
            return dataSourceProcessingSnapshot;
        }

    }

    /**
     * A handle to a data source level ingest module that can be used to get
     * basic information about the module and to request cancellation of the
     * module.
     */
    public static class DataSourceIngestModuleHandle {

        private final IngestJobPipeline ingestJobPipeline;
        private final DataSourceIngestPipeline.DataSourcePipelineModule module;
        private final boolean cancelled;

        /**
         * Constructs a handle to a data source level ingest module that can be
         * used to get basic information about the module and to request
         * cancellation of the module.
         *
         * @param ingestJobPipeline The ingestJobPipeline that owns the data
         *                          source level ingest module.
         * @param module            The data source level ingest module.
         */
        private DataSourceIngestModuleHandle(IngestJobPipeline ingestJobPipeline, DataSourceIngestPipeline.DataSourcePipelineModule module) {
            this.ingestJobPipeline = ingestJobPipeline;
            this.module = module;
            this.cancelled = ingestJobPipeline.currentDataSourceIngestModuleIsCancelled();
        }

        /**
         * Gets the display name of the data source level ingest module
         * associated with this handle.
         *
         * @return The display name.
         */
        public String displayName() {
            return this.module.getDisplayName();
        }

        /**
         * Gets the time the data source level ingest module associated with
         * this handle began processing.
         *
         * @return The module processing start time.
         */
        public Date startTime() {
            return this.module.getProcessingStartTime();
        }

        /**
         * Queries whether or not cancellation of the data source level ingest
         * module associated with this handle has been requested.
         *
         * @return True or false.
         */
        public boolean isCancelled() {
            return this.cancelled;
        }

        /**
         * Requests cancellation of the ingest module associated with this
         * handle. Returns immediately, but there may be a delay before the
         * ingest module responds by stopping processing.
         */
        public void cancel() {
            if (this.ingestJobPipeline.getCurrentDataSourceIngestModule() == this.module) {
                this.ingestJobPipeline.cancelCurrentDataSourceIngestModule();
            }
        }

    }

}
