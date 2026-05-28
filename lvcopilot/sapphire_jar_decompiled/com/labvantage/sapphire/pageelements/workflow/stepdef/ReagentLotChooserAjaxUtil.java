/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.stepdef;

import java.util.Calendar;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ReagentLotChooserAjaxUtil
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90031 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        ObjectType type;
        Mode mode;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "StepHandler");
        String qcbatchid = ajaxResponse.getRequestParameter("qcbatchid", "");
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        String reagentlotid = ajaxResponse.getRequestParameter("reagentlotid", "");
        try {
            mode = Mode.valueOf(ajaxResponse.getRequestParameter("mode", Mode.VALIDATION.toString()).toUpperCase());
        }
        catch (Exception e) {
            mode = Mode.VALIDATION;
        }
        try {
            type = ObjectType.valueOf(ajaxResponse.getRequestParameter("type", ObjectType.QCBATCHSAMPLETYPES.toString()).toUpperCase());
        }
        catch (Exception e) {
            type = ObjectType.QCBATCHSAMPLETYPES;
        }
        if (qcbatchid.length() > 0) {
            if (mode == Mode.SAVE) {
                if (trackitemid.length() > 0) {
                    if (reagentlotid.length() > 0) {
                        String itemid = ajaxResponse.getRequestParameter("itemid", "");
                        if (itemid.length() > 0) {
                            PropertyList actionProps = new PropertyList();
                            switch (type) {
                                case QCBATCHREAGENTS: {
                                    actionProps.setProperty("sdcid", "LV_QCBatchReagent");
                                    actionProps.setProperty("keyid1", itemid);
                                    actionProps.setProperty("reagentlotid", reagentlotid);
                                    actionProps.setProperty("trackitemid", trackitemid);
                                    try {
                                        this.getActionProcessor().processAction("EditSDI", "1", actionProps);
                                    }
                                    catch (Exception e) {
                                        this.logger.error("Failed to update QC Batch Reagent", e);
                                        ajaxResponse.setError(this.getTranslationProcessor().translate("Failed to update QC Batch Reagent."));
                                    }
                                    break;
                                }
                                default: {
                                    actionProps.setProperty("qcbatchid", qcbatchid);
                                    actionProps.setProperty("s_qcbatchsampletypeid", itemid);
                                    actionProps.setProperty("trackitemid", trackitemid);
                                    actionProps.setProperty("reagentlotid", reagentlotid);
                                    try {
                                        this.getActionProcessor().processAction("FillQCSampleTypes", "1", actionProps);
                                    }
                                    catch (Exception e) {
                                        this.logger.error("Failed to update QC Sample Types", e);
                                        ajaxResponse.setError(this.getTranslationProcessor().translate("Failed to update QC Sample Types."));
                                    }
                                    break;
                                }
                            }
                        } else {
                            ajaxResponse.setError(this.getTranslationProcessor().translate("No Item Id provided."));
                        }
                    } else {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("No Reagent Lot Id provided."));
                    }
                } else {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("No Track Item Id provided."));
                }
            } else {
                String reagenttypeid = ajaxResponse.getRequestParameter("reagenttypeid", "");
                boolean valid = false;
                String out_reagentLotId = "";
                String out_reagentLotDesc = "";
                String out_reagentTypeId = "";
                String out_expirydt = "";
                String row = ajaxResponse.getRequestParameter("row", "0");
                String reason = "";
                if (trackitemid.length() > 0) {
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuffer sql = new StringBuffer();
                    sql.append("SELECT linkkeyid1, expirydt, ").append("(SELECT reagentlotdesc FROM reagentlot WHERE reagentlotid = linkkeyid1) rl_desc, ").append("(SELECT reagenttypeid FROM reagentlot WHERE reagentlotid = linkkeyid1) rl_typeid, ").append("(SELECT expirydt FROM reagentlot WHERE reagentlotid = linkkeyid1) rl_expirydt ").append(" FROM trackitem WHERE trackitemid=").append(safeSQL.addVar(trackitemid));
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds != null && ds.size() > 0) {
                        Calendar exdate = ds.getCalendar(0, "expirydt");
                        Calendar rl_expdt = ds.getCalendar(0, "rl_expirydt");
                        if (exdate != null) {
                            out_expirydt = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId())).format(exdate);
                        } else if (exdate == null && rl_expdt != null) {
                            out_expirydt = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId())).format(rl_expdt);
                        }
                        out_reagentLotDesc = ds.getValue(0, "rl_desc", "");
                        out_reagentTypeId = ds.getValue(0, "rl_typeid", "");
                        out_reagentLotId = ds.getValue(0, "linkkeyid1", "");
                        if (out_reagentLotId.length() > 0) {
                            boolean validReagentType = true;
                            if (reagenttypeid.length() > 0 && !reagenttypeid.equalsIgnoreCase(out_reagentTypeId)) {
                                validReagentType = false;
                                reason = "Reagent types do not match.";
                            }
                            if (validReagentType) {
                                if (exdate == null) {
                                    valid = true;
                                } else if (exdate.before(Calendar.getInstance())) {
                                    reason = "Track item has expired.";
                                } else {
                                    valid = true;
                                }
                            }
                        } else {
                            reason = "No reagent lot for container.";
                        }
                    } else {
                        reason = "No data obtained.";
                    }
                } else {
                    reason = "No track item id entered.";
                }
                ajaxResponse.addCallbackArgument("row", row);
                ajaxResponse.addCallbackArgument("reagentLotId", out_reagentLotId);
                ajaxResponse.addCallbackArgument("reagentLotDesc", out_reagentLotDesc);
                ajaxResponse.addCallbackArgument("reagentTypeId", out_reagentTypeId);
                ajaxResponse.addCallbackArgument("expirydt", out_expirydt);
                ajaxResponse.addCallbackArgument("valid", valid ? "Y" : "N");
                ajaxResponse.addCallbackArgument("reason", reason);
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No QC Batch Id provided."));
        }
        ajaxResponse.print();
    }

    protected static enum Mode {
        SAVE,
        VALIDATION;

    }

    public static enum ObjectType {
        QCBATCHSAMPLETYPES,
        QCBATCHREAGENTS;

    }
}

