package com.its.service;

import com.its.machine.RPCResult;
import com.its.machine.VoteRPCParam;

import java.util.List;

/**
 * Created by lidapeng on 2017/8/4.
 */
public interface VoteService {
    boolean voteForMyself();

    boolean voteForOther(VoteRPCParam voteRPCParam) throws Exception;

    List<RPCResult> gatherVoteResultFromOthers(VoteRPCParam voteRPCParam);

    void prepareGatherVoteFromOthers();
}
