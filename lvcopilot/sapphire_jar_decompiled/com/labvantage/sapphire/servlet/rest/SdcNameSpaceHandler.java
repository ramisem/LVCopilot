/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.rest;

import com.labvantage.sapphire.gwt.shared.constants.DatasetNameConstants;
import com.labvantage.sapphire.servlet.rest.BaseNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.RestException;
import com.labvantage.sapphire.util.jndi.ServiceLocator;
import com.labvantage.sapphire.xml.Column;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SdcNameSpaceHandler
extends BaseNameSpaceHandler
implements DatasetNameConstants {
    private static HashMap<String, HashMap<String, PropertyList>> databaseResources = new HashMap();

    public static void policyChange(String databaseid) {
        databaseResources.entrySet().removeIf(stringHashMapEntry -> ((String)stringHashMapEntry.getKey()).startsWith(databaseid + ";"));
    }

    @Override
    public boolean requiresConnection() {
        return true;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public void process() throws Exception {
        block62: {
            block76: {
                block80: {
                    block79: {
                        block78: {
                            block77: {
                                block73: {
                                    block75: {
                                        block74: {
                                            block58: {
                                                block71: {
                                                    block72: {
                                                        block69: {
                                                            block70: {
                                                                block67: {
                                                                    block68: {
                                                                        block59: {
                                                                            block66: {
                                                                                block65: {
                                                                                    block64: {
                                                                                        block63: {
                                                                                            block60: {
                                                                                                block61: {
                                                                                                    block53: {
                                                                                                        cacheKey = this.sapphireConnection.getDatabaseId() + ";" + this.getRestPolicyNodeid() + ";" + this.sapphireConnection.getSysuserId();
                                                                                                        resources = SdcNameSpaceHandler.databaseResources.get(cacheKey);
                                                                                                        if (resources != null) break block53;
                                                                                                        sdiData = new SDIData();
                                                                                                        resources = new HashMap<K, V>();
                                                                                                        sdcResources = this.getPolicyPropertyList().getCollectionNotNull("sdcresources");
                                                                                                        sdcResources.index("resourceid");
                                                                                                        for (i = 0; i < sdcResources.size(); ++i) {
                                                                                                            block54: {
                                                                                                                sdcResource = sdcResources.getPropertyList(i);
                                                                                                                if (!sdcResource.getProperty("type").equals("P")) break block54;
                                                                                                                sdcResourceSdcid = sdcResource.getProperty("sdcid");
                                                                                                                sdcProps = this.getSDCProcessor().getPropertyList(sdcResourceSdcid);
                                                                                                                sdcResource.put("keycolid1", sdcProps.getProperty("keycolid1"));
                                                                                                                sdcResource.put("keycolid2", sdcProps.getProperty("keycolid2"));
                                                                                                                sdcResource.put("keycolid3", sdcProps.getProperty("keycolid3"));
                                                                                                                resources.put(sdcResource.getProperty("path"), sdcResource);
                                                                                                                subResources = sdcResource.getCollection("subresources");
                                                                                                                block13: for (j = 0; j < subResources.size(); ++j) {
                                                                                                                    block55: {
                                                                                                                        block57: {
                                                                                                                            block56: {
                                                                                                                                subResource = subResources.getPropertyList(j);
                                                                                                                                subResourcePolicy = sdcResources.getIndexedPropertyList(subResource.getProperty("resourceid"));
                                                                                                                                if (subResourcePolicy == null) continue;
                                                                                                                                subResource.setProperty("subresourcepolicy", subResourcePolicy);
                                                                                                                                subResource.setProperty("path", subResourcePolicy.getProperty("path"));
                                                                                                                                if (!subResourcePolicy.getProperty("type").equals("S")) break block55;
                                                                                                                                datasetname = subResourcePolicy.getProperty("datasetname");
                                                                                                                                keys = sdiData.getKeys(datasetname);
                                                                                                                                offset = Integer.parseInt(this.getSDCProcessor().getProperty(sdcResourceSdcid, "keycolumns"));
                                                                                                                                if (keys == null) break block56;
                                                                                                                                if (keys.length < 4 || !keys[0].equals("sdcid") || !keys[1].equals("keyid1") || !keys[2].equals("keyid2") || !keys[3].equals("keyid3")) break block57;
                                                                                                                                offset = 4;
                                                                                                                                break block57;
                                                                                                                            }
                                                                                                                            subResourceLinks = this.getSDCProcessor().getLinksData(sdcResourceSdcid);
                                                                                                                            for (k = 0; k < subResourceLinks.size(); ++k) {
                                                                                                                                if (!subResourceLinks.getValue(k, "linktype").equals("D") && !subResourceLinks.getValue(k, "linktype").equals("M") || !subResourceLinks.getValue(k, "linktableid").equals(datasetname)) continue;
                                                                                                                                linkProps = this.getSDCProcessor().getLinkProperties(sdcResourceSdcid, subResourceLinks.getValue(k, "linkid"));
                                                                                                                                keycols = Integer.parseInt((String)linkProps.get("keycolcount"));
                                                                                                                                keys = new String[keycols];
                                                                                                                                for (l = 0; l < keycols; ++l) {
                                                                                                                                    keys[l] = (String)linkProps.get("keycolid" + (l + 1));
                                                                                                                                }
                                                                                                                                break;
                                                                                                                            }
                                                                                                                        }
                                                                                                                        subResourcePolicy.put("keycols", keys);
                                                                                                                        subResourcePolicy.put("keycoloffset", offset);
                                                                                                                        continue;
                                                                                                                    }
                                                                                                                    if (!subResourcePolicy.getProperty("type").equals("R")) continue;
                                                                                                                    subResourceSdcid = subResourcePolicy.getProperty("sdcid");
                                                                                                                    subResourceProps = this.getSDCProcessor().getProperties(subResourceSdcid);
                                                                                                                    keys = new String[]{subResourceProps.getProperty("keycolid1"), subResourceProps.getProperty("keycolid2"), subResourceProps.getProperty("keycolid3")};
                                                                                                                    subResourcePolicy.put("keycols", keys);
                                                                                                                    subResourcePolicy.put("keycoloffset", 0);
                                                                                                                    subResourceLinks = this.getSDCProcessor().getLinksData(subResourceSdcid);
                                                                                                                    for (k = 0; k < subResourceLinks.size(); ++k) {
                                                                                                                        if (!subResourceLinks.getValue(k, "linktype").equals("F") || !subResourceLinks.getValue(k, "linksdcid").equals(sdcResource.getProperty("sdcid"))) continue;
                                                                                                                        subResourcePolicy.setProperty("linkkeycolid1", subResourceLinks.getValue(k, "sdccolumnid"));
                                                                                                                        subResourcePolicy.setProperty("linkkeycolid2", subResourceLinks.getValue(k, "sdccolumnid2"));
                                                                                                                        subResourcePolicy.setProperty("linkkeycolid3", subResourceLinks.getValue(k, "sdccolumnid3"));
                                                                                                                        continue block13;
                                                                                                                    }
                                                                                                                }
                                                                                                                subResources.index("path");
                                                                                                            }
                                                                                                            if (sdcResource.getCollection("resourcerootmethods") != null) {
                                                                                                                sdcResource.getCollection("resourcerootmethods").index(new String[]{"method", "methodqualifier"}, ";");
                                                                                                            }
                                                                                                            if (sdcResource.getCollection("resourceidmethods") != null) {
                                                                                                                sdcResource.getCollection("resourceidmethods").index(new String[]{"method", "methodqualifier"}, ";");
                                                                                                            }
                                                                                                            if (sdcResource.getCollection("columns") == null) continue;
                                                                                                            sdcResource.getCollection("columns").index("columnid");
                                                                                                        }
                                                                                                        SdcNameSpaceHandler.databaseResources.put(cacheKey, resources);
                                                                                                    }
                                                                                                    resourcepath = null;
                                                                                                    sdcid = null;
                                                                                                    resourcePolicy = null;
                                                                                                    subResources = null;
                                                                                                    resourceValues = new PropertyList();
                                                                                                    if (this.getNameSpaceSegmentCount() >= 1) {
                                                                                                        resourcepath = this.getNameSpaceSegment(0);
                                                                                                        resourcePolicy = resources.get(resourcepath);
                                                                                                        if (resourcePolicy != null) {
                                                                                                            sdcid = resourcePolicy.getProperty("sdcid");
                                                                                                            resourceValues.setProperty("sdcid", sdcid);
                                                                                                            subResources = resourcePolicy.getCollection("subresources");
                                                                                                            if (!this.isJUnit && !resourcePolicy.getProperty("enabled", "N").equals("Y")) {
                                                                                                                throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
                                                                                                            }
                                                                                                        } else {
                                                                                                            throw new RestException(400, "Malformed request body", "Unrecognized resource path '" + resourcepath + "'");
                                                                                                        }
                                                                                                    }
                                                                                                    if (!this.doGet()) break block58;
                                                                                                    if (this.getNameSpaceSegmentCount() == 0) {
                                                                                                        throw new RestException(400, "Malformed request URL", "Expecting request in the form /sdc/{sdcresource}/{resource}, e.g. /sdc/samples/S1");
                                                                                                    }
                                                                                                    if (this.getNameSpaceSegmentCount() != 1) break block59;
                                                                                                    getMethodProps = this.getPolicyMethod(resourcePolicy, true, "GET");
                                                                                                    queryid = this.request.getParameter("queryid");
                                                                                                    keyid1 = this.request.getParameter("keyid1");
                                                                                                    filter = this.request.getParameter("filter");
                                                                                                    template = this.request.getParameter("template");
                                                                                                    showqueries = this.request.getParameter("showqueries");
                                                                                                    definition = this.request.getParameter("definition");
                                                                                                    enforceDataMasking = "Y".equals(StringUtil.getYN(getMethodProps.getProperty("enforcedatamasking", "N"), "N"));
                                                                                                    includeTemplatesStr = this.request.getParameter("includetemplates");
                                                                                                    v0 = includeTemplatesBool = includeTemplatesStr != null && includeTemplatesStr.equals("true") != false;
                                                                                                    if (queryid == null || queryid.length() <= 0) break block60;
                                                                                                    validQueries = (HashSet)resourcePolicy.get("validqueries");
                                                                                                    if (validQueries == null) {
                                                                                                        this.getQueries(sdcid, resourcePolicy, includeTemplatesBool);
                                                                                                        validQueries = (HashSet)resourcePolicy.get("validqueries");
                                                                                                    }
                                                                                                    if (!this.isJUnit && (validQueries == null || !validQueries.contains(queryid))) break block61;
                                                                                                    sdiRequest = new SDIRequest();
                                                                                                    sdiRequest.setReturnMaskedData(enforceDataMasking);
                                                                                                    sdiRequest.setSDCid(sdcid);
                                                                                                    sdiRequest.setQueryid(queryid);
                                                                                                    if (includeTemplatesBool) {
                                                                                                        sdiRequest.setShowTemplates(true);
                                                                                                    }
                                                                                                    params = null;
                                                                                                    for (i = 1; i < 12 && (param = this.request.getParameter("param" + i)) != null && param.length() > 0; ++i) {
                                                                                                        if (params == null) {
                                                                                                            params = new ArrayList<String>();
                                                                                                        }
                                                                                                        params.add(param);
                                                                                                    }
                                                                                                    if (params != null && params.size() > 0) {
                                                                                                        sdiRequest.setQueryParams(params.toArray(new String[params.size()]));
                                                                                                    }
                                                                                                    this.addResourceData(sdiRequest, resourcePolicy);
                                                                                                    break block62;
                                                                                                }
                                                                                                throw new RestException(400, "Malformed request URL", "Unrecognized query - check policy settings");
                                                                                            }
                                                                                            if (keyid1 == null || keyid1.length() <= 0) break block63;
                                                                                            sdiRequest = new SDIRequest();
                                                                                            sdiRequest.setReturnMaskedData(enforceDataMasking);
                                                                                            sdiRequest.setSDCid(sdcid);
                                                                                            sdiRequest.setKeyid1List(keyid1);
                                                                                            if (this.request.getParameter("keyid2") != null) {
                                                                                                sdiRequest.setKeyid2List(this.request.getParameter("keyid2"));
                                                                                            }
                                                                                            if (this.request.getParameter("keyid3") != null) {
                                                                                                sdiRequest.setKeyid3List(this.request.getParameter("keyid3"));
                                                                                            }
                                                                                            this.addResourceData(sdiRequest, resourcePolicy);
                                                                                            break block62;
                                                                                        }
                                                                                        if (showqueries == null || !showqueries.equals("Y")) break block64;
                                                                                        resourceObj = new JSONObject();
                                                                                        this.setResponseValue(resourcepath, resourceObj);
                                                                                        resourceObj.put("queries", this.getQueries(sdcid, resourcePolicy, includeTemplatesBool));
                                                                                        break block62;
                                                                                    }
                                                                                    if (template == null || template.length() <= 0) break block65;
                                                                                    resourceObj = new JSONObject();
                                                                                    this.setResponseValue(resourcepath, resourceObj);
                                                                                    resourceObj.put("template", this.getTemplate(sdcid, template));
                                                                                    break block62;
                                                                                }
                                                                                if (definition == null || definition.length() <= 0) break block66;
                                                                                resourceObj = new JSONObject();
                                                                                this.setResponseValue(resourcepath, resourceObj);
                                                                                resourceObj.put("definition", this.getDefinition(sdcid, definition));
                                                                                break block62;
                                                                            }
                                                                            throw new RestException(400, "Malformed request URL", "Expecting request in the form /sdc/{sdcresource}?queryid={queryid} or /sdc/{sdcresource}?keyid1={keyid1list} e.g. /sdc/samples/queryid=TodaysSamples or /sdc/samples/keyid1=S1;S2");
                                                                        }
                                                                        if (this.getNameSpaceSegmentCount() != 2) break block67;
                                                                        this.addResourceKeyValues(resourceValues, this.getNameSpaceSegment(1), resourcePolicy);
                                                                        getMethodProps = this.getPolicyMethod(resourcePolicy, false, "GET");
                                                                        sdiRequest = new SDIRequest();
                                                                        sdiRequest.setReturnMaskedData("Y".equals(StringUtil.getYN(getMethodProps.getProperty("enforcedatamasking", "N"), "N")));
                                                                        if (resourceValues.getProperty("keyid1").length() <= 0) break block68;
                                                                        sdiRequest.setSDIList(sdcid, resourceValues.getProperty("keyid1"), resourceValues.getProperty("keyid2"), resourceValues.getProperty("keyid3"));
                                                                        this.addResourceData(sdiRequest, resourcePolicy);
                                                                        break block62;
                                                                    }
                                                                    throw new RestException(400, "Malformed request URL", "Expecting request in the form /sdc/{sdcresource}/{resource}, e.g. /sdc/samples/S1");
                                                                }
                                                                if (this.getNameSpaceSegmentCount() != 3) break block69;
                                                                this.addResourceKeyValues(resourceValues, this.getNameSpaceSegment(1), resourcePolicy);
                                                                this.getPolicyMethod(resourcePolicy, false, "GET");
                                                                subResourcePolicy = this.getSubResourcePolicy(subResources, this.getNameSpaceSegment(2));
                                                                this.getPolicyMethod(subResourcePolicy, true, "GET");
                                                                if (resourceValues.getProperty("keyid1").length() <= 0) break block70;
                                                                sdiRequest = new SDIRequest();
                                                                sdiRequest.setSDIList(sdcid, resourceValues.getProperty("keyid1"), resourceValues.getProperty("keyid2"), resourceValues.getProperty("keyid3"));
                                                                this.addResourceData(sdiRequest, false, resourcePolicy, subResourcePolicy, "");
                                                                break block62;
                                                            }
                                                            throw new RestException(400, "Malformed request URL", "Expecting request in the form /sdc/{sdcresource}/{resource}/{sdcsubresource}, e.g. /sdc/samples/S1/tests");
                                                        }
                                                        if (this.getNameSpaceSegmentCount() != 4) break block71;
                                                        this.addResourceKeyValues(resourceValues, this.getNameSpaceSegment(1), resourcePolicy);
                                                        this.getPolicyMethod(resourcePolicy, false, "GET");
                                                        subresourcekey = this.getNameSpaceSegment(3);
                                                        subResourcePolicy = this.getSubResourcePolicy(subResources, this.getNameSpaceSegment(2));
                                                        this.getPolicyMethod(subResourcePolicy, false, "GET");
                                                        if (resourceValues.getProperty("keyid1").length() <= 0) break block72;
                                                        sdiRequest = new SDIRequest();
                                                        sdiRequest.setSDIList(sdcid, resourceValues.getProperty("keyid1"), resourceValues.getProperty("keyid2"), resourceValues.getProperty("keyid3"));
                                                        this.addResourceData(sdiRequest, false, resourcePolicy, subResourcePolicy, subresourcekey);
                                                        break block62;
                                                    }
                                                    throw new RestException(400, "Malformed request URL", "Expecting request in the form /sdc/{sdcresource}/{resource}/{sdcsubresource}/{subresource}, e.g. /sdc/samples/S1/tests/MoistureAnlysis");
                                                }
                                                throw new RestException(400, "Malformed request URL", "Expecting request in the form /sdc/{sdcresource}/{resource}/{sdcsubresource}/{subresource}, e.g. /sdc/samples/S1/tests/MoistureAnalysis");
                                            }
                                            if (!this.doPost()) break block73;
                                            if (this.request.getParameter("queryid") != null || this.request.getParameter("keyid1") != null || this.request.getParameter("filter") != null) {
                                                throw new RestException(400, "Malformed request URL", "Request query parameters queryid, keyid1 and filter not supported with the POST method");
                                            }
                                            if (this.getNameSpaceSegmentCount() != 1) break block74;
                                            method = this.getPolicyMethod(resourcePolicy, true, "POST");
                                            template = this.request.getParameter("template");
                                            if (template != null && template.length() > 0) {
                                                resourceObj = new JSONObject();
                                                this.setResponseValue(resourcepath, resourceObj);
                                                resourceObj.put("template", this.getTemplate(sdcid, template));
                                            } else {
                                                postProps = this.executeMethodAction(method, resourceValues, resourcePolicy.getProperty("restrictivewhere"));
                                                keyid1 = postProps.getProperty("newkeyid1");
                                                if (keyid1.length() > 0) {
                                                    this.setResponseCode(201, sdcid + " " + keyid1 + " created");
                                                    this.setResponseHeader("Location", this.getNameSpaceRootURL() + "/" + resourcepath + "/" + keyid1);
                                                    try {
                                                        sdiRequest = new SDIRequest();
                                                        sdiRequest.setSDIList(sdcid, keyid1, postProps.getProperty("newkeyid2"), postProps.getProperty("newkeyid3"));
                                                        this.addResourceData(sdiRequest, resourcePolicy);
                                                    }
                                                    catch (RestException e) {
                                                        this.handlePostActionResourceNotFound(e);
                                                    }
                                                }
                                            }
                                            break block62;
                                        }
                                        if (this.getNameSpaceSegmentCount() == 2) {
                                            throw new RestException(405, "Invalid request method", "POST not supported against a resource");
                                        }
                                        if (this.getNameSpaceSegmentCount() != 3) break block75;
                                        this.addResourceKeyValues(resourceValues, this.getNameSpaceSegment(1), resourcePolicy);
                                        subResourcePolicy = this.getSubResourcePolicy(subResources, this.getNameSpaceSegment(2));
                                        this.addSubResourceKeyValues(resourceValues, null, subResourcePolicy);
                                        method = this.getPolicyMethod(subResourcePolicy, true, "POST");
                                        template = this.request.getParameter("template");
                                        if (template != null && template.length() > 0) {
                                            resourceObj = new JSONObject();
                                            this.setResponseValue(resourcepath, resourceObj);
                                            resourceObj.put("template", this.getTemplate(sdcid, template));
                                        } else {
                                            this.executeMethodAction(method, resourceValues, resourcePolicy.getProperty("restrictivewhere"));
                                            this.setResponseCode(201, sdcid + " " + subResourcePolicy.getProperty("datasetname") + " created");
                                            try {
                                                sdiRequest = new SDIRequest();
                                                sdiRequest.setSDIList(sdcid, resourceValues.getProperty("keyid1"), resourceValues.getProperty("keyid2"), resourceValues.getProperty("keyid3"));
                                                this.addResourceData(sdiRequest, false, resourcePolicy, subResourcePolicy, null);
                                            }
                                            catch (RestException e) {
                                                this.handlePostActionResourceNotFound(e);
                                            }
                                        }
                                        break block62;
                                    }
                                    if (this.getNameSpaceSegmentCount() == 4) {
                                        throw new RestException(405, "Invalid request method", "POST not supported against a subresource");
                                    }
                                    throw new RestException(400, "Malformed request URL", "Expecting request in the form /sdc/{sdcresource}, e.g. /sdc/samples");
                                }
                                if (!this.doPut()) break block76;
                                if (this.request.getParameter("queryid") != null || this.request.getParameter("keyid1") != null || this.request.getParameter("filter") != null) {
                                    throw new RestException(400, "Malformed request URL", "Request query parameters queryid, keyid1 and filter not supported with the PUT method");
                                }
                                if (this.getNameSpaceSegmentCount() != 1) break block77;
                                method = this.getPolicyMethod(resourcePolicy, true, "PUT");
                                this.addResourceKeyValues(resourceValues, null, resourcePolicy);
                                template = this.request.getParameter("template");
                                if (template != null && template.length() > 0) {
                                    resourceObj = new JSONObject();
                                    this.setResponseValue(resourcepath, resourceObj);
                                    resourceObj.put("template", this.getTemplate(sdcid, template));
                                } else {
                                    this.executeMethodAction(method, resourceValues, resourcePolicy.getProperty("restrictivewhere"));
                                    this.setResponseCode(200, sdcid + " " + resourceValues.getProperty("keyid1") + " updated");
                                    try {
                                        sdiRequest = new SDIRequest();
                                        sdiRequest.setSDIList(sdcid, resourceValues.getProperty("keyid1"), resourceValues.getProperty("keyid2"), resourceValues.getProperty("keyid3"));
                                        this.addResourceData(sdiRequest, resourcePolicy);
                                    }
                                    catch (RestException e) {
                                        this.handlePostActionResourceNotFound(e);
                                    }
                                }
                                break block62;
                            }
                            if (this.getNameSpaceSegmentCount() != 2) break block78;
                            method = this.getPolicyMethod(resourcePolicy, false, "PUT");
                            this.addResourceKeyValues(resourceValues, this.getNameSpaceSegment(1), resourcePolicy);
                            template = this.request.getParameter("template");
                            if (template != null && template.length() > 0) {
                                resourceObj = new JSONObject();
                                this.setResponseValue(resourcepath, resourceObj);
                                resourceObj.put("template", this.getTemplate(sdcid, template));
                            } else {
                                this.executeMethodAction(method, resourceValues, resourcePolicy.getProperty("restrictivewhere"));
                                this.setResponseCode(200, sdcid + " " + resourceValues.getProperty("keyid1") + " updated");
                                try {
                                    sdiRequest = new SDIRequest();
                                    sdiRequest.setSDIList(sdcid, resourceValues.getProperty("keyid1"), resourceValues.getProperty("keyid2"), resourceValues.getProperty("keyid3"));
                                    this.addResourceData(sdiRequest, resourcePolicy);
                                }
                                catch (RestException e) {
                                    this.handlePostActionResourceNotFound(e);
                                }
                            }
                            break block62;
                        }
                        if (this.getNameSpaceSegmentCount() != 3) break block79;
                        subResourcePolicy = this.getSubResourcePolicy(subResources, this.getNameSpaceSegment(2));
                        methodqualifier = this.request.getParameter("action");
                        method = this.getPolicyMethod(subResourcePolicy, true, "PUT", methodqualifier != null ? methodqualifier : "");
                        this.addResourceKeyValues(resourceValues, this.getNameSpaceSegment(1), resourcePolicy);
                        this.addSubResourceKeyValues(resourceValues, null, subResourcePolicy);
                        template = this.request.getParameter("template");
                        if (template != null && template.length() > 0) {
                            resourceObj = new JSONObject();
                            this.setResponseValue(resourcepath, resourceObj);
                            resourceObj.put("template", this.getTemplate(sdcid, template));
                        } else {
                            this.executeMethodAction(method, resourceValues, resourcePolicy.getProperty("restrictivewhere"));
                            this.setResponseCode(200, sdcid + " " + resourceValues.getProperty("keyid1") + " " + subResourcePolicy.getProperty("path") + " updated");
                            sdiRequest = new SDIRequest();
                            sdiRequest.setSDIList(sdcid, resourceValues.getProperty("keyid1"), resourceValues.getProperty("keyid2"), resourceValues.getProperty("keyid3"));
                            try {
                                this.addResourceData(sdiRequest, false, resourcePolicy, subResourcePolicy, null);
                            }
                            catch (RestException e) {
                                this.handlePostActionResourceNotFound(e);
                            }
                        }
                        break block62;
                    }
                    if (this.getNameSpaceSegmentCount() != 4) break block80;
                    subResourcePolicy = this.getSubResourcePolicy(subResources, this.getNameSpaceSegment(2));
                    methodqualifier = this.request.getParameter("action");
                    method = this.getPolicyMethod(subResourcePolicy, false, "PUT", methodqualifier != null ? methodqualifier : "");
                    this.addResourceKeyValues(resourceValues, this.getNameSpaceSegment(1), resourcePolicy);
                    this.addSubResourceKeyValues(resourceValues, this.getNameSpaceSegment(3), subResourcePolicy);
                    template = this.request.getParameter("template");
                    if (template != null && template.length() > 0) {
                        resourceObj = new JSONObject();
                        this.setResponseValue(resourcepath, resourceObj);
                        resourceObj.put("template", this.getTemplate(sdcid, template));
                    } else {
                        this.executeMethodAction(method, resourceValues, resourcePolicy.getProperty("restrictivewhere"));
                        this.setResponseCode(200, sdcid + " " + resourceValues.getProperty("keyid1") + " " + subResourcePolicy.getProperty("path") + " updated");
                        try {
                            sdiRequest = new SDIRequest();
                            sdiRequest.setSDIList(sdcid, resourceValues.getProperty("keyid1"), resourceValues.getProperty("keyid2"), resourceValues.getProperty("keyid3"));
                            this.addResourceData(sdiRequest, false, resourcePolicy, subResourcePolicy, this.getNameSpaceSegment(3));
                        }
                        catch (RestException e) {
                            this.handlePostActionResourceNotFound(e);
                        }
                    }
                    break block62;
                }
                throw new RestException(400, "Malformed request URL", "Expecting request in the form /sdc/{sdcresource}/{resource} or /sdc/{sdcresource}/{resource}/{sdcsubresource}/{subresource}, e.g. /sdc/samples/S1 or /sdc/samples/S1/tests/MoistureAnalysis");
            }
            if (!this.doDelete()) ** GOTO lbl394
            if (this.request.getParameter("queryid") != null || this.request.getParameter("keyid1") != null || this.request.getParameter("filter") != null) {
                throw new RestException(400, "Malformed request URL", "Request query parameters queryid, keyid1 and filter not supported with the DELETE method");
            }
            if (this.getNameSpaceSegmentCount() == 2) {
                method = this.getPolicyMethod(resourcePolicy, false, "DELETE");
                this.addResourceKeyValues(resourceValues, this.getNameSpaceSegment(1), resourcePolicy);
                this.executeMethodAction(method, resourceValues, resourcePolicy.getProperty("restrictivewhere"));
                this.setResponseCode(200, sdcid + " " + resourceValues.getProperty("keyid1") + " deleted");
            } else if (this.getNameSpaceSegmentCount() == 4) {
                subResourcePolicy = this.getSubResourcePolicy(subResources, this.getNameSpaceSegment(2));
                method = this.getPolicyMethod(subResourcePolicy, false, "DELETE");
                this.addResourceKeyValues(resourceValues, this.getNameSpaceSegment(1), resourcePolicy);
                this.addSubResourceKeyValues(resourceValues, this.getNameSpaceSegment(3), subResourcePolicy);
                this.executeMethodAction(method, resourceValues, resourcePolicy.getProperty("restrictivewhere"));
                this.setResponseCode(200, sdcid + " " + resourceValues.getProperty("keyid1") + " " + subResourcePolicy.getProperty("datasetname") + " deleted");
            } else {
                throw new RestException(400, "Malformed request URL", "Expecting request in the form /sdc/{sdcresource}/{resource}, e.g. /sdc/samples/S1");
lbl394:
                // 1 sources

                throw new RestException(405, "Invalid request method", "/sdc resource only accepts GET, POST, PUT and DELETE methods.");
            }
        }
    }

    private void addResourceKeyValues(PropertyList resourceValues, String resourcekey, PropertyList resourcePolicy) {
        if (resourcekey != null && resourcekey.length() > 0) {
            String[] keyparts = StringUtil.split(resourcekey, ";");
            for (int i = 0; i < keyparts.length; ++i) {
                if (keyparts[i].length() <= 0) continue;
                resourceValues.put("keyid" + (i + 1), keyparts[i]);
            }
        }
        String path = resourcePolicy.getProperty("path");
        JSONArray items = null;
        try {
            items = this.jsonRequest.getJSONArray(path);
            if (items.length() > 0) {
                HashMap<String, String> values = new HashMap<String, String>();
                JSONObject item0 = items.getJSONObject(0);
                Iterator iterator = item0.keys();
                while (iterator.hasNext()) {
                    String key = (String)iterator.next();
                    values.put(key, String.valueOf(item0.opt(key)));
                }
                for (int i = 1; i < items.length(); ++i) {
                    JSONObject item = items.getJSONObject(i);
                    Iterator iterator2 = item0.keys();
                    while (iterator2.hasNext()) {
                        String key = (String)iterator2.next();
                        values.put(key, (String)values.get(key) + ";" + item.optString(key));
                    }
                }
                for (String key : values.keySet()) {
                    resourceValues.setProperty(key, (String)values.get(key));
                    if (key.equals(resourcePolicy.getProperty("keycolid1"))) {
                        resourceValues.put("keyid1", values.get(key));
                        continue;
                    }
                    if (key.equals(resourcePolicy.getProperty("keycolid2"))) {
                        resourceValues.put("keyid2", values.get(key));
                        continue;
                    }
                    if (!key.equals(resourcePolicy.getProperty("keycolid3"))) continue;
                    resourceValues.put("keyid3", values.get(key));
                }
            }
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
    }

    private void addSubResourceKeyValues(PropertyList resourceValues, String subresourcekey, PropertyList subResourcePolicy) {
        String[] keycols = (String[])subResourcePolicy.get("keycols");
        int offset = (Integer)subResourcePolicy.get("keycoloffset");
        if (keycols != null) {
            if (subresourcekey != null && subresourcekey.length() > 0) {
                String[] keyvals = StringUtil.split(HttpUtil.decodeURIComponent(subresourcekey), ";");
                for (int i = 0; i < keyvals.length; ++i) {
                    resourceValues.setProperty(keycols[offset + i], keyvals[i]);
                }
            }
            String path = subResourcePolicy.getProperty("path");
            JSONArray items = null;
            try {
                items = this.jsonRequest.getJSONArray(path);
                if (items.length() > 0) {
                    HashMap<String, String> values = new HashMap<String, String>();
                    JSONObject item0 = items.getJSONObject(0);
                    Iterator iterator = item0.keys();
                    while (iterator.hasNext()) {
                        String key = (String)iterator.next();
                        values.put(key, String.valueOf(item0.opt(key)));
                    }
                    for (int i = 1; i < items.length(); ++i) {
                        JSONObject item = items.getJSONObject(i);
                        Iterator iterator2 = item0.keys();
                        while (iterator2.hasNext()) {
                            String key = (String)iterator2.next();
                            values.put(key, (String)values.get(key) + ";" + item.optString(key));
                        }
                    }
                    for (String key : values.keySet()) {
                        resourceValues.setProperty(key, (String)values.get(key));
                    }
                }
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
        }
    }

    private void handlePostActionResourceNotFound(RestException e) throws RestException {
        if (!e.getError().equals("Resource not found")) {
            throw e;
        }
    }

    private PropertyList getPolicyMethod(PropertyList policy, boolean root, String method) throws RestException {
        return this.getPolicyMethod(policy, root, method, "");
    }

    private PropertyList getPolicyMethod(PropertyList policy, boolean root, String method, String methodqualifier) throws RestException {
        PropertyListCollection policyMethods = policy.getCollection(root ? "resourcerootmethods" : "resourceidmethods");
        if (policyMethods != null) {
            PropertyList policyMethod = policyMethods.getIndexedPropertyList(method + ";" + methodqualifier);
            if (policyMethod != null && (this.isJUnit || policyMethod.getProperty("enabled", "N").equals("Y"))) {
                return policyMethod;
            }
            throw new RestException(400, "Resource not available", "Resource disabled or undefined in REST policy");
        }
        throw new RestException(400, "Resource not available", "Resource disabled or undefined in REST policy");
    }

    private PropertyList getSubResourcePolicy(PropertyListCollection subResources, String subresource) throws RestException {
        PropertyList subResource = subResources.getIndexedPropertyList(subresource);
        if (subResource != null) {
            PropertyList subResourcePolicy = subResource.getPropertyList("subresourcepolicy");
            if (subResourcePolicy != null) {
                return subResourcePolicy;
            }
            throw new RestException(400, "Resource not available", "Resource disabled or undefined in REST policy");
        }
        throw new RestException(400, "Resource not available", "Resource disabled or undefined in REST policy");
    }

    private PropertyList executeMethodAction(PropertyList method, PropertyList resourceValues, String restrictivewhere) throws Exception {
        String actionid;
        if (!method.getProperty("method").equals("POST") && resourceValues.containsKey("keyid1")) {
            if (StringUtil.split(resourceValues.getProperty("keyid1"), ";").length > 90) {
                throw new RestException(400, "Malformed request body", "Max 90 resources exceeded in request");
            }
            if (!ServiceLocator.getInstance().getDataAccessManager().checkRESTAccess(this.sapphireConnection.getConnectionId(), resourceValues.getProperty("sdcid"), resourceValues.getProperty("keyid1"), resourceValues.getProperty("keyid2"), resourceValues.getProperty("keyid3"), restrictivewhere, method.getProperty("operationid"))) {
                throw new RestException(404, "Resource not available", "One or all of the resources not available due to locking or restricted access");
            }
        }
        if ((actionid = method.getProperty("actionid")).length() > 0) {
            String restrictedproperties;
            PropertyList actionProps = new PropertyList();
            PropertyListCollection actionProperties = method.getCollection("actionproperties");
            HashSet<String> evaluatedProperties = new HashSet<String>();
            for (int i = 0; i < actionProperties.size(); ++i) {
                boolean mandatory;
                PropertyList actionProperty = actionProperties.getPropertyList(i);
                String propertyid = actionProperty.getProperty("propertyid");
                actionProps.setProperty(propertyid, this.evalProperty(propertyid, actionProperty.getProperty("propertyvalue"), resourceValues, actionProperty.getProperty("ignoretokens", "N").equals("Y")));
                evaluatedProperties.add(propertyid);
                boolean bl = mandatory = this.isJUnit && this.request.getParameter("__mandatoryproperties") != null ? this.request.getParameter("__mandatoryproperties").equals("Y") : actionProperty.getProperty("mandatory", "N").equals("Y");
                if (!mandatory || actionProps.getProperty(propertyid).length() != 0) continue;
                throw new RestException(400, "Malformed request body", "Missing mandatory property '" + propertyid + "' in request body");
            }
            String string = restrictedproperties = this.isJUnit && this.request.getParameter("__restrictedproperties") != null ? this.request.getParameter("__restrictedproperties") : method.getProperty("restrictedproperties", "I");
            if (!restrictedproperties.equals("I")) {
                Iterator iterator = this.jsonRequest.keys();
                while (iterator.hasNext()) {
                    String name = (String)iterator.next();
                    Object value = this.jsonRequest.get(name);
                    if (value instanceof JSONArray) {
                        if (((JSONArray)value).length() <= 0) continue;
                        JSONObject value0 = ((JSONArray)value).getJSONObject(0);
                        Iterator iterator1 = value0.keys();
                        while (iterator1.hasNext()) {
                            String name1 = (String)iterator1.next();
                            if (evaluatedProperties.contains(name1)) continue;
                            if (restrictedproperties.equals("N")) {
                                actionProps.setProperty(name1, resourceValues.getProperty(name1));
                                continue;
                            }
                            throw new RestException(400, "Malformed request body", "Data request violates restricted properties policy");
                        }
                        continue;
                    }
                    if (evaluatedProperties.contains(name)) continue;
                    if (restrictedproperties.equals("N")) {
                        actionProps.setProperty(name, this.evalProperty(name, this.jsonRequest.getString(name), resourceValues, false));
                        continue;
                    }
                    throw new RestException(400, "Malformed request body", "Data request violates restricted properties policy");
                }
            }
            this.getActionProcessor().processAction(actionid, "1", actionProps);
            this.setResponseValue("output", this.getActionOutput(actionid, actionProps));
            return actionProps;
        }
        throw new RestException(400, "Resource not available", "Resource method action missing in REST policy");
    }

    private String evalProperty(String propertyid, String propertyvalue, PropertyList tokenValues, boolean ignoreTokens) {
        if (propertyvalue.length() == 0) {
            propertyvalue = tokenValues.getProperty(propertyid, this.jsonRequest.optString(propertyid));
        }
        if (!ignoreTokens && propertyvalue.contains("[")) {
            String[] tokens = StringUtil.getTokens(propertyvalue);
            for (int i = 0; i < tokens.length; ++i) {
                propertyvalue = tokens[i].equalsIgnoreCase("currentuser") ? StringUtil.replaceAll(propertyvalue, "[" + tokens[i] + "]", this.sapphireConnection.getSysuserId()) : (tokenValues.containsKey(tokens[i]) ? StringUtil.replaceAll(propertyvalue, "[" + tokens[i] + "]", tokenValues.getProperty(tokens[i].toLowerCase())) : StringUtil.replaceAll(propertyvalue, "[" + tokens[i] + "]", this.jsonRequest.optString(tokens[i].toLowerCase())));
            }
            return propertyvalue;
        }
        return propertyvalue;
    }

    private void addResourceData(SDIRequest sdiRequest, PropertyList resourcePolicy) throws RestException {
        this.addResourceData(sdiRequest, true, resourcePolicy, null, null);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void addResourceData(SDIRequest sdiRequest, boolean primaryresource, PropertyList resourcePolicy, PropertyList subResourcePolicy, String subresourcekey) throws RestException {
        String expand;
        String sdcid = sdiRequest.getSDCid();
        sdiRequest.setRequestItem("primary");
        String restrictivewhere = resourcePolicy.getProperty("restrictivewhere");
        if (sdiRequest.getQueryWhere().length() > 0) {
            sdiRequest.setQueryWhere((restrictivewhere.length() > 0 ? resourcePolicy.getProperty("restrictivewhere") + " AND " : "") + "(" + sdiRequest.getQueryWhere() + ")");
        } else if (restrictivewhere.length() > 0) {
            sdiRequest.setQueryWhere(resourcePolicy.getProperty("restrictivewhere"));
        }
        HashMap<String, PropertyList> datasetPolicies = new HashMap<String, PropertyList>();
        HashMap<String, PropertyList> datanamePolicies = new HashMap<String, PropertyList>();
        String string = expand = this.request.getParameter("expand") != null ? this.request.getParameter("expand") : "";
        if (expand.length() > 0) {
            if (primaryresource) {
                if (expand.equalsIgnoreCase("all")) {
                    PropertyListCollection subresources = resourcePolicy.getCollection("subresources");
                    for (int i = 0; i < subresources.size(); ++i) {
                        PropertyList subresource = subresources.getPropertyList(i);
                        PropertyList subresourcepolicy = subresource.getPropertyList("subresourcepolicy");
                        if (subresourcepolicy == null) continue;
                        if (subresourcepolicy.getProperty("type").equals("S")) {
                            datasetPolicies.put(subresourcepolicy.getProperty("datasetname"), subresourcepolicy);
                            sdiRequest.setRequestItem(subresourcepolicy.getProperty("datasetname"));
                            continue;
                        }
                        SDIRequest rfkRequest = new SDIRequest();
                        rfkRequest.setRequestid(subresourcepolicy.getProperty("sdcid"));
                        rfkRequest.setSDCid(subresourcepolicy.getProperty("sdcid"));
                        rfkRequest.setRequestItem("primary");
                        sdiRequest.setSDIRequest(rfkRequest);
                        subresourcepolicy.setProperty("datasetname", "primary");
                        datanamePolicies.put(subresourcepolicy.getProperty("sdcid"), subresourcepolicy);
                    }
                } else {
                    PropertyList subresource = resourcePolicy.getCollection("subresources").getIndexedPropertyList(expand);
                    if (subresource == null) throw new RestException(400, "Malformed request URL", "Unrecognized 'expand' option '" + expand + "' for sdcid '" + sdcid + "'");
                    PropertyList subresourcepolicy = subresource.getPropertyList("subresourcepolicy");
                    if (subresourcepolicy == null) throw new RestException(400, "Malformed request URL", "Unrecognized 'expand' option '" + expand + "' for sdcid '" + sdcid + "'");
                    if (subresourcepolicy.getProperty("type").equals("S")) {
                        datasetPolicies.put(subresourcepolicy.getProperty("datasetname"), subresourcepolicy);
                        sdiRequest.setRequestItem(subresourcepolicy.getProperty("datasetname"));
                    } else {
                        SDIRequest rfkRequest = null;
                        rfkRequest = new SDIRequest();
                        rfkRequest.setRequestid(subresourcepolicy.getProperty("sdcid"));
                        rfkRequest.setSDCid(subresourcepolicy.getProperty("sdcid"));
                        rfkRequest.setRequestItem("primary");
                        sdiRequest.setSDIRequest(rfkRequest);
                        subresourcepolicy.setProperty("datasetname", "primary");
                        datanamePolicies.put(subresourcepolicy.getProperty("sdcid"), subresourcepolicy);
                    }
                }
            }
        } else if (!primaryresource && subResourcePolicy != null) {
            if (subResourcePolicy.getProperty("type").equals("S")) {
                sdiRequest.setRequestItem(subResourcePolicy.getProperty("datasetname"));
            } else {
                SDIRequest rfkRequest = null;
                rfkRequest = new SDIRequest();
                rfkRequest.setRequestid(subResourcePolicy.getProperty("sdcid"));
                rfkRequest.setSDCid(subResourcePolicy.getProperty("sdcid"));
                rfkRequest.setRequestItem("primary");
                sdiRequest.setSDIRequest(rfkRequest);
                subResourcePolicy.setProperty("datasetname", "primary");
                datanamePolicies.put(subResourcePolicy.getProperty("sdcid"), subResourcePolicy);
            }
        }
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        SDIProcessor sdiProcessor = this.getSDIProcessor();
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        if (sdiData == null) throw new RestException(500, "Unexpected server error", "Failed to load SDIData");
        DataSet primary = sdiData.getDataset("primary");
        if (primary == null) throw new RestException(500, "Unexpected server error", "Failed to load primary data");
        if (primary.size() <= 0) throw new RestException(404, "Resource not found", "No data found");
        try {
            PropertyList sdc = sdcProcessor.getPropertyList(sdcid);
            String keycolid1 = sdc.getProperty("keycolid1");
            String keycolid2 = sdc.getProperty("keycolid2");
            String keycolid3 = sdc.getProperty("keycolid3");
            String[] primaryfields = this.getFields(resourcePolicy, !primaryresource);
            JSONArray primaryrows = new JSONArray();
            for (int i = 0; i < primary.size(); ++i) {
                JSONObject primaryrow;
                JSONObject parentrow = primaryrow = this.getJSONRow(primary, i, primaryfields, true, true);
                if (primaryresource && expand.length() > 0) {
                    Set datasetnames = sdiData.getDatasets();
                    for (String datasetname : datasetnames) {
                        if (datasetname.equals("primary")) continue;
                        this.addSubResourceData(sdiRequest, sdiData, primary, sdcid, keycolid1, keycolid2, keycolid3, i, parentrow, (PropertyList)datasetPolicies.get(datasetname), "");
                    }
                    Set datanames = sdiData.getSDIData();
                    for (String dataname : datanames) {
                        this.addSubResourceData(sdiRequest, sdiData, primary, sdcid, keycolid1, keycolid2, keycolid3, i, parentrow, (PropertyList)datanamePolicies.get(dataname), "");
                    }
                } else if (!primaryresource && subResourcePolicy != null) {
                    this.addSubResourceData(sdiRequest, sdiData, primary, sdcid, keycolid1, keycolid2, keycolid3, i, parentrow, subResourcePolicy, subresourcekey);
                }
                primaryrows.put(primaryrow);
            }
            this.setResponseValue(resourcePolicy.getProperty("path"), primaryrows);
            return;
        }
        catch (JSONException e) {
            throw new RestException(500, "Unexpected server error", e.getMessage(), e);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void addSubResourceData(SDIRequest sdiRequest, SDIData sdiData, DataSet primary, String sdcid, String keycolid1, String keycolid2, String keycolid3, int primaryrow, JSONObject parentrow, PropertyList subResourcePolicy, String subresourcekey) throws JSONException, RestException {
        DataSet filteredDataSet;
        BigDecimal bd;
        HashMap<String, Object> filterMap = new HashMap<String, Object>();
        DataSet dataset = null;
        if (subResourcePolicy.getProperty("type").equals("S")) {
            String datasetname = subResourcePolicy.getProperty("datasetname");
            dataset = sdiData.getDataset(datasetname);
            if (dataset == null) throw new RestException(500, "Unexpected server error", "Failed to find dataset for datasetname '" + datasetname + "' in subresource " + subResourcePolicy.getProperty("resourceid"));
            String[] keycols = sdiData.getKeys(datasetname);
            int nextkeycol = 0;
            for (int i = 0; i < keycols.length; ++i) {
                if (keycols[i].equals("sdcid")) {
                    filterMap.put("sdcid", sdcid);
                    nextkeycol = 1;
                    continue;
                }
                if (keycols[i].equals("keyid1")) {
                    filterMap.put("keyid1", primary.getValue(primaryrow, keycolid1));
                    nextkeycol = 2;
                    continue;
                }
                if (keycols[i].equals("keyid2")) {
                    filterMap.put("keyid2", keycolid2.length() > 0 ? primary.getValue(primaryrow, keycolid2) : "(null)");
                    nextkeycol = 3;
                    continue;
                }
                if (keycols[i].equals("keyid3")) {
                    filterMap.put("keyid3", keycolid3.length() > 0 ? primary.getValue(primaryrow, keycolid3) : "(null)");
                    nextkeycol = 4;
                    continue;
                }
                if (keycols[i].equals(keycolid1)) {
                    filterMap.put(keycolid1, primary.getValue(primaryrow, keycolid1));
                    nextkeycol = 1;
                    continue;
                }
                if (keycols[i].equals(keycolid2)) {
                    filterMap.put(keycolid2, primary.getValue(primaryrow, keycolid2));
                    nextkeycol = 2;
                    continue;
                }
                if (!keycols[i].equals(keycolid3)) continue;
                filterMap.put(keycolid3, primary.getValue(primaryrow, keycolid3));
                nextkeycol = 3;
            }
            if (subresourcekey != null && subresourcekey.length() > 0) {
                String[] keyvals = StringUtil.split(HttpUtil.decodeURIComponent(subresourcekey), ";");
                for (int i = 0; i < keyvals.length; ++i) {
                    if (dataset.getColumnType(keycols[nextkeycol + i]) == 0) {
                        filterMap.put(keycols[nextkeycol + i], keyvals[i]);
                        continue;
                    }
                    try {
                        bd = new BigDecimal(keyvals[i]);
                        filterMap.put(keycols[nextkeycol + i], bd);
                        continue;
                    }
                    catch (Exception e) {
                        filterMap.put(keycols[nextkeycol + i], null);
                    }
                }
            }
            String filter = this.request.getParameter("filter");
            String filterseparator = this.request.getParameter("filterseparator");
            if (filterseparator == null || filterseparator.length() == 0) {
                filterseparator = ",";
            }
            if (filter != null && filter.length() > 2) {
                if (!filter.startsWith("(") || !filter.endsWith(")")) {
                    throw new RestException(400, "Malformed request URL", "Filter query parameter must start with a ( and end with a ), e.g. filter=(paramtype=Average)");
                }
                String[] filters = StringUtil.split(filter.substring(1, filter.length() - 1).trim(), filterseparator, true);
                for (int i = 0; i < filters.length; ++i) {
                    if (!filters[i].contains("=")) {
                        throw new RestException(400, "Malformed request URL", "Filter query parameter must be in the format filter=([filtercolumn1]=[filtervalue1],[filtercolumn2]=[filtervalue2],...), e.g. filter=(paramtype=Average,s_analystid=admin)");
                    }
                    filterMap.put(filters[i].substring(0, filters[i].indexOf("=")).trim(), filters[i].substring(filters[i].indexOf("=") + 1).trim());
                }
            }
        } else {
            String subResourceSdcid = subResourcePolicy.getProperty("sdcid");
            SDIData subResourceSDIData = sdiData.getSDIData(subResourceSdcid);
            dataset = subResourceSDIData.getDataset(subResourcePolicy.getProperty("datasetname"));
            if (dataset == null) throw new RestException(500, "Unexpected server error", "Failed to find dataset for datasetname '" + subResourcePolicy.getProperty("datasetname") + "' in primary subresource " + subResourcePolicy.getProperty("resourceid"));
            filterMap.put(subResourcePolicy.getProperty("linkkeycolid1"), primary.getValue(primaryrow, keycolid1));
            if (subResourcePolicy.getProperty("linkkeycolid2").length() > 0) {
                filterMap.put(subResourcePolicy.getProperty("linkkeycolid2"), primary.getValue(primaryrow, keycolid2));
            }
            if (subResourcePolicy.getProperty("linkkeycolid3").length() > 0) {
                filterMap.put(subResourcePolicy.getProperty("linkkeycolid3"), primary.getValue(primaryrow, keycolid3));
            }
            if (subresourcekey != null && subresourcekey.length() > 0) {
                String[] keycols = subResourceSDIData.getKeys(subResourcePolicy.getProperty("datasetname"));
                String[] keyvals = StringUtil.split(HttpUtil.decodeURIComponent(subresourcekey), ";");
                for (int i = 0; i < keyvals.length; ++i) {
                    if (dataset.getColumnType(keycols[i]) == 0) {
                        filterMap.put(keycols[i], keyvals[i]);
                        continue;
                    }
                    try {
                        bd = new BigDecimal(keyvals[i]);
                        filterMap.put(keycols[i], bd);
                        continue;
                    }
                    catch (Exception e) {
                        filterMap.put(keycols[i], null);
                    }
                }
            }
        }
        if ((filteredDataSet = dataset.getFilteredDataSet(filterMap)) == null) throw new RestException(500, "Unexpected server error", "Failed to filter data for subresource " + subResourcePolicy.getProperty("resourceid"));
        if (filteredDataSet.size() <= 0) return;
        String[] datasetfields = this.getFields(subResourcePolicy, false);
        JSONArray datasetrows = new JSONArray();
        for (int l = 0; l < filteredDataSet.size(); ++l) {
            JSONObject datasetrow = this.getJSONRow(filteredDataSet, l, datasetfields, true, true);
            datasetrows.put(datasetrow);
        }
        parentrow.put(subResourcePolicy.getProperty("path"), datasetrows);
    }

    private JSONObject getJSONRow(DataSet data, int datarow, String[] fields, boolean excludeAuditColumns, boolean excludeSecurityColumns) throws JSONException {
        String[] columns = data.getColumns();
        JSONObject row = new JSONObject();
        if (fields != null) {
            for (int i = 0; i < fields.length; ++i) {
                String columnid = fields[i].trim();
                if (!data.isValidColumn(columnid)) continue;
                if (data.getColumnType(columnid) == 0 || data.getColumnType(columnid) == 3) {
                    row.put(columnid, data.getValue(datarow, columnid));
                    continue;
                }
                if (data.getColumnType(columnid) == 2) {
                    row.put(columnid, data.getValue(datarow, columnid));
                    continue;
                }
                String tempStr = "";
                try {
                    tempStr = data.getValue(datarow, columnid);
                    Integer tempInt = data.getInt(datarow, columnid);
                    Double tempDub = data.getDouble(datarow, columnid);
                    if (tempStr.equals(tempInt.toString())) {
                        row.put(columnid, tempInt);
                        continue;
                    }
                    if (!tempStr.equals(tempDub.toString())) continue;
                    row.put(columnid, tempDub);
                    continue;
                }
                catch (Exception e) {
                    throw new JSONException("Unable to parse DataSet.NUMBER value \"" + tempStr + "\" while forming JSON string");
                }
            }
        } else {
            for (int i = 0; i < columns.length; ++i) {
                if (columns[i].startsWith("_") || excludeAuditColumns && (!excludeAuditColumns || Column.isAuditColumn(columns[i])) || excludeSecurityColumns && (!excludeSecurityColumns || Column.isSecurityColumn(columns[i]))) continue;
                if (data.getColumnType(columns[i]) == 0 || data.getColumnType(columns[i]) == 3) {
                    row.put(columns[i], data.getValue(datarow, columns[i]));
                    continue;
                }
                row.put(columns[i], data.getInt(datarow, columns[i]));
            }
        }
        return row;
    }

    private String[] getFields(PropertyList resourcePolicy, boolean summaryColumns) throws RestException {
        String[] fields = null;
        String restrictedcolumns = this.isJUnit && this.request.getParameter("__restrictedcolumns") != null ? this.request.getParameter("__restrictedcolumns") : resourcePolicy.getProperty("restrictedcolumns", "I");
        PropertyListCollection columns = resourcePolicy.getCollection("columns");
        String fieldrequest = this.request.getParameter("fields");
        if (!summaryColumns && fieldrequest != null && fieldrequest.length() > 2) {
            if (!fieldrequest.startsWith("(") || !fieldrequest.endsWith(")")) {
                throw new RestException(400, "Malformed request URL", "Fields query parameter must start with a ( and end with a ), e.g. fields=(s_sampleid,sampledesc)");
            }
            fields = StringUtil.split(fieldrequest.substring(1, fieldrequest.length() - 1).trim(), ",", true);
            if (!restrictedcolumns.equals("N")) {
                for (int i = 0; i < fields.length; ++i) {
                    PropertyList column = columns.getIndexedPropertyList(fields[i]);
                    if (column != null && !column.getProperty("enabled", "Y").equals("N")) continue;
                    if (restrictedcolumns.equals("I")) {
                        fields[i] = "";
                        continue;
                    }
                    throw new RestException(400, "Malformed request URL", "Fields request violates restricted columns policy");
                }
            }
        } else {
            ArrayList<String> columnlist = new ArrayList<String>();
            for (int i = 0; i < columns.size(); ++i) {
                PropertyList column = columns.getPropertyList(i);
                if (!column.getProperty("enabled", "Y").equals("Y") || summaryColumns && (!summaryColumns || !column.getProperty("summarycolumn").equals("Y"))) continue;
                columnlist.add(column.getProperty("columnid"));
            }
            fields = columnlist.toArray(new String[columnlist.size()]);
        }
        return fields;
    }

    private String[] getFields(DataSet dataset, String[] keyCols) {
        ArrayList<String> requestFields = new ArrayList<String>();
        Iterator iterator = this.jsonRequest.keys();
        while (iterator.hasNext()) {
            String name = (String)iterator.next();
            if (!dataset.isValidColumn(name)) continue;
            requestFields.add(name);
        }
        for (int i = 0; i < keyCols.length; ++i) {
            if (requestFields.contains(keyCols[i])) continue;
            requestFields.add(keyCols[i]);
        }
        return requestFields.toArray(new String[requestFields.size()]);
    }

    private JSONObject getDefinition(String sdcid, String definition) throws JSONException {
        JSONObject definitionObj = new JSONObject();
        PropertyList sdc = this.getSDCProcessor().getPropertyList(sdcid);
        PropertyListCollection columns = sdc.getCollection("columns");
        columns.index("columnid");
        if (definition.equalsIgnoreCase("full") || definition.equalsIgnoreCase("policy")) {
            // empty if block
        }
        return definitionObj;
    }

    private JSONObject getTemplate(String sdcid, String template) throws JSONException {
        JSONObject templateObj = new JSONObject();
        if (template.equalsIgnoreCase("full") || template.equalsIgnoreCase("policy")) {
            // empty if block
        }
        return templateObj;
    }

    private JSONArray getQueries(String sdcid, PropertyList resourcePolicy, boolean includeTemplatesBool) throws JSONException {
        String show;
        JSONArray queries = new JSONArray();
        PropertyList queryoptions = resourcePolicy.getPropertyList("rootresourcequeries");
        if (queryoptions != null && ((show = queryoptions.getProperty("showqueries", "N")).equals("C") && queryoptions.getProperty("categoryid").length() > 0 || show.equals("A") || show.equals("L"))) {
            SDIData sdiData;
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("Query");
            if (show.equals("A")) {
                sdiRequest.setQueryFrom("query");
                sdiRequest.setQueryWhere("basedonid = '" + SafeSQL.encodeForSQL(sdcid, this.sapphireConnection.isOracle()) + "'");
            } else if (show.equals("C")) {
                sdiRequest.setQueryFrom("query, categoryitem");
                sdiRequest.setQueryWhere("basedonid = '" + SafeSQL.encodeForSQL(sdcid, this.sapphireConnection.isOracle()) + "' AND query.queryid = categoryitem.keyid1 AND categoryitem.sdcid = 'Query' AND categoryid = '" + SafeSQL.encodeForSQL(queryoptions.getProperty("categoryid"), this.sapphireConnection.isOracle()) + "'");
            } else {
                PropertyListCollection querylist = queryoptions.getCollection("queries");
                if (querylist != null && querylist.size() > 0) {
                    StringBuffer queryreq = new StringBuffer();
                    StringBuffer sdcreq = new StringBuffer();
                    for (int i = 0; i < querylist.size(); ++i) {
                        queryreq.append(";").append(querylist.getPropertyList(i).getProperty("queryid"));
                        sdcreq.append(";").append(sdcid);
                    }
                    sdiRequest.setKeyid1List(queryreq.substring(1));
                    sdiRequest.setKeyid2List(sdcreq.substring(1));
                }
            }
            sdiRequest.setQueryOrderBy("queryid");
            sdiRequest.setRequestItem("primary");
            sdiRequest.setRequestItem("queryarg");
            if (includeTemplatesBool) {
                sdiRequest.setShowTemplates(true);
            }
            if ((sdiData = this.getSDIProcessor().getSDIData(sdiRequest)) != null && sdiData.getDataset("primary") != null) {
                DataSet primary = sdiData.getDataset("primary");
                DataSet args = sdiData.getDataset("queryarg");
                HashSet<String> validQueries = (HashSet<String>)resourcePolicy.get("validqueries");
                if (validQueries == null) {
                    validQueries = new HashSet<String>();
                    resourcePolicy.put("validqueries", validQueries);
                }
                HashMap<String, String> filterMap = new HashMap<String, String>();
                for (int i = 0; i < primary.size(); ++i) {
                    JSONObject primaryrow = this.getJSONRow(primary, i, new String[]{"queryid", "querydesc"}, true, true);
                    validQueries.add(primary.getValue(i, "queryid"));
                    filterMap.put("queryid", primary.getValue(i, "queryid"));
                    DataSet filteredargs = args.getFilteredDataSet(filterMap);
                    if (filteredargs.size() > 0) {
                        JSONArray queryargs = new JSONArray();
                        for (int j = 0; j < filteredargs.size(); ++j) {
                            JSONObject row = new JSONObject();
                            row.put("arg", "param" + (j + 1));
                            row.put("argdesc", filteredargs.getValue(j, "argdesc", filteredargs.getValue(j, "argid")));
                            queryargs.put(row);
                        }
                        primaryrow.put("arguments", queryargs);
                    }
                    queries.put(primaryrow);
                }
            }
        }
        return queries;
    }
}

