/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.qcbatch;

import com.labvantage.opal.exception.QCPositioningException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class QCPositioning {
    static String LABVANTAGE_CVS_ID = "$Revision: 53708 $";
    private final String POSITION_AT = "At";
    private final String POSITION_ABSOLUTEAT = "At Absolute";
    private final String POSITION_FIRST = "First";
    private final String POSITION_LAST = "Last";
    private final String POSITION_EVERY = "Every";
    private final String POSITION_DISTRIBUTED_EVEN = "Evenly Distributed";
    private final String POSITION_DISTRIBUTED_RANDOM = "Randomly Distributed";
    private final String POSITION_RANDOM_ANCHORED = "Random Anchored";
    private final String POSITION_FROM_END = "From End";
    private final String POSITION_NONE = "None";
    private final String POSITION_EVERYABSBEFORE = "Every Absolute Before";
    private final String POSITION_EVERYABSAFTER = "Every Absolute After";
    private final String POSITION_EVERYABSBEFOREAFTER = "Every Absolute Before & After";
    public static final String COMMA_REPLACEMENT_STRING = "#COMMA#";
    public static final String NEW_QCSAMPLE_STRING = "(Auto)";
    public static final String[] REQUIRED_COLUMNS = new String[]{"linktoqcbatchitemid", "qcsampletype", "keyid1", "qcbatchsampletypeid", "qcbatchitemdesc", "linkedto", "__status"};
    private final String STATUS_FLAG = "N";
    private final int NULL_IN_DATABASE_EQUIV = -999999999;
    private String __UnknownSamplesData;
    private String __UnknownSampleColumns;
    private QueryProcessor __QueryProcessor = null;
    private String __MethodID = null;
    private String __MethodVersionID = null;
    private String __BatchID = null;
    private boolean __ReferToBatchNotMethod = false;
    private boolean __NoUnknowns = false;
    private String __FinalSamplesList;
    private HashMap __RequiredColumnDetails;

    public String getUnknownSamplesData() {
        return this.__UnknownSamplesData;
    }

    public void setUnknownSamplesData(String newUnknownSamplesData) {
        this.__UnknownSamplesData = newUnknownSamplesData;
    }

    public String getUnknownSampleColumns() {
        return this.__UnknownSampleColumns;
    }

    public void setUnknownSampleColumns(String newUnknownSampleColumns) {
        this.__UnknownSampleColumns = newUnknownSampleColumns;
    }

    public QueryProcessor getQueryProcessor() {
        return this.__QueryProcessor;
    }

    public void setQueryProcessor(QueryProcessor newQueryProcessor) {
        this.__QueryProcessor = newQueryProcessor;
    }

    public String getMethodID() {
        return this.__MethodID;
    }

    public void setMethodID(String newMethodID) {
        this.__MethodID = newMethodID;
    }

    public String getMethodVersionID() {
        return this.__MethodVersionID;
    }

    public void setMethodVersionID(String newMethodVersionID) {
        this.__MethodVersionID = newMethodVersionID;
    }

    public String getBatchID() {
        return this.__BatchID;
    }

    public void setBatchID(String newBatchID) {
        this.__BatchID = newBatchID;
    }

    public boolean getReferToBatchNotMethod() {
        return this.__ReferToBatchNotMethod;
    }

    public void setReferToBatchNotMethod(boolean referToBatch) {
        this.__ReferToBatchNotMethod = referToBatch;
    }

    public String getFinalSamplesList() {
        return this.__FinalSamplesList;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void insertQCSamples() throws QCPositioningException {
        String[] sampleColumns = null;
        ArrayList<String[]> finalSamples = null;
        ArrayList<String[]> unknownSamples = null;
        String positionColumnsQuery = null;
        HashMap existingQCSamples = new HashMap();
        if (this.__UnknownSamplesData == null || this.__UnknownSamplesData.length() == 0) {
            this.__NoUnknowns = true;
        }
        if (this.__QueryProcessor == null) {
            throw new QCPositioningException("Error: Query Processor not specified, cannot execute query for positioning.");
        }
        if (!this.__ReferToBatchNotMethod) {
            if (this.__MethodID == null || this.__MethodID == "" || this.__MethodVersionID == null || this.__MethodVersionID == "") {
                throw new QCPositioningException("Error: QC Method not specified with method id and version, cannot apply positioning rule.");
            }
        } else if (this.__BatchID == null || this.__BatchID == "") {
            throw new QCPositioningException("Error: QC Batch not specified, cannot apply any positioning rule.");
        }
        sampleColumns = StringUtil.split(this.__UnknownSampleColumns, "|");
        unknownSamples = new ArrayList<String[]>();
        if (!this.__NoUnknowns) {
            if (this.__UnknownSamplesData != null && this.__UnknownSamplesData.startsWith("[")) {
                try {
                    JSONArray jsonArray = new JSONArray(this.__UnknownSamplesData);
                    for (int row = 0; row < jsonArray.length(); ++row) {
                        JSONObject jsonObject = jsonArray.getJSONObject(row);
                        String[] data = new String[sampleColumns.length];
                        String sampleQCType = "";
                        for (int col = 0; col < sampleColumns.length; ++col) {
                            String key = Integer.toString(col);
                            String string = data[col] = jsonObject.has(key) ? jsonObject.getString(key) : "";
                            if (!"qcsampletype".equalsIgnoreCase(sampleColumns[col])) continue;
                            sampleQCType = data[col];
                        }
                        if ("Unknown".equalsIgnoreCase(sampleQCType) || "".equals(sampleQCType)) {
                            unknownSamples.add(data);
                            continue;
                        }
                        if (existingQCSamples.containsKey(sampleQCType)) {
                            ((ArrayList)existingQCSamples.get(sampleQCType)).add(data);
                            continue;
                        }
                        ArrayList<String[]> al = new ArrayList<String[]>();
                        al.add(data);
                        existingQCSamples.put(sampleQCType, al);
                    }
                    finalSamples = new ArrayList(unknownSamples);
                }
                catch (JSONException e) {
                    Logger.logError(e.getMessage(), e);
                }
            } else {
                String[] sampleRowData = StringUtil.split(this.__UnknownSamplesData, "|");
                block6: for (int i = 0; i < sampleRowData.length; ++i) {
                    String[] columnWiseDataForEachRow = StringUtil.split(sampleRowData[i], ",");
                    String sampleQCType = "";
                    for (int k = 0; k < sampleColumns.length; ++k) {
                        if (!"qcsampletype".equalsIgnoreCase(sampleColumns[k])) continue;
                        sampleQCType = columnWiseDataForEachRow[k];
                        if ("Unknown".equalsIgnoreCase(columnWiseDataForEachRow[k]) || "".equals(columnWiseDataForEachRow[k])) {
                            unknownSamples.add(columnWiseDataForEachRow);
                            continue block6;
                        }
                        if (existingQCSamples.containsKey(sampleQCType)) {
                            ((ArrayList)existingQCSamples.get(sampleQCType)).add(columnWiseDataForEachRow);
                            continue block6;
                        }
                        ArrayList<String[]> al = new ArrayList<String[]>();
                        al.add(columnWiseDataForEachRow);
                        existingQCSamples.put(sampleQCType, al);
                        continue block6;
                    }
                }
                finalSamples = new ArrayList(unknownSamples);
            }
        } else {
            finalSamples = new ArrayList<String[]>();
        }
        if (unknownSamples == null || unknownSamples.size() == 0) {
            throw new QCPositioningException("Error: Positioning rule could not be applied. No unknown samples selected.");
        }
        this.__RequiredColumnDetails = new HashMap();
        block8: for (int i = 0; i < REQUIRED_COLUMNS.length; ++i) {
            for (int j = 0; j < sampleColumns.length; ++j) {
                if (!sampleColumns[j].equalsIgnoreCase(REQUIRED_COLUMNS[i])) continue;
                this.__RequiredColumnDetails.put(REQUIRED_COLUMNS[i], new Integer(j));
                continue block8;
            }
        }
        SafeSQL safeSQL = new SafeSQL();
        positionColumnsQuery = this.__ReferToBatchNotMethod ? "SELECT s_qcbatchsampletypeid, qcsampletype, qcbatchsampletypedesc, positiontype, positionstart, positionend, positionevery, positioncount, linkedto FROM s_qcbatchsampletype WHERE qcbatchid = " + safeSQL.addVar(this.__BatchID) + " order by usersequence" : "SELECT s_qcmethodsampletypeid, qcsampletype, qcmethoditemsampledesc, positiontype, positionstart, positionend, positionevery, positioncount, linkedto FROM s_qcmethodsampletype  WHERE qcmethodid = " + safeSQL.addVar(this.__MethodID) + " AND qcmethodversionid = " + safeSQL.addVar(this.__MethodVersionID) + " order by usersequence";
        try {
            DataSet qcDataSet = this.__QueryProcessor.getPreparedSqlDataSet(positionColumnsQuery, safeSQL.getValues());
            if (qcDataSet == null || qcDataSet.size() < 1) {
                if (this.__NoUnknowns) throw new QCPositioningException("Error: No positioning rule found and no unknown samples either.");
                Logger.logWarn("No positioning rule found.");
            } else {
                int index1 = (Integer)this.__RequiredColumnDetails.get(REQUIRED_COLUMNS[1]);
                int index2 = (Integer)this.__RequiredColumnDetails.get(REQUIRED_COLUMNS[2]);
                int index3 = (Integer)this.__RequiredColumnDetails.get(REQUIRED_COLUMNS[3]);
                int index4 = (Integer)this.__RequiredColumnDetails.get(REQUIRED_COLUMNS[4]);
                int index5 = (Integer)this.__RequiredColumnDetails.get(REQUIRED_COLUMNS[5]);
                int sampleColumnsLength = sampleColumns.length;
                int tempArrayLength = sampleColumns.length + 1;
                int rows = qcDataSet.size();
                for (int i = 0; i < rows; ++i) {
                    int spaceBetween;
                    HashMap<String, String> values;
                    String[] temp;
                    int tempPos;
                    int j;
                    int count;
                    String qcSampleTypeId = "";
                    String qcMethodItemSampleDesc = "";
                    String qcSampleType = qcDataSet.getString(i, "qcsampletype");
                    String positionType = qcDataSet.getString(i, "positiontype");
                    if ("None".equalsIgnoreCase(positionType)) continue;
                    String linkedTo = qcDataSet.getString(i, "linkedto");
                    String[] unknownSampleAtPositionStart = null;
                    if (this.__ReferToBatchNotMethod) {
                        qcSampleTypeId = qcDataSet.getString(i, "s_qcbatchsampletypeid");
                        qcMethodItemSampleDesc = qcDataSet.getString(i, "qcbatchsampletypedesc");
                    } else {
                        qcSampleTypeId = qcDataSet.getString(i, "s_qcmethodsampletypeid");
                        qcMethodItemSampleDesc = qcDataSet.getString(i, "qcmethoditemsampledesc");
                    }
                    int positionStart = this.__handleNullForInt(qcDataSet.getInt(i, "positionstart"));
                    if (--positionStart >= unknownSamples.size() || positionStart < 0) {
                        positionStart = 0;
                    }
                    int positionEnd = this.__handleNullForInt(qcDataSet.getInt(i, "positionend"));
                    --positionEnd;
                    if (!this.__NoUnknowns) {
                        if (positionEnd >= unknownSamples.size() || positionEnd < positionStart) {
                            positionEnd = unknownSamples.size() - 1;
                        }
                    } else {
                        positionEnd = positionStart + 1;
                    }
                    if ((count = this.__handleNullForInt(qcDataSet.getInt(i, "positioncount"))) == 0) {
                        count = 1;
                    }
                    if (!this.__NoUnknowns) {
                        unknownSampleAtPositionStart = (String[])unknownSamples.get(positionStart);
                    }
                    if (positionType.equalsIgnoreCase("At")) {
                        for (j = 0; j < count; ++j) {
                            tempPos = positionStart;
                            temp = new String[tempArrayLength];
                            if (!this.__NoUnknowns) {
                                tempPos = finalSamples.indexOf(unknownSampleAtPositionStart);
                            }
                            temp[index1] = qcSampleType;
                            temp[index3] = qcSampleTypeId;
                            temp[index4] = qcMethodItemSampleDesc;
                            temp[index5] = linkedTo;
                            this.searchFromExistingSamples(existingQCSamples, qcSampleType, temp, index2, sampleColumnsLength);
                            finalSamples.add(tempPos, temp);
                        }
                    }
                    if (positionType.equalsIgnoreCase("At Absolute")) {
                        positionStart = this.__handleNullForInt(qcDataSet.getInt(i, "positionstart"));
                        if (--positionStart < 0) {
                            positionStart = 0;
                        }
                        if (positionStart >= finalSamples.size()) {
                            int tempPos2 = positionStart = finalSamples.size();
                            for (int j2 = 0; j2 < count; ++j2) {
                                temp = new String[tempArrayLength];
                                temp[index1] = qcSampleType;
                                temp[index3] = qcSampleTypeId;
                                temp[index4] = qcMethodItemSampleDesc;
                                temp[index5] = linkedTo;
                                this.searchFromExistingSamples(existingQCSamples, qcSampleType, temp, index2, sampleColumnsLength);
                                finalSamples.add(tempPos2++, temp);
                            }
                            continue;
                        }
                        String[] finalSampleAtPositionStart = null;
                        finalSampleAtPositionStart = (String[])finalSamples.get(positionStart);
                        tempPos = finalSamples.indexOf(finalSampleAtPositionStart);
                        for (int j3 = 0; j3 < count; ++j3) {
                            temp = new String[tempArrayLength];
                            temp[index1] = qcSampleType;
                            temp[index3] = qcSampleTypeId;
                            temp[index4] = qcMethodItemSampleDesc;
                            temp[index5] = linkedTo;
                            this.searchFromExistingSamples(existingQCSamples, qcSampleType, temp, index2, sampleColumnsLength);
                            finalSamples.add(tempPos, temp);
                            ++tempPos;
                        }
                        continue;
                    }
                    if (positionType.equalsIgnoreCase("First")) {
                        for (j = 0; j < count; ++j) {
                            temp = new String[tempArrayLength];
                            temp[index1] = qcSampleType;
                            temp[index3] = qcSampleTypeId;
                            temp[index4] = qcMethodItemSampleDesc;
                            temp[index5] = linkedTo;
                            this.searchFromExistingSamples(existingQCSamples, qcSampleType, temp, index2, sampleColumnsLength);
                            finalSamples.add(0, temp);
                        }
                        continue;
                    }
                    if (positionType.equalsIgnoreCase("Last")) {
                        for (j = 0; j < count; ++j) {
                            temp = new String[tempArrayLength];
                            temp[index1] = qcSampleType;
                            temp[index3] = qcSampleTypeId;
                            temp[index4] = qcMethodItemSampleDesc;
                            temp[index5] = linkedTo;
                            this.searchFromExistingSamples(existingQCSamples, qcSampleType, temp, index2, sampleColumnsLength);
                            finalSamples.add(temp);
                        }
                        continue;
                    }
                    if (positionType.equalsIgnoreCase("From End")) {
                        for (j = 0; j < count; ++j) {
                            temp = new String[tempArrayLength];
                            tempPos = unknownSamples.size() - 1 - positionStart;
                            if (tempPos < 0) {
                                tempPos = 0;
                            }
                            if (!this.__NoUnknowns) {
                                tempPos = finalSamples.indexOf((String[])unknownSamples.get(tempPos));
                            }
                            temp[index1] = qcSampleType;
                            temp[index3] = qcSampleTypeId;
                            temp[index4] = qcMethodItemSampleDesc;
                            temp[index5] = linkedTo;
                            this.searchFromExistingSamples(existingQCSamples, qcSampleType, temp, index2, sampleColumnsLength);
                            finalSamples.add(tempPos + 1, temp);
                        }
                        continue;
                    }
                    if (positionType.equalsIgnoreCase("Every")) {
                        values = new HashMap<String, String>();
                        int[] indices = new int[]{index1, index2, index3, index4, index5};
                        spaceBetween = this.__handleNullForInt(qcDataSet.getInt(i, "positionevery"));
                        values.put("qcSampleType", qcSampleType);
                        values.put("qcSampleTypeId", qcSampleTypeId);
                        values.put("qcMethodItemSampleDesc", qcMethodItemSampleDesc);
                        values.put("linkedTo", linkedTo);
                        positionStart = this.__handleNullForInt(qcDataSet.getInt(i, "positionstart"));
                        if (--positionStart < 0) {
                            positionStart = 0;
                        }
                        this.__insertQCSamplesForPositionEvery(positionStart, positionEnd, count, spaceBetween, sampleColumns, unknownSamples, finalSamples, indices, values, existingQCSamples);
                        continue;
                    }
                    if (positionType.equalsIgnoreCase("Every Absolute Before")) {
                        values = new HashMap();
                        int[] indices = new int[]{index1, index2, index3, index4, index5};
                        spaceBetween = this.__handleNullForInt(qcDataSet.getInt(i, "positionevery"));
                        values.put("qcSampleType", qcSampleType);
                        values.put("qcSampleTypeId", qcSampleTypeId);
                        values.put("qcMethodItemSampleDesc", qcMethodItemSampleDesc);
                        values.put("linkedTo", linkedTo);
                        positionStart = this.__handleNullForInt(qcDataSet.getInt(i, "positionstart"));
                        if (--positionStart < 0) {
                            positionStart = 0;
                        }
                        positionEnd = this.__handleNullForInt(qcDataSet.getInt(i, "positionend"));
                        this.__insertQCSamplesForPositionEveryAbsolute(positionStart, positionEnd, count, spaceBetween, sampleColumns, unknownSamples, finalSamples, indices, values, existingQCSamples, positionType);
                        continue;
                    }
                    if (positionType.equalsIgnoreCase("Every Absolute After")) {
                        values = new HashMap();
                        int[] indices = new int[]{index1, index2, index3, index4, index5};
                        spaceBetween = this.__handleNullForInt(qcDataSet.getInt(i, "positionevery"));
                        values.put("qcSampleType", qcSampleType);
                        values.put("qcSampleTypeId", qcSampleTypeId);
                        values.put("qcMethodItemSampleDesc", qcMethodItemSampleDesc);
                        values.put("linkedTo", linkedTo);
                        positionStart = this.__handleNullForInt(qcDataSet.getInt(i, "positionstart"));
                        if (--positionStart < 0) {
                            positionStart = 0;
                        }
                        positionEnd = this.__handleNullForInt(qcDataSet.getInt(i, "positionend"));
                        this.__insertQCSamplesForPositionEveryAbsolute(positionStart, positionEnd, count, spaceBetween, sampleColumns, unknownSamples, finalSamples, indices, values, existingQCSamples, positionType);
                        continue;
                    }
                    if (positionType.equalsIgnoreCase("Every Absolute Before & After")) {
                        values = new HashMap();
                        int[] indices = new int[]{index1, index2, index3, index4, index5};
                        spaceBetween = this.__handleNullForInt(qcDataSet.getInt(i, "positionevery"));
                        values.put("qcSampleType", qcSampleType);
                        values.put("qcSampleTypeId", qcSampleTypeId);
                        values.put("qcMethodItemSampleDesc", qcMethodItemSampleDesc);
                        values.put("linkedTo", linkedTo);
                        positionStart = this.__handleNullForInt(qcDataSet.getInt(i, "positionstart"));
                        if (--positionStart < 0) {
                            positionStart = 0;
                        }
                        positionEnd = this.__handleNullForInt(qcDataSet.getInt(i, "positionend"));
                        this.__insertQCSamplesForPositionEveryAbsolute(positionStart, positionEnd, count, spaceBetween, sampleColumns, unknownSamples, finalSamples, indices, values, existingQCSamples, positionType);
                        continue;
                    }
                    if (positionType.equalsIgnoreCase("Evenly Distributed")) {
                        int[] indices = new int[]{index1, index2, index3, index4, index5};
                        HashMap<String, String> values2 = new HashMap<String, String>();
                        values2.put("qcSampleType", qcSampleType);
                        values2.put("qcSampleTypeId", qcSampleTypeId);
                        values2.put("qcMethodItemSampleDesc", qcMethodItemSampleDesc);
                        values2.put("linkedTo", linkedTo);
                        this.__insertQCSamplesForPositionDistributedEven(positionStart, positionEnd, count, sampleColumns, unknownSamples, finalSamples, indices, values2, true, existingQCSamples);
                        continue;
                    }
                    if (positionType.equalsIgnoreCase("Randomly Distributed")) {
                        values = new HashMap();
                        int[] indices = new int[]{index1, index2, index3, index4, index5};
                        values.put("qcSampleType", qcSampleType);
                        values.put("qcSampleTypeId", qcSampleTypeId);
                        values.put("qcMethodItemSampleDesc", qcMethodItemSampleDesc);
                        values.put("linkedTo", linkedTo);
                        this.__insertQCSamplesForPositionDistributedRandom(positionStart, positionEnd, count, sampleColumns, unknownSamples, finalSamples, indices, values, existingQCSamples);
                        continue;
                    }
                    if (!positionType.equalsIgnoreCase("Random Anchored")) continue;
                    values = new HashMap();
                    int nextPosition = 0;
                    int[] indices = new int[]{index1, index2, index3, index4, index5};
                    int spaceBetween2 = this.__handleNullForInt(qcDataSet.getInt(i, "positionevery"));
                    int endOfBatch = 0;
                    Random randomGenerator = new Random();
                    if (positionEnd != positionStart) {
                        nextPosition = randomGenerator.nextInt(positionEnd - positionStart) + positionStart;
                        if (nextPosition == positionEnd) {
                            --nextPosition;
                        }
                    } else {
                        nextPosition = positionStart;
                    }
                    values.put("qcSampleType", qcSampleType);
                    values.put("qcSampleTypeId", qcSampleTypeId);
                    values.put("qcMethodItemSampleDesc", qcMethodItemSampleDesc);
                    values.put("linkedTo", linkedTo);
                    endOfBatch = !this.__NoUnknowns ? unknownSamples.size() - 1 : nextPosition + 1;
                    this.__insertQCSamplesForPositionEvery(nextPosition, endOfBatch, count, spaceBetween2, sampleColumns, unknownSamples, finalSamples, indices, values, existingQCSamples);
                }
            }
            this.__FinalSamplesList = "";
            StringBuffer finalSamplesList = new StringBuffer();
            for (int i = 0; i < finalSamples.size(); ++i) {
                String[] temp = (String[])finalSamples.get(i);
                StringBuffer sampleRow = new StringBuffer();
                for (int j = 0; j < temp.length; ++j) {
                    if (temp[j] == null || temp[j].equals(String.valueOf(-999999999))) {
                        temp[j] = "";
                    }
                    temp[j] = temp[j].replaceAll(",", COMMA_REPLACEMENT_STRING);
                    sampleRow.append(temp[j].trim()).append(",");
                }
                if (sampleRow.length() > 0 && sampleRow.charAt(sampleRow.length() - 1) == ',') {
                    sampleRow.deleteCharAt(sampleRow.length() - 1);
                }
                finalSamplesList.append(sampleRow.append("|"));
            }
            if (finalSamplesList.length() > 0 && finalSamplesList.charAt(finalSamplesList.length() - 1) == '|') {
                finalSamplesList.deleteCharAt(finalSamplesList.length() - 1);
            }
            this.__FinalSamplesList = finalSamplesList.toString();
            return;
        }
        catch (Exception ge) {
            Logger.logError(new StringBuffer().append("Exception in QC Batch Positioning : ").append(ge.getMessage()).toString(), ge);
            throw new QCPositioningException(ge.getMessage());
        }
    }

    private void searchFromExistingSamples(HashMap existingQCSamples, String qcSampleType, String[] temp, int index2, int sampleColumnsLength) {
        if (existingQCSamples.containsKey(qcSampleType)) {
            int ind = 0;
            ArrayList existSamples = (ArrayList)existingQCSamples.get(qcSampleType);
            if (ind < existSamples.size()) {
                String[] sampleData = (String[])existSamples.get(ind);
                temp[index2] = sampleData[2];
                temp[sampleColumnsLength] = "";
                existSamples.remove(ind);
                if (existSamples.size() == 0) {
                    existingQCSamples.remove(qcSampleType);
                }
            }
        } else {
            temp[index2] = NEW_QCSAMPLE_STRING;
            temp[sampleColumnsLength] = "N";
        }
    }

    private int __handleNullForInt(int i) {
        if (i == -999999999) {
            return 0;
        }
        return i;
    }

    private void __insertQCSamplesForPositionEvery(int positionStart, int positionEnd, int count, int spaceBetween, String[] sampleColumns, ArrayList unknownSamples, ArrayList finalSamples, int[] indices, HashMap values, HashMap existingQCSamples) {
        String qcSampleType = (String)values.get("qcSampleType");
        String qcSampleTypeId = (String)values.get("qcSampleTypeId");
        String qcMethodItemSampleDesc = (String)values.get("qcMethodItemSampleDesc");
        String linkedTo = (String)values.get("linkedTo");
        int position = positionStart + 1;
        if (spaceBetween > positionStart) {
            position = spaceBetween - 1;
        }
        int sampleColumnsLength = sampleColumns.length;
        int index1 = indices[0];
        int index2 = indices[1];
        int index3 = indices[2];
        int index4 = indices[3];
        int index5 = indices[4];
        if (spaceBetween <= 0) {
            spaceBetween = 1;
        }
        if (position > positionEnd) {
            return;
        }
        while (position <= positionEnd) {
            for (int j = 0; j < count; ++j) {
                int tempPos = position;
                String[] temp = new String[sampleColumns.length + 1];
                if (!this.__NoUnknowns) {
                    String[] sampleData = (String[])unknownSamples.get(position);
                    tempPos = finalSamples.indexOf(sampleData);
                }
                temp[index1] = qcSampleType;
                temp[index3] = qcSampleTypeId;
                temp[index4] = qcMethodItemSampleDesc;
                temp[index5] = linkedTo;
                this.searchFromExistingSamples(existingQCSamples, qcSampleType, temp, index2, sampleColumnsLength);
                finalSamples.add(tempPos + 1, temp);
            }
            position += spaceBetween;
        }
    }

    private void __insertQCSamplesForPositionEveryAbsolute(int positionStart, int positionEnd, int count, int spaceBetween, String[] sampleColumns, ArrayList unknownSamples, ArrayList finalSamples, int[] indices, HashMap values, HashMap existingQCSamples, String positionType) {
        String qcSampleType = (String)values.get("qcSampleType");
        String qcSampleTypeId = (String)values.get("qcSampleTypeId");
        String qcMethodItemSampleDesc = (String)values.get("qcMethodItemSampleDesc");
        String linkedTo = (String)values.get("linkedTo");
        int position = positionStart;
        int sampleColumnsLength = sampleColumns.length;
        int index1 = indices[0];
        int index2 = indices[1];
        int index3 = indices[2];
        int index4 = indices[3];
        int index5 = indices[4];
        if (spaceBetween <= 0) {
            spaceBetween = 1;
        }
        boolean positionEndCalculated = false;
        if (positionEnd == 0 || positionEnd - 1 < positionStart) {
            positionEnd = finalSamples.size();
            positionEndCalculated = true;
        } else {
            --positionEnd;
        }
        if (position > positionEnd) {
            return;
        }
        boolean lastControlAdded = false;
        while (position <= positionEnd) {
            if (positionType.equalsIgnoreCase("Every Absolute After") && (position += spaceBetween) > positionEnd) {
                if (positionEndCalculated) {
                    position = positionEnd;
                } else {
                    if (positionEnd < finalSamples.size()) break;
                    position = finalSamples.size();
                }
            }
            for (int j = 0; j < count; ++j) {
                int tempPos = position;
                String[] temp = new String[sampleColumns.length + 1];
                temp[index1] = qcSampleType;
                temp[index3] = qcSampleTypeId;
                temp[index4] = qcMethodItemSampleDesc;
                temp[index5] = linkedTo;
                this.searchFromExistingSamples(existingQCSamples, qcSampleType, temp, index2, sampleColumnsLength);
                if (position < finalSamples.size()) {
                    String[] sampleData = (String[])finalSamples.get(position);
                    tempPos = finalSamples.indexOf(sampleData);
                    finalSamples.add(tempPos, temp);
                    position = tempPos + 1;
                    continue;
                }
                finalSamples.add(temp);
                position = finalSamples.size();
                lastControlAdded = true;
            }
            if (lastControlAdded) break;
            if (positionEndCalculated) {
                positionEnd = finalSamples.size();
            }
            if (!positionType.equalsIgnoreCase("Every Absolute Before") && !positionType.equalsIgnoreCase("Every Absolute Before & After")) continue;
            if (positionType.equalsIgnoreCase("Every Absolute Before & After") && (position += spaceBetween) > positionEnd) {
                if (positionEndCalculated) {
                    position = finalSamples.size();
                } else if (positionEnd >= finalSamples.size()) {
                    position = finalSamples.size();
                }
            }
            if (!positionType.equalsIgnoreCase("Every Absolute Before") || position <= positionEnd && position != finalSamples.size()) continue;
            break;
        }
    }

    private void __insertQCSamplesForPositionDistributedEven(int positionStart, int positionEnd, int count, String[] sampleColumns, ArrayList unknownSamples, ArrayList finalSamples, int[] indices, HashMap values, boolean enforceEvenness, HashMap existingQCSamples) throws QCPositioningException {
        String qcSampleType = (String)values.get("qcSampleType");
        String qcSampleTypeId = (String)values.get("qcSampleTypeId");
        String qcMethodItemSampleDesc = (String)values.get("qcMethodItemSampleDesc");
        String linkedTo = (String)values.get("linkedTo");
        int spaceBetween = 0;
        int position = positionStart + 1;
        int sampleColumnsLength = sampleColumns.length;
        int index1 = indices[0];
        int index2 = indices[1];
        int index3 = indices[2];
        int index4 = indices[3];
        int index5 = indices[4];
        if (enforceEvenness && positionEnd - positionStart < count) {
            throw new QCPositioningException("Error: Too many QC samples for too few positions of insertion.");
        }
        spaceBetween = (double)Math.round((double)(positionEnd - positionStart) / (double)count) < 2.0 ? (positionEnd + 1 - positionStart) / count - 1 : (positionEnd - positionStart) / count;
        while (count > 0) {
            int tempPos = position;
            if (!this.__NoUnknowns) {
                tempPos = finalSamples.indexOf(unknownSamples.get(position));
            }
            String[] temp = new String[sampleColumns.length + 1];
            temp[index1] = qcSampleType;
            temp[index3] = qcSampleTypeId;
            temp[index4] = qcMethodItemSampleDesc;
            temp[index5] = linkedTo;
            this.searchFromExistingSamples(existingQCSamples, qcSampleType, temp, index2, sampleColumnsLength);
            finalSamples.add(tempPos, temp);
            if ((position += spaceBetween + 1) > positionEnd) {
                position = positionStart + 1;
            }
            --count;
        }
    }

    private void __insertQCSamplesForPositionDistributedRandom(int positionStart, int positionEnd, int count, String[] sampleColumns, ArrayList unknownSamples, ArrayList finalSamples, int[] indices, HashMap values, HashMap existingQCSamples) throws QCPositioningException {
        String qcSampleType = (String)values.get("qcSampleType");
        String qcSampleTypeId = (String)values.get("qcSampleTypeId");
        String qcMethodItemSampleDesc = (String)values.get("qcMethodItemSampleDesc");
        String linkedTo = (String)values.get("linkedTo");
        int position = positionStart + 1;
        int sampleColumnsLength = sampleColumns.length;
        int randomPosition = 0;
        int tempPos = 0;
        Random randomGenerator = new Random();
        int index1 = indices[0];
        int index2 = indices[1];
        int index3 = indices[2];
        int index4 = indices[3];
        int index5 = indices[4];
        if (positionEnd - positionStart < count) {
            throw new QCPositioningException("Error: Too many QC samples for too few positions of insertion.");
        }
        while (count > 0) {
            randomPosition = randomGenerator.nextInt(positionEnd - positionStart) + positionStart;
            position = randomPosition;
            if (position > positionEnd) {
                position = positionEnd;
            }
            String[] temp = new String[sampleColumns.length + 1];
            tempPos = position;
            if (!this.__NoUnknowns) {
                tempPos = finalSamples.indexOf(unknownSamples.get(position));
            }
            temp[index1] = qcSampleType;
            temp[index3] = qcSampleTypeId;
            temp[index4] = qcMethodItemSampleDesc;
            temp[index5] = linkedTo;
            this.searchFromExistingSamples(existingQCSamples, qcSampleType, temp, index2, sampleColumnsLength);
            finalSamples.add(tempPos, temp);
            --count;
        }
    }
}

