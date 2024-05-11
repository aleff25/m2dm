package ejm2.views;

import java.util.ArrayList;
import java.util.List;

public class PackageNode {
	
	String name;
	List<PackageNode> children = new ArrayList<>();
	List<ClassNode> classes = new ArrayList<>();
	PackageNode parent;
	boolean containsMain = false;
	
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
}
