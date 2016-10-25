package rfx.track.common;

import io.netty.handler.codec.http.HttpHeaders;
import rfx.common.template.DataModel;

import java.util.List;

public class DefaultModel implements DataModel {
	

	@Override
	public void freeResource() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getClasspath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOutputable() {		
		return true;
	}

	@Override
	public List<HttpHeaders> getHttpHeaders() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
