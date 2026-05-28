/*
 * Decompiled with CFR 0.152.
 */
package sapphire.error;

import java.io.Serializable;

public class ErrorDetail
extends com.labvantage.sapphire.gwt.shared.error.ErrorDetail
implements Serializable {
    ErrorDetail(String sdcid, String event, String errorid, String errorType, String message) {
        super(sdcid, event, errorid, errorType, message);
    }
}

