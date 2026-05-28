/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array;

import com.labvantage.sapphire.array.EvaluateArrayLayoutRules;

public class Priority {
    private EvaluateArrayLayoutRules.PriorityDimensions[] horizontalP = null;
    private EvaluateArrayLayoutRules.PriorityDimensions[] verticalP = null;

    public void setPriority(EvaluateArrayLayoutRules.PriorityDimensions[] hP, EvaluateArrayLayoutRules.PriorityDimensions[] vP) {
        int counter;
        if (hP == null || hP.length <= 0 || vP == null || vP.length <= 0 || hP.length + vP.length != 4) {
            // empty if block
        }
        if (hP != null && hP.length > 0) {
            this.horizontalP = new EvaluateArrayLayoutRules.PriorityDimensions[hP.length];
            counter = 0;
            for (EvaluateArrayLayoutRules.PriorityDimensions p : hP) {
                this.horizontalP[counter++] = p;
            }
        }
        if (vP != null && vP.length > 0) {
            this.verticalP = new EvaluateArrayLayoutRules.PriorityDimensions[vP.length];
            counter = 0;
            for (EvaluateArrayLayoutRules.PriorityDimensions p : vP) {
                this.verticalP[counter++] = p;
            }
        }
    }

    public EvaluateArrayLayoutRules.PriorityDimensions[] getVerticalPriority() {
        return this.verticalP;
    }

    public EvaluateArrayLayoutRules.PriorityDimensions[] getHorizontalPriority() {
        return this.horizontalP;
    }
}

