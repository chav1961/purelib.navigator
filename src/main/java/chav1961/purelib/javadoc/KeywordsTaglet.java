package chav1961.purelib.javadoc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jdk.javadoc.doclet.Taglet.Location;

public class KeywordsTaglet extends AbstractTaglet {
	private static final Set<Location>	LOCATION;
	
	static {
		final Set<Location>	temp = new HashSet<>();
		
		temp.add(Location.CONSTRUCTOR);		
		temp.add(Location.FIELD);		
		temp.add(Location.METHOD);		
		temp.add(Location.OVERVIEW);		
		temp.add(Location.PACKAGE);		
		temp.add(Location.TYPE);		
		LOCATION = Collections.unmodifiableSet(temp);
	}
	
	@Override public String getName() {return PureLibDoclet.TAGNAME_KEYWORDS;}

	@Override
	public Set<Location> getAllowedLocations() {
		return LOCATION;
	}
	
//	public static void register(final Map<String,Taglet> tagletMap) {
//		register(tagletMap,new KeywordsTaglet());
//	}
}
