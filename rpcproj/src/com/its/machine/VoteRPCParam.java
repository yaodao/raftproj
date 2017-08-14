package com.its.machine;

/**
 * Created by lidapeng on 2017/8/4.
 */
public class VoteRPCParam {
    public Integer term;
    public String candidateId;//先使用IP表示
    public Integer lastLogIndex;
    public Integer lastLogTerm;
}
