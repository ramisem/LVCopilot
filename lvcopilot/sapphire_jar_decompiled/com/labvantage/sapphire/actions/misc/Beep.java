/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.misc;

import java.util.Random;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class Beep
extends BaseAction
implements sapphire.action.Beep {
    @Override
    public boolean isDatabaseRequired() {
        return false;
    }

    @Override
    public void processAction(PropertyList properties) {
        this.logger.info(properties.getProperty("beepmessage", "BEEP!"));
        if (properties.getProperty("beeplength").length() > 0) {
            try {
                Thread.sleep(Integer.parseInt(properties.getProperty("beeplength")));
            }
            catch (Exception exception) {}
        } else if (properties.getProperty("randombeepmin").length() > 0 && properties.getProperty("randombeepmax").length() > 0) {
            try {
                Random r = new Random();
                int low = Integer.parseInt(properties.getProperty("randombeepmin"));
                int high = Integer.parseInt(properties.getProperty("randombeepmax"));
                int duration = r.nextInt(high - low) + low;
                Thread.sleep(duration);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }
}

