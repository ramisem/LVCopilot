/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.finance;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.finance.BaseFinanceAction;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddChargeListItem
extends BaseFinanceAction
implements sapphire.action.AddChargeListItem {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            this.initRSet("ChargeList", properties.getProperty("chargelistid"), properties.getProperty("chargelistitemid"), properties);
            this.setValues();
            String value = (String)properties.get("templateid");
            if (StringUtil.getLen(value) > 0L) {
                int i;
                this.financeitems.addColumn("_templateid", 0);
                String[] rowvalues = StringUtil.split(value, this.separator);
                if (rowvalues.length > 1) {
                    for (i = 0; i < rowvalues.length; ++i) {
                        if (rowvalues[i].equalsIgnoreCase("(null)")) {
                            this.financeitems.setString(i, "_templateid", "");
                            continue;
                        }
                        this.financeitems.setString(i, "_templateid", rowvalues[i]);
                    }
                } else {
                    for (i = 0; i < this.financeids.length; ++i) {
                        if (rowvalues[0].equalsIgnoreCase("(null)")) {
                            this.financeitems.setString(i, "_templateid", "");
                            continue;
                        }
                        this.financeitems.setString(i, "_templateid", rowvalues[0]);
                    }
                }
            }
            int ii = this.financeitems.getRowCount();
            String lastkeyid1 = "";
            String lastkeyid2 = "";
            String lastkeyid3 = "";
            String newkeyid1 = "";
            String newkeyid2 = "";
            String newkeyid3 = "";
            this.financeitems.sort("chargekeyid1,chargekeyid2,chargekeyid3");
            ActionProcessor actionProcessor = this.getActionProcessor();
            for (int i = 0; i < ii; ++i) {
                String keyid1 = this.financeitems.getString(i, "chargekeyid1");
                if (keyid1 == null || !keyid1.startsWith("NewSample")) continue;
                String keyid2 = this.financeitems.getString(i, "chargekeyid2");
                String keyid3 = this.financeitems.getString(i, "chargekeyid3");
                if (!(lastkeyid1.equals(keyid1) && lastkeyid2.equals(keyid2) && lastkeyid3.equals(keyid3))) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", this.financeitems.getString(i, "sdcid"));
                    props.setProperty("copies", "1");
                    props.setProperty("templateid", this.financeitems.getString(i, "_templateid"));
                    this.logger.info("Creating new SDI using template " + this.financeitems.getString(i, "_templateid"));
                    try {
                        actionProcessor.processAction("AddSDI", "1", props);
                        newkeyid1 = props.getProperty("newkeyid1", "(null)");
                        newkeyid2 = props.getProperty("newkeyid2", "(null)");
                        newkeyid3 = props.getProperty("newkeyid3", "(null)");
                        continue;
                    }
                    catch (ActionException actionException) {
                        throw new SapphireException("PROCESSACTION_FAILED", "Action AddSDI failed: " + ErrorUtil.extractMessageFromException(actionException, ErrorUtil.isUserAdmin(this.getConnectionId())), actionException);
                    }
                }
                this.financeitems.setString(i, "chargekeyid1", newkeyid1);
                this.financeitems.setString(i, "chargekeyid2", newkeyid2);
                this.financeitems.setString(i, "chargekeyid3", newkeyid3);
            }
            this.updateDatabase(true);
        }
        catch (SapphireException sapphireException) {
            throw new SapphireException("PROCESSACTION_FAILED", "Action failed : " + ErrorUtil.extractMessageFromException(sapphireException, ErrorUtil.isUserAdmin(this.getConnectionId())), sapphireException);
        }
    }
}

