/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.transport;

import com.labvantage.sapphire.webservices.transport.PropertyListCollectionTransportBean;
import com.labvantage.sapphire.webservices.transport.PropertyListTransportBean;
import java.io.Serializable;
import sapphire.util.ActionBlock;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ActionBlockTransportBean
implements Serializable {
    private PropertyListCollectionTransportBean commands = new PropertyListCollectionTransportBean();
    private PropertyListTransportBean blockproperties = new PropertyListTransportBean();
    private PropertyListTransportBean returnproperties = new PropertyListTransportBean();
    private int errorAction = -1;
    private String test;

    public ActionBlockTransportBean() {
    }

    public ActionBlockTransportBean(ActionBlock ab) {
        this.setActionBlock(ab);
    }

    public ActionBlock toActionBlock() {
        return this.getActionBlock();
    }

    public void setTest(String test) {
        this.test = test;
    }

    public String getTest() {
        return this.test;
    }

    public void setErrorAction(int errorAction) {
        this.errorAction = errorAction;
    }

    public int getErrorAction() {
        return this.errorAction;
    }

    public PropertyListCollectionTransportBean getCommands() {
        return this.commands;
    }

    public void setCommands(PropertyListCollectionTransportBean commands) {
        this.commands = commands;
    }

    public PropertyListTransportBean getBlockProperties() {
        return this.blockproperties;
    }

    public void setBlockProperties(PropertyListTransportBean blockProperties) {
        this.blockproperties = blockProperties;
    }

    public PropertyListTransportBean getReturnProperties() {
        return this.returnproperties;
    }

    public void setReturnProperties(PropertyListTransportBean returnProperties) {
        this.returnproperties = returnProperties;
    }

    protected void setActionBlock(ActionBlock actionBlock) {
        if (actionBlock != null) {
            PropertyListCollection coms = new PropertyListCollection();
            for (int i = 0; i < actionBlock.getActionCount(); ++i) {
                try {
                    PropertyList list = new PropertyList();
                    list.setProperty("actionClass", actionBlock.getActionClass(i));
                    list.setProperty("actionid", actionBlock.getActionid(i));
                    list.setProperty("name", actionBlock.getActionName(i));
                    list.setProperty("versionid", actionBlock.getVersionid(i));
                    list.setProperty("properties", new PropertyList(actionBlock.getActionProperties(i)));
                    list.setProperty("test", actionBlock.getActionTest(i));
                    coms.add(list);
                    continue;
                }
                catch (Exception list) {
                    // empty catch block
                }
            }
            this.commands.setPropertyListCollection(coms);
            PropertyList bp = new PropertyList(actionBlock.getBlockProperties());
            this.blockproperties.setPropertyList(bp);
            PropertyList rp = new PropertyList(actionBlock.getReturnProperties());
            this.returnproperties.setPropertyList(rp);
            this.errorAction = actionBlock.getErrorAction();
            this.test = actionBlock.getTest();
        }
    }

    protected ActionBlock getActionBlock() {
        ActionBlock out = new ActionBlock();
        PropertyListCollection actions = this.commands.getPropertyListCollection();
        for (int i = 0; i < actions.size(); ++i) {
            PropertyList action = actions.getPropertyList(i);
            try {
                if (action.getProperty("name", "").length() <= 0) continue;
                if (action.getProperty("actionid", "").length() > 0) {
                    out.setAction(action.getProperty("name"), action.getProperty("test"), "", action.getProperty("actionid"), action.getProperty("versionid", "1"), action.getPropertyList("properties"));
                    continue;
                }
                if (action.getProperty("actionClass", "").length() <= 0) continue;
                out.setAction(action.getProperty("name"), action.getProperty("test"), action.getProperty("actionClass"), "", "", action.getPropertyList("properties"));
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        out.setBlockProperties(this.blockproperties.getPropertyList());
        out.setReturnProperties(this.returnproperties.getPropertyList());
        out.setTest(this.test);
        out.setErrorAction(this.errorAction);
        return out;
    }
}

