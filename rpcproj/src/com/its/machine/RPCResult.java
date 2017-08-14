package com.its.machine;

/**
 * Created by lidapeng on 2017/8/4.
 */
public class RPCResult {
    /*
    term编号
     */
    public Integer term;
    /*
    log entry的编号是否匹配上.(包括preindex和preterm的匹配)
     */
    public Boolean isSuccess;

    /*
    接收方是否投票; true 投,false 不投.
     */
    public Boolean voteGranted;

    /**
     * follower是否将leader发来的log entry存入自己的数组中.
     */
    public Boolean isCopy;
}
