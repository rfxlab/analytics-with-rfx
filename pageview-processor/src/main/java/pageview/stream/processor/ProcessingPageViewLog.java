package pageview.stream.processor;

import common.utils.BatchLogWriter;
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
                
        processLogData(ld);
    }
    

    static void processLogData(LogData ld) {
        String url = ld.getUrl();
        String metric = ld.getMetric();
        //TODO
    }
}
