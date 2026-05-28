/*
 * Decompiled with CFR 0.152.
 */
package sapphire.util;

import com.labvantage.sapphire.gwt.shared.JSONable;
import java.io.Serializable;
import sapphire.util.SDIList;

public class SDIRequest
extends com.labvantage.sapphire.gwt.shared.util.SDIRequest
implements Serializable,
JSONable {
    public void setSDIList(SDIList sdiList) {
        this.setSDIList(sdiList.getSdcid(), sdiList.getSDIList(SDIList.KeyId.KEYID1), sdiList.getSDIList(SDIList.KeyId.KEYID2), sdiList.getSDIList(SDIList.KeyId.KEYID3));
    }

    public SDIRequest[] getSDIRequests() {
        return this._sdirequests.toArray(new SDIRequest[this._sdirequests.size()]);
    }

    public SDIRequest getSDIRequest(String sdiRequestName) {
        SDIRequest[] sdiRequests;
        if (sdiRequestName.length() == 0) {
            return this;
        }
        for (SDIRequest sdiRequestCurrent : sdiRequests = this.getSDIRequests()) {
            if (!sdiRequestCurrent._requestid.equals(sdiRequestName)) continue;
            return sdiRequestCurrent;
        }
        return null;
    }

    public SDIRequest findSDIRequest(String sdcid) {
        SDIRequest[] sdiRequests;
        if (sdcid.length() == 0 || this.getSDCid().equalsIgnoreCase(sdcid)) {
            return this;
        }
        for (SDIRequest sdiRequestCurrent : sdiRequests = this.getSDIRequests()) {
            if (sdiRequestCurrent.getSDCid().equalsIgnoreCase(sdcid)) {
                return sdiRequestCurrent;
            }
            SDIRequest other = sdiRequestCurrent.findSDIRequest(sdcid);
            if (other == null) continue;
            return other;
        }
        return null;
    }

    @Override
    public String toJSONString() {
        return null;
    }
}

