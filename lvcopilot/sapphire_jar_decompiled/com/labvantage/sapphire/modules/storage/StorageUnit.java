/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.storage;

import java.util.ArrayList;
import java.util.HashMap;
import sapphire.util.StringUtil;

public class StorageUnit {
    private String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private String id = null;
    private String suTypeHierarchyId = "";
    private String suTypeId = "";
    private String nodeid = "";
    private String propertytreeid = "";
    private ArrayList children = new ArrayList();
    private int childIdSequence = 1;
    private int level = -1;
    private StorageUnit parent = null;
    private String childIds = "";
    private String parentId = "";
    private String storageUnitType = "";
    private int suNodeCount = 1;
    private String suEnvironment = "";
    private String suLabel = "";
    private String suDesc = "";
    private String suMaxTIAllowed = "";
    private String suMoveableFlag = "";

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSuTypeHierarchyId() {
        return this.suTypeHierarchyId;
    }

    public void setSuTypeHierarchyId(String suTypeHierarchyId) {
        this.suTypeHierarchyId = suTypeHierarchyId;
    }

    public String getSuTypeId() {
        return this.suTypeId;
    }

    public void setSuTypeId(String suTypeId) {
        this.suTypeId = suTypeId;
    }

    public String getNodeid() {
        return this.nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    public String getPropertytreeid() {
        return this.propertytreeid;
    }

    public void setPropertytreeid(String propertytreeid) {
        this.propertytreeid = propertytreeid;
    }

    public int getChildIdSequence() {
        return this.childIdSequence;
    }

    public void setChildIdSequence(int childIdSequence) {
        this.childIdSequence = childIdSequence;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public StorageUnit getParent() {
        return this.parent;
    }

    public void setParent(StorageUnit parent) {
        this.parent = parent;
    }

    public String getChildIds() {
        return this.childIds;
    }

    public void setChildIds(String childIds) {
        this.childIds = childIds;
    }

    public String getParentId() {
        return this.parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getSuNodeCount() {
        return this.suNodeCount;
    }

    public void setSuNodeCount(int suNodeCount) {
        this.suNodeCount = suNodeCount;
    }

    public String getStorageUnitType() {
        return this.storageUnitType;
    }

    public void setStorageUnitType(String storageUnitType) {
        this.storageUnitType = storageUnitType;
    }

    public String getSuEnvironment() {
        return this.suEnvironment;
    }

    public void setSuEnvironment(String suEnvironment) {
        this.suEnvironment = suEnvironment;
    }

    public String getSuLabel() {
        return this.suLabel;
    }

    public void setSuLabel(String suLabel) {
        this.suLabel = suLabel;
    }

    public String getSuDesc() {
        return this.suDesc;
    }

    public void setSuDesc(String suDesc) {
        this.suDesc = suDesc;
    }

    public String getSuMaxTIAllowed() {
        return this.suMaxTIAllowed;
    }

    public void setSuMaxTIAllowed(String suMaxTIAllowed) {
        this.suMaxTIAllowed = suMaxTIAllowed;
    }

    public String getSuMoveableFlag() {
        return this.suMoveableFlag;
    }

    public void setSuMoveableFlag(String suMoveableFlag) {
        this.suMoveableFlag = suMoveableFlag;
    }

    public ArrayList getChildren() {
        return this.children;
    }

    public void associateChildStorageUnits(HashMap allStorageUnits) {
        if (this.parentId != null && this.parentId.trim().length() > 0) {
            this.parent = (StorageUnit)allStorageUnits.get(this.parentId);
        }
        if (this.childIds != null && this.childIds.trim().length() > 2) {
            this.childIds = this.childIds.substring(1, this.childIds.trim().length() - 1);
            String[] childIdsArr = StringUtil.split(this.childIds, ",");
            for (int count = 0; count < childIdsArr.length; ++count) {
                String tempChildId = childIdsArr[count];
                if (tempChildId == null || tempChildId.trim().length() <= 0) continue;
                this.children.add(allStorageUnits.get(tempChildId));
            }
        }
    }

    public String toString() {
        StringBuffer storageUnit = new StringBuffer();
        storageUnit.append(" Id: ").append(this.getId()).append(" sutypehierarchyid: ").append(this.getSuTypeHierarchyId()).append(" sutypeid: ").append(this.getSuTypeId()).append(" nodeid: ").append(this.getNodeid()).append(" propertytreeid: ").append(this.getPropertytreeid()).append(" sunodecount: ").append(this.getSuNodeCount()).append(" suenvironment: ").append(this.getSuEnvironment()).append(" level: ").append(this.getLevel()).append(" childids: ").append(this.getChildIds()).append(" childidsequence: ").append(this.getChildIdSequence()).append(" parentid: ").append(this.getParentId()).append(" label: ").append(this.getSuLabel()).append(" sudesc: ").append(this.getSuDesc()).append(" maxtiallowed: ").append(this.getSuMaxTIAllowed()).append(" moveableflag: ").append(this.getSuMoveableFlag());
        return storageUnit.toString();
    }

    public int calculateRowCount(int parentCount) {
        return parentCount * this.getSuNodeCount();
    }

    public int calculateSize(int parentCount) {
        int size = this.calculateRowCount(parentCount);
        int childrenSize = 0;
        for (int count = 0; count < this.children.size(); ++count) {
            StorageUnit childSU = (StorageUnit)this.children.get(count);
            childrenSize += childSU.calculateSize(1);
        }
        size += size * childrenSize;
        return size;
    }

    public void addChild(StorageUnit childStorageUnit) {
        this.children.add(childStorageUnit);
    }
}

