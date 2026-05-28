/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.qcactions;

import com.labvantage.opal.actions.qcactions.QCBatchBaseAction;
import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.util.OpalUtil;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class QCBulletinOnSuccess
extends QCBatchBaseAction {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    int rc = 1;

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        if (actionid.equals("QCBulletinOnSuccess")) {
            this.rc = this.doQCBulletinOnSuccess(props);
        }
        return this.rc;
    }

    private int doQCBulletinOnSuccess(HashMap props) {
        int rc = 1;
        QCBatch qcBatch = null;
        String qcbatchid = (String)props.get("qcbatchid");
        if (qcbatchid == null || qcbatchid.length() == 0) {
            return this.setError("Missing mandatory input : QCBatchId ");
        }
        qcBatch = QCBatchPool.getQCBatch(this.getQueryProcessor(), qcbatchid);
        if (qcBatch == null) {
            return this.setError("QC Batch does not exists: " + qcbatchid);
        }
        String ruleEvaluationDisposition = (String)props.get("ruleevaluationdisposition");
        String specCondition = (String)props.get("speccondition");
        try {
            PropertyList specInterpretationMap = OpalUtil.getSpecInterpretationMap(this.getConfigurationProcessor());
            String passCondition = OpalUtil.getSpecCondition(specInterpretationMap, "Pass");
            if (passCondition.length() == 0) {
                passCondition = "Pass";
            }
            TranslationProcessor tp = this.getTranslationProcessor();
            String createBy = qcBatch.getCreateBy();
            ActionProcessor ap = this.getActionProcessor();
            HashMap<String, String> actionprops = new HashMap<String, String>();
            actionprops.put("source", this.connectionInfo.getSysuserId());
            actionprops.put("description", tp.translate("QCBatch Success Bulletin"));
            StringBuffer bulletinBody = new StringBuffer();
            if (ruleEvaluationDisposition != null && ruleEvaluationDisposition.equals("Pass")) {
                bulletinBody.append(tp.translate("QC Rule Evaluation") + " ");
            }
            if (specCondition != null && specCondition.equals(passCondition)) {
                if (bulletinBody.length() > 0) {
                    bulletinBody.append(tp.translate("and") + " ");
                }
                bulletinBody.append(tp.translate("Spec Evaluation") + " ");
            }
            bulletinBody.append(tp.translate("on") + " " + new SimpleDateFormat().format(Calendar.getInstance().getTime()));
            actionprops.put("body", tp.translate("Batch") + " " + qcbatchid + " " + tp.translate("passes") + " " + bulletinBody.toString());
            actionprops.put("url", "rc?command=page&page=QCBatchReviewMaint&fromBulletin=Y&keyid1=" + HttpUtil.encodeURIComponent(qcbatchid));
            actionprops.put("user", createBy);
            ap.processAction("SendBulletin", "1", actionprops);
        }
        catch (Exception ex) {
            this.logger.error("doQCBulletinOnSuccess error", ex);
        }
        QCBatchPool.releaseQCBatch(qcBatch);
        return rc;
    }
}

