package pageview.stream.data;

import java.io.Serializable;

import rfx.core.util.StringUtil;

public class EventViewData implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private String id;

  private String prefix;

  private int startTime;

  private int endTime;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public int getStartTime() {
    return startTime;
  }

  public void setStartTime(int startTime) {
    this.startTime = startTime;
  }

  public int getEndTime() {
    return endTime;
  }

  public void setEndTime(int endTime) {
    this.endTime = endTime;
  }

  public String makeObjectId() {
    return buildId(prefix, startTime, endTime);
  }

  public static String buildId(String prefix, int startTime, int endTime) {
    return (startTime / 1000) + (endTime / 1000) + StringUtil.toString(prefix);
  }

  public EventViewData() {
    super();
    // TODO Auto-generated constructor stub
  }

  public EventViewData(String prefix, int startTime, int endTime) {
    super();
    this.id = makeObjectId();
    this.prefix = prefix;
    this.startTime = startTime;
    this.endTime = endTime;
  }

}
