package pageview.stream.processor;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ip2location.IPResult;

import common.utils.DeviceParserUtil;
import common.utils.IP2LocationUtil;
import pageview.stream.data.LogData;
import pageview.util.QueryDataUtil;
import rfx.core.stream.functor.StreamProcessor;
import rfx.core.stream.message.Fields;
import rfx.core.stream.message.Tuple;
import rfx.core.stream.message.Values;
import rfx.core.stream.model.DataFlowInfo;
import rfx.core.stream.processor.HttpEventProcessor;
import rfx.core.stream.topology.BaseTopology;
import rfx.core.stream.util.ua.Client;
import rfx.core.stream.util.ua.Parser;
import rfx.core.util.DateTimeUtil;
import rfx.core.util.LogUtil;
import rfx.core.util.StringUtil;

public class ParsingPageViewLog extends StreamProcessor {

    public static final String LOG_DATA = "LogData";
    public static final String CXKW = "cxkw";
    static Fields outFields = new Fields(LOG_DATA);

    protected ParsingPageViewLog(DataFlowInfo dataFlowInfo, BaseTopology topology) {
        super(dataFlowInfo, topology);
    }

    @Override
    public void onReceive(Tuple inputTuple) throws Exception {
        try {
            String query = inputTuple.getStringByField(HttpEventProcessor.QUERY);
            int loggedTime = inputTuple.getIntegerByField(HttpEventProcessor.LOGGEDTIME);
            String userAgent = inputTuple.getStringByField(HttpEventProcessor.USERAGENT);
            String ip = inputTuple.getStringByField(HttpEventProcessor.IP);
            String cookie = inputTuple.getStringByField(HttpEventProcessor.COOKIE);
            int partitionId = StringUtil.safeParseInt(inputTuple.getStringByField(HttpEventProcessor.PARTITION_ID));
            
            System.out.println(query);
            
            
            if (StringUtil.isEmpty(query)) {
                return;
            }
            System.out.println("loggedTime " + DateTimeUtil.formatDateHourMinute(new Date(loggedTime * 1000l)));
            
            Map<String, List<String>> params = QueryDataUtil.getQueryMap(query);
            String uuid = QueryDataUtil.getParam(params, "uuid", "");
            String referrer = QueryDataUtil.getParam(params, "referrer");
            String url = QueryDataUtil.getParam(params, "url", QueryDataUtil.extractRefererURL(cookie));
            String metric = QueryDataUtil.getParam(params, "metric");
            String contextKeyword = QueryDataUtil.getParam(params, "keywords");

            LogData o = new LogData(loggedTime, metric, uuid, ip, partitionId);

//            System.out.println("==> "+query);
            IPResult ipResult = IP2LocationUtil.find(ip);

            if (ipResult != null) {
                //System.out.println(ip + " #### location "+ipResult.getCity());
                long locationId = IP2LocationUtil.updateAndGetLocationId(loggedTime, ipResult);
                o.setCity(ipResult.getCity());
                o.setCountry(ipResult.getCountryLong());
                o.setLocationId(locationId);
            } else {
                System.out.println(ip + " #### location NOT FOUND");
            }
            o.setUrl(url);
            o.setRefererUrl(referrer);

            if(userAgent.contains("okhttp")){
                o.setDeviceType(DeviceParserUtil.NATIVE_APP);
                o.setDeviceName(DeviceParserUtil.DEVICE_ANDROID);
                o.setDeviceOs(DeviceParserUtil.DEVICE_ANDROID);
            } else {
                Client uaClient = Parser.load().parse(userAgent);
                int deviceType = DeviceParserUtil.getDeviceType(uaClient);
                o.setDeviceType(deviceType);
                o.setDeviceName(uaClient.device.family);
                o.setDeviceOs(uaClient.os.family);
            }

            o.setUserAgent(userAgent);
            o.setContextKeyword(contextKeyword);
            this.emit(new Tuple(outFields, new Values(o)));
        } catch (IllegalArgumentException e) {
            LogUtil.e("ParsingPlayViewLog occurs", e.getMessage());
        } catch (Exception e) {
            LogUtil.e("ParsingPlayViewLog occurs", e.getClass().getName() + " - " + e.getMessage());
        } finally {
            inputTuple.clear();
        }
        this.doPostProcessing();
    }
}
