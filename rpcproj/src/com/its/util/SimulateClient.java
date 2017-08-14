package com.its.util;

/**
 * Created by lidapeng on 2017/8/10.
 */

import com.its.machine.LogEntry;
import com.its.machine.MachineInfo;
import com.its.machine.RPCFramework;
import com.its.machine.RPCResult;
import com.its.outer.OuterService;

/**
 * 用于发送log entry给leader(模拟发送命令给leader)
 */
public class SimulateClient {

    public static void main(String[] args) throws Exception {
        LogEntry logEntry = new LogEntry();
        logEntry.data = "example command";

        //获取别人提供的服务
        OuterService outerService = RPCFramework.getService(OuterService.class, MachineInfo.addrs[0], MachineInfo.ports[0]);
        //要票
        boolean res = outerService.addEntryToLeader(logEntry);
        System.out.println("client get result from cluster: " + res);
    }
}
