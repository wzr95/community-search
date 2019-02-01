package csd.util;

import csd.Config;
import csd.util.LogWriter.QueryResultWritter;

import java.util.*;

/**
 * Created by Zhhongran 2017/6/13.
 * After we get the k,l-core from the index table
 * Do BFS in the (k,l)-core and find the community we want.
 */
public class BFS {

    //do bfs without considering edges' directions
    public static int [] bfs_Und(int queryNode, HashSet<Integer> subGraph, int k,int [][] undGraph){
        Queue<Integer> myque = new LinkedList<Integer>();
        int [] result = new int [subGraph.size()];
        int resultPos = 0;
        myque.add(queryNode);
        int numOfNodes = undGraph.length;
        boolean []visited = new boolean[numOfNodes];
        visited[queryNode] = true;
        while(!myque.isEmpty()){
            int temp = myque.poll();
            result[resultPos] = temp;
            resultPos++;
            for(int i = 0;i<undGraph[temp].length;i++){
                int neigh = undGraph[temp][i];
                if (subGraph.contains(neigh)&& visited[neigh]!=true){
                    myque.add(neigh);
                    visited[neigh] = true;
                }
            }
        }
        result = Arrays.copyOfRange(result,0,resultPos);
        if(Config.outPut)   QueryResultWritter.write(queryNode,"QueryResult",result);
        return result;
    }

    //do bfs considering edges' directions
    public static int [] bfs_D(int queryNode, HashSet<Integer> subGraph, int [][] inGraph, int[][] outGraph){
        if(subGraph.size() == 0) return null;
        Queue<Integer> myque = new LinkedList<Integer>();
        boolean [] inSub = new boolean[inGraph.length];
        for (Integer i: subGraph
             ) {
            inSub[i] = true;
        }
        int [] result = new int [subGraph.size()];
        int resultPos = 0;
        myque.add(queryNode);
        int numOfNodes = inGraph.length;
        boolean []visited = new boolean[numOfNodes];
        visited[queryNode] = true;
        int neigh = 0;
        while(!myque.isEmpty()){
            int temp = myque.poll();
            result[resultPos] = temp;
            resultPos ++;
            for(int i = 0;i<inGraph[temp].length;i++){
                neigh = inGraph[temp][i];
                if(!inSub[neigh]||visited[neigh]){
                    continue;
                }
                myque.add(neigh);
                visited[neigh] = true;
            }
            for(int i = 0;i<outGraph[temp].length;i++){
                neigh = outGraph[temp][i];
                if(!inSub[neigh]||visited[neigh]){
                    continue;
                }
                myque.add(neigh);
                visited[neigh] = true;

            }
        }
        result = Arrays.copyOfRange(result,0,resultPos);
        if(Config.outPut)   QueryResultWritter.write(queryNode,"QueryResult",result);

        return result;
    }

    public static int [] writeToFile(int queryNode, int k, int l,HashSet<Integer> subGraph,String type){
        int [] result = new int[subGraph.size()];
        int resultPos = 0;
        Iterator<Integer> it = subGraph.iterator();
        while(it.hasNext()){
            result[resultPos] = it.next();
            resultPos ++;
        }
        result = Arrays.copyOfRange(result,0,resultPos);
        if(Config.outPut){
            QueryResultWritter.write(queryNode, type, result);
        }
        return result;
    }
}
