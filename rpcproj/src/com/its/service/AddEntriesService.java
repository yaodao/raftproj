package com.its.service;

import com.its.machine.AppendRPCParam;
import com.its.machine.RPCResult;

import java.util.List;

/**
 * Created by lidapeng on 2017/8/10.
 */
public interface AddEntriesService {

    /**
     * leader拷贝log entry给所有follower
     * @param appendRPCParam
     * @return
     */
    List<RPCResult> copyLogEntryToFollowers(AppendRPCParam appendRPCParam);

    /**
     * leader拷贝log entry给单个follower(该函数主要用于对单台机器的重发)
     * @param idx 机器序号(也就是需要重发的那台机器)
     * @param appendRPCParam
     * @return true 拷贝成功,false 拷贝失败
     */
    boolean copyLogEntryToFollower(int idx,AppendRPCParam appendRPCParam);
}
