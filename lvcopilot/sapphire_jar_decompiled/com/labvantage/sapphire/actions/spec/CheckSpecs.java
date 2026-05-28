/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.spec;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataEntryAction;
import com.labvantage.sapphire.util.MiscUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CheckSpecs
extends BaseSDIDataEntryAction
implements sapphire.action.CheckSpecs {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        block24: {
            String specid = properties.getProperty("specid");
            String specversionid = properties.getProperty("specversionid");
            this.logger.debug("-- specid = " + specid);
            this.logger.debug("-- specversionid = " + specversionid);
            if (specid.length() > 0 && specversionid.length() > 0) {
                String[] specversionarray;
                String[] specidarray = StringUtil.split(specid, ";");
                if (specidarray.length == (specversionarray = StringUtil.split(specversionid, ";")).length) {
                    boolean propsmatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
                    String sdcid = properties.getProperty("sdcid");
                    String keyid1 = properties.getProperty("keyid1");
                    String keyid2 = properties.getProperty("keyid2");
                    String keyid3 = properties.getProperty("keyid3");
                    this.logger.debug("-- sdcid = " + sdcid);
                    this.logger.debug("-- keyid1 = " + keyid1);
                    this.logger.debug("-- keyid2 = " + keyid2);
                    this.logger.debug("-- keyid3 = " + keyid3);
                    if (sdcid.length() > 0 && keyid1.length() > 0) {
                        String[] keyid1array = StringUtil.split(keyid1, ";");
                        String[] keyid2array = null;
                        if (keyid2.length() > 0) {
                            keyid2array = StringUtil.split(keyid2, ";");
                        }
                        String[] keyid3array = null;
                        if (keyid3.length() > 0) {
                            keyid3array = StringUtil.split(keyid3, ";");
                        }
                        if (keyid2array != null && keyid2array.length != keyid1array.length || keyid3array != null && keyid3array.length != keyid3array.length) {
                            throw new SapphireException("keyid2(s) or kyid3(s) provided do not match keyid1(s) provided.");
                        }
                        if (propsmatch && keyid1array.length != specidarray.length) {
                            this.logger.debug("-- props match and array lengths do not match thus trim arrays");
                            if (keyid1array.length > specidarray.length) {
                                String[] newkeyid1array = new String[specidarray.length];
                                System.arraycopy(keyid1array, 0, newkeyid1array, 0, specidarray.length);
                                keyid1array = newkeyid1array;
                                if (keyid2array != null) {
                                    String[] newkeyid2array = new String[specidarray.length];
                                    System.arraycopy(keyid2array, 0, newkeyid2array, 0, specidarray.length);
                                    keyid2array = newkeyid2array;
                                }
                                if (keyid3array != null) {
                                    String[] newkeyid3array = new String[specidarray.length];
                                    System.arraycopy(keyid3array, 0, newkeyid3array, 0, specidarray.length);
                                    keyid3array = newkeyid3array;
                                }
                                this.logger.debug("-- keyid1 array trimmed");
                            } else if (specidarray.length > keyid1array.length) {
                                String[] newspecidarray = new String[keyid1array.length];
                                System.arraycopy(specidarray, 0, newspecidarray, 0, keyid1array.length);
                                specidarray = newspecidarray;
                                String[] newspecversionarray = new String[keyid1array.length];
                                System.arraycopy(specversionarray, 0, newspecversionarray, 0, keyid1array.length);
                                specversionarray = newspecversionarray;
                                this.logger.debug("-- specid array trimmed");
                            }
                        }
                        String rsetid = this.getRSet(sdcid, keyid1, keyid2, keyid3, false);
                        String selectds = "SELECT\tsdidata.* FROM\tsdidata, rsetitems WHERE\tsdidata.sdcid = rsetitems.sdcid AND \t\tsdidata.keyid1 = rsetitems.keyid1 AND \t\tsdidata.keyid2 = rsetitems.keyid2 AND \t\tsdidata.keyid3 = rsetitems.keyid3 AND \t\trsetid = ?";
                        String selectdi = "SELECT\tsdidataitem.* FROM\tsdidataitem, rsetitems WHERE\tsdidataitem.sdcid = rsetitems.sdcid AND \t\tsdidataitem.keyid1 = rsetitems.keyid1 AND \t\tsdidataitem.keyid2 = rsetitems.keyid2 AND \t\tsdidataitem.keyid3 = rsetitems.keyid3 AND \t\trsetid = ?";
                        this.database.createPreparedResultSet("sdidata", selectds, new Object[]{rsetid});
                        this.database.createPreparedResultSet("sdidataitems", selectdi, new Object[]{rsetid});
                        DataSet sdidataitem = new DataSet(this.database.getResultSet("sdidataitems"));
                        DataSet sdidata = new DataSet(this.database.getResultSet("sdidata"));
                        if (sdidataitem.size() > 0) {
                            QueryProcessor qp = this.getQueryProcessor();
                            SafeSQL safeSQL = new SafeSQL();
                            String sql = "SELECT specid, specversionid, paramlistid, paramlistversionid, variantid, paramid, paramtype from specparamlimits WHERE specid IN (" + safeSQL.addIn(specid, ";") + ") AND specversionid IN (" + safeSQL.addIn(specversionid, ";") + ")";
                            DataSet specparamlimits = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
                            if (specparamlimits != null) {
                                if (specparamlimits.size() > 0) {
                                    DataSet sdidataitemspec = this.createSDIDataItemSpec(sdcid, keyid1array, keyid2array, keyid3array, specidarray, specversionarray, propsmatch, sdidataitem, specparamlimits);
                                    HashSet<String> specmap = new HashSet<String>();
                                    for (int i = 0; i < specidarray.length; ++i) {
                                        if (StringUtil.getLen(specidarray[i]) <= 0L) continue;
                                        specmap.add(specidarray[i] + ";" + specversionarray[i]);
                                    }
                                    try {
                                        this.checkSpecs(sdidataitem, sdidataitemspec, specmap);
                                        DataSet sdispec = this.createSDISpec(sdcid, keyid1array, keyid2array, keyid3array, specidarray, specversionarray, propsmatch);
                                        this.logger.debug("sdispec -- size = " + sdispec.size());
                                        try {
                                            PropertyList specRuleEvalPolicyPL = this.getSpecRulesPolicy();
                                            DataSet filteredSpecRuleEvalDataItems = new DataSet();
                                            filteredSpecRuleEvalDataItems = sdidataitemspec.getRowCount() > 0 && this.isSpecFilterRequired(specRuleEvalPolicyPL) ? this.filterSpecDataItemBasedOnPolicy(specRuleEvalPolicyPL, sdidata, sdidataitem, sdidataitemspec) : sdidataitemspec;
                                            this.checkSpecRules(this.database, sdispec, filteredSpecRuleEvalDataItems, sdidataitem);
                                            StringBuffer outspecid = new StringBuffer();
                                            StringBuffer outspecversionid = new StringBuffer();
                                            StringBuffer outkeyid1 = new StringBuffer();
                                            StringBuffer outkeyid2 = new StringBuffer();
                                            StringBuffer outkeyid3 = new StringBuffer();
                                            StringBuffer outcond = new StringBuffer();
                                            for (int index = 0; index < sdispec.getRowCount(); ++index) {
                                                String thekeyid3;
                                                MiscUtil.MiscString.appendDelimeteredString(outspecid, sdispec.getString(index, "specid", ""), ";");
                                                MiscUtil.MiscString.appendDelimeteredString(outspecversionid, sdispec.getString(index, "specversionid", ""), ";");
                                                MiscUtil.MiscString.appendDelimeteredString(outcond, sdispec.getString(index, "condition", ""), ";", index);
                                                MiscUtil.MiscString.appendDelimeteredString(outkeyid1, sdispec.getString(index, "keyid1", ""), ";");
                                                String thekeyid2 = sdispec.getString(index, "keyid2", "(null)");
                                                if (thekeyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                                                    MiscUtil.MiscString.appendDelimeteredString(outkeyid2, keyid2, ";");
                                                }
                                                if ((thekeyid3 = sdispec.getString(index, "keyid3", "(null)")).length() <= 0 || keyid3.equalsIgnoreCase("(null)")) continue;
                                                MiscUtil.MiscString.appendDelimeteredString(outkeyid3, keyid3, ";");
                                            }
                                            this.logger.debug("-- outspecid = " + outspecid.toString() + " --");
                                            properties.put("outspecid", outspecid.toString());
                                            this.logger.debug("-- outspecversionid = " + outspecversionid.toString() + " --");
                                            properties.put("outspecversionid", outspecversionid.toString());
                                            this.logger.debug("-- outkeyid1 = " + outkeyid1.toString() + " --");
                                            properties.put("outkeyid1", outkeyid1.toString());
                                            if (outkeyid2.length() > 0) {
                                                this.logger.debug("-- outkeyid2 = " + outkeyid2.toString() + " --");
                                                properties.put("outkeyid2", outkeyid2.toString());
                                            }
                                            if (outkeyid3.length() > 0) {
                                                this.logger.debug("-- outkeyid3 = " + outkeyid3.toString() + " --");
                                                properties.put("outkeyid3", outkeyid3.toString());
                                            }
                                            this.logger.debug("-- outcondition = " + outcond.toString() + " --");
                                            properties.put("outcondition", outcond.toString());
                                            break block24;
                                        }
                                        catch (Exception e) {
                                            throw new SapphireException("Failed to check the specification rules. Reason:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                                        }
                                    }
                                    catch (Exception e) {
                                        throw new SapphireException("Failed to check the specification. Reason:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                                    }
                                }
                                throw new SapphireException("No spec data was found.");
                            }
                            throw new SapphireException("Could not obtain spec data.");
                        }
                        throw new SapphireException("No sdidata and/or sdidataitems could be found.");
                    }
                    throw new SapphireException("No sdcid or keyid1 provided.");
                }
                throw new SapphireException("The spec id(s) provided do not match the spec version(s) provided.");
            }
            throw new SapphireException("No spec id(s) or spec version(s) provided.");
        }
    }

    protected DataSet createSDISpec(String sdcid, String[] keyid1, String[] keyid2, String[] keyid3, String[] specid, String[] specversionid, boolean propsmatch) {
        DataSet sdispec = new DataSet();
        sdispec.addColumn("sdcid", 0);
        sdispec.addColumn("keyid1", 0);
        sdispec.addColumn("keyid2", 0);
        sdispec.addColumn("keyid3", 0);
        sdispec.addColumn("specid", 0);
        sdispec.addColumn("specversionid", 0);
        sdispec.addColumn("decheckflag", 0);
        sdispec.addColumn("waivedflag", 0);
        sdispec.addColumn("condition", 0);
        sdispec.addColumn("_modifiedtotal", 0);
        sdispec.setString(-1, "_modifiedtotal", "N");
        for (int keyindex = 0; keyindex < keyid1.length; ++keyindex) {
            if (propsmatch && keyid1.length == specid.length) {
                this.logger.debug("-- props match and array lengths match thus add matched spec");
                int sdispec_row = sdispec.addRow();
                sdispec.setString(sdispec_row, "sdcid", sdcid);
                sdispec.setString(sdispec_row, "keyid1", keyid1[keyindex]);
                if (keyid2 != null && keyid2.length == keyid1.length) {
                    sdispec.setString(sdispec_row, "keyid2", keyid2[keyindex]);
                    if (keyid3 != null && keyid3.length == keyid1.length) {
                        sdispec.setString(sdispec_row, "keyid3", keyid3[keyindex]);
                    } else {
                        sdispec.setString(sdispec_row, "keyid3", "(null)");
                    }
                } else {
                    sdispec.setString(sdispec_row, "keyid2", "(null)");
                    sdispec.setString(sdispec_row, "keyid3", "(null)");
                }
                sdispec.setString(sdispec_row, "specid", specid[keyindex]);
                sdispec.setString(sdispec_row, "specversionid", specversionid[keyindex]);
                sdispec.setString(sdispec_row, "waivedflag", "N");
                sdispec.setString(sdispec_row, "decheckflag", "Y");
                continue;
            }
            this.logger.debug("-- props match false or array lengths do not match thus add all specs");
            for (int specindex = 0; specindex < specid.length; ++specindex) {
                int sdispec_row = sdispec.addRow();
                sdispec.setString(sdispec_row, "sdcid", sdcid);
                sdispec.setString(sdispec_row, "keyid1", keyid1[keyindex]);
                if (keyid2 != null && keyid2.length == keyid1.length) {
                    sdispec.setString(sdispec_row, "keyid2", keyid2[keyindex]);
                    if (keyid3 != null && keyid3.length == keyid1.length) {
                        sdispec.setString(sdispec_row, "keyid3", keyid3[keyindex]);
                    } else {
                        sdispec.setString(sdispec_row, "keyid3", "(null)");
                    }
                } else {
                    sdispec.setString(sdispec_row, "keyid2", "(null)");
                    sdispec.setString(sdispec_row, "keyid3", "(null)");
                }
                sdispec.setString(sdispec_row, "specid", specid[specindex]);
                sdispec.setString(sdispec_row, "specversionid", specversionid[specindex]);
                sdispec.setString(sdispec_row, "waivedflag", "N");
                sdispec.setString(sdispec_row, "decheckflag", "Y");
            }
        }
        return sdispec;
    }

    protected DataSet createSDIDataItemSpec(String sdcid, String[] keyid1, String[] keyid2, String[] keyid3, String[] specid, String[] specversionid, boolean propsmatch, DataSet sdidataitem, DataSet specparamlimits) {
        DataSet sdidataitemspec = new DataSet();
        sdidataitemspec.addColumn("specid", 0);
        sdidataitemspec.addColumn("specversionid", 0);
        sdidataitemspec.addColumn("paramlistid", 0);
        sdidataitemspec.addColumn("paramlistversionid", 0);
        sdidataitemspec.addColumn("variantid", 0);
        sdidataitemspec.addColumn("paramid", 0);
        sdidataitemspec.addColumn("paramtype", 0);
        sdidataitemspec.addColumn("dataset", 1);
        sdidataitemspec.addColumn("replicateid", 1);
        sdidataitemspec.addColumn("keyid1", 0);
        sdidataitemspec.addColumn("keyid2", 0);
        sdidataitemspec.addColumn("keyid3", 0);
        sdidataitemspec.addColumn("sdcid", 0);
        sdidataitemspec.addColumn("limittypeid", 0);
        sdidataitemspec.addColumn("condition", 0);
        sdidataitemspec.addColumn("waivedflag", 0);
        sdidataitemspec.addColumn("displayvalue", 0);
        sdidataitemspec.addColumn("reportflag", 0);
        sdidataitemspec.addColumn("_modifiedtotal", 0);
        sdidataitemspec.setString(-1, "_modifiedtotal", "N");
        for (int sdidataitem_row = 0; sdidataitem_row < sdidataitem.getRowCount(); ++sdidataitem_row) {
            int keyindex;
            String sdidataitem_keyid1 = sdidataitem.getString(sdidataitem_row, "keyid1", "(null)");
            String sdidataitem_keyid2 = sdidataitem.getString(sdidataitem_row, "keyid2", "(null)");
            String sdidataitem_keyid3 = sdidataitem.getString(sdidataitem_row, "keyid3", "(null)");
            String sdidataitem_sdcid = sdidataitem.getString(sdidataitem_row, "sdcid", "");
            if (sdidataitem_sdcid.length() <= 0 || !sdidataitem_sdcid.equalsIgnoreCase(sdcid) || (keyindex = this.findKey(sdidataitem_keyid1, sdidataitem_keyid2, sdidataitem_keyid3, keyid1, keyid2, keyid3)) <= -1) continue;
            this.logger.debug("-- key found. keyindex - " + keyindex);
            String sdidataitem_paramlistid = sdidataitem.getString(sdidataitem_row, "paramlistid", "");
            String sdidataitem_paramlistversionid = sdidataitem.getString(sdidataitem_row, "paramlistversionid", "");
            String sdidataitem_variantid = sdidataitem.getString(sdidataitem_row, "variantid", "");
            String sdidataitem_paramid = sdidataitem.getString(sdidataitem_row, "paramid", "");
            String sdidataitem_paramtype = sdidataitem.getString(sdidataitem_row, "paramtype", "");
            BigDecimal sdidataitem_dataset = sdidataitem.getBigDecimal(sdidataitem_row, "dataset");
            BigDecimal sdidataitem_replicateid = sdidataitem.getBigDecimal(sdidataitem_row, "replicateid");
            HashMap<String, String> find = new HashMap<String, String>(10);
            find.put("paramid", sdidataitem_paramid);
            find.put("paramtype", sdidataitem_paramtype);
            int found = specparamlimits.findRow(find);
            while (found > -1) {
                this.logger.debug("-- row found - " + found);
                String specparamlimits_specid = specparamlimits.getString(found, "specid", "");
                String specparamlimits_specversionid = specparamlimits.getString(found, "specversionid", "");
                boolean cancontinue = false;
                if (propsmatch && keyid1.length == specid.length) {
                    this.logger.debug("-- Props match and arrays match thus add matched spec only");
                    if (specid[keyindex].equals(specparamlimits_specid)) {
                        this.logger.debug("-- Spec " + specparamlimits_specid + " in matched array thus continue...");
                        cancontinue = true;
                    } else {
                        this.logger.debug("-- Spec " + specparamlimits_specid + " not in matched array thus do not continue...");
                    }
                } else {
                    this.logger.debug("-- Props match is false or arrays do not match thus add all specs");
                    for (int i = 0; i < specid.length; ++i) {
                        if (!specid[i].equals(specparamlimits_specid)) continue;
                        cancontinue = true;
                        this.logger.debug("-- Spec " + specparamlimits_specid + " found in array thus continue...");
                        break;
                    }
                }
                this.logger.debug("-- cancontinue - " + cancontinue);
                if (cancontinue) {
                    int sdidataitemspec_row = sdidataitemspec.addRow();
                    sdidataitemspec.setString(sdidataitemspec_row, "paramlistid", sdidataitem_paramlistid);
                    sdidataitemspec.setString(sdidataitemspec_row, "paramlistversionid", sdidataitem_paramlistversionid);
                    sdidataitemspec.setString(sdidataitemspec_row, "variantid", sdidataitem_variantid);
                    sdidataitemspec.setString(sdidataitemspec_row, "paramid", sdidataitem_paramid);
                    sdidataitemspec.setString(sdidataitemspec_row, "paramtype", sdidataitem_paramtype);
                    sdidataitemspec.setNumber(sdidataitemspec_row, "dataset", sdidataitem_dataset);
                    sdidataitemspec.setNumber(sdidataitemspec_row, "replicateid", sdidataitem_replicateid);
                    sdidataitemspec.setString(sdidataitemspec_row, "sdcid", sdidataitem_sdcid);
                    sdidataitemspec.setString(sdidataitemspec_row, "keyid1", sdidataitem_keyid1);
                    sdidataitemspec.setString(sdidataitemspec_row, "keyid2", sdidataitem_keyid2);
                    sdidataitemspec.setString(sdidataitemspec_row, "keyid3", sdidataitem_keyid3);
                    sdidataitemspec.setString(sdidataitemspec_row, "specid", specparamlimits_specid);
                    sdidataitemspec.setString(sdidataitemspec_row, "specversionid", specparamlimits_specversionid);
                    sdidataitemspec.setString(sdidataitemspec_row, "reportflag", "N");
                    sdidataitemspec.setString(sdidataitemspec_row, "waivedflag", "N");
                    sdidataitemspec.setString(sdidataitemspec_row, "displayvalue", sdidataitem.getString(sdidataitem_row, "displayvalue", ""));
                }
                found = specparamlimits.findRow(find, found + 1);
            }
        }
        return sdidataitemspec;
    }

    private int findKey(String keyid1, String keyid2, String keyid3, String[] keyid1array, String[] keyid2array, String[] keyid3array) {
        int found = -1;
        this.logger.debug("-- findKey - keyid1 - " + keyid1);
        this.logger.debug("-- findKey - keyid2 - " + keyid2);
        this.logger.debug("-- findKey - keyid3 - " + keyid3);
        for (int index = 0; index < keyid1array.length; ++index) {
            if (!keyid1.equals(keyid1array[index])) continue;
            if (keyid2 != null && !keyid2.equals("(null)") && keyid2array != null) {
                if (!keyid2.equals(keyid2array[index])) continue;
                if (keyid3 != null && !keyid3.equals("(null)") && keyid3array != null) {
                    if (!keyid3.equals(keyid3array[index])) continue;
                    found = index;
                    break;
                }
                found = index;
                break;
            }
            found = index;
            break;
        }
        this.logger.debug("-- findKey - found - " + found);
        return found;
    }
}

