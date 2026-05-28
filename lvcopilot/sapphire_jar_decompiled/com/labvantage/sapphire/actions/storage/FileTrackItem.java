/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import com.labvantage.opal.actions.storageunit.AddStorageUnit;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.StorageUnitTypeDef;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.ReserveTrackItem;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class FileTrackItem
extends BaseAction
implements sapphire.action.FileTrackItem {
    public static final String FILLDIRECTION_HORIZONTAL_LEFT_TO_RIGHT = "Left to Right";
    public static final String FILLDIRECTION_HORIZONTAL_RIGHT_TO_LEFT = "Right to Left";
    public static final String FILLDIRECTION_VERTICAL_TOP_TO_BOTTOM = "Top to Bottom";
    public static final String FILLDIRECTION_VERTICAL_BOTTOM_TO_TOP = "Bottom to Top";
    public static final String ORIENTATION_ROWMAJOR = "Row Major";
    public static final String ORIENTATION_COLUMNMAJOR = "Column Major";
    public static final String NOLAYOUT = "No Layout";
    private String inputStorageUnitId;
    private String newStorageUnitType;
    private String overFlowFlag;
    private String inputMaxTIAllowed;
    private String inputFilingZone;
    private String inputArrayLayoutId;
    private String inputArrayLayoutVersionId;
    private String inputStartPosition;
    private String inputFillDirectionHorizontal;
    private String inputFillDirectionVertical;
    private String inputOrientation;
    private boolean hasChildStorageUnits = true;
    private String labelRowMin = "";
    private String labelRowMax = "";
    private String labelColMin = "";
    private String labelColMax = "";
    private List<String> newStorageUnitList = new ArrayList<String>();
    private String currentStorageUnitId = "";
    private String currentStorageLayout = "";
    private DataSet currentStorageChildDataSet;
    private int currentPositionIndex = -1;
    private int inputStorageIndex = 0;
    private boolean isTopToBottom;
    private boolean isLeftToRight;
    private boolean isRowMajor;
    private boolean isCurrentStorageRowMajor = true;
    private String currentStorageFillDirectionHorizontal;
    private String currentStorageFillDirectionVertical;
    private String currentStorageOrientation;
    private int currentStorageMaxTIAllowed;
    private int currentStorageContentCount;

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String storageunitid;
        DataSet trackitemDS;
        this.newStorageUnitList.clear();
        String input_trackitemid = actionProps.getProperty("trackitemid").trim();
        String input_storageunitsdcid = actionProps.getProperty("storageunitsdcid").trim();
        String input_storageunitkeyid1 = actionProps.getProperty("storageunitkeyid1").trim();
        this.inputStorageUnitId = actionProps.getProperty("storageunitid").trim();
        this.inputMaxTIAllowed = actionProps.getProperty("maxtiallowed").trim();
        this.newStorageUnitType = actionProps.getProperty("storageunittype").trim();
        this.overFlowFlag = actionProps.getProperty("overflowflag", "I").trim();
        this.inputFilingZone = actionProps.getProperty("filingzone").trim();
        this.inputArrayLayoutId = actionProps.getProperty("arraylayoutid").trim();
        this.inputArrayLayoutVersionId = actionProps.getProperty("arraylayoutversionid").trim();
        this.inputStartPosition = actionProps.getProperty("startposition").trim().toLowerCase();
        this.inputFillDirectionHorizontal = actionProps.getProperty("filldirection_horizontal").trim();
        this.inputFillDirectionVertical = actionProps.getProperty("filldirection_vertical").trim();
        this.inputOrientation = actionProps.getProperty("orientation").trim();
        String sysuserid = "(system)".equals(this.connectionInfo.getSysuserId()) ? "" : this.connectionInfo.getSysuserId();
        boolean bypasscustodycheck = "Y".equalsIgnoreCase(actionProps.getProperty("bypasscustodycheck", "Y"));
        if ("(system)".equals(this.connectionInfo.getSysuserId())) {
            throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("FileTrackItem Action must be run as a registered application user"));
        }
        if (input_trackitemid.length() == 0) {
            String input_trackitemsdcid = actionProps.getProperty("trackitemsdcid").trim();
            String input_trackitemkeyid1 = actionProps.getProperty("trackitemkeyid1").trim();
            if (input_trackitemsdcid.length() <= 0 || input_trackitemkeyid1.length() <= 0) throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("Either trackitemid or trackitemsdcid/trackitemkeyid1 must be given"));
            PropertyList sdcPropertyList = this.getSDCProcessor().getPropertyList(input_trackitemsdcid);
            if (sdcPropertyList == null) {
                throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("Invalid Trackitem SDC") + ": " + input_trackitemsdcid);
            }
            String tableid = sdcPropertyList.getProperty("tableid");
            String keycolid1 = sdcPropertyList.getProperty("keycolid1");
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select s." + keycolid1 + ", t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.currentstorageunitid, (select count(t1.trackitemid) from trackitem t1 where t1.linksdcid = t.linksdcid and t1.linkkeyid1 = t.linkkeyid1) ticount from " + tableid + " s LEFT OUTER JOIN trackitem t on t.linksdcid = '" + input_trackitemsdcid + "' and t.linkkeyid1 = s." + keycolid1 + " where s." + keycolid1 + " in (" + safeSQL.addIn(input_trackitemkeyid1, ";") + ")";
            trackitemDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (trackitemDS != null) {
                String[] s;
                DataSet addDS = new DataSet();
                for (int i = 0; i < trackitemDS.size(); ++i) {
                    int ticount = trackitemDS.getInt(i, "ticount", 0);
                    if (ticount == 0) {
                        addDS.copyRow(trackitemDS, i, 1);
                        continue;
                    }
                    if (ticount <= 1) continue;
                    throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("Multiple trackitem records found for item being filed"));
                }
                if (addDS.size() > 0) {
                    if (!bypasscustodycheck) {
                        throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("Custody check failed. Used must have custody of items being filed."));
                    }
                    bypasscustodycheck = true;
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "TrackItemSDC");
                    props.setProperty("copies", String.valueOf(addDS.size()));
                    props.setProperty("linksdcid", input_trackitemsdcid);
                    props.setProperty("linkkeyid1", addDS.getColumnValues(keycolid1, ";"));
                    props.setProperty("custodialuserid", sysuserid);
                    props.setProperty("custodialdepartmentid", this.connectionInfo.getDefaultDepartment());
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    String newtrackitemid = props.getProperty("newkeyid1");
                    if (newtrackitemid.length() <= 0) throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("Error creating trackitem records"));
                    trackitemDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                }
                DataSet tempds = new DataSet();
                for (String keyid1 : s = StringUtil.split(input_trackitemkeyid1, ";")) {
                    int row = trackitemDS.findRow(keycolid1, keyid1);
                    if (row == -1) continue;
                    tempds.copyRow(trackitemDS, row, 1);
                }
                trackitemDS.clear();
                trackitemDS = tempds;
            }
        } else {
            String[] s;
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select t.trackitemid, t.custodialuserid, t.custodialdepartmentid, t.currentstorageunitid, t.linksdcid, t.linkkeyid1 from trackitem t where t.trackitemid in (" + safeSQL.addIn(input_trackitemid, ";") + ")";
            trackitemDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            DataSet tempds = new DataSet();
            for (String trackitemid : s = StringUtil.split(input_trackitemid, ";")) {
                int row = trackitemDS.findRow("trackitemid", trackitemid);
                if (row == -1) continue;
                tempds.copyRow(trackitemDS, row, 1);
            }
            trackitemDS.clear();
            trackitemDS = tempds;
        }
        if (trackitemDS == null) throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("Trackitem(s) may not exist"));
        if (!bypasscustodycheck) {
            for (int i = 0; i < trackitemDS.size(); ++i) {
                String custodialuserid = trackitemDS.getString(i, "custodialuserid", "");
                String custodialdepartmentid = trackitemDS.getString(i, "custodialdepartmentid", "");
                if (custodialuserid.length() > 0 && !sysuserid.equals(custodialuserid)) {
                    throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("Trackitem is in other user's custody"));
                }
                if (custodialdepartmentid.length() <= 0 || OpalUtil.toList(this.connectionInfo.getDepartmentList(), ";").contains(custodialdepartmentid)) continue;
                throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("User is not a member of Trackitem's department"));
            }
        }
        if (this.inputStorageUnitId.length() == 0) {
            if (input_storageunitsdcid.length() > 0 && input_storageunitkeyid1.length() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                DataSet _ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid from storageunit where linksdcid = " + safeSQL.addVar(input_storageunitsdcid) + " and linkkeyid1 in (" + safeSQL.addIn(input_storageunitkeyid1, ";") + ")", safeSQL.getValues());
                if (!OpalUtil.isNotEmpty(_ds)) throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("No storage unit found for passed in storageunitsdcid and storageunitkeyid1"));
                this.inputStorageUnitId = _ds.getColumnValues("storageunitid", ";");
                actionProps.setProperty("storageunitid", this.inputStorageUnitId);
            } else {
                if (this.newStorageUnitType.length() <= 0) throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("Missing target storageunit information."));
                if (!StorageUnitTypeDef.getInstance().getTypeMap(this.getQueryProcessor()).containsKey(this.newStorageUnitType)) {
                    throw new SapphireException("FileTrackItem Error", "VALIDATION", this.getTranslationProcessor().translate("Invalid storageunittype") + ": " + this.newStorageUnitType);
                }
            }
        }
        this.setCurrentStorageUnit(true);
        for (int i = 0; i < trackitemDS.size() && (storageunitid = this.getNextAvailableStorageUnitId()) != null && storageunitid.length() > 0; ++i) {
            trackitemDS.setString(i, "currentstorageunitid", storageunitid);
        }
        DataSet ds = new DataSet();
        for (int i = 0; i < trackitemDS.size(); ++i) {
            if (trackitemDS.getString(i, "currentstorageunitid", "").length() <= 0) continue;
            int row = ds.addRow();
            ds.setString(row, "trackitemid", trackitemDS.getString(i, "trackitemid"));
            ds.setString(row, "currentstorageunitid", trackitemDS.getString(i, "currentstorageunitid"));
        }
        if ("Y".equals(actionProps.getProperty("reservecurrentlocation", "N")) && ds.size() > 0) {
            DataSet dataSet2;
            String sql;
            if (ds.size() > 1000) {
                String rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", ds.getColumnValues("trackitemid", ";"), null, null);
                sql = "select trackitemid, currentstorageunitid, (select su.maxtiallowed from storageunit su where su.storageunitid = trackitem.currentstorageunitid) maxtiallowed from trackitem where trackitemid in (select r.keyid1 from rsetitems r where r.rsetid = ?) and currentstorageunitid is not null";
                dataSet2 = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql = "select trackitemid, currentstorageunitid, (select su.maxtiallowed from storageunit su where su.storageunitid = trackitem.currentstorageunitid) maxtiallowed from trackitem where trackitemid in (" + safeSQL.addIn(ds.getColumnValues("trackitemid", ";"), ";") + ") and currentstorageunitid is not null";
                dataSet2 = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
            if (OpalUtil.isNotEmpty(dataSet2)) {
                DataSet reserveDS = new DataSet();
                for (int i = 0; i < dataSet2.size(); ++i) {
                    if (!"1".equals(dataSet2.getValue(i, "maxtiallowed"))) continue;
                    reserveDS.copyRow(dataSet2, i, 1);
                }
                if (reserveDS.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("trackitemid", reserveDS.getColumnValues("trackitemid", ";"));
                    props.setProperty("storageunitid", reserveDS.getColumnValues("currentstorageunitid", ";"));
                    props.setProperty("propsmatch", "Y");
                    this.getActionProcessor().processActionClass(ReserveTrackItem.class.getName(), props);
                }
            }
        }
        if (ds.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "TrackItemSDC");
            props.setProperty("keyid1", ds.getColumnValues("trackitemid", ";"));
            props.setProperty("currentstorageunitid", ds.getColumnValues("currentstorageunitid", ";"));
            props.setProperty("auditreason", actionProps.getProperty("auditreason"));
            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
            props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        actionProps.setProperty("newstorageunitid", OpalUtil.toDelimitedString(this.newStorageUnitList, ";"));
    }

    private String getNextAvailableStorageUnitId() throws SapphireException {
        String storageunitid = "";
        if (this.hasChildStorageUnits) {
            int nextAvailablePosition = this.getNextAvailablePosition(this.currentStorageChildDataSet, this.currentPositionIndex);
            if (nextAvailablePosition == -1) {
                this.setCurrentStorageUnit(false);
                storageunitid = this.getNextAvailableStorageUnitId();
            } else {
                this.currentPositionIndex = nextAvailablePosition;
                storageunitid = this.currentStorageChildDataSet.getString(nextAvailablePosition, "storageunitid");
            }
        } else if (this.currentStorageMaxTIAllowed == -1) {
            storageunitid = this.currentStorageUnitId;
        } else if (this.currentStorageContentCount < this.currentStorageMaxTIAllowed) {
            ++this.currentStorageContentCount;
            storageunitid = this.currentStorageUnitId;
        } else if (this.setCurrentStorageUnit(false).length() > 0) {
            storageunitid = this.getNextAvailableStorageUnitId();
        }
        return storageunitid;
    }

    private String setCurrentStorageUnit(boolean initial) throws SapphireException {
        String[] s;
        String nextstorageunitid = "";
        if (this.inputStorageUnitId.length() > 0 && (s = StringUtil.split(this.inputStorageUnitId, ";")).length > this.inputStorageIndex) {
            nextstorageunitid = s[this.inputStorageIndex++];
        }
        if (nextstorageunitid.length() == 0) {
            nextstorageunitid = this.addNewStorageUnit(initial);
        }
        if (nextstorageunitid != null && nextstorageunitid.length() > 0) {
            this.populateCurrentStorageUnitInformation(nextstorageunitid);
        }
        return nextstorageunitid == null ? "" : nextstorageunitid;
    }

    private String addNewStorageUnit(boolean initial) throws SapphireException {
        this.currentStorageUnitId = "";
        String newStorageUnitId = "";
        if (this.newStorageUnitType.length() > 0) {
            if (initial || "C".equalsIgnoreCase(this.overFlowFlag)) {
                PropertyList storageUnitTypeDefinition = StorageUnitTypeDef.getInstance().getTypeDefinition(this.getQueryProcessor(), this.newStorageUnitType);
                String propertytreeid = storageUnitTypeDefinition.getProperty("propertytreeid");
                PropertyList props = new PropertyList();
                props.setProperty("storageunittype", this.newStorageUnitType);
                props.setProperty("copies", "1");
                props.setProperty("trackitem_custodialuserid", this.connectionInfo.getSysuserId());
                props.setProperty("trackitem_custodialdepartmentid", this.connectionInfo.getDefaultDepartment());
                if (OpalUtil.isNotEmpty(this.inputArrayLayoutId)) {
                    props.setProperty("arraylayoutid", this.inputArrayLayoutId);
                    props.setProperty("arraylayoutversionid", this.inputArrayLayoutVersionId);
                }
                if (propertytreeid.equals(NOLAYOUT)) {
                    if (this.inputMaxTIAllowed.length() == 0) {
                        this.inputMaxTIAllowed = storageUnitTypeDefinition.getProperty("maxtiallowed", "-1");
                    }
                    props.setProperty("storageunit_maxtiallowed", this.inputMaxTIAllowed);
                }
                this.getActionProcessor().processActionClass(AddStorageUnit.class.getName(), props);
                newStorageUnitId = props.getProperty("storageunitid");
                this.newStorageUnitList.add(newStorageUnitId);
            } else if (!"I".equalsIgnoreCase(this.overFlowFlag)) {
                throw new SapphireException("FileTrackItem", "VALIDATION", this.getTranslationProcessor().translate("Not enough space available for filing. Must set overflow flag to 'C' to add more storage units for filing."));
            }
        } else if (!"I".equalsIgnoreCase(this.overFlowFlag)) {
            throw new SapphireException("FileTrackItem", "VALIDATION", this.getTranslationProcessor().translate("Not enough space available for filing. Must set storage unit type for new storage units to be created."));
        }
        return newStorageUnitId;
    }

    private void populateCurrentStorageUnitInformation(String storageunitid) throws SapphireException {
        this.currentStorageUnitId = storageunitid;
        PropertyList currentStorageProps = StorageUnitTypeDef.getInstance().getTypeDefinitionByID(this.getQueryProcessor(), this.currentStorageUnitId);
        if ("C".equalsIgnoreCase(this.overFlowFlag) && this.newStorageUnitType.length() == 0) {
            this.newStorageUnitType = currentStorageProps.getProperty("nodeid");
        }
        this.currentStorageLayout = currentStorageProps.getProperty("propertytreeid");
        boolean bl = this.hasChildStorageUnits = !NOLAYOUT.equals(this.currentStorageLayout);
        if (this.hasChildStorageUnits) {
            PropertyList indexOrder = currentStorageProps.getPropertyListNotNull("indexorder");
            String string = this.currentStorageFillDirectionVertical = "Top->Bottom".equals(indexOrder.getProperty("vertical")) ? FILLDIRECTION_VERTICAL_TOP_TO_BOTTOM : FILLDIRECTION_VERTICAL_BOTTOM_TO_TOP;
            if (this.inputFillDirectionVertical.length() == 0) {
                this.isTopToBottom = FILLDIRECTION_VERTICAL_TOP_TO_BOTTOM.equals(this.currentStorageFillDirectionVertical);
            } else {
                this.isTopToBottom = FILLDIRECTION_VERTICAL_TOP_TO_BOTTOM.equals(this.inputFillDirectionVertical);
                if (this.currentStorageFillDirectionVertical.equals(FILLDIRECTION_VERTICAL_BOTTOM_TO_TOP)) {
                    this.isTopToBottom = !this.isTopToBottom;
                }
            }
            String string2 = this.currentStorageFillDirectionHorizontal = "Left->Right".equals(indexOrder.getProperty("horizontal")) ? FILLDIRECTION_HORIZONTAL_LEFT_TO_RIGHT : FILLDIRECTION_HORIZONTAL_RIGHT_TO_LEFT;
            if (this.inputFillDirectionHorizontal.length() == 0) {
                this.isLeftToRight = FILLDIRECTION_HORIZONTAL_LEFT_TO_RIGHT.equals(this.currentStorageFillDirectionHorizontal);
            } else {
                this.isLeftToRight = FILLDIRECTION_HORIZONTAL_LEFT_TO_RIGHT.equals(this.inputFillDirectionHorizontal);
                if (this.currentStorageFillDirectionHorizontal.equals(FILLDIRECTION_HORIZONTAL_RIGHT_TO_LEFT)) {
                    this.isLeftToRight = !this.isLeftToRight;
                }
            }
            this.currentStorageOrientation = currentStorageProps.getProperty("orientation", ORIENTATION_ROWMAJOR);
            this.isRowMajor = ORIENTATION_ROWMAJOR.equals(this.inputOrientation.length() == 0 ? this.currentStorageOrientation : this.inputOrientation);
            this.isCurrentStorageRowMajor = ORIENTATION_ROWMAJOR.equals(this.currentStorageOrientation);
            this.currentStorageChildDataSet = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, parentid, storageunitindex, storageunitlabel, labelrow, labelcol, arraylayoutzone, maxtiallowed, (select count(t.trackitemid) from trackitem t where t.currentstorageunitid = storageunit.storageunitid) contentcount, (select count(r.storageunitid) from reservestorageunit r where r.storageunitid = storageunit.storageunitid) reservecount from storageunit where parentid = ? order by storageunitindex", (Object[])new String[]{this.currentStorageUnitId});
            ArrayList<String> listColLabel = new ArrayList<String>();
            ArrayList<String> listRowLabel = new ArrayList<String>();
            if (this.currentStorageChildDataSet != null) {
                int i;
                for (i = 0; i < this.currentStorageChildDataSet.size(); ++i) {
                    String labelcol = this.currentStorageChildDataSet.getString(i, "labelcol", "");
                    String labelrow = this.currentStorageChildDataSet.getString(i, "labelrow", "");
                    if (!listColLabel.contains(labelcol)) {
                        listColLabel.add(labelcol);
                    }
                    if (listRowLabel.contains(labelrow)) continue;
                    listRowLabel.add(labelrow);
                }
                if (this.isCurrentStorageRowMajor) {
                    this.labelRowMin = (String)listRowLabel.get(0);
                    this.labelRowMax = (String)listRowLabel.get(listRowLabel.size() - 1);
                    this.labelColMin = (String)listColLabel.get(0);
                    this.labelColMax = (String)listColLabel.get(listColLabel.size() - 1);
                } else {
                    this.labelRowMin = (String)listColLabel.get(0);
                    this.labelRowMax = (String)listColLabel.get(listRowLabel.size() - 1);
                    this.labelColMin = (String)listRowLabel.get(0);
                    this.labelColMax = (String)listRowLabel.get(listColLabel.size() - 1);
                }
                if (this.inputStartPosition.length() > 0) {
                    for (i = 0; i < this.currentStorageChildDataSet.size(); ++i) {
                        if (!this.inputStartPosition.equals(this.currentStorageChildDataSet.getString(i, "storageunitlabel", "").toLowerCase())) continue;
                        this.currentPositionIndex = i;
                        break;
                    }
                    if (this.currentPositionIndex == -1) {
                        throw new SapphireException("FileTrackItem", "VALIDATION", this.getTranslationProcessor().translate("Invalid start position given") + " [" + this.inputStartPosition + "]");
                    }
                } else {
                    this.currentPositionIndex = 0;
                }
            }
        } else {
            this.currentStorageMaxTIAllowed = -1;
            this.currentStorageContentCount = 0;
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select maxtiallowed, (select count(t.trackitemid) from trackitem t where t.currentstorageunitid = storageunit.storageunitid) contentcount from storageunit where storageunitid = ?", (Object[])new String[]{this.currentStorageUnitId});
            if (ds != null && ds.size() > 0) {
                this.currentStorageMaxTIAllowed = ds.getInt(0, "maxtiallowed");
                this.currentStorageContentCount = ds.getInt(0, "contentcount");
            }
        }
    }

    private int getNextAvailablePosition(DataSet ds, int currentPosition) {
        String nextLabelCol;
        String nextLabelRow;
        if (currentPosition >= ds.size()) {
            return -1;
        }
        if (this.isPositionEmpty(ds, currentPosition)) {
            return currentPosition;
        }
        if (!(this.inputFillDirectionHorizontal.length() != 0 && !this.inputFillDirectionHorizontal.equals(this.currentStorageFillDirectionHorizontal) || this.inputFillDirectionVertical.length() != 0 && !this.inputFillDirectionVertical.equals(this.currentStorageFillDirectionVertical) || this.inputOrientation.length() != 0 && !this.inputOrientation.equals(this.currentStorageOrientation))) {
            return this.getNextAvailablePosition(ds, currentPosition + 1);
        }
        String currentLabelRow = ds.getString(currentPosition, this.isCurrentStorageRowMajor ? "labelrow" : "labelcol");
        String currentLabelCol = ds.getString(currentPosition, this.isCurrentStorageRowMajor ? "labelcol" : "labelrow");
        if (this.isTopToBottom) {
            if (this.isLeftToRight) {
                if (this.isRowMajor) {
                    if (currentLabelRow.equals(this.labelRowMax)) {
                        if (currentLabelCol.equals(this.labelColMax)) {
                            nextLabelRow = "-1";
                            nextLabelCol = "-1";
                        } else {
                            nextLabelRow = currentLabelRow;
                            nextLabelCol = FileTrackItem.increment(currentLabelCol);
                        }
                    } else if (currentLabelCol.equals(this.labelColMax)) {
                        nextLabelRow = FileTrackItem.increment(currentLabelRow);
                        nextLabelCol = this.labelColMin;
                    } else {
                        nextLabelRow = currentLabelRow;
                        nextLabelCol = FileTrackItem.increment(currentLabelCol);
                    }
                } else if (currentLabelRow.equals(this.labelRowMax)) {
                    if (currentLabelCol.equals(this.labelColMax)) {
                        nextLabelRow = "-1";
                        nextLabelCol = "-1";
                    } else {
                        nextLabelRow = this.labelRowMin;
                        nextLabelCol = FileTrackItem.increment(currentLabelCol);
                    }
                } else {
                    nextLabelRow = FileTrackItem.increment(currentLabelRow);
                    nextLabelCol = currentLabelCol;
                }
            } else if (this.isRowMajor) {
                if (currentLabelRow.equals(this.labelRowMax)) {
                    if (currentLabelCol.equals(this.labelColMin)) {
                        nextLabelRow = "-1";
                        nextLabelCol = "-1";
                    } else {
                        nextLabelRow = currentLabelRow;
                        nextLabelCol = FileTrackItem.decrement(currentLabelCol);
                    }
                } else if (currentLabelCol.equals(this.labelColMin)) {
                    nextLabelRow = FileTrackItem.increment(currentLabelRow);
                    nextLabelCol = this.labelColMax;
                } else {
                    nextLabelRow = currentLabelRow;
                    nextLabelCol = FileTrackItem.decrement(currentLabelCol);
                }
            } else if (currentLabelRow.equals(this.labelRowMax)) {
                if (currentLabelCol.equals(this.labelColMin)) {
                    nextLabelRow = "-1";
                    nextLabelCol = "-1";
                } else {
                    nextLabelRow = this.labelRowMin;
                    nextLabelCol = FileTrackItem.decrement(currentLabelCol);
                }
            } else {
                nextLabelRow = FileTrackItem.increment(currentLabelRow);
                nextLabelCol = currentLabelCol;
            }
        } else if (this.isLeftToRight) {
            if (this.isRowMajor) {
                if (currentLabelRow.equals(this.labelRowMin)) {
                    if (currentLabelCol.equals(this.labelColMax)) {
                        nextLabelRow = "-1";
                        nextLabelCol = "-1";
                    } else {
                        nextLabelRow = currentLabelRow;
                        nextLabelCol = FileTrackItem.increment(currentLabelCol);
                    }
                } else if (currentLabelCol.equals(this.labelColMax)) {
                    nextLabelRow = FileTrackItem.decrement(currentLabelRow);
                    nextLabelCol = this.labelColMin;
                } else {
                    nextLabelRow = currentLabelRow;
                    nextLabelCol = FileTrackItem.increment(currentLabelCol);
                }
            } else if (currentLabelRow.equals(this.labelRowMin)) {
                if (currentLabelCol.equals(this.labelColMax)) {
                    nextLabelRow = "-1";
                    nextLabelCol = "-1";
                } else {
                    nextLabelRow = this.labelRowMax;
                    nextLabelCol = FileTrackItem.increment(currentLabelCol);
                }
            } else {
                nextLabelRow = FileTrackItem.decrement(currentLabelRow);
                nextLabelCol = currentLabelCol;
            }
        } else if (this.isRowMajor) {
            if (currentLabelRow.equals(this.labelRowMin)) {
                if (currentLabelCol.equals(this.labelColMin)) {
                    nextLabelRow = "-1";
                    nextLabelCol = "-1";
                } else {
                    nextLabelRow = currentLabelRow;
                    nextLabelCol = FileTrackItem.decrement(currentLabelCol);
                }
            } else if (currentLabelCol.equals(this.labelColMin)) {
                nextLabelRow = FileTrackItem.decrement(currentLabelRow);
                nextLabelCol = this.labelColMax;
            } else {
                nextLabelRow = currentLabelRow;
                nextLabelCol = FileTrackItem.decrement(currentLabelCol);
            }
        } else if (currentLabelRow.equals(this.labelRowMin)) {
            if (currentLabelCol.equals(this.labelColMin)) {
                nextLabelRow = "-1";
                nextLabelCol = "-1";
            } else {
                nextLabelRow = this.labelRowMax;
                nextLabelCol = FileTrackItem.decrement(currentLabelCol);
            }
        } else {
            nextLabelRow = FileTrackItem.decrement(currentLabelRow);
            nextLabelCol = currentLabelCol;
        }
        if ("-1".equals(nextLabelRow) || "-1".equals(nextLabelCol)) {
            return -1;
        }
        int index = this.findIndexByLabel(ds, nextLabelRow, nextLabelCol);
        return this.getNextAvailablePosition(ds, index);
    }

    private int findIndexByLabel(DataSet ds, String labelRow, String labelCol) {
        int index = -1;
        for (int i = 0; i < ds.size(); ++i) {
            if (!labelRow.equals(ds.getString(i, this.isCurrentStorageRowMajor ? "labelrow" : "labelcol")) || !labelCol.equals(ds.getString(i, this.isCurrentStorageRowMajor ? "labelcol" : "labelrow"))) continue;
            index = i;
            break;
        }
        return index;
    }

    private boolean isPositionEmpty(DataSet ds, int index) {
        int maxtiallowed = ds.getInt(index, "maxtiallowed", 0);
        int contentcount = ds.getInt(index, "contentcount", 0);
        int reservecount = ds.getInt(index, "reservecount", 0);
        if (reservecount == 0 && contentcount < maxtiallowed && (OpalUtil.isEmpty(this.inputFilingZone) || this.inputFilingZone.equals(ds.getString(index, "arraylayoutzone")))) {
            ds.setNumber(index, "contentcount", ++contentcount);
            return true;
        }
        return false;
    }

    private static String increment(String number) {
        if ("9".equals(number)) {
            return "10";
        }
        if ("99".equals(number)) {
            return "100";
        }
        char[] cars = number.toUpperCase().toCharArray();
        for (int i = cars.length - 1; i >= 0; --i) {
            if (cars[i] == 'Z') {
                cars[i] = 65;
                continue;
            }
            if (cars[i] == '9') {
                cars[i] = 48;
                continue;
            }
            int n = i;
            cars[n] = (char)(cars[n] + '\u0001');
            break;
        }
        return String.valueOf(cars);
    }

    private static String decrement(String number) {
        if ("10".equals(number)) {
            return "9";
        }
        if ("100".equals(number)) {
            return "99";
        }
        char[] cars = number.toUpperCase().toCharArray();
        for (int i = cars.length - 1; i >= 0; --i) {
            if (cars[i] == 'A') {
                cars[i] = 90;
                continue;
            }
            if (cars[i] == '0') {
                cars[i] = 57;
                continue;
            }
            int n = i;
            cars[n] = (char)(cars[n] - '\u0001');
            break;
        }
        return String.valueOf(cars);
    }
}

