/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.storage;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.error.ErrorHandler;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EditTrackItem
extends BaseAction
implements sapphire.action.EditTrackItem {
    public static String LABVANTAGE_CVS_ID = "$Revision: 90483 $";
    public static final String ID = "EditTrackItem";
    public static final String VERSION = "1";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String trackitemid;
        if (actionProps.getProperty("tracelogid", "").length() == 0 && actionProps.getProperty("auditreason", "").length() == 0) {
            actionProps.setProperty("auditreason", "DataEdit");
        }
        if ((trackitemid = actionProps.getProperty("trackitemid").trim()).length() == 0) {
            ArrayList<String> trackitemList = new ArrayList<String>();
            DataSet addTrackItemDS = new DataSet();
            String sdcid = actionProps.getProperty("sdcid").trim();
            String keyid1 = actionProps.getProperty("keyid1", "").trim();
            String keyid2 = actionProps.getProperty("keyid2", "").trim();
            String keyid3 = actionProps.getProperty("keyid3", "").trim();
            if (sdcid.trim().length() > 0 && keyid1.trim().length() > 0) {
                StringBuilder sql = new StringBuilder();
                String[] key1array = StringUtil.split(keyid1, ";");
                String[] key2array = StringUtil.split(keyid2, ";");
                String[] key3array = StringUtil.split(keyid3, ";");
                boolean hasSecondKey = OpalUtil.isNotEmpty(this.getSDCProcessor().getProperty(sdcid, "keycolid2"));
                boolean hasThirdKey = OpalUtil.isNotEmpty(this.getSDCProcessor().getProperty(sdcid, "keycolid3"));
                if (hasSecondKey) {
                    if (key2array.length != key1array.length) {
                        throw new SapphireException("VALIDATION", this.getTranslationProcessor().translate("Invalid action input"), this.getTranslationProcessor().translate("Input action property do not match") + ": KEYID1-KEYID2");
                    }
                    if (hasThirdKey && key3array.length != key1array.length) {
                        throw new SapphireException("VALIDATION", this.getTranslationProcessor().translate("Invalid action input"), this.getTranslationProcessor().translate("Input action property do not match") + ": KEYID1-KEYID2-KEYID3");
                    }
                }
                String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
                try {
                    DataSet ds;
                    sql.append("select trackitem.trackitemid, trackitem.linksdcid, trackitem.linkkeyid1, trackitem.linkkeyid2, trackitem.linkkeyid3");
                    sql.append(" from trackitem, rsetitems");
                    sql.append(" where rsetitems.rsetid = ?");
                    sql.append(" and trackitem.linksdcid = rsetitems.sdcid");
                    sql.append(" and trackitem.linkkeyid1 = rsetitems.keyid1");
                    if (hasSecondKey) {
                        sql.append(" and trackitem.linkkeyid2 = rsetitems.keyid2");
                        if (hasThirdKey) {
                            sql.append(" and trackitem.linkkeyid3 = rsetitems.keyid3");
                        }
                    }
                    if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid})) != null) {
                        HashMap<String, Object> filter = new HashMap<String, Object>();
                        for (int i = 0; i < key1array.length; ++i) {
                            if (OpalUtil.isEmpty(key1array[i])) {
                                throw new SapphireException(this.getTranslationProcessor().translate("Invalid Action Input"), "VALIDATION", this.getTranslationProcessor().translate("Input key is empty"));
                            }
                            filter.clear();
                            filter.put("linksdcid", sdcid);
                            filter.put("linkkeyid1", key1array[i]);
                            if (hasSecondKey) {
                                if (OpalUtil.isEmpty(key2array[i])) {
                                    throw new SapphireException(this.getTranslationProcessor().translate("Invalid Action Input"), "VALIDATION", this.getTranslationProcessor().translate("Input key 2 is empty"));
                                }
                                filter.put("linkkeyid2", key2array[i]);
                                if (hasThirdKey) {
                                    if (OpalUtil.isEmpty(key3array[i])) {
                                        throw new SapphireException(this.getTranslationProcessor().translate("Invalid Action Input"), "VALIDATION", this.getTranslationProcessor().translate("Input key 3 is empty"));
                                    }
                                    filter.put("linkkeyid3", key3array[i]);
                                }
                            }
                            if (ds.getFilteredDataSet(filter).size() > 1) {
                                if (key1array.length > 1) {
                                    throw new SapphireException("VALIDATION", this.getTranslationProcessor().translate("Error editing Trackitem"), this.getTranslationProcessor().translate("Only one SDI's trackitems can be edited if SDI has multiple trackitems"));
                                }
                                trackitemList.addAll(OpalUtil.toList(ds.getColumnValues("trackitemid", ";"), ";"));
                                continue;
                            }
                            int trackitemrow = ds.findRow(filter);
                            if (trackitemrow == -1) {
                                int row = addTrackItemDS.addRow();
                                addTrackItemDS.setString(row, "sdcid", sdcid);
                                addTrackItemDS.setString(row, "linkkeyid1", key1array[i]);
                                if (!hasSecondKey) continue;
                                addTrackItemDS.setString(row, "linkkeyid2", key2array[i]);
                                if (!hasThirdKey) continue;
                                addTrackItemDS.setString(row, "linkkeyid3", key3array[i]);
                                continue;
                            }
                            trackitemList.add(ds.getString(trackitemrow, "trackitemid"));
                        }
                        if (addTrackItemDS.size() > 0) {
                            trackitemList.clear();
                            PropertyList props = new PropertyList();
                            props.setProperty("sdcid", "TrackItemSDC");
                            props.setProperty("copies", String.valueOf(addTrackItemDS.size()));
                            props.setProperty("linksdcid", sdcid);
                            props.setProperty("linkkeyid1", addTrackItemDS.getColumnValues("linkkeyid1", ";"));
                            if (hasSecondKey) {
                                props.setProperty("linkkeyid2", addTrackItemDS.getColumnValues("linkkeyid2", ";"));
                                if (hasThirdKey) {
                                    props.setProperty("linkkeyid3", addTrackItemDS.getColumnValues("linkkeyid3", ";"));
                                }
                            }
                            this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                            DataSet ds2 = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                            for (int i = 0; i < key1array.length; ++i) {
                                int trackitemrow;
                                filter.clear();
                                filter.put("linksdcid", sdcid);
                                filter.put("linkkeyid1", key1array[i]);
                                if (hasSecondKey) {
                                    filter.put("linkkeyid2", key2array[i]);
                                    if (hasThirdKey) {
                                        filter.put("linkkeyid3", key3array[i]);
                                    }
                                }
                                if ((trackitemrow = ds2.findRow(filter)) == -1) {
                                    throw new SapphireException("VALIDATION", this.getTranslationProcessor().translate("Error editing Trackitem"), this.getTranslationProcessor().translate("Failed to add trackitems"));
                                }
                                trackitemList.add(ds2.getString(trackitemrow, "trackitemid", ""));
                            }
                        }
                    }
                }
                finally {
                    if (StringUtil.getLen(rsetid) > 0L) {
                        this.getDAMProcessor().clearRSet(rsetid);
                    }
                }
            }
            trackitemid = OpalUtil.toDelimitedString(trackitemList, ";");
        }
        if (trackitemid.length() > 0) {
            ArrayList<String> filterList = new ArrayList<String>();
            filterList.add("trackitemid");
            filterList.add("sdcid");
            filterList.add("keyid1");
            filterList.add("keyid2");
            filterList.add("keyid3");
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "TrackItemSDC");
            props.put("keyid1", trackitemid);
            for (Object o : actionProps.keySet()) {
                String propertyid = (String)o;
                if (filterList.contains(propertyid)) continue;
                String property = propertyid.toLowerCase();
                String value = actionProps.getProperty(propertyid);
                if ("auditreason".equals(property) && OpalUtil.isNotEmpty(value)) {
                    try {
                        if (value.contains("%")) {
                            value = StringUtil.replaceAll(value, "%", "%25");
                        }
                        value = URLDecoder.decode(value, "UTF-8");
                    }
                    catch (UnsupportedEncodingException unsupportedEncodingException) {
                        // empty catch block
                    }
                }
                props.put(property, value);
            }
            try {
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props, false);
                ErrorHandler errorHandler = this.getActionProcessor().getErrorHandler();
                if (errorHandler != null && errorHandler.hasInfoErrors()) {
                    this.setErrors(this.getActionProcessor().getErrorHandler());
                }
            }
            catch (ActionException e) {
                this.setErrors(e.getErrorHandler());
            }
        }
    }
}

