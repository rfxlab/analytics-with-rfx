package pageview.stream.processor;

import common.utils.BatchLogWriter;
import pageview.stream.data.LogData;
import pageview.util.FptPlayPlacementUtil;
import pageview.util.UserRedisUtil;
import rfx.core.stream.functor.StreamProcessor;
import rfx.core.stream.message.Tuple;
import rfx.core.stream.model.DataFlowInfo;
import rfx.core.stream.topology.BaseTopology;

public class ProcessingPlayViewLog extends StreamProcessor {

    public static final String PLV = "plv";
    static final BatchLogWriter logWriterFptPlay = new BatchLogWriter("playview-fptlay");
    static final BatchLogWriter logWriterOther = new BatchLogWriter("playview-other");

    protected ProcessingPlayViewLog(DataFlowInfo dataFlowInfo, BaseTopology topology) {
        super(dataFlowInfo, topology);
    }

    @Override
    public void onReceive(Tuple inTuple) throws Exception {
        LogData ld = (LogData) inTuple.getValueByField(ParsingPlayViewLog.LOG_DATA);
        //System.out.println(String.format("user %s use %s at %s", ld.getUuid(), ld.getDeviceOs(), ld.getCity() + " " + ld.getCountry()));

        //TODO
        //processLogData(ld);
        boolean isFptPlay = FptPlayPlacementUtil.isFptPlay(ld.getPlacement());        
        if (isFptPlay) {
        	logWriterFptPlay.writeString(ld.getLoggedTime(), ld.getPartitionId(), ld.toRawLogRecord());	
        }
        
    }
    

    static void processLogData(LogData ld) {

        String url = ld.getUrl();
        int placementId = ld.getPlacement();
        boolean isFptPlay = FptPlayPlacementUtil.isFptPlay(placementId);

        
        if (isFptPlay) {
            int idx = url.indexOf("/livetv");
            boolean isLiveTVOnWeb = idx > 0;

            if (isLiveTVOnWeb) {
                ld.setPlacement(101);                
                String channelName = url.endsWith("/livetv") ? "vtv1-hd" : url.substring(idx + 8);
                int lidx = channelName.indexOf("?");
                if (lidx > 0) {
                    channelName = channelName.substring(0, lidx);
                }
                lidx = channelName.indexOf("/");
                if (lidx > 0) {
                    channelName = channelName.substring(0, lidx);
                }
                lidx = channelName.indexOf("#");
                if (lidx > 0) {
                    channelName = channelName.substring(0, lidx);
                }

                String event1 = "pm-" + placementId;
                String event2 = "live";
                String event3 = "ch-" + channelName;
//                AdDataRedisUtil.updateStreamDataStats(ld.getLoggedTime(), new String[]{event1, event2, event3});
            } else {
                ld.setPlacement(102);                
                String event1 = "pm-" + placementId;
                String event2 = "vod";
//                AdDataRedisUtil.updateStreamDataStats(ld.getLoggedTime(), new String[]{event1, event2});
            }
//            AdDataRedisUtil.updateContextKeyword(ld.getLoggedTime(), ld.getContextKeyword());
//            IP2LocationUtil.updateLocationStats(ld.getLoggedTime(), ld.getLocationId(), PLV);
            UserRedisUtil.addPlayViewUser(ld.getLoggedTime(), placementId, ld.getUuid());
            logWriterFptPlay.writeString(ld.getLoggedTime(), ld.getPartitionId(), ld.toRawLogRecord());
        } else {
            if (placementId > 0) {
                String event1 = "pm-" + placementId;
//                AdDataRedisUtil.updateStreamDataStats(ld.getLoggedTime(), new String[]{event1});
                UserRedisUtil.addPlayViewUser(ld.getLoggedTime(), placementId, ld.getUuid());
                logWriterOther.writeString(ld.getLoggedTime(), ld.getPartitionId(), ld.toRawLogRecord());
            }
        }
    }
}
