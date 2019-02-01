package csd.util.LogWriter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Johnny on 2017/6/13.
 */
//This static class provides methods to write community queries' result to file
public class QueryResultWritter {
    public static void write(int queryNode, String fileName, int [] result){
        try{
            System.out.println("Writing Results...");
            FileWriter fileWritter = new FileWriter(fileName,true);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write("Query Node: " + queryNode +  " Component has following nodes: ");
            for(int i=0;i<result.length;i++){
                bufferWritter.write(result[i] + ", ");
            }
            bufferWritter.newLine();
            bufferWritter.flush();
            bufferWritter.close();
            System.out.println("Done.");
        }catch (IOException e){
            e.printStackTrace();;
        }
    }
}
