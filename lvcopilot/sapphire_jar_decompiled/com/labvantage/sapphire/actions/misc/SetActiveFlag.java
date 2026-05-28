/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SetActiveFlag
extends BaseSDIAction
implements sapphire.action.SetActiveFlag {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList props) throws SapphireException {
        block10: {
            block9: {
                String sdcid = props.getProperty("sdcid");
                if (StringUtil.getLen(sdcid) <= 0L) break block9;
                HashMap sdcprops = this.getSDCProcessor().getSDCProperties(sdcid);
                if (!"Y".equals(sdcprops.get("activeableflag"))) break block10;
                if ("Y".equals(sdcprops.get("versionedflag"))) {
                    String tableid = (String)sdcprops.get("tableid");
                    String keycolid1 = (String)sdcprops.get("keycolid1");
                    String keycolid2 = (String)sdcprops.get("keycolid2");
                    String keycolid3 = (String)sdcprops.get("keycolid3");
                    if (StringUtil.getLen(keycolid2) > 0L) {
                        String rsetid;
                        block8: {
                            rsetid = null;
                            boolean hasKeycol3 = StringUtil.getLen(keycolid3) > 0L;
                            try {
                                rsetid = this.getDAMProcessor().createRSet(sdcid, props.getProperty("keyid1"), props.getProperty("keyid2"), props.getProperty("keyid3"));
                                SafeSQL safeSQL = new SafeSQL();
                                StringBuilder sql = new StringBuilder();
                                sql.append("select t.").append(keycolid1).append(", t.").append(keycolid2);
                                if (hasKeycol3) {
                                    sql.append(", t.").append(keycolid3);
                                }
                                sql.append(" from ").append(tableid).append(" t, rsetitems r");
                                sql.append(" where t.").append(keycolid1).append(" = r.keyid1");
                                if (hasKeycol3) {
                                    sql.append(" and t.").append(keycolid3).append(" = r.keyid3");
                                }
                                sql.append(" and r.rsetid = ").append(safeSQL.addVar(rsetid));
                                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                                if (ds == null || ds.size() <= 0) break block8;
                                props.setProperty("keyid1", ds.getColumnValues(keycolid1, ";"));
                                props.setProperty("keyid2", ds.getColumnValues(keycolid2, ";"));
                                if (!hasKeycol3) break block8;
                                props.setProperty("keyid3", ds.getColumnValues(keycolid3, ";"));
                            }
                            catch (Throwable throwable) {
                                if (StringUtil.getLen(rsetid) > 0L) {
                                    this.getDAMProcessor().clearRSet(rsetid);
                                }
                                throw throwable;
                            }
                        }
                        if (StringUtil.getLen(rsetid) > 0L) {
                            this.getDAMProcessor().clearRSet(rsetid);
                        }
                    }
                }
                this.setSDIValue("SetActiveFlag", "activeflag", props.getProperty("activeflag"), props);
                break block10;
            }
            throw new SapphireException("INVALID_PROPERTY", "SDCID not defined.");
        }
    }
}

