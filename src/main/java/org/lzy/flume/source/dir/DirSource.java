package org.lzy.flume.source.dir;


import com.google.common.base.Preconditions;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;
import org.lzy.flume.source.ftp.source.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author Luis Lázaro <lalazaro@keedio.com>
 */

public class DirSource extends AbstractSource implements Configurable, PollableSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(DirSource.class);

    private SourceUtils sourceUtils;
    private static final Logger log = LoggerFactory.getLogger(DirSource.class);
    private HashMap<File, Long> sizeFileList = new HashMap<>();
    private HashMap<File, Long> markFileList = new HashMap<>();
    private HashMap<File, Boolean> channelList = new HashMap<>();
    private HashSet<File> deletedFiles = new HashSet<>();
    private int chunkSize = 1024;


    private String inputBatchPath;
    private String table2fieldSize;
    public static Map<String,Integer> table2fieldSizeMap=new HashMap<String,Integer>();

    private String excelPath;
    private String csvPath;
    private List fileTypeList=new ArrayList<String>(Arrays.asList("GSM-SITE","GSM-CELL","WCDMA-SITE","WCDMA-CELL","LTE-SITE","LTE-CELL","PHYSICS","GSM-SCENE","WCDMA-SCENE","LTE-SCENE"));


    @Override
    public void configure(Context context) {
        setSourceUtils(new SourceUtils(context));
        log.info("Reading and processing configuration values for source " + getName());
        log.info("Loading previous flumed data.....  " + getName());
        try {
            setSizeFileList(loadMap("/software/flume-ng/tools/ser/1hasmapS.ser"));
            setMarkFileList(loadMap("/software/flume-ng/tools/ser/2hasmapS.ser"));
            setChannelList(loadMapB("/software/flume-ng/tools/ser/hasmapB.ser"));
            closeChannel(getChannelList());

        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            log.error("Fail to load previous flumed data.");
        }

        //获取每张表的对应长度
        table2fieldSize = context.getString("table2fieldSize");
        Preconditions.checkNotNull(table2fieldSize, "table2fieldSize must be set!!");
        inputBatchPath = context.getString("inputBatchPath");
        Preconditions.checkNotNull(inputBatchPath, "inputBatchPath must be set!!");

        excelPath = context.getString("excelPath");
        Preconditions.checkNotNull(excelPath, "excelPath must be set!!");
//        csvPath = context.getString("working.directory");
//        Preconditions.checkNotNull(csvPath, "csvPath must be set!!");
    }

    /*
    @enum Status , process source configured from context
    */
    public PollableSource.Status process() throws EventDeliveryException {

        discoverElements();
        try {
            Thread.sleep(getSourceUtils().getRunDiscoverDelay());
            return PollableSource.Status.READY;     //source was successfully able to generate events
        } catch (InterruptedException inte) {
            inte.printStackTrace();
            return PollableSource.Status.BACKOFF;   //inform the runner thread to back off for a bit
        }
    }




    public void start(Context context) {
        log.info("Starting sql source {} ...", getName());
        super.start();
    }


    @Override
    public void stop() {
        saveMap(getSizeFileList(), 1);
        saveMap(getMarkFileList(), 2);
        saveMapB(getChannelList());
        log.info("Stopping sql source {} ...", getName());
        super.stop();
    }


    /*
    @void process last append to files
    */
    public void processMessage(byte[] lastInfo,String fileName, String filePath) {
        byte[] message = lastInfo;
        Event event = new SimpleEvent();
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("fileName", fileName);
        headers.put("filePath", filePath);
        headers.put("timestamp", String.valueOf(System.currentTimeMillis()));
        //发送文件标志
        String flag=fileName.split("_")[0].toUpperCase();
        headers.put("flag",flag);
        event.setBody(message);
        event.setHeaders(headers);
        try {
            getChannelProcessor().processEvent(event);
        } catch (ChannelException e) {
            LOGGER.error("ChannelException", e);
        }
    }


    /*
    @void retrieve files from directories
    */
    public void discoverElements() {
        try {
            Path start = Paths.get(getSourceUtils().getFolder());

            Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, BasicFileAttributes attributes) throws IOException {

                    cleanList(getSizeFileList());
                    cleanListMirror(getMarkFileList());
                    if (getSizeFileList().containsKey(file.toFile())) {                              // known file
                        final RandomAccessFile ranAcFile = new RandomAccessFile(file.toFile(), "r");
                        ranAcFile.seek(getSizeFileList().get(file.toFile()));
                        long size = ranAcFile.length() - getSizeFileList().get(file.toFile());
                        if (size > 0) {
                            getSizeFileList().put(file.toFile(), ranAcFile.length());
                            log.info("Modified: " + file.getFileName() + " ," + getSizeFileList().size());
                            ReadFileWithFixedSizeBuffer(ranAcFile, file.toFile());


                        } else if (size == 0) { //known & NOT modified
                            if (getMarkFileList().get(file.toFile()) < ranAcFile.length()) {
                                if (getChannelList().get(file.toFile())) {
                                    log.info("channel open: " + file.getFileName());
                                    ranAcFile.close();
                                } else {
                                    log.info("channel closed: " + file.getFileName());

                                    Thread threadReFile = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                getChannelList().put(file.toFile(), true);
                                                final RandomAccessFile ranAcFile = new RandomAccessFile(file.toFile(), "r");
                                                ranAcFile.seek(getMarkFileList().get(file.toFile()));
                                                ReadFileWithFixedSizeBuffer(ranAcFile, file.toFile());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    threadReFile.setName("hiloReFile_" + file.getFileName());
                                    threadReFile.start();

                                }
                            }
                            ranAcFile.close();
                        } else if (size < 0) { //known &  modified from offset 0
                            ranAcFile.seek(0);
                            getSizeFileList().put(file.toFile(), ranAcFile.length());
                            log.info("full modified: " + file.getFileName() + "," + attributes.fileKey() + " ," + getSizeFileList().size());
                            ReadFileWithFixedSizeBuffer(ranAcFile, file.toFile());
                        }

                    } else {    //new File
                        final RandomAccessFile ranAcFile = new RandomAccessFile(file.toFile(), "r");
                        getSizeFileList().put(file.toFile(), ranAcFile.length());
                        getChannelList().put(file.toFile(), true);
                        log.info("discovered: " + file.getFileName() + " ," + getSizeFileList().size());
                        Thread threadNewFile = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ReadFileWithFixedSizeBuffer(ranAcFile, file.toFile());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        threadNewFile.setName("hiloNewFile_" + file.getFileName());
                        threadNewFile.start();

                    }

                    return FileVisitResult.CONTINUE;

                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
    @void, delete file from hashmaps if deleted from ftp
    */
    public void cleanList(HashMap<File, Long> map) {
        for (Iterator<File> iter = map.keySet().iterator(); iter.hasNext(); ) {
            final File file = iter.next();
            if (!(file.exists())) {
                iter.remove();
                getDeletedFiles().add(file);
            }
        }
    }

    /*
    @void. Avoid double concurrent access
    */
    public void cleanListMirror(HashMap<File, Long> map) {
        for (File file : getDeletedFiles()) {
            map.remove(file);
        }
    }

    /*
    @void Serialize hashmap
    */
    public void saveMap(HashMap<File, Long> map, int num) {
        try {
            FileOutputStream fileOut = new FileOutputStream(num + "hasmapS.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    /*
    @void Serialize hashmap
    */

    public void saveMapB(HashMap<File, Boolean> map) {
        try {
            FileOutputStream fileOut = new FileOutputStream("hasmapB.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(map);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
    @return HashMap<File,Long> objects
    */
    public HashMap<File, Long> loadMap(String name) throws ClassNotFoundException, IOException {
        FileInputStream map = new FileInputStream(name);
        ObjectInputStream in = new ObjectInputStream(map);
        HashMap hasMap = (HashMap) in.readObject();
        in.close();
        return hasMap;

    }


    /*
    @return HashMap<File,Long> objects
    */
    public HashMap<File, Boolean> loadMapB(String name) throws ClassNotFoundException, IOException {
        FileInputStream map = new FileInputStream(name);
        ObjectInputStream in = new ObjectInputStream(map);
        HashMap hasMap = (HashMap) in.readObject();
        in.close();
        return hasMap;

    }

    /*
    @void, Read a large file in chunks with fixed size buffer and process chunk
    */
//    public void ReadFileWithFixedSizeBuffer(RandomAccessFile aFile) throws IOException {
//        FileChannel inChannel = aFile.getChannel();
//        ByteBuffer buffer = ByteBuffer.allocateDirect(getChunkSize());
//        while (inChannel.read(buffer) > 0) {
//            FileLock lock = inChannel.lock(inChannel.position(), getChunkSize(), true);
//            byte[] data = new byte[getChunkSize()];
//            buffer.flip(); //alias for buffer.limit(buffer.position()).position(0)
//            for (int i = 0; i < buffer.limit(); i++) {
//                data[i] = buffer.get();
//            }
//            processMessage(data);
//            buffer.clear(); // sets the limit to the capacity and the position back to 0
//            lock.release();
//        }
//        inChannel.close();
//        aFile.close();
//    }


    /*
    @void, Read a large file in chunks with fixed size buffer and process chunk
    */
    public void ReadFileWithFixedSizeBuffer(RandomAccessFile aFile, File file) throws IOException {
        FileChannel inChannel = aFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocateDirect(getChunkSize());
        while (inChannel.read(buffer) > 0) {
            getMarkFileList().put(file, inChannel.position());
            FileLock lock = inChannel.lock(inChannel.position(), getChunkSize(), true);
            byte[] data = new byte[getChunkSize()];
            buffer.flip(); //alias for buffer.limit(buffer.position()).position(0)
            for (int i = 0; i < buffer.limit(); i++) {
                data[i] = buffer.get();
            }
            processMessage(data,file.getName(),file.getPath());
            buffer.clear(); // sets the limit to the capacity and the position back to 0
            lock.release();
        }

        getChannelList().put(file, false);
        inChannel.close();
        aFile.close();
    }


    /*
    @return void, if any channel persists opened must be closed of reboot process
    */
    public void closeChannel(HashMap<File, Boolean> map) {
        for (Iterator<File> iter = map.keySet().iterator(); iter.hasNext(); ) {
            final File file = iter.next();
            if (map.get(file)) {
                map.put(file, Boolean.FALSE);
            }
        }
    }

    /**
     * @return the sourceUtils
     */
    public SourceUtils getSourceUtils() {
        return sourceUtils;
    }

    /**
     * @param sourceUtils the sourceUtils to set
     */
    public void setSourceUtils(SourceUtils sourceUtils) {
        this.sourceUtils = sourceUtils;
    }

    /**
     * @return the sizeFileList
     */
    public HashMap<File, Long> getSizeFileList() {
        return sizeFileList;
    }

    /**
     * @param sizeFileList the sizeFileList to set
     */
    public void setSizeFileList(HashMap<File, Long> sizeFileList) {
        this.sizeFileList = sizeFileList;
    }

    /**
     * @return the markFileList
     */
    public HashMap<File, Long> getMarkFileList() {
        return markFileList;
    }

    /**
     * @param markFileList the markFileList to set
     */
    public void setMarkFileList(HashMap<File, Long> markFileList) {
        this.markFileList = markFileList;
    }

    /**
     * @return the channelList
     */
    public HashMap<File, Boolean> getChannelList() {
        return channelList;
    }

    /**
     * @param channelList the channelList to set
     */
    public void setChannelList(HashMap<File, Boolean> channelList) {
        this.channelList = channelList;
    }

    /**
     * @return the deletedFiles
     */
    public HashSet<File> getDeletedFiles() {
        return deletedFiles;
    }

    /**
     * @param deletedFiles the deletedFiles to set
     */
    public void setDeletedFiles(HashSet<File> deletedFiles) {
        this.deletedFiles = deletedFiles;
    }

    /**
     * @return the chunkSize
     */
    public int getChunkSize() {
        return chunkSize;
    }

    /**
     * @param chunkSize the chunkSize to set
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
    @Override
    public long getBackOffSleepIncrement() {
        return 0;
    }

    @Override
    public long getMaxBackOffSleepInterval() {
        return 0;
    }
}

