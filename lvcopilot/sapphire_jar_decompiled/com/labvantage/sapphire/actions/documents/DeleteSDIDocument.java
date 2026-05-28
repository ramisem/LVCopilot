/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.documents;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.PreparedStatement;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDIDocument
extends BaseAction
implements sapphire.action.DeleteSDIDocument {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String sdcid = properties.getProperty("sdcid");
        String rsetid = properties.getProperty("rsetid");
        if (rsetid == null) {
            rsetid = "";
        }
        boolean deleterset = false;
        DAMProcessor dam = this.getDAMProcessor();
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            rsetid = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3")) : dam.createRSet(sdcid, properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"));
            if (rsetid.length() == 0) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create RSET for edit");
            }
            deleterset = true;
        }
        String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), ";");
        String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), ";");
        String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), ";");
        String[] documentidprop = StringUtil.split(properties.getProperty("documentid"), ";");
        String[] documentversionidprop = StringUtil.split(properties.getProperty("documentversionid"), ";");
        if (keyid1prop.length == documentidprop.length && documentidprop.length == documentversionidprop.length) {
            try {
                PreparedStatement delete = this.database.prepareStatement("DELETE FROM sdidocument WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND documentid = ? AND documentversionid = ?");
                for (int i = 0; i < keyid1prop.length; ++i) {
                    delete.setString(1, sdcid);
                    delete.setString(2, keyid1prop[i]);
                    delete.setString(3, keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
                    delete.setString(4, keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
                    delete.setString(5, documentidprop[i]);
                    delete.setString(6, documentversionidprop[i]);
                    delete.execute();
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to delete sdidocument records. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }
}

