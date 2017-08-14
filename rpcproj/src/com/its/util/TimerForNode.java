package com.its.util;

import com.its.machine.AppendRPCParam;
import com.its.machine.RPCResult;
import com.its.machine.SystemState;
import com.its.machine.VoteRPCParam;
import com.its.service.AddEntriesService;
import com.its.service.AddEntriesServiceImpl;
import com.its.service.HeartBeatService;
import com.its.service.HeartBeatServiceImpl;
import com.its.service.VoteService;
import com.its.service.VoteServiceImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by lidapeng on 2017/8/7.
 */
//负责超时计数的类
public class TimerForNode {

    private static VoteService voteService = new VoteServiceImpl();
    private static HeartBeatService heartBeatService = new HeartBeatServiceImpl();
    private static AddEntriesService addEntriesService = new AddEntriesServiceImpl();

    /**
     * 这个函数需要一直运行,因为它是对自身的一个计时器.
     * (若自己的角色是leader,则停止运行;其他角色,一直运行.)
     */
    public static void startTimer() {
        final boolean runFlag = true;
        while (runFlag) {
            //倒计时(倒计时结束,表示超时没有收到leader的心跳)
            countdownForSelf();

            leaderElect();

            //执行到这,说明已经选出leader(可能是本机,也可能不是,反正是有leader了);
            //这里只需要根据 role=1 执行对自身的倒计时;role=3 执行发送心跳.
            if (SystemState.role == 1) {
                countdownForSelf();
            }
            if (SystemState.role == 3) {
                while (runFlag) {//不停对所有follower发心跳
                    AppendRPCParam param = RaftObjectUtil.buildAppendRPCParamForHeartBeat();
                    heartBeatService.gatherHeartBeatFromOthers(param);
                }
            }
        }
    }


    /**
     * leader拷贝log entry到其他机器
     */
    public static void copyLogEntry() {
        AppendRPCParam appendRPCParam = RaftObjectUtil.buildAppendRPCParamForCopyLogEntry();
        List<RPCResult> resArr = addEntriesService.copyLogEntryToFollowers(appendRPCParam);
        boolean canCommit = RaftObjectUtil.isCopyToMajority(resArr);
        if (canCommit) {
            //更改本机状态.(我这里更改本机状态直接就等价于应用到状态机上了,不再另写应用命令到状态机.)
            SystemState.commitIndex = SystemState.entries.size();
            SystemState.lastApplied = SystemState.commitIndex;
        }

        if (SystemState.failIdx.size() > 0) {
            boolean isRun = true;
            while (isRun) {
                //leader需要不停的发entry给SystemState.failIdx中的机器.
                Iterator<Integer> iterator = SystemState.failIdx.iterator();
                while (iterator.hasNext()) {
                    Integer elem = iterator.next();
                    boolean tem = addEntriesService.copyLogEntryToFollower(elem, appendRPCParam);
                    if (tem == true) {
                        iterator.remove();
                    }
                }
                if (resArr.size() == 0) {
                    isRun = false;
                }
            }
        }
    }


    /**
     * 选举
     * 该函数直到 SystemState.role == 1(follower) 或者SystemState.role == 3(leader)  的时候才会结束
     * 也就是选举出结果之后,该函数会结束,否则一直循环选leader
     */
    private static void leaderElect() {
        //leader elect
        SystemState.role = 2;
        while (SystemState.role != 3 && SystemState.role != 1) {
            voteService.prepareGatherVoteFromOthers();

            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            //倒计时没有结束,则只能接收其他server的拉票信息
            while (endTime - startTime < RaftUtil.interval) {
                //已经成为follower
                if (SystemState.role == 1) {
                    break;
                }
                endTime = System.currentTimeMillis();
            }
            if (SystemState.role == 1) {
                break;
            }

            //倒计时结束,本机可以开始拉票
            if (endTime - startTime >= RaftUtil.interval) {
                //新一轮倒计时
                startTime = System.currentTimeMillis();
                endTime = startTime;
                try {
                    //并发跟其它server要票
                    VoteRPCParam voteRPCParam = RaftObjectUtil.buildDefaultVoteRPCParam();
                    List<RPCResult> voteRes = voteService.gatherVoteResultFromOthers(voteRPCParam);
                    //判断是否被选上leader
                    boolean leaderFlag = RaftObjectUtil.isLeader(voteRes);

                    //// TODO: 2017/8/10 注释
                    // 倒计时时间内,也需要接受其他server的信号
                    // (这里将接受其他server的信号,改为循环判断自身的角色,可以达到相同的效果,
                    // 因为本机通过与其他机器交流信息,会改变自身的角色)
                    while ((endTime - startTime) < RaftUtil.interval) {
                        if (leaderFlag) {
                            SystemState.role = 3;//当选,之后需要发送心跳.
                            break;
                        }
                        if (SystemState.role == 1) {
                            //已经成为follower了,需要接受leader的心跳
                            break;
                        }
                        endTime = System.currentTimeMillis();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //本机角色已经不是candidate
            if (SystemState.role != 2) {
                break;
            }
            //新的一轮选举,需要新产生一个对自己的倒计时时间.
            RaftUtil.interval = RaftUtil.getNumber();
        }
    }

    /**
     * 一个倒计时函数,会阻塞调用线程,直到有结果产生.
     * (该函数所使用的计时器可以被重置,只需要在另一个线程中不断的将resetFlag赋值为true)
     *
     * @return true, 表示计时器超时
     */
    private static boolean countdownForSelf() {
        ExecutorService exec = Executors.newFixedThreadPool(1);
        //启动计时器,对自己进行倒计时
        Future<Boolean> fur = exec.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                System.out.println("本机得到的倒计时 时间: " + RaftUtil.interval);
                //启动可重置的计时器,开始倒计时,(计时期间应该收到心跳,从而被不断的重置)
                return RaftUtil.timerCanReset(RaftUtil.interval);
            }
        });
        exec.shutdown();

        boolean isElect = false;
        try {
            isElect = fur.get();
            //程序执行到这里,说明:直到计时结束也没有收到心跳,可以开始选举
            System.out.println("自身的计时器超时...开始选举");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return isElect;
    }

    private static void setFun() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int counter = 0;
                while (counter < 10) {
                    //调用这一句,就可以让自身的计时器重置.
                    RaftUtil.setResetFlag(true);
                    try {
                        Thread.sleep(100);
//                        System.out.println("sleep 100 " + RaftUtil.isResetFlag());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    counter++;
                }

            }
        }).start();
    }

    public static void main(String[] args) throws InterruptedException {
        TimerForNode.setFun();
        TimerForNode.startTimer();
    }
}
