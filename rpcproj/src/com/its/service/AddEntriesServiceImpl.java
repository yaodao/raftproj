package com.its.service;

import com.its.machine.AppendRPCParam;
import com.its.machine.MachineInfo;
import com.its.machine.RPCFramework;
import com.its.machine.RPCResult;
import com.its.machine.SystemState;
import com.its.outer.OuterService;
import com.its.util.TaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by lidapeng on 2017/8/10.
 */

/**
 * leader拷贝log entry到其他机器
 */
class CopyLogEntryTask implements Callable<RPCResult> {
    int idx;//任务序号(与MachineInfo的addrs数组的下标对应)
    AppendRPCParam appendRPCParam;

    public CopyLogEntryTask(int idx, AppendRPCParam param) {
        this.idx = idx;
        this.appendRPCParam = param;
    }

    @Override
    public RPCResult call() throws Exception {
        //获取别人提供的服务
        OuterService outerService = RPCFramework.getService(OuterService.class, MachineInfo.addrs[idx], MachineInfo.ports[idx]);
        //拷贝log entry到其他follower
        RPCResult res = outerService.copyEntryToOthers(appendRPCParam);
        return res;
    }
}

public class AddEntriesServiceImpl implements AddEntriesService {
    List<Future<RPCResult>> futureArr = new ArrayList<>();
    List<RPCResult> resArr = new ArrayList<>();

    @Override
    public List<RPCResult> copyLogEntryToFollowers(AppendRPCParam appendRPCParam) {
        ExecutorService exec = Executors.newFixedThreadPool(MachineInfo.nodeCount);
        for (int i = 0; i < MachineInfo.nodeCount; i++) {
            Future<RPCResult> res = exec.submit(new CopyLogEntryTask(i, appendRPCParam));
            futureArr.add(res);
        }
        //取线程的运行结果.
        for (int i = 0; i < futureArr.size(); i++) {
            Future<RPCResult> future = futureArr.get(i);
            RPCResult tem = null;
            try {
                tem = future.get(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
                System.out.println("复制log entry到follower, rpc调用超时");
            }
            if (tem != null) {
                resArr.add(tem);
                SystemState.failIdx.add(i);
            }
        }
        exec.shutdown();
        return resArr;
    }

    @Override
    public boolean copyLogEntryToFollower(int idx, AppendRPCParam appendRPCParam) {
        boolean res = false;
        Future<RPCResult> future = TaskExecutor.submitTask(new CopyLogEntryTask(idx, appendRPCParam));
        try {
            RPCResult rpcResult = future.get(50, TimeUnit.MILLISECONDS);
            res = rpcResult.isCopy;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        return res;
    }
}
