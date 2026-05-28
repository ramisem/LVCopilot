/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.EJBException
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.I18NManagement;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.I18NService;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import sapphire.xml.PropertyList;

public class I18NManagerBean
extends BaseManager
implements SessionBean,
I18NManagement {
    public I18NManagerBean() {
        this.logName = "I18NManager";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean isAutoFillTempAllowed(String connectionid) throws ManagerException {
        String methodName = "isAutoFillTempAllowed";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName);
            this.startMethod(methodName, connectionid);
            I18NService i18n = new I18NService(this.sapphireConnection);
            boolean bl = i18n.isAutoFillTempAllowed();
            return bl;
        }
        catch (Exception e) {
            this.logError("Failed to get profile property 'masterupdate' - using default of N. Exception:" + e.getMessage(), e);
            boolean bl = false;
            return bl;
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void addToTransmasterTemp(String connectionid, String originaltext, String texttype) throws ManagerException {
        String methodName = "addToTransmasterTemp";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, originaltext + ";" + texttype);
            this.startMethod(methodName, connectionid);
            I18NService i18n = new I18NService(this.sapphireConnection);
            i18n.addToTransmasterTemp(originaltext, texttype);
        }
        catch (Exception e) {
            this.logError("Failed to add transmastertemp record", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public void saveTranslation(String connectionid, String language, String textidlist, String transtextlist, String texttypelist) throws ManagerException {
        String methodName = "saveTranslation";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, language + ";" + textidlist + ";" + transtextlist + ";" + texttypelist);
            this.startMethod(methodName, connectionid);
            I18NService i18n = new I18NService(this.sapphireConnection);
            i18n.saveTranslation(language, textidlist, transtextlist, texttypelist);
        }
        catch (Exception e) {
            this.logError("Failed to save translation data");
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public PropertyList translateTable(String connectionid, String languageid, PropertyList transTable) throws ManagerException {
        String methodName = "translateTable";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, languageid + ";" + transTable.toString());
            this.startMethod(methodName, connectionid);
            I18NService i18n = new I18NService(this.sapphireConnection);
            PropertyList propertyList = i18n.translateTable(languageid, transTable);
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to translate table", e);
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }

    @Override
    public PropertyList getWebTranslations(String connectionid, String languageid) throws ManagerException {
        String methodName = "getWebTranslations";
        try {
            Trace.setStartCodeBlock(this.logName + "." + methodName, languageid);
            this.startMethod(methodName, connectionid);
            I18NService i18n = new I18NService(this.sapphireConnection);
            PropertyList propertyList = i18n.getWebTranslations(languageid);
            return propertyList;
        }
        catch (Exception e) {
            this.logError("Failed to get web transaltions");
            this.beforeTransactionAbort();
            throw new EJBException(e);
        }
        finally {
            this.endMethod(methodName);
            Trace.setEndCodeBlock(this.logName + "." + methodName);
        }
    }
}

