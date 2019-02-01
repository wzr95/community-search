package csd.util;
import csd.Config;

import java.io.*;
import java.util.*;

//Given 2 files:
//       1. a directed graph in adjacent format
//       2. all nodes' in-degree and out-degree
//Read into memory: two tables ;
// InTable means nodes and neighbors pointing to them;
// OutTable means nodes and neighbors be pointed

public class DataReader {
    private String rootName = null;

    //used in reading data, to create an array with exact lenght of neighbor's number
    private int numOfNodes,numOfEdges;
    private int [][] inGraph,outGraph;
    private int [] inNum,outNum;
    private HashMap<Integer,Integer> reflect;
    private  int sampleTail;
    private BufferedReader graphReader;

    //take $sampleRate$ percent vertices from the data
    //when sampling, we need do a reflect to make vertices ID begins from 0 and keep continuous
    public DataReader(String dataName,int sampleRate){
        this.rootName = Config.root + dataName + "/";
         numOfNodes = 0;
         reflect = new HashMap<Integer,Integer>();
         numOfEdges = 0;
         sampleTail = sampleRate - 1;
        try{
            graphReader = new BufferedReader(new FileReader(rootName + Config.graphName));
            String tempString = graphReader.readLine();
            String []splitString = tempString.split(" ");
            this.numOfNodes =Integer.parseInt(splitString[0]);
            //do sampling
            this.sampleTail  = sampleTail;
            int tempNum = (numOfNodes-1) / sampleRate;
            int tempRest = (numOfNodes-1) % sampleRate;
            tempNum = tempNum * (sampleTail + 1);
            for(int i = 0;i<=tempRest && i <=sampleTail;i++) tempNum++;
            numOfNodes = tempNum;
            inGraph = new int[numOfNodes][];
            outGraph = new int[numOfNodes][];
            inNum = new int[numOfNodes];
            outNum = new int [numOfNodes];
        }
        catch(IOException e){
            e.printStackTrace();
            return;
        }

    }

    public int convertID(int nodeId){  //given a true nodeId, return a sampled Id
        int temp = nodeId % 100;
        int rest = 0;
        for(int i =0;i<=temp && i<=sampleTail;i++) rest ++;
        rest --;
        return (sampleTail + 1) * (nodeId /100) + rest;
    }

    public void read(){
        String tempString1,tempString2;
        String [] split1,split2;
        try {
            BufferedReader edgeReader = new BufferedReader(new FileReader(rootName + Config.edgeFileName));
            while((tempString1 = edgeReader.readLine())!=null){
                split1 = tempString1.split(" "); // nodeId indegree outdegree
                int nid = Integer.parseInt(split1[0]);
                if(nid % 100 >sampleTail) continue;;
                int cNid = convertID(nid);
                if(!reflect.containsKey(cNid))reflect.put(cNid,nid);
                inGraph[cNid] = new int[Integer.parseInt(split1[1])];
                outGraph[cNid] = new int[Integer.parseInt(split1[2])];
            }
            while((tempString2 = graphReader.readLine())!=null){
                split2 = tempString2.split(" "); // nodeId outNeigh1 outNeigh2 ..
                int nid = Integer.parseInt(split2[0]);
                if(nid % 100 >sampleTail) continue;;// ...
                int cNid = convertID(nid);
                if(!reflect.containsKey(cNid))reflect.put(cNid,nid);
                for(int k =1 ;k<split2.length;k++   ){
                    int neighborId = Integer.parseInt(split2[k]);
                    if(neighborId == nid || neighborId % 100 >sampleTail) continue;;
                    int cNeighId = convertID(neighborId);
                    if(!reflect.containsKey(cNeighId))reflect.put(cNeighId,neighborId);
                    outGraph[cNid][outNum[cNid]] = cNeighId;
                    outNum[cNid]++;
                    inGraph[cNeighId][inNum[cNeighId]] = cNid;
                    inNum[cNeighId]++;
                }
                outGraph[cNid] = Arrays.copyOfRange(outGraph[cNid], 0, outNum[cNid]);
            }
            for(int k = 0; k< numOfNodes;k++){
                if(inGraph[k] != null)
                    inGraph[k] = Arrays.copyOfRange(inGraph[k],0,inNum[k]);
            }
            edgeReader.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public int[][] getIn(){
        return inGraph;
    }

    public int[][] getOut(){
        return outGraph;
    }

    public int getNumOfEdges(){
        for(int i = 0;i<numOfEdges;i++){
            numOfEdges += inGraph[i].length;
            System.out.println("NumOfEdegs "+ numOfEdges);
        }
        return numOfEdges;
    }

    //output a sample graph to disk
    public void outPutSampleGraph(){
        try{
            int [] inDegrees = new int [inGraph.length];
            String linkName = "OutInlink.dat";
            String degreeName = "OutDegree.dat";
            FileWriter linkWritter = new FileWriter(linkName,false);
            BufferedWriter blinkWritter = new BufferedWriter(linkWritter);
            FileWriter degreeWritter = new FileWriter(degreeName,false);
            BufferedWriter bdegreeWritter = new BufferedWriter(degreeWritter);
            blinkWritter.write(inGraph.length + "");
            blinkWritter.newLine();
            for(int i = 0;i<inGraph.length;i++){
                if(inGraph[i] == null) inDegrees[i] = 0;
                else inDegrees[i] = inGraph[i].length;
            }
            for(int i = 0;i<outGraph.length;i++){
                int tempOutDegree;
                if(outGraph[i] == null) tempOutDegree = 0;
                else tempOutDegree = outGraph[i].length;
                bdegreeWritter.write(i + " " + inDegrees[i] + " " + tempOutDegree);
                bdegreeWritter.newLine();
                if(outGraph[i] == null) continue;
                else{
                    blinkWritter.write(i + " ");
                    for(int l = 0;l<outGraph[i].length;l++){
                        blinkWritter.write(outGraph[i][l] + " ");
                    }
                    blinkWritter.newLine();
                }
            }
            blinkWritter.flush();
            blinkWritter.close();
            bdegreeWritter.flush();
            bdegreeWritter.close();
        }catch (IOException e){
            e.printStackTrace();;
        }
    }
    public HashMap<Integer,Integer> getReflect(){
        return reflect;
    }
}
