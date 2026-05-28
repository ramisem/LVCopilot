/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array.transfer;

import com.labvantage.sapphire.array.transfer.BaseSubOperation;
import com.labvantage.sapphire.array.transfer.CondenseInterspersedSubOp;
import com.labvantage.sapphire.array.transfer.CondenseSrcColsSubOp;
import com.labvantage.sapphire.array.transfer.CondenseSrcRowsSubOp;
import com.labvantage.sapphire.array.transfer.CondenseWholeArraySubOp;
import com.labvantage.sapphire.array.transfer.SplitInterspersedSubOp;
import com.labvantage.sapphire.array.transfer.SplitWholeArraySubOp;
import com.labvantage.sapphire.array.transfer.StampWholeArraySubOp;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;

public class TransferOperationUtil {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    static List<String> operationsList = null;
    static List<String> subOperationsCondenseList = null;
    static List<String> subOperationsSplitList = null;
    static List<String> subOperationsReplicationList = null;

    public List<String> getAllOperations() {
        return operationsList;
    }

    public List<String> getAllSubOperations(String operation) {
        List<Object> subOperationsList = new ArrayList();
        if (operation.equalsIgnoreCase("Condensing")) {
            subOperationsList = subOperationsCondenseList;
        } else if (operation.equalsIgnoreCase("Splitting")) {
            subOperationsList = subOperationsSplitList;
        } else if (operation.equalsIgnoreCase("Replication")) {
            subOperationsList = subOperationsReplicationList;
        }
        return subOperationsList;
    }

    /*
     * WARNING - void declaration
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public BaseSubOperation getSubOperation(String operation, String subOperationType) throws SapphireException {
        void var3_11;
        Object var3_3 = null;
        if (operation.equalsIgnoreCase("Condensing")) {
            if (subOperationType.equalsIgnoreCase("WholeArray")) {
                CondenseWholeArraySubOp condenseWholeArraySubOp = new CondenseWholeArraySubOp();
                return var3_11;
            } else if (subOperationType.equalsIgnoreCase("Interspersed")) {
                CondenseInterspersedSubOp condenseInterspersedSubOp = new CondenseInterspersedSubOp();
                return var3_11;
            } else if (subOperationType.equalsIgnoreCase("CondenseSourceRows")) {
                CondenseSrcRowsSubOp condenseSrcRowsSubOp = new CondenseSrcRowsSubOp();
                return var3_11;
            } else {
                if (!subOperationType.equalsIgnoreCase("CondenseSourceColumns")) throw new SapphireException("SubOperation type not supported. For Condensing operation, the suboperation must be either WholeArray, Interspersed, CondenseSourceRows or CondenseSourceColumns");
                CondenseSrcColsSubOp condenseSrcColsSubOp = new CondenseSrcColsSubOp();
            }
            return var3_11;
        } else if (operation.equalsIgnoreCase("Splitting")) {
            if (subOperationType.equalsIgnoreCase("WholeArray")) {
                SplitWholeArraySubOp splitWholeArraySubOp = new SplitWholeArraySubOp();
                return var3_11;
            } else {
                if (!subOperationType.equalsIgnoreCase("Interspersed")) throw new SapphireException("SubOperation type not supported. For Splitting operation, the suboperation must be either WholeArray or Interspersed");
                SplitInterspersedSubOp splitInterspersedSubOp = new SplitInterspersedSubOp();
            }
            return var3_11;
        } else {
            if (!operation.equalsIgnoreCase("Replication")) return var3_11;
            if (!subOperationType.equalsIgnoreCase("Stamping")) throw new SapphireException("SubOperation type not supported. For Replication operation, the suboperation must be Stamping");
            StampWholeArraySubOp stampWholeArraySubOp = new StampWholeArraySubOp();
        }
        return var3_11;
    }

    static {
        operationsList = new ArrayList<String>();
        operationsList.add("Condensing");
        operationsList.add("Splitting");
        operationsList.add("Replication");
        operationsList.add("Custom");
        subOperationsCondenseList = new ArrayList<String>();
        subOperationsCondenseList.add("WholeArray");
        subOperationsCondenseList.add("Interspersed");
        subOperationsCondenseList.add("CondenseSourceRows");
        subOperationsCondenseList.add("CondenseSourceColumns");
        subOperationsSplitList = new ArrayList<String>();
        subOperationsSplitList.add("WholeArray");
        subOperationsSplitList.add("Interspersed");
        subOperationsReplicationList = new ArrayList<String>();
        subOperationsReplicationList.add("Stamping");
    }
}

