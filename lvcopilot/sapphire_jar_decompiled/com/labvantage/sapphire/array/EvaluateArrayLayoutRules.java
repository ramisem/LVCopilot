/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.array;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.array.WellValues;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.util.StringUtil;

public class EvaluateArrayLayoutRules {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private int _repeatCt;
    private int _treatmentCt;
    private int _sampleBound;
    private int _dilutionStep;
    public EvaluateArrayLayoutRulesHelper _helper = new EvaluateArrayLayoutRulesHelper();
    private int _templateCols = -1;
    private int _templateRows = -1;

    public EvaluateArrayLayoutRules() {
    }

    public EvaluateArrayLayoutRules(int sampleBound, int repeatCt, int dilutions) {
        this._sampleBound = sampleBound;
        this._repeatCt = repeatCt;
        this._dilutionStep = dilutions;
        this._treatmentCt = 1;
    }

    public WellValues[][] plateArray(String alldelimitedAvailableCellsCoordinates, int sampleBound, int repeatCount, int dilutionSteps, float dilutionFactor, boolean diluteFirst, int treatmentCount, String horizontalPriority, String verticalPriority) throws SapphireException {
        int n;
        int n2;
        int n3;
        int n4;
        this._sampleBound = sampleBound;
        this._repeatCt = repeatCount;
        this._dilutionStep = dilutionSteps;
        String[] tempdelimitedCartesianPairArr = alldelimitedAvailableCellsCoordinates.split(";");
        TreeSet<String> distinctCoordinates = new TreeSet<String>();
        for (int i = 0; i < tempdelimitedCartesianPairArr.length; ++i) {
            distinctCoordinates.add(tempdelimitedCartesianPairArr[i]);
        }
        String[] delimitedCartesianPairArr = new String[distinctCoordinates.size()];
        Object[] objArr = distinctCoordinates.toArray();
        for (int i = 0; i < distinctCoordinates.size(); ++i) {
            delimitedCartesianPairArr[i] = (String)objArr[i];
        }
        TreeSet<Integer> rowSet = new TreeSet<Integer>();
        TreeSet<Integer> colSet = new TreeSet<Integer>();
        for (String string : tempdelimitedCartesianPairArr) {
            String[] rowColArr = string.split(",");
            rowSet.add(Integer.valueOf(rowColArr[0]));
            colSet.add(Integer.valueOf(rowColArr[1]));
        }
        int maxRow = (Integer)rowSet.toArray()[rowSet.size() - 1];
        int maxCol = (Integer)colSet.toArray()[colSet.size() - 1];
        int[][] coordinateMap = new int[maxRow + 1][maxCol + 1];
        for (String rowcolpair : delimitedCartesianPairArr) {
            String[] rowColArr = rowcolpair.split(",");
            coordinateMap[Integer.valueOf((String)rowColArr[0]).intValue()][Integer.valueOf((String)rowColArr[1]).intValue()] = 1;
        }
        int n5 = 1;
        if (horizontalPriority.length() > 0 && horizontalPriority.indexOf("C") != -1 || verticalPriority.length() > 0 && verticalPriority.indexOf("C") != -1) {
            n4 = n5 * sampleBound;
        }
        if (horizontalPriority.length() > 0 && horizontalPriority.indexOf("D") != -1 || verticalPriority.length() > 0 && verticalPriority.indexOf("D") != -1) {
            n3 = n4 * dilutionSteps;
        }
        if (horizontalPriority.length() > 0 && horizontalPriority.indexOf("R") != -1 || verticalPriority.length() > 0 && verticalPriority.indexOf("R") != -1) {
            n2 = n3 * repeatCount;
        }
        if (horizontalPriority.length() > 0 && horizontalPriority.indexOf("T") != -1 || verticalPriority.length() > 0 && verticalPriority.indexOf("T") != -1) {
            n = n2 * treatmentCount;
        }
        int numberOfSets = delimitedCartesianPairArr.length / n;
        if (delimitedCartesianPairArr.length % n != 0 && horizontalPriority.length() > 0 && verticalPriority.length() > 0) {
            throw new SapphireException("Invalid properties. Product of dimensions in horizontal & vertical priority should be either equal to/multiple of/factor of number of wells in a zone");
        }
        PriorityDimensions[] horizontalP = this._helper.getPriorityDimensionArray(horizontalPriority);
        PriorityDimensions[] verticalP = this._helper.getPriorityDimensionArray(verticalPriority);
        int sampleStartNum = 1;
        ArrayList<WellValues[][]> sbSetList = new ArrayList<WellValues[][]>();
        for (int i = 0; i < numberOfSets; ++i) {
            WellValues[][] fillTemplate = this.createFillTemplate(horizontalP, verticalP, sampleStartNum, dilutionFactor, diluteFirst);
            if (horizontalP != null && Arrays.asList(horizontalP).contains((Object)PriorityDimensions.SAMPLE)) {
                if (fillTemplate[0].length > colSet.size() && horizontalP.length > 1 && verticalP.length > 1) {
                    throw new SapphireException("Number of columns needed for template are not specified");
                }
            } else if (fillTemplate.length > rowSet.size() && horizontalP.length > 1 && verticalP.length > 1) {
                throw new SapphireException("Number of rows needed for template are not specified");
            }
            sbSetList.add(fillTemplate);
            sampleStartNum += sampleBound;
        }
        int maxAvailRow = rowSet.toArray(new Integer[rowSet.size()])[rowSet.size() - 1];
        int maxAvailCol = colSet.toArray(new Integer[colSet.size()])[colSet.size() - 1];
        WellValues[][] plateWV = new WellValues[maxAvailRow + 1][maxAvailCol + 1];
        int templateRows = ((WellValues[][])sbSetList.get(0)).length;
        int templateCols = ((WellValues[][])sbSetList.get(0))[0].length;
        this._templateRows = templateRows;
        this._templateCols = templateCols;
        if (horizontalP != null && Arrays.asList(horizontalP).contains((Object)PriorityDimensions.SAMPLE)) {
            int[] position = this.getNextPositionHorizontal(coordinateMap, maxRow, maxCol);
            int currentRow = position[0];
            int currentCol = position[1];
            for (int i = 0; i < sbSetList.size(); ++i) {
                WellValues[][] currentSBSet = (WellValues[][])sbSetList.get(i);
                if (templateRows == 1) {
                    int currentTemplateColumn = 0;
                    while (currentTemplateColumn < templateCols) {
                        if (currentCol >= coordinateMap[0].length) {
                            position = this.getNextPositionHorizontal(coordinateMap, maxRow, maxCol);
                            currentRow = position[0];
                            currentCol = position[1];
                        }
                        if (coordinateMap[currentRow][currentCol] == 1) {
                            Trace.logDebug("Assigned position is [" + currentRow + "][" + currentCol + "]:" + currentSBSet[0][currentTemplateColumn].sample);
                            plateWV[currentRow][currentCol] = currentSBSet[0][currentTemplateColumn];
                            coordinateMap[currentRow][currentCol] = 0;
                        } else {
                            Trace.logDebug("Cannot Assigned position for [" + currentRow + "][" + currentCol + "]:" + currentSBSet[0][currentTemplateColumn].sample);
                        }
                        ++currentTemplateColumn;
                        ++currentCol;
                    }
                } else if (templateCols == 1) {
                    int currentTemplateRow = 0;
                    while (currentTemplateRow < templateRows) {
                        if (currentRow >= coordinateMap.length) {
                            position = this.getNextPositionVertical(coordinateMap, maxRow, maxCol);
                            currentRow = position[0];
                            currentCol = position[1];
                        }
                        if (coordinateMap[currentRow][currentCol] == 1) {
                            Trace.logDebug("Assigned position is [" + currentRow + "][" + currentCol + "]:" + currentSBSet[currentTemplateRow][0].sample);
                            plateWV[currentRow][currentCol] = currentSBSet[currentTemplateRow][0];
                            coordinateMap[currentRow][currentCol] = 0;
                        } else {
                            Trace.logDebug("Cannot Assigned position for [" + currentRow + "][" + currentCol + "]:" + currentSBSet[currentTemplateRow][0].sample);
                        }
                        ++currentTemplateRow;
                        ++currentRow;
                    }
                } else {
                    for (int r = 0; r < templateRows; ++r) {
                        for (int c = 0; c < templateCols; ++c) {
                            if (currentRow + r >= coordinateMap.length || currentCol + c >= coordinateMap[0].length) {
                                throw new SapphireException("Specified arrangement rules cannot be applied");
                            }
                            if (coordinateMap[currentRow + r][currentCol + c] != 1) {
                                Trace.logDebug("Cannot Assigned position for [" + currentRow + "][" + currentCol + "]:" + currentSBSet[r][c].sample);
                                throw new SapphireException("Specified arrangement rules cannot be applied");
                            }
                            Trace.logDebug("Assigned position is [" + currentRow + "][" + currentCol + "]:" + currentSBSet[r][c].sample);
                            plateWV[currentRow + r][currentCol + c] = currentSBSet[r][c];
                            coordinateMap[currentRow + r][currentCol + c] = 0;
                        }
                    }
                }
                if (i == sbSetList.size() - 1) continue;
                position = this.getNextPositionHorizontal(coordinateMap, maxRow, maxCol);
                currentRow = position[0];
                currentCol = position[1];
            }
        } else {
            int[] position = this.getNextPositionVertical(coordinateMap, maxRow, maxCol);
            int currentRow = position[0];
            int currentCol = position[1];
            for (int i = 0; i < sbSetList.size(); ++i) {
                WellValues[][] currentSBSet = (WellValues[][])sbSetList.get(i);
                if (templateCols == 1) {
                    int currentTemplateRow = 0;
                    while (currentTemplateRow < templateRows) {
                        if (currentRow >= coordinateMap.length) {
                            position = this.getNextPositionVertical(coordinateMap, maxRow, maxCol);
                            currentRow = position[0];
                            currentCol = position[1];
                        }
                        if (coordinateMap[currentRow][currentCol] == 1) {
                            Trace.logDebug("Assigned position is [" + currentRow + "][" + currentCol + "]:" + currentSBSet[currentTemplateRow][0].sample);
                            plateWV[currentRow][currentCol] = currentSBSet[currentTemplateRow][0];
                            coordinateMap[currentRow][currentCol] = 0;
                        } else {
                            Trace.logDebug("Cannot Assigned position for [" + currentRow + "][" + currentCol + "]:" + currentSBSet[currentTemplateRow][0].sample);
                        }
                        ++currentTemplateRow;
                        ++currentRow;
                    }
                } else {
                    for (int c = 0; c < templateCols; ++c) {
                        for (int r = 0; r < templateRows; ++r) {
                            if (currentRow + r >= coordinateMap.length || currentCol + c >= coordinateMap[0].length) {
                                throw new SapphireException("Specified arrangement rules cannot be applied");
                            }
                            if (coordinateMap[currentRow + r][currentCol + c] != 1) {
                                Trace.logDebug("Cannot Assigned position for [" + currentRow + "][" + currentCol + "]:" + currentSBSet[r][c].sample);
                                throw new SapphireException("Arrangement rules cannot be applied");
                            }
                            Trace.logDebug("Assigned position is [" + currentRow + "][" + currentCol + "]:" + currentSBSet[r][c].sample);
                            plateWV[currentRow + r][currentCol + c] = currentSBSet[r][c];
                            coordinateMap[currentRow + r][currentCol + c] = 0;
                        }
                    }
                }
                if (i == sbSetList.size() - 1) continue;
                position = this.getNextPositionVertical(coordinateMap, maxRow, maxCol);
                currentRow = position[0];
                currentCol = position[1];
            }
        }
        return plateWV;
    }

