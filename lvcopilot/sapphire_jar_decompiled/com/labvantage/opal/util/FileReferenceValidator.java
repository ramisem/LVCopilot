/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import sapphire.accessor.TranslationProcessor;

public class FileReferenceValidator {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String DEFAULT_STRING_VALUE = "";
    protected static final String ERROR_MESSAGE_NO_REFERENCE_FOUND = "No Reference Found";
    protected static final String ERROR_MESSAGE_REFERENCE_NULL = "File Name is not available";
    protected Map _FileReferences = null;
    protected ArrayList _ErrorMessages = new ArrayList();
    protected boolean _IsError = false;

    public FileReferenceValidator() {
    }

    public FileReferenceValidator(Map fileReferences) {
        this();
        this._FileReferences = fileReferences;
    }

    public void setFileReferences(Map fileReferences) {
        this._FileReferences = fileReferences;
    }

    public Map getFileReferences() {
        return this._FileReferences;
    }

    protected void setErrorMessage(String errorMessage) {
        this._ErrorMessages.add(errorMessage);
    }

    protected void setErrorStatus(boolean isError) {
        this._IsError = isError;
    }

    public boolean isAnyError() {
        return this._IsError;
    }

    public ArrayList getErrorMessagesList() {
        if (this._ErrorMessages.isEmpty()) {
            return null;
        }
        return this._ErrorMessages;
    }

    public String getErrorMessages(TranslationProcessor tp) {
        if (!this._ErrorMessages.isEmpty()) {
            String returnString = DEFAULT_STRING_VALUE;
            boolean isTranslationPossible = true;
            if (tp == null) {
                isTranslationPossible = false;
            }
            if (this._ErrorMessages.size() == 1) {
                String currentMessage = (String)this._ErrorMessages.get(0);
                returnString = isTranslationPossible ? tp.translate(currentMessage) : currentMessage;
            } else if (this._ErrorMessages.size() > 1) {
                String currentMessage = (String)this._ErrorMessages.get(0);
                returnString = isTranslationPossible ? tp.translate(currentMessage) : currentMessage;
                for (int errorCount = 1; errorCount < this._ErrorMessages.size() - 1; ++errorCount) {
                    currentMessage = (String)this._ErrorMessages.get(errorCount);
                    String messageAfterTranslation = isTranslationPossible ? tp.translate(currentMessage) : currentMessage;
                    returnString = returnString + "\\n" + messageAfterTranslation;
                }
            }
            return returnString;
        }
        return DEFAULT_STRING_VALUE;
    }

    public boolean validateAll() {
        boolean validateAllStatus = true;
        if (this._FileReferences == null || this._FileReferences.isEmpty()) {
            this.setErrorStatus(true);
            this.setErrorMessage(ERROR_MESSAGE_NO_REFERENCE_FOUND);
            validateAllStatus = false;
            return validateAllStatus;
        }
        for (String rowNumber : this._FileReferences.keySet()) {
            String[] fileNames = (String[])this._FileReferences.get(rowNumber);
            if (fileNames == null || fileNames.length < 1 || this.validate(fileNames[0], rowNumber) || !validateAllStatus) continue;
            this.setErrorStatus(true);
            validateAllStatus = false;
        }
        Iterator iterator = null;
        return validateAllStatus;
    }

    public boolean validate(String fileName, String rowNumber) {
        boolean validateStatus = true;
        if (fileName == null) {
            validateStatus = false;
            this.setErrorMessage(ERROR_MESSAGE_REFERENCE_NULL);
            return validateStatus;
        }
        File file = new File(fileName);
        if (!file.exists() || !file.canRead()) {
            String displayName = fileName;
            if (displayName.indexOf(92) != -1) {
                displayName = fileName.substring(fileName.lastIndexOf(92) + 1);
            }
            if (rowNumber == null || rowNumber.trim().equals(DEFAULT_STRING_VALUE)) {
                this.setErrorMessage("Referenced File " + displayName + " not accessible from Application Server.");
            } else {
                this.setErrorMessage("Referenced File " + displayName + " not accessible from Application Server. ( Row Number " + rowNumber + " )");
            }
            validateStatus = false;
        }
        file = null;
        return validateStatus;
    }
}

