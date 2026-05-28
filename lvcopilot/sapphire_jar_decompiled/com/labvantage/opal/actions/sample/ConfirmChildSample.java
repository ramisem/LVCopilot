/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions.sample;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.storage.EditTrackItem;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ConfirmChildSample
extends BaseAction {
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String sampleid = actionProps.getProperty("sampleid", "");
        sampleid = StringUtil.replaceAll(sampleid, "%3B", ";");
        String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        String auditreason = actionProps.getProperty("auditreason", "Confirming child samples");
        this.validateChildSamples(sampleid, sysuserid);
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Sample");
        props.setProperty("keyid1", sampleid);
        props.setProperty("storagestatus", "In Circulation");
        props.setProperty("confirmedby", sysuserid);
        props.setProperty("confirmeddt", "n");
        props.setProperty("auditreason", auditreason);
        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        props.clear();
        props.setProperty("sdcid", "Sample");
        props.setProperty("keyid1", sampleid);
        props.setProperty("custodialuserid", sysuserid);
        props.setProperty("custodytakendt", "n");
        this.getActionProcessor().processActionClass(EditTrackItem.class.getName(), props);
    }

    private void validateChildSamples(String sampleid, String sysuserid) throws SapphireException {
        if (StringUtil.getLen(sampleid) == 0L) {
            throw new SapphireException("Sampleid not found in request. Please make sure you selected samples. If problem persists, contact Administrator.");
        }
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        String sampleInClause = StringUtil.replaceAll(sampleid, ";", "','");
        sql.append("select s.s_sampleid, (select count(t2.trackitemid) from trackitem t2 where t2.linksdcid = 'Sample' and t2.linkkeyid1 = s.s_sampleid ) ticount");
        sql.append(" from s_sample s");
        sql.append(" where s.s_sampleid in (").append(safeSQL.addIn(sampleInClause)).append(")");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                int count = ds.getInt(i, "ticount");
                if (count == 0) {
                    throw new SapphireException("[" + ds.getValue(i, "s_sampleid") + "] " + this.getTranslationProcessor().translate("Sample does not have associated TrackItem."));
                }
                if (count <= 1) continue;
                throw new SapphireException("[" + ds.getValue(i, "s_sampleid") + "] " + this.getTranslationProcessor().translate("Sample has multiple TrackItems."));
            }
        }
        boolean isGLPRuleActive = this.isBBRuleActive("GLP Rule");
        boolean isUserGLP = "Y".equals(OpalUtil.getColumnValue(this.getQueryProcessor(), "sysuser", "glpflag", "sysuserid = ?", new String[]{sysuserid}));
        safeSQL.reset();
        sql.setLength(0);
        sql.append("select s.s_sampleid, s.glpflag, s.sampletypeid, s.storagestatus, s.confirmedby, t.custodialuserid, t.custodialdepartmentid");
        sql.append(" from s_sample s, trackitem t");
        sql.append(" where s.s_sampleid in (").append(safeSQL.addIn(sampleInClause)).append(")");
        sql.append(" and t.linksdcid = 'Sample'");
        sql.append(" and t.linkkeyid1 = s.s_sampleid");
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                String glpflag = ds.getValue(i, "glpflag", "N");
                String storagestatus = ds.getString(i, "storagestatus");
                String sampleCD = ds.getString(i, "custodialdepartmentid");
                if (!this.isDepartmentMember(sampleCD)) {
                    throw new SapphireException(this.getTranslationProcessor().translate("User is not a member of Sample's Custodial Department"));
                }
                if (isGLPRuleActive && "Y".equals(glpflag) && !isUserGLP) {
                    throw new SapphireException(this.getTranslationProcessor().translate("The Sample will lose GLP status as user is not GLP certified"));
                }
                if ("In Prep".equals(storagestatus)) continue;
                throw new SapphireException(this.getTranslationProcessor().translate("Sample must be in the status of In Prep"));
            }
        }
    }

    private boolean isBBRuleActive(String ruleName) throws SapphireException {
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
        return policy != null && "Active".equals(policy.getPropertyListNotNull("rules").getProperty(ruleName, "Active"));
    }

    private boolean isDepartmentMember(String department) {
        return this.getDepartmentList().contains(department);
    }
}

