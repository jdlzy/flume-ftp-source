package com.lzy.poi.test;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.lzy.poi.xlsx2csv.poi.monitor.FileListener;
import org.lzy.poi.xlsx2csv.poi.monitor.FileMonitor;

import java.util.concurrent.TimeUnit;

/**
 * Created by taihe on 2018/3/19.
 */
public class monitorListenerTest {
    public static void main(String[] args)  {
        String dir="E:/input/";
        String outputPath="E:/output/";
        FileMonitor.moveFile(dir,outputPath);
//        //轮询时间
//        long interval= TimeUnit.SECONDS.toMillis(5);
//        FileAlterationObserver observer=new FileAlterationObserver(dir);
//        observer.addListener(new FileListener("E:/output/"));
//        //创建文件变化监听器
//        FileAlterationMonitor monitor=new FileAlterationMonitor(interval,observer);
//        //开始监控
//        try {
//            monitor.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
