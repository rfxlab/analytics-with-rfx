package pageview.stream.processor;

import common.utils.BatchLogWriter;
import common.utils.RealtimeTrackingUtil;
import pageview.stream.data.LogData;
import rfx.core.stream.functor.StreamProcessor;
import rfx.core.stream.message.Tuple;
import rfx.core.stream.model.DataFlowInfo;
import rfx.core.stream.topology.BaseTopology;

public class ProcessingPageViewLog extends StreamProcessor {

    public static final String PLV = "plv";
    
    static final BatchLogWriter logWriterOther = new BatchLogWriter("pageview");

    protected ProcessingPageViewLog(DataFlowInfo dataFlowInfo, BaseTopology topology) {
        super(dataFlowInfo, topology);
    }

    @Override
    public void onReceive(Tuple inTuple) throws Exception {
        LogData ld = (LogData) inTuple.getValueByField(ParsingPageViewLog.LOG_DATA);                
        processRealtimeLogData(ld);
        saveLogDataForBatchProcessing(ld);
    }
    

    static void processRealtimeLogData(LogData ld) {        
        String metric = ld.getMetric();
        RealtimeTrackingUtil.updateKafkaLogEvent(ld.getLoggedTime(), metric);
        //TODO add more real-time metrics
    }
    
    static void saveLogDataForBatchProcessing(LogData ld) {
       String log = ld.toRawLogRecord();
       //TODO save to storage
       // Option 1: save to HDFS
       // Option 2: PosgresSQL or MySQL
       // Option 3: Apache Cassandra
       // Option 4: Apache Phoenix
       // Option 5: normal file 
    }
}