    private int[] getNextPositionHorizontal(int[][] coordinateMap, int maxRow, int maxCol) throws SapphireException {
        int[] position = new int[2];
        for (int r = 0; r <= maxRow; ++r) {
            for (int c = 0; c <= maxCol; ++c) {
                if (coordinateMap[r][c] != 1) continue;
                position[0] = r;
                position[1] = c;
                return position;
            }
        }
        throw new SapphireException("No position available");
    }

    private int[] getNextPositionVertical(int[][] coordinateMap, int maxRow, int maxCol) throws SapphireException {
        int[] position = new int[2];
        for (int c = 0; c <= maxCol; ++c) {
            for (int r = 0; r <= maxRow; ++r) {
                if (coordinateMap[r][c] != 1) continue;
                position[0] = r;
                position[1] = c;
                return position;
            }
        }
        throw new SapphireException("No position available");
    }

    public WellValues[][] createFillTemplate(PriorityDimensions[] horizontalP, PriorityDimensions[] verticalP, int sampleStartNum, float dilutionFactor, boolean diluteFirst) throws SapphireException {
        int copiesHorizontal = this._helper.getItemCountForDirection(horizontalP);
        int copiesVertical = this._helper.getItemCountForDirection(verticalP);
        WellValues[][] templatePerSample = new WellValues[copiesVertical][copiesHorizontal];
        for (int row = 0; row < copiesVertical; ++row) {
            for (int col = 0; col < copiesHorizontal; ++col) {
                templatePerSample[row][col] = new WellValues();
            }
        }
        if (horizontalP != null && Arrays.asList(horizontalP).contains((Object)PriorityDimensions.SAMPLE)) {
            this.fillTemplateHorizontal(horizontalP, copiesVertical, copiesHorizontal, templatePerSample, sampleStartNum, dilutionFactor, diluteFirst, true);
            if (verticalP != null) {
                this.fillTemplateHorizontal(verticalP, copiesVertical, copiesHorizontal, templatePerSample, sampleStartNum, dilutionFactor, diluteFirst, false);
            }
        } else {
            this.fillTemplateVertical(verticalP, copiesVertical, copiesHorizontal, templatePerSample, sampleStartNum, dilutionFactor, diluteFirst, true);
            if (horizontalP != null) {
                this.fillTemplateVertical(horizontalP, copiesVertical, copiesHorizontal, templatePerSample, sampleStartNum, dilutionFactor, diluteFirst, false);
            }
        }
        return templatePerSample;
    }

