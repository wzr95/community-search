package csd.Algos.DirectedAlgo.index;

import csd.Config;
import csd.Algos.DirectedAlgo.dcore.DcoreDecomposition;
import csd.util.BFS;
import csd.util.Abstracts.Tables;
import csd.util.LogWriter.Log;

import java.util.*;

/**
 * Created by Wang Zhongran on 2016/12/26.
 */

// This class is used to build a table index with a dynamic contain direction path

public class PathIdx extends Tables {

    // Index is stored in a 2-dim array, one row is is in a 1-dim array, and we use mark arrays to set apart cells in one row
    private int [][] rows;

    private int [][] binStart,binEnd; //for each cell in a row,its beginPos and endPos
    private int [] lMax; //for each row,its number of colomns
    private int globalLMax;
    private DcoreDecomposition myDcore;
    private int kCoreMax;
    private boolean [][]direction;  //false means right and true means left
    private int rowsNo[];
    private HashMap<Integer,Integer> []convertedIdx;
    private int [][] coreSize;
    private int numOfNodes;
    private double memNum;

    public PathIdx(DcoreDecomposition myD){
        globalLMax = 0;
        this.myDcore = myD;
        myDcore.calKZero();
        memNum  = 0;
        numOfNodes = myD.getNodesNum();
        this.kCoreMax = myD.getKCoreMax();               //how many rows?
        rows = new int [kCoreMax+1][];
        direction = new boolean[kCoreMax+1][];
        lMax = new int[kCoreMax+1];
        binStart = new int [kCoreMax+1][];
        binEnd = new int [kCoreMax+1][];
        rowsNo = new int[kCoreMax+1];
        coreSize = new int [kCoreMax+1][];
        convertedIdx = new HashMap[numOfNodes];
        for(int i = 0;i<numOfNodes;i++) convertedIdx[i] = new HashMap<Integer,Integer>();
    }

    //First, we build NestedIdx, then we compress the NestedIdx in the PathIdx way
    public void buildIndex(){
        int lTemp = 0;
        int rowsPos = 0;
        int upRow = -1;
        int [] independentRows = myDcore.inDependentRows();
        int counter = 0;
        for(int i = 0; i< independentRows.length;i++){
            counter ++;
            int row  = independentRows[i];
            int [] rowTemp = myDcore.calRow(independentRows[i]);
            rowsNo[rowsPos] = independentRows[i];
            rowsPos++;
            int [] subLTemp = myDcore.getSubL();
            int [] subKTemp = myDcore.getSubK();
            int lMaxTemp = subLTemp[rowTemp[rowTemp.length - 1 ]];
            if(lMaxTemp > globalLMax) globalLMax = lMaxTemp;
            //compute bins
            rows[independentRows[i]] = rowTemp;
            originalBins(row,rowTemp,subLTemp);
            makeCoreSize(row);
            lMax[independentRows[i]] = lMaxTemp;
            if(upRow>=0){
                compressTwoRows(upRow,row);
                updateArray(upRow);
                if(Config.saveSpace) saveSpace(upRow);
            }
            upRow = row;
            if(lMaxTemp > lTemp) lTemp = lMaxTemp;
            boolean flag = false;
            if(i < independentRows.length-1){
                int minRest = independentRows[i+1];
                for(int l = 0; l<rowTemp.length;l++){
                    if(subKTemp[rowTemp[l]]<minRest){
                        minRest = subKTemp[rowTemp[l]];
                        flag = true;
                    }
                }
                if(flag == true && independentRows[i+1] - independentRows[i] > 1) {
                    independentRows[i] = minRest + 1;
                    i--;
                }
            }
        }
        int finalRow = independentRows[independentRows.length-1];
        direction[finalRow] = new boolean[lMax[finalRow]+1];
        rowsNo = Arrays.copyOfRange(rowsNo,0,rowsPos);
        Arrays.sort(rowsNo);
        idxConversion();
    }

