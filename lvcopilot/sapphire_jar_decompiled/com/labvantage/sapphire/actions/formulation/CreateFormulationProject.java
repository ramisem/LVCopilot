/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.formulation;

import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class CreateFormulationProject
extends BaseAction
implements sapphire.action.CreateFormulationProject {
    public static final String LABVANTAGE_CVS_ID = "$Revision: 78971 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.validate(properties);
        String createOption = properties.getProperty("createoption", "fromscratch");
        if (createOption.equals("fromscratch")) {
            this.createFromScratch(properties, "", "");
        } else if (createOption.equals("fromexistingformulationproject")) {
            this.createFromExistingFormulationProject(properties);
        } else if (createOption.equals("fromexistingproduct") || createOption.equals("fromexistingformulationiteration")) {
            this.createFromScratch(properties, properties.getProperty("productid", ""), properties.getProperty("productversionid", ""));
        }
    }

    private void validate(PropertyList properties) throws SapphireException {
        String formulationProjectId = properties.getProperty("formulationprojectid", "");
        if (formulationProjectId.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Mandatory input formulationprojectid not specified"));
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT formulationprojectid FROM formulationproject";
        sql = sql + " WHERE formulationprojectid = " + safeSQL.addVar(formulationProjectId);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("FormulationProject with same id already exists"));
        }
    }

    private void createFromScratch(PropertyList properties, String productId, String productVersionId) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_FormulationProject");
        props.setProperty("keyid1", properties.getProperty("formulationprojectid", ""));
        props.setProperty("sampletypeid", properties.getProperty("formulationtype", "Development Formulation"));
        this.getActionProcessor().processAction("AddSDI", "1", props);
        String newFormulationProjectId = props.getProperty("newkeyid1", "");
        if (newFormulationProjectId.length() <= 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Formulation Project could not be created"));
        }
        properties.setProperty("newkeyid1", newFormulationProjectId);
        this.createAndAttachFormulationTemplate(newFormulationProjectId, productId, productVersionId);
    }

    private void createAndAttachFormulationTemplate(String newFormulationProjectId, String templateFormulationTemplateId, String templateFormulationTemplateVersionId) throws ActionException {
        ActionBlock formulationTemplateBlock = new ActionBlock();
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Product");
        props.setProperty("formulationprojectid", newFormulationProjectId);
        props.setProperty("newkeyid1", "[formulationtemplateid]");
        props.setProperty("newkeyid2", "[formulationtemplateversionid]");
        props.setProperty("templateflag", "Y");
        if (templateFormulationTemplateId != null && templateFormulationTemplateId.length() > 0) {
            props.setProperty("templatekeyid1", templateFormulationTemplateId);
            props.setProperty("templatekeyid2", templateFormulationTemplateVersionId);
            props.setProperty("copyattachment", "Y");
            props.setProperty("copyattachmentmode", "E");
            props.setProperty("sampletypeid", "(null)");
            props.setProperty("usersequence", "(null)");
            props.setProperty("formulationstatus", "(null)");
            props.setProperty("recipetypeflag", "(null)");
            props.setProperty("parentproductid", "(null)");
            props.setProperty("parentproductversionid", "(null)");
        } else {
            props.setProperty("batchsamplecount", "1");
        }
        formulationTemplateBlock.setAction("Add Formulation Template", "AddSDI", "1", props);
        props = new PropertyList();
        props.setProperty("sdcid", "LV_FormulationProject");
        props.setProperty("keyid1", newFormulationProjectId);
        props.setProperty("templateproductid", "[formulationtemplateid]");
        props.setProperty("templateproductversionid", "[formulationtemplateversionid]");
        props.setProperty("parentproductid", templateFormulationTemplateId);
        props.setProperty("parentproductversionid", templateFormulationTemplateVersionId);
        formulationTemplateBlock.setAction("Set Formulation Template", "EditSDI", "1", props);
        this.getActionProcessor().processActionBlock(formulationTemplateBlock);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void createFromExistingFormulationProject(PropertyList properties) throws SapphireException {
        String templateFormulationProjectId = properties.getProperty("formulationprojecttemplate", "");
        if (templateFormulationProjectId.length() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("Mandatory formulationprojecttemplate not provided"));
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_FormulationProject");
        props.setProperty("keyid1", properties.getProperty("formulationprojectid", ""));
        props.setProperty("templatekeyid1", templateFormulationProjectId);
        props.setProperty("copyattachment", "Y");
        props.setProperty("copyattachmentmode", "E");
        props.setProperty("sampletypeid", properties.getProperty("formulationtype", "Development Formulation"));
        props.setProperty("templateproductid", "");
        props.setProperty("templateproductversionid", "");
        this.getActionProcessor().processAction("AddSDI", "1", props);
        String newFormulationProjectId = props.getProperty("newkeyid1", "");
        if (newFormulationProjectId.length() <= 0) throw new SapphireException(this.getTranslationProcessor().translate("Formulation Project could not be created"));
        properties.setProperty("newkeyid1", newFormulationProjectId);
        String copyAllIterationsFlag = properties.getProperty("copyallformulationflag", "N");
        String templateFormulationTemplateId = properties.getProperty("templateformulationtemplateid", "");
        String templateFormulationTemplateVersionId = properties.getProperty("templateformulationtemplateversionid", "1");
        String selectedFormulationIds = properties.getProperty("selectedformulationids", "");
        String selectedFormulationVersionIds = properties.getProperty("selectedformulationversionids", "");
        String formulationIdToBeCopied = "";
        String formulationVersionIdToBeCopied = "";
        String formulationTemplateFlag = "";
        if (selectedFormulationIds.length() > 0) {
            DataSet templateFormulationDs;
            formulationIdToBeCopied = selectedFormulationIds;
            formulationVersionIdToBeCopied = selectedFormulationVersionIds;
            int noOfIterationsSelected = formulationIdToBeCopied.length() > 0 ? StringUtil.split(formulationIdToBeCopied, ";", true).length : 0;
            for (int i = 0; i < noOfIterationsSelected; ++i) {
                formulationTemplateFlag = formulationTemplateFlag + ";N";
            }
            if (formulationTemplateFlag.length() > 0) {
                formulationTemplateFlag = formulationTemplateFlag.substring(1);
            }
            if ((templateFormulationDs = this.getProjectTemplateDataSet(templateFormulationProjectId)).getRowCount() > 0) {
                if (formulationIdToBeCopied.length() > 0) {
                    formulationIdToBeCopied = formulationIdToBeCopied + ";" + templateFormulationDs.getValue(0, "s_productid", "");
                    formulationVersionIdToBeCopied = formulationVersionIdToBeCopied + ";" + templateFormulationDs.getValue(0, "s_productversionid", "");
                    formulationTemplateFlag = formulationTemplateFlag + ";Y";
                } else {
                    formulationIdToBeCopied = templateFormulationDs.getValue(0, "s_productid", "");
                    formulationVersionIdToBeCopied = templateFormulationDs.getValue(0, "s_productversionid", "");
                    formulationTemplateFlag = "Y";
                }
            }
        } else if (templateFormulationTemplateId.length() > 0) {
            formulationIdToBeCopied = templateFormulationTemplateId;
            formulationVersionIdToBeCopied = templateFormulationTemplateVersionId;
            formulationTemplateFlag = "Y";
        } else if (copyAllIterationsFlag.equals("Y")) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder("SELECT ");
            sql.append("s_productid").append(", ").append("s_productversionid");
            sql.append(" , ").append("templateflag");
            sql.append(" FROM ").append("s_product");
            sql.append(" WHERE ").append("formulationprojectid").append(" = ").append(safeSQL.addVar(templateFormulationProjectId));
            DataSet iterationsDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            formulationIdToBeCopied = iterationsDs.getColumnValues("s_productid", ";");
            formulationVersionIdToBeCopied = iterationsDs.getColumnValues("s_productversionid", ";");
            formulationTemplateFlag = iterationsDs.getColumnValues("templateflag", ";");
        } else {
            formulationTemplateFlag = "Y";
            DataSet templateFormulationDs = this.getProjectTemplateDataSet(templateFormulationProjectId);
            if (templateFormulationDs.getRowCount() > 0) {
                formulationIdToBeCopied = templateFormulationDs.getValue(0, "s_productid", "");
                formulationVersionIdToBeCopied = templateFormulationDs.getValue(0, "s_productversionid", "");
            }
        }
        if (formulationTemplateFlag.length() <= 0) return;
        String[] formulationIdToBeCopiedArray = StringUtil.split(formulationIdToBeCopied, ";", true);
        String[] formulationVersionIdToBeCopiedArray = StringUtil.split(formulationVersionIdToBeCopied, ";", true);
        String[] formulationTemplateFlagArray = StringUtil.split(formulationTemplateFlag, ";", true);
        if (formulationIdToBeCopiedArray.length != formulationVersionIdToBeCopiedArray.length) throw new SapphireException(this.getTranslationProcessor().translate("FormulationId-FormulationVersionId length mismatch"));
        ActionBlock formulationIterationBlock = new ActionBlock();
        for (int i = 0; i < formulationIdToBeCopiedArray.length; ++i) {
            String templateFormulationId = formulationIdToBeCopiedArray[i];
            String templateFormulationVersionId = formulationVersionIdToBeCopiedArray[i];
            if (formulationTemplateFlagArray[i].equalsIgnoreCase("Y")) {
                this.createAndAttachFormulationTemplate(newFormulationProjectId, templateFormulationId, templateFormulationVersionId);
                continue;
            }
            props = new PropertyList();
            props.setProperty("sdcid", "Product");
            props.setProperty("templatekeyid1", templateFormulationId);
            props.setProperty("templatekeyid2", templateFormulationVersionId);
            props.setProperty("copyattachment", "Y");
            props.setProperty("copyattachmentmode", "E");
            props.setProperty("sampletypeid", properties.getProperty("formulationtype", "Development Formulation"));
            props.setProperty("formulationprojectid", newFormulationProjectId);
            formulationIterationBlock.setAction("Add Formulation iteration: " + i, "AddSDI", "1", props);
        }
        this.getActionProcessor().processActionBlock(formulationIterationBlock);
    }

    private DataSet getProjectTemplateDataSet(String templateFormulationProjectId) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append("s_productid").append(", ").append("s_productversionid");
        sql.append(" FROM ").append("s_product");
        sql.append(" WHERE ").append("formulationprojectid").append(" = ").append(safeSQL.addVar(templateFormulationProjectId));
        sql.append(" AND ").append("templateflag").append(" = 'Y'");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }
}

