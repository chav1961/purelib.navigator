package chav1961.purelibnavigator;

import java.util.Arrays;

import chav1961.purelibnavigator.TableRepo.ContentType;

public class AncestorNode {
	private final TableRepo.ContentType	type;
	private final AncestorSource[]		sources;
	
	public AncestorNode(final TableRepo.ContentType type, final AncestorSource... sources) {
		if (type == null) {
			throw new NullPointerException("Type can't be null");
		}
		else if (sources == null || sources.length == 0) {
			throw new IllegalArgumentException("Ancestors can't be null or empty");
		}
		else {
			this.type = type;
			this.sources = sources;
		}
	}
	
	@Override
	public String toString() {
		return "AncestorNode [type=" + type + ", sources=" + Arrays.toString(sources) + "]";
	}

	public static class AncestorSource {
		private final TableRepo.ContentType type;
		private final int					required;
		
		public AncestorSource(ContentType type, int required) {
			this.type = type;
			this.required = required;
		}

		@Override
		public String toString() {
			return "AncestorSource [type=" + type + ", required=" + required + "]";
		}
	}
}
