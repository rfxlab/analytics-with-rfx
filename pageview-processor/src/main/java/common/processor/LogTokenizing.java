package common.processor;

import java.util.concurrent.atomic.AtomicBoolean;

import rfx.core.stream.functor.StreamProcessor;
import rfx.core.stream.functor.common.DataSourceFunctor;
import rfx.core.stream.functor.common.HttpEventLogTokenizing;
import rfx.core.stream.message.Fields;
import rfx.core.stream.message.Tuple;
import rfx.core.stream.message.Values;
import rfx.core.stream.model.DataFlowInfo;
import rfx.core.stream.processor.HttpEventProcessor;
import rfx.core.stream.topology.BaseTopology;
import rfx.core.util.LogUtil;
import rfx.core.util.StringPool;
import rfx.core.util.StringUtil;
import rfx.core.util.Utils;


public class LogTokenizing extends StreamProcessor {
	
	static AtomicBoolean stopProcessing = new AtomicBoolean(false);	
	public static final void stopProcessing(){
		stopProcessing.set(true);
	}
			
	public LogTokenizing(DataFlowInfo dataFlowInfo,
			BaseTopology topology) {
		super(dataFlowInfo, topology);	
	}

	public void onReceive(Tuple in) throws Exception {
		if(stopProcessing.get()){
			in.clear();		
			unhandled(in);
			Utils.sleep(4000);	
			return;
		}
		this.doPreProcessing();		
		Tuple out = doProcessing(in, HttpEventLogTokenizing.outputFields);
		if (out != null) {
			// output to next phase
			this.emit(out, self());
			String k = StringUtil.toString(this.getMetricKey(),	out.getStringByField(HttpEventProcessor.TOPIC), "#", out.getIntegerByField(HttpEventProcessor.PARTITION_ID));
			this.counter(k).incrementAndGet();
			this.topology.counter().incrementAndGet();
		} else {
			LogUtil.error("logTokens.length (delimiter is tab) is NOT = 5, INVALID LOG ROW FORMAT ");
		}
		in.clear();
	}
		
	
	Tuple doProcessing(Tuple inputTuple, Fields outputMetadataFields) {		
		String event = inputTuple.getStringByField(DataSourceFunctor.EVENT);
    	String topic = inputTuple.getStringByField(HttpEventProcessor.TOPIC,HttpEventProcessor._FROM_FILE);
    	int partitionId = inputTuple.getIntegerByField(HttpEventProcessor.PARTITION_ID,0);
    	long offsetId = inputTuple.getIntegerByField(HttpEventProcessor.OFFSET_ID,0);
    	    	
		String[] logTokens = event.split(HttpEventProcessor.TAB_STRING);
		//System.out.println("## event: "+event);
		
		if(logTokens.length == 5){
			if(	   StringUtil.isNotEmpty(logTokens[0])
				&& StringUtil.isNotEmpty(logTokens[1]) 
				&& StringUtil.isNotEmpty(logTokens[2]) 
				&& StringUtil.isNotEmpty(logTokens[3]) 
				&& StringUtil.isNotEmpty(logTokens[4])
				){
				String ip = StringUtil.safeSplitAndGet(logTokens[0], StringPool.COLON, 0);
				int loggedtime = StringUtil.safeParseInt(logTokens[1]);								
				String useragent = StringUtil.safeString(logTokens[2]);
				String query = logTokens[3];
				String cookie = StringUtil.safeString(logTokens[4]);
			
				return new Tuple(outputMetadataFields, new Values(query ,cookie ,loggedtime ,ip ,useragent ,topic ,partitionId,offsetId));
			}
		} else if(logTokens.length == 4){
			String ip = StringUtil.safeSplitAndGet(logTokens[0], StringPool.COLON, 0);
			int loggedtime = StringUtil.safeParseInt(logTokens[1]);							
			String useragent = StringUtil.safeString(logTokens[2]);
			String query = logTokens[3];
			String cookie = "-";		
			return new Tuple(outputMetadataFields, new Values(query ,cookie ,loggedtime ,ip ,useragent ,topic ,partitionId,offsetId));
		}
		return null;
	}
}

