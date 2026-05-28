/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.sdidetailmaint.handler;

import com.labvantage.opal.elements.detailmaint.BaseDetailPropertyHandler;
import com.labvantage.opal.util.ElementColumns;
import com.labvantage.opal.util.ElementData;
import com.labvantage.opal.util.SDIWorkItem;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.actions.workitem.AddSDIWorkItem;
import com.labvantage.sapphire.actions.workitem.ApplySDIWorkItem;
import com.labvantage.sapphire.actions.workitem.DeleteSDIWorkItem;
import com.labvantage.sapphire.actions.workitem.EditSDIWorkItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class SDIWorkitemPropertyHandler
extends BaseDetailPropertyHandler {
    public static String LABVANTAGE_CVS_ID = "$Revision: 84980 $";

    @Override
    protected void saveData() throws SapphireException {
        String workitemid;
        ElementColumns elementColumns = new ElementColumns(this._Ecolumns);
        ElementData elementData = new ElementData(elementColumns, this._Edata);
        BaseDetailPropertyHandler.ActionItem deleteWorkitem = this.addActionItem(DeleteSDIWorkItem.class.getName());
        BaseDetailPropertyHandler.ActionItem addWorkitem = this.addActionItem(AddSDIWorkItem.class.getName());
        BaseDetailPropertyHandler.ActionItem applySDIWorkitem = this.addActionItem(ApplySDIWorkItem.class.getName());
        String removedkeys = (String)this._ElementProps.get("eremove");
        if (removedkeys != null && removedkeys.length() > 1) {
            SDI sdi = new SDI(this.getSdcId(), this.getKeyid1(), this.getKeyid2(), this.getKeyid3());
            SDIWorkItem sdiWorkItem = new SDIWorkItem(sdi, "", "", this.getConnectionInfo().isOracle());
            String[] removekey = StringUtil.split(removedkeys, ";");
            for (int i = 0; i < removekey.length - 1; ++i) {
                String[] ds = StringUtil.split(removekey[i], "|");
                workitemid = ds[0];
                String instance = ds[1];
                sdiWorkItem.setWorkitemID(workitemid);
                sdiWorkItem.setWorkitemInstance(instance);
                if (sdiWorkItem.isAnyDataEntered(this.getQueryProcessor())) continue;
                this.putKeysInActionItem(deleteWorkitem);
                deleteWorkitem.put("workitemid", workitemid);
                deleteWorkitem.put("workiteminstance", instance);
                deleteWorkitem.put("cascadedeletes", "Y");
            }
        }
        List editableColumnList = elementColumns.getExcludedColumnList(SDIWorkitemPropertyHandler.getCoreColumnsList());
        editableColumnList.remove("appliedflag");
        HashMap<String, String> updateMap = new HashMap<String, String>();
        List<Sequence> sequenceList = new ArrayList();
        boolean updateSequence = false;
        for (int i = 0; i < elementData.size(); ++i) {
            workitemid = elementData.getColumnData(i, "workitemid");
            String workitemversionid = elementData.getColumnData(i, "workitemversionid");
            String workiteminstance = elementData.getColumnData(i, "workiteminstance");
            String groupid = elementData.getColumnData(i, "groupid");
            String groupinstance = elementData.getColumnData(i, "groupinstance");
            String workitemtypeflag = elementData.getColumnData(i, "workitemtypeflag");
            String __status = elementData.getColumnData(i, "__status");
            String applyWorkitem = __status.endsWith("A") ? "Y" : "N";
            int rowNum = i + 1;
            sequenceList.add(new Sequence(this.getSdcId(), this.getKeyid1(), workitemid, workiteminstance, String.valueOf(rowNum), groupid, groupinstance, workitemtypeflag));
            if (__status.startsWith("N")) {
                this.putKeysInActionItem(addWorkitem);
                addWorkitem.put("workitemid", workitemid);
                addWorkitem.put("workitemversionid", workitemversionid);
                addWorkitem.put("applyworkitem", applyWorkitem);
                addWorkitem.put("usersequence", String.valueOf(rowNum));
                addWorkitem.put("propsmatchtestmethodorder", String.valueOf(rowNum));
                addWorkitem.putColumns(editableColumnList, elementData, i);
                continue;
            }
            if (__status.equals("SA")) {
                this.putKeysInActionItem(applySDIWorkitem);
                applySDIWorkitem.put("workitemid", workitemid);
                applySDIWorkitem.put("applyworkitem", "Y");
                applySDIWorkitem.put("usersequence", String.valueOf(rowNum));
                applySDIWorkitem.putColumns(editableColumnList, elementData, i);
                continue;
            }
            if (!__status.startsWith("E")) continue;
            updateSequence = true;
            if (editableColumnList.size() <= 0) continue;
            for (Object anEditableColumnList : editableColumnList) {
                String columnid = (String)anEditableColumnList;
                if (!this._TableMD.doesColumnExists(columnid)) continue;
                String value = elementData.getColumnData(i, columnid);
                if (updateMap.containsKey(columnid)) {
                    updateMap.put(columnid, (String)updateMap.get(columnid) + ";" + value);
                    continue;
                }
                updateMap.put(columnid, value);
            }
            updateMap.put("workitemid", updateMap.containsKey("workitemid") ? (String)updateMap.get("workitemid") + ";" + workitemid : workitemid);
            updateMap.put("workiteminstance", updateMap.containsKey("workiteminstance") ? (String)updateMap.get("workiteminstance") + ";" + workiteminstance : workiteminstance);
            updateMap.put("usersequence", updateMap.containsKey("usersequence") ? (String)updateMap.get("usersequence") + ";" + String.valueOf(rowNum) : String.valueOf(rowNum));
        }
        this.processActionItems();
        if (updateMap.size() > 0) {
            updateMap.put("__actionclass", EditSDIWorkItem.class.getName());
            updateMap.put("__actionclasspropsmatch", "Y");
            this.updateSDIDetail(updateMap);
        }
        if (updateSequence) {
            if (this.checkForWIGroup(elementData)) {
                sequenceList = this.resetUserSequence(sequenceList);
            }
            this.updateSDIWorkItemSequence(sequenceList);
        }
    }

    private List resetUserSequence(List sequenceList) {
        int lastParentUserSeq = 0;
        int lastChildUserSeq = 0;
        for (int i = 0; i < sequenceList.size(); ++i) {
            Sequence data = (Sequence)sequenceList.get(i);
            if (data.getGroupid() != null && data.getGroupid().length() > 0 && data.getWorkitemtypeflag() != null && data.getWorkitemtypeflag().equals("P")) {
                data.setUsersequence(String.valueOf(++lastParentUserSeq));
                for (int j = i + 1; j < sequenceList.size(); ++j) {
                    Sequence childData = (Sequence)sequenceList.get(j);
                    if (childData.getGroupid() == null || !childData.getGroupid().equals(data.getGroupid()) || childData.getGroupinstance() == null || !childData.getGroupinstance().equals(data.getGroupinstance()) || childData.getWorkitemtypeflag() == null || !childData.getWorkitemtypeflag().equals("W")) continue;
                    childData.setUsersequence(String.valueOf(++lastChildUserSeq));
                }
                continue;
            }
            if (data.getGroupid() != null && data.getGroupid().length() > 0 && data.getWorkitemtypeflag() != null && data.getWorkitemtypeflag().equals("W") || data.getGroupid() != null && data.getGroupid().length() != 0) continue;
            data.setUsersequence(String.valueOf(++lastParentUserSeq));
        }
        return sequenceList;
    }

    private boolean checkForWIGroup(ElementData elementData) {
        boolean isGroup = false;
        for (int i = 0; i < elementData.size(); ++i) {
            String groupid = elementData.getColumnData(i, "groupid");
            if (groupid == null || groupid.length() <= 0) continue;
            isGroup = true;
            break;
        }
        return isGroup;
    }

    private void updateSDIWorkItemSequence(List sequenceList) throws SapphireException {
        DBUtil dbUtil = new DBUtil();
        dbUtil.setConnection(this.sapphireConnection);
        for (Object aSequenceList : sequenceList) {
            String currentusersequence;
            String usersequence;
            Sequence sequence = (Sequence)aSequenceList;
            String sdcid = sequence.getSdcid();
            String keyid1 = sequence.getKeyid1();
            String workitemid = sequence.getWorkitemid();
            String workiteminstance = sequence.getWorkiteminstance();
            Object[] keys = new String[]{sdcid, keyid1, workitemid, workiteminstance};
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select usersequence from sdiworkitem where sdcid = ? and keyid1 = ? and workitemid = ? and workiteminstance = ?", keys);
            if (ds == null || ds.size() <= 0 || (usersequence = sequence.getUsersequence()).equals(currentusersequence = ds.getValue(0, "usersequence"))) continue;
            Object[] upd_keys = new String[]{usersequence, sdcid, keyid1, workitemid, workiteminstance};
            dbUtil.executePreparedUpdate("update sdiworkitem set usersequence = ? where sdcid = ? and keyid1 = ? and workitemid = ? and workiteminstance = ?", upd_keys);
        }
    }

    class Sequence {
        private String sdcid;
        private String keyid1;
        private String keyid2;
        private String keyid3;
        private String workitemid;
        private String workiteminstance;
        private String usersequence;
        private String groupid;
        private String groupinstance;
        private String workitemtypeflag;

        public Sequence(String sdcid, String keyid1, String workitemid, String workiteminstance, String usersequence, String groupid, String groupinstance, String workitemtypeflag) {
            this.sdcid = sdcid;
            this.keyid1 = keyid1;
            this.workitemid = workitemid;
            this.workiteminstance = workiteminstance;
            this.usersequence = usersequence;
            this.groupid = groupid;
            this.groupinstance = groupinstance;
            this.workitemtypeflag = workitemtypeflag;
        }

        public String getSdcid() {
            return this.sdcid;
        }

        public void setSdcid(String sdcid) {
            this.sdcid = sdcid;
        }

        public String getKeyid1() {
            return this.keyid1;
        }

        public void setKeyid1(String keyid1) {
            this.keyid1 = keyid1;
        }

        public String getKeyid2() {
            return this.keyid2;
        }

        public void setKeyid2(String keyid2) {
            this.keyid2 = keyid2;
        }

        public String getKeyid3() {
            return this.keyid3;
        }

        public void setKeyid3(String keyid3) {
            this.keyid3 = keyid3;
        }

        public String getWorkitemid() {
            return this.workitemid;
        }

        public void setWorkitemid(String workitemid) {
            this.workitemid = workitemid;
        }

        public String getWorkiteminstance() {
            return this.workiteminstance;
        }

        public void setWorkiteminstance(String workiteminstance) {
            this.workiteminstance = workiteminstance;
        }

        public String getUsersequence() {
            return this.usersequence;
        }

        public void setUsersequence(String usersequence) {
            this.usersequence = usersequence;
        }

        public String getGroupid() {
            return this.groupid;
        }

        public void setGroupid(String groupid) {
            this.groupid = groupid;
        }

        public String getGroupinstance() {
            return this.groupinstance;
        }

        public void setGroupinstance(String groupinstance) {
            this.groupinstance = groupinstance;
        }

        public String getWorkitemtypeflag() {
            return this.workitemtypeflag;
        }

        public void setWorkitemtypeflag(String workitemtypeflag) {
            this.workitemtypeflag = workitemtypeflag;
        }
    }
}