    private void fillTemplateHorizontal(PriorityDimensions[] priority, int copiesVertical, int copiesHorizontal, WellValues[][] template, int sampleStartNum, float dilutionFactor, boolean diluteFirst, boolean fillHorizontal) throws SapphireException {
        int paramNumber = 0;
        for (PriorityDimensions params : priority) {
            int i;
            ++paramNumber;
            int dimNoInWell = 1;
            int repeatUntil = 1;
            if (fillHorizontal) {
                for (i = 0; i < paramNumber - 1; ++i) {
                    repeatUntil *= this._helper.getCountForDimension(priority[i]);
                }
            } else {
                repeatUntil = copiesHorizontal;
                for (i = 0; i < paramNumber - 1; ++i) {
                    repeatUntil *= this._helper.getCountForDimension(priority[i]);
                }
            }
            int noOfTimesRepeated = 1;
            for (int row = 0; row < copiesVertical; ++row) {
                for (int col = 0; col < copiesHorizontal; ++col) {
                    if (dimNoInWell > this._helper.getCountForDimension(priority[paramNumber - 1])) {
                        dimNoInWell = 1;
                    }
                    switch (params) {
                        case DILUTION: {
                            float currentDilution = 1.0f;
                            if (diluteFirst) {
                                currentDilution *= dilutionFactor;
                            }
                            for (int dilutionCount = 1; dilutionCount < dimNoInWell; ++dilutionCount) {
                                currentDilution *= dilutionFactor;
                            }
                            template[row][col].dilution = dimNoInWell;
                            template[row][col].dilutionfactor = currentDilution;
                            break;
                        }
                        case REPEAT: {
                            template[row][col].repeat = dimNoInWell;
                            break;
                        }
                        case SAMPLE: {
                            template[row][col].sample = sampleStartNum + (dimNoInWell - 1);
                            break;
                        }
                        case TREATMENT: {
                            template[row][col].treatment = dimNoInWell;
                        }
                    }
                    if (noOfTimesRepeated++ != repeatUntil) continue;
                    ++dimNoInWell;
                    noOfTimesRepeated = 1;
                }
            }
        }
    }

