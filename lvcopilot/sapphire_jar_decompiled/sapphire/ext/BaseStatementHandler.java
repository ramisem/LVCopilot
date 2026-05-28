/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package sapphire.ext;

import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.StatementsUtil;
import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public abstract class BaseStatementHandler
implements Serializable {
    protected PropertyList authenticationProps = null;
    protected String username = null;
    protected String database = null;
    protected String connectionid = null;
    protected DataSet statements = null;
    protected String statementid = null;
    protected String statementversionid = null;
    protected String statementtype = null;
    protected String statementcounter = "0";
    protected String donotpromptagain = "N";

    public void init(PropertyList authenticationPL, String username, String database, String connectionid, String systemPassword) {
        DataSet statements;
        this.authenticationProps = authenticationPL;
        this.connectionid = connectionid;
        if (username == null && database == null && connectionid != null && connectionid.length() > 0) {
            SapphireConnection sapphireConnection = new ConnectionProcessor(connectionid).getSapphireConnection();
            this.username = sapphireConnection.getSysuserName();
            this.database = sapphireConnection.getDatabaseId();
            this.authenticationProps.setDatabaseid(this.database);
        } else {
            this.username = username;
            this.database = database;
        }
        String sysconnectionid = new ConnectionProcessor().getConnectionid(this.database, "(system)", systemPassword);
        QueryProcessor qp = new QueryProcessor(sysconnectionid);
        this.statements = statements = StatementsUtil.getUserPendingStatements(this.username, qp);
    }

    public String getConnectionid() {
        return this.connectionid;
    }

    public String getUsername() {
        return this.username;
    }

    public String getDatabase() {
        return this.database;
    }

    public void setStatementId(String statementid) {
        this.statementid = statementid;
    }

    public void setStatementVersionId(String statementversionid) {
        this.statementversionid = statementversionid;
    }

    public void setStatementType(String statementtype) {
        this.statementtype = statementtype;
    }

    public void setStatementCounter(String statementcounter) {
        this.statementcounter = statementcounter;
    }

    public void setDoNotPromptAgain(String donotpromptagain) {
        this.donotpromptagain = donotpromptagain;
    }

    public String getStatementId() {
        return this.statementid;
    }

    public String getStatementVersionId() {
        return this.statementversionid;
    }

    public String getStatementType() {
        return this.statementtype;
    }

    public DataSet getStatements() {
        return this.statements;
    }

    public String getStatementCounter() {
        return this.statementcounter;
    }

    public PropertyList getAuthenticationProps() {
        return this.authenticationProps;
    }

    public String getDoNotPromptAgain() {
        return this.donotpromptagain;
    }

    public abstract void renderPrompt(HttpServletRequest var1, HttpServletResponse var2) throws Exception;
}

