package csd.Algos.DirectedAlgo.index;

import csd.Algos.DirectedAlgo.dcore.DcoreDecomposition;
import csd.util.Abstracts.Tables;

import java.util.*;

/**
 * Created by Wang Zhongran on 2016/12/26.
 * In this class, we implement a basic index.
 * Note that real basic index is too large to be stored in memory, thus we store it in a nested style while
 * counting its mem cost in basic style.
 *
 * Note : because of memory space constraint, it is hard to build a real BasicIdx in memory
 * So we only compute the number of elements here
 */


public class BasicIdx extends Tables {
    public int [][] bins;
    private int [][] coreSize;
    public DcoreDecomposition myDcore;
    private int kCoreMax,numOfNodes;
    private double memNum;
    private int[] rowsNo;
    //variables for dynamci maintenance
    private HashMap<Integer,Integer> []convertedIdx;

    public BasicIdx(DcoreDecomposition myD){
        memNum  = 0;
        this.myDcore = myD;
        myDcore.calKZero();
        this.numOfNodes = myD.getNodesNum();
        this.kCoreMax = myD.getKCoreMax();               //how many rows?
        convertedIdx = new HashMap[numOfNodes];
        for(int i = 0;i<numOfNodes;i++) convertedIdx[i] = new HashMap<Integer,Integer>();
    }
    public void buildIndex(){
        int [][] inGraph = myDcore.getInGraph();
        bins = new int [kCoreMax+1][];
        int [] independentRows = myDcore.inDependentRows();
        int [] rowVisited = new int [inGraph.length];
        int rowsPos = 0;
        rowsNo = new int [kCoreMax+1];
        for(int i=independentRows.length-1;i>=0;i--){
            int [] rowTemp = myDcore.calRow(independentRows[i]);
            if(rowVisited[independentRows[i]] == 1) continue;
            rowVisited[independentRows[i]] = 1;
            rowsNo[rowsPos] = independentRows[i];
            rowsPos++;
            int [] subLTemp = myDcore.getSubL();
            int [] subKTemp = myDcore.getSubK();
            int [] binTemp = new int[subLTemp[rowTemp[rowTemp.length-1]] + 1];    //because rowTemp is strictly sorted
            for(int k = 0;k<binTemp.length;k++)binTemp[k] = -1;
            int tempDegree = -1;
            for(int k = 0;k<rowTemp.length;k++){
                memNum = memNum + (subLTemp[rowTemp[k]] + 1);
                if(subLTemp[rowTemp[k]] > tempDegree){
                    binTemp[subLTemp[rowTemp[k]]] = k;
                    tempDegree = subLTemp[rowTemp[k]];
                }
            }
            bins[independentRows[i]] = binTemp;                       //binTemp records every cell's start position
            if(i != independentRows.length - 1){
                boolean flag = false;
                int minRest = independentRows[independentRows.length-1];
                int beginNum = independentRows[i];
                int endNum = independentRows[i+1] - 1;
                for(int l = 0; l<rowTemp.length;l++){
                    if(subKTemp[rowTemp[l]]  >= beginNum && subKTemp[rowTemp[l]]<endNum){
                        if(subKTemp[rowTemp[l]] < minRest)
                            minRest = subKTemp[rowTemp[l]];
                        flag = true;
                    }
                }
                if(flag == true) {
                    independentRows[i] = minRest + 1;
                    i++;
                }
            }
        }

        rowsNo = Arrays.copyOfRange(rowsNo,0,rowsPos);
        Arrays.sort(rowsNo);
    }
    public double calMem(){
        return memNum;
    }

    public int[] query(int qNode, int kConstraint, int lConstraint){
        return null;
    }

    public int getKCoreMax(){return kCoreMax;}
}
