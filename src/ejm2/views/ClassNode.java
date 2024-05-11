package ejm2.views;

public class ClassNode {

	String name;
	int loc;
	int methods;
	double coverage;
	int complexity;
	PackageNode parent;
	boolean isMain = false;
	
	
	public ClassNode(String name, int loc, int methods, double coverage, int complexity, boolean isMain) {
		super();
		this.name = name;
		this.loc = loc;
		this.methods = methods;
		this.coverage = coverage;
		this.complexity = complexity;
		this.isMain = isMain;
	}
	
	
}
