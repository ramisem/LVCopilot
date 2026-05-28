/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.coc;

import com.labvantage.opal.elements.coc.SDICOCItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDICOC {
    static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private List __ItemList = new ArrayList();
    private List __ItemNameList = new ArrayList();
    private Set __CustodianSet = new HashSet();
    private HashMap __CustodianPwdMap;
    private String __SysUserID;
    private PropertyList __Element;

    public void add(String keyid1) {
        this.__ItemList.add(new SDICOCItem(keyid1));
        this.__ItemNameList.add(keyid1);
    }

    public boolean remove(String keyid1) {
        int index = this.__ItemNameList.indexOf(keyid1);
        if (index != -1) {
            this.__ItemList.remove(index);
            this.__ItemNameList.remove(index);
            return true;
        }
        return false;
    }

    public int size() {
        return this.__ItemList.size();
    }

    public SDICOCItem get(String keyid1) {
        int index = this.__ItemNameList.indexOf(keyid1);
        if (index != -1) {
            return (SDICOCItem)this.__ItemList.get(index);
        }
        return null;
    }

    public List getItems(String custodianid) {
        ArrayList<SDICOCItem> list = new ArrayList<SDICOCItem>();
        for (int i = 0; i < this.__ItemList.size(); ++i) {
            SDICOCItem item = (SDICOCItem)this.__ItemList.get(i);
            String currentcustodian = item.getCurrentCustodian();
            if (currentcustodian == null || !currentcustodian.equals(custodianid)) continue;
            list.add(item);
        }
        return list;
    }

    public List getPendingItems() {
        ArrayList<SDICOCItem> list = new ArrayList<SDICOCItem>();
        for (int i = 0; i < this.__ItemList.size(); ++i) {
            SDICOCItem item = (SDICOCItem)this.__ItemList.get(i);
            if (item.getCurrentCustodian() != null) continue;
            list.add(item);
        }
        return list;
    }

    public boolean setCocStarted(String keyid1, boolean cocStarted) {
        int index = this.__ItemNameList.indexOf(keyid1);
        if (index != -1) {
            ((SDICOCItem)this.__ItemList.get(index)).setCocStarted(cocStarted);
            return true;
        }
        return false;
    }

    public boolean setCurrentCustodian(String keyid1, String custodianid) {
        int index = this.__ItemNameList.indexOf(keyid1);
        if (index != -1) {
            ((SDICOCItem)this.__ItemList.get(index)).setCurrentCustodian(custodianid);
            this.__CustodianSet.add(custodianid);
            return true;
        }
        return false;
    }

    public boolean setLastCustodian(String keyid1, String custodianid) {
        int index = this.__ItemNameList.indexOf(keyid1);
        if (index != -1) {
            ((SDICOCItem)this.__ItemList.get(index)).setLastCustodian(custodianid);
            return true;
        }
        return false;
    }

    public boolean setControlsubstance(String keyid1) {
        int index = this.__ItemNameList.indexOf(keyid1);
        if (index != -1) {
            ((SDICOCItem)this.__ItemList.get(index)).setControlsubstance(true);
            return true;
        }
        return false;
    }

    public Set getCustodianSet() {
        return this.__CustodianSet;
    }

    public int getUniqueCustodianCount() {
        return this.__CustodianSet.size();
    }

    public boolean isAnyItemPending() {
        for (int i = 0; i < this.__ItemList.size(); ++i) {
            SDICOCItem item = (SDICOCItem)this.__ItemList.get(i);
            if (item.getCurrentCustodian() != null) continue;
            return true;
        }
        return false;
    }

    public boolean contains(String keyid1) {
        for (int i = 0; i < this.__ItemList.size(); ++i) {
            SDICOCItem item = (SDICOCItem)this.__ItemList.get(i);
            if (!item.getKeyid1().equals(keyid1)) continue;
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName() + '@' + Integer.toHexString(this.hashCode()));
        sb.append("Item Count: " + this.size());
        for (int i = 0; i < this.size(); ++i) {
            SDICOCItem item = (SDICOCItem)this.__ItemList.get(i);
            sb.append("Item : " + i);
            sb.append(" Keyid1: " + item.getKeyid1());
            sb.append(" COC Started: " + (item.isCocStarted() ? "Yes" : "No"));
            sb.append(" Current Custodian: " + (item.getCurrentCustodian() == null ? "-None-" : item.getCurrentCustodian()));
        }
        return sb.toString();
    }

    public String getJSArray() {
        StringBuffer sb = new StringBuffer();
        sb.append("var _groupbarvisible = true;");
        sb.append("var _fromcustodianarray = new Array();");
        sb.append("var _dataarray = new Array();");
        sb.append("var _custodianpwdmap = new Array();");
        sb.append("var _tocustodianid = '';");
        sb.append("var _tocustodianpassword = '';");
        sb.append("var _witnessrequired = false;");
        sb.append("var _witnessid = '';");
        sb.append("var _witnesspassword = '';");
        sb.append("var _sysuserid = '" + this.__SysUserID + "';");
        PropertyListCollection col = this.__Element.getPropertyList("data").getPropertyList("body").getCollection("columns");
        Iterator<Object> iterator = this.__CustodianSet.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            String fromcustodianid = (String)iterator.next();
            List list = this.getItems(fromcustodianid);
            sb.append("_dataarray[" + index + "] = [");
            for (int i = 0; i < list.size(); ++i) {
                SDICOCItem item = (SDICOCItem)list.get(i);
                sb.append("[");
                for (int j = 0; j < col.size(); ++j) {
                    PropertyList columns = col.getPropertyList(j);
                    if (columns.getProperty("column").equals("keyid1")) {
                        sb.append("'" + item.getKeyid1() + "',");
                        continue;
                    }
                    sb.append("'',");
                }
                if (col.size() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append("],");
            }
            if (list.size() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("];");
            sb.append("_fromcustodianarray[" + index + "] = ['" + fromcustodianid + "', ''];");
            ++index;
        }
        if (this.isWitnessrequired()) {
            sb.append("_witnessrequired = true;");
        }
        if (this.isAnyItemPending()) {
            sb.append("_fromcustodianarray[" + index + "] = ['Not Started',''];");
            List list = this.getPendingItems();
            sb.append("_dataarray[" + index + "] = [");
            for (int i = 0; i < list.size(); ++i) {
                SDICOCItem item = (SDICOCItem)list.get(i);
                sb.append("[");
                for (int j = 0; j < col.size(); ++j) {
                    PropertyList columns = col.getPropertyList(j);
                    if (columns.getProperty("column").equals("keyid1")) {
                        sb.append("'" + item.getKeyid1() + "',");
                        continue;
                    }
                    sb.append("'',");
                }
                if (col.size() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append("],");
            }
            if (list.size() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("];");
        }
        Set set = this.__CustodianPwdMap.keySet();
        iterator = set.iterator();
        index = 0;
        while (iterator.hasNext()) {
            String id = (String)iterator.next();
            String flag = (String)this.__CustodianPwdMap.get(id);
            sb.append("_custodianpwdmap[" + index++ + "] = ['" + id + "','" + flag + "'];");
        }
        return sb.toString();
    }

    public void setCustodianPwdMap(HashMap map) {
        this.__CustodianPwdMap = map;
    }

    public String getSysuserid() {
        return this.__SysUserID;
    }

    public void setSysuserid(String sysuserid) {
        this.__SysUserID = sysuserid;
    }

    public void setElement(PropertyList element) {
        this.__Element = element;
    }

    public boolean isWitnessrequired() {
        for (int i = 0; i < this.__ItemList.size(); ++i) {
            if (!((SDICOCItem)this.__ItemList.get(i)).isControlsubstance()) continue;
            return true;
        }
        return false;
    }

    public boolean hasMultipleCustodians() {
        if (this.isAnyItemPending()) {
            return this.getUniqueCustodianCount() > 0;
        }
        return this.getUniqueCustodianCount() > 1;
    }
}

