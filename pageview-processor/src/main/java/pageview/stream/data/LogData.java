package pageview.stream.data;

import common.utils.DeviceParserUtil;
import rfx.core.util.CharPool;
import rfx.core.util.StringPool;
import rfx.core.util.StringUtil;

public class LogData {

    protected int loggedTime;
    protected String metric;
    protected String uuid = "0";
    protected int deviceType = DeviceParserUtil.PC;
    protected String ip = "0";
    protected long locationId = 0;
    protected String country = "0";
    protected String city = "0";
    protected String url = "0";
    protected String refererUrl = "0";
    protected String deviceName = "0";
    protected String deviceOs = "0";
    protected int partitionId;
    protected String userAgent;
    protected String contextKeyword;

    public LogData(int loggedTime, String metric, String uuid, String ip, int partitionId) {
        super();
        this.loggedTime = loggedTime;
        this.metric = metric;
        this.uuid = uuid;
        this.ip = ip;
        this.partitionId = partitionId;
    }

    public LogData() {
        super();
    }

    public void setMetric(String metric) {
		this.metric = metric;
	}

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public void setCountry(String country) {
        if (country != null)
            this.country = country;
    }

    public void setCity(String city) {
        if (city != null) {
            this.city = city;
        }
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        if (deviceName != null) {
            this.deviceName = deviceName;
        }
    }

    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

    public String getDeviceOs() {
        return deviceOs;
    }

    public void setDeviceOs(String deviceOs) {
        if (deviceOs != null) {
            this.deviceOs = deviceOs;
        }
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUrl(String url) {
        if (StringUtil.isNotEmpty(url)) {
            this.url = url;
        }
    }

    public void setRefererUrl(String refererUrl) {
        if (StringUtil.isNotEmpty(refererUrl)) {
            this.refererUrl = refererUrl;
        }
    }

    public int getDeviceType() {
        return deviceType;
    }

    public long getLocationId() {
        return locationId;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public void setLocationId(long locationId) {
        this.locationId = locationId;
    }

    public String getUrl() {
        if (StringUtil.isEmpty(url)) {
            url = "0";
        }
        return url;
    }

    public String getRefererUrl() {
        if (StringUtil.isEmpty(refererUrl)) {
            refererUrl = "0";
        }
        return refererUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMetric() {
		return metric;
	}

    public int getLoggedTime() {
        return loggedTime;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getContextKeyword() {
        return contextKeyword;
    }

    public void setContextKeyword(String contextKeyword) {
        this.contextKeyword = contextKeyword;
    }

    public String toRawLogRecord() {
        return toLogRecordSplitByTab();
    }

    String toLogRecordSplitByTab() {
        StringBuilder s = new StringBuilder();

        // event time
        char tab = CharPool.TAB;
        s.append(this.loggedTime).append(tab);
    
        s.append(this.metric).append(tab);

        // context logs
        s.append(this.ip).append(tab);
        s.append(this.uuid).append(tab);
        s.append(this.url).append(tab);
        s.append(this.refererUrl).append(tab);
        s.append(this.city).append(tab);
        s.append(this.country).append(tab);
        s.append(this.deviceType).append(tab);
        s.append(this.deviceName).append(tab);
        s.append(this.deviceOs).append(tab);
        s.append(this.userAgent).append(tab);
        s.append(this.contextKeyword);
        s.append(StringPool.NEW_LINE);
        return s.toString();
    }


}