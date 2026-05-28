/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.console.diagnostics;

public class Property {
    private final String id;
    private final String title;
    private final String help;

    public Property(String id, String title, String help) {
        this.id = id;
        this.title = title;
        this.help = help;
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getHelp() {
        return this.help;
    }
}

