/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.PreparedStatement;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Address
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 84852 $";

    @Override
    public void postAddKey(DataSet primary, PropertyList actionProps) {
        this.autoAllocateAddressId(primary, actionProps);
    }

    private void autoAllocateAddressId(DataSet primary, PropertyList actionProps) {
        if ("Y".equals(actionProps.getProperty("autoallocateaddressid", "N"))) {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                if (primary.getString(i, "addressid", "").length() != 0 && !"(Auto)".equalsIgnoreCase(primary.getString(i, "addressid", ""))) continue;
                int nextSeq = this.getSequenceProcessor().getSequence("Address", "addressidseq", 1, 1);
                primary.setString(i, "addressid", "ADDR-" + String.format("%06d", nextSeq));
            }
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.deleteAddressFromLabelEvent(actionProps);
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String contactFunction;
        if (actionProps.getProperty("addsdiaddress").equals("Y") && (contactFunction = actionProps.getProperty("addsdiaddress_contactfunction")).length() > 0) {
            DataSet primary = sdiData.getDataset("primary");
            for (int i = 0; i < primary.size(); ++i) {
                String addressid = primary.getString(i, "addressid");
                String addresstype = primary.getString(i, "addresstype");
                PropertyList addSDIAddressProps = new PropertyList();
                addSDIAddressProps.put("sdcid", actionProps.getProperty("addsdiaddress_sdcid"));
                addSDIAddressProps.put("keyid1", actionProps.getProperty("addsdiaddress_keyid1"));
                addSDIAddressProps.put("keyid2", actionProps.getProperty("addsdiaddress_keyid2"));
                addSDIAddressProps.put("keyid3", actionProps.getProperty("addsdiaddress_keyid3"));
                addSDIAddressProps.put("addressid", addressid);
                addSDIAddressProps.put("addresstype", addresstype);
                addSDIAddressProps.put("contactfunction", contactFunction);
                this.getActionProcessor().processAction("AddSDIAddress", "1", addSDIAddressProps);
            }
        }
    }

    private void deleteAddressFromLabelEvent(PropertyList actionProps) throws SapphireException {
        String[] printeraddressid = StringUtil.split(actionProps.getProperty("keyid1", ""), ";");
        String[] printeraddresstype = StringUtil.split(actionProps.getProperty("keyid2", ""), ";");
        if (printeraddressid.length > 0) {
            StringBuffer sql = new StringBuffer("SELECT labeleventid FROM labelevent WHERE printeraddressid = ?");
            sql.append(printeraddresstype.length == printeraddressid.length ? " AND printeraddresstype = ?" : "");
            PreparedStatement selectLabelEventStatement = this.database.prepareStatement("selectLabelEvent", sql.toString());
            StringBuffer labelEventsString = new StringBuffer();
            try {
                for (int i = 0; i < printeraddressid.length; ++i) {
                    DataSet labelEvents;
                    selectLabelEventStatement.setString(1, printeraddressid[i]);
                    if (printeraddresstype.length == printeraddressid.length) {
                        selectLabelEventStatement.setString(2, printeraddresstype[i]);
                    }
                    if ((labelEvents = new DataSet(selectLabelEventStatement.executeQuery())).getRowCount() <= 0) continue;
                    if (labelEventsString.length() > 0) {
                        labelEventsString.append(";");
                        labelEventsString.append(labelEvents.getColumnValues("labeleventid", ";"));
                        continue;
                    }
                    labelEventsString.append(labelEvents.getColumnValues("labeleventid", ";"));
                }
                if (labelEventsString.length() > 0) {
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put("sdcid", "LV_LabelEvent");
                    props.put("keyid1", labelEventsString.toString());
                    this.getActionProcessor().processAction("DeleteSDI", "1", props);
                }
            }
            catch (Exception e) {
                throw new SapphireException("Unable to delete link from labelevent table: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
        }
    }
}

