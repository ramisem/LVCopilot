/*
 * Decompiled with CFR 0.152.
 */
package sapphire.ext;

import com.labvantage.sapphire.gwt.shared.JSONable;
import com.labvantage.sapphire.util.groovy.DBRead;
import com.labvantage.sapphire.util.groovy.GroovyLogger;
import java.util.HashMap;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.WorkflowProcessor;
import sapphire.ext.BaseScript;
import sapphire.util.M18NUtil;
import sapphire.util.SDIList;
import sapphire.util.TaskContext;
import sapphire.xml.PropertyList;

public abstract class BaseTaskScript
extends BaseScript {
    private HashMap sapphireobjects;
    private TaskContext taskContext;
    private HashMap variables;
    private PropertyList stepproperty;
    private HashMap output;
    protected DBRead database;

    @Override
    protected void setContext(HashMap bindings) {
        super.setContext(bindings);
        this.sapphireobjects = (HashMap)bindings.get("sapphireobjects");
        this.taskContext = (TaskContext)bindings.get("taskContext");
        this.variables = (HashMap)bindings.get("variables");
        this.stepproperty = (PropertyList)bindings.get("stepproperty");
        this.output = (HashMap)bindings.get("output");
    }

    protected ActionProcessor getActionProcessor() {
        return this.sapphireobjects != null ? (ActionProcessor)this.sapphireobjects.get("actionProcessor") : null;
    }

    protected QueryProcessor getQueryProcessor() {
        return this.sapphireobjects != null ? (QueryProcessor)this.sapphireobjects.get("queryProcessor") : null;
    }

    protected SDCProcessor getSDCProcessor() {
        return this.sapphireobjects != null ? (SDCProcessor)this.sapphireobjects.get("sdcProcessor") : null;
    }

    protected SequenceProcessor getSequenceProcessor() {
        return this.sapphireobjects != null ? (SequenceProcessor)this.sapphireobjects.get("sequenceProcessor") : null;
    }

    protected SDIProcessor getSDIProcessor() {
        return this.sapphireobjects != null ? (SDIProcessor)this.sapphireobjects.get("sdiProcessor") : null;
    }

    protected WorkflowProcessor getWorkflowProcessor() {
        return this.sapphireobjects != null ? (WorkflowProcessor)this.sapphireobjects.get("workflowProcessor") : null;
    }

    protected DBRead getDatabase() {
        return this.sapphireobjects != null ? (DBRead)this.sapphireobjects.get("database") : null;
    }

    protected M18NUtil getM18N() {
        return this.sapphireobjects != null ? (M18NUtil)this.sapphireobjects.get("m18n") : null;
    }

    protected GroovyLogger getLogger() {
        return this.sapphireobjects != null ? (GroovyLogger)this.sapphireobjects.get("logger") : null;
    }

    protected TaskContext getTaskContext() {
        return this.taskContext;
    }

    protected JSONable getVariable(String variableid) {
        return (JSONable)this.variables.get(variableid);
    }

    protected void setVariable(String variableid, String value) {
        this.output.put(variableid, value);
    }

    protected void setVariable(String variableid, SDIList value) {
        this.output.put(variableid, value);
    }

    protected PropertyList getStepProperty() {
        return this.stepproperty;
    }

    protected void setOutput(String name, Object value) {
        this.output.put(name, value);
    }
}

