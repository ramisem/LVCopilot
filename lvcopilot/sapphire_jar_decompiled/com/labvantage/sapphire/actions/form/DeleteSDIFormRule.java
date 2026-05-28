/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.form;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.PreparedStatement;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeleteSDIFormRule
extends BaseAction
implements sapphire.action.DeleteSDIFormRule {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

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
        String[] formidprop = StringUtil.split(properties.getProperty("formid"), ";");
        String[] forminstanceprop = StringUtil.split(properties.getProperty("forminstance"), ";");
        if (keyid1prop.length == formidprop.length && formidprop.length == forminstanceprop.length) {
            try {
                PreparedStatement delete = this.database.prepareStatement("DELETE FROM sdiformrule WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND formid = ? AND forminstance = ?");
                for (int i = 0; i < keyid1prop.length; ++i) {
                    delete.setString(1, sdcid);
                    delete.setString(2, keyid1prop[i]);
                    delete.setString(3, keyid2prop.length > i && keyid2prop[i].length() > 0 ? keyid2prop[i] : "(null)");
                    delete.setString(4, keyid3prop.length > i && keyid3prop[i].length() > 0 ? keyid3prop[i] : "(null)");
                    delete.setString(5, formidprop[i]);
                    delete.setString(6, forminstanceprop[i]);
                    delete.execute();
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to delete sdiformrule records. Exception: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        if (deleterset) {
            dam.clearRSet(rsetid);
        }
    }
}

