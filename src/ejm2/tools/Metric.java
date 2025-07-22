package ejm2.tools;

import java.util.Objects;

public class Metric implements Comparable<Metric> {
    public String name;
    public String ocl;
    public String type;
    public boolean isActive;

    public Metric(String name, String ocl, String type, String isActive) {
        this.name = name;
        this.ocl = ocl;
        this.type = type;
        this.isActive = isActive.equalsIgnoreCase("true");
    }
    
    public Metric(String name, String ocl, String type, boolean isActive) {
        this.name = name;
        this.ocl = ocl;
        this.type = type;
        this.isActive = isActive;
    }
    
    public String getIsActive() {
    	return isActive ? "true" : "false";
    }
    
    public String transformToOCL() {
    	StringBuilder builder = new StringBuilder("");
    	builder.append("@metric" + type + "(active = \"" + getIsActive() + "\")\n");
    	builder.append(name + " () : Integer = " + ocl + "\n");
    	return builder.toString();
    }
    
    @Override
    public int compareTo(Metric other) {
    	int typeComparison = Integer.compare(getTypeOrder(this.type), getTypeOrder(other.type));
        if (typeComparison != 0) {
            return typeComparison;
        }
        return this.name.compareTo(other.name);
    }
    
    private int getTypeOrder(String type) {
        if (type == null) return 3;
        switch (type) {
            case "Package":
                return 0;
            case "Class":
                return 1;
            case "Method":
                return 2;
            default:
                return 3;
        }
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Metric other = (Metric) obj;
		return Objects.equals(name, other.name) && Objects.equals(type, other.type);
	}

	
}
