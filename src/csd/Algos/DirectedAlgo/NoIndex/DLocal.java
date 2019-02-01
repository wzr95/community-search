package csd.Algos.DirectedAlgo.NoIndex;

import csd.util.Abstracts.NoIndexMethod;
import csd.util.BFS;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Johnny on 2017/12/3.
 */
public class DLocal implements NoIndexMethod {
    private int numOfNodes;
    private int[][] inGraph, outGraph;
    private HashSet<Integer> subGraphSet;
    private int[] subInDegree, subOutDegree;
    private HashSet<Integer>[] inDegBucket, outDegBucket;
    private int removedVertex;
    private Queue<Integer> candidateQueue;
    private boolean [] candidateVisit;
    private boolean hasSolution;

    public DLocal(int[][] inGraph, int[][] outGraph) {
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

    public int [] reDGlobal (int queryNode, int k, int l) {
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
    private void initialLocal(int queryNode) {
        subGraphSet.clear();
        removedVertex = -1;
        candidateVisit = new boolean[numOfNodes];
        candidateVisit[queryNode] = true;
        inDegBucket = new HashSet[numOfNodes];
        outDegBucket = new HashSet[numOfNodes];
        hasSolution = true;
        candidateQueue = new LinkedList<Integer>();
        candidateQueue.add(queryNode);
        for (int i = 0; i < numOfNodes; i++) {
            subInDegree[i] = -1;
            subOutDegree[i] = -1;
            inDegBucket[i] = new HashSet<Integer>();
            outDegBucket[i] = new HashSet<Integer>();
        }
    }
    private boolean isLocalSolution(int queryNode, int k, int l){
        boolean isSolution = true;
        for(int i = 0; i< k;i++){
            if(inDegBucket[i].size()!=0) {
                isSolution =false;
                return  isSolution;
            }
        }
        for(int i = 0;i <l;i++){
            if(outDegBucket[i].size()!=0){
                isSolution=false;
                return  isSolution;
            }
        }
        return  isSolution;
    }
    private void addVertex(int addedNode, int k, int l,HashSet<Integer> dcore) {  //if it finds a solution ,return true, else false
        if(subGraphSet.contains(addedNode)) return;
        subGraphSet.add(addedNode);
        subInDegree[addedNode] = 0;
        subOutDegree[addedNode] = 0;
        for (int i = 0; i < inGraph[addedNode].length; i++) {
            int tempInNeigh = inGraph[addedNode][i];
            if(dcore!=null && !dcore.contains(tempInNeigh))
                continue;
            if (subGraphSet.contains(tempInNeigh)) {
                subInDegree[addedNode]++;
                outDegBucket[subOutDegree[tempInNeigh]].remove(tempInNeigh);
                outDegBucket[subOutDegree[tempInNeigh] + 1].add(tempInNeigh);
                subOutDegree[tempInNeigh]++;
            }else{
                if(inGraph[tempInNeigh].length>=k && outGraph[tempInNeigh].length>=l){
                    if(candidateVisit[tempInNeigh]==false){
                        candidateVisit[tempInNeigh] = true;
                        candidateQueue.add(tempInNeigh);
                    }
                }
            }
        }
        for (int i = 0; i < outGraph[addedNode].length; i++) {
            int tempOutNeigh = outGraph[addedNode][i];
            if(dcore!=null && !dcore.contains(tempOutNeigh))
                continue;
            if (subGraphSet.contains(tempOutNeigh)) {
                subOutDegree[addedNode]++;
                inDegBucket[subInDegree[tempOutNeigh]].remove(tempOutNeigh);
                inDegBucket[subInDegree[tempOutNeigh] + 1].add(tempOutNeigh);
                subInDegree[tempOutNeigh]++;
            }else{
                if(inGraph[tempOutNeigh].length>=k && outGraph[tempOutNeigh].length>=l){
                    if(candidateVisit[tempOutNeigh]==false){
                        candidateVisit[tempOutNeigh]=true;
                        candidateQueue.add(tempOutNeigh);
                    }
                }
            }
        }

        inDegBucket[subInDegree[addedNode]].add(addedNode);
        outDegBucket[subOutDegree[addedNode]].add(addedNode);
        return;
    }
    public int [] query(int queryNode, int k, int l) {
        initialLocal(queryNode);
        if (inGraph[queryNode].length < k || outGraph[queryNode].length < l) {
            //System.out.println("This query doesn't have a solution!");
            hasSolution = false;
            return null;
        }
        while (!candidateQueue.isEmpty()) {
            int tempNode = candidateQueue.poll();
            addVertex(tempNode, k, l,null);
            //System.out.println("Adding vertex with No. " + tempNode);
            if(isLocalSolution(queryNode,k,l)){
                //System.out.println("A solution community is found while generating candidates!");
                return  BFS.bfs_D(queryNode,subGraphSet,inGraph,outGraph);
            }
        }
        return reDGlobal(queryNode,k,l);
    }
    public int [] dcoreLocal(HashSet<Integer> dcore, int queryNode, int k, int l,String type) {
        //System.out.println("D-core's Local Search Begins with Query Node" + queryNode);
        initialLocal(queryNode);
        if (!dcore.contains(queryNode)) {
            System.out.println("This query doesn't have a solution!");
            hasSolution = false;
            return null;
        }
        while (!candidateQueue.isEmpty()) {
            int tempNode = candidateQueue.poll();
            addVertex(tempNode, k, l,dcore);
            //System.out.println("Adding vertex with No. " + tempNode);
            if(isLocalSolution(queryNode,k,l)){
                //System.out.println("A solution community is found while generating candidates!");
                return  BFS.writeToFile(queryNode,k,l,subGraphSet,type);
            }
        }
        return reDGlobal(queryNode,k,l);
    }
}
