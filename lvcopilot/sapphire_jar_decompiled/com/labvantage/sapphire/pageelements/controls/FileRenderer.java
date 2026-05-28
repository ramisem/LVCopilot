/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.controls;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import sapphire.pageelements.BaseElement;

public class FileRenderer
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        try {
            String line;
            BufferedReader reader;
            if ("file system".equals(this.element.getProperty("filereference").toLowerCase())) {
                FileReader file = new FileReader(this.element.getProperty("absfileref"));
                reader = new BufferedReader(file);
            } else {
                URL url = this.pageContext.getServletContext().getResource("/" + this.element.getProperty("webfileref"));
                reader = new BufferedReader(new InputStreamReader(url.openStream()));
            }
            while ((line = reader.readLine()) != null) {
                html.append(line);
            }
            reader.close();
        }
        catch (Exception e) {
            this.logger.error("Failed to load and render html file", e);
            html.append("Failed to load and render html file: ").append(e.getMessage());
        }
        return html.toString();
    }
}

