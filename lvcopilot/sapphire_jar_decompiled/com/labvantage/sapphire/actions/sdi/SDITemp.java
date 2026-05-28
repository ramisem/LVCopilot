/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDITemp
extends BaseAction {
    public static final String MODE_LIST = "list";
    public static final String MODE_LISTFULL = "listfull";
    public static final String MODE_ADD = "add";
    public static final String MODE_REMOVE = "remove";
    public static final String MODE_UPDATE = "update";
    public static final String MODE_CHECKUPDATE = "checkupdate";
    public static final String PROPERTY_MODE = "mode";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_TEMPID = "tempid";
    public static final String PROPERTY_COUNT = "count";
    public static final String PROPERTY_TEMPVALUE = "tempvalue";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_MODBY = "modby";
    public static final String PROPERTY_MODDT = "moddt";

    private void add(String sdcid, String keyid1, String keyid2, String keyid3, String[] tempids, PropertyList props) throws SapphireException {
        if (sdcid == null || sdcid.length() == 0) {
            sdcid = "SDC";
        }
        for (int i = 0; i < tempids.length; ++i) {
            String tempvalue = i == 0 ? props.getProperty("tempvalue1", props.getProperty("value1", props.getProperty(PROPERTY_TEMPVALUE, props.getProperty(PROPERTY_VALUE, "")))) : props.getProperty(PROPERTY_TEMPVALUE + (i + 1), props.getProperty(PROPERTY_VALUE + (i + 1), ""));
            if (tempvalue.length() <= 0) {
                throw new SapphireException("No data to add to temp provided.");
            }
            String user = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer("INSERT INTO sditemp (sdcid, keyid1, keyid2, keyid3, tempid, tempvalue, createdt, createby, createtool, moddt, modby, modtool");
            sql.append(") VALUES (");
            sql.append(safeSQL.addVar(sdcid != null && sdcid.length() > 0 ? sdcid : "(null)"));
            sql.append(",").append(safeSQL.addVar(keyid1 != null && keyid1.length() > 0 ? keyid1 : "(null)"));
            sql.append(",").append(safeSQL.addVar(keyid2 != null && keyid2.length() > 0 ? keyid2 : "(null)"));
            sql.append(",").append(safeSQL.addVar(keyid3 != null && keyid3.length() > 0 ? keyid3 : "(null)"));
            sql.append(",").append(safeSQL.addVar(tempids[i]));
            sql.append(",").append(safeSQL.addVar(tempvalue));
            sql.append(",").append(safeSQL.addVar(DateTimeUtil.getNowTimestamp()));
            sql.append(",").append(safeSQL.addVar(user));
            sql.append(",").append("'").append("SDITemp").append("'");
            sql.append(",").append(safeSQL.addVar(DateTimeUtil.getNowTimestamp()));
            sql.append(",").append(safeSQL.addVar(user));
            sql.append(",").append("'").append("SDITemp").append("'");
            sql.append(")");
            this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
        }
    }

    private void update(String sdcid, String keyid1, String keyid2, String keyid3, String tempid, PropertyList props) throws SapphireException {
        String[] tempids = StringUtil.split(tempid, ";");
        for (int i = 0; i < tempids.length; ++i) {
            String tempvalue = i == 0 ? props.getProperty("tempvalue1", props.getProperty("value1", props.getProperty(PROPERTY_TEMPVALUE, props.getProperty(PROPERTY_VALUE, "")))) : props.getProperty(PROPERTY_TEMPVALUE + (i + 1), props.getProperty(PROPERTY_VALUE + (i + 1), ""));
            if (tempvalue.length() > 0) {
                String user = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
                SafeSQL safeSQL = new SafeSQL();
                String sql = "UPDATE sditemp SET tempvalue=" + safeSQL.addVar(tempvalue);
                sql = sql + ", moddt = " + safeSQL.addVar(DateTimeUtil.getNowTimestamp());
                sql = sql + ", modby=" + safeSQL.addVar(user);
                sql = sql + ", modtool='SDITemp' ";
                String where = "";
                if (sdcid.length() > 0) {
                    where = where + (where.length() > 0 ? " AND " : "") + " sdcid=" + safeSQL.addVar(sdcid);
                }
                if (keyid1.length() > 0) {
                    where = where + (where.length() > 0 ? " AND " : "") + " keyid1=" + safeSQL.addVar(keyid1);
                }
                if (keyid2.length() > 0) {
                    where = where + (where.length() > 0 ? " AND " : "") + " keyid2=" + safeSQL.addVar(keyid2);
                }
                if (keyid3.length() > 0) {
                    where = where + (where.length() > 0 ? " AND " : "") + " keyid3=" + safeSQL.addVar(keyid3);
                }
                where = where + (where.length() > 0 ? " AND " : "") + " tempid=" + safeSQL.addVar(tempids[i]);
                try {
                    DBUtil db = new DBUtil();
                    db.setConnection(this.getConnectionProcessor().getSapphireConnection().getDbms(), this.database.getConnection());
                    db.executePreparedUpdate(sql + " WHERE " + where, safeSQL.getValues());
                    continue;
                }
                catch (SapphireException e) {
                    throw new SapphireException("Could not update clob.", e);
                }
            }
            throw new SapphireException("No data to add to temp provided.");
        }
    }

    private void remove(String sdcid, String keyid1, String keyid2, String keyid3, String tempid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("DELETE sditemp WHERE ");
        StringBuffer where = new StringBuffer();
        if (keyid1 != null && keyid1.length() > 0) {
            where.append("keyid1=");
            where.append(safeSQL.addVar(keyid1));
        }
        if (sdcid.length() > 0) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append("sdcid=").append(safeSQL.addVar(sdcid)).append("");
        }
        if (keyid2.length() > 0) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append("keyid2=").append(safeSQL.addVar(keyid2)).append("");
        }
        if (keyid3.length() > 0) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append("keyid3=").append(safeSQL.addVar(keyid3)).append("");
        }
        if (tempid.length() > 0) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            if (tempid.contains(";")) {
                tempid = StringUtil.replaceAll(tempid, ";", "','");
                where.append(" tempid IN (").append(safeSQL.addIn(tempid)).append(") ");
            } else {
                where.append(" tempid=").append(safeSQL.addVar(tempid)).append(" ");
            }
        }
        sql.append(where);
        this.database.executePreparedUpdate(sql.toString(), safeSQL.getValues());
    }

    private int list(String sdcid, String keyid1, String keyid2, String keyid3, String tempid, boolean full, PropertyList props) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        if (full) {
            sql.append("SELECT tempid, moddt, modby, tempvalue FROM sditemp WHERE ");
        } else {
            sql.append("SELECT tempid, moddt, modby FROM sditemp WHERE ");
        }
        StringBuffer where = new StringBuffer();
        if (keyid1 != null && keyid1.length() > 0) {
            where.append("keyid1=");
            where.append(safeSQL.addVar(keyid1));
        }
        if (sdcid.length() > 0) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append("sdcid=").append(safeSQL.addVar(sdcid)).append("");
        }
        if (keyid2.length() > 0) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append("keyid2=").append(safeSQL.addVar(keyid2)).append("");
        }
        if (keyid3.length() > 0) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            where.append("keyid3=").append(safeSQL.addVar(keyid3)).append("");
        }
        if (tempid.length() > 0) {
            if (where.length() > 0) {
                where.append(" AND ");
            }
            if (tempid.indexOf(";") > -1) {
                where.append(" tempid IN (").append(safeSQL.addIn(tempid, ";")).append(") ");
            } else {
                where.append(" tempid=").append(safeSQL.addVar(tempid)).append(" ");
            }
        }
        sql.append(where);
        sql.append(" ORDER BY moddt ");
        DataSet temps = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues(), true);
        if (temps != null) {
            props.setProperty(PROPERTY_TEMPID, temps.getColumnValues(PROPERTY_TEMPID, ";"));
            props.setProperty(PROPERTY_MODBY, temps.getColumnValues(PROPERTY_MODBY, ";"));
            props.setProperty(PROPERTY_MODDT, temps.getColumnValues(PROPERTY_MODDT, ";"));
            props.setProperty(PROPERTY_COUNT, "" + temps.getRowCount());
            if (full) {
                for (int i = 0; i < temps.getRowCount(); ++i) {
                    String clob = temps.getClob(i, PROPERTY_TEMPVALUE, "");
                    props.setProperty(PROPERTY_TEMPVALUE + (i + 1), clob);
                }
            }
        } else {
            throw new SapphireException("Could not obtain data.");
        }
        int out = temps.getRowCount();
        return out;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String mode = properties.getProperty(PROPERTY_MODE, MODE_LIST);
        String sdcid = properties.getProperty(PROPERTY_SDCID, "");
        String keyid1 = properties.getProperty(PROPERTY_KEYID1, "");
        String keyid2 = properties.getProperty(PROPERTY_KEYID2, "");
        String keyid3 = properties.getProperty(PROPERTY_KEYID3, "");
        String tempid = properties.getProperty(PROPERTY_TEMPID, properties.getProperty(PROPERTY_ID, ""));
        if (mode.equalsIgnoreCase(MODE_LIST)) {
            if (this.list(sdcid, keyid1, keyid2, keyid3, tempid, false, properties) <= -1) return;
            return;
        } else if (mode.equalsIgnoreCase(MODE_LISTFULL)) {
            if (this.list(sdcid, keyid1, keyid2, keyid3, tempid, true, properties) <= -1) return;
            return;
        } else if (mode.equalsIgnoreCase(MODE_ADD)) {
            if (tempid.length() <= 0) throw new SapphireException("Temp Id required.");
            this.add(sdcid, keyid1, keyid2, keyid3, StringUtil.split(tempid, ";"), properties);
            return;
        } else if (mode.equalsIgnoreCase(MODE_UPDATE)) {
            if (tempid.length() <= 0) throw new SapphireException("Temp Id required.");
            this.update(sdcid, keyid1, keyid2, keyid3, tempid, properties);
            return;
        } else if (mode.equalsIgnoreCase(MODE_CHECKUPDATE)) {
            if (tempid.length() <= 0) throw new SapphireException("Temp Id required.");
            int rows = this.list(sdcid, keyid1, keyid2, keyid3, tempid, false, properties);
            if (rows <= -1) return;
            if (rows == 0) {
                this.add(sdcid, keyid1, keyid2, keyid3, StringUtil.split(tempid, ";"), properties);
                return;
            } else {
                this.update(sdcid, keyid1, keyid2, keyid3, tempid, properties);
            }
            return;
        } else {
            if (!mode.equalsIgnoreCase(MODE_REMOVE)) throw new SapphireException("Invalid mode provided.");
            this.remove(sdcid, keyid1, keyid2, keyid3, tempid);
        }
    }
}

