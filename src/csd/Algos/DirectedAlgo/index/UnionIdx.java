package csd.Algos.DirectedAlgo.index;
/**
 * Created by Wang Zhongran on 2017/1/13.
 */
import csd.Config;
import csd.Algos.DirectedAlgo.dcore.DcoreDecomposition;
import csd.util.*;
import csd.util.Abstracts.Tables;

import java.util.*;

//This class is used to build a table index using a union operation.
public class UnionIdx extends Tables {

    // Index is stored in a 2-dim array, one row is is in a 1-dim array, and we use mark arrays to set apart cells in one row
    private int[][] rows;

    //for each row's each cell, its begin and end pos, the cell's directions, and the core size
    private int [][] binStart, binEnd,directions,coreSize;

    private int[] lMax,rowsNo; //lamx -> for each row,its number of colomns   rowsNo->after prune, the rest rows' id, sorted ascend

    private int numOfNodes, kCoreMax;
    private DcoreDecomposition myDcore;
    private HashMap<Integer, Integer>[] convertedIdx;
    public int [] vMaxK;
    private double memNum;
    private int [][]inGraph,outGraph;


    public UnionIdx(DcoreDecomposition myD) {
        memNum = 0;
        this.myDcore = myD;
        inGraph = myD.getInGraph();outGraph = myD.getOutGraph();
        myDcore.calKZero();
        numOfNodes = myD.getNodesNum(); kCoreMax = myD.getKCoreMax();
        vMaxK = new int[numOfNodes];
        rows = new int[kCoreMax + 1][];
        binStart = new int[kCoreMax + 1][];binEnd = new int[kCoreMax + 1][];
        lMax = new int[kCoreMax + 1];rowsNo = new int[kCoreMax + 1];
        directions = new int[kCoreMax + 1][];              //0->both, 1->right, 2->left
        convertedIdx = new HashMap[numOfNodes];            //this is a converted index, for each row, we record which point appears in which cell
        coreSize = new int [kCoreMax+1][];
        for (int i = 0; i < numOfNodes; i++) convertedIdx[i] = new HashMap<Integer, Integer>();
    }

