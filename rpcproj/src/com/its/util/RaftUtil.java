package com.its.util;

import com.its.machine.VoteRPCParam;

import java.util.Random;

/**
 * Created by lidapeng on 2017/8/4.
 */
public class RaftUtil {

    private static volatile boolean resetFlag = false;
    //随机的倒计时时间
    public static volatile int interval = getNumber();

    public static void setResetFlag(boolean resetFlag) {
        RaftUtil.resetFlag = resetFlag;
    }

    public static boolean isResetFlag() {
        return resetFlag;
    }

    /*
       产生[150,300)之间的整数
    */
    public static int getNumber() {
        Random r = new Random();
        return r.nextInt(150) + 150;
    }

    /*
    是否超时(单位ms)
     */
    public static boolean isTimeout() {
        int num = getNumber();
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        while (endTime - startTime < num) {
            endTime = System.currentTimeMillis();
        }
        return true;
    }

    /**
     * 按给定的参数进行倒计时
     *
     * @param num 计时时间(ms)
     */
    public static boolean timerWithInterval(int num) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        while (endTime - startTime < num) {
            endTime = System.currentTimeMillis();
        }
        return true;
    }

    /**
     * 计时器重新开始倒计时(给resetFlag赋值为true一次,重置一次计数器)
     *
     * @param num 计时时间(ms)
     */
    public static boolean timerCanReset(int num) {
        long startTime = System.currentTimeMillis();
        long originalTime = startTime;
        long endTime = startTime;
        while (endTime - startTime < num) {
            if (resetFlag) {
                System.out.println("timer is reset");
                startTime = endTime;
                resetFlag = false;
            }
            endTime = System.currentTimeMillis();
        }
        System.out.println("endTime - originalTime " + (endTime - originalTime));
        return true;
    }


    public static void main(String[] args) throws InterruptedException {
//        while (true) {
//            System.out.println(RaftUtil.getNumber());
//        }
//        System.out.println(RaftUtil.isTimeout());
//
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                RaftUtil.resetFlag = true;
//            }
//        }).start();
//
//        timerCanReset(10000);

        System.out.println(RaftUtil.getNumber());
        System.out.println(RaftUtil.interval);
        System.out.println(RaftUtil.interval);
        System.out.println(RaftUtil.interval);
    }
}
//    public static void timerCanReset(int num) {
//        long startTime = System.currentTimeMillis();
//        long origin = startTime;
//        long endTime = startTime;
//        while (endTime - startTime < num) {
//            if (resetFlag) {
//                System.out.println(endTime - startTime);
//                startTime = endTime;
//                resetFlag = false;
//            }
//            endTime = System.currentTimeMillis();
//        }
//        System.out.println(endTime - origin);
//    }