    private void fillTemplateVertical(PriorityDimensions[] priority, int vDim, int hDim, WellValues[][] wv, int sampleStartNum, float dilutionFactor, boolean diluteFirst, boolean fillVertical) throws SapphireException {
        int paramNumber = 0;
        for (PriorityDimensions params : priority) {
            int i;
            ++paramNumber;
            int dimNoInWell = 1;
            int repeatUntil = 1;
            if (fillVertical) {
                for (i = 0; i < paramNumber - 1; ++i) {
                    repeatUntil *= this._helper.getCountForDimension(priority[i]);
                }
            } else {
                repeatUntil = vDim;
                for (i = 0; i < paramNumber - 1; ++i) {
                    repeatUntil *= this._helper.getCountForDimension(priority[i]);
                }
            }
            int noOfTimesRepeated = 1;
            for (int col = 0; col < hDim; ++col) {
                for (int row = 0; row < vDim; ++row) {
                    if (dimNoInWell > this._helper.getCountForDimension(priority[paramNumber - 1])) {
                        dimNoInWell = 1;
                    }
                    switch (params) {
                        case DILUTION: {
                            float currentDilution = 1.0f;
                            if (diluteFirst) {
                                currentDilution *= dilutionFactor;
                            }
                            for (int dilutionCount = 1; dilutionCount < dimNoInWell; ++dilutionCount) {
                                currentDilution *= dilutionFactor;
                            }
                            wv[row][col].dilution = dimNoInWell;
                            wv[row][col].dilutionfactor = currentDilution;
                            break;
                        }
                        case REPEAT: {
                            wv[row][col].repeat = dimNoInWell;
                            break;
                        }
                        case SAMPLE: {
                            wv[row][col].sample = sampleStartNum + (dimNoInWell - 1);
                            break;
                        }
                        case TREATMENT: {
                            wv[row][col].treatment = dimNoInWell;
                        }
                    }
                    if (noOfTimesRepeated++ != repeatUntil) continue;
                    ++dimNoInWell;
                    noOfTimesRepeated = 1;
                }
            }
        }
    }

