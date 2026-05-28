/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.messaging.MessageLogUtil;
import java.io.File;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class ProcessFile
extends BaseAction
implements sapphire.action.ProcessFile {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String path = properties.getProperty("path", "");
        String fileName = properties.getProperty("filename", "");
        String actionId = properties.getProperty("processactionid", "");
        String actionVersionId = properties.getProperty("processactionversionid", "");
        String failPath = properties.getProperty("failpath", "");
        String sucessPath = properties.getProperty("successpath", "");
        boolean readFileContent = "Y".equalsIgnoreCase(properties.getProperty("readfilecontent", "N"));
        String fileContentPropertyId = properties.getProperty("filecontentpropertyid", "filecontent");
        boolean retainOnSuccess = "Y".equals(properties.getProperty("retainonsuccess", "N"));
        boolean retainOnFailure = "Y".equals(properties.getProperty("retainonfailure", "N"));
        if (path.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", "The path " + path + " is invalid.");
        }
        path = path.replaceAll("\\\\", "/");
        if (fileName.length() == 0) {
            fileName = path.substring(path.lastIndexOf(47) + 1);
            path = path.substring(0, path.lastIndexOf(47) + 1);
            properties.put("filename", fileName);
            properties.put("path", path);
        }
        if (fileName.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY The path or filename is invalid");
        }
        fileName = fileName.trim();
        if (actionId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY The actionid processactionid is invalid.");
        }
        if (actionVersionId.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY The processactionversionid processactionversionid is invalid.");
        }
        ActionProcessor ap = this.getActionProcessor();
        PropertyList actionProperties = new PropertyList();
        actionProperties.putAll(properties);
        try {
            if (readFileContent) {
                String readAction = properties.getProperty("readactionid", "");
                String readActionVersion = properties.getProperty("readactionversionid", "1");
                if (readAction.length() == 0) {
                    File file = new File(path + "/" + fileName);
                    String fileContent = FileUtil.getFileString(file, properties.getProperty("encoding", "UTF-8"));
                    actionProperties.put(fileContentPropertyId, fileContent);
                } else {
                    PropertyList readActionProperties = properties.copy();
                    ap.processAction(readAction, readActionVersion, readActionProperties);
                    String fileContent = readActionProperties.getProperty("filecontent", "");
                    actionProperties.put(fileContentPropertyId, fileContent);
                }
            } else {
                actionProperties.put("filename", fileName);
                actionProperties.put("path", path);
                actionProperties.put("failpath", failPath);
                actionProperties.put("successpath", sucessPath);
            }
            ap.processAction(actionId, actionVersionId, actionProperties);
            String messageLogId = actionProperties.getProperty("messagelogid", "");
            properties.putAll(actionProperties);
            if ("FAILED".equals(actionProperties.getProperty("status"))) {
                String validationlog;
                if (messageLogId.length() > 0 && (validationlog = actionProperties.getProperty("validationlog", "")).length() > 0) {
                    String url = "<A target='_top' HREF=rc?command=page&page=LV_MessageLogList&keyid1=" + messageLogId + ">Check Message Log Details: " + messageLogId + "</A>";
                    throw new SapphireException(actionProperties.getProperty("error") + " " + url);
                }
                throw new SapphireException(actionProperties.getProperty("error"));
            }
        }
        catch (ActionException e) {
            this.logger.error("Failed to process file: " + fileName);
            File f = new File(path + "/" + fileName);
            if (failPath.equals("")) {
                if (!retainOnFailure) {
                    this.logger.info("Deleting failed file");
                    f.delete();
                }
            } else {
                boolean status = FileUtil.renameTo(f, new File(failPath + "/" + fileName));
                if (!status) {
                    Trace.log("Failed to rename file to " + failPath + "/" + fileName);
                }
                String messageLogId = actionProperties.getProperty("messagelogid", "");
                MessageLogUtil.updateFilePath(this.getActionProcessor(), messageLogId, failPath + "/" + fileName);
            }
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        catch (SapphireException e) {
            this.logger.error("Failed to process file: " + fileName);
            File f = new File(path + "/" + fileName);
            if (failPath.equals("")) {
                if (!retainOnFailure) {
                    this.logger.info("Deleting failed file");
                    f.delete();
                }
            } else {
                boolean status = FileUtil.renameTo(f, new File(failPath + "/" + fileName));
                if (!status) {
                    Trace.log("Failed to rename file to:  " + failPath + "/" + fileName);
                }
                String messageLogId = actionProperties.getProperty("messagelogid", "");
                MessageLogUtil.updateFilePath(this.getActionProcessor(), messageLogId, failPath + File.separator + fileName);
            }
            throw new SapphireException("Failed to processfile: " + fileName + ". " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        catch (IOException e) {
            throw new SapphireException("Failed to processfile: " + fileName + ". " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        File f = new File(path + "/" + fileName);
        if (sucessPath.equals("")) {
            if (!retainOnSuccess) {
                this.logger.info("Deleting file after successful processing");
                f.delete();
            }
        } else {
            String messageLogId;
            this.logger.info("Moving file after successful processing");
            boolean status = FileUtil.renameTo(f, new File(sucessPath + "/" + fileName));
            if (!status) {
                Trace.log("Failed to rename file to:  " + sucessPath + "/" + fileName);
            }
            if ((messageLogId = actionProperties.getProperty("messagelogid", "")).length() > 0) {
                MessageLogUtil.updateFilePath(this.getActionProcessor(), messageLogId, sucessPath + "/" + fileName);
            }
        }
    }
}

