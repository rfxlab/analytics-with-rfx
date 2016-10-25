package common.processor;

import rfx.core.stream.functor.StreamProcessor;
import rfx.core.stream.message.Tuple;
import rfx.core.stream.model.DataFlowInfo;
import rfx.core.stream.processor.HttpEventProcessor;
import rfx.core.stream.topology.BaseTopology;
import rfx.core.stream.util.ua.Client;
import rfx.core.stream.util.ua.Parser;
import rfx.core.util.StringUtil;


public abstract class LogParsing extends StreamProcessor{
	
	public LogParsing(DataFlowInfo dataFlowInfo,BaseTopology topology) {
		super(dataFlowInfo, topology);	
	}

	static Parser uaParser = Parser.load();
	public void onReceive(Tuple input) throws Exception {
		try {
			String query = input.getStringByField(HttpEventProcessor.QUERY);
			String cookie = input.getStringByField(HttpEventProcessor.COOKIE);
			int loggedTime = input.getIntegerByField(HttpEventProcessor.LOGGEDTIME);
			String ip = input.getStringByField(HttpEventProcessor.IP);
			String useragent = input.getStringByField(HttpEventProcessor.USERAGENT);
			int partitionId = input.getIntegerByField(HttpEventProcessor.PARTITION_ID);
			long offsetId = input.getLongByField(HttpEventProcessor.OFFSET_ID);
			
			//verify log
			if(StringUtil.isEmpty(query) || StringUtil.isEmpty(useragent) || StringUtil.isEmpty(cookie)){
				//TODO log for debugging
				input.clear();
				return;
			}
			
			//Get browser & os        		
    		Client client = uaParser.parse(useragent);
			
    		//call the implementation
    		doProcessing(loggedTime, ip, useragent, query, cookie, client, partitionId, offsetId);
			
		} catch(Throwable e){
			e.printStackTrace();
		}
		input.clear();		
	}
		
	public abstract void doProcessing(int loggedTime, String ip, String useragent, String query, String cookie, Client client, int partitionId, long offsetId);	
}