    //Build NestIdx
    private void originalBins(int row, int [] rowTemp, int [] subLTemp) {
        int [] binTemp = new int[subLTemp[rowTemp[rowTemp.length-1]] + 1];    //because rowTemp is strictly sorted
        for(int k = 0;k<binTemp.length;k++)binTemp[k] = -1;
        //the end element of rowTemp is the max l, where there is a (k,l) core
        int tempDegree = -1;
        for(int k = 0;k<rowTemp.length;k++){
            if(subLTemp[rowTemp[k]] > tempDegree){
                binTemp[subLTemp[rowTemp[k]]] = k;
                tempDegree = subLTemp[rowTemp[k]];
            }
        }
        int binSTemp[] = new int [binTemp.length];
        int binETemp[] = new int [binTemp.length];
        for(int k = 0;k<binETemp.length;k++){
            binETemp[k] = -1;
            binSTemp[k] = -1;
        }
        int l,lastCell = 0;
        for(l = 1;l<binTemp.length;l++){
            if(binTemp[l] == -1) continue;
            binSTemp[l] = binTemp[l];
            binETemp[lastCell] = binTemp[l]-1;
            lastCell = l;
        }
        binETemp[lastCell]= rows[row].length-1;
        binStart[row] = binSTemp;
        binEnd[row] = binETemp;
    }

    private void saveSpace(int row){
        if(Config.saveSpace && row<Config.defaultValue){
            for(int i = 0;i<binStart[row].length;i++){
                memNum+= binEnd[row][i] -binStart[row][i];
            }
            rows[row] = null;
            binStart[row] = null;
            binEnd[row] = null;
        }
        if(Config.saveSpace && rows[row]!=null){
            for(int a = 0;a<Config.defaultValue;a++){
                binStart[row][a] = -1;
                binEnd[row][a] = -1;
                memNum += binEnd[row][a] - binStart[row][a];
            }
            int start = 0;
            for(int b = Config.defaultValue;b<binStart[row].length;b++){
                if(binStart[row][b] == -1) continue;
                else{
                    start = binStart[row][b];
                    rows[row] = Arrays.copyOfRange(rows[row],binStart[row][b],rows[row].length);
                    break;
                }
            }
            for(int c = 0;c<binStart[row].length;c++){
                if(binStart[row][c]!=-1) {
                    binStart[row][c] -= start;
                    binEnd[row][c] -= start;
                }
            }
        }
    }

    private void makeCoreSize(int rowNum){
        if(Config.saveSpace && rowNum < Config.defaultValue) return;
        coreSize[rowNum] = new int [binStart[rowNum].length];
        int beforeSize = 0;
        for(int k = 0; k< coreSize[rowNum].length;k++){
            if(binStart[rowNum][k] == -1) coreSize[rowNum][k] = beforeSize;
            else coreSize[rowNum][k] = binEnd[rowNum][k] + 1 -binStart[rowNum][k] + beforeSize;
            beforeSize = coreSize[rowNum][k];
        }
    }

