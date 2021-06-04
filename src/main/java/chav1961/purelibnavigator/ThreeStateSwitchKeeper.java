package chav1961.purelibnavigator;

public interface ThreeStateSwitchKeeper {
	public enum SwitchState {
		ALL_OFF, LEFT_ON, RIGHT_ON;
	}
	
	SwitchState getState();
}
