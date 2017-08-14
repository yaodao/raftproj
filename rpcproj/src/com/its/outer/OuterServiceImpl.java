package com.its.outer;

import com.its.machine.AppendRPCParam;
import com.its.machine.LogEntry;
import com.its.machine.RPCResult;
import com.its.machine.SystemState;
import com.its.machine.VoteRPCParam;
import com.its.util.RaftUtil;

/**
 * Created by lidapeng on 2017/8/6.
 */

/**
 * //todo 注释
 * 别人调用我的rpc,我返回结果给别人
 */
public class OuterServiceImpl implements OuterService {
    @Override
    public RPCResult heartBeatEntryForLeader(AppendRPCParam rpcParam) {
        //重置自身的计时器
        RaftUtil.setResetFlag(true);
        //返回结果给leader
        return buildRPCResult();
    }

    private RPCResult buildRPCResult() {
        RPCResult res = new RPCResult();
        res.term = SystemState.currentTerm;
        res.isSuccess = true;
        return res;
    }

    @Override
    public RPCResult requestForVoteFromOther(VoteRPCParam voteRPCParam) {
        RPCResult rpcResult = null;
        //不投票给对方
        if (voteRPCParam.term < SystemState.currentTerm) {
            rpcResult = buildRPCResult(SystemState.currentTerm, false);
            return rpcResult;
        }
        //退回到follower状态,并投票给对方(当票还没投出去,或者投给了自己的时候)
        if (voteRPCParam.term >= SystemState.currentTerm &&
                (SystemState.votedFor == null || SystemState.votedFor.equals(SystemState.myID))) {
            SystemState.role = 1;
            //本机的term跟最新的term保持一致
            SystemState.currentTerm = voteRPCParam.term;
            SystemState.votedFor = voteRPCParam.candidateId;
            rpcResult = buildRPCResult(SystemState.currentTerm, true);
            SystemState.currentTerm = voteRPCParam.term;
            return rpcResult;
        }
        //todo 和上面的判断两个之间选择一个,论文说的不清楚,文中用的是上面的判断,表格给的是用下面的判断
        //退回到follower状态,并投票给对方(当票还没投出去,或者投给了自己的时候)
        if (voteRPCParam.lastLogIndex >= SystemState.commitIndex &&
                (SystemState.votedFor == null || SystemState.votedFor.equals(SystemState.myID))) {
            SystemState.role = 1;
            SystemState.votedFor = voteRPCParam.candidateId;
            rpcResult = buildRPCResult(SystemState.currentTerm, true);
            SystemState.currentTerm = voteRPCParam.term;
            return rpcResult;
        }

        return rpcResult;
    }

    private RPCResult buildRPCResult(Integer term, Boolean voteGranted) {
        RPCResult rpcResult = new RPCResult();
        rpcResult.voteGranted = voteGranted;
        rpcResult.term = term;
        return rpcResult;
    }

    /**
     * 将log entry复制给大多数机器,并且应用到自己的状态机上,才算成功
     *
     * @param rpcParam
     * @return
     */
    @Override
    public boolean addEntryToLeader(LogEntry rpcParam) {
        rpcParam.index = SystemState.commitIndex + 1;
        rpcParam.term = SystemState.currentTerm;
        return addEntry(rpcParam);
    }

    private boolean addEntry(LogEntry rpcParam) {
        SystemState.entries.add(rpcParam);
        SystemState.newestIndex.addAndGet(1);
        return false;
    }

    /**
     * 需要对参数进行处理,包括:更新index,term,data值, 是否拒绝entry(preindex和preterm是否匹配)
     *
     * @param rpcParam 这个参数的term和index值,是由调用方给出
     * @return
     */
    @Override
    public RPCResult copyEntryToOthers(AppendRPCParam rpcParam) {
        //preindex和preterm是否match
        if (rpcParam.prevLogIndex.equals(SystemState.commitIndex) && rpcParam.prevLogTerm.equals(SystemState.currentTerm)) {
            if (rpcParam.logEntries != null && rpcParam.logEntries.size() > 0) {
                SystemState.entries.addAll(rpcParam.logEntries);
                SystemState.commitIndex += rpcParam.logEntries.size();
                SystemState.lastApplied += rpcParam.logEntries.size();
                return buildCopyEntryRPCResult(true);
            }
        }
        return buildCopyEntryRPCResult(false);
    }

    private RPCResult buildCopyEntryRPCResult(boolean param) {
        RPCResult res = new RPCResult();
        res.term = SystemState.currentTerm;
        res.isCopy = param;
        return res;
    }
}
