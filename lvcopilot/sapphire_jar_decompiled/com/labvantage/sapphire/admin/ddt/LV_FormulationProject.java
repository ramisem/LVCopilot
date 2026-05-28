/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_FormulationProject
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77330 $";
    public static final String SDCID = "LV_FormulationProject";
    public static final String TABLEID = "formulationproject";
    public static final String COLUMN_TEMPLATEPRODUCTID = "templateproductid";
    public static final String COLUMN_TEMPLATEPRODUCTVERSIONID = "templateproductversionid";
    public static final String COLUMN_FORMULATIONPROJECTID = "formulationprojectid";
    public static final String COLUMN_RECIPETYPEFLAG = "recipetypeflag";
    public static final String COLUMN_FORMULATIONPROJECTSTATUS = "formulationprojectstatus";
    public static final String COLUMN_STARTDATE = "startdt";
    public static final String COLUMN_COMPLETEDDT = "completeddt";
    public static final String COLUMN_CANCELLEDDT = "cancelleddt";
    public static final String COLUMN_SAMPLETYPEID = "sampletypeid";
    public static final String COLUMN_PARENTFORMULATIONPROJECTID = "parentformulationprojectid";
    public static final String COLUMN_PARENTPRODUCTID = "parentproductid";
    public static final String COLUMN_PARENTPRODUCTVERSIONID = "parentproductversionid";
    public static final String RECIPETYPEFLAG_PROPORTIONALQUANTITIES = "P";
    public static final String RECIPETYPEFLAG_ABSOLUTE = "A";
    public static final String FORMULATIONPROJECTSTATUS_ACTIVE = "Active";
    public static final String FORMULATIONPROJECTSTATUS_COMPLETED = "Completed";
    public static final String FORMULATIONPROJECTSTATUS_CANCELLED = "Cancelled";
    public static final String PARAMTYPE_QUANTITY = "Quantity";
    public static final String PARAMTYPE_FRACTION = "Fraction";
    public static final String SAMPLETYPEID_DEVELOPMENT = "Development Formulation";
    public static final String SAMPLETYPEID_PILOT = "Pilot Formulation";

    @Override
    public void postAddKey(DataSet primary, PropertyList actionProps) {
        this.setDefaultValues(primary, actionProps);
    }

    private void setDefaultValues(DataSet primary, PropertyList actionProps) {
        String templateId = actionProps.getProperty("templateid", actionProps.getProperty("templatekeyid1", ""));
        for (int i = 0; i < primary.getRowCount(); ++i) {
            primary.setString(i, COLUMN_RECIPETYPEFLAG, primary.getValue(i, COLUMN_RECIPETYPEFLAG, RECIPETYPEFLAG_PROPORTIONALQUANTITIES));
            primary.setString(i, COLUMN_FORMULATIONPROJECTSTATUS, primary.getValue(i, COLUMN_FORMULATIONPROJECTSTATUS, FORMULATIONPROJECTSTATUS_ACTIVE));
            primary.setString(i, COLUMN_PARENTFORMULATIONPROJECTID, primary.getValue(i, COLUMN_PARENTFORMULATIONPROJECTID, templateId));
            primary.setDate(i, COLUMN_STARTDATE, new M18NUtil(this.connectionInfo).getNowCalendar());
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.doStatusChangeTasks(sdiData);
    }

    private void doStatusChangeTasks(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_FORMULATIONPROJECTSTATUS)) continue;
            String previousFormulationProjectStatus = this.getOldPrimaryValue(primary, i, COLUMN_FORMULATIONPROJECTSTATUS);
            String currentFormulationProjectStatus = primary.getValue(i, COLUMN_FORMULATIONPROJECTSTATUS, "");
            if (currentFormulationProjectStatus.equals(FORMULATIONPROJECTSTATUS_CANCELLED)) {
                primary.setDate(i, COLUMN_CANCELLEDDT, new M18NUtil(this.connectionInfo).getNowCalendar());
            } else if (currentFormulationProjectStatus.equals(FORMULATIONPROJECTSTATUS_COMPLETED)) {
                primary.setDate(i, COLUMN_COMPLETEDDT, new M18NUtil(this.connectionInfo).getNowCalendar());
            }
            if (FORMULATIONPROJECTSTATUS_CANCELLED.equals(previousFormulationProjectStatus)) {
                primary.setDate(i, COLUMN_CANCELLEDDT, "(null)");
                continue;
            }
            if (!FORMULATIONPROJECTSTATUS_COMPLETED.equals(previousFormulationProjectStatus)) continue;
            primary.setDate(i, COLUMN_COMPLETEDDT, "(null)");
        }
    }

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet methods = sdiData.getDataset("formulationprojectmethod");
        if (methods != null) {
            ArrayList<String> projectValidated = new ArrayList<String>();
            for (int i = 0; i < methods.getRowCount(); ++i) {
                String projectId = methods.getValue(i, COLUMN_FORMULATIONPROJECTID);
                if (projectValidated.contains(projectId)) continue;
                projectValidated.add(projectId);
                this.ensureParamTypeValidation(projectId);
            }
        }
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet methods = sdiData.getDataset("formulationprojectmethod");
        if (methods != null) {
            ArrayList<String> projectValidated = new ArrayList<String>();
            for (int i = 0; i < methods.getRowCount(); ++i) {
                String projectId = methods.getValue(i, COLUMN_FORMULATIONPROJECTID);
                if (projectValidated.contains(projectId)) continue;
                projectValidated.add(projectId);
                this.ensureParamTypeValidation(projectId);
            }
        }
    }

    private void ensureParamTypeValidation(String projectId) throws SapphireException {
        PreparedStatement getParamDetails = this.database.prepareStatement("paramdetail", "select p.paramid, p.paramtype, fp.recipetypeflag from paramlistitem p, formulationmethodparamlist fmpl, formulationprojectmethod fpm, formulationproject fp   where p.paramlistid = fmpl.paramlistid and p.paramlistversionid = fmpl.paramlistversionid and p.variantid = fmpl.variantid and  fmpl.formulationmethodid = fpm.formulationmethodid and fmpl.formulationmethodversionid = fpm.formulationmethodversionid  and fpm.formulationprojectid = fp.formulationprojectid and fp.formulationprojectid = ?");
        TranslationProcessor tp = this.getTranslationProcessor();
        try {
            StringBuffer msg = new StringBuffer();
            getParamDetails.setString(1, projectId);
            DataSet ds = new DataSet(getParamDetails.executeQuery());
            int ptypeQuantity = 0;
            int ptypeFraction = 0;
            if (ds.getRowCount() > 0) {
                boolean absoluteQty = RECIPETYPEFLAG_ABSOLUTE.equalsIgnoreCase(ds.getValue(0, COLUMN_RECIPETYPEFLAG));
                for (int d = 0; d < ds.getRowCount(); ++d) {
                    if (PARAMTYPE_QUANTITY.equals(ds.getValue(d, "paramtype"))) {
                        ++ptypeQuantity;
                        continue;
                    }
                    if (!PARAMTYPE_FRACTION.equals(ds.getValue(d, "paramtype"))) continue;
                    ++ptypeFraction;
                }
                if (absoluteQty) {
                    if (ptypeQuantity == 0) {
                        msg.append(tp.translate("No parameter exists with paramtype") + " '" + PARAMTYPE_QUANTITY + "'!");
                    } else if (ptypeQuantity > 1) {
                        msg.append(tp.translate("Exists more than one parameter with paramtype") + " '" + PARAMTYPE_QUANTITY + "'!");
                    }
                    if (ptypeFraction > 0) {
                        msg.append(msg.length() > 0 ? "\n" : "");
                        msg.append(tp.translate("Absolute Quantities Formulation recipe cannot have parameters with paramtype") + " '" + PARAMTYPE_FRACTION + "'.");
                    }
                } else {
                    if (ptypeFraction == 0) {
                        msg.append(tp.translate("No parameter exists with paramtype") + " '" + PARAMTYPE_FRACTION + "'!");
                    } else if (ptypeFraction > 1) {
                        msg.append(msg.length() > 0 ? "\n" : "");
                        msg.append(tp.translate("Exists more than one parameter with paramtype") + " '" + PARAMTYPE_FRACTION + "'!");
                    }
                    if (ptypeQuantity > 0) {
                        msg.append(msg.length() > 0 ? "\n" : "");
                        msg.append(tp.translate("Proportional Quantities Formulation recipe cannot have parameters with paramtype") + " '" + PARAMTYPE_QUANTITY + "'.");
                    }
                }
                if (msg.length() > 0) {
                    this.throwError("Validation Failed", "VALIDATION", msg.toString());
                }
            }
        }
        catch (Exception e) {
            this.logger.error("Error occured in FormulationProject save validation", e);
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        finally {
            this.database.closeStatement("paramdetail");
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

