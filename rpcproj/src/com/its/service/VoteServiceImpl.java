package com.its.service;


import com.its.machine.MachineInfo;
import com.its.machine.RPCFramework;
import com.its.machine.RPCResult;
import com.its.machine.SystemState;
import com.its.machine.VoteRPCParam;
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
 * Created by lidapeng on 2017/8/4.
 */

/**
 * 跟其他server要票的任务
 */
class GetVoteTask implements Callable<RPCResult> {
    int idx;
    VoteRPCParam voteRPCParam;

    public GetVoteTask(int idx, VoteRPCParam voteRPCParam) {
        this.idx = idx;
        this.voteRPCParam = voteRPCParam;
    }

    @Override
    public RPCResult call() throws Exception {
        //获取别人提供的服务
        OuterService outerService = RPCFramework.getService(OuterService.class, MachineInfo.addrs[idx], MachineInfo.ports[idx]);
        //要票
        RPCResult rpcResult = outerService.requestForVoteFromOther(voteRPCParam);
        return rpcResult;
    }
}

public class VoteServiceImpl implements VoteService {
    List<Future<RPCResult>> futureArr = new ArrayList<>();
    List<RPCResult> resArr = new ArrayList<>();

    @Override
    public boolean voteForMyself() {
        SystemState.votedFor = SystemState.myID;
        return true;
    }

    @Override
    public boolean voteForOther(VoteRPCParam voteRPCParam) throws Exception {
        return false;
    }

    @Override
    public List<RPCResult> gatherVoteResultFromOthers(VoteRPCParam voteRPCParam) {
        ExecutorService exec = Executors.newFixedThreadPool(MachineInfo.nodeCount);
        for (int i = 0; i < MachineInfo.nodeCount; i++) {
            Future<RPCResult> res = exec.submit(new GetVoteTask(i, voteRPCParam));
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
                System.out.println("拉票rpc 调用超时");
            }
            if (tem != null) {
                resArr.add(tem);
            }
        }
        exec.shutdown();
        return resArr;
    }

    @Override
    public void prepareGatherVoteFromOthers() {
        SystemState.role = 2;
        SystemState.currentTerm += 1;
        voteForMyself();
    }
}