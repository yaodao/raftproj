package com.its.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lidapeng on 2017/8/4.
 */
public class SystemState {
    public static String myID="172.26.28.95";//这个可以从配置文件中读取
    public static volatile int currentTerm=0;
    public static String votedFor=null;
    public static List<LogEntry> entries = new ArrayList<>();
    //1 follower,2 candidate,3 leader
    public static volatile int role=1;
    public static volatile Integer commitIndex;//暂时认为,该值是最新的那个 应用到本机的log entry的index值
    public static volatile Integer lastApplied;//暂时认为,同commitIndex值相同

    public static AtomicLong newestIndex;//本机log entry最新的值(无论是否已提交)


    public static volatile Integer nextIndex[];
    public static volatile Integer matchIndex[];

    public static List<Integer> failIdx = new ArrayList<>();//leader发送log entry失败的那些机器的序号
}
