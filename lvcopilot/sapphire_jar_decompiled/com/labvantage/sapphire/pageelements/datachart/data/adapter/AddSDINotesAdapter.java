/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.data.adapter.AbstractDataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.AddSDINotesAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public class AddSDINotesAdapter
extends AbstractDataSetAdapter {
    private final String delimiter;
    private final String columnName;
    private final String sdcId;
    private final String keyId1Column;
    private final String keyId2Column;
    private final String keyId3Column;
    private final int maxNoteLength;
    private String rSetId;

    public AddSDINotesAdapter(String connectionId, DataBindingMap dataBindingMap, AddSDINotesAdapterConfiguration addSDINotesAdapterConf) {
        super(connectionId, dataBindingMap);
        this.delimiter = addSDINotesAdapterConf.getDelimiter();
        this.columnName = addSDINotesAdapterConf.getColumnName();
        this.sdcId = addSDINotesAdapterConf.getSdcId();
        this.keyId1Column = addSDINotesAdapterConf.getKeyId1Column();
        this.keyId2Column = addSDINotesAdapterConf.getKeyId2Column();
        this.keyId3Column = addSDINotesAdapterConf.getKeyId3Column();
        this.maxNoteLength = addSDINotesAdapterConf.getMaxNoteLength();
        try {
            this.rSetId = addSDINotesAdapterConf.getRSetId().evaluate(dataBindingMap);
        }
        catch (SapphireException e) {
            throw new IllegalArgumentException("Cannot evaluate RSet ID expression: " + addSDINotesAdapterConf.getRSetId().getExpression());
        }
        if (this.columnName.isEmpty()) {
            throw new IllegalArgumentException("Column name is empty");
        }
        if (this.delimiter.isEmpty()) {
            throw new IllegalArgumentException("Delimiter is empty");
        }
        if (this.sdcId.isEmpty()) {
            throw new IllegalArgumentException("SDC ID is empty");
        }
        if (this.keyId1Column.isEmpty()) {
            throw new IllegalArgumentException("Key ID 1 column is empty");
        }
    }

    @Override
    public void processDataSetAdapter(DataSet dataSet) throws SapphireException {
        if (dataSet.isValidColumn(this.columnName)) {
            throw new IllegalArgumentException("Column already exists in the data set: " + this.columnName);
        }
        if (!dataSet.isValidColumn(this.keyId1Column)) {
            throw new IllegalArgumentException("Key ID 1 column is not a valid column in data set: " + this.keyId1Column);
        }
        if (!this.keyId2Column.isEmpty() && !dataSet.isValidColumn(this.keyId2Column)) {
            throw new IllegalArgumentException("Key ID 2 column is not a valid column in data set: " + this.keyId2Column);
        }
        if (!this.keyId3Column.isEmpty() && !dataSet.isValidColumn(this.keyId3Column)) {
            throw new IllegalArgumentException("Key ID 3 column is not a valid column in data set: " + this.keyId3Column);
        }
        int sdiCount = 0;
        boolean clearRSet = false;
        if (this.rSetId.isEmpty()) {
            ArrayList<String> keyId1List = new ArrayList<String>();
            ArrayList<String> keyId2List = new ArrayList<String>();
            ArrayList<String> keyId3List = new ArrayList<String>();
            Util.populateKeyLists(dataSet, keyId1List, keyId2List, keyId3List, this.sdcId, this.keyId1Column, this.keyId2Column, this.keyId3Column);
            sdiCount = keyId1List.size();
            StringBuilder keyId1s = new StringBuilder();
            StringBuilder keyId2s = new StringBuilder();
            StringBuilder keyId3s = new StringBuilder();
            for (int i = 0; i < sdiCount; ++i) {
                keyId1s.append(";").append((String)keyId1List.get(i));
                keyId2s.append(";").append((String)keyId2List.get(i));
                keyId3s.append(";").append((String)keyId3List.get(i));
            }
            this.rSetId = this.getDAMProcessor().createRSet(this.sdcId, keyId1s.substring(1), keyId2s.substring(1), keyId3s.substring(1));
            clearRSet = true;
        }
        if (sdiCount > 0 || !this.rSetId.isEmpty()) {
            String getSDINotesSql = "SELECT coalesce(linkednote.note, primarynote.note) note, primarynote.sdcid, primarynote.keyid1, primarynote.keyid2, primarynote.keyid3 FROM sdinote primarynote JOIN rsetitems ON  primarynote.sdcid = rsetitems.sdcid AND primarynote.keyid1 = rsetitems.keyid1 AND primarynote.keyid2 = rsetitems.keyid2 AND primarynote.keyid3 = rsetitems.keyid3 LEFT JOIN sdinote linkednote ON primarynote.linksdcid = linkednote.sdcid AND primarynote.linkkeyid1 = linkednote.keyid1 AND primarynote.linkkeyid2 = linkednote.keyid2 AND primarynote.linkkeyid3 = linkednote.keyid3 AND primarynote.linknotenum = linkednote.notenum WHERE rsetitems.rsetid = ? ORDER BY primarynote.notenum";
            DataSet getSDINotesDs = this.getQueryProcessor().getPreparedSqlDataSet(getSDINotesSql, (Object[])new String[]{this.rSetId}, true);
            if (clearRSet) {
                this.getDAMProcessor().clearRSet(this.rSetId);
            }
            Map<List<String>, String> sdiNoteMap = this.flattenSDINotes(getSDINotesDs);
            dataSet.addColumn(this.columnName, 0);
            block1: for (int i = 0; i < dataSet.getRowCount(); ++i) {
                String sdiKeyId1 = dataSet.getString(i, this.keyId1Column);
                String sdiKeyId2 = dataSet.getString(i, this.keyId2Column, "(null)");
                String sdiKeyId3 = dataSet.getString(i, this.keyId3Column, "(null)");
                Set<Map.Entry<List<String>, String>> sdiNoteEntrySet = sdiNoteMap.entrySet();
                for (Map.Entry<List<String>, String> sdiNoteEntry : sdiNoteEntrySet) {
                    List<String> sdiKeyList = sdiNoteEntry.getKey();
                    String note = sdiNoteEntry.getValue() != null ? sdiNoteEntry.getValue() : "";
                    String noteKeyId1 = sdiKeyList.get(0);
                    String noteKeyId2 = sdiKeyList.get(1);
                    String noteKeyId3 = sdiKeyList.get(2);
                    if (!sdiKeyId1.equals(noteKeyId1) || !sdiKeyId2.equals(noteKeyId2) || !sdiKeyId3.equals(noteKeyId3)) continue;
                    String clippedNote = note;
                    if (clippedNote.length() > this.maxNoteLength) {
                        clippedNote = note.substring(0, this.maxNoteLength) + " ... " + (note.length() - this.maxNoteLength) + " " + this.getTranslationProcessor().translate("more");
                    }
                    dataSet.setString(i, this.columnName, clippedNote);
                    continue block1;
                }
            }
        }
        this.setProcessedDataSet(dataSet);
    }

    private Map<List<String>, String> flattenSDINotes(DataSet getSDINotesDs) {
        HashMap<List<String>, String> sdiNoteMap = new HashMap<List<String>, String>();
        for (int i = 0; i < getSDINotesDs.getRowCount(); ++i) {
            String keyId1 = getSDINotesDs.getString(i, "keyid1");
            String keyId2 = getSDINotesDs.getString(i, "keyid2", "(null)");
            String keyId3 = getSDINotesDs.getString(i, "keyid3", "(null)");
            String note = getSDINotesDs.getClob(i, "note");
            ArrayList<String> sdiKey = new ArrayList<String>();
            sdiKey.add(keyId1);
            sdiKey.add(keyId2);
            sdiKey.add(keyId3);
            if (sdiNoteMap.containsKey(sdiKey)) {
                note = (String)sdiNoteMap.get(sdiKey) + this.delimiter + note;
            }
            sdiNoteMap.put(sdiKey, note);
        }
        return sdiNoteMap;
    }
}

