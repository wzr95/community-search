package csd.Algos.DirectedAlgo.NoIndex;
import csd.util.Abstracts.NoIndexMethod;
import csd.util.BFS;

import java.util.*;
/**
 * Created by Wang Zhongran on 2017/4/4.
 */
public class DGlobal implements NoIndexMethod {
    private int numOfNodes;
    private int[][] inGraph, outGraph;
    private HashSet<Integer> subGraphSet;
    private int[] subInDegree, subOutDegree;
    private HashSet<Integer>[] inDegBucket, outDegBucket;
    private int removedVertex;
    private Queue<Integer> candidateQueue;
    private boolean [] candidateVisit;
    private boolean hasSolution;

    public DGlobal(int[][] inGraph, int[][] outGraph) {
        this.numOfNodes = inGraph.length;
        this.inGraph = inGraph;
        this.outGraph = outGraph;
        subInDegree = new int[numOfNodes];
        subOutDegree = new int[numOfNodes];
        subGraphSet = new HashSet<Integer>();
    }

    private void initGlobal() {
        subGraphSet.clear();
        inDegBucket = new HashSet[numOfNodes];
        outDegBucket = new HashSet[numOfNodes];
        hasSolution = true;
        for (int i = 0; i < numOfNodes; i++) {       //Initialize variables
            subInDegree[i] = inGraph[i].length;
            subOutDegree[i] = outGraph[i].length;
            subGraphSet.add(i);
        }
        for (int i = 0; i < numOfNodes; i++) {
            if (inDegBucket[subInDegree[i]] == null) {
                inDegBucket[subInDegree[i]] = new HashSet<Integer>();
            }
            inDegBucket[subInDegree[i]].add(i);
            if (outDegBucket[subOutDegree[i]] == null) {
                outDegBucket[subOutDegree[i]] = new HashSet<Integer>();
            }
            outDegBucket[subOutDegree[i]].add(i);
        }
        //System.out.println("Initial Ends! ");
    }
    private boolean deleteVertex(int queryNode, int k, int l) {  //if it finds a solution ,return true, else false
        int flag = 0;
        Iterator<Integer> it;
        int tempRemove = -1;
        for (int i = 0; i < k; i++) {
            if (inDegBucket[i] != null && inDegBucket[i].size() != 0) {
                it = inDegBucket[i].iterator();
                tempRemove = it.next();
                inDegBucket[i].remove(tempRemove);
                subGraphSet.remove(tempRemove);
                outDegBucket[subOutDegree[tempRemove]].remove(tempRemove);
                flag = 1;
                break;
            }
        }
        if (flag == 0) {
            for (int i = 0; i < l; i++) {
                if (outDegBucket[i] != null && outDegBucket[i].size() != 0) {
                    it = outDegBucket[i].iterator();
                    tempRemove = it.next();
                    outDegBucket[i].remove(tempRemove);
                    subGraphSet.remove(tempRemove);
                    inDegBucket[subInDegree[tempRemove]].remove(tempRemove);
                    flag = 1;
                    break;
                }
            }
        }
        this.removedVertex = tempRemove;
        if (tempRemove == queryNode) {
            //System.out.println("Fail, the queryNode doesn't satisfy constraint.!");
            hasSolution = false;
            return false;
        }
        if (flag == 0) {
            //System.out.println("Global Ends with Solution.");
            return true;
        }
        return false;
    }

    public int [] query (int queryNode, int k, int l) {
        //System.out.println("Global Search Begins with QueryNode " + queryNode);
        initGlobal();
        while (true) {                                           //we need to consider the deleting sequence here
            boolean findSolution = deleteVertex(queryNode, k, l);
            if (findSolution == true){
                //System.out.println("Now need BFS! ");
                return  BFS.bfs_D(queryNode,subGraphSet,inGraph,outGraph);
            }
            if (hasSolution == false){
                //System.out.println("Query Faile");
                return null;
            }
            for (int i = 0; i < inGraph[removedVertex].length; i++) {
                if (subGraphSet.contains(inGraph[removedVertex][i])) {
                    int tempDeg = subOutDegree[inGraph[removedVertex][i]];
                    subOutDegree[inGraph[removedVertex][i]] -= 1;
                    outDegBucket[tempDeg].remove(inGraph[removedVertex][i]);
                    if (outDegBucket[tempDeg - 1] == null) outDegBucket[tempDeg - 1] = new HashSet<Integer>();
                    outDegBucket[tempDeg - 1].add(inGraph[removedVertex][i]);
                }
            }
            for (int i = 0; i < outGraph[removedVertex].length; i++) {
                if (subGraphSet.contains(outGraph[removedVertex][i])) {
                    int tempDeg = subInDegree[outGraph[removedVertex][i]];
                    subInDegree[outGraph[removedVertex][i]] -= 1;
                    inDegBucket[tempDeg].remove(outGraph[removedVertex][i]);
                    if (inDegBucket[tempDeg - 1] == null) inDegBucket[tempDeg - 1] = new HashSet<Integer>();
                    inDegBucket[tempDeg - 1].add(outGraph[removedVertex][i]);
                }
            }
        }
    }
    public HashSet<Integer> calKLCore(int k , int l){
        initGlobal();
        int queryNode = -1;
        while (true) {                                           //we need to consider the deleting sequence here
            boolean findSolution = deleteVertex(queryNode, k, l);
            if (hasSolution == false){
                //System.out.println("Query Faile");
                return subGraphSet;
            }
            for (int i = 0; i < inGraph[removedVertex].length; i++) {
                if (subGraphSet.contains(inGraph[removedVertex][i])) {
                    int tempDeg = subOutDegree[inGraph[removedVertex][i]];
                    subOutDegree[inGraph[removedVertex][i]] -= 1;
                    outDegBucket[tempDeg].remove(inGraph[removedVertex][i]);
                    if (outDegBucket[tempDeg - 1] == null) outDegBucket[tempDeg - 1] = new HashSet<Integer>();
                    outDegBucket[tempDeg - 1].add(inGraph[removedVertex][i]);
                }
            }
            for (int i = 0; i < outGraph[removedVertex].length; i++) {
                if (subGraphSet.contains(outGraph[removedVertex][i])) {
                    int tempDeg = subInDegree[outGraph[removedVertex][i]];
                    subInDegree[outGraph[removedVertex][i]] -= 1;
                    inDegBucket[tempDeg].remove(outGraph[removedVertex][i]);
                    if (inDegBucket[tempDeg - 1] == null) inDegBucket[tempDeg - 1] = new HashSet<Integer>();
                    inDegBucket[tempDeg - 1].add(outGraph[removedVertex][i]);
                }
            }
        }
    }

}
