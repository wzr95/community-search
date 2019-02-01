package csd.Algos.DirectedAlgo.index;

import csd.Config;
import csd.Algos.DirectedAlgo.dcore.DcoreDecomposition;
import csd.util.BFS;
import csd.util.Abstracts.Tables;

import java.util.*;

/**
 * Created by Wang Zhongran on 2016/12/26.
 */

public class NestIdx extends Tables {
    private double memNum;
    private int numOfNodes,kCoreMax;
    public int [][] rows;
    public int [][] binStart,binEnd;// for each cell in a row, store the cell's start pos and the cell's end pos
    private int [][] coreSize;

    public double [][] aveConnectSize;
    public DcoreDecomposition myDcore;
    private int[] rowsNo;

    private HashMap<Integer,Integer> []convertedIdx;

    public NestIdx(DcoreDecomposition myD){
        this.myDcore = myD;
        myDcore.calKZero();
        memNum = 0;
        this.numOfNodes = myD.getNodesNum();
        this.kCoreMax = myD.getKCoreMax();               //how many rows?
        rows = new int [kCoreMax+1][];
        binStart = new int [kCoreMax+1][];
        binEnd = new int [kCoreMax+1][];
        coreSize = new int [kCoreMax+1][];
        aveConnectSize = new double [Config.cx+1][Config.cy+1];
        convertedIdx = new HashMap[numOfNodes];
        for(int i = 0;i<numOfNodes;i++) convertedIdx[i] = new HashMap<Integer,Integer>();
        rowsNo = new int[kCoreMax+1];
    }

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
            buildBins(row,rowTemp,subLTemp);
            makeCoreSize(row);
            if(upRow>=0){
                if(Config.saveSpace) saveSpace(upRow);
            }
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
        rowsNo = Arrays.copyOfRange(rowsNo,0,rowsPos);
        Arrays.sort(rowsNo);
        idxConversion();
    }

    private void buildBins(int row, int [] rowTemp, int [] subLTemp) {
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

    //record each k,l - core's size
    private void makeCoreSize(int rowNum){
        if(Config.saveSpace && rowNum < Config.defaultValue) return;
        coreSize[rowNum] = new int [binStart[rowNum].length];
        int lastBin = 0;
        for(int k = 0; k< coreSize[rowNum].length;k++){
            if(binStart[rowNum][k] == -1)coreSize[rowNum][k] = rows[rowNum].length - lastBin;
            else {
                lastBin = binStart[rowNum][k];
                coreSize[rowNum][k] = rows[rowNum].length - binStart[rowNum][k];
            }
        }

        //make connected size
        if(Config.calComponentSize){
            //only used in experiments and tests
            if(rowNum <= Config.cx){
                aveConnectSize[rowNum] = new double [Config.cy+1];
                lastBin = 0;
                for(int k = 0; k< Math.min(coreSize[rowNum].length,Config.cy+1);k++){
                    if(binStart[rowNum][k] == -1)coreSize[rowNum][k] = rows[rowNum].length - lastBin;
                    else {
                        lastBin = binStart[rowNum][k];
                    }
                    int [] component = Arrays.copyOfRange(rows[rowNum],lastBin,rows[rowNum].length);
                    aveConnectSize[rowNum][k] = calAveConnectSize(component);
                }
            }
        }
    }

    //This functions is only used in experiments and tests
    //this is to count the average size of connected component found by BFS
    //in a word, to find out the average size of communities
    private double calAveConnectSize(int [] component){
        double resultSize = 0;
        HashSet<Integer> subGraph = new HashSet<Integer>();
        HashSet<Integer> delete = new HashSet<Integer>();

        for(int x:component) subGraph.add(x);
        while(!subGraph.isEmpty()){
            delete.clear();
            Iterator<Integer> it = subGraph.iterator();
            int beginPoint = it.next();
            //do DFS
            Queue<Integer> myque = new LinkedList<Integer>();
            myque.add(beginPoint);
            delete.add(beginPoint);
            while(!myque.isEmpty()){
                int currentPoint = myque.poll();
                for(int n: myDcore.getInGraph()[currentPoint]){
                    if(subGraph.contains(n) && !delete.contains(n)){
                        myque.add(n);
                        delete.add(n);
                    }
                }
                for(int n: myDcore.getOutGraph()[currentPoint]){
                    if(subGraph.contains(n) && !delete.contains(n)){
                        myque.add(n);
                        delete.add(n);
                    }
                }
            }
            resultSize += delete.size() * 1.0 / component.length * delete.size();
            Iterator<Integer> it2 = delete.iterator();
            while(it2.hasNext()){
                subGraph.remove(it2.next());
            }

        }
        return resultSize;

    }

    //compute the number of elements in the index
    public double calMem(){
        double sum  = memNum;
        for(int i = 0;i<rows.length;i++){
            if(rows[i] == null) continue;
            sum = sum + rows[i].length;
        }
        return sum;
    }


    private void saveSpace(int row){
        if(Config.saveSpace && row<Config.defaultValue){
            for(int i = 0;i<binStart[row].length;i++){
                memNum+= binEnd[row][i] -binStart[row][i] + 1;
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

    //making query use this idx
    public int[] query(int qNode, int kConstraint, int lConstraint){
        int requiredRowPos = 0;
        //first find the minimum k that k>kConstraint
        for(int i = 0;i<rowsNo.length;i++){
            if(rowsNo[i] >= kConstraint){
                requiredRowPos = i;
                if(i!=0 && rowsNo[i] != kConstraint) requiredRowPos--;
                break;
            }
            if(i == rowsNo.length -1 ) {
                return null;
            }
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
        if(hasSolution == false) {
            return null;
        }
        else {
            lConstraint = convertedIdx[qNode].get(minRow) ;
        }
        //get the subgraph from index
        HashSet<Integer> subGraph = new HashSet<Integer>();
        for(int l = binStart[minRow][lConstraint]; l < rows[minRow].length;l++){
            subGraph.add(rows[minRow][l]);
        }

        if(!subGraph.contains(qNode)){
            return null;}
        else{
            //Log.log("Nested solution D-core size " + subGraph.size());
        }
        int [] result = BFS.bfs_D(qNode,subGraph,myDcore.getInGraph(),myDcore.getOutGraph());
        return result;

    }

    public int getKCoreMax(){
        return kCoreMax;
    }

    public int [][] getCoreSize(){
        return  coreSize;
    }

}
