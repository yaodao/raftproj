package com.its.machine;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lidapeng on 2017/8/4.
 */
public class MachineInfo {
    public static int nodeCount = 3;
    public static String[] addrs = {"172.26.28.95", "172.26.28.59", "172.26.20.14"};
    public static int[] ports = {1001, 1003, 1002};

    public static String pc1 = "172.26.28.95";
    public static String pc2 = "172.26.28.59";
    public static String pc3 = "172.26.20.14";

    public static Map<String,Integer> ipToIdx = new HashMap<>();
    static {
        ipToIdx.put("172.26.28.95",0);
        ipToIdx.put("172.26.28.59",1);
        ipToIdx.put("172.26.20.14",2);
    }
}