/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class FindFreeStorageSpace
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        int search_freespace;
        PropertyList searchstoragelocation;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        boolean rootonly = "Y".equals(ajaxResponse.getRequestParameter("rootonly", "N"));
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid");
        long start = System.currentTimeMillis();
        if (storageunitid.length() == 0) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("StorageUnitSDC");
            sdiRequest.setRequestItem("primary");
            sdiRequest.setQueryFrom("storageunit");
            sdiRequest.setQueryOrderBy("storageunitid");
            sdiRequest.setQueryWhere("linksdcid = 'PhysicalStore'");
            DataSet primary = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
            storageunitid = primary.getColumnValues("storageunitid", ";");
        }
        List<String> storageunitlist = OpalUtil.toList(storageunitid, ";");
        String search_storageunittype = ajaxResponse.getRequestParameter("search_storageunittype");
        String search_freespace_text = ajaxResponse.getRequestParameter("search_freespace");
        String search_studyid = ajaxResponse.getRequestParameter("search_studyid");
        String search_sampletypeid = ajaxResponse.getRequestParameter("search_sampletypeid");
        try {
            searchstoragelocation = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getPropertyListNotNull("searchstoragelocation");
        }
        catch (SapphireException e) {
            searchstoragelocation = new PropertyList();
        }
        PropertyListCollection storageunittypes = searchstoragelocation.getCollectionNotNull("storageunittypes");
        HashSet<String> spaceAvailableSet = new HashSet<String>();
        try {
            search_freespace = Integer.parseInt(search_freespace_text);
        }
        catch (NumberFormatException e) {
            search_freespace = 1;
        }
        long dsstart = System.currentTimeMillis();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "";
        sql = this.getConnectionProcessor().isOra() ? "select storageunit.storageunitid, storageunit.parentid, storageunit.storageunittype, storageunit.linksdcid, storageunit.linkkeyid1, storageunit.maxtiallowed, storageunit.spaceavailflag, storageunit.storageunitsize, storageunitstats.specimencapacity, storageunitstats.specimencount, storageunitstats.lastnodeflag, storagerestriction.restrictionbasedon, storagerestriction.propertyid, storagerestriction.operator, storagerestriction.propertyvalue from storageunit   left outer join storagerestriction on storagerestriction.storageunitid = storageunit.storageunitid   left outer join storageunitstats on storageunitstats.storageunitid = storageunit.storageunitid where storageunit.storageunittype not in ('BoxPos', 'Well') connect by prior storageunit.storageunitid = storageunit.parentid start with storageunit.storageunitid in (" + safeSQL.addIn(storageunitlist) + ")" : "WITH StorageUnitTree (storageunitid) AS (    SELECT su.storageunitid    FROM storageunit AS su    WHERE su.storageunitid in (" + safeSQL.addIn(storageunitlist) + ")    UNION ALL    SELECT su.storageunitid    FROM storageunit AS su    INNER JOIN StorageUnitTree AS d    ON su.parentid = d.storageunitid    ) select storageunit.storageunitid, storageunit.parentid, storageunit.storageunittype, storageunit.linksdcid, storageunit.linkkeyid1,  storageunit.maxtiallowed, storageunit.spaceavailflag, storageunit.storageunitsize, storageunitstats.specimencapacity, storageunitstats.specimencount,  storageunitstats.lastnodeflag, storagerestriction.restrictionbasedon, storagerestriction.propertyid, storagerestriction.operator,  storagerestriction.propertyvalue  from storageunit        left outer join storagerestriction on storagerestriction.storageunitid = storageunit.storageunitid        left outer join storageunitstats on storageunitstats.storageunitid = storageunit.storageunitid  where storageunit.storageunittype not in ('BoxPos', 'Well')  and storageunit.storageunitid in (SELECT st.storageunitid FROM StorageUnitTree st)";
        DataSet maindataset = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (maindataset != null && maindataset.size() > 0) {
            this.logger.info("Find free space Query took: " + (System.currentTimeMillis() - dsstart) + " ms and returned " + maindataset.size() + " rows.");
            HashMap<String, String> filter = new HashMap<String, String>();
            for (int i = 0; i < storageunittypes.size(); ++i) {
                String title;
                PropertyList pl = storageunittypes.getPropertyList(i);
                if (!"Y".equals(pl.getProperty("show")) || !OpalUtil.isNotEmpty(title = pl.getProperty("title").trim()) || !search_storageunittype.equals(title)) continue;
                String propertyid = pl.getProperty("propertyid");
                String propertyvalue = pl.getProperty("propertyvalue").trim();
                if (propertyid.length() <= 0 || propertyvalue.length() <= 0) continue;
                filter.put(propertyid, propertyvalue);
            }
            DataSet filteredds = maindataset.getFilteredDataSet(filter);
            if (filteredds != null && filteredds.size() > 0) {
                HashSet<String> processedSet = new HashSet<String>();
                for (int i = 0; i < filteredds.size(); ++i) {
                    String _storageunitid;
                    block32: {
                        boolean _isStorageContainer;
                        _storageunitid = filteredds.getString(i, "storageunitid");
                        String _parentid = filteredds.getString(i, "parentid");
                        if (processedSet.contains(_storageunitid)) continue;
                        processedSet.add(_storageunitid);
                        int _maxtiallowed = filteredds.getInt(i, "maxtiallowed", 0);
                        int _storageunitsize = filteredds.getInt(i, "storageunitsize", 0);
                        int _specimencapacity = filteredds.getInt(i, "specimencapacity", 0);
                        int _specimencount = filteredds.getInt(i, "specimencount", 0);
                        boolean _isLastNode = "Y".equals(filteredds.getString(i, "lastnodeflag", "N"));
                        boolean _isRootNode = filteredds.getString(i, "parentid", "").length() == 0;
                        boolean _isParentLastNode = false;
                        boolean _isParentAPackage = false;
                        int _parentRow = maindataset.findRow("storageunitid", _parentid);
                        if (_parentRow != -1) {
                            _isParentLastNode = "Y".equals(maindataset.getString(_parentRow, "lastnodeflag", "N"));
                            _isParentAPackage = "LV_Package".equals(maindataset.getString(_parentRow, "linksdcid"));
                        }
                        if (_isParentAPackage) continue;
                        int availableSpaceCount = 0;
                        boolean bl = _isStorageContainer = _isParentLastNode && filteredds.getString(i, "linksdcid", "").length() > 0;
                        if (_isStorageContainer) {
                            filter.clear();
                            filter.put("parentid", _parentid);
                            if (maindataset.getFilteredDataSet(filter).size() == 1) {
                                int _parentSpecimenCapacity = maindataset.getInt(_parentRow, "specimencapacity", 0);
                                int _parentSpecimenCount = maindataset.getInt(_parentRow, "specimencount", 0);
                                availableSpaceCount = _parentSpecimenCapacity - _parentSpecimenCount;
                            } else if (_maxtiallowed > 0) {
                                try {
                                    int trackitemcount = this.getQueryProcessor().getPreparedCount("select count(trackitemid) trackitemcount from trackitem where currentstorageunitid = ?", new String[]{_storageunitid});
                                    availableSpaceCount = _maxtiallowed - trackitemcount;
                                }
                                catch (SapphireException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    int trackitemcount = this.getQueryProcessor().getPreparedCount("select count(trackitemid) trackitemcount from trackitem where currentstorageunitid in (select su.storageunitid from storageunit su where su.parentid = ?)", new String[]{_storageunitid});
                                    availableSpaceCount = _storageunitsize - trackitemcount;
                                }
                                catch (SapphireException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            filter.clear();
                            filter.put("parentid", _storageunitid);
                            if (maindataset.getFilteredDataSet(filter).size() > 0) {
                                availableSpaceCount = _specimencapacity - _specimencount;
                                if (!rootonly && availableSpaceCount >= search_freespace) {
                                    this.addChildToAvailableSpaceSet(spaceAvailableSet, _storageunitid, maindataset);
                                }
                            } else {
                                availableSpaceCount = _maxtiallowed > 0 ? _maxtiallowed - _specimencount : 0;
                            }
                        }
                        if (availableSpaceCount < search_freespace) continue;
                        if (search_sampletypeid.length() > 0 || search_studyid.length() > 0) {
                            try {
                                if (!this.validateStorageRestrictions(maindataset, _storageunitid, search_sampletypeid, search_studyid)) {
                                }
                                break block32;
                            }
                            catch (SapphireException e) {
                                this.logger.error("FindFreeStorageSpace", "Sapphire Exception while validation Storage Restrictions: " + e.getMessage());
                            }
                            continue;
                        }
                    }
                    if (rootonly) {
                        if (storageunitlist.contains(_storageunitid)) {
                            spaceAvailableSet.add(_storageunitid);
                            continue;
                        }
                        this.addRootToAvailableSpaceSet(spaceAvailableSet, _storageunitid, maindataset, storageunitlist);
                        continue;
                    }
                    spaceAvailableSet.add(_storageunitid);
                    this.addParentToAvailableSpaceSet(spaceAvailableSet, _storageunitid, maindataset);
                }
            }
        }
        String usersearchoptions = "";
        try {
            String storagespacesearchoptions = "";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("search_storageunittype", search_storageunittype);
            jsonObject.put("search_freespace", search_freespace);
            jsonObject.put("search_studyid", search_studyid);
            jsonObject.put("search_sampletypeid", search_sampletypeid);
            usersearchoptions = jsonObject.toString();
            if (!"Y".equals(ajaxResponse.getRequestParameter("search_reset", "N"))) {
                storagespacesearchoptions = usersearchoptions;
            }
            new ConfigurationProcessor(this.getConnectionid()).setProfileProperty(this.getConnectionProcessor().getSapphireConnection().getSysuserId(), "userconfig_storagespacesearchoptions", storagespacesearchoptions);
        }
        catch (JSONException | SapphireException e) {
            e.printStackTrace();
        }
        this.logger.info("Find storage space took " + (System.currentTimeMillis() - start) + " ms");
        ajaxResponse.addCallbackArgument("storageunitids", OpalUtil.toDelimitedString(spaceAvailableSet, ";"));
        ajaxResponse.addCallbackArgument("storagespacesearchoptions", usersearchoptions);
        ajaxResponse.print();
    }

    private boolean validateStorageRestrictions(DataSet maindataset, String storageunitid, String search_sampletypeid, String search_studyid) throws SapphireException {
        boolean isRestrictionValid = true;
        DataSet restrictionsds = new DataSet();
        this.populateStorageRestrictions(restrictionsds, storageunitid, maindataset);
        for (int i = 0; i < restrictionsds.size(); ++i) {
            String sql;
            String propertyid = restrictionsds.getString(i, "propertyid");
            String operator = restrictionsds.getString(i, "operator");
            String propertyvalue = restrictionsds.getString(i, "propertyvalue");
            if (search_sampletypeid.length() > 0 && "sampletypeid".equals(propertyid)) {
                if ("Equals".equals(operator) && !search_sampletypeid.equals(propertyvalue)) {
                    isRestrictionValid = false;
                    break;
                }
                if ("Not Equals".equals(operator) && search_sampletypeid.equals(propertyvalue)) {
                    isRestrictionValid = false;
                    break;
                }
                if ("In".equals(operator)) {
                    if (!OpalUtil.toList(propertyvalue, ";").contains(search_sampletypeid)) {
                        isRestrictionValid = false;
                        break;
                    }
                } else if ("Not In".equals(operator)) {
                    if (OpalUtil.toList(propertyvalue, ";").contains(search_sampletypeid)) {
                        isRestrictionValid = false;
                        break;
                    }
                } else if ("Homogeneous".equals(operator)) {
                    sql = "select count(s_sample.s_sampleid) samplecount from s_sample, trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid and s_sample.sampletypeid != ? and trackitem.currentstorageunitid in (select su.storageunitid from storageunit su where su.storageunitid = ? or su.parentid = ?)";
                    if (this.getQueryProcessor().getPreparedCount(sql, new String[]{search_sampletypeid, storageunitid, storageunitid}) > 0) {
                        isRestrictionValid = false;
                        break;
                    }
                }
            }
            if (search_studyid.length() <= 0 || !"sstudyid".equals(propertyid)) continue;
            if ("Equals".equals(operator) && !search_studyid.equals(propertyvalue)) {
                isRestrictionValid = false;
                break;
            }
            if ("Not Equals".equals(operator) && search_studyid.equals(propertyvalue)) {
                isRestrictionValid = false;
                break;
            }
            if ("In".equals(operator)) {
                if (OpalUtil.toList(propertyvalue, ";").contains(search_studyid)) continue;
                isRestrictionValid = false;
                break;
            }
            if ("Not In".equals(operator)) {
                if (!OpalUtil.toList(propertyvalue, ";").contains(search_studyid)) continue;
                isRestrictionValid = false;
                break;
            }
            if (!"Homogeneous".equals(operator)) continue;
            sql = "select count(s_sample.s_sampleid) samplecount from s_sample, trackitem where trackitem.linksdcid = 'Sample' and trackitem.linkkeyid1 = s_sample.s_sampleid and s_sample.sstudyid != ? and trackitem.currentstorageunitid in (select su.storageunitid from storageunit su where su.storageunitid = ? or su.parentid = ?)";
            if (this.getQueryProcessor().getPreparedCount(sql, new String[]{search_studyid, storageunitid, storageunitid}) <= 0) continue;
            isRestrictionValid = false;
            break;
        }
        return isRestrictionValid;
    }

    private void populateStorageRestrictions(DataSet restrictionsds, String storageunitid, DataSet maindataset) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("storageunitid", storageunitid);
        DataSet filteredDataSet = maindataset.getFilteredDataSet(filter);
        if (filteredDataSet != null && filteredDataSet.size() > 0) {
            for (int i = 0; i < filteredDataSet.size(); ++i) {
                String propertyid;
                String restrictionbasedon = filteredDataSet.getString(i, "restrictionbasedon", "");
                if (!restrictionbasedon.equals("Sample") && !restrictionbasedon.equals("Sample Family") || !(propertyid = filteredDataSet.getString(i, "propertyid", "")).equals("sampletypeid") && !propertyid.equals("sstudyid")) continue;
                String operator = filteredDataSet.getString(i, "operator", "");
                String propertyvalue = filteredDataSet.getString(i, "propertyvalue", "");
                if ((operator.length() <= 0 || propertyvalue.length() <= 0) && !"Homogeneous".equals(operator)) continue;
                int r = restrictionsds.addRow();
                restrictionsds.setString(r, "restrictionbasedon", restrictionbasedon);
                restrictionsds.setString(r, "propertyid", propertyid);
                restrictionsds.setString(r, "operator", operator);
                restrictionsds.setString(r, "propertyvalue", propertyvalue);
            }
            String parentid = filteredDataSet.getString(0, "parentid", "");
            if (parentid.length() > 0) {
                this.populateStorageRestrictions(restrictionsds, parentid, maindataset);
            }
        }
    }

    private void addChildToAvailableSpaceSet(Set<String> availableSpaceSet, String storageunitid, DataSet ds) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("parentid", storageunitid);
        DataSet filterds = ds.getFilteredDataSet(filter);
        if (filterds != null) {
            for (int i = 0; i < filterds.size(); ++i) {
                String childstorageunitid = filterds.getString(i, "storageunitid");
                availableSpaceSet.add(childstorageunitid);
                this.addChildToAvailableSpaceSet(availableSpaceSet, childstorageunitid, filterds);
            }
        }
    }

    private void addParentToAvailableSpaceSet(Set<String> availableSpaceSet, String storageunitid, DataSet ds) {
        String parentid;
        int row = ds.findRow("storageunitid", storageunitid);
        if (row != -1 && (parentid = ds.getString(row, "parentid", "")).length() > 0) {
            availableSpaceSet.add(parentid);
            this.addParentToAvailableSpaceSet(availableSpaceSet, parentid, ds);
        }
    }

    private void addRootToAvailableSpaceSet(Set<String> availableSpaceSet, String storageunitid, DataSet ds, List<String> storageunitlist) {
        String parentid;
        int row = ds.findRow("storageunitid", storageunitid);
        if (row != -1 && (parentid = ds.getString(row, "parentid", "")).length() > 0) {
            if (storageunitlist.contains(parentid)) {
                availableSpaceSet.add(parentid);
            } else {
                this.addRootToAvailableSpaceSet(availableSpaceSet, parentid, ds, storageunitlist);
            }
        }
    }
}