    public WellValues[][] applyLoadingDirection(String selectedSamples, String startPosition, String delimitedAvailableCellsCoordinates, String loadingDirection, WellValues[][] layoutMap) throws SapphireException {
        if (startPosition == null || startPosition.length() == 0) {
            throw new SapphireException("Start Position not specified.");
        }
        WellValues[][] plateWV = new WellValues[layoutMap.length][layoutMap[0].length];
        int startxPos = Integer.parseInt(startPosition.split(",")[0]);
        int startyPos = Integer.parseInt(startPosition.split(",")[1]);
        plateWV = this.processItemAtPosition(plateWV, startxPos, startyPos, selectedSamples, delimitedAvailableCellsCoordinates, loadingDirection, layoutMap);
        return plateWV;
    }

    public WellValues[][] applyMultipleLoadingDirection(String selectedSamplesList, ArrayList<String> positions, String delimitedAvailableCellsCoordinates, String loadingDirection, WellValues[][] layoutMap) throws SapphireException {
        String[] selectedSample = StringUtil.split(selectedSamplesList, ";");
        WellValues[][] plateWV = new WellValues[layoutMap.length][layoutMap[0].length];
        int startxPos = -1;
        int startyPos = -1;
        for (int startPos = 0; startPos < positions.size(); ++startPos) {
            try {
                String coordinates = positions.get(startPos);
                String[] toks = StringUtil.split(coordinates, ",");
                startxPos = Integer.parseInt(toks[0]);
                startyPos = Integer.parseInt(toks[1]);
            }
            catch (Exception e) {
                new SapphireException("Failed to apply loading rules");
            }
            plateWV = this.processItemAtPosition(plateWV, startxPos, startyPos, selectedSample[startPos], delimitedAvailableCellsCoordinates, loadingDirection, layoutMap);
        }
        return plateWV;
    }

