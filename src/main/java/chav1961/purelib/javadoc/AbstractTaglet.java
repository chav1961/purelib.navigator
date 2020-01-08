package chav1961.purelib.javadoc;


import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;

import com.sun.source.doctree.DocTree;

import jdk.javadoc.doclet.*;

abstract class AbstractTaglet implements Taglet {
	@Override public abstract String getName();
	@Override public abstract Set<Location> getAllowedLocations();
	
	@Override
	public boolean isInlineTag() {
		return false;
	}


	@Override
	public String toString(List<? extends DocTree> tags, Element element) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public String toString(final Tag tag) {
//		return tag.text();
//	}
//
//	@Override
//	public String toString(final Tag[] tags) {
//		final StringBuilder	sb = new StringBuilder();
//		
//		for (Tag item : tags) {
//			sb.append(',').append(item.text());
//		}
//		return sb.delete(0,0).toString();
//	}
//
//	static void register(final Map<String,Taglet> tagletMap, final Taglet tag) {
//		final Taglet 	old = tagletMap.get(tag.getName());
//		
//		if (old != null) {
//			tagletMap.remove(tag.getName());
//		}
//		tagletMap.put(tag.getName(), tag);
//    }
}
