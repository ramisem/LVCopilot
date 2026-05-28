/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.util.StringHolder;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddSDIRole
extends BaseAction
implements sapphire.action.AddSDIRole {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        int rc = 1;
        boolean deleterset = false;
        String rsetid = properties.getProperty("rsetid");
        DAMProcessor dam = this.getDAMProcessor();
        DataSet sdiroles = null;
        DataSet newadd = null;
        String sdcid = properties.getProperty("sdcid", "");
        String keyid1p = properties.getProperty("keyid1", "");
        String roleidp = properties.getProperty("roleid", "");
        if (sdcid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "sdcid is mandatory");
        }
        if (keyid1p.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "keyid1 is mandatory");
        }
        if (roleidp.length() == 0) {
            throw new SapphireException("INVALID_PROPERTIES", "roleid is mandatory");
        }
        if (rsetid.length() == 0) {
            boolean applylock = properties.getProperty("applylock").equals("Y");
            StringHolder rsetidHolder = new StringHolder();
            rc = applylock ? dam.createLockedRSet(sdcid, properties.getProperty("keyid1"), "", "", rsetidHolder) : dam.createRSet(sdcid, properties.getProperty("keyid1"), "", "", rsetidHolder);
            if (rc != 1) {
                throw new SapphireException("CREATE_RSET_FAILURE", "Failed to create rset");
            }
            rsetid = rsetidHolder.value;
            deleterset = true;
        }
        try {
            newadd = new DataSet();
            sdiroles = new DataSet();
            String selectSDIRoles = new StringBuffer().append("SELECT sdirole.sdcid, sdirole.keyid1, sdirole.roleid, sdirole.privid ").append("FROM sdirole, rsetitems ").append("WHERE rsetitems.sdcid = ? ").append("AND rsetitems.rsetid = ? ").append("AND rsetitems.sdcid = sdirole.sdcid ").append("AND rsetitems.keyid1 = sdirole.keyid1 ").append("ORDER BY sdirole.sdcid, sdirole.keyid1, sdirole.roleid, sdirole.privid").toString();
            try {
                this.database.createPreparedResultSet(selectSDIRoles, new Object[]{sdcid, rsetid});
                sdiroles.setResultSet(this.database.getResultSet());
            }
            catch (SapphireException e) {
                throw new SapphireException("CREATE_RESULTSET_FAILED", "Failed to get result set for: " + selectSDIRoles);
            }
            boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
            HashMap<String, String> findmap = new HashMap<String, String>();
            String separator = properties.getProperty("separator", ";");
            String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
            String[] roleidprop = StringUtil.split(properties.getProperty("roleid"), separator);
            String[] prividprop = StringUtil.split(properties.getProperty("privid"), separator);
            if (roleidprop.length > 0) {
                for (int sdi = 0; sdi < (propsmatch ? 1 : keyid1prop.length); ++sdi) {
                    for (int role = 0; role < roleidprop.length; ++role) {
                        String keyid1 = "";
                        keyid1 = propsmatch ? keyid1prop[role] : keyid1prop[sdi];
                        findmap.put("keyid1", keyid1);
                        String roleid = roleidprop[role];
                        String privid = prividprop.length == 0 || prividprop.length < keyid1prop.length || prividprop[sdi].length() == 0 ? "list" : prividprop[role];
                        findmap.put("roleid", roleid);
                        findmap.put("privid", privid);
                        int findrow = sdiroles.findRow(findmap);
                        if (findrow != -1) continue;
                        int newrow = newadd.addRow();
                        newadd.setString(newrow, "sdcid", sdcid);
                        newadd.setString(newrow, "keyid1", keyid1);
                        newadd.setString(newrow, "roleid", roleid);
                        newadd.setString(newrow, "privid", privid);
                    }
                }
            } else {
                throw new SapphireException("INVALID_PROPERTIES", "There must be a roleid and priv for each");
            }
            this.logger.info("Processing the sdirole inserts: " + newadd);
            try {
                DataSetUtil.insert(this.database, newadd, "sdirole");
            }
            catch (SapphireException e) {
                throw new SapphireException("DB_INSERT_FAILED", "Failed to update sdi roles: " + e);
            }
        }
        catch (SapphireException e) {
            throw new SapphireException("Error: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            if (deleterset) {
                dam.clearRSet(rsetid);
            }
            newadd.reset();
            sdiroles.reset();
        }
    }
}

