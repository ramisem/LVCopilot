/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.io.File;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ProcessFiles
extends BaseAction
implements sapphire.action.ProcessFiles {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String lookupDirs = properties.getProperty("path");
        String filters = properties.getProperty("filter").toLowerCase();
        String actionid = properties.getProperty("processactionid");
        String actionVersionid = properties.getProperty("processactionversionid");
        String processPath = properties.getProperty("processpath");
        String successPath = properties.getProperty("successpath");
        String failPath = properties.getProperty("failpath");
        String synchronousFlag = properties.getProperty("synchronous", "Y");
        if (lookupDirs.equals("")) {
            throw new SapphireException("INVALID_PARAMETER", "The path '" + lookupDirs + "' is invalid.");
        }
        if (actionid.equals("")) {
            throw new SapphireException("INVALID_PARAMETER", "The actionid '" + actionid + "' is invalid.");
        }
        if (actionVersionid.equals("")) {
            actionVersionid = "1";
        }
        String[] lookupDirArr = StringUtil.split(lookupDirs, ";");
        String[] filtersArr = null;
        filtersArr = StringUtil.split(filters, ";");
        for (int indexLookup = 0; indexLookup < lookupDirArr.length; ++indexLookup) {
            File dir = new File(lookupDirArr[indexLookup]);
            if (dir == null || !dir.isDirectory()) {
                this.logger.info("INVALID_PROPERTY LOOKUP DIRECTORY " + lookupDirArr[indexLookup]);
                continue;
            }
            File[] files = this.sortFile(dir.listFiles());
            if (files == null) continue;
            for (int fileIndex = 0; fileIndex < files.length; ++fileIndex) {
                String tempFileName = files[fileIndex].getName().toLowerCase();
                File f = files[fileIndex];
                if (!this.matchFilters(filtersArr, tempFileName) || !f.isFile()) continue;
                boolean status = f.renameTo(new File(processPath + "/", f.getName()));
                if (status) {
                    PropertyList actionProperties = new PropertyList();
                    actionProperties.putAll(properties);
                    actionProperties.put("filename", files[fileIndex].getName());
                    actionProperties.put("path", processPath);
                    if (synchronousFlag.equalsIgnoreCase("Y")) {
                        try {
                            Trace.log("Synchronously Processing File " + files[fileIndex].getName());
                            ActionProcessor ap = this.getActionProcessor();
                            ap.processAction("ProcessFile", "1", actionProperties, true);
                        }
                        catch (SapphireException ex) {
                            Trace.logError("Error processing file " + files[fileIndex].getName());
                        }
                        continue;
                    }
                    Trace.log("Asynchronously Processing File " + files[fileIndex].getName());
                    AutomationService automation = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    try {
                        automation.addToDoListEntry("ProcessFiles", "ProcessFile", "1", actionProperties, "NOW", true);
                        continue;
                    }
                    catch (ServiceException e) {
                        throw new SapphireException("AUTOMATION_SERVICE_FAILED", "Error submitting" + actionid + " to automation servie ");
                    }
                }
                Trace.logError("Error renaming file " + f.getName());
            }
        }
    }

    private File[] sortFile(File[] fileList) {
        fileList = FileUtil.sortFilesByLastModified(fileList);
        return fileList;
    }

    private boolean matchFilters(String[] filtersArr, String fileName) {
        boolean flag = false;
        for (int filterIndex = 0; filterIndex < filtersArr.length && !flag; ++filterIndex) {
            flag = FileUtil.wildcardMatch(fileName, filtersArr[filterIndex], false);
        }
        return flag;
    }
}