    public WellValues[][] applyLoadingDirection(String selectedSamples, JSONArray selectedCells, String delimitedAvailableCellsCoordinates, String loadingDirection, WellValues[][] layoutMap) throws SapphireException {
        WellValues[][] plateWV = new WellValues[layoutMap.length][layoutMap[0].length];
        int startxPos = -1;
        int startyPos = -1;
        for (int startPos = 0; startPos < selectedCells.length(); ++startPos) {
            try {
                JSONObject coord = selectedCells.getJSONObject(startPos);
                startxPos = Integer.parseInt(coord.get("x").toString());
                startyPos = Integer.parseInt(coord.get("y").toString());
            }
            catch (JSONException e) {
                new SapphireException("Failed to apply loading rules");
            }
            plateWV = this.processItemAtPosition(plateWV, startxPos, startyPos, selectedSamples, delimitedAvailableCellsCoordinates, loadingDirection, layoutMap);
        }
        return plateWV;
    }

    private WellValues[][] processItemAtPosition(WellValues[][] plateWV, int startxPos, int startyPos, String selectedSamples, String delimitedAvailableCellsCoordinates, String loadingDirection, WellValues[][] layoutMap) throws SapphireException {
        String[] commaDelimitedCartesianPairArr = delimitedAvailableCellsCoordinates.split(";");
        String[] selectedSamplesArr = selectedSamples.split(";");
        int selectedSampleIndex = 0;
        if (loadingDirection.length() > 0) {
            int[] xy;
            plateWV = this.loadSiblingArrayItems(startxPos, startyPos, plateWV, layoutMap, selectedSamplesArr, selectedSampleIndex);
            int currentx = startxPos;
            int currenty = startyPos;
            while (selectedSampleIndex < selectedSamplesArr.length && (xy = this.findNextPosition(loadingDirection, currentx, currenty, layoutMap)) != null) {
                int x = xy[0];
                int y = xy[1];
                if (plateWV[x][y] == null && layoutMap[x][y] != null && this.findIndexInArray(commaDelimitedCartesianPairArr, x + "," + y) != -1 && ++selectedSampleIndex < selectedSamplesArr.length) {
                    plateWV = this.loadSiblingArrayItems(x, y, plateWV, layoutMap, selectedSamplesArr, selectedSampleIndex);
                }
                currentx = x;
                currenty = y;
            }
        }
        return plateWV;
    }

    private int findIndexInArray(String[] commaDelimitedCartesianPairArr, String position) {
        int index = -1;
        for (int i = 0; i < commaDelimitedCartesianPairArr.length; ++i) {
            if (!commaDelimitedCartesianPairArr[i].equals(position)) continue;
            index = i;
        }
        return index;
    }

    private boolean checkIfRowIsFilled(WellValues[][] plateWV, int rowIndex) {
        boolean isRowFilled = false;
        for (int i = 0; i < plateWV[0].length; ++i) {
            if (plateWV[rowIndex][i] == null) continue;
            isRowFilled = true;
            break;
        }
        return isRowFilled;
    }

    private boolean checkIfColIsFilled(WellValues[][] plateWV, int colIndex) {
        boolean isColFilled = false;
        for (int i = 0; i < plateWV.length; ++i) {
            if (plateWV[i][colIndex] == null) continue;
            isColFilled = true;
            break;
        }
        return isColFilled;
    }

    private int[] findNextPosition(String loadingdirection, int currentx, int currenty, WellValues[][] layoutMap) throws SapphireException {
        if (loadingdirection.equals("Horizontal")) {
            return this.findNextHorizontalPosition(currentx, currenty, layoutMap);
        }
        if (loadingdirection.equals("Vertical")) {
            return this.findNextVerticalPosition(currentx, currenty, layoutMap);
        }
        if (loadingdirection.equals("HorizontalSnaking")) {
            return this.findNextHorizontalSnakingPosition(currentx, currenty, layoutMap);
        }
        if (loadingdirection.equals("VerticalSnaking")) {
            return this.findNextVerticalSnakingPosition(currentx, currenty, layoutMap);
        }
        throw new SapphireException("Invalid loading direction specified:" + loadingdirection);
    }

