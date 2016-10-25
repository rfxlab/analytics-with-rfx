package common.utils;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import java.io.IOException;
import java.util.Map;

import rfx.core.util.StringPool;

public class GroovyScriptUtil {
	static GroovyScriptEngine groovyScriptEngine;
	static final String path = "./scripts/groovy-jobs/";
	static {
		try {			
			String[] roots = new String[] { path };
			groovyScriptEngine = new GroovyScriptEngine(roots);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Can not init GroovyScriptEngine!");			
		}
	}
	
	public static String runControllerScript(String controllerName, Map<String, String[]> params){
		String output;
		try {
			Binding binding = new Binding();
			binding.setVariable("params", params);
			groovyScriptEngine.run(controllerName+".groovy", binding);
			output = binding.getVariable("output").toString();
		} catch (Exception e) {			
			e.printStackTrace();
			output = StringPool.BLANK;
		}
		return output;
	}
}
