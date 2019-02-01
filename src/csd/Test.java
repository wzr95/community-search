package csd;

import java.util.*;
import csd.Algos.DirectedAlgo.dcore.DcoreDecomposition;
import csd.Algos.DirectedAlgo.index.*;
import csd.util.DataReader;

import csd.util.Abstracts.Tables;

/**
 * @author Zhongran
 * @date Jun 29, 2017
 * to show how to build an index
 */
public class Test {

    public static void main(String [] args){
        String dataName = "TestDataset";

        DataReader reader = new DataReader(dataName, 100);
        reader.read(); //Read data

        DcoreDecomposition decomp = new DcoreDecomposition(reader.getIn(), reader.getOut()); // Do decomposition

        //Tables t = new NestIdx(decomp);
        Tables t = new PathIdx(decomp);
        //Tables t = new UnionIdx(decomp);
        t.buildIndex(); // build a nested idx
        //Use index to query
        int qNode = 2;
        int k = 1;
        int l = 1;

        int [] queryResult = t.query(qNode, k, l);
        if(queryResult == null) System.out.println("No community found!");

    }
}
