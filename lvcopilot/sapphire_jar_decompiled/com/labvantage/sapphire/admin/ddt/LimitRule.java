/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.util.HashMap;
import sapphire.accessor.ActionException;
import sapphire.action.BaseSDCRules;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LimitRule
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 72472 $";

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) {
        this.logger.debug("LimitRule postAdd called...");
        if (sdiData.getDataset("limitrulelimittype") != null && sdiData.getDataset("limitrulelimittype").getRowCount() == 0) {
            DataSet primary = sdiData.getDataset("primary");
            ActionBlock block = new ActionBlock();
            for (int row = 0; row < primary.getRowCount(); ++row) {
                String limitruleid = primary.getString(row, "limitruleid", "");
                String limitruleversionid = primary.getString(row, "limitruleversionid", "");
                if (limitruleid.length() <= 0 || limitruleversionid.length() <= 0) continue;
                this.logger.debug("limitruleid = " + limitruleid);
                this.logger.debug("limitruleversionid = " + limitruleversionid);
                HashMap<String, String> props = new HashMap<String, String>();
                props.put("linkid", "limittypes");
                props.put("sdcid", "LimitRule");
                props.put("limitruleid", limitruleid);
                props.put("limitruleversionid", limitruleversionid);
                props.put("displayformat", "(null)");
                props.put("qualifier", "(null)");
                props.put("transformrule", "(null)");
                props.put("condition", "(null)");
                props.put("limittypeid", "__null");
                props.put("limittypestatus", "__null");
                props.put("usersequence", "1");
                try {
                    block.setAction("addsdidetail" + row, "AddSDIDetail", "1", props);
                    continue;
                }
                catch (ActionException e) {
                    this.logger.error("ERROR: Could not add action to block at row " + row + ".", e);
                }
            }
            if (block.getActionCount() > 0) {
                try {
                    this.getActionProcessor().processActionBlock(block);
                }
                catch (ActionException e) {
                    this.logger.error("ERROR: Could not process action block.", e);
                }
            }
        }
    }
}

