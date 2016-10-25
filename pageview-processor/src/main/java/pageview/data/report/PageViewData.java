package pageview.data.report;

import rfx.core.util.StringUtil;

public class PageViewData {
	int loggedTime;
	String loggedTimeString;
	int websiteId;
	int url;
	int playviewCount;

	public int getLoggedTime() {
		return loggedTime;
	}

	public void setLoggedTime(int loggedTime) {
		this.loggedTime = loggedTime;
	}

	public String getLoggedTimeString() {
		return loggedTimeString;
	}

	public void setLoggedTimeString(String loggedTimeString) {
		this.loggedTimeString = loggedTimeString;
	}

	public int getWebsiteId() {
		return websiteId;
	}

	public void setWebsiteId(int websiteId) {
		this.websiteId = websiteId;
	}

	public int getUrl() {
		return url;
	}

	public void setUrl(int url) {
		this.url = url;
	}

	public int getPlayviewCount() {
		return playviewCount;
	}

	public void setPlayviewCount(int playviewCount) {
		this.playviewCount = playviewCount;
	}

	public PageViewData(int loggedTime, int websiteId, int url, int pageview) {
		super();
		this.loggedTime = loggedTime;
		this.websiteId = websiteId;
		this.url = url;
		this.playviewCount = pageview;
	}

	public static PageViewData fromRedisDataToPageviewData(String key, String field, int value) {
		return new PageViewData(key, field, value);
	}

	protected PageViewData(String hourkey, String field, int value) {
		StringBuilder timeKey = new StringBuilder(hourkey);// db:2013-05-13-21
		timeKey.replace(0, 3, "");
		timeKey.replace(10, 11, " ");
		timeKey.append(":00:00");
		this.loggedTimeString = timeKey.toString();

		String[] toks = field.split(":");
		if (toks.length >= 3) {
			this.websiteId = StringUtil.safeParseInt(toks[1], 0);
			this.url = StringUtil.safeParseInt(toks[2], 0);
		}

		this.playviewCount = value;
	}
}
