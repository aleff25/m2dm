package ejm2.views;

import java.util.ArrayList;
import java.util.List;

import ejm2.tools.Metric;

public class ClassNode {

	String name;
	PackageNode parent;
	boolean isMain = false;
	List<Metric> metrics = new ArrayList<>();
	
	public ClassNode(String name, boolean isMain, List<Metric> metrics) {
		super();
		this.name = name;
		this.isMain = isMain;
		this.metrics = metrics;
	}
	
	public ClassNode() {}
	
	public String getMetricByKey(String key) {
		return metrics.stream()
				.filter(m -> m.toString().equals(key))
				.findFirst()
				.orElse(new Metric("", "", "", "false"))
				.ocl;
	}
}
