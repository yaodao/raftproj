package com.its.service;

import com.its.machine.AppendRPCParam;
import com.its.machine.MachineInfo;
import com.its.machine.RPCFramework;
import com.its.machine.RPCResult;
import com.its.outer.OuterService;

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
 * Created by lidapeng on 2017/8/9.
 */

/**
 * 获取所有follower心跳的响应的任务
 */
class GetHeartBeatTask implements Callable<RPCResult> {
    int idx;//任务序号(与MachineInfo的addrs数组的下标对应)
    AppendRPCParam appendRPCParam;

    public GetHeartBeatTask(int idx, AppendRPCParam param) {
        this.idx = idx;
        this.appendRPCParam = param;
    }

    @Override
    public RPCResult call() throws Exception {
        //获取别人提供的服务
        OuterService outerService = RPCFramework.getService(OuterService.class, MachineInfo.addrs[idx], MachineInfo.ports[idx]);
        //要票
        RPCResult rpcResult = outerService.heartBeatEntryForLeader(appendRPCParam);
        return rpcResult;
    }
}

public class HeartBeatServiceImpl implements HeartBeatService {
    List<Future<RPCResult>> futureArr = new ArrayList<>();
    List<RPCResult> resArr = new ArrayList<>();

    @Override
    public List<RPCResult> gatherHeartBeatFromOthers(AppendRPCParam appendRPCParam) {
        ExecutorService exec = Executors.newFixedThreadPool(MachineInfo.nodeCount);
        for (int i = 0; i < MachineInfo.nodeCount; i++) {
            Future<RPCResult> res = exec.submit(new GetHeartBeatTask(i, appendRPCParam));
            futureArr.add(res);
        }
        //取线程的运行结果.
        for (Future<RPCResult> future : futureArr) {
            RPCResult tem = null;
            try {
                tem = future.get(50, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
                System.out.println("心跳rpc 调用超时");
            }
            if (tem != null) {
                resArr.add(tem);
            }
        }
        exec.shutdown();
        return resArr;
    }
}
