package pageview.stream.topology;

import common.processor.LogTokenizing;
import common.utils.FileUtil;
import pageview.stream.processor.ParsingPageViewLog;
import pageview.stream.processor.ProcessingPageViewLog;
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
                .apply(ParsingPageViewLog.class) 
                .apply(ProcessingPageViewLog.class)
                .done();
    }
    
    private static final String TOPIC = "pageview";		
    public static void main(String[] args) {
    	FileUtil.checkAndCreateDirectories("data/kafka-offset");
    	LogUtil.setPrefixFileName(TOPIC);
		int begin  = 0;
		int end  = 0;		
		PipelineTopology topo = new PageViewTopology();
		topo.initKafkaDataSeeders(TOPIC, begin, end).buildTopology().start();
		Utils.sleep(2000);
	}
}