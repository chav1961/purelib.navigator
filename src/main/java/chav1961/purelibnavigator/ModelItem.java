package chav1961.purelibnavigator;

public interface ModelItem extends Comparable<ModelItem> {
	public enum SourceType {
		CONSUMER, PRODUCER 
	}

	public enum Restrictions {
		NONE, RESTRICT, PROCESS_OVERFLOW
	}
	
	SourceType getSourceType();
	TableRepo.ContentType getContentType();
	AncestorNode[] getAncestors();
	int getPriority();
	int getAmountAwaited();
	boolean needClearAfterTick();
	Restrictions getRestrictions();
	void setAmountAwaited(int amount);
}
