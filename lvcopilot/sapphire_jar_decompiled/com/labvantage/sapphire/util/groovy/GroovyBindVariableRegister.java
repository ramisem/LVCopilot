/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.groovy;

public class GroovyBindVariableRegister {
    public static final String COMMON_ELEMENT_VARS = "pagedata;sdc;element;user";
    public static final String PRIMARY = "primary";
    public static final String PRIMARY_COLUMNS = "primary[columns]";
    public static final String PRIMARY_SDCCOLUMNS = "primary[sdccolumns]";
    public static final String POLICY = "policy";

    public static String getVariables(String variableName) {
        if (variableName != null) {
            switch (variableName) {
                case "element_maint_column_property": {
                    return "pagedata;sdc;element;user;primary[columns];policy;primarydataset;currentrow;devmode";
                }
                case "element_list_mode": {
                    return COMMON_ELEMENT_VARS;
                }
                case "element_gwtdataentrygrid_dataitemdisplayrule": {
                    return "primary[sdccolumns];sdidata;sdidataitem;user";
                }
                case "element_gwtdataentrygrid_datasetalertrule": {
                    return "primary[sdccolumns];sdidata;sdidataitemdataset;pagedata;sdc;element;user";
                }
                case "element_advancedtoolbar_button_show": 
                case "element_advancedtoolbar_esig_required": {
                    return "pagedata;sdc;element;user;elements;policy";
                }
                case "pagetype_AdhocQuery_restrictivewhere": {
                    return "pagedata;user";
                }
                case "pagetype_MaintenanceForm_elements_show": {
                    return "pagedata;sdc;element;user;primary;policy;primarydataset;currentrow;devmode";
                }
                case "pagetype_Prompt_column_property": {
                    return "pagedata;sdc;element;user;primary[columns];policy;primarydataset;currentrow;devmode";
                }
                case "pagetype_QuickCreateSDI_column_property": {
                    return "pagedata;sdc;element;user;primary;policy;devmode";
                }
                case "pagetype_QuickCreateSDI_column_property_defaultvalue": {
                    return "pagedata.userinput;pagedata;sdc;element;user;primary;policy;devmode";
                }
                case "pagetype_Searcher_imagesrc": {
                    return "primary;note;attachment;type.sdi;type.note;type.attachment";
                }
                case "NavigatorPolicy_node_operation_applytoset": {
                    return "sdc;user";
                }
                case "NavigatorPolicy_node_operation_show": {
                    return "primary;sdc;user;primarydataset";
                }
                case "NavigatorPolicy_node_childnode_show": {
                    return PRIMARY;
                }
                case "MaskingPolicy_sdc_col_enablemasking": {
                    return "primary;user;columnid;sqldataset";
                }
                case "MaskingPolicy_sdc_col_datatype_expression": {
                    return "primary;user;value;columnid";
                }
                case "CMTPolicy": {
                    return "sdcid;keyid1;keyid2;keyid3;primary;max( columnid );max( datasetname, columnid );";
                }
                case "element_sqlview_column_show": {
                    return "user";
                }
                case "pagetype_TrackitemManagement_extraprops": {
                    return "trackitem.;samplefamily.;study.;participant.;sdcid;keyid1;scannedvalue;tableid;scannedvaluefound;queryprocessor;sourcesdcid;sourcekeyid1;sourcestorageunitid;targetstorageunitid;user.";
                }
                case "gizmo_imagemapgizmo_color": {
                    return "user;element;count;gizmo.hotspot";
                }
            }
        }
        return "";
    }
}

