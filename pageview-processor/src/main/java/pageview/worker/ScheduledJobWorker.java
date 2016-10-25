package pageview.worker;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

import rfx.core.configs.ScheduledJobsConfigs;
import rfx.core.stream.node.worker.BaseWorker;
import rfx.core.util.LogUtil;
import rfx.core.util.StringUtil;

import com.google.gson.Gson;
import common.utils.job.SynchDataJob;

public class ScheduledJobWorker extends BaseWorker {
	
	private static Collection<ScheduledJobsConfigs> scheduledJobsConfigs = ScheduledJobsConfigs.load();
	static final Map<String,Timer> timerMap = new HashMap<>();
		
	static public int startScheduledJobs(){
		for (ScheduledJobsConfigs config : scheduledJobsConfigs) {
			if(config.getDelay() >= 0){
				try {
					String classpath = config.getClasspath();
					System.out.println("#process autoTaskConfig:"+config + " "+classpath);
					
					Class<?> clazz = Class.forName(classpath);
					SynchDataJob autoTask = (SynchDataJob) clazz.newInstance();
					autoTask.setScheduledJobsConfigs(config);
					
					if(config.getPeriod() <= 0){
						new Timer().schedule(autoTask, config.getDelay()*1000L);
					} else {							
						Timer timer = timerMap.get(classpath);
						if(timer == null){
							timer = new Timer(true);
							timerMap.put(classpath, timer);
						}
						timer.schedule(autoTask, config.getDelay()*1000L, config.getPeriod()*1000L);
					}
				}  catch (Exception e) {				
					e.printStackTrace();
				}
			}
		}
		return scheduledJobsConfigs.size();
	}
	
	public static class ScheduledJobParam {
		String beginTimeKey = "";
		int hoursGoBack = 2;
		String jobs = "";
				
		public ScheduledJobParam(String beginTimeKey, int hoursGoBack) {
			super();
			this.beginTimeKey = beginTimeKey;
			this.hoursGoBack = hoursGoBack;
		}
		public String getBeginTimeKey() {
			return beginTimeKey;
		}
		public void setBeginTimeKey(String beginTimeKey) {
			this.beginTimeKey = beginTimeKey;
		}
		public int getHoursGoBack() {
			return hoursGoBack;
		}
		public void setHoursGoBack(int hoursGoBack) {
			this.hoursGoBack = hoursGoBack;
		}
		public void setJobs(String jobs) {
			this.jobs = jobs;
		}
		public String getJobs() {			
			return jobs != null ? jobs : "";
		}		
		
		@Override
		public String toString() {
			return new Gson().toJson(this);
		}
	}

	public ScheduledJobWorker(String name) {
		super(name);
	}

	@Override
	public void start(String host, int port) {
		Handler<HttpServerRequest> handler = new Handler<HttpServerRequest>() {
            public void handle(HttpServerRequest request) {
                if (request.absoluteURI().getPath().equals("/kill")) {
                    request.response().end("Exiting after 5s ...");
                    killWorker();
                    return;
                } else if (request.absoluteURI().getPath().equals("/ping")) {
                    request.response().end("PONG");
                    return;
                } 
                request.response().end("ScheduledJobNode is running");
            }
        };
        registerWorkerHttpHandler(host, port, handler);
	}
	
	 @Override
	protected void onStartDone() {
		LogUtil.setSuffixLogFile(getName());
		int c = startScheduledJobs();
		LogUtil.i("ScheduledJobManager.started "+ c + " ScheduledJobs");
	}

	public static void main(String[] args) throws IOException {	
		int port = 14999;
		String host = "127.0.0.1";	
				
		if(args.length == 2){
			host = args[0];
			port = StringUtil.safeParseInt(args[1]);
		}		
		else {
			LogUtil.i("ScheduledJobWorker", "started job at" + new Date().toString() + " ", true);
		}
		try {
			String name = host + "_" + port;
			new ScheduledJobWorker(name).start(host, port);
		} catch (Exception e) {			
			e.printStackTrace();
			System.exit(1);
		}
	}
}
