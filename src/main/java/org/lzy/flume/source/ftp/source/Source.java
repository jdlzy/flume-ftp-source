/*
 * KEEDIO
 */
package org.lzy.flume.source.ftp.source;

import java.io.*;
import java.util.*;

import com.google.common.base.Preconditions;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.lzy.flume.source.ftp.client.filters.KeedioFileFilter;
import org.lzy.flume.source.ftp.source.utils.PropertiesUtils;
import org.lzy.poi.xlsx2csv.poi.monitor.FileMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flume.ChannelException;

import org.lzy.flume.source.ftp.source.utils.FTPSourceEventListener;

import org.lzy.flume.source.ftp.metrics.SourceCounter;

import java.util.List;

import org.lzy.flume.source.ftp.client.factory.SourceFactory;
import org.lzy.flume.source.ftp.client.KeedioSource;

import java.nio.charset.Charset;

import org.apache.flume.source.AbstractSource;

/**
 * @author luislazaro lalazaro@keedio.com - KEEDIO
 */
public class Source extends AbstractSource implements Configurable, PollableSource {

    private SourceFactory sourceFactory = new SourceFactory();
    private KeedioSource keedioSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(Source.class);
    private static final short ATTEMPTS_MAX = 3; //  max limit attempts reconnection
    private static final long EXTRA_DELAY = 10000;
    private int counterConnect = 0;
    private FTPSourceEventListener listener = new FTPSourceEventListener();
    private SourceCounter sourceCounter;
    private String workingDirectory;
    private KeedioFileFilter keedioFileFilter;

    private String inputBatchPath;
    private String table2fieldSize;
    public static Map<String,Integer> table2fieldSizeMap=new HashMap<String,Integer>();

    private String excelPath;
    private String csvPath;
    private List fileTypeList=new ArrayList<String>(Arrays.asList("GSM-SITE","GSM-CELL","WCDMA-SITE","WCDMA-CELL","LTE-SITE","LTE-CELL","PHYSICS","GSM-SCENE","WCDMA-SCENE","LTE-SCENE"));
    /**
     * Request keedioSource to the factory
     *
     * @param context
     * @return KeedioSource
     */
    public KeedioSource orderKeedioSource(Context context) {
        keedioSource = sourceFactory.createKeedioSource(context);
        return keedioSource;
    }

    /**
     * @param context
     */
    @Override
    public void configure(Context context) {
        keedioSource = orderKeedioSource(context);
        if (keedioSource.existFolder()) {
            keedioSource.makeLocationFile();
        } else {
            LOGGER.error("Folder " + keedioSource.getPathTohasmap().toString() + " not exists");
        }



        keedioSource.connect();
        sourceCounter = new SourceCounter("SOURCE." + getName());
        workingDirectory = keedioSource.getWorkingDirectory();
        keedioFileFilter = new KeedioFileFilter(keedioSource.getKeedioFilterRegex());
        keedioSource.checkPreviousMap();
        //获取每张表的对应长度
        table2fieldSize = context.getString("table2fieldSize");
        Preconditions.checkNotNull(table2fieldSize, "table2fieldSize must be set!!");
        inputBatchPath = context.getString("inputBatchPath");
        Preconditions.checkNotNull(inputBatchPath, "inputBatchPath must be set!!");

        excelPath = context.getString("excelPath");
        Preconditions.checkNotNull(excelPath, "excelPath must be set!!");
        csvPath = context.getString("working.directory");
        Preconditions.checkNotNull(csvPath, "csvPath must be set!!");
    }

    /**
     * @return Status , process source configured from context
     * 返回一个源数据的处理状态
     * @throws org.apache.flume.EventDeliveryException
     */
    @Override
    public PollableSource.Status process() throws EventDeliveryException {

        try {
            if (workingDirectory == null) {
                LOGGER.info("property workdir is null, setting to default");
                workingDirectory = keedioSource.getDirectoryserver();
            }

            LOGGER.info("当前监控:  " + workingDirectory + " 文件数量: "
                    + keedioSource.getFileList().size());

            discoverElements(keedioSource, workingDirectory, "", 0);
            keedioSource.cleanList(); //clean list according existing actual files
            keedioSource.getExistFileList().clear();
        } catch (IOException e) {
            LOGGER.error("Exception thrown in proccess, try to reconnect " + counterConnect, e);

            if (!keedioSource.connect()) {
                counterConnect++;
            } else {
                keedioSource.checkPreviousMap();
            }

            if (counterConnect < ATTEMPTS_MAX) {
                process();
            } else {
                LOGGER.error("Server connection closed without indication, reached limit reconnections " + counterConnect);
                try {
                    Thread.sleep(keedioSource.getRunDiscoverDelay() + EXTRA_DELAY);
                    counterConnect = 0;
                } catch (InterruptedException ce) {
                    LOGGER.error("InterruptedException", ce);
                }
            }
        }
        keedioSource.saveMap();

        try {
            Thread.sleep(keedioSource.getRunDiscoverDelay());
            return PollableSource.Status.READY;     //source was successfully able to generate events
        } catch (InterruptedException inte) {
            LOGGER.error("Exception thrown in process while putting to sleep", inte);
            return PollableSource.Status.BACKOFF;   //inform the runner thread to back off for a bit
        }
    }

