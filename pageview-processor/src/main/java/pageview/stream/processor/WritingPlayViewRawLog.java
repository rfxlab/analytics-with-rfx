package pageview.stream.processor;

import com.google.gson.Gson;

import common.utils.BatchLogWriter;
import pageview.stream.data.LogData;
import rfx.core.stream.functor.StreamProcessor;
import rfx.core.stream.message.Tuple;
import rfx.core.stream.model.DataFlowInfo;
import rfx.core.stream.topology.BaseTopology;

public class WritingPlayViewRawLog extends StreamProcessor {

	public static final String PLV = "plv";
	static final BatchLogWriter logWriter = new BatchLogWriter("pageview");

	protected WritingPlayViewRawLog(DataFlowInfo dataFlowInfo, BaseTopology topology) {
		super(dataFlowInfo, topology);
	}

	@Override
	public void onReceive(Tuple inTuple) throws Exception {
		LogData ld  = (LogData) inTuple.getValueByField(ParsingPlayViewLog.LOG_DATA);
		System.out.println("beacon "+ new Gson().toJson(ld));
		
		String url = ld.getUrl();
	
			
//				String event1 = "pm-"+placement;
//				String event2 = "live";
//				String event3 = "ch-" + channelName;
//				AdDataRedisUtil.updateStreamDataStats(ld.getLoggedTime(), new String[] {event1, event2, event3 });
		
//			IP2LocationUtil.updateLocationStats(ld.getLoggedTime(), ld.getLocationId(), PLV);
		logWriter.writeString(ld.getLoggedTime(), ld.getPartitionId(), ld.toRawLogRecord());
		
			
	}
}
