/*
 *
 * Autopsy Forensic Browser
 *
 * Copyright 2020 Basis Technology Corp.
 *
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
package org.sleuthkit.autopsy.recentactivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle.Messages;
import org.sleuthkit.autopsy.casemodule.Case;
import org.sleuthkit.autopsy.casemodule.services.FileManager;
import org.sleuthkit.autopsy.coreutils.ExecUtil;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.coreutils.PlatformUtil;
import org.sleuthkit.autopsy.coreutils.SQLiteDBConnect;
import org.sleuthkit.autopsy.datamodel.ContentUtils;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProcessTerminator;
import org.sleuthkit.autopsy.ingest.DataSourceIngestModuleProgress;
import org.sleuthkit.autopsy.ingest.IngestJobContext;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.Blackboard;
import org.sleuthkit.datamodel.BlackboardArtifact;
import static org.sleuthkit.datamodel.BlackboardArtifact.ARTIFACT_TYPE.TSK_ASSOCIATED_OBJECT;
import org.sleuthkit.datamodel.BlackboardAttribute;
import static org.sleuthkit.datamodel.BlackboardAttribute.ATTRIBUTE_TYPE.TSK_ASSOCIATED_ARTIFACT;
import org.sleuthkit.datamodel.Content;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * Extract the EVTX Event logs to individual SQLite databases to be used by a
 * content viewer
 */
final class ExtractPrefetch extends Extract {

    private static final Logger logger = Logger.getLogger(ExtractPrefetch.class.getName());

    private IngestJobContext context;


    private static final String MODULE_NAME = "extractPREFETCH"; //NON-NLS

    private static final String PREFETCH_TSK_COMMENT = "Prefetch File";
    private static final String PREFETCH_FILE_LOCATION = "/Windows/Prefetch";
    private static final String PREFETCH_TOOL_FOLDER = "markmckinnon"; //NON-NLS
    private static final String PREFETCH_TOOL_NAME_WINDOWS_64 = "parse_prefetch_64.exe"; //NON-NLS
    private static final String PREFETCH_TOOL_NAME_WINDOWS_32 = "parse_prefetch_32.exe"; //NON-NLS
    private static final String PREFETCH_TOOL_NAME_MACOS = "parse_prefetch_macos"; //NON-NLS
    private static final String PREFETCH_TOOL_NAME_LINUX = "parse_prefetch_linux"; //NON-NLS
    private static final String PREFETCH_OUTPUT_FILE_NAME = "Output.txt"; //NON-NLS
    private static final String PREFETCH_ERROR_FILE_NAME = "Error.txt"; //NON-NLS
    private static final String PREFETCH_PARSER_DB_FILE = "Autopsy_PF_DB.db3"; //NON-NLS
    private static final String PREFETCH_DIR_NAME = "prefetch"; //NON-NLS

    @Messages({
        "ExtractPrefetch_module_name=Windows Prefetch Extractor"
    })
    ExtractPrefetch() {
        this.moduleName = Bundle.ExtractPrefetch_module_name();
    }

    @Override
    void process(Content dataSource, IngestJobContext context, DataSourceIngestModuleProgress progressBar) {

        this.context = context;

        String modOutPath = Case.getCurrentCase().getModuleDirectory() + File.separator + PREFETCH_DIR_NAME;
        File dir = new File(modOutPath);
        if (dir.exists() == false) {
            dir.mkdirs();
        }
        
        extractPrefetchFiles(dataSource);
        
        final String prefetchDumper = getPathForPrefetchDumper();
        if (prefetchDumper == null) {
            logger.log(Level.SEVERE, "Error finding parse_prefetch program"); //NON-NLS
            return; //If we cannot find the parse_prefetch program we cannot proceed
        }

        if (context.dataSourceIngestIsCancelled()) {
            return;
        }

        String modOutFile = modOutPath + File.separator + PREFETCH_PARSER_DB_FILE;
        try {
            String tempDirPath = RAImageIngestModule.getRATempPath(Case.getCurrentCase(), PREFETCH_DIR_NAME );
            parsePrefetchFiles(prefetchDumper, tempDirPath, modOutFile, modOutPath);
            createAppExecArtifacts(modOutFile, dataSource);
        } catch (IOException ex) {
            logger.log(Level.WARNING, "Error runing parse_prefetch or creating artifacts.", ex); //NON-NLS             
        }
    }

