package org.lzy.poi.xlsx2csv.poi.monitor;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.lzy.poi.xlsx2csv.poi.XLSX2CSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
/**
 * Created by taihe on 2018/3/19.
 */
public class FileListener extends FileAlterationListenerAdaptor {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(FileListener.class);
    private String outputPath;
    public FileListener(String outputPath){
        this.outputPath=outputPath;
    };
    public FileListener() {
        super();
        log.info("开始监控》》》》");

    }

    @Override
    public void onStart(FileAlterationObserver observer) {
        super.onStart(observer);
        log.info("正在监控>"+observer.getDirectory().getPath());
    }

    /**
     * 目录创建
     * @param directory
     */
    @Override
    public void onDirectoryCreate(File directory) {
        super.onDirectoryCreate(directory);
    }

    /**
     * 目录修改
     * @param directory
     */
    @Override
    public void onDirectoryChange(File directory) {
        super.onDirectoryChange(directory);
    }

    /**
     * 目录删除
     * @param directory
     */
    @Override
    public void onDirectoryDelete(File directory) {
        super.onDirectoryDelete(directory);
    }

    /**
     * 文件创建
     * @param file
     */
    @Override
    public void onFileCreate(File file) {
        super.onFileCreate(file);
        String fileName=file.getName();
        log.info("新增文件："+fileName);
        String[] fileNameSplits=fileName.split("\\.");
        //如果后缀是xlsx
        if(fileNameSplits[fileNameSplits.length-1].equals("xlsx")) {
            String outputFileName = fileNameSplits[0] + ".csv";
            String outputFile =outputPath + outputFileName;
            //如果有文件创建，那么将其转换为
            try {
                XLSX2CSV.saveToCSV(file, outputFile);
            } catch (OpenXML4JException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
        else if (fileNameSplits[fileNameSplits.length-1].toLowerCase().equals("csv")){
            String outputFileName =outputPath + fileName.split("\\.")[0]+".process";
            File outputFileLast=new File(outputPath + fileName);
            File outputFile=new File(outputFileName);
            FileInputStream ins = null;
            FileOutputStream out=null;
            try {
                ins = new FileInputStream(file);
                 out = new FileOutputStream(outputFile);
                byte[] b = new byte[1024];
                int n=0;
                while((n=ins.read(b ))!=-1){
                    out.write(b, 0, n);
                }
                outputFile.renameTo(outputFileLast);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    ins.close();
                out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //文件修改
    @Override
    public void onFileChange(File file) {
        super.onFileChange(file);
    }

    /**
     * 文件删除
     * @param file
     */
    @Override
    public void onFileDelete(File file) {
        super.onFileDelete(file);
        File dir=new File(outputPath);
//        File TrxFiles[] = dir.listFiles();
//        for(File curFile:TrxFiles ){
//            curFile.delete();
//        }
        String[] fileNameSplits=file.getName().split("\\.");

        if(fileNameSplits[fileNameSplits.length-1].equals("xlsx")) {
            String outputFileName = fileNameSplits[0] + ".csv";
            String outputFile = outputPath + outputFileName;
            File deleteFile=new File(outputFile);
            deleteFile.delete();
        }else{
            File deleteFile=new File(outputPath+file.getName());
            deleteFile.delete();
        }
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        super.onStop(observer);
    }
//    public static void main(String[] args)  {
//        String dir="E:/input";
//    //轮询时间
//        long interval= TimeUnit.SECONDS.toMillis(1);
//        FileAlterationObserver observer=new FileAlterationObserver(dir);
//        observer.addListener(new FileListener());
//        //创建文件变化监听器
//        FileAlterationMonitor monitor=new FileAlterationMonitor(interval,observer);
//        //开始监控
//        try {
//            monitor.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    }
