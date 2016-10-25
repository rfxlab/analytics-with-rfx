package pageview.stream.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ip2location.IPResult;

import common.utils.DeviceParserUtil;
import common.utils.IP2LocationUtil;
import pageview.stream.data.EventViewData;
import pageview.stream.data.LogData;
import pageview.util.BeaconUtil;
import pageview.util.FptPlayPlacementUtil;
import rfx.core.stream.functor.StreamProcessor;
import rfx.core.stream.message.Fields;
import rfx.core.stream.message.Tuple;
import rfx.core.stream.message.Values;
import rfx.core.stream.model.DataFlowInfo;
import rfx.core.stream.processor.HttpEventProcessor;
import rfx.core.stream.topology.BaseTopology;
import rfx.core.stream.util.HashUtil;
import rfx.core.stream.util.ua.Client;
import rfx.core.stream.util.ua.Parser;
import rfx.core.util.DateTimeUtil;
import rfx.core.util.LogUtil;
import rfx.core.util.StringUtil;

public class EventCountProcessor extends StreamProcessor {

  public static final String LOG_DATA = "LogData";

  public static final String CXKW = "cxkw";

  static Fields outFields = new Fields(LOG_DATA);

  static List<EventViewData> listEventView = new ArrayList<>();
  static {
    listEventView.add(new EventViewData("premier-league", 1472704083, 1474284480));
  }

  protected EventCountProcessor(DataFlowInfo dataFlowInfo, BaseTopology topology) {
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
      int partitionId =
          StringUtil.safeParseInt(inputTuple.getStringByField(HttpEventProcessor.PARTITION_ID));
      if (StringUtil.isEmpty(query)) {
        return;
      }
      
      System.out.println(loggedTime);
      
       System.out
       .println("loggedTime " + DateTimeUtil.formatDateHourMinute(new Date(loggedTime * 1000l)));

      Map<String, List<String>> params = BeaconUtil.getQueryMap(query);
      String uuid = BeaconUtil.getParam(params, "uuid", "");
      String referrer = BeaconUtil.getParam(params, "referrer");
      String url = BeaconUtil.getParam(params, "url", BeaconUtil.extractRefererURL(cookie));
      int placement = StringUtil.safeParseInt(BeaconUtil.getParam(params, "placement"));

      if (placement > 200) {
         System.out.println("==> Query: " + query);
        // System.out.println("==> userAgent: " + userAgent);
        if (StringUtil.isEmpty(uuid)) {
          uuid = HashUtil.sha1(ip + userAgent + cookie);
        }
      }

      String contextKeyword = BeaconUtil.getParam(params, "cxkw");

      LogData logData = new LogData(loggedTime, placement, uuid, ip, partitionId);

      IPResult ipResult = IP2LocationUtil.find(ip);

      if (ipResult != null) {
        long locationId = IP2LocationUtil.updateAndGetLocationId(loggedTime, ipResult);
        logData.setCity(ipResult.getCity());
        logData.setCountry(ipResult.getCountryLong());
        logData.setLocationId(locationId);
      } else {
        System.out.println(ip + " #### location NOT FOUND");
      }
      logData.setUrl(url);
      logData.setRefererUrl(referrer);

      if (userAgent.contains("okhttp")) {
        logData.setDeviceType(DeviceParserUtil.NATIVE_APP);
        logData.setDeviceName(DeviceParserUtil.DEVICE_ANDROID);
        logData.setDeviceOs(DeviceParserUtil.DEVICE_ANDROID);
      } else {
        Client uaClient = Parser.load().parse(userAgent);
        int deviceType = DeviceParserUtil.getDeviceType(uaClient);
        logData.setDeviceType(deviceType);
        logData.setDeviceName(uaClient.device.family);
        logData.setDeviceOs(uaClient.os.family);
      }

      logData.setUserAgent(userAgent);
      logData.setContextKeyword(contextKeyword);

      // check is that user view Live TV at event time such as Premier League or AFF Cup.
      if (checkTimeRange(loggedTime) && checkLiveTvPlacement(placement)
          && checkPrefixLiveEvent(url)) {
        // this method will transfer data to the next step of topology.
        System.out.println(loggedTime + " --- " + placement + " --- " + url);
        this.emit(new Tuple(outFields, new Values(logData)));
      }

    } catch (IllegalArgumentException e) {
      LogUtil.e("ParsingPlayViewLog occurs", e.getMessage());
    } catch (Exception e) {
      LogUtil.e("ParsingPlayViewLog occurs", e.getClass().getName() + " - " + e.getMessage());
    } finally {
      inputTuple.clear();
    }
    this.doPostProcessing();
  }

  /**
   * Check if event live tv time in range (between start time and end time).
   * @param loggedTime live time of event.
   * @return
   */
  private boolean checkTimeRange(int loggedTime) {
    for (EventViewData eventView : listEventView) {
      // check if loggedTime is in time range of live tv event.
      if (eventView.getStartTime() <= loggedTime && loggedTime <= eventView.getEndTime()) {
        return true;
      } else {
        return false;
      }
    }
    return false;
  }

  /**
   * Check if event run on Live placement.
   * @param placementId
   * @return
   */
  private boolean checkLiveTvPlacement(int placementId) {
    // Web Live TV
    if (placementId == FptPlayPlacementUtil.FPT_PLAY_LIVE_TV) {
      return true;
    }
    // Smart TV Live TV
    else if (placementId == FptPlayPlacementUtil.FPT_PLAY_SMARTTV_LIVE_TV) {
      return true;
    }
    // iOs Live TV
    else if (placementId == FptPlayPlacementUtil.FPT_PLAY_LIVE_STREAM_IOS) {
      return true;
    }
    // Android Box Live TV
    else if (placementId == FptPlayPlacementUtil.FPT_PLAY_LIVE_STREAM_ANDROID_BOX) {
      return true;
    }
    // Android App Live TV
    else if (placementId == FptPlayPlacementUtil.FPT_PLAY_LIVE_STREAM_ANDROID_APP) {
      return true;
    }
    // Smart TV Android Live TV.
    else if (placementId == FptPlayPlacementUtil.FPT_PLAY_LIVE_STREAM_ANDROID_SMART_TV) {
      return true;
    }
    return false;
  }

  /**
   * Check prefix URL of lieve event.
   * @param url
   * @return
   */
  private boolean checkPrefixLiveEvent(String url) {
    for (EventViewData eventPrefix : listEventView) {
      if (url.contains(eventPrefix.getPrefix())) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Convert String date format yyyy-MM-dd HH:mm:ss to unix time.
   * @param date
   * @return
   */
  protected static long convertDateToUnix(String date) {
    return DateTimeUtil.parseDateStrRaw(date).getTime()/1000L;
  }
}
