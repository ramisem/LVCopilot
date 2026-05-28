/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.test.sec;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class MaterialUpdate
extends BaseAction {
    public static final String PROPERTY_MATERIALID = "materialid";
    public static final String PROPERTY_MATERIALDESC = "materialdesc";
    public static final String PROPERTY_MATERIALGROUP = "materialgroup";
    public static final String PROPERTY_PURCHGROUP = "purchgroup";
    public static final String PROPERTY_PLANT = "plant";
    public static final String PROPERTY_UNIT = "unit";

    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String materialId = propertyList.getProperty(PROPERTY_MATERIALID);
        String materialDesc = propertyList.getProperty(PROPERTY_MATERIALDESC, "");
        String materialGroup = propertyList.getProperty(PROPERTY_MATERIALGROUP, "");
        String plant = propertyList.getProperty(PROPERTY_PLANT, "");
        String purchGroup = propertyList.getProperty(PROPERTY_PURCHGROUP, "");
        String unit = propertyList.getProperty(PROPERTY_UNIT, "");
        PropertyList addProps = new PropertyList();
        addProps.setProperty("sdcid", "Material");
        addProps.setProperty("keyid1", materialId);
        if (materialDesc.length() != 0) {
            if ("NULL".equals(materialDesc)) {
                materialDesc = "";
            }
            addProps.setProperty(PROPERTY_MATERIALDESC, materialDesc);
        }
        if (materialGroup.length() != 0) {
            if ("NULL".equals(materialGroup)) {
                materialGroup = "";
            }
            addProps.setProperty("u_matgroup", materialGroup);
        }
        if (plant.length() != 0) {
            if ("NULL".equals(plant)) {
                plant = "";
            }
            addProps.setProperty("u_plant", plant);
        }
        if (purchGroup.length() != 0) {
            if ("NULL".equals(purchGroup)) {
                purchGroup = "";
            }
            addProps.setProperty("u_purchgroup", purchGroup);
        }
        if (unit.length() != 0) {
            if ("NULL".equals(unit)) {
                unit = "";
            }
            addProps.setProperty("u_unit", unit);
        }
        this.getActionProcessor().processAction("EditSDI", "1", addProps);
    }
}

