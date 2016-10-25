package pageview.worker;

import rfx.core.stream.model.KafkaTaskDef;
import rfx.core.stream.model.TaskDef;
import rfx.core.stream.worker.StreamProcessingWorker;
import rfx.core.util.LogUtil;
import rfx.core.util.StringUtil;
import rfx.core.util.Utils;

import common.processor.LogTokenizing;

public class AdProcessingWorker extends StreamProcessingWorker {

	public AdProcessingWorker(String name) {		
		super(name);
		System.out.println("StreamProcessingWorker name: " + name);
	}
	
	@Override
	protected void onBeforeBeStopped() {
		System.out.println(" =====>>> worker.restart <<<====");
		LogTokenizing.stopProcessing();	
	}	
	
	public static void main(String[] args) {
		if(args.length != 5){
			System.err.println("need 5 params [topic] [host] [port] [beginPartitionId] [endPartitionId]");
			return;
		}
		LogUtil.setDebug(false);
		
		String topic = args[0];
		String host = args[1];
		int port = StringUtil.safeParseInt(args[2]);
		int beginPartition = StringUtil.safeParseInt(args[3]);
		int endPartition = StringUtil.safeParseInt(args[4]);
				
		TaskDef taskDef = new KafkaTaskDef(topic , beginPartition, endPartition);		
		new AdProcessingWorker("AdProcessingWorker:"+topic).setTaskDef(taskDef).start(host, port);		
		Utils.sleep(2000);
	}
}
