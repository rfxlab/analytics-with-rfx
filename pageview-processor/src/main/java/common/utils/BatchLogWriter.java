package common.utils;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rfx.core.configs.WorkerConfigs;
import rfx.core.util.CharPool;
import rfx.core.util.DateTimeUtil;
import rfx.core.util.StringPool;

public class BatchLogWriter {

    private static final WorkerConfigs WORKER_CONFIGS = WorkerConfigs.load();
	static final String DAY_NAME_TOKEN = "/day-";
    static final String HOUR_NAME_TOKEN = "/hour-";
    static final String LOG_EXT = ".log";
    static final String BASE_LOG_FOLDER = WORKER_CONFIGS.getCustomConfig("baseRawLogPath").trim();
    static final boolean ENABLED_WRITE_RAW_LOG = "true".equalsIgnoreCase(WORKER_CONFIGS.getCustomConfig("enableWriteRawLog").trim());
    
    final LogFileWriter rawlogFileWriter;
    final String topicName;
    
    public BatchLogWriter(String topicName) {
		this.topicName = topicName;
		rawlogFileWriter = new LogFileWriter(3000);
	}
    
    public static String buildLogFilePath(int unixLoggedTime, int partitionId, String topicName, boolean hourly) {
        Date loggedDate = new Date(unixLoggedTime * 1000L);
        String dateFolder = DateTimeUtil.formatDate(loggedDate, "yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(loggedDate);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String hourFolder = (hour < 10) ? ("0" + hour) : "" + hour;
        String dirTopic = FileUtil.createDirectory(BASE_LOG_FOLDER + File.separator + topicName);
        String dirDate = FileUtil.createDirectory(dirTopic + DAY_NAME_TOKEN + dateFolder);        
        
        StringBuilder filePath;
        if(hourly){
        	String dirHour = FileUtil.createDirectory(dirDate + HOUR_NAME_TOKEN + hourFolder);
        	filePath = new StringBuilder(dirHour);
        } else {
        	filePath = new StringBuilder(dirDate);
        }
              
        filePath.append(File.separator);
        if (partitionId >= 0) {
            filePath.append(partitionId).append(LOG_EXT);
        }
        return filePath.toString();
    }

    private void writeListObject(final int unixLoggedTime, List<List<Object>> valueList, String logFilePath) {
        try {
            StringBuilder data = new StringBuilder();
            for (List<Object> values : valueList) {
                int lastIndex = values.size() - 1;
                for (int i = 0; i < values.size(); i++) {
                    String val = values.get(i).toString();
                    if (val == null) {
                        val = StringPool.MINUS;
                    }
                    data.append(val);
                    if (i == lastIndex) {
                        data.append(StringPool.NEW_LINE);
                    } else {
                        data.append(CharPool.TAB);
                    }
                }
                values.clear();
            }
            valueList.clear();
            String logData = data.toString();
            rawlogFileWriter.write(logFilePath, logData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }  
    
    private void writeListString(final int unixLoggedTime, List<String> valueList, String logFilePath) {
        try {
            StringBuilder data = new StringBuilder();
            for (String val : valueList) {
            	data.append(val).append(StringPool.NEW_LINE);                    
            }
            valueList.clear();
            String logData = data.toString();
            rawlogFileWriter.write(logFilePath, logData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 
    
    /**
     * How to use: <br>
     * 	List<List<Object>> valueList = new ArrayList<>(2);  <br>
		valueList.add(new Values("a","b","c"));  <br>
		valueList.add(new Values("d","e","f"));  <br>
		RawLogUtil.writeRawLog(DateTimeUtil.currentUnixTimestamp(), 1, valueList);  <br>
     * 
     * @param unixLoggedTime
     * @param partitionId
     * @param valueList
     */
    public void writeListObject(int unixLoggedTime, int partitionId, List<List<Object>> valueList) {
    	if(ENABLED_WRITE_RAW_LOG){
    		String logFilePath = buildLogFilePath(unixLoggedTime, partitionId, this.topicName,false);        
    		writeListObject(unixLoggedTime, valueList, logFilePath);
    	}
 
    }
    
    public void writeListString(int unixLoggedTime, int partitionId, List<String> valueList) {
    	if(ENABLED_WRITE_RAW_LOG){
    		String logFilePath = buildLogFilePath(unixLoggedTime, partitionId, this.topicName,false);        
    		writeListString(unixLoggedTime, valueList, logFilePath);
    	}
    }
    
    public void writeString(int unixLoggedTime, int partitionId, String logData) {
    	if(ENABLED_WRITE_RAW_LOG){
    		String logFilePath = buildLogFilePath(unixLoggedTime, partitionId, this.topicName,false);
    		rawlogFileWriter.write(logFilePath, logData);
    	}
    }
    
    public void writeString(int unixLoggedTime, int partitionId, String logData, boolean hourly) {
    	if(ENABLED_WRITE_RAW_LOG){
    		String logFilePath = buildLogFilePath(unixLoggedTime, partitionId, this.topicName,hourly);
    		rawlogFileWriter.write(logFilePath, logData);
    	}
    }
    
    
}
