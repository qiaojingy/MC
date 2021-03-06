package cs224n.features;

public class FeatureValue {
	private final String FEATURE_NAME;
	private double value;

	public FeatureValue(String name, double value) {
		this.FEATURE_NAME = name;
		this.value = value;
	}

	public String getName() {
		return FEATURE_NAME;
	}

	public double getValue() {
		return value;
	}

	public String toString() {
		return this.getName() + ": " + this.getValue();
	}
}
