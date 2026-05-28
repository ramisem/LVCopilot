/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.sql;

import com.labvantage.sapphire.DataSetUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ExecuteInsert
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String PROPERTY_TABLEID = "tableid";
    public static final String PROPERTY_DATASET = "dataset";

    @Override
    public void processAction(PropertyList props) throws SapphireException {
        DataSet ds;
        String tableid = props.getProperty(PROPERTY_TABLEID);
        if (StringUtil.getLen(tableid) > 0L && (ds = (DataSet)props.get(PROPERTY_DATASET)) != null) {
            DataSetUtil.insert(this.database, ds, tableid);
        }
    }
}

