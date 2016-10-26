package pageview.worker;

import common.processor.LogTokenizing;
import common.utils.FileUtil;
import rfx.core.stream.model.KafkaTaskDef;
import rfx.core.stream.model.TaskDef;
import rfx.core.stream.worker.StreamProcessingWorker;
import rfx.core.util.LogUtil;
import rfx.core.util.StringUtil;
import rfx.core.util.Utils;

public class LogProcessingWorker extends StreamProcessingWorker {

	private static final String DATA_KAFKA_OFFSET = "data/kafka-offset";

	public LogProcessingWorker(String name) {		
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
				
		FileUtil.checkAndCreateDirectories(DATA_KAFKA_OFFSET);
		TaskDef taskDef = new KafkaTaskDef(topic , beginPartition, endPartition);		
		new LogProcessingWorker(LogProcessingWorker.class.getName()+":"+topic).setTaskDef(taskDef).start(host, port);		
		Utils.sleep(2000);
	}
}
