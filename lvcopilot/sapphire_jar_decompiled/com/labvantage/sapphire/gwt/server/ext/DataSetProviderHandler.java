/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.gwt.server.ext;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.gwt.server.ext.BaseDataSetProvider;
import com.labvantage.sapphire.pageelements.PropertyHandler;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class DataSetProviderHandler
extends PropertyHandler {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        PropertyList properties = (PropertyList)props.get("properties");
        String datasetProviderClass = (String)props.get("datasetproviderclass");
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            Class<?> c = Class.forName(datasetProviderClass);
            BaseDataSetProvider dataSetProvider = (BaseDataSetProvider)c.newInstance();
            dataSetProvider.setSapphireConnection(this.sapphireConnection);
            dataSetProvider.setDatabase(dbu);
            props.put("dataset", dataSetProvider.getDataSet(properties));
        }
        catch (Exception e) {
            this.logError("Error getting data from DataSet provider '" + datasetProviderClass + "'. Reason: " + e.getMessage());
        }
        finally {
            if (dbu != null) {
                dbu.releaseConnection();
            }
        }
    }
}