    //First, we build NestedIdx, then we compress the NestedIdx in the UnionIdx way
    public void buildIndex(){
        int lTemp = 0;
        int rowsPos = 0;
        int upRow = -1;
        int [] independentRows = myDcore.inDependentRows();
        for(int i = 0; i< independentRows.length;i++){
            int row  = independentRows[i];
            int [] rowTemp = myDcore.calRow(independentRows[i]);
            rowsNo[rowsPos] = independentRows[i];
            rowsPos++;
            int [] subLTemp = myDcore.getSubL();
            int [] subKTemp = myDcore.getSubK();
            int lMaxTemp = subLTemp[rowTemp[rowTemp.length - 1 ]];
            //compute bins
            rows[independentRows[i]] = rowTemp;
            originalBins(row,rowTemp,subLTemp);
            lMax[independentRows[i]] = lMaxTemp;
            makeCoreSize(row);
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
        directions[finalRow] = new int [lMax[finalRow]+1];
        for(int i = 0;i<directions[finalRow].length;i++) directions[finalRow][i] = 1;
        rowsNo = Arrays.copyOfRange(rowsNo,0,rowsPos);
        Arrays.sort(rowsNo);
        idxConversion();
    }

    //Build NestIdx
    private void originalBins(int row, int [] rowTemp, int [] subLTemp) {   //this function build bins before compression
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
        if(binETemp[0] != -1) binSTemp[0] = 0;
        binETemp[lastCell]= rows[row].length-1;
        binStart[row] = binSTemp;
        binEnd[row] = binETemp;
    }

    private void saveSpace(int row){
        if(Config.saveSpace && row<Config.defaultValue){
            for(int i = 0;i<binStart[row].length;i++){
                memNum+=  binEnd[row][i] - binStart[row][i] + 1;
            }
            rows[row] = null;
            binStart[row] = null;
            binEnd[row] = null;
        }
        if(Config.saveSpace && rows[row]!=null){
            for(int a = 0;a<Config.defaultValue;a++){
                memNum += binEnd[row][a] - binStart[row][a] + 1;
                binStart[row][a] = -1;
                binEnd[row][a] = -1;
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
        for(int k = coreSize[rowNum].length -1 ; k>=0 ;k--){
            if(binStart[rowNum][k] == -1) coreSize[rowNum][k] = beforeSize;
            else coreSize[rowNum][k] = binEnd[rowNum][k] + 1 -binStart[rowNum][k] + beforeSize;
            beforeSize = coreSize[rowNum][k];
        }
    }

    //This function compress the NestedIdx in UnionIdx way
    private void compressTwoRows(int upRow, int belowRow){
        HashSet<Integer> unionSet = new HashSet<Integer>();
        HashSet<Integer> belowSet = new HashSet<Integer>();
        HashSet<Integer> rightSet = new HashSet<Integer>();
        int[] tempBucketDeleteNum = new int[lMax[upRow] + 1];
        int[] directionTemp = new int[lMax[upRow] + 1];
        int l;
        for (int k = lMax[upRow]; k >= 0; k--) {
            if (k <= lMax[belowRow]){
                if(binStart[belowRow][k]!=-1){
                    for (l = binStart[belowRow][k]; l <= binEnd[belowRow][k]; l++) {
                        belowSet.add(rows[belowRow][l]);
                        unionSet.add(rows[belowRow][l]);
                    }
                }
            }
            if (unionSet.size() == belowSet.size()) directionTemp[k] = 2;  //direction == 2 means arrow points at below
            else if (unionSet.size() == rightSet.size()) directionTemp[k] = 1; //direction == 1 means arrow points at right
            if(binStart[upRow][k] >= 0){
                for (l = binStart[upRow][k]; l <= binEnd[upRow][k]; l++) {
                    if (unionSet.contains(rows[upRow][l])) {
                        int swapTemp = rows[upRow][l];
                        rows[upRow][l] = rows[upRow][binEnd[upRow][k]];
                        rows[upRow][binEnd[upRow][k]] = swapTemp;
                        binEnd[upRow][k]--;
                        l--;
                    }
                }
                for (l = binStart[upRow][k]; l <= binStart[upRow][k]; l++) {
                    unionSet.add(rows[upRow][l]);
                    rightSet.add(rows[upRow][l]);
                }
            }
        }
        directions[upRow] = directionTemp;
    }
    private void updateArray(int row){
        int lastPos = 0;
        for(int i = 0;i<lMax[row]+1;i++){
            if(binStart[row][i] == -1) continue;
            else{
                int beginTemp = lastPos;
                for(int k = binStart[row][i];k<binEnd[row][i];k++){
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
                    vMaxK[rows[row][k]] = row;
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
        boolean [][]visitedCell = new boolean[rowsNo.length][];
        for(int i = 0;i<rowsNo.length;i++){
            visitedCell[i] = new boolean[lMax[rowsNo[i]] + 1];
        }
        Queue<Cell> myque = new LinkedList<Cell>();
        HashMap<Integer,Integer> rowPruner,colPruner;
        rowPruner = new HashMap<Integer,Integer>();
        colPruner = new HashMap<Integer,Integer>();
        myque.add(new Cell(requiredRowPos,lConstraint));
        while(!myque.isEmpty()){
            Cell tempCell = myque.poll();
            int kPos = tempCell.k;
            int lPos = tempCell.l;
            int row = rowsNo[kPos];
            for(int i = binStart[row][lPos];i<binEnd[row][lPos];i++){
                subGraph.add(rows[row][i]);
            }
            if(directions[rowsNo[kPos]][lPos] == 1){
                if(!colPruner.containsKey(lPos))  colPruner.put(lPos,kPos);
            }
            if(directions[rowsNo[kPos]][lPos] == 2){
                if(!rowPruner.containsKey(kPos))rowPruner.put(kPos,lPos);
            }
            if(lPos + 1 < binStart[rowsNo[kPos]].length && visitedCell[kPos][lPos+1] != true){
                if(!colPruner.containsKey(lPos + 1) || colPruner.get(lPos+1) > kPos){
                    if(!rowPruner.containsKey(kPos) || rowPruner.get(kPos)>lPos + 1)
                        myque.add(new Cell(kPos,lPos+1));
                    visitedCell[kPos][lPos+1] = true;
                }
            }

            if(kPos + 1 < rowsNo.length && lPos < visitedCell[kPos+1].length && visitedCell[kPos+1][lPos] != true){
                if(!colPruner.containsKey(lPos) || colPruner.get(lPos) > kPos+1){
                    if(!rowPruner.containsKey(kPos+1)|| rowPruner.get(kPos+1)>lPos){
                        myque.add(new Cell(kPos+1,lPos));
                        visitedCell[kPos+1][lPos] = true;
                    }
                }
            }

        }
        if(!subGraph.contains(qNode))
            return null;
        else{
            //Log.log("Union solution D-core size " + subGraph.size());
        }
        int [] result = BFS.bfs_D(qNode,subGraph,myDcore.getInGraph(),myDcore.getOutGraph());
        return result;
    }

    public int getKCoreMax(){
        return kCoreMax;
    }

    public int [][] getCoreSize(){return coreSize;};

    private int validOutNeighs(int v, int lb,int ub,int k,Queue<Integer> bfsQue, boolean [] visited, int [] validOutDegree){
        int count = 0;
        HashSet<Integer> partVisitVert = new HashSet<Integer>();
        for(int i = 0;i<outGraph[v].length;i++){
            int nei = outGraph[v][i];
            int mark = kFindL(nei,k);
            if(mark >= lb){
                count ++;
                if(!visited[nei]){
                    if(mark <= ub)
                        partVisitVert.add(nei);
                }
            }
        }
        if(count >= lb && count <= ub){
            for(int x:partVisitVert){
                visited[x] = true;
                bfsQue.add(x);
            }
        }
        validOutDegree[v] = count;
        return count;
    }
    private int kFindL(int v, int k){
        //given a certain k and a vertex v, find its maxL
        if(k > vMaxK[v]){
            return - 1;
        }
        else{
            for(int i = k; i<=vMaxK[v];i++){
                if(!convertedIdx[v].containsKey(i)) continue;
                else return convertedIdx[v].get(i);
            }
        }
        return -1;
    }
}