    private int[] findNextHorizontalPosition(int currentx, int currenty, WellValues[][] layoutMap) {
        if (layoutMap[currentx][currenty] == null) {
            return null;
        }
        int lastLoadedSample = layoutMap[currentx][currenty].sample;
        if (this._templateCols == -1 || this._templateRows == -1) {
            this._templateCols = 1;
            this._templateRows = 1;
        }
        for (int row = currentx; row < layoutMap.length; ++row) {
            for (int col = 0; col < layoutMap[currentx].length; ++col) {
                if (row == currentx && col < currenty || layoutMap[row][col] == null || layoutMap[row][col].repeat > 1 || layoutMap[row][col].dilution > 1 || layoutMap[row][col].sample == lastLoadedSample) continue;
                return new int[]{row, col};
            }
        }
        return null;
    }

    private int[] findNextVerticalPosition(int currentx, int currenty, WellValues[][] layoutMap) {
        if (layoutMap[currentx][currenty] == null) {
            return null;
        }
        int lastLoadedSample = layoutMap[currentx][currenty].sample;
        if (this._templateCols == -1 || this._templateRows == -1) {
            this._templateCols = 1;
            this._templateRows = 1;
        }
        for (int col = currenty; col < layoutMap[currentx].length; ++col) {
            for (int row = 0; row < layoutMap.length; ++row) {
                if (col == currenty && row < currentx || layoutMap[row][col] == null || layoutMap[row][col].repeat > 1 || layoutMap[row][col].dilution > 1 || layoutMap[row][col].sample == lastLoadedSample) continue;
                return new int[]{row, col};
            }
        }
        return null;
    }

    private int[] findNextHorizontalSnakingPosition(int currentx, int currenty, WellValues[][] layoutMap) throws SapphireException {
        int lastLoadedSample = layoutMap[currentx][currenty].sample;
        if (this._templateCols == -1 || this._templateRows == -1) {
            this._templateCols = 1;
            this._templateRows = 1;
        }
        for (int row = currentx; row < layoutMap.length; row += this._templateRows) {
            int col;
            boolean forwards;
            int currentslice = row / this._templateRows;
            boolean bl = forwards = currentslice % 2 == 0;
            if (forwards) {
                for (col = 0; col < layoutMap[currentx].length; col += this._templateCols) {
                    if (row == currentx && col < currenty || layoutMap[row][col] == null || layoutMap[row][col].sample == lastLoadedSample) continue;
                    return new int[]{row, col};
                }
                continue;
            }
            for (col = layoutMap[currentx].length - 1; col >= 0; col -= this._templateCols) {
                if (row == currentx && col > currenty || layoutMap[row][col] == null || layoutMap[row][col].sample == lastLoadedSample) continue;
                return new int[]{row, col};
            }
        }
        return null;
    }

    private int[] findNextVerticalSnakingPosition(int currentx, int currenty, WellValues[][] layoutMap) throws SapphireException {
        int lastLoadedSample = layoutMap[currentx][currenty].sample;
        if (this._templateCols == -1 || this._templateRows == -1) {
            this._templateCols = 1;
            this._templateRows = 1;
        }
        for (int col = currenty; col < layoutMap[0].length; col += this._templateCols) {
            int row;
            boolean forwards;
            int currentslice = col / this._templateCols;
            boolean bl = forwards = currentslice % 2 == 0;
            if (forwards) {
                for (row = 0; row < layoutMap.length; row += this._templateRows) {
                    if (col == currenty && row < currentx || layoutMap[row][col] == null || layoutMap[row][col].sample == lastLoadedSample) continue;
                    return new int[]{row, col};
                }
                continue;
            }
            for (row = layoutMap.length - 1; row >= 0; row -= this._templateRows) {
                if (col == currenty && row > currentx || layoutMap[row][col] == null || layoutMap[row][col].sample == lastLoadedSample) continue;
                return new int[]{row, col};
            }
        }
        return null;
    }

