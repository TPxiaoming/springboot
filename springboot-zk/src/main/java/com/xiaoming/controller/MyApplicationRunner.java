package com.xiaoming.controller;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动后执行的方法
 * 1.项目启动的时候会在 zookeeper 上创建一个相同的临时节点
 * 2.谁能够创建成功谁就是主服务器
 * 3.使用服务监听节点是否被删除，如果接收到节点被删除的话，重新开始选举（重新开始创建节点）
 * @author xiaoming
 * @Date 2019/11/21
 */
@Component
public class MyApplicationRunner implements ApplicationRunner {
    private ZkClient zkClient = new ZkClient("127.0.0.1:2181");
    private String path = "/election";
    @Value("${server.port}")
    private String serverPort;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("项目启动成功。。。");
        createEphemeral();

        zkClient.subscribeDataChanges(path, new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            /**
             * 节点被删除后
             * @param s
             * @throws Exception
             */
            @Override
            public void handleDataDeleted(String s) throws Exception {
                System.out.println("开始重新选举策略。。。");
                createEphemeral();
            }
        });
    }

    public void createEphemeral(){
        try {
            zkClient.createEphemeral(path);
            System.out.println("serverPort:" + serverPort + "，选举成功。。。");
            ElectionMaster.isSurvival = true;
        } catch (RuntimeException e) {
            System.out.println("该节点已经存在");
            ElectionMaster.isSurvival = false;
        }
    }
}
