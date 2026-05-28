/*
 * Decompiled with CFR 0.152.
 */
package sapphire.tagext;

import com.labvantage.sapphire.BaseClass;
import java.util.ArrayList;

public class BaseTagInfo
extends BaseClass {
    private StringBuffer errorcodes = new StringBuffer();
    private ArrayList errorstack = new ArrayList();

    public void setErrorStack(String errorcodes, ArrayList errorstack) {
        this.errorcodes.append(";" + errorcodes);
        this.errorstack.addAll(errorstack);
    }

    public String getErrorStack(String endlinestring) {
        StringBuffer value = new StringBuffer();
        int errors = this.errorstack.size();
        for (int i = errors - 1; i >= 0; --i) {
            value.append((String)this.errorstack.get(i) + endlinestring);
        }
        return value.toString();
    }

    public String getErrorCodes() {
        String value = "";
        if (this.errorcodes.length() > 0) {
            value = this.errorcodes.substring(1);
        }
        return value;
    }

    public String getLastError() {
        int errors = this.errorstack.size() - 1;
        return (String)this.errorstack.get(errors);
    }
}

