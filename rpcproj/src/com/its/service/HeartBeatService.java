package com.its.service;

import com.its.machine.AppendRPCParam;
import com.its.machine.RPCResult;

import java.util.List;

/**
 * Created by lidapeng on 2017/8/9.
 */
public interface HeartBeatService {
    List<RPCResult> gatherHeartBeatFromOthers(AppendRPCParam appendRPCParam);
}
