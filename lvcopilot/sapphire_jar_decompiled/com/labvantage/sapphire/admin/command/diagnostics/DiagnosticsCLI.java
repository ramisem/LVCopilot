/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.command.diagnostics;

import com.labvantage.sapphire.admin.command.BaseCLI;
import com.labvantage.sapphire.admin.command.diagnostics.CheckCode;

public class DiagnosticsCLI
extends BaseCLI {
    static {
        DiagnosticsCLI.addCommand("checkcode", CheckCode.class.getName());
    }
}

