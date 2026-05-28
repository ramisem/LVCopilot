/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.gwt.server;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.I18nUtil;
import com.labvantage.sapphire.RequestParser;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.pageelements.gwt.server.GWTDataEntry;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataItemCrossTabModel {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private PropertyList dataEntryPolicy;
    private PropertyList element;
    private ConnectionInfo connectionInfo;
    private HashMap datasetKeyIndexMap = new HashMap();
    private HashMap primaryKeyIndexMap = new HashMap();
    private String[] cHeaderColids;
    private String[] rHeaderColids;
    private static Cache userCertificationCache = new Cache("User Certification Cache", 100);
    private boolean isInitialRendering = true;
    private int totalItemCount;
    private DataSet processedDataitems;
    private HashMap<String, String> specconditionMap = null;
    private HashMap<String, Integer> worstConditionMap = null;
    private static final String SPEC_DISPLAY_OPTION_FIRST_MATCH = "0";
    private static final String SPEC_DISPLAY_OPTION_WORSET_MATCH = "1";
    private String spec_displayoption = "0";
    private String passedinSpecID = "";
    private String passedinSpecVersionID = "";
    private String sortbyKeyid1list = null;
    private HashMap<String, String> hasnotesMap = new HashMap();

    public DataItemCrossTabModel(String connectionid) {
        this.isInitialRendering = false;
        this.connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
    }

    public DataItemCrossTabModel(PropertyList element, PropertyList dataEntryPolicy, String connectionid, DataSet sdinotes) {
        this.element = element;
        this.dataEntryPolicy = dataEntryPolicy;
        if (dataEntryPolicy == null) {
            this.isInitialRendering = false;
        }
        this.connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
        if (sdinotes != null) {
            for (int i = 0; i < sdinotes.getRowCount(); ++i) {
                this.hasnotesMap.put(sdinotes.getValue(i, "context"), "Y");
            }
        }
    }

    public DataItemCrossTabModel(PropertyList element, PropertyList dataEntryPolicy, String connectionid) {
        this.element = element;
        this.dataEntryPolicy = dataEntryPolicy;
        if (dataEntryPolicy == null) {
            this.isInitialRendering = false;
        }
        this.connectionInfo = new ConnectionProcessor(connectionid).getConnectionInfo(connectionid);
    }

    public void setPassedinSpec(String passedinSpecID, String passsedinSpecVersionid) {
        this.passedinSpecID = passedinSpecID;
        this.passedinSpecVersionID = passsedinSpecVersionid;
    }

    public void setSortbyKeyid1list(String keyid1list) {
        this.sortbyKeyid1list = keyid1list;
    }

    public DataSet getProcessedDataitems() {
        return this.processedDataitems;
    }

    public HashMap getDatasetKeyIndexMap() {
        return this.datasetKeyIndexMap;
    }

    public HashMap getPrimaryKeyIndexMap() {
        return this.primaryKeyIndexMap;
    }

    public JSONObject toJSONObjectCrossTabModel(DataSet dataitems, DataSet dataset, DataSet primary, DataSet dataspecs, String[] cHeaderColids, String[] rHeaderColids) throws JSONException, SapphireException {
        this.cHeaderColids = cHeaderColids;
        this.rHeaderColids = rHeaderColids;
        return this.toJSONObjectCrossTabModel(dataitems, dataset, primary, dataspecs);
    }

    public JSONObject toJSONObjectCrossTabModel(DataSet dataitems, DataSet dataset, DataSet primary, DataSet dataspecs) throws JSONException, SapphireException {
        PropertyList filterPL;
        PropertyListCollection filterColumns;
        int i;
        String[] cheaders = new String[]{"paramlistid", "paramid", "paramtype", "variantid", "replicateid"};
        String[] cheadersTrans = new String[]{"Y", "Y", "Y", "Y", "N"};
        String[] rheaders = new String[]{"keyid1", "dataset"};
        String[] rheadersTrans = new String[]{"N", "N"};
        boolean cSortByDSUserSeq = true;
        boolean cSortByDIUserSeq = true;
        boolean rSortByDSUserSeq = true;
        boolean rSortByDIUserSeq = true;
        String[] additionalcolids = null;
        I18nUtil.localizeDisplayValues(dataitems, this.connectionInfo);
        if (this.dataEntryPolicy == null) {
            this.dataEntryPolicy = GWTDataEntry.getDataEntryPolicy(this.connectionInfo.getConnectionId(), this.element);
        }
        this.spec_displayoption = this.dataEntryPolicy.getProperty("specconditiondisplayoption");
        if (SPEC_DISPLAY_OPTION_WORSET_MATCH.equals(this.spec_displayoption)) {
            this.worstConditionMap = new HashMap();
            PropertyListCollection specconditions = this.dataEntryPolicy.getCollectionNotNull("SpecConditions");
            for (int i2 = 0; i2 < specconditions.size(); ++i2) {
                if (specconditions.getPropertyList(i2).getProperty("SpecCond").length() <= 0) continue;
                this.worstConditionMap.put(specconditions.getPropertyList(i2).getProperty("SpecCond"), new Integer(i2));
            }
        }
        HashMap userCertCache = null;
        if (this.connectionInfo != null) {
            userCertCache = this.getUserCertMap(dataset, false);
        }
        PropertyListCollection visualcues = this.dataEntryPolicy.getCollection("visualpolicies");
        String sdcid = dataitems.getValue(0, "sdcid") != null && dataitems.getValue(0, "sdcid").length() > 0 ? dataitems.getValue(0, "sdcid") : "Sample";
        SDCProcessor sdcProcessor = new SDCProcessor(this.connectionInfo.getConnectionId());
        TranslationProcessor tp = null;
        if (this.connectionInfo.getLanguage() != null && this.connectionInfo.getLanguage().length() > 0) {
            tp = new TranslationProcessor(this.connectionInfo.getConnectionId());
        }
        String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
        String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
        String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
        int keycolumns = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
        if (this.element != null) {
            String dataitemdisplayRule;
            PropertyListCollection additionalcolumns;
            int i3;
            if (this.element.getPropertyList("columnheader") != null) {
                PropertyListCollection cheadercols = this.element.getPropertyList("columnheader").getCollection("columns");
                cSortByDSUserSeq = !"N".equals(this.element.getPropertyList("columnheader").getProperty("sortbydatasetusersequence"));
                boolean bl = cSortByDIUserSeq = !"N".equals(this.element.getPropertyList("columnheader").getProperty("sortbydataitemusersequence"));
                if (cheadercols != null && cheadercols.size() > 0) {
                    cheaders = new String[cheadercols.size()];
                    cheadersTrans = new String[cheadercols.size()];
                    for (i3 = 0; i3 < cheaders.length; ++i3) {
                        cheaders[i3] = cheadercols.getPropertyList(i3).getProperty("columnid");
                        cheadersTrans[i3] = cheadercols.getPropertyList(i3).getProperty("translatevalue");
                    }
                }
            }
            PropertyListCollection rheadercols = this.element.getPropertyList("rowheader").getCollection("columns");
            rSortByDSUserSeq = !"N".equals(this.element.getPropertyList("rowheader").getProperty("sortbydatasetusersequence"));
            boolean bl = rSortByDIUserSeq = !"N".equals(this.element.getPropertyList("rowheader").getProperty("sortbydataitemusersequence"));
            if (rheadercols != null && rheadercols.size() > 0) {
                rheaders = new String[rheadercols.size()];
                rheadersTrans = new String[rheadercols.size()];
                for (i3 = 0; i3 < rheaders.length; ++i3) {
                    rheaders[i3] = rheadercols.getPropertyList(i3).getProperty("columnid");
                    rheadersTrans[i3] = rheadercols.getPropertyList(i3).getProperty("translatevalue");
                }
            }
            if ((additionalcolumns = this.element.getCollection("columns")) != null && additionalcolumns.size() > 0) {
                additionalcolids = new String[additionalcolumns.size()];
                for (i3 = 0; i3 < additionalcolids.length; ++i3) {
                    String columnid = additionalcolumns.getPropertyList(i3).getProperty("columnid");
                    if (columnid.indexOf(" ") > 0) {
                        columnid = RequestParser.parseAlias(columnid);
                        additionalcolumns.getPropertyList(i3).setProperty("columnid", columnid);
                    }
                    additionalcolids[i3] = columnid;
                }
            }
            if ((dataitemdisplayRule = this.element.getProperty("dataitemdisplayrule")).length() > 0 || dataset.findRow("blockflag", "Y") >= 0) {
                this.datasetKeyIndexMap = DataItemCrossTabModel.getDatasetKeyIndexMap(dataset);
                this.primaryKeyIndexMap = DataItemCrossTabModel.getPrimaryKeyIndexMap(primary, keycolid1, keycolid2, keycolid3);
                this.processedDataitems = this.evaluateItemProtectionRule(sdcid, dataitems, dataitemdisplayRule, keycolumns);
            } else {
                this.processedDataitems = dataitems;
            }
            this.totalItemCount = this.processedDataitems.getRowCount();
            int maxitemlimit = -1;
            try {
                maxitemlimit = this.element.getProperty("maxitemcount").length() > 0 ? Integer.parseInt(this.element.getProperty("maxitemcount")) : -1;
            }
            catch (Exception exception) {
                // empty catch block
            }
            int totalitemcount = this.processedDataitems.getRowCount();
            if (maxitemlimit > 0 && totalitemcount > maxitemlimit) {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("filtermet", "Y");
                for (int i4 = maxitemlimit; i4 < totalitemcount; ++i4) {
                    this.processedDataitems.setValue(i4, "filtermet", "N");
                }
                this.processedDataitems = this.processedDataitems.getFilteredDataSet(filter);
            }
            if (this.processedDataitems == null || this.processedDataitems.getRowCount() == 0) {
                throw new SapphireException("No qualified data items found. Probably all filtered out by data item display rule.");
            }
        } else {
            this.processedDataitems = dataitems;
            cheaders = this.cHeaderColids;
            rheaders = this.rHeaderColids;
            cheadersTrans = new String[cheaders.length];
            rheadersTrans = new String[rheaders.length];
        }
        ArrayList<PropertyList> conditions = new ArrayList<PropertyList>();
        HashMap specFind = new HashMap();
        ArrayList<String> attrList = new ArrayList<String>();
        attrList.add("keyid1");
        attrList.add("keyid2");
        attrList.add("keyid3");
        attrList.add("paramlistid");
        attrList.add("paramlistversionid");
        attrList.add("variantid");
        attrList.add("dataset");
        attrList.add("paramid");
        attrList.add("paramtype");
        attrList.add("replicateid");
        attrList.add("valuestatus");
        attrList.add("protection");
        attrList.add("displayvalue");
        attrList.add("displayunits");
        attrList.add("enteredtext");
        attrList.add("datatypes");
        attrList.add("releasedflag");
        attrList.add("mandatoryflag");
        attrList.add("entrysdcid");
        attrList.add("entryreftypeid");
        attrList.add("__lockedby");
        attrList.add("isusercertified");
        attrList.add("s_acoverriddenflag");
        attrList.add("s_acoverriddenflagoriginal");
        attrList.add("calcexcludeflag");
        attrList.add("instrumentid");
        attrList.add("instrumentfieldid");
        attrList.add("sdidata.blockflag");
        attrList.add("defaultvalue");
        attrList.add("paramlistitem.displaywidth");
        attrList.add("hasdataitemnotes");
        attrList.add("hasdatasetnotes");
        attrList.add("uncertaintydisplayvalue");
        attrList.add("uncertaintydisplayvalueupper");
        attrList.add("paramlistitem.editorstyleid");
        attrList.add("samplestatus");
        this.processedDataitems.addColumn("speccondition", 0);
        this.processedDataitems.setValue(0, "speccondition", "");
        this.processedDataitems.addColumn("isusercertified", 0);
        this.processedDataitems.setValue(0, "isusercertified", "");
        this.processedDataitems.addColumn("hasdataitemnotes", 0);
        this.processedDataitems.setValue(0, "hasdataitemnotes", "");
        this.processedDataitems.addColumn("hasdatasetnotes", 0);
        this.processedDataitems.setValue(0, "hasdatasetnotes", "");
        for (int i5 = 0; i5 < visualcues.size(); ++i5) {
            PropertyListCollection values;
            String columnid = visualcues.getPropertyList(i5).getProperty("columnid");
            if ("speccondition".equals(columnid) && (values = visualcues.getPropertyList(i5).getCollection("values")).size() > 0) {
                for (int v = 0; v < values.size(); ++v) {
                    conditions.add(values.getPropertyList(v));
                }
            }
            if (attrList.contains(columnid)) continue;
            attrList.add(columnid);
        }
        for (int rh = 0; rh < rheaders.length; ++rh) {
            if (!attrList.contains(rheaders[rh])) {
                attrList.add(rheaders[rh]);
            }
            if (rheaders[rh].indexOf("primary.") == 0) {
                this.processedDataitems.addColumn(rheaders[rh], primary.getColumnType(rheaders[rh].substring(8)));
                continue;
            }
            if (rheaders[rh].indexOf("sdidata.") != 0) continue;
            int coltype = dataset.getColumnType(rheaders[rh].substring(8));
            this.processedDataitems.addColumn(rheaders[rh], coltype);
        }
        for (int ch = 0; ch < cheaders.length; ++ch) {
            if (!attrList.contains(cheaders[ch])) {
                attrList.add(cheaders[ch]);
            }
            if (cheaders[ch].indexOf("primary.") == 0) {
                this.processedDataitems.addColumn(cheaders[ch], primary.getColumnType(cheaders[ch].substring(8)));
                continue;
            }
            if (cheaders[ch].indexOf("sdidata.") != 0) continue;
            this.processedDataitems.addColumn(cheaders[ch], dataset.getColumnType(cheaders[ch].substring(8)));
        }
        if (additionalcolids != null) {
            for (int c = 0; c < additionalcolids.length; ++c) {
                if (attrList.contains(additionalcolids[c])) continue;
                attrList.add(additionalcolids[c]);
                if (additionalcolids[c].indexOf("primary.") == 0) {
                    this.processedDataitems.addColumn(additionalcolids[c], primary.getColumnType(additionalcolids[c].substring(8)));
                    continue;
                }
                if (additionalcolids[c].indexOf("sdidata.") != 0) continue;
                this.processedDataitems.addColumn(additionalcolids[c], dataset.getColumnType(additionalcolids[c].substring(8)));
            }
        }
        JSONObject jsonKeyIndex = new JSONObject();
        JSONObject jsonItemKeyHashIndex = new JSONObject();
        JSONArray jsonItemKeyArray = new JSONArray();
        JSONObject colMap = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONArray columnidArray = new JSONArray();
        JSONObject headerTranslationMap = new JSONObject();
        String[] columns = new String[attrList.size()];
        for (int col = 0; col < attrList.size(); ++col) {
            columns[col] = (String)attrList.get(col);
        }
        HashMap<String, Integer> datasetKeyRowMap = new HashMap<String, Integer>();
        HashMap<String, Integer> primaryKeyRowMap = new HashMap<String, Integer>();
        int rows = dataset.getRowCount();
        for (i = 0; i < rows; ++i) {
            if (dataset.getValue(i, "displayunderparamlistname").length() == 0) {
                dataset.setValue(i, "displayunderparamlistname", dataset.getValue(i, "paramlistid"));
            }
            if (dataset.getValue(i, "displayundervariantname").length() == 0) {
                dataset.setValue(i, "displayundervariantname", dataset.getValue(i, "variantid"));
            }
            datasetKeyRowMap.put(dataset.getValue(i, "keyid1") + ";" + dataset.getValue(i, "keyid2") + ";" + dataset.getValue(i, "keyid3") + ";" + dataset.getValue(i, "paramlistid") + ";" + dataset.getValue(i, "paramlistversionid") + ";" + dataset.getValue(i, "variantid") + ";" + dataset.getValue(i, "dataset"), new Integer(i));
        }
        rows = primary.getRowCount();
        for (i = 0; i < rows; ++i) {
            primaryKeyRowMap.put(primary.getValue(i, keycolid1) + (keycolumns >= 2 ? ";" + (primary.getValue(i, keycolid2).length() == 0 ? "(null)" : primary.getValue(i, keycolid2)) : "") + (keycolumns == 3 ? ";" + (primary.getValue(i, keycolid3).length() == 0 ? "(null)" : primary.getValue(i, keycolid3)) : ""), new Integer(i));
        }
        int rowcount = this.processedDataitems.getRowCount();
        for (int i6 = 0; i6 < rowcount; ++i6) {
            String value;
            if (this.processedDataitems.getValue(i6, "displayunderparamname").length() == 0) {
                this.processedDataitems.setValue(i6, "displayunderparamname", this.processedDataitems.getValue(i6, "paramid"));
            }
            if (this.processedDataitems.getValue(i6, "displayunderparamtype").length() == 0) {
                this.processedDataitems.setValue(i6, "displayunderparamtype", this.processedDataitems.getValue(i6, "paramtype"));
            }
            JSONArray jsonObjRowArray = new JSONArray();
            int index = 0;
            String datasetkeys = this.processedDataitems.getValue(i6, "keyid1") + ";" + this.processedDataitems.getValue(i6, "keyid2") + ";" + this.processedDataitems.getValue(i6, "keyid3") + ";" + this.processedDataitems.getValue(i6, "paramlistid") + ";" + this.processedDataitems.getValue(i6, "paramlistversionid") + ";" + this.processedDataitems.getValue(i6, "variantid") + ";" + this.processedDataitems.getValue(i6, "dataset");
            String dataitemkeys = datasetkeys + ";" + this.processedDataitems.getValue(i6, "paramid") + ";" + this.processedDataitems.getValue(i6, "paramtype") + ";" + this.processedDataitems.getValue(i6, "replicateid");
            String sdiKey = this.processedDataitems.getValue(i6, "keyid1") + (keycolumns >= 2 ? ";" + (this.processedDataitems.getValue(i6, "keyid2").length() == 0 ? "(null)" : this.processedDataitems.getValue(i6, "keyid2")) : "") + (keycolumns == 3 ? ";" + (this.processedDataitems.getValue(i6, "keyid3").length() == 0 ? "(null)" : this.processedDataitems.getValue(i6, "keyid3")) : "");
            int primaryRow = primaryKeyRowMap.containsKey(sdiKey) ? (Integer)primaryKeyRowMap.get(sdiKey) : -1;
            int datasetRow = datasetKeyRowMap.containsKey(datasetkeys) ? (Integer)datasetKeyRowMap.get(datasetkeys) : -1;
            for (int c = 0; c < columns.length; ++c) {
                if (i6 == 0) {
                    colMap.put(columns[c], index);
                    columnidArray.put(index, columns[c]);
                }
                String value2 = "";
                if (!"speccondition".equals(columns[c]) && columns[c].indexOf(".") > 0) {
                    if (primary != null && primaryRow >= 0 && columns[c].indexOf("primary.") == 0) {
                        value2 = primary.getValue(primaryRow, columns[c].substring(8), "");
                        this.processedDataitems.setValue(i6, columns[c], value2);
                    } else if (dataset != null && datasetRow >= 0 && columns[c].indexOf("sdidata.") == 0) {
                        value2 = dataset.getValue(datasetRow, columns[c].substring(8), "");
                        this.processedDataitems.setValue(i6, columns[c], value2);
                    } else {
                        value2 = this.processedDataitems.getValue(i6, columns[c], "");
                    }
                } else if ("isusercertified".equals(columns[c])) {
                    if (datasetRow >= 0 && "Y".equals(dataset.getValue(datasetRow, "s_trainingreqflag"))) {
                        Object flag;
                        String paramlistkey = this.processedDataitems.getValue(i6, "paramlistid") + ";" + this.processedDataitems.getValue(i6, "paramlistversionid") + ";" + this.processedDataitems.getValue(i6, "variantid");
                        Object v2 = flag = userCertCache == null ? null : userCertCache.get(paramlistkey);
                        if (flag == null) {
                            userCertCache = this.getUserCertMap(dataset, true);
                            flag = userCertCache.get(paramlistkey);
                        }
                        value2 = flag == null ? "Y" : (String)flag;
                    } else {
                        value2 = "Y";
                    }
                    this.processedDataitems.setValue(i6, columns[c], value2);
                } else if ("hasdataitemnotes".equals(columns[c])) {
                    value2 = this.hasnotesMap.get(sdcid + ";" + dataitemkeys) == null ? "N" : "Y";
                    this.processedDataitems.setValue(i6, columns[c], value2);
                } else if ("hasdatasetnotes".equals(columns[c])) {
                    value2 = this.hasnotesMap.get(sdcid + ";" + datasetkeys) == null ? "N" : "Y";
                    this.processedDataitems.setValue(i6, columns[c], value2);
                } else {
                    value2 = "s_acoverriddenflagoriginal".equals(columns[c]) ? this.processedDataitems.getValue(i6, "s_acoverriddenflag") : this.processedDataitems.getValue(i6, columns[c], "");
                }
                jsonObjRowArray.put(index, value2);
                ++index;
            }
            if (dataspecs != null) {
                String specCondition;
                if (i6 == 0) {
                    if (!colMap.has("speccondition") || colMap.get("speccondition") == null) {
                        colMap.put("speccondition", index);
                        columnidArray.put(index, "speccondition");
                    }
                    this.specconditionMap = new HashMap();
                    for (int sp = 0; sp < dataspecs.getRowCount(); ++sp) {
                        String dataitemspeccondition;
                        String currentkeyset;
                        String oosgeneratingflag = dataspecs.getValue(sp, "oosgeneratingflag");
                        if ("N".equals(oosgeneratingflag) && (this.passedinSpecID == null || this.passedinSpecID.length() <= 0) || this.specconditionMap.get(currentkeyset = dataspecs.getValue(sp, "keyid1") + ";" + dataspecs.getValue(sp, "keyid2") + ";" + dataspecs.getValue(sp, "keyid3") + ";" + dataspecs.getValue(sp, "paramlistid") + ";" + dataspecs.getValue(sp, "paramlistversionid") + ";" + dataspecs.getValue(sp, "variantid") + ";" + dataspecs.getValue(sp, "dataset") + ";" + dataspecs.getValue(sp, "paramid") + ";" + dataspecs.getValue(sp, "paramtype") + ";" + dataspecs.getValue(sp, "replicateid")) != null && !SPEC_DISPLAY_OPTION_WORSET_MATCH.equals(this.spec_displayoption) || conditions == null || conditions.size() <= 0 || (dataitemspeccondition = dataspecs.getValue(sp, "condition")).length() <= 0) continue;
                        for (int s = 0; s < conditions.size(); ++s) {
                            PropertyList cuePl = (PropertyList)conditions.get(s);
                            String specCondition2 = cuePl.getProperty("value");
                            if (specCondition2.length() <= 0 || !specCondition2.equals(dataitemspeccondition)) continue;
                            if (this.passedinSpecID != null && this.passedinSpecID.length() > 0) {
                                if (!this.passedinSpecID.equals(dataspecs.getValue(sp, "specid")) || !this.passedinSpecVersionID.equals(dataspecs.getValue(sp, "specversionid"))) continue;
                                this.specconditionMap.put(currentkeyset, specCondition2);
                                break;
                            }
                            if (SPEC_DISPLAY_OPTION_FIRST_MATCH.equals(this.spec_displayoption)) {
                                this.specconditionMap.put(currentkeyset, specCondition2);
                                break;
                            }
                            if (!SPEC_DISPLAY_OPTION_WORSET_MATCH.equals(this.spec_displayoption) || this.specconditionMap.get(currentkeyset) != null && this.worstConditionMap.get(dataitemspeccondition) <= this.worstConditionMap.get(this.specconditionMap.get(currentkeyset))) continue;
                            this.specconditionMap.put(currentkeyset, specCondition2);
                        }
                        if (this.specconditionMap.get(currentkeyset) != null || this.passedinSpecID != null && this.passedinSpecID.length() > 0) continue;
                        this.specconditionMap.put(currentkeyset, dataitemspeccondition);
                    }
                }
                if ((specCondition = this.specconditionMap.get(dataitemkeys)) != null) {
                    jsonObjRowArray.put(colMap.getInt("speccondition"), specCondition);
                    this.processedDataitems.setValue(i6, "speccondition", specCondition);
                } else {
                    jsonObjRowArray.put(colMap.getInt("speccondition"), "");
                }
                ++index;
            }
            StringBuffer key = new StringBuffer();
            for (int rh = 0; rh < rheaders.length; ++rh) {
                value = this.processedDataitems.getValue(i6, rheaders[rh]);
                if (tp != null && "Y".equals(rheadersTrans[rh])) {
                    value = this.getHeaderColValueAndAddtoTransMap(value, rheaders[rh], tp, dataitems.getValue(0, "sdcid"), headerTranslationMap);
                }
                key.append((rh == 0 ? "" : ";") + value.replaceAll(";", "#semicolon#"));
            }
            for (int ch = 0; ch < cheaders.length; ++ch) {
                value = this.processedDataitems.getValue(i6, cheaders[ch]);
                if (tp != null && "Y".equals(cheadersTrans[ch])) {
                    value = this.getHeaderColValueAndAddtoTransMap(value, cheaders[ch], tp, dataitems.getValue(0, "sdcid"), headerTranslationMap);
                }
                key.append(";" + value.replaceAll(";", "#semicolon#"));
            }
            if (this.isInitialRendering && jsonKeyIndex.has(key.toString())) {
                String value3;
                int conflictRow = (Integer)jsonKeyIndex.get(key.toString());
                String[] debugcolumns = new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"};
                StringBuffer html = new StringBuffer("<p>Cross Tab Key:</p><br/><table cellspacing=0 style=\"border-collapse: collapse;\">");
                for (int rh = 0; rh < rheaders.length; ++rh) {
                    html.append("<tr>");
                    value3 = this.processedDataitems.getValue(i6, rheaders[rh]);
                    if (tp != null && "Y".equals(rheadersTrans[rh])) {
                        value3 = this.getHeaderColValueAndAddtoTransMap(value3, rheaders[rh], tp, dataitems.getValue(0, "sdcid"), headerTranslationMap);
                    }
                    html.append("<td style=\"border: 1px solid black\">" + rheaders[rh] + "</td><td style=\"border: 1px solid black\">" + value3.replaceAll(";", "#semicolon#") + "</td>");
                    html.append("</tr>");
                }
                for (int ch = 0; ch < cheaders.length; ++ch) {
                    html.append("<tr>");
                    value3 = this.processedDataitems.getValue(i6, cheaders[ch]);
                    if (tp != null && "Y".equals(cheadersTrans[ch])) {
                        value3 = this.getHeaderColValueAndAddtoTransMap(value3, cheaders[ch], tp, dataitems.getValue(0, "sdcid"), headerTranslationMap);
                    }
                    html.append("<td style=\"border: 1px solid black\">" + cheaders[ch] + "</td><td style=\"border: 1px solid black\">" + value3.replaceAll(";", "#semicolon#") + "</td>");
                    html.append("</tr>");
                }
                html.append("</table>");
                html.append("<br/><p>linked to multiple records:</p><br/>");
                html.append("<table cellspacing=0 style=\"border-collapse: collapse;\">");
                int[] debugrows = new int[]{0, conflictRow, i6};
                for (int debugrow = 0; debugrow < debugrows.length; ++debugrow) {
                    html.append("<tr>");
                    for (int debugcol = 0; debugcol < debugcolumns.length; ++debugcol) {
                        html.append("<td style=\"border: 1px solid black\">");
                        if (debugrow == 0) {
                            html.append(debugcolumns[debugcol]);
                        } else {
                            html.append(this.processedDataitems.getValue(debugrows[debugrow], debugcolumns[debugcol]));
                        }
                        html.append("</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</table>");
                throw new SapphireException(html.toString());
            }
            jsonKeyIndex.put(key.toString(), i6);
            if (this.isInitialRendering) {
                jsonItemKeyHashIndex.put("" + dataitemkeys.hashCode(), i6);
            } else {
                jsonItemKeyArray.put(i6, "" + dataitemkeys.hashCode());
            }
            jsonArray.put(i6, jsonObjRowArray);
        }
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("columns", colMap);
        jsonObj.put("columnidArray", columnidArray);
        jsonObj.put("dataset", jsonArray);
        jsonObj.put("keyindex", jsonKeyIndex);
        jsonObj.put("sdcid", this.processedDataitems.getValue(0, "sdcid"));
        if (this.isInitialRendering) {
            jsonObj.put("dataitemkeyindex", jsonItemKeyHashIndex);
        } else {
            jsonObj.put("dataitemkeyArray", jsonItemKeyArray);
        }
        jsonObj.put("columnheader", JSONUtil.toJSONArray(this.getHeaderList(this.processedDataitems, cheaders, cheadersTrans, cSortByDSUserSeq, cSortByDIUserSeq, tp, headerTranslationMap)));
        jsonObj.put("rowheader", JSONUtil.toJSONArray(this.getHeaderList(this.processedDataitems, rheaders, rheadersTrans, rSortByDSUserSeq, rSortByDIUserSeq, tp, headerTranslationMap)));
        if (this.element != null && this.element.getPropertyList("gridtopfilter") != null && "Y".equals(this.element.getPropertyList("gridtopfilter").getProperty("show")) && (filterColumns = (filterPL = this.element.getPropertyList("gridtopfilter")).getCollection("columns")) != null && filterColumns.size() > 0) {
            String[] filtercols;
            StringBuffer filterAttrs = new StringBuffer();
            for (int i7 = 0; i7 < filterColumns.size(); ++i7) {
                filterAttrs.append(";" + filterColumns.getPropertyList(i7).getProperty("columnid"));
            }
            String[] filtercolsTrans = filtercols = StringUtil.split(filterAttrs.substring(1), ";");
            jsonObj.put("gridtopfiltervalues", JSONUtil.toJSONArray(this.getHeaderList(this.processedDataitems, filtercols, filtercolsTrans, rSortByDSUserSeq, rSortByDIUserSeq, tp, headerTranslationMap)));
        }
        jsonObj.put("columnheaderid", JSONUtil.toJSONArray(cheaders));
        jsonObj.put("rowheaderid", JSONUtil.toJSONArray(rheaders));
        jsonObj.put("translationmap", headerTranslationMap);
        return jsonObj;
    }

    private DataSet evaluateItemProtectionRule(String sdcid, DataSet dataitems, String dataitemdisplayRule, int keycolumns) {
        dataitems.addColumn("filtermet", 0, 1);
        dataitems.addColumn("rownum", 1);
        HashMap<String, HashMap> bindMap = new HashMap<String, HashMap>();
        bindMap.put("user", this.connectionInfo.getUserAttributeMap());
        int rows = dataitems.getRowCount();
        HashMap<HashMap, Integer> rownumMap = new HashMap<HashMap, Integer>();
        for (int i = 0; i < rows; ++i) {
            HashMap sdidataitem = (HashMap)dataitems.get(i);
            bindMap.put("sdidataitem", sdidataitem);
            String primarykey = dataitems.getValue(i, "keyid1") + (keycolumns >= 2 ? ";" + dataitems.getValue(i, "keyid2") : "") + (keycolumns == 3 ? ";" + dataitems.getValue(i, "keyid3") : "");
            HashMap primaryrow = (HashMap)this.primaryKeyIndexMap.get(primarykey);
            bindMap.put("primary", primaryrow);
            HashMap datasetrow = (HashMap)this.datasetKeyIndexMap.get(dataitems.getValue(i, "keyid1") + ";" + dataitems.getValue(i, "keyid2") + ";" + dataitems.getValue(i, "keyid3") + ";" + dataitems.getValue(i, "paramlistid") + ";" + dataitems.getValue(i, "paramlistversionid") + ";" + dataitems.getValue(i, "variantid") + ";" + dataitems.getValue(i, "dataset"));
            boolean isBlocked = "Y".equals(datasetrow.get("blockflag"));
            bindMap.put("sdidata", datasetrow);
            int itemrownum = 0;
            if (rownumMap.get(datasetrow) != null) {
                itemrownum = (Integer)rownumMap.get(datasetrow);
            }
            rownumMap.put(datasetrow, new Integer(itemrownum + 1));
            try {
                String filtermet;
                String mode = dataitemdisplayRule;
                dataitems.setNumber(i, "rownum", itemrownum);
                if (mode.indexOf("$G{") == 0) {
                    mode = GroovyUtil.getInstance(this.connectionInfo).evaluateSecure(dataitemdisplayRule, bindMap);
                }
                String string = filtermet = !"exclude".equals(mode) ? "Y" : "N";
                if (!"Y".equals(filtermet)) continue;
                dataitems.setValue(i, "filtermet", filtermet);
                if ("S".equals(dataitems.getValue(i, "protection"))) continue;
                if ("Sample".equals(sdcid)) {
                    String samplestatus;
                    String string2 = samplestatus = primaryrow != null ? primaryrow.get("samplestatus") : "";
                    dataitems.setValue(i, "protection", isBlocked ? "Y" : ("readonly".equals(mode) ? (!"Completed".equals(samplestatus) && !"InProgress".equals(samplestatus) ? "Y" : "P") : "N"));
                    continue;
                }
                dataitems.setValue(i, "protection", isBlocked ? "Y" : ("readonly".equals(mode) ? "P" : "N"));
                continue;
            }
            catch (Exception e) {
                throw new RuntimeException("Data Item Display Rule groovy syntax error:" + e.getMessage());
            }
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("filtermet", "Y");
        dataitems = dataitems.getFilteredDataSet(filter);
        return dataitems;
    }

    private ArrayList getHeaderList(DataSet data, String[] headerColids, String[] headerColTrans, boolean sortbyDSUserSeq, boolean sortbyDIUserSeq, TranslationProcessor tp, JSONObject headerTranslationMap) throws JSONException {
        String sortbyString = "";
        boolean keyid1Found = false;
        boolean paramlistidFound = false;
        boolean paramidFound = false;
        boolean isAlignParamlist = false;
        boolean isAlignParamlistinkeyid = false;
        boolean isAlignParam = false;
        boolean usersequenceFound = false;
        boolean sdidatausersequenceFound = false;
        for (int i = 0; i < headerColids.length; ++i) {
            if ("sdidata.usersequence".equals(headerColids[i])) {
                sdidatausersequenceFound = true;
            }
            if (!"usersequence".equals(headerColids[i])) continue;
            usersequenceFound = true;
        }
        int keyid1Index = -1;
        int paramlistidIndex = -1;
        int paramlistversionidIndex = -1;
        int variantidIndex = -1;
        int paramidIndex = -1;
        int paramtypeIndex = -1;
        int replicateidIndex = -1;
        for (int i = 0; i < headerColids.length; ++i) {
            if ("keyid1".equals(headerColids[i]) && !keyid1Found) {
                keyid1Found = true;
                keyid1Index = i;
            }
            if ("paramlistid".equals(headerColids[i]) && !paramlistidFound) {
                paramlistidFound = true;
            }
            if ("paramlistid".equals(headerColids[i])) {
                paramlistidIndex = i;
                if (!sdidatausersequenceFound && sortbyDSUserSeq) {
                    if (!keyid1Found) {
                        isAlignParamlist = true;
                        sortbyString = sortbyString + ",__sdidata_usersequence";
                    } else {
                        isAlignParamlistinkeyid = true;
                        sortbyString = sortbyString + ",__sdidata_usersequence";
                    }
                }
            } else if ("paramlistversionid".equals(headerColids[i])) {
                paramlistversionidIndex = i;
            } else if ("variantid".equals(headerColids[i])) {
                variantidIndex = i;
            } else if ("paramid".equals(headerColids[i])) {
                if (!usersequenceFound && sortbyDIUserSeq) {
                    sortbyString = sortbyString + ",usersequence";
                }
                paramidIndex = i;
                if (!paramlistidFound) {
                    isAlignParam = true;
                }
                paramidFound = true;
            } else if ("paramtype".equals(headerColids[i])) {
                paramtypeIndex = i;
            } else if ("replicateid".equals(headerColids[i])) {
                replicateidIndex = i;
            }
            sortbyString = sortbyString + "," + headerColids[i];
        }
        if (sortbyString.indexOf(",") == 0) {
            sortbyString = sortbyString.substring(1);
        }
        if (!(paramidFound || paramlistidFound || keyid1Found || usersequenceFound || !sortbyDIUserSeq)) {
            sortbyString = "usersequence," + sortbyString;
        }
        if (keyid1Found && this.sortbyKeyid1list != null && sortbyString.indexOf("keyid1,") == 0) {
            sortbyString = "keyid1 " + (this.sortbyKeyid1list.length() == 0 ? "m" : this.sortbyKeyid1list) + sortbyString.substring(6);
        }
        ArrayList<String[]> headerArrayList = new ArrayList<String[]>();
        ArrayList<String> headerStringList = new ArrayList<String>();
        Trace.log("$$$$SortBy:" + sortbyString);
        data.sort(sortbyString);
        for (int r = 0; r < data.getRowCount(); ++r) {
            String[] headercolvalues = new String[headerColids.length];
            StringBuilder headercolSb = new StringBuilder();
            for (int i = 0; i < headerColids.length; ++i) {
                String value = data.getValue(r, headerColids[i]);
                if (tp != null && "Y".equals(headerColTrans[i])) {
                    value = this.getHeaderColValueAndAddtoTransMap(value, headerColids[i], tp, data.getValue(0, "sdcid"), headerTranslationMap);
                }
                headercolvalues[i] = value.replaceAll(";", "#semicolon#");
                headercolSb.append(";" + headercolvalues[i]);
            }
            String headercolString = headercolSb.substring(1);
            if (headerStringList.contains(headercolString)) continue;
            int addindex = headerArrayList.size();
            if (sortbyDSUserSeq && (isAlignParamlist || isAlignParamlistinkeyid) && paramlistidIndex >= 0 && paramlistversionidIndex >= 0 && variantidIndex >= 0) {
                String paramlistid = data.getValue(r, headerColids[paramlistidIndex]);
                String paramlistversionid = paramlistversionidIndex >= 0 ? data.getValue(r, headerColids[paramlistversionidIndex]) : "";
                String variantid = variantidIndex >= 0 ? data.getValue(r, headerColids[variantidIndex]) : "";
                for (int h = headerArrayList.size() - 1; h >= 0; --h) {
                    String[] currentheadervalues = (String[])headerArrayList.get(h);
                    if (!currentheadervalues[paramlistidIndex].equals(paramlistid) || paramlistversionid.length() != 0 && !currentheadervalues[paramlistversionidIndex].equals(paramlistversionid) || variantid.length() != 0 && !currentheadervalues[variantidIndex].equals(variantid)) continue;
                    if (isAlignParamlistinkeyid) {
                        if (!currentheadervalues[keyid1Index].equals(data.getValue(r, headerColids[keyid1Index]))) continue;
                        addindex = h + 1;
                        break;
                    }
                    addindex = h + 1;
                    break;
                }
            }
            if (sortbyDIUserSeq && isAlignParam && paramidIndex >= 0) {
                String paramid = data.getValue(r, headerColids[paramidIndex]);
                for (int h = headerArrayList.size() - 1; h >= 0; --h) {
                    String[] currentheadervalues = (String[])headerArrayList.get(h);
                    if (!currentheadervalues[paramidIndex].equals(paramid)) continue;
                    addindex = h + 1;
                    break;
                }
            }
            headerArrayList.add(addindex, headercolvalues);
            headerStringList.add(addindex, headercolString);
        }
        return headerStringList;
    }

    private static HashMap getDatasetKeyIndexMap(DataSet dataset) {
        HashMap datasetKeyIndexMap = new HashMap();
        int rows = dataset.getRowCount();
        for (int i = 0; i < rows; ++i) {
            datasetKeyIndexMap.put(dataset.getValue(i, "keyid1") + ";" + dataset.getValue(i, "keyid2") + ";" + dataset.getValue(i, "keyid3") + ";" + dataset.getValue(i, "paramlistid") + ";" + dataset.getValue(i, "paramlistversionid") + ";" + dataset.getValue(i, "variantid") + ";" + dataset.getValue(i, "dataset"), dataset.get(i));
        }
        return datasetKeyIndexMap;
    }

    private static HashMap getPrimaryKeyIndexMap(DataSet primary, String keycolid1, String keycolid2, String keycolid3) {
        HashMap primaryKeyIndexMap = new HashMap();
        int rows = primary.getRowCount();
        for (int i = 0; i < rows; ++i) {
            primaryKeyIndexMap.put(primary.getValue(i, keycolid1) + (keycolid2 == null || keycolid2.length() == 0 ? "" : ";" + primary.getValue(i, keycolid2)) + (keycolid3 == null || keycolid3.length() == 0 ? "" : ";" + primary.getValue(i, keycolid3)), primary.get(i));
        }
        return primaryKeyIndexMap;
    }

    private HashMap getUserCertMap(DataSet dataset, boolean forceUpdate) {
        boolean certificationCheck = dataset.findRow("s_trainingreqflag", "Y") >= 0;
        HashMap<String, String> userCertCache = (HashMap<String, String>)userCertificationCache.get(this.connectionInfo.getConnectionId());
        if (forceUpdate || certificationCheck && userCertCache == null) {
            if (userCertCache == null) {
                userCertCache = new HashMap<String, String>();
            }
            HashMap<String, String> certReqDataSetFilter = new HashMap<String, String>();
            certReqDataSetFilter.put("s_trainingreqflag", "Y");
            DataSet certReqDataSet = dataset.getFilteredDataSet(certReqDataSetFilter);
            if (certReqDataSet != null && certReqDataSet.getRowCount() > 0) {
                String rsetid = "";
                DAMProcessor damProcessor = new DAMProcessor(this.connectionInfo.getConnectionId());
                try {
                    rsetid = damProcessor.createRSet("ParamList", certReqDataSet.getColumnValues("paramlistid", ";"), certReqDataSet.getColumnValues("paramlistversionid", ";"), certReqDataSet.getColumnValues("variantid", ";"));
                    StringBuffer sbSQLStmt = new StringBuffer();
                    SafeSQL safeSQL = new SafeSQL();
                    sbSQLStmt.append("SELECT certifiedforkeyid1, certifiedforkeyid2, certifiedforkeyid3, expirationdt, graceperiod, graceperiodunits ");
                    sbSQLStmt.append("FROM s_sdicertification, rsetitems ");
                    sbSQLStmt.append("WHERE certificationstatus IN ('Valid', 'In Training') ");
                    sbSQLStmt.append("AND resourcesdcid='User' ");
                    sbSQLStmt.append("AND resourcekeyid1=" + safeSQL.addVar(this.connectionInfo.getSysuserId()) + " ");
                    sbSQLStmt.append(" AND certificationtype='Analyst Training' ");
                    sbSQLStmt.append(" AND certifiedforsdcid=rsetitems.sdcid ");
                    sbSQLStmt.append(" AND certifiedforkeyid1=rsetitems.keyid1 ");
                    sbSQLStmt.append(" AND (certifiedforkeyid2 = '(null)' or certifiedforkeyid2=rsetitems.keyid2) ");
                    sbSQLStmt.append(" AND (certifiedforkeyid3 = '(null)' or certifiedforkeyid3=rsetitems.keyid3) ");
                    sbSQLStmt.append(" AND rsetitems.rsetid =" + safeSQL.addVar(rsetid));
                    DataSet userCertificationDs = new QueryProcessor(this.connectionInfo.getConnectionId()).getPreparedSqlDataSet(sbSQLStmt.toString(), safeSQL.getValues());
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    for (int p = 0; p < certReqDataSet.getRowCount(); ++p) {
                        String paramlistid = certReqDataSet.getValue(p, "paramlistid");
                        String paramlistversionid = certReqDataSet.getValue(p, "paramlistversionid");
                        String variantid = certReqDataSet.getValue(p, "variantid");
                        if (userCertCache.get(paramlistid + ";" + paramlistversionid + ";" + variantid) != null) continue;
                        boolean allowoverride = "Y".equals(certReqDataSet.getValue(p, "s_overrideallowedflag"));
                        findMap.put("certifiedforkeyid1", paramlistid);
                        findMap.put("certifiedforkeyid2", paramlistversionid);
                        findMap.put("certifiedforkeyid3", variantid);
                        int i = userCertificationDs.findRow(findMap);
                        if (i == -1) {
                            findMap.put("certifiedforkeyid2", "(null)");
                            findMap.put("certifiedforkeyid3", variantid);
                            i = userCertificationDs.findRow(findMap);
                        }
                        if (i == -1) {
                            findMap.put("certifiedforkeyid2", paramlistversionid);
                            findMap.put("certifiedforkeyid3", "(null)");
                            i = userCertificationDs.findRow(findMap);
                        }
                        if (i == -1) {
                            findMap.put("certifiedforkeyid2", "(null)");
                            findMap.put("certifiedforkeyid3", "(null)");
                            i = userCertificationDs.findRow(findMap);
                        }
                        boolean hasValidCert = false;
                        if (i >= 0) {
                            Calendar expirationdt = userCertificationDs.getCalendar(i, "expirationdt");
                            if (expirationdt == null) {
                                hasValidCert = true;
                            } else {
                                int graceperiod = userCertificationDs.getInt(i, "graceperiod");
                                if (graceperiod > 0) {
                                    String graceperiodunits = userCertificationDs.getString(i, "graceperiodunits");
                                    if ("Days".equals(graceperiodunits)) {
                                        expirationdt.add(5, graceperiod);
                                    } else if ("Weeks".equals(graceperiodunits)) {
                                        expirationdt.add(5, graceperiod * 7);
                                    } else if ("Months".equals(graceperiodunits)) {
                                        expirationdt.add(2, graceperiod);
                                    } else if ("Years".equals(graceperiodunits)) {
                                        expirationdt.add(1, graceperiod);
                                    }
                                }
                                if (Calendar.getInstance().before(expirationdt)) {
                                    hasValidCert = true;
                                }
                            }
                        }
                        if (!hasValidCert) {
                            userCertCache.put(paramlistid + ";" + paramlistversionid + ";" + variantid, allowoverride ? "A" : "N");
                            continue;
                        }
                        userCertCache.put(paramlistid + ";" + paramlistversionid + ";" + variantid, "Y");
                    }
                    userCertificationCache.put(this.connectionInfo.getConnectionId(), userCertCache);
                }
                catch (Exception e) {
                    throw new RuntimeException("<p>Error:" + e.getMessage() + "</p>");
                }
                finally {
                    if (damProcessor != null && rsetid != null && rsetid.length() > 0) {
                        damProcessor.clearRSet(rsetid);
                    }
                }
            }
        }
        return userCertCache;
    }

    public int getTotalItemCount() {
        return this.totalItemCount;
    }

    private String getHeaderColValueAndAddtoTransMap(String value, String columnid, TranslationProcessor tp, String primarysdcid, JSONObject headerTranslationMap) throws JSONException {
        String transvalue = "paramlistid".equals(columnid) || "paramid".equals(columnid) || "variantid".equals(columnid) || "paramtype".equals(columnid) ? tp.translate(value, this.connectionInfo.getLanguage(), "ParamList") : tp.translate(value, this.connectionInfo.getLanguage(), primarysdcid);
        headerTranslationMap.put(value, transvalue);
        return value;
    }
}

