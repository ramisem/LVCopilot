/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.util.StringUtil;

public class SDIAliasPropertyHandler
extends BaseDetailPropertyHandler {
    public static String LABVANTAGE_CVS_ID = "$Revision: 58185 $";

    @Override
    protected void saveData() throws SapphireException {
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        List columnsList = elementData.getColumnList();
        if (!columnsList.contains("usersequence") && this._TableMD.doesColumnExists("usersequence")) {
            elementData.addColumn("usersequence");
        }
        String sdcid = this.getSdcId();
        String keyid1 = this.getKeyid1();
        String keyid2 = this.getKeyid2();
        String keyid3 = this.getKeyid3();
        String removedkeys = (String)this._ElementProps.get("eremove");
        ActionBlock ab = new ActionBlock();
        this.addDeleteAction(ab, removedkeys, sdcid, keyid1, keyid2, keyid3);
        this.addAddAction(ab, elementData, sdcid, keyid1, keyid2, keyid3);
        this.getActionProcessor().processActionBlock(ab);
    }

    private void addDeleteAction(ActionBlock ab, String removedkeys, String sdcid, String keyid1, String keyid2, String keyid3) throws SapphireException {
        HashMap<String, String> deleteAlias = new HashMap<String, String>();
        if (removedkeys != null && removedkeys.length() > 1) {
            StringBuffer aliasidbuf = new StringBuffer();
            StringBuffer aliastypebuf = new StringBuffer();
            StringBuffer keyid1buf = new StringBuffer();
            StringBuffer keyid2buf = new StringBuffer();
            StringBuffer keyid3buf = new StringBuffer();
            String[] keys = StringUtil.split(removedkeys, ";");
            for (int index = 0; index < keys.length - 1; ++index) {
                String[] row = StringUtil.split(keys[index], "|");
                if (row.length > 1) {
                    if (aliasidbuf.length() > 0) {
                        aliasidbuf.append(";").append(row[0]);
                        aliastypebuf.append(";").append(row[1]);
                        keyid1buf.append(";").append(keyid1);
                        keyid2buf.append(";").append(keyid2);
                        keyid3buf.append(";").append(keyid3);
                        continue;
                    }
                    aliasidbuf.append(row[0]);
                    aliastypebuf.append(row[1]);
                    keyid1buf.append(keyid1);
                    keyid2buf.append(keyid2);
                    keyid3buf.append(keyid3);
                    continue;
                }
                throw new SapphireException("AliasId and AliasType not provided for delete.");
            }
            if (aliasidbuf.length() > 0) {
                deleteAlias.put("sdcid", sdcid);
                deleteAlias.put("keyid1", keyid1buf.toString());
                deleteAlias.put("keyid2", keyid2buf.toString());
                deleteAlias.put("keyid3", keyid3buf.toString());
                deleteAlias.put("aliasid", aliasidbuf.toString());
                deleteAlias.put("aliastype", aliastypebuf.toString());
                ab.setAction("deletesdialias", "DeleteSDIAlias", "1", deleteAlias);
            }
        }
    }

    private void addAddAction(ActionBlock ab, ElementData elementData, String sdcid, String keyid1, String keyid2, String keyid3) throws SapphireException {
        HashMap<String, String> addAlias = new HashMap<String, String>();
        if (elementData.size() > 0) {
            StringBuffer aliasidbuf = new StringBuffer();
            StringBuffer aliastypebuf = new StringBuffer();
            StringBuffer keyid1buf = new StringBuffer();
            StringBuffer keyid2buf = new StringBuffer();
            StringBuffer keyid3buf = new StringBuffer();
            for (int i = 0; i < elementData.size(); ++i) {
                String __status = elementData.getColumnData(i, "__status");
                if (!__status.equalsIgnoreCase("N")) continue;
                String aliasid = elementData.getColumnData(i, "aliasid");
                String aliastype = elementData.getColumnData(i, "aliastype");
                if (aliasid != null && aliastype != null) {
                    if (aliasid.length() > 0 && aliastype.length() > 0) {
                        if (aliasidbuf.length() > 0) {
                            aliasidbuf.append(";").append(aliasid);
                            aliastypebuf.append(";").append(aliastype);
                            keyid1buf.append(";").append(keyid1);
                            keyid2buf.append(";").append(keyid2);
                            keyid3buf.append(";").append(keyid3);
                            continue;
                        }
                        aliasidbuf.append(aliasid);
                        aliastypebuf.append(aliastype);
                        keyid1buf.append(keyid1);
                        keyid2buf.append(keyid2);
                        keyid3buf.append(keyid3);
                        continue;
                    }
                    throw new SapphireException("AliasId and AliasType not provided.");
                }
                throw new SapphireException("Could not find AliasId and AliasType data.");
            }
            if (aliasidbuf.length() > 0) {
                addAlias.put("sdcid", sdcid);
                addAlias.put("keyid1", keyid1buf.toString());
                addAlias.put("keyid2", keyid2buf.toString());
                addAlias.put("keyid3", keyid3buf.toString());
                addAlias.put("aliasid", aliasidbuf.toString());
                addAlias.put("aliastype", aliastypebuf.toString());
                ab.setAction("addsdialias", "AddSDIAlias", "1", addAlias);
            }
        }
    }
}

