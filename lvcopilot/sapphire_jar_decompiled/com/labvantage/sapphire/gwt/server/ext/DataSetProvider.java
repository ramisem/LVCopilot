/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.gwt.server.ext;

import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public interface DataSetProvider {
    public DataSet getDataSet(PropertyList var1) throws SapphireException;
}

