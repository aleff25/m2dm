package ejm2.views;

import java.util.ArrayList;
import java.util.List;

import org.quasar.juse.api.JUSE_ProgramingFacade;
import org.tzi.use.uml.sys.MObject;

import ejm2.tools.Metric;

public class PackageNode {
	
	String name;
	List<PackageNode> children = new ArrayList<>();
	List<ClassNode> classes = new ArrayList<>();
	List<Metric> metrics = new ArrayList<>();
	PackageNode parent;
	boolean containsMain = false;
	String path;
	
	public PackageNode(String name) {
		this.name = name;
	}
	
	void addChild(PackageNode child){
		children.add(child);
		child.parent = this;
	}
	
	void addClass(ClassNode classNode) {
		classes.add(classNode);
		classNode.parent = this;
		if(classNode.isMain) {
			setMainClazz();
		}
	}
	
	void setMainClazz() {
		containsMain = true;
		if (parent != null) {
			parent.setMainClazz();
		}
	}
	
	void addMetrics(List<Metric> metrics, JUSE_ProgramingFacade api) {
		this.metrics.addAll(metrics);
        MObject mObject = api.allObjects().stream().filter(obj -> obj.name().endsWith(name)).findFirst().get();

		for (Metric metric: this.metrics) {

            if (mObject != null) {            	
            	String value = api.oclEvaluator(mObject.name() + "." + metric.name + "()").toString();
            	metric.ocl = value;
            }
		}
		
		for (PackageNode child : children) {
			MObject mObjectChild = api.allObjects().stream().filter(obj -> obj.name().endsWith(child.name)).findFirst().get();
			for (Metric childMetric: metrics) {
	            if (mObjectChild != null) {            	
	            	String value = api.oclEvaluator(mObjectChild.name() + "." + childMetric.name + "()").toString();
	            	childMetric.ocl = value;
	            }
			}
		}
	}
	
	PackageNode findOrAddChild(String name) {
        for (PackageNode child : children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        PackageNode newChild = new PackageNode(name);
        children.add(newChild);
        newChild.parent = this;
        return newChild;
    }
	
	public void setPackagePath(String path) {
		this.path = path;
	}
	
	
	public String getMetricByKey(String key) {
		return metrics.stream()
				.filter(m -> m.toString().equals(key))
				.findFirst()
				.orElse(new Metric("", "", "", "false"))
				.ocl;
	}
	
}
