package csd;

/**
 * Created by Administrator on 2016/12/16.
 */
public class Config {
    //File Path
    public static String root = "";

    public static String DataName = "TestDataset";
    public static String rootFold = root + DataName + "/";
    //File recording every node's in degree and out degree.
    public static String edgeFileName = "Degree.dat";
    //Fiel recording the graph in a adjacent format(
    public static String graphName = "Graph.dat";
    public static String logFilePath = "expLog.dat";


    //SaveSpace Variable is only used for the convenience for conducting experiments, having no effect on the results
    //This variable is used determine whether to store all the idx in memory
    //During our experiment, due to hardware constraint, when there is no need to store all index table in memory, this variable is set to be true
    //Note that saveSpace has no influence on calculating the different indexes' space cost
    public static boolean saveSpace = false;

    //default k and l value during experiments
    public static int defaultValue = 8;

    public static int cx = 1000; //draw 3-dimmension connected componet size table
    public static int cy = 1000;

    //used in experiments, whether calculate average connected component's(community) size
    public static boolean calComponentSize = false;

    //whether output query result
    public static boolean outPut = true;
}
