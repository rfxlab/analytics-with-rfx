package rfx.common.template;

import org.apache.commons.lang3.StringEscapeUtils;

import rfx.track.common.DefaultModel;


public class TemplateUtil {
	
	public static String render(String tplPath, DataModel model){		
		return StringEscapeUtils.unescapeHtml4(HandlebarsTemplateUtil.execute(tplPath, model));
	}
	
	public static String render(String tplPath){		
		return StringEscapeUtils.unescapeHtml4(HandlebarsTemplateUtil.execute(tplPath, new DefaultModel()));
	}
}
