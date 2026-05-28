/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.groovy;

import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.util.SDIList;
import sapphire.util.StringUtil;

public class RenderUtil {
    public static final int RENDER_HTML = 0;
    private int renderOption = 0;

    public RenderUtil(SapphireConnection sapphireConnection, int renderOption) {
        this.renderOption = renderOption;
    }

    public String toText(SDIList sdiList) {
        return sdiList.toText();
    }

    public String toTable(SDIList sdiList) {
        StringBuffer html = new StringBuffer("<table border=\"0\" cellspacing=\"0\" cellpadding\"0\">");
        for (int i = 0; i < sdiList.size(); ++i) {
            html.append("<tr><td>").append(sdiList.getKeyid1(i)).append("</td>");
            String keyid2 = sdiList.getKeyid2(i);
            if (!keyid2.equals("(null)")) {
                html.append("<td>").append(keyid2).append("</td>");
                String keyid3 = sdiList.getKeyid3(i);
                if (!keyid3.equals("(null)")) {
                    html.append("<td>").append(keyid3).append("</td>");
                }
            }
            html.append("</tr>");
        }
        return html.append("</table>").toString();
    }

    public String toList(SDIList sdiList) {
        String[] list = new String[sdiList.size()];
        for (int i = 0; i < sdiList.size(); ++i) {
            list[i] = sdiList.getKeyid(i);
        }
        return this.toList(list, false);
    }

    public String toList(String stringList) {
        return this.toList(stringList, ";");
    }

    public String toList(String stringList, String delimeter) {
        String[] list = StringUtil.split(stringList, delimeter);
        return this.toList(list, false);
    }

    private String toList(String[] list, boolean ordered) {
        StringBuffer html = new StringBuffer(ordered ? "<ol>" : "<ul>");
        for (int i = 0; i < list.length; ++i) {
            html.append("<li>").append(list[i]).append("</li>");
        }
        return html.append(ordered ? "</ol>" : "</ul>").toString();
    }
}

