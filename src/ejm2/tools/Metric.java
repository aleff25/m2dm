package ejm2.tools;

import java.util.Objects;

public class Metric {
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
