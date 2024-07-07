package ejm2.views;

import java.util.List;

import ejm2.tools.Metric;

public class MethodNode extends ClassNode {
	
	final String nameMethod;
	final List<Metric> metricsMethod;
	
    public MethodNode(String name, List<Metric> metrics) {
        this.nameMethod = name;
        this.metricsMethod = metrics;
    }
    
    @Override
    public String toString() {
    	return nameMethod + " (" + metricsMethod + ")";
    }
    
    public String getMethodMetric(String key) {
		return metricsMethod.stream()
				.filter(m -> m.toString().equals(key))
				.findFirst()
				.orElse(new Metric("", "", "", "false"))
				.ocl;
	}
}
