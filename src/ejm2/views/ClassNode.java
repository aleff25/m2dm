package ejm2.views;

import java.util.HashMap;
import java.util.Map;

public class ClassNode {

	String name;
	PackageNode parent;
	boolean isMain = false;
	Map<String, Object> metrics = new HashMap<>();
	
	public ClassNode(String name, boolean isMain, Map<String, Object> metrics) {
		super();
		this.name = name;
		this.isMain = isMain;
		this.metrics = metrics;
	}
	
	public String getMetricByKey(String key) {
		return metrics.getOrDefault(key, "").toString();
	}
}
