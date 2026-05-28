/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 */
package sapphire.ext;

import javax.servlet.http.HttpServletRequest;
import sapphire.util.DataSet;

public interface LogonRequestValidator {
    public boolean isRequireSysuserInfo();

    public String validateRequest(HttpServletRequest var1);

    public String validateRequest(HttpServletRequest var1, DataSet var2);
}