    /**
     * @return void
     */
    @Override
    public synchronized void start() {
        LOGGER.info("监控ftp文件开始 ...", this.getName());

        LOGGER.info("Source {} starting. Metrics: {}", getName(), sourceCounter);
        super.start();
        sourceCounter.start();
        FileMonitor.moveFile(excelPath,csvPath);
        String[] table2fieldSizes=table2fieldSize.split(",");
        for(String t2f:table2fieldSizes){
            String[] splited=t2f.split("\\|");
            table2fieldSizeMap.put(splited[0],Integer.valueOf(splited[1]));
        }
//        for (Map.Entry<String, Integer> entry : table2fieldSizeMap.entrySet()) {
//            System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
//        }
    }

    /**
     * @return void
     */
    @Override
    public synchronized void stop() {
        keedioSource.saveMap();
        if (keedioSource.isConnected()) {
            keedioSource.disconnect();
        }
        sourceCounter.stop();
        super.stop();
    }

    /**
     * discoverElements: find files to process them
     * 发现文件并处理其
     *
     * @param <T>
     * @param keedioSource
     * @param parentDir,   will be the directory retrieved by the server when
     *                     connected
     * @param currentDir,  actual dir in the recursive method
     * @param level,       deep to search
     * @throws IOException
     */
    // @SuppressWarnings("UnnecessaryContinue")
    public <T> void discoverElements(KeedioSource keedioSource, String parentDir, String currentDir, int level) throws
            IOException {

    long position = 0L;
//文件目录
        String dirToList = parentDir;
        if (!("").equals(currentDir)) {
            dirToList += "/" + currentDir;
        }
        List<T> list = keedioSource.listElements(dirToList, keedioFileFilter);
        ZipSecureFile.setMinInflateRatio(-1.0d);
        if (!(list.isEmpty())) {

            for (T element : list) {
                String elementName = keedioSource.getObjectName(element);
                if (elementName.equals(".") || elementName.equals("..")) {
                    continue;
                }
//如果输入路径是一个目录
                if (keedioSource.isDirectory(element)) {
                    LOGGER.info("[" + elementName + "]");
                    keedioSource.changeToDirectory(parentDir);
                    discoverElements(keedioSource, dirToList, elementName, level + 1);
//如果输入路径是一个文件
                } else if (keedioSource.isFile(element)) { //element is a regular file
                    keedioSource.changeToDirectory(dirToList);
                    keedioSource.getExistFileList().add(dirToList + "/" + elementName);  //control of deleted files in server

                    //test if file is new in collection
                    if (!(keedioSource.getFileList().containsKey(dirToList + "/" + elementName))) { //new file
                        sourceCounter.incrementFilesCount(); //include all files, even not yet processed
                        position = 0L;
                        LOGGER.info("新增: " + elementName + " ,文件大小: " + keedioSource.getObjectSize(element)/1024+"KB");
                        PropertiesUtils.modif(inputBatchPath,elementName.split("_")[0]);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else { //known file
                        long prevSize = (long) keedioSource.getFileList().get(dirToList + "/" + elementName);
                        position = prevSize;
                        long dif = keedioSource.getObjectSize(element) - (long) keedioSource.getFileList()
                                .get(dirToList + "/" + elementName);

                        if (dif > 0) {
                            LOGGER.info("Modified: " + elementName + " ,size: " + dif);
                        } else if (dif < 0) { //known and full modified
                            keedioSource.getExistFileList().remove(dirToList + "/" + elementName); //will be rediscovered as new file
                            keedioSource.saveMap();
                            continue;
                        } else {
                            continue;
                        }

                    } //end if known file

                    //common for all regular files
                    InputStream inputStream = null;
                    try {
                        inputStream = keedioSource.getInputStream(element);
//                        ZipSecureFile.setMinInflateRatio(-1.0d);
                        listener.fileStreamRetrieved();

                        if (!readStream(inputStream, position, elementName, dirToList)) {
                            inputStream = null;
                        }

                        boolean success = inputStream != null && keedioSource.particularCommand(); //mandatory if FTPClient
                        if (success) {
                            keedioSource.getFileList().put(dirToList + "/" + elementName, keedioSource.getObjectSize(element));
                            keedioSource.saveMap();

                            if (position != 0) {
                                sourceCounter.incrementCountModProc();
                            } else {
                                sourceCounter.incrementFilesProcCount();
                            }

                            LOGGER  .info("接收处理:  " + elementName + " ,总文件数: " + this.keedioSource.getFileList().size() + "\n");

                        } else {
                            handleProcessError(elementName);
                        }
                    } catch (IOException e) {
                        handleProcessError(elementName);
                        LOGGER.error("Failed retrieving inputStream on discoverElements ", e);
                        continue;
                    }

                    keedioSource.changeToDirectory(dirToList);
//如果输入路径是一个链接
                } else if (keedioSource.isLink(element)) {
                    LOGGER.info(elementName + " is a link of " + this.keedioSource.getLink(element) + " could not retrieve size");
                    keedioSource.changeToDirectory(parentDir);
                    continue;
                } else {
                    LOGGER.info(elementName + " unknown type of file");
                    keedioSource.changeToDirectory(parentDir);
                    continue;
                }
                keedioSource.changeToDirectory(parentDir);

            }
        }
    }

    /**
     * Read retrieved stream from ftpclient into byte[] and process. If
     * flushlines is true the retrieved inputstream will be readed by lines. And
     * the type of file is set to ASCII from KeedioSource.
     *
     * @param inputStream
     * @param position
     * @return boolean
     */
    public boolean readStream(InputStream inputStream, long position, String fileName, String filePath) {
        LOGGER.info("fileName:" + fileName);
        if (inputStream == null) {
            return false;
        }
        String[] fileSuffixs = fileName.split("\\.", -1);
        String fileSuffix = fileSuffixs[fileSuffixs.length - 1].toLowerCase();
        //判断是否有指定前缀
        Boolean isRightFileType=true;
        if(!fileTypeList.contains(fileName.split("_")[0].toUpperCase())){
            isRightFileType=false;

            LOGGER.error("请为["+fileName+"]指定对应的文件前缀=>"+fileTypeList.toString());
        }
        boolean successRead = true;

        if (keedioSource.isFlushLines()) {
            //判断是否为csv
            if (fileSuffix.equals("csv")&&isRightFileType) {
                try {
                    inputStream.skip(position);
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
                        String line = null;
                        int index=0;
                        while ((line = in.readLine()) != null) {
                            index++;
                            if(index==1){continue;}
                            processMessage(line.getBytes(), fileName, filePath);
                        }

                    }
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                    successRead = false;
                }
            }
        } else {

            try {
                inputStream.skip(position);
                int chunkSize = keedioSource.getChunkSize();
                byte[] bytesArray = new byte[chunkSize];
                int bytesRead = -1;
                while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                    try (ByteArrayOutputStream baostream = new ByteArrayOutputStream(chunkSize)) {
                        baostream.write(bytesArray, 0, bytesRead);
                        byte[] data = baostream.toByteArray();
                        processMessage(data, fileName, filePath);
                    }
                }

                inputStream.close();
            } catch (IOException e) {
                LOGGER.error("on readStream", e);
                successRead = false;

            }
        }
        return successRead;
    }

    /**
     * @param lastInfo byte[]
     * @void process last appended data to files
     */
    public void processMessage(byte[] lastInfo, String fileName, String filePath) {
        byte[] message = lastInfo;
        Event event = new SimpleEvent();
        Map<String, String> headers = new HashMap<>();

        headers.put("fileName", fileName);
        headers.put("filePath", filePath);
        headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
        //发送文件标识
        String flag=fileName.split("_")[0].toUpperCase();
        headers.put("flag",flag);
        event.setBody(message);
        event.setHeaders(headers);
        try {
            getChannelProcessor().processEvent(event);
        } catch (ChannelException e) {
            LOGGER.error("ChannelException", e);
        }
        sourceCounter.incrementCountSizeProc(message.length);
        sourceCounter.incrementEventCount();
    }

    /**
     * @param listener
     */

    public void setListener(FTPSourceEventListener listener) {
        this.listener = listener;
    }

    /**
     * @param fileName
     */
    public void handleProcessError(String fileName) {
        LOGGER.info("failed retrieving stream from file, will try in next poll :" + fileName);
        sourceCounter.incrementFilesProcCountError();
    }

    /**
     * @param ftpSourceCounter
     */
    public void setFtpSourceCounter(SourceCounter ftpSourceCounter) {
        this.sourceCounter = ftpSourceCounter;
    }

    /**
     * @return KeedioSource
     */
    public KeedioSource getKeedioSource() {
        return keedioSource;
    }

    @Override
    public long getBackOffSleepIncrement() {
        return 0;
    }

    @Override
    public long getMaxBackOffSleepInterval() {
        return 0;
    }
} //endclass
