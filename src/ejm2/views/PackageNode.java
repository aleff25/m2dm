package ejm2.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.quasar.juse.api.JUSE_ProgramingFacade;
import org.tzi.use.uml.sys.MObject;

import java.util.HashMap;

public class PackageNode {
	
	String name;
	List<PackageNode> children = new ArrayList<>();
	List<ClassNode> classes = new ArrayList<>();
	PackageNode parent;
	boolean containsMain = false;
	Map<String, String> metrics = new HashMap<>();
	
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
	
	void addMetrics(Map<String, String> metrics, JUSE_ProgramingFacade api) {
		this.metrics.putAll(metrics);
        MObject mObject = api.allObjects().stream().filter(obj -> obj.name().endsWith(name)).findFirst().get();
        System.out.println("Object:" + mObject);
        
		for (String key: metrics.keySet()) {

            if (mObject != null) {            	
            	String value = api.oclEvaluator(mObject.name() + "." + key + "()").toString();
            	this.metrics.put(key, value);
            }
		}
		
		for (PackageNode child : children) {
			MObject mObjectChild = api.allObjects().stream().filter(obj -> obj.name().endsWith(child.name)).findFirst().get();
			for (String key: metrics.keySet()) {

	            if (mObjectChild != null) {            	
	            	String value = api.oclEvaluator(mObjectChild.name() + "." + key + "()").toString();
	            	child.metrics.put(key, value);
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
	
	
	public String getMetricByKey(String key) {
		return metrics.getOrDefault(key, "");
	}
	
}
