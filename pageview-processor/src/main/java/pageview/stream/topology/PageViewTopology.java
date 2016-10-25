package pageview.stream.topology;

import common.processor.LogTokenizing;
import pageview.stream.processor.ParsingPlayViewLog;
import pageview.stream.processor.ProcessingPlayViewLog;
import rfx.core.stream.topology.BaseTopology;
import rfx.core.stream.topology.Pipeline;
import rfx.core.stream.topology.PipelineTopology;
import rfx.core.util.LogUtil;
import rfx.core.util.Utils;

public class PageViewTopology extends PipelineTopology {        
    @Override
    public BaseTopology build() {   
    	System.out.println("... buildTopology " + this.topologyName);    	
        return Pipeline.create(this)
        		.apply(LogTokenizing.class)				
                .apply(ParsingPlayViewLog.class) 
                .apply(ProcessingPlayViewLog.class)
                .done();
    }
    
    private static final String TOPIC = "pageview";		
    public static void main(String[] args) {
    	LogUtil.setPrefixFileName(TOPIC);
		int begin  = 0;
		int end  = 3;		
		PipelineTopology topo = new PageViewTopology();
		topo.initKafkaDataSeeders(TOPIC, begin, end).buildTopology().start();
		Utils.sleep(2000);
	}
}