    void extractPrefetchFiles(Content dataSource) {
        List<AbstractFile> pFiles;

        FileManager fileManager = Case.getCurrentCase().getServices().getFileManager();
        String tempDirPath = RAImageIngestModule.getRATempPath(Case.getCurrentCase(), "prefetch"); //NON-NLS

        try {
            pFiles = fileManager.findFiles(dataSource, "%.pf"); //NON-NLS            
        } catch (TskCoreException ex) {
            logger.log(Level.WARNING, "Unable to find prefetch files.", ex); //NON-NLS
            return;  // No need to continue
        }

        AbstractFile prefetchAbstractFile = null;

        for (AbstractFile pFile : pFiles) {

            if (context.dataSourceIngestIsCancelled()) {
                return;
            }

            String prefetchFile = tempDirPath + File.separator + pFile.getName();
            if (pFile.getParentPath().contains(PREFETCH_FILE_LOCATION)) {
                try {
                    ContentUtils.writeToFile(pFile, new File(prefetchFile));
                } catch (IOException ex) {
                    logger.log(Level.WARNING, String.format("Unable to write %s to temp directory. File name: %s", pFile.getName(), prefetchFile), ex); //NON-NLS
                }
            }
        }

    }
    
    /**
     * Run the export prefetchdb program against the prefetchdb.dat file
     *
     * @param prefetchExePath
     * @param tempDirPath
     * @param tempOutPath
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    void parsePrefetchFiles(String prefetchExePath, String prefetchFile, String tempOutFile, String tempOutPath) throws FileNotFoundException, IOException {
        final Path outputFilePath = Paths.get(tempOutPath, PREFETCH_OUTPUT_FILE_NAME);
        final Path errFilePath = Paths.get(tempOutPath, PREFETCH_ERROR_FILE_NAME);

        List<String> commandLine = new ArrayList<>();
        commandLine.add(prefetchExePath);
        commandLine.add(prefetchFile);  //NON-NLS
        commandLine.add(tempOutFile);

        ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.redirectOutput(outputFilePath.toFile());
        processBuilder.redirectError(errFilePath.toFile());

        ExecUtil.execute(processBuilder, new DataSourceIngestModuleProcessTerminator(context));
    }

    private String getPathForPrefetchDumper() {
        Path path = null;
        if (PlatformUtil.isWindowsOS()) {
            if (PlatformUtil.is64BitOS()) {
                path = Paths.get(PREFETCH_TOOL_FOLDER, PREFETCH_TOOL_NAME_WINDOWS_64);
            } else {
                path = Paths.get(PREFETCH_TOOL_FOLDER, PREFETCH_TOOL_NAME_WINDOWS_32);            
            }
        } else {
            if ("Linux".equals(PlatformUtil.getOSName())) {
                path = Paths.get(PREFETCH_TOOL_FOLDER, PREFETCH_TOOL_NAME_LINUX);
            } else {
                path = Paths.get(PREFETCH_TOOL_FOLDER, PREFETCH_TOOL_NAME_MACOS);                
            }
        }
        File prefetchToolFile = InstalledFileLocator.getDefault().locate(path.toString(),
                ExtractPrefetch.class.getPackage().getName(), false);
        if (prefetchToolFile != null) {
            return prefetchToolFile.getAbsolutePath();
        }

        return null;

    }

    private void createAppExecArtifacts(String prefetchDb, Content dataSource) {
        List<BlackboardArtifact> bba = new ArrayList<>();

        String sqlStatement = "SELECT prefetch_File_Name, actual_File_Name, file_path, Number_time_file_run, Embeded_date_Time_Unix_1, " +
                              " Embeded_date_Time_Unix_2, Embeded_date_Time_Unix_3, Embeded_date_Time_Unix_4, Embeded_date_Time_Unix_5," +
                              " Embeded_date_Time_Unix_6, Embeded_date_Time_Unix_7, Embeded_date_Time_Unix_8 " +
                              " FROM prefetch_file_info;"; //NON-NLS

        try (SQLiteDBConnect tempdbconnect = new SQLiteDBConnect("org.sqlite.JDBC", "jdbc:sqlite:" + prefetchDb); //NON-NLS
                ResultSet resultSet = tempdbconnect.executeQry(sqlStatement)) {

            while (resultSet.next()) {

                if (context.dataSourceIngestIsCancelled()) {
                    logger.log(Level.INFO, "Cancelled Prefetch Artifact Creation."); //NON-NLS
                    return;
                }

                String prefetchFileName = resultSet.getString("prefetch_File_Name");
                String applicationName = resultSet.getString("actual_File_Name"); //NON-NLS
                List<Long> executionTimes = new ArrayList<>();
                executionTimes.add(Long.valueOf(resultSet.getInt("Embeded_date_Time_Unix_1")));
                executionTimes.add(Long.valueOf(resultSet.getInt("Embeded_date_Time_Unix_2")));
                executionTimes.add(Long.valueOf(resultSet.getInt("Embeded_date_Time_Unix_3")));
                executionTimes.add(Long.valueOf(resultSet.getInt("Embeded_date_Time_Unix_4")));
                executionTimes.add(Long.valueOf(resultSet.getInt("Embeded_date_Time_Unix_5")));
                executionTimes.add(Long.valueOf(resultSet.getInt("Embeded_date_Time_Unix_6")));
                executionTimes.add(Long.valueOf(resultSet.getInt("Embeded_date_Time_Unix_7")));
                executionTimes.add(Long.valueOf(resultSet.getInt("Embeded_date_Time_Unix_8")));
                String timesProgramRun = resultSet.getString("Number_time_file_run"); 
                String filePath = resultSet.getString("file_path");

                AbstractFile pfAbstractFile = getAbstractFile(prefetchFileName, PREFETCH_FILE_LOCATION);
                
                if (pfAbstractFile == null) {
                    logger.log(Level.SEVERE, "File has a null value " + prefetchFileName);//NON-NLS
                }
                
               for (Long executionTime : executionTimes) {

                    // only add prefetch file entries that have an actual date associated with them
                    if (executionTime > 0) {                 
                        Collection<BlackboardAttribute> bbattributes = Arrays.asList(
                            new BlackboardAttribute(
                                    BlackboardAttribute.ATTRIBUTE_TYPE.TSK_PROG_NAME, getName(),
                                    applicationName),//NON-NLS
                            new BlackboardAttribute(
                                    BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME, getName(),
                                    executionTime),
                            new BlackboardAttribute(
                                    BlackboardAttribute.ATTRIBUTE_TYPE.TSK_COUNT, getName(), Integer.valueOf(timesProgramRun)),
                            new BlackboardAttribute(
                                    BlackboardAttribute.ATTRIBUTE_TYPE.TSK_COMMENT, getName(), PREFETCH_TSK_COMMENT));

                        try {
                            BlackboardArtifact bbart = pfAbstractFile.newArtifact(BlackboardArtifact.ARTIFACT_TYPE.TSK_PROG_RUN);
                            bbart.addAttributes(bbattributes);
                            bba.add(bbart);
                            BlackboardArtifact associateBbArtifact = createAssociatedArtifact(applicationName.toLowerCase(), filePath, bbart);
                            if (associateBbArtifact != null) {
                                bba.add(associateBbArtifact);
                            }
                        } catch (TskCoreException ex) {
                            logger.log(Level.SEVERE, "Exception Adding Artifact.", ex);//NON-NLS
                        }
                    }
               }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error while trying to read into a sqlite db.", ex);//NON-NLS
        }

        try {
            blackboard.postArtifacts(bba, MODULE_NAME);
        } catch (Blackboard.BlackboardException ex) {
            logger.log(Level.SEVERE, "Error Posting Artifact.", ex);//NON-NLS
        }
    }
    
   /**
     * Create associated artifacts using file path name and the artifact it associates with
     * 
     * @param filePathName file and path of object being associated with
     * 
     * @param bba blackboard artifact to associate with
     * 
     * @returnv BlackboardArtifact or a null value 
     */  
    private BlackboardArtifact createAssociatedArtifact(String fileName, String filePathName, BlackboardArtifact bba) {
        AbstractFile sourceFile = getAbstractFile(fileName, filePathName);
        if (sourceFile != null) {
            Collection<BlackboardAttribute> bbattributes2 = new ArrayList<>();
            bbattributes2.addAll(Arrays.asList(
                 new BlackboardAttribute(TSK_ASSOCIATED_ARTIFACT, this.getName(),
                 bba.getArtifactID())));

            BlackboardArtifact associatedObjectBba = createArtifactWithAttributes(TSK_ASSOCIATED_OBJECT, sourceFile, bbattributes2);
            if (associatedObjectBba != null) {
                return associatedObjectBba;
            }
        }
       
        return null;
    }
    
    AbstractFile getAbstractFile(String fileName, String filePath) {
        List<AbstractFile> files;
        
        FileManager fileManager = Case.getCurrentCase().getServices().getFileManager();

        try {
            files = fileManager.findFiles(fileName, filePath); //NON-NLS            
        } catch (TskCoreException ex) {
            logger.log(Level.WARNING, "Unable to find prefetch files.", ex); //NON-NLS
            return null;  // No need to continue
        }
        
        if (!files.isEmpty()) {
            return files.get(0);
        } else {
            return null;
        }
        
    }
 
}
