/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.util;

import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FilterUtil {
    private PropertyList pageProps;

    public FilterUtil(PropertyList pageProps) {
        this.pageProps = pageProps;
    }

    private PropertyListCollection parseFilters(String dataSourceName) {
        PropertyListCollection filters = new PropertyListCollection();
        for (Object key : this.pageProps.keySet()) {
            String elementId = (String)key;
            PropertyList elementProps = this.pageProps.getPropertyList(elementId);
            if (elementProps == null || !elementProps.containsKey("filters")) continue;
            PropertyListCollection elementFilters = elementProps.getCollection("filters");
            for (int i = 0; i < elementFilters.size(); ++i) {
                PropertyList filter = elementFilters.getPropertyList(i);
                if (!filter.getProperty("tableid", "").equals(dataSourceName) && !elementId.equals(dataSourceName)) continue;
                filters.add(filter);
            }
        }
        return filters;
    }

    private void doFilterDataset(String filtercolumn, String filtermode, String valuelist, DataSet dataset) {
        for (int i = dataset.getRowCount() - 1; i >= 0; --i) {
            String[] arrValues;
            boolean deleteRow = filtermode.equals("Show");
            String value = dataset.getValue(i, filtercolumn, "");
            for (String filtervalue : arrValues = valuelist.split(";")) {
                if (!filtervalue.equals(value)) continue;
                deleteRow ^= true;
                break;
            }
            if (!deleteRow) continue;
            dataset.deleteRow(i);
        }
    }

    public void filterDataset(DataSet ds, String dataSourceName) {
        PropertyListCollection filters = this.parseFilters(dataSourceName);
        for (Object filterObj : filters) {
            PropertyList filter = (PropertyList)filterObj;
            String rule = filter.getProperty("rule", "");
            String filtercolumn = filter.getProperty("columnid", "");
            String valuelist = filter.getProperty("valuelist", "");
            if (ds == null) continue;
            this.doFilterDataset(filtercolumn, rule, valuelist, ds);
        }
    }
}

