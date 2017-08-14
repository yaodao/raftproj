package com.its.util;

import com.its.machine.AppendRPCParam;
import com.its.machine.MachineInfo;
import com.its.machine.RPCResult;
import com.its.machine.SystemState;
import com.its.machine.VoteRPCParam;

import java.util.Arrays;
import java.util.List;

/**
 * Created by lidapeng on 2017/8/8.
 */
public class RaftObjectUtil {


    public static VoteRPCParam buildVoteRPCParam(String candidateId, Integer term, Integer lastLogIndex,
                                                 Integer lastLogTerm) {
        VoteRPCParam voteParam = new VoteRPCParam();
        voteParam.candidateId = candidateId;
        voteParam.term = term;
        voteParam.lastLogIndex = lastLogIndex;
        voteParam.lastLogTerm = lastLogTerm;
        return voteParam;
    }

    /**
     * 该函数返回一个 拉票时,发送给其它server的参数对象.
     *
     * @return
     */
    public static VoteRPCParam buildDefaultVoteRPCParam() {
        VoteRPCParam voteParam = new VoteRPCParam();
        voteParam.candidateId = SystemState.myID;
        voteParam.term = SystemState.currentTerm;//currentTerm的值是已有的term值加1
        voteParam.lastLogIndex = SystemState.commitIndex;
        voteParam.lastLogTerm = SystemState.currentTerm - 1;
        return voteParam;
    }

    /**
     * 该函数返回一个对象,用于leader发送心跳时传送.
     * @return
     */
    public static AppendRPCParam buildAppendRPCParamForHeartBeat() {
        AppendRPCParam appendRPCParam = new AppendRPCParam();
        appendRPCParam.term = SystemState.currentTerm;
        appendRPCParam.leaderId = SystemState.myID;
        appendRPCParam.prevLogIndex = SystemState.lastApplied - 1;
        appendRPCParam.prevLogTerm = SystemState.currentTerm - 1;
        appendRPCParam.logEntries = null;
        appendRPCParam.leaderCommit = SystemState.commitIndex;
        return appendRPCParam;
    }

    /**
     * 该函数返回一个对象,用于leader拷贝 新的 log entry给其它follower
     * @return
     */
    public static AppendRPCParam buildAppendRPCParamForCopyLogEntry(){
        AppendRPCParam appendRPCParam = new AppendRPCParam();
        appendRPCParam.term = SystemState.currentTerm;
        appendRPCParam.leaderId = SystemState.myID;
        appendRPCParam.prevLogIndex = SystemState.commitIndex - 1;
        appendRPCParam.prevLogTerm = SystemState.currentTerm - 1;
        //暂时做成一次拷贝一个log entry
        appendRPCParam.logEntries = Arrays.asList(SystemState.entries.get(SystemState.commitIndex));
        appendRPCParam.leaderCommit = SystemState.commitIndex;
        return appendRPCParam;
    }

    //true leader, false 不是leader
    public static boolean isLeader(List<RPCResult> arr) {
        if (arr == null) {
            return false;
        }
        int counter = 1;//candidate投给自己的一票
        for (RPCResult res : arr) {
            if (res.voteGranted) {
                counter++;
            }
        }
        return counter > MachineInfo.nodeCount / 2;
    }

    public static boolean isCopyToMajority(List<RPCResult> arr){
        if (arr == null) {
            return false;
        }
        int counter = 1;//自己有一份
        for (RPCResult res : arr) {
            if (res.isCopy) {
                counter++;
            }
        }
        return counter > MachineInfo.nodeCount / 2;
    }

    public static void main(String[] args) {
//        List<RPCResult> arr = new ArrayList<>();
        List<RPCResult> arr = null;
        boolean res = isLeader(arr);
        System.out.println(res);
    }
}
