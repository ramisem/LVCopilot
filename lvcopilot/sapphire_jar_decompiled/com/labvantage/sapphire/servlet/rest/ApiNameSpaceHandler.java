/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet.rest;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.servlet.rest.ActionsNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.BaseNameSpaceHandler;
import com.labvantage.sapphire.servlet.rest.RestException;
import java.io.IOException;
import java.io.PrintWriter;
import org.json.JSONException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ApiNameSpaceHandler
extends BaseNameSpaceHandler {
    @Override
    public boolean requiresConnection() {
        return true;
    }

    @Override
    public void process() throws Exception {
        if (!this.getPolicyPropertyList().getPropertyListNotNull("systemresources").getPropertyListNotNull("rootresources").getPropertyListNotNull("restapi").getProperty("enabled", "N").equals("Y")) {
            throw new RestException(400, "Resource not available", "Resource disabled in REST policy");
        }
        if (!this.doGet()) {
            throw new RestException(405, "Invalid request method", "/api resource only supports the GET method");
        }
    }

    @Override
    public void respond() throws IOException, JSONException {
        String servletPath = this.request.getServletPath();
        String contextPath = this.request.getContextPath();
        String servletName = contextPath + "/" + (servletPath.startsWith("/") ? servletPath.substring(1) : servletPath);
        if (this.getNameSpaceSegmentCount() == 0) {
            StringBuffer url = this.request.getRequestURL();
            String redirect = url.substring(0, url.lastIndexOf(servletName + "/api"));
            String connectionid = this.request.getParameter("connectionid");
            String token = this.request.getParameter("token");
            String fullRedirect = contextPath + "/WEB-CORE/extapps/swaggerui/customindex.html?url=" + redirect + contextPath + "/rest/api/def";
            if (connectionid != null && connectionid.length() > 0) {
                fullRedirect = fullRedirect + "?connectionid=" + connectionid;
            } else if (token != null && token.length() > 0) {
                fullRedirect = fullRedirect + "?token=" + token;
            }
            this.response.sendRedirect(fullRedirect);
        } else {
            String actionid;
            int i;
            String[] actions;
            String[] stringArray;
            PrintWriter out = this.response.getWriter();
            PropertyList policy = this.getPolicyPropertyList();
            PropertyList rootResources = policy.getPropertyListNotNull("systemresources").getPropertyListNotNull("rootresources");
            PropertyList connectionResources = policy.getPropertyListNotNull("systemresources").getPropertyListNotNull("connectionresources");
            PropertyList actionResources = policy.getPropertyListNotNull("systemresources").getPropertyListNotNull("actionresources");
            PropertyListCollection sdcResources = policy.getCollectionNotNull("sdcresources");
            StringBuffer definition = new StringBuffer();
            definition.append("swagger: '2.0'\n").append("info:\n").append("  version: " + Build.getVersion() + "\n").append("  title: LabVantage REST API\n").append("  description: >\n").append("    The LabVantage REST API provides a lightweight API to access and operate upon").append("    LabVantage resources, e.g. Samples, Batches, Products, etc..\n").append("schemes:\n").append("  - http\n").append("  - https\n").append("basePath: " + contextPath + "\n").append("consumes:\n").append("  - application/json\n").append("produces:\n").append("  - application/json\n").append("paths:\n").append("  " + servletName + ":\n").append("    get:\n").append("      summary: API Home\n").append("      description: Home URI for the LabVantage REST API\n").append("      tags:\n").append("        - /rest/api\n").append("      responses:\n").append("        '200':\n").append("          description: Returns status, build and version\n");
            if (rootResources.getPropertyList("restapi").getProperty("enabled", "N").equals("Y")) {
                definition.append("  " + servletName + "/api:\n").append("    get:\n").append("      summary: API Documentation\n").append("      description: Home URI for the LabVantage REST API documentation - redirects to this page\n").append("      tags:\n").append("        - /rest/api\n").append("      responses:\n").append("        '200':\n").append("          description: Redirects to documentation page\n");
            }
            if (connectionResources.getPropertyList("postconnection").getProperty("enabled", "N").equals("Y")) {
                definition.append("  " + servletName + "/connections:\n").append("    post:\n").append("      summary: Get Connection\n").append("      description: Create a new connectionid\n").append("      tags:\n").append("        - /rest/connections\n").append("      parameters:\n").append("        - name: jsonRequest\n").append("          in: body\n").append("          description: Database connection details\n").append("          required: true\n").append("          schema: \n").append("            $ref: '#/definitions/connectiondetails'\n").append("      responses:\n").append("        '201':\n").append("          description: Connectionid created\n").append("        '403':\n").append("          description: Forbidden / invalid connection details\n");
            }
            if (connectionResources.getPropertyList("getconnection").getProperty("enabled", "N").equals("Y") || connectionResources.getPropertyList("putconnection").getProperty("enabled", "N").equals("Y") || connectionResources.getPropertyList("deleteconnection").getProperty("enabled", "N").equals("Y")) {
                definition.append("  " + servletName + "/connections/{connectionid}:\n").append("    parameters:\n").append("      - name: connectionid\n").append("        in: path\n").append("        description: The connectionid\n").append("        required: true\n").append("        type: string\n");
            }
            if (connectionResources.getPropertyList("getconnection").getProperty("enabled", "N").equals("Y")) {
                definition.append("    get:\n").append("      summary: Check Connection\n").append("      description: Checks the status of a connectionid\n").append("      tags:\n").append("        - /rest/connections\n").append("      responses:\n").append("        '200':\n").append("          description: Connectionid is valid\n").append("        '404':\n").append("          description: Connectionid is invalid or timed out\n");
            }
            if (connectionResources.getPropertyList("putconnection").getProperty("enabled", "N").equals("Y")) {
                definition.append("    put:\n").append("      summary: Ping Connection\n").append("      description: Refreshes the connectionid\n").append("      tags:\n").append("        - /rest/connections\n").append("      responses:\n").append("        '200':\n").append("          description: Connectionid refreshed\n");
            }
            if (connectionResources.getPropertyList("deleteconnection").getProperty("enabled", "N").equals("Y")) {
                definition.append("    delete:\n").append("      summary: Delete Connection\n").append("      description: Deletes/clears the connectionid\n").append("      tags:\n").append("        - /rest/connections\n").append("      responses:\n").append("        '200':\n").append("          description: Connectionid deleted\n");
            }
            if (actionResources.getPropertyList("getactions").getProperty("enabled", "N").equals("Y") || actionResources.getPropertyList("postactions").getProperty("enabled", "N").equals("Y")) {
                definition.append("  " + servletName + "/actions:\n");
            }
            if (actionResources.getPropertyList("getactions").getProperty("enabled", "N").equals("Y")) {
                definition.append("    get:\n").append("      summary: Actions API\n").append("      description: Returns the list of available actions\n").append("      tags:\n").append("        - /rest/actions\n").append("      responses:\n").append("        '200':\n").append("          description: Returns a list of available API actions\n");
            }
            if (actionResources.getPropertyList("postactions").getProperty("enabled", "N").equals("Y")) {
                definition.append("    post:\n").append("      summary: ActionBlock Execution\n").append("      description: Executes an action block\n").append("      tags:\n").append("        - /rest/actions\n").append("      parameters:\n").append("        - name: jsonRequest\n").append("          in: body\n").append("          description: JSON representation of an ActionBlock\n").append("          required: true\n").append("          schema: \n").append("            $ref: '#/definitions/actionblock'\n").append("      responses:\n").append("        '200':\n").append("          description: Returns details about the action block execution\n");
            }
            boolean actionGet = actionResources.getPropertyList("getaction").getProperty("enabled", "N").equals("Y");
            boolean actionPost = actionResources.getPropertyList("postaction").getProperty("enabled", "N").equals("Y");
            if (this.isJUnit) {
                String[] stringArray2 = new String[3];
                stringArray2[0] = "AddSDI";
                stringArray2[1] = "EditSDI";
                stringArray = stringArray2;
                stringArray2[2] = "DeleteSDI";
            } else {
                stringArray = actions = ActionsNameSpaceHandler.getPermittedActions(this.sapphireConnection.getConnectionId());
            }
            if (actionGet || actionPost) {
                for (i = 0; i < actions.length; ++i) {
                    actionid = StringUtil.replaceAll(actions[i], "_", "");
                    definition.append("  " + servletName + "/actions/" + actionid + ":\n");
                    if (actionGet) {
                        definition.append("    get:\n").append("      summary: " + actionid + " API\n").append("      description: Get details about the " + actionid + " action\n").append("      tags:\n").append("        - /rest/actions\n").append("      responses:\n").append("        '200':\n").append("          description: Returns details about the " + actionid + " action\n");
                    }
                    if (!actionPost) continue;
                    definition.append("    post:\n").append("      summary: " + actionid + " Execution\n").append("      description: Executes the " + actionid + " action\n").append("      tags:\n").append("        - /rest/actions\n").append("      parameters:\n").append("        - name: jsonRequest\n").append("          in: body\n").append("          description: JSON representation of action properties\n").append("          required: true\n").append("          schema: \n").append("            $ref: '#/definitions/" + actionid + "'\n").append("      responses:\n").append("        '200':\n").append("          description: Returns details about the " + actionid + " action execution\n");
                }
            }
            sdcResources.index("resourceid");
            for (i = 0; i < sdcResources.size(); ++i) {
                PropertyList sdcResource = sdcResources.getPropertyList(i);
                if (!sdcResource.getProperty("enabled", "N").equals("Y") || !sdcResource.getProperty("type").equals("P")) continue;
                String sdcid = sdcResource.getProperty("sdcid");
                PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcid);
                String singular = sdcProps.getProperty("singular").toLowerCase();
                String plural = sdcProps.getProperty("plural").toLowerCase();
                String key = sdcProps.getProperty("keycolid1") + (sdcProps.getProperty("keycolid2").length() > 0 ? ";" + sdcProps.getProperty("keycolid2") : "") + (sdcProps.getProperty("keycolid3").length() > 0 ? ";" + sdcProps.getProperty("keycolid3") : "");
                this.getRootMethodDefinitions(definition, sdcResource, singular, plural, key, "", sdcResource.getProperty("path"));
                this.getIdMethodDefinitions(definition, sdcResource, singular, key, "", "", sdcResource.getProperty("path"));
                PropertyListCollection subResources = sdcResource.getCollectionNotNull("subresources");
                for (int j = 0; j < subResources.size(); ++j) {
                    PropertyList subResource = subResources.getPropertyList(j);
                    PropertyList subResourcePolicy = sdcResources.getIndexedPropertyList(subResource.getProperty("resourceid"));
                    if (subResourcePolicy == null) continue;
                    this.getRootMethodDefinitions(definition, subResourcePolicy, singular + " " + subResourcePolicy.getProperty("path"), singular + " " + subResourcePolicy.getProperty("path"), key, sdcResource.getProperty("path") + "/{" + key + "}", sdcResource.getProperty("path"));
                    this.getIdMethodDefinitions(definition, subResourcePolicy, singular + " " + subResourcePolicy.getProperty("path"), key, subResourcePolicy.getProperty("path") + "keys", sdcResource.getProperty("path") + "/{" + key + "}", sdcResource.getProperty("path"));
                }
            }
            definition.append("definitions:\n").append("  connectiondetails:\n").append("    type: object\n").append("    required:\n").append("      - databasdeid\n").append("      - username\n").append("      - password\n").append("    properties:\n").append("      databaseid:\n").append("        type: string\n").append("      username:\n").append("        type: string\n").append("      password:\n").append("        type: string\n").append("  actionblock:\n").append("    type: object\n").append("    required:\n").append("      - actionblock\n").append("    properties:\n").append("      actionblock:\n").append("        type: array\n").append("        items: \n").append("          $ref: '/definitions/actionactionblockitem'\n").append("  actionactionblockitem:\n").append("    type: object\n").append("    required:\n").append("      - action\n").append("    properties:\n").append("      action:\n").append("        $ref: '/definitions/action'\n").append("  action:\n").append("    type: object\n").append("    required:\n").append("      - id\n").append("      - name\n").append("    properties:\n").append("      id:\n").append("        type: string\n").append("      name:\n").append("        type: string\n").append("  returnproperty:\n").append("    type: object\n").append("    required:\n").append("      - id\n").append("      - value\n").append("    properties:\n").append("      id:\n").append("        type: string\n").append("      value:\n").append("        type: string\n");
            for (i = 0; i < actions.length; ++i) {
                actionid = StringUtil.replaceAll(actions[i], "_", "");
                DataSet actionProps = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM actionproperty WHERE actionid = ? AND actionversionid = ? ORDER BY usersequence", new Object[]{actions[i], 1});
                definition.append("  " + actionid + ":\n").append("    type: object\n");
                if (actionProps.size() <= 0) continue;
                definition.append("    properties:\n");
                for (int j = 0; j < actionProps.size(); ++j) {
                    definition.append("      " + actionProps.getValue(j, "propertyid") + ":\n ").append("        type: string\n");
                }
            }
            out.print(definition);
            out.close();
        }
    }

    private void getRootMethodDefinitions(StringBuffer definition, PropertyList resourcePolicy, String singular, String plural, String resourceid, String primarypath, String tags) {
        StringBuffer rootmethoddefinition = new StringBuffer();
        PropertyListCollection rootmethods = resourcePolicy.getCollectionNotNull("resourcerootmethods");
        for (int j = 0; j < rootmethods.size(); ++j) {
            PropertyList rootmethod = rootmethods.getPropertyList(j);
            if (!rootmethod.getProperty("enabled", "N").equals("Y")) continue;
            if (rootmethod.getProperty("method").equals("GET")) {
                rootmethoddefinition.append("    get:\n").append("      summary: Get " + StringUtil.initCaps(singular) + " Data\n").append("      description: Get details about " + plural + "\n").append("      tags:\n").append("        - /rest/sdc/" + tags + "\n");
                if (resourcePolicy.getProperty("type").equals("P")) {
                    rootmethoddefinition.append("      parameters:\n").append("        - name: queryid\n").append("          in: query\n").append("          description: The query to get the " + plural + " (used mutually exclusively with keyid1/2/3 lists)\n").append("          required: false\n").append("          type: string\n").append("        - name: param1..n\n").append("          in: query\n").append("          description: The query parameters specified as param1=value1&amp;param2=value2, etc.\n").append("          required: false\n").append("          type: string\n").append("        - name: keyid1\n").append("          in: query\n").append("          description: The keyid1 list of the " + plural + " to get (used mutually exclusively with queryid)\n").append("          required: false\n").append("          type: string\n").append("        - name: keyid2\n").append("          in: query\n").append("          description: The keyid2 list of the " + plural + " to get (used mutually exclusively with queryid)\n").append("          required: false\n").append("          type: string\n").append("        - name: keyid3\n").append("          in: query\n").append("          description: The keyid3 list of the " + plural + " to get (used mutually exclusively with queryid)\n").append("          required: false\n").append("          type: string\n").append("        - name: fields\n").append("          in: query\n").append("          description: Defines the required return columns, e.g. filter=(s_sampleid, sampledesc)\n").append("          required: false\n").append("          type: string\n");
                } else {
                    rootmethoddefinition.append("      parameters:\n").append("        - name: " + resourceid + "\n").append("          in: path\n").append("          description: The " + singular.substring(0, singular.indexOf(" ")) + " key\n").append("          required: true\n").append("          type: string\n");
                }
                rootmethoddefinition.append("      responses:\n").append("        '200':\n").append("          description: Returns details about " + plural + "\n");
                continue;
            }
            if (rootmethod.getProperty("method").equals("POST")) {
                rootmethoddefinition.append("    post:\n").append("      summary: Add " + StringUtil.initCaps(plural) + "\n").append("      description: Add new " + plural + "\n").append("      tags:\n").append("        - /rest/sdc/" + tags + "\n").append("      parameters:\n").append("        - name: jsonRequest\n").append("          in: body\n").append("          description: JSON representation of the " + singular + " properties\n").append("          required: true\n").append("          schema:\n").append("            $ref: '#/definitions/" + singular + "'\n").append("      responses:\n").append("        '200':\n").append("          description: Returns the new " + plural + " created\n");
                continue;
            }
            if (!rootmethod.getProperty("method").equals("PUT")) continue;
            rootmethoddefinition.append("    put:\n").append("      summary: Update " + StringUtil.initCaps(plural) + "\n").append("      description: Update existing " + plural + "\n").append("      tags:\n").append("        - /rest/sdc/" + tags + "\n").append("      parameters:\n").append("        - name: jsonRequest\n").append("          in: body\n").append("          description: JSON representation of the " + singular + " properties including key columns\n").append("          required: true\n").append("          schema:\n").append("            $ref: '#/definitions/" + singular + "'\n").append("      responses:\n").append("        '200':\n").append("          description: Returns the updated " + plural + "\n");
        }
        if (rootmethoddefinition.length() > 0) {
            definition.append("  /" + this.servletName + "/" + "sdc" + "/" + (primarypath.length() > 0 ? primarypath + "/" : "") + resourcePolicy.getProperty("path") + ":\n").append(rootmethoddefinition);
        }
    }

    private void getIdMethodDefinitions(StringBuffer definition, PropertyList resourcePolicy, String singular, String resourceid, String subresourceid, String primarypath, String tags) {
        StringBuffer idmethoddefinition = new StringBuffer();
        PropertyListCollection idmethods = resourcePolicy.getCollectionNotNull("resourceidmethods");
        for (int j = 0; j < idmethods.size(); ++j) {
            PropertyList rootmethod = idmethods.getPropertyList(j);
            if (!rootmethod.getProperty("enabled", "N").equals("Y")) continue;
            if (rootmethod.getProperty("method").equals("GET")) {
                idmethoddefinition.append("    get:\n").append("      summary: Get " + StringUtil.initCaps(singular) + " Data\n").append("      description: Get details about a specific " + singular + "\n").append("      tags:\n").append("        - /rest/sdc/" + tags + "\n").append("      parameters:\n").append("        - name: " + resourceid + "\n").append("          in: path\n").append("          description: The " + (subresourceid.length() > 0 ? singular.substring(0, singular.indexOf(" ")) : singular) + " key\n").append("          required: true\n").append("          type: string\n");
                if (subresourceid.length() > 0) {
                    idmethoddefinition.append("        - name: " + subresourceid + "\n").append("          in: path\n").append("          description: The " + resourcePolicy.getProperty("path") + " key\n").append("          required: true\n").append("          type: string\n");
                }
                idmethoddefinition.append("      responses:\n").append("        '200':\n").append("          description: Returns details about " + singular + " " + resourceid + "\n").append("        '404':\n").append("          description: " + singular + " " + resourceid + " not found\n");
                continue;
            }
            if (rootmethod.getProperty("method").equals("PUT")) {
                idmethoddefinition.append("    put:\n").append("      summary: Edit " + StringUtil.initCaps(singular) + " Details\n").append("      description: Edit an existing " + singular + "\n").append("      tags:\n").append("        - /rest/sdc/" + tags + "\n").append("      parameters:\n").append("        - name: " + resourceid + "\n").append("          in: path\n").append("          description: The " + (subresourceid.length() > 0 ? singular.substring(0, singular.indexOf(" ")) : singular) + " key\n").append("          required: true\n").append("          type: string\n");
                if (subresourceid.length() > 0) {
                    idmethoddefinition.append("        - name: " + subresourceid + "\n").append("          in: path\n").append("          description: The " + resourcePolicy.getProperty("path") + " key\n").append("          required: true\n").append("          type: string\n");
                }
                idmethoddefinition.append("      responses:\n").append("        '200':\n").append("          description: " + singular + " updated\n").append("        '404':\n").append("          description: " + singular + " " + resourceid + " not found\n");
                continue;
            }
            if (!rootmethod.getProperty("method").equals("DELETE")) continue;
            idmethoddefinition.append("    delete:\n").append("      summary: Delete " + StringUtil.initCaps(singular) + "\n").append("      description: Delete an existing " + singular + "\n").append("      tags:\n").append("        - /rest/sdc/" + tags + "\n").append("      parameters:\n").append("        - name: " + resourceid + "\n").append("          in: path\n").append("          description: The " + (subresourceid.length() > 0 ? singular.substring(0, singular.indexOf(" ")) : singular) + " key\n").append("          required: true\n").append("          type: string\n");
            if (subresourceid.length() > 0) {
                idmethoddefinition.append("        - name: " + subresourceid + "\n").append("          in: path\n").append("          description: The " + resourcePolicy.getProperty("path") + " key\n").append("          required: true\n").append("          type: string\n");
            }
            idmethoddefinition.append("      responses:\n").append("        '200':\n").append("          description: " + singular + " deleted\n").append("        '404':\n").append("          description: " + singular + " " + resourceid + " not found\n");
        }
        if (idmethoddefinition.length() > 0) {
            definition.append("  /" + this.servletName + "/" + "sdc" + "/" + (primarypath.length() > 0 ? primarypath + "/" : "") + resourcePolicy.getProperty("path") + "/{" + (subresourceid.length() > 0 ? subresourceid : resourceid) + "}:\n").append(idmethoddefinition);
        }
    }
}

