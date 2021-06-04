package chav1961.purelibnavigator;

public interface ValueKeeper {
	public enum ValueType {
		AMOUNT, PRICE, SLIDER;
	}
	ValueType getType();
	int getValue();
	default int getMinValue() {return getValue();}
	default int getMaxValue() {return getValue();}
	boolean isHidden();
}
