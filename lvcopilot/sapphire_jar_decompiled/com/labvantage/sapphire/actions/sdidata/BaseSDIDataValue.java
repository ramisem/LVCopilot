/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public abstract class BaseSDIDataValue
extends BaseAction {
    public static final String COLUMNID = "columnid";
    public static final String VALUE = "value";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void getSDIDataValue(PropertyList properties, String tableId, int datatype) throws SapphireException {
        String rsetid = BaseSDIDataAction.createRSet(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), this.database, this.connectionInfo, false);
        try {
            boolean sdidata = tableId.equals("sdidata");
            boolean sdidataitem = tableId.equals("sdidataitem");
            StringBuffer selectSQL = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            selectSQL.append("SELECT\t").append(tableId).append(".* ").append("FROM\trsetitems, ").append(tableId).append(" WHERE rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" AND ").append(" rsetitems.sdcid = ").append(tableId).append(".sdcid AND ").append(" rsetitems.keyid1 = ").append(tableId).append(".keyid1 AND ").append("\trsetitems.keyid2 = ").append(tableId).append(".keyid2 AND ").append(" rsetitems.keyid3 = ").append(tableId).append(".keyid3");
            DataSet sdidatatable = new DataSet();
            this.database.createPreparedResultSet(selectSQL.toString(), safeSQL.getValues());
            sdidatatable.setResultSet(this.database.getResultSet());
            if (sdidatatable.getRowCount() > 0) {
                String separator = properties.getProperty("separator", ";");
                String sdcid = properties.getProperty("sdcid");
                String[] keyid1prop = StringUtil.split(properties.getProperty("keyid1"), separator);
                String[] keyid2prop = StringUtil.split(properties.getProperty("keyid2"), separator);
                String[] keyid3prop = StringUtil.split(properties.getProperty("keyid3"), separator);
                String[] paramlistidprop = StringUtil.split(properties.getProperty("paramlistid"), separator);
                String[] paramlistversionidprop = StringUtil.split(properties.getProperty("paramlistversionid"), separator);
                String[] variantidprop = StringUtil.split(properties.getProperty("variantid"), separator);
                String[] datasetprop = StringUtil.split(properties.getProperty("dataset"), separator);
                String[] paramidprop = StringUtil.split(properties.getProperty("paramid"), separator);
                String[] paramtypeprop = StringUtil.split(properties.getProperty("paramtype"), separator);
                String[] replicateidprop = StringUtil.split(properties.getProperty("replicateid"), separator);
                String columnid = properties.getProperty(COLUMNID);
                int findrow = -1;
                HashMap<String, Object> findmap = new HashMap<String, Object>();
                findmap.put("sdcid", sdcid);
                StringBuffer result = new StringBuffer("");
                NumberFormat numberFormat = NumberFormat.getInstance(I18nUtil.getConnectionLocale(this.connectionInfo));
                numberFormat.setGroupingUsed(false);
                DateFormat dateFormat = DateFormat.getDateTimeInstance(3, 3, I18nUtil.getConnectionLocale(this.connectionInfo));
                DateFormat dateFormatO = DateFormat.getDateInstance(2, I18nUtil.getConnectionLocale(this.connectionInfo));
                dateFormat.setTimeZone(I18nUtil.getConnectionTimeZone(this.connectionInfo));
                for (int i = 0; i < keyid1prop.length; ++i) {
                    String replicateid;
                    String keyid1 = keyid1prop[i];
                    String keyid2 = keyid2prop.length == 0 || keyid2prop.length < keyid1prop.length || keyid2prop[i].length() == 0 ? "(null)" : keyid2prop[i];
                    String keyid3 = keyid3prop.length == 0 || keyid3prop.length < keyid1prop.length || keyid3prop[i].length() == 0 ? "(null)" : keyid3prop[i];
                    String paramlistid = paramlistidprop.length == 0 || paramlistidprop.length < keyid1prop.length || paramlistidprop[i].length() == 0 ? "" : paramlistidprop[i];
                    String paramlistversionid = paramlistversionidprop.length == 0 || paramlistversionidprop.length < keyid1prop.length || paramlistversionidprop[i].length() == 0 ? "" : paramlistversionidprop[i];
                    String variantid = variantidprop.length == 0 || variantidprop.length < keyid1prop.length || variantidprop[i].length() == 0 ? "" : variantidprop[i];
                    String datasetstr = datasetprop.length == 0 || datasetprop.length < keyid1prop.length || datasetprop[i].length() == 0 ? "" : datasetprop[i];
                    String paramid = paramidprop.length == 0 || paramidprop.length < keyid1prop.length || paramidprop[i].length() == 0 ? "" : paramidprop[i];
                    String paramtype = paramtypeprop.length == 0 || paramtypeprop.length < keyid1prop.length || paramtypeprop[i].length() == 0 ? "" : paramtypeprop[i];
                    String string = replicateid = replicateidprop.length == 0 || replicateidprop.length < keyid1prop.length || replicateidprop[i].length() == 0 ? "" : replicateidprop[i];
                    if (sdidata && (paramlistid.length() == 0 || paramlistversionid.length() == 0 || variantid.length() == 0 || datasetstr.length() == 0)) {
                        throw new SapphireException("INVALID_PROPERTIES", "Property values for sdidata do not match");
                    }
                    if (sdidataitem && (paramlistid.length() == 0 || paramlistversionid.length() == 0 || variantid.length() == 0 || datasetstr.length() == 0 || paramid.length() == 0 || paramtype.length() == 0 || replicateid.length() == 0)) {
                        throw new SapphireException("INVALID_PROPERTIES", "Property values for sdidataitem do not match");
                    }
                    findmap.put("keyid1", keyid1);
                    findmap.put("keyid2", keyid2);
                    findmap.put("keyid3", keyid3);
                    findmap.put("paramlistid", paramlistid);
                    findmap.put("paramlistversionid", paramlistversionid);
                    findmap.put("variantid", variantid);
                    findmap.put("dataset", new BigDecimal(datasetstr));
                    if (sdidataitem) {
                        findmap.put("paramid", paramid);
                        findmap.put("paramtype", paramtype);
                        findmap.put("replicateid", new BigDecimal(replicateid));
                    }
                    if ((findrow = sdidatatable.findRow(findmap)) < 0) continue;
                    if (sdidatatable.getColumnType(columnid) == datatype) {
                        switch (datatype) {
                            case 0: {
                                result.append(sdidatatable.getString(findrow, columnid));
                                break;
                            }
                            case 1: {
                                result.append(numberFormat.format(sdidatatable.getBigDecimal(findrow, columnid).doubleValue()));
                                break;
                            }
                            case 2: {
                                if ("Y".equals(this.getSDCProcessor().getSDCColumnProperty("DataItem", columnid, "timezoneindependent"))) {
                                    result.append(dateFormatO.format(sdidatatable.getCalendar(findrow, columnid).getTime()));
                                    break;
                                }
                                result.append(dateFormat.format(sdidatatable.getCalendar(findrow, columnid).getTime()));
                            }
                        }
                        if (i >= keyid1prop.length - 1) continue;
                        result.append(";");
                        continue;
                    }
                    throw new SapphireException("INVALID_PROPERTIES", "Invalid column data type.");
                }
                properties.setProperty(VALUE, result.toString());
            }
        }
        finally {
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    @Override
    public abstract void processAction(PropertyList var1) throws SapphireException;
}

