package com.its.machine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lidapeng on 2017/8/4.
 */
public class AppendRPCParam {

    public Integer term;//发送方当前term号

    public String leaderId;//发送方地址

    public Integer prevLogIndex;//新log entry的前一个log entry的index值

    public Integer prevLogTerm;//新log entry的前一个log entry的term值

    public List<LogEntry> logEntries = new ArrayList<>();//要发送的log entry(对心跳来说是空)

    public Integer leaderCommit;//发送方已经committed的log entry的index值
}