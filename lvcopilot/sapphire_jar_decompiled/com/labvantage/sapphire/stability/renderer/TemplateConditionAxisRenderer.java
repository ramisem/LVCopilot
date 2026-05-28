/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability.renderer;

import com.labvantage.sapphire.stability.BaseAxis;
import com.labvantage.sapphire.stability.renderer.AxisRenderer;
import com.labvantage.sapphire.stability.renderer.ConditionAxisRenderer;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.SafeHTML;

public class TemplateConditionAxisRenderer
extends ConditionAxisRenderer
implements AxisRenderer {
    @Override
    public String getTitleHTML(BaseAxis axis, int row) throws SapphireException {
        TranslationProcessor tp = new TranslationProcessor(axis.connectionInfo.getConnectionId());
        StringBuffer output = new StringBuffer();
        output.append(this.getSimpleLabel(axis, row));
        String templateDesc = axis.items.getValue(row, "templatedesc");
        String includetimerulesflag = axis.items.getValue(row, "includetimerulesflag");
        output.append(templateDesc.length() == 0 ? "<span style=\"color:red\">" + tp.translate("No description set") + "</span>" : SafeHTML.encodeForHTMLAttribute(templateDesc)).append("</br>");
        output.append(tp.translate(includetimerulesflag.equals("Y") ? "Add all timerules" : "Add used timerules"));
        output.append("<br>");
        output.append(this.getDetails(axis, row));
        return output.toString();
    }
}

