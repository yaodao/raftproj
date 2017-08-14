package com.its.util;

import com.its.machine.MachineInfo;
import com.its.machine.RPCResult;
import jdk.nashorn.internal.codegen.CompilerConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by lidapeng on 2017/8/11.
 */
public class TaskExecutor {
    public static ExecutorService exec = Executors.newFixedThreadPool(MachineInfo.nodeCount);
    public static List<Future<RPCResult>> futureArr = new ArrayList<>();

    public static Future<RPCResult> submitTask(Callable task) {
        return  exec.submit(task);
    }

    public static List<Future<RPCResult>> submitTasks(List<Callable> tasks) {
        for (Callable task : tasks) {
            futureArr.add(exec.submit(task));
        }
        return futureArr;
    }

    @Override
    protected void finalize() {
        exec.shutdown();
    }
}
