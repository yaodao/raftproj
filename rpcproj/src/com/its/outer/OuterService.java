package com.its.outer;

import com.its.machine.AppendRPCParam;
import com.its.machine.LogEntry;
import com.its.machine.RPCResult;
import com.its.machine.VoteRPCParam;

/**
 * Created by lidapeng on 2017/8/6.
 */

/**
 * 对外暴露的服务类
 */
public interface OuterService {
    //todo 注释
    /**
     * 供leader调用的心跳RPC(leader发心跳给我)
     * @param rpcParam
     * @return
     */
    public RPCResult heartBeatEntryForLeader(AppendRPCParam rpcParam);

    //todo 注释
    /**
     * 供其他server调用的 拉票RPC(其他server跟我要票)
     * @param voteRPCParam
     * @return
     */
    public RPCResult requestForVoteFromOther(VoteRPCParam voteRPCParam);

    //todo 注释
    /**
     * 供客户端调用,给leader添加log entry(假设本机就是leader)
     * @return
     */
    public boolean addEntryToLeader(LogEntry rpcParam);

    /**
     * 供leader调用,将leader刚接到的entry发送给每个follower
     * @param rpcParam
     * @return
     */
    public RPCResult copyEntryToOthers(AppendRPCParam rpcParam);
}
