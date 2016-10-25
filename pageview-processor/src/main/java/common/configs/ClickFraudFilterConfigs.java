package common.configs;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rfx.core.util.FileUtils;
import rfx.core.util.LogUtil;
import rfx.core.util.StringPool;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ClickFraudFilterConfigs {
	public static final String CLICK_FRAUD_FILTER_CONFIG_FILE = "configs/click-fraud-filter-configs.json";
	
	static ClickFraudFilterConfigs _instance;
	
	static Map<Integer, Boolean> websiteIdBlackMap = new HashMap<Integer, Boolean>();
	
	boolean checkClickFraud;
	boolean checkEmptyVisitorId;
	int limitValidScore = 66;//default
	List<Integer> websiteIdBlackList;	
	String pythonScriptPath;
	String pythonRuntimePath;
	int minimumTimeToValidClick = 3;
	int maximumTimeToValidClick = 10800;
	
	
	public boolean isCheckClickFraud() {
		return checkClickFraud;
	}
	public void setCheckClickFraud(boolean checkClickFraud) {
		this.checkClickFraud = checkClickFraud;
	}
	public int getLimitValidScore() {
		return limitValidScore;
	}
	public void setLimitValidScore(int limitValidScore) {
		this.limitValidScore = limitValidScore;
	}
	public List<Integer> getWebsiteIdBlackList() {
		if(websiteIdBlackList == null){
			websiteIdBlackList = new ArrayList<Integer>(0);
		}
		return websiteIdBlackList;
	}
	public void setWebsiteIdBlackList(List<Integer> websiteIdBlackList) {
		this.websiteIdBlackList = websiteIdBlackList;
	}
	
	public String getPythonScriptPath() {
		return pythonScriptPath;
	}
	public File getPythonScriptFile() {
		return new File(pythonScriptPath);
	}
	public void setPythonScriptPath(String pythonScriptPath) {
		this.pythonScriptPath = pythonScriptPath;
	}
	public boolean isCheckEmptyVisitorId() {
		return checkEmptyVisitorId;
	}
	public void setCheckEmptyVisitorId(boolean checkEmptyVisitorId) {
		this.checkEmptyVisitorId = checkEmptyVisitorId;
	}	
	
	public int getMinimumTimeToValidClick() {
		return minimumTimeToValidClick;
	}
	public void setMinimumTimeToValidClick(int minimumTimeToValidClick) {
		this.minimumTimeToValidClick = minimumTimeToValidClick;
	}
	public int getMaximumTimeToValidClick() {
		return maximumTimeToValidClick;
	}
	public void setMaximumTimeToValidClick(int maximumTimeToValidClick) {
		this.maximumTimeToValidClick = maximumTimeToValidClick;
	}
	public boolean checkWebsiteIdFromBlackList(int websiteId){
		if(websiteIdBlackMap.isEmpty()){
			for (int webId : getWebsiteIdBlackList()) {
				websiteIdBlackMap.put(webId, true);
			}
		}		
		return websiteIdBlackMap.containsKey(websiteId);
	}
	
	
	public String getPythonRuntimePath() {
		if(pythonScriptPath == null){
			pythonScriptPath = StringPool.BLANK;
		}
		return pythonRuntimePath;
	}
	public void setPythonRuntimePath(String pythonRuntimePath) {
		this.pythonRuntimePath = pythonRuntimePath;
	}
	public static final ClickFraudFilterConfigs load() {
		if (_instance == null) {
			try {
				String json = FileUtils.readFileAsString(CLICK_FRAUD_FILTER_CONFIG_FILE);
				_instance = new Gson().fromJson(json, ClickFraudFilterConfigs.class);
			} catch (Exception e) {
				if (e instanceof JsonSyntaxException) {
					e.printStackTrace();
					System.err.println("Wrong JSON syntax in file "+CLICK_FRAUD_FILTER_CONFIG_FILE);
				} else {
					e.printStackTrace();
				}
			}
		}
		if(_instance == null){
			System.err.println("Can not load configs from file "+CLICK_FRAUD_FILTER_CONFIG_FILE);
			System.exit(1);
		}
		if( ! _instance.getPythonScriptFile().isFile() || _instance.getPythonRuntimePath().isEmpty() ){
			System.err.println(_instance.getPythonScriptPath() + " is NOT valid path ");
			System.exit(1);
		}
		return _instance;
	}
	
	public static final ClickFraudFilterConfigs reload() {
		LogUtil.i("ClickFraudFilterConfigs", "reload called", true);
		websiteIdBlackMap.clear();
		_instance = null;		
		return load();
	}
}