    private WellValues[][] loadSiblingArrayItems(int startxPos, int startyPos, WellValues[][] plateWV, WellValues[][] layoutMap, String[] selectedSamplesArr, int selectedSampleIndex) {
        plateWV[startxPos][startyPos] = layoutMap[startxPos][startyPos];
        if (plateWV[startxPos][startyPos] != null) {
            plateWV[startxPos][startyPos].sampleid = selectedSamplesArr[selectedSampleIndex];
            int lastLoadedSample = layoutMap[startxPos][startyPos].sample;
            if (lastLoadedSample == -1) {
                return plateWV;
            }
            for (int i = 0; i < layoutMap.length; ++i) {
                for (int j = 0; j < layoutMap[0].length; ++j) {
                    if (layoutMap[i][j] == null || layoutMap[i][j].sample != lastLoadedSample) continue;
                    plateWV[i][j] = layoutMap[i][j];
                    plateWV[i][j].sampleid = selectedSamplesArr[selectedSampleIndex];
                }
            }
        }
        return plateWV;
    }

    public class EvaluateArrayLayoutRulesHelper {
        int getCountForDimension(PriorityDimensions pd) throws SapphireException {
            if (pd == PriorityDimensions.SAMPLE) {
                return EvaluateArrayLayoutRules.this._sampleBound;
            }
            if (pd == PriorityDimensions.DILUTION) {
                return EvaluateArrayLayoutRules.this._dilutionStep;
            }
            if (pd == PriorityDimensions.REPEAT) {
                return EvaluateArrayLayoutRules.this._repeatCt;
            }
            if (pd == PriorityDimensions.TREATMENT) {
                return EvaluateArrayLayoutRules.this._treatmentCt;
            }
            throw new SapphireException("Unexpected Error");
        }

        int getItemCountForDirection(PriorityDimensions[] priority) {
            int dim = 1;
            if (priority != null) {
                for (PriorityDimensions hp : priority) {
                    if (hp == PriorityDimensions.SAMPLE) {
                        dim *= EvaluateArrayLayoutRules.this._sampleBound;
                        continue;
                    }
                    if (hp == PriorityDimensions.DILUTION) {
                        dim *= EvaluateArrayLayoutRules.this._dilutionStep;
                        continue;
                    }
                    if (hp == PriorityDimensions.REPEAT) {
                        dim *= EvaluateArrayLayoutRules.this._repeatCt;
                        continue;
                    }
                    if (hp != PriorityDimensions.TREATMENT) continue;
                    dim *= EvaluateArrayLayoutRules.this._treatmentCt;
                }
            }
            return dim;
        }

        PriorityDimensions[] getPriorityDimensionArray(String delimitedPriortiesList) {
            PriorityDimensions[] priorityDimArr = new PriorityDimensions[]{};
            if (delimitedPriortiesList != null && delimitedPriortiesList.length() > 0 && (priorityDimArr = new PriorityDimensions[delimitedPriortiesList.split(";").length]).length > 0) {
                int counter = 0;
                for (String d : delimitedPriortiesList.split(";")) {
                    switch (d.charAt(0)) {
                        case 'R': {
                            priorityDimArr[counter] = PriorityDimensions.REPEAT;
                            break;
                        }
                        case 'D': {
                            priorityDimArr[counter] = PriorityDimensions.DILUTION;
                            break;
                        }
                        case 'T': {
                            priorityDimArr[counter] = PriorityDimensions.TREATMENT;
                            break;
                        }
                        case 'C': {
                            priorityDimArr[counter] = PriorityDimensions.SAMPLE;
                        }
                    }
                    ++counter;
                }
            }
            return priorityDimArr;
        }
    }

    public static enum PriorityDimensions {
        SAMPLE,
        DILUTION,
        REPEAT,
        TREATMENT;

    }
}

