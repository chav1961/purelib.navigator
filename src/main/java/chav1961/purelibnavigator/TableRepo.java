package chav1961.purelibnavigator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import chav1961.purelib.basic.subscribable.SubscribableInt;

public class TableRepo implements Iterable<TableRepo.Content> {
	public enum  ContentType {
		ITEM_1,
		ITEM_2,
		ITEM_3;
	}

	private final Map<ContentType,Content>	content = new HashMap<>();
	
	public TableRepo() {
		for (ContentType item : ContentType.values()) {
			content.put(item, new Content(item));
		}
	}

	public SubscribableInt getAmount(final ContentType type) {
		if (type == null) {
			throw new NullPointerException("Type can't be null");
		}
		else {
			return content.get(type).getAmount();
		}
	}
	
	@Override
	public Iterator<Content> iterator() {
		return null;
	}
	
	public static class Content {
		private final ContentType		type;
		private final SubscribableInt	amount = new SubscribableInt();
		
		public Content(ContentType type) {
			this.type = type;
		}
		
		public ContentType getType() {
			return type;
		}
		
		public SubscribableInt getAmount() {
			return amount;
		}

		@Override
		public String toString() {
			return "Content [type=" + type + ", amount=" + amount + "]";
		}
	}
}
