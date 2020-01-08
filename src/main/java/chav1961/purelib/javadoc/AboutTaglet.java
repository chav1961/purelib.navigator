package chav1961.purelib.javadoc;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class AboutTaglet extends AbstractTaglet {
	private static final Set<Location>	LOCATION;
	
	static {
		final Set<Location>	temp = new HashSet<>();
		
		temp.add(Location.TYPE);		
		LOCATION = Collections.unmodifiableSet(temp);
	}
	
	@Override public String getName() {return PureLibDoclet.TAGNAME_ABOUT;}

	@Override
	public Set<Location> getAllowedLocations() {
		return LOCATION;
	}

//	public static void register(final Map<String,Taglet> tagletMap) {
//		register(tagletMap,new AboutTaglet());
//	}
}