    //This function compress the NestedIdx in PathIdx way
    private void compressTwoRows(int upRow, int belowRow){
        HashSet<Integer> differMap = new HashSet<Integer>();
        int upLMax = lMax[upRow];
        int [][] rowDifferSet = new int [upLMax+1][];
        direction[upRow] = new boolean[upLMax+1];
        //do compression
        int rightSize=0, belowSize=0;
        int [] rowTemp = rows[upRow];
        //Make rowDifferSet for every certain cell in this row
        for(int l = upLMax;l>=0;l--) {
            //if it has a cell below,then we have to compare the size of below cell and left cell
            if (l == upLMax) rightSize = 0;
            else {
                for(int c = l + 1; c<upLMax;c++){
                    if(binStart[upRow][c] != -1){
                        rightSize = rowTemp.length - binStart[upRow][c];
                        break;
                    }
                }
            }
            if(lMax[belowRow] < l) belowSize = -1;
            else  {
                for(int c = l ; c<binStart[belowRow].length;c++){
                    if(binStart[belowRow][c] != -1){
                        belowSize = rows[belowRow].length - binStart[belowRow][c];
                        break;
                    }
                }
            }
            //maintain its differset map between its below cell
            if(binStart[upRow][l]!=-1){
                for (int k = binStart[upRow][l]; k <= binEnd[upRow][l]; k++) {
                    differMap.add(rowTemp[k]);
                }
            }
            if(lMax[belowRow] >= l){
                if(binStart[belowRow][l]!=-1){
                    for (int k = binStart[belowRow][l]; k <= binEnd[belowRow][l]; k++) {
                        differMap.remove(rows[belowRow][k]);
                    }
                }
            }
            if (belowSize > rightSize) {
                direction[upRow][l] = true;
                rowDifferSet[l] = new int[differMap.size()];
                Iterator<Integer> it = differMap.iterator();
                int tempPos = 0;
                while(it.hasNext()) {
                    rowDifferSet[l][tempPos] = it.next();
                    tempPos++;
                }
            }
        }
        // Do substituion useing rowDifferSet
        for (int k = 0; k <= upLMax; k++) {
            if(binStart[upRow][k] == binEnd[upRow][k]){
                continue;
            }
            if (direction[upRow][k] == true) {
                for (int q = 0; q < rowDifferSet[k].length; q++) {
                    rows[upRow][binStart[upRow][k] + q] = rowDifferSet[k][q];
                }
                binEnd[upRow][k] = binStart[upRow][k] + rowDifferSet[k].length;
            }
        }
    }
    private void updateArray(int row){
        int lastPos = 0;
        for(int i = 0;i<lMax[row]+1;i++){
            if(binStart[row][i] == -1) continue;
            else{
                int beginTemp = lastPos;
                for(int k = binStart[row][i];k<=binEnd[row][i];k++){
                    rows[row][lastPos] = rows[row][k];
                    lastPos++;
                }
                int endTemp = lastPos-1;
                binStart[row][i] = beginTemp;
                binEnd[row][i] = endTemp;
            }
        }
        rows[row] = Arrays.copyOfRange(rows[row],0,lastPos);
    }
    // "binEnd" is an array of Max K arrays, however, some arrays in rows need not to be calculated, so they are null
    public double calMem() {
        double sum = memNum;
        for (int i = 0; i < rowsNo.length; i++) {
            int rowNumber = rowsNo[i];
            if(rows[rowNumber] == null) continue;
            else{
                for(int k = 0; k< binStart[rowNumber].length;k++){
                    if(binStart[rowNumber][k] >= 0)
                        sum += binEnd[rowNumber][k] - binStart[rowNumber][k] + 1;
                }
            }
        }
        return sum;
    }
    public void idxConversion(){                       // a conversion of table index
        for(int i = 0;i<rowsNo.length;i++) {
            int row = rowsNo[i];
            if(Config.saveSpace && row < Config.defaultValue) continue;
            int [] temp = new int [numOfNodes];
            for(int l = 0;l<temp.length;l++) temp[l] = -1;
            int l;
            //iteratively convert this row
            for(l = 0;l<binStart[row].length;l++){
                if(binStart[row][l] == -1) continue;
                for(int k = binStart[row][l];k<binEnd[row][l];k++){
                    convertedIdx[rows[row][k]].put(row,l);
                }
            }
            //process the last cell of this row
        }
    }
    public int[] query(int qNode, int kConstraint, int lConstraint){
        int requiredRowPos = 0;
        //first find the minimum k that k>kConstraint
        for(int i = 0;i<rowsNo.length;i++){
            if(rowsNo[i] >= kConstraint){
                requiredRowPos = i;
                if(i!=0 && rowsNo[i] != kConstraint) requiredRowPos--;
                break;
            }
            if(i == rowsNo.length -1 ) return null;
        }
        int minRow = rowsNo[requiredRowPos];
        //find the minimum core
        boolean hasSolution = false;
        for(int i = requiredRowPos;i<rowsNo.length;i++){
            int row = rowsNo[i];
            if(convertedIdx[qNode].containsKey(row) && convertedIdx[qNode].get(row)>=lConstraint) {
                hasSolution = true;
                if(!convertedIdx[qNode].containsKey(minRow)) minRow = row;
                else {
                    if (coreSize[row][convertedIdx[qNode].get(row)] <= coreSize[minRow][convertedIdx[qNode].get(minRow)] )
                        minRow = row;
                }
            }
        }

        if(hasSolution == false) return null;
        else {
            lConstraint = convertedIdx[qNode].get(minRow) ;
        }
        //get the subgraph from index
        HashSet<Integer> subGraph = new HashSet<Integer>();
        int nowAddRow = minRow;
        int nowAddCol = lConstraint;
        while(true){
            for(int k = binStart[nowAddRow][nowAddCol]; k<= binEnd[nowAddRow][nowAddCol];k++){
                if(k!=-1)
                    subGraph.add(rows[nowAddRow][k]);
            }
            if(direction[nowAddRow][nowAddCol] == true){
                nowAddRow++;
                while(binStart[nowAddRow] == null) nowAddRow++;
                if(nowAddRow > rowsNo[rowsNo.length-1]) break;
            }
            else {
                nowAddCol++;
                if(binStart[nowAddRow].length <= nowAddCol) break;
            }
        }

        if(!subGraph.contains(qNode))
            return null;
        else{
            //Log.log("Path solution D-core size " + subGraph.size());
        }
        int [] result = BFS.bfs_D(qNode,subGraph,myDcore.getInGraph(),myDcore.getOutGraph());
        return result;
    }
    public int getKCoreMax(){
        return kCoreMax;
    }
}
