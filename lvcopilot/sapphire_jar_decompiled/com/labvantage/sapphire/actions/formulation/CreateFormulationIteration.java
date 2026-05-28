/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.formulation;

import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class CreateFormulationIteration
extends BaseAction
implements sapphire.action.CreateFormulationIteration {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53423 $";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.validate(properties);
        this.createIteration(properties);
    }

    private void createIteration(PropertyList properties) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Product");
        props.setProperty("templatekeyid1", properties.getProperty("formulationtemplateid"));
        props.setProperty("templatekeyid2", properties.getProperty("formulationtemplateversionid"));
        props.setProperty("copyattachment", "Y");
        props.setProperty("copyattachmentmode", "E");
        props.setProperty("formulationprojectid", properties.getProperty("formulationprojectid"));
        props.setProperty("formulationlabel", properties.getProperty("formulationlabel"));
        props.setProperty("usersequence", properties.getProperty("usersequence"));
        props.setProperty("sampletypeid", properties.getProperty("sampletypeid"));
        if (properties.getProperty("copymode", "").equals("CHILD")) {
            props.setProperty("parentproductid", properties.getProperty("formulationtemplateid"));
            props.setProperty("parentproductversionid", properties.getProperty("formulationtemplateversionid"));
        }
        this.getActionProcessor().processAction("AddSDI", "1", props);
        properties.setProperty("newformulationid", props.getProperty("newkeyid1"));
        properties.setProperty("newformulationversionid", props.getProperty("newkeyid2"));
        properties.setProperty("keyid1", props.getProperty("newkeyid1"));
        properties.setProperty("keyid2", props.getProperty("newkeyid2"));
        properties.setProperty("sdcid", "Product");
    }

    private void validate(PropertyList properties) throws SapphireException {
        String sql;
        TranslationProcessor tp = this.getTranslationProcessor();
        String formulationTemplateId = properties.getProperty("formulationtemplateid", "");
        String formulationTemplateVersionId = properties.getProperty("formulationtemplateversionid", "");
        String formulationProjectId = properties.getProperty("formulationprojectid", "");
        String formulationLabel = properties.getProperty("formulationlabel", "");
        String copyMode = properties.getProperty("copymode", "PARENT");
        String userSequence = properties.getProperty("usersequence", "");
        String batchSampleCount = properties.getProperty("batchsamplecount", "1");
        String sampleTypeId = properties.getProperty("sampletypeid", "Development Formulation");
        SafeSQL safeSQL = new SafeSQL();
        if (formulationTemplateId.length() == 0) {
            if (formulationProjectId.length() == 0) {
                throw new SapphireException(tp.translate("Mandatory formulationtemplateid/formulationprojectid not passed"));
            }
            sql = "SELECT templateproductid, templateproductversionid FROM formulationproject WHERE formulationprojectid = " + safeSQL.addVar(formulationProjectId);
            DataSet formulationProjectDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (formulationProjectDs.getRowCount() == 0) {
                throw new SapphireException(tp.translate("Failed to retrieve the  formulation-project"));
            }
            formulationTemplateId = formulationProjectDs.getValue(0, "templateproductid", "");
            formulationTemplateVersionId = formulationProjectDs.getValue(0, "templateproductversionid", "");
            if (formulationTemplateId.length() == 0) {
                throw new SapphireException(tp.translate("The formulation-project doesn't have any template"));
            }
        }
        if (formulationTemplateVersionId.length() == 0) {
            throw new SapphireException("Mandatory formulationtemplateversionid not passed");
        }
        if (formulationProjectId.length() == 0) {
            safeSQL.reset();
            sql = "SELECT formulationprojectid FROM s_product WHERE s_productid = '" + formulationTemplateId + "' AND " + "s_productversionid" + " = " + safeSQL.addVar(formulationTemplateVersionId);
            DataSet formulationTemplateDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (formulationTemplateDs.getRowCount() == 0) {
                throw new SapphireException(tp.translate("Mandatory formulationprojectid not passed or filed to retrieve the formulation template"));
            }
            formulationProjectId = formulationTemplateDs.getValue(0, "formulationprojectid", "");
            if (formulationProjectId.length() == 0) {
                throw new SapphireException(tp.translate("The formulation-template doesn't have any project"));
            }
        }
        properties.clear();
        properties.setProperty("formulationtemplateid", formulationTemplateId);
        properties.setProperty("formulationtemplateversionid", formulationTemplateVersionId);
        properties.setProperty("formulationprojectid", formulationProjectId);
        properties.setProperty("formulationlabel", formulationLabel);
        properties.setProperty("copymode", copyMode);
        properties.setProperty("usersequence", userSequence);
        properties.setProperty("batchsamplecount", batchSampleCount);
        properties.setProperty("sampletypeid", sampleTypeId);
    }
}

