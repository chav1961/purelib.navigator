package chav1961.purelibnavigator.admin;

import java.util.HashSet;
import java.util.Set;

public class Balancer {
	public enum UnderflowType {
		RESTRICT, REJECT;
	}
	
	public enum ResourceType {
		TYPE1(UnderflowType.RESTRICT), 
		TYPE2(UnderflowType.RESTRICT, new ResourceAncestor(TYPE1, 1)),
		TYPE3(UnderflowType.RESTRICT, new ResourceAncestor(TYPE2, 1));
		
		private final UnderflowType			underflow;
		private final ResourceAncestor[]	ancestors;
		
		private ResourceType(final UnderflowType underflow, final ResourceAncestor... ancestors) {
			this.underflow = underflow;
			this.ancestors = ancestors;
		}
		
		public ResourceAncestor[] getAncestors() {
			return ancestors;
		}
		
		public UnderflowType getUnderflowType() {
			return underflow;
		}
	}
	
	public enum OperationType {
		OPER1, OPER2, OPER3;
	}
	
	public static class ResourceAncestor {
		private final ResourceType	type;
		private final int			amount;
		
		private ResourceAncestor(ResourceType type, int amount) {
			this.type = type;
			this.amount = amount;
		}

		@Override
		public String toString() {
			return "ResourceAncestor [type=" + type + ", amount=" + amount + "]";
		}

		public ResourceType getType() {
			return type;
		}

		public int getAmount() {
			return amount;
		}
	}

	public enum ResourceRepoType {
		REPO1(1),
		REPO2(2);
		
		private final int	prty;
		
		private ResourceRepoType(final int prty) {
			this.prty = prty;
		}
		
		public int getPriority() {
			return prty;
		}
	}
	
	public static class Resource {
		private final ResourceType	type;
		private int					total;
		private int					produced;
		private int					reserved;
		
		public Resource(ResourceType type, int total, int produced, int reserved) {
			this.type = type;
			this.total = total;
			this.produced = produced;
			this.reserved = reserved;
		}

		public int getTotal() {
			return total;
		}

		public void setTotal(int total) {
			this.total = total;
		}

		public int getProduced() {
			return produced;
		}

		public void setProduced(int produced) {
			this.produced = produced;
		}

		public int getReserved() {
			return reserved;
		}

		public void setReserved(int reserved) {
			this.reserved = reserved;
		}

		public ResourceType getType() {
			return type;
		}

		@Override
		public String toString() {
			return "Resource [type=" + type + ", total=" + total + ", produced=" + produced + ", reserved=" + reserved + "]";
		}
	}
	
	public static class ResourceRepo {
		private final ResourceRepoType	type;
		private final Resource[]		resources = new Resource[ResourceType.values().length];

		@FunctionalInterface
		private interface ResourceCallback {
			void process(ResourceType type, int amount);
		}
		
		public ResourceRepo(final ResourceRepoType type) {
			if (type == null) {
				throw new NullPointerException("Repo type can't be null");
			}
			else {
				this.type = type;
				
				for (ResourceType item : ResourceType.values()) {
					internalAdd(item, 0);
				}
			}
		}
		
		public ResourceRepoType getType() {
			return type;
		}
		
		public void add(final ResourceType type, final int amount) throws NullPointerException, IllegalArgumentException {
			if (type == null) {
				throw new NullPointerException("Resource type can't be null"); 
			}
			else if (amount < 0) {
				throw new IllegalArgumentException("Amount to add ["+amount+"] can't be negative"); 
			}
			else {
				internalAdd(type, amount);
			}
		}

		public int available(final ResourceType type) throws NullPointerException {
			if (type == null) {
				throw new NullPointerException("Resource type can't be null"); 
			}
			else {
				return internalBalance(type, resources[type.ordinal()].getTotal(), new HashSet<>(), (t,a)->{});
			}
		}
		
		public int reserve(final ResourceType type, final int amount) {
			if (type == null) {
				throw new NullPointerException("Resource type can't be null"); 
			}
			else if (amount < 0) {
				throw new IllegalArgumentException("Amount to add ["+amount+"] can't be negative"); 
			}
			else {
				final int	result = internalBalance(type, amount, new HashSet<>(), (t,a)->{});
				
				if (result > 0) {
					internalBalance(type, result, new HashSet<>(), (t,a)->resources[t.ordinal()].setReserved(resources[t.ordinal()].getReserved() + a));
				}
				return result;
			}
		}

		public int undoReserve(final ResourceType type, final int amount) {
			if (type == null) {
				throw new NullPointerException("Resource type can't be null"); 
			}
			else if (amount < 0) {
				throw new IllegalArgumentException("Amount to add ["+amount+"] can't be negative"); 
			}
			else {
				final int	canUndo = Math.max(0,resources[type.ordinal()].getTotal() - resources[type.ordinal()].getReserved());
				
				if (canUndo < amount) {
					for (ResourceAncestor item : type.getAncestors()) {
						undoReserve(item.getType(), item.getAmount() * (amount - canUndo));
					}
					resources[type.ordinal()].setReserved(resources[type.ordinal()].getReserved()-(amount - canUndo));
				}
				else {
					resources[type.ordinal()].setReserved(resources[type.ordinal()].getReserved()-amount);
				}
				return canUndo;
			}
		}
		
		public int remove(final ResourceType type, final int amount) {
			if (type == null) {
				throw new NullPointerException("Resource type can't be null"); 
			}
			else if (amount < 0) {
				throw new IllegalArgumentException("Amount to add ["+amount+"] can't be negative"); 
			}
			else {
				final int	result = internalBalance(type, amount, new HashSet<>(), (t,a)->{});
				
				if (result > 0) {
					internalBalance(type, result, new HashSet<>(), (t,a)->{
						resources[t.ordinal()].setReserved(resources[t.ordinal()].getReserved() - a);
						resources[t.ordinal()].setTotal(resources[t.ordinal()].getTotal() - a);
					});
				}
				return result;
			}
		}
		
		public int produce(final ResourceType type, final int amount) {
			if (type == null) {
				throw new NullPointerException("Resource type can't be null"); 
			}
			else if (amount < 0) {
				throw new IllegalArgumentException("Amount to add ["+amount+"] can't be negative"); 
			}
			else {
				int		produce = amount;
				
				for (ResourceAncestor item : type.getAncestors()) {
					for (; produce > 0; produce--) {
						final int	wanted = produce * item.getAmount();
						final int	got = internalBalance(type, produce, new HashSet<>(), (t,a)->{});
						
						if (wanted == got) {
							break;
						}
					}
				}
				if (produce > 0) {
					for (ResourceAncestor item : type.getAncestors()) {
						remove(item.getType(), produce * item.getAmount());
					}
				}
				return produce;
			}
		}
		
		private void internalAdd(final ResourceType type, final int amount) {
			if (resources[type.ordinal()] == null) {
				resources[type.ordinal()] = new Resource(type, 0, 0, 0); 
			}
			resources[type.ordinal()].setProduced(resources[type.ordinal()].getProduced() + amount);
			resources[type.ordinal()].setTotal(resources[type.ordinal()].getTotal() + amount);
		}
		
		private int internalBalance(final ResourceType type, final int amount, final Set<ResourceType> processed, final ResourceCallback callback) {
			if (!processed.contains(type)) {
				processed.add(type);

				int	reserved = Math.max(0, Math.min(amount, resources[type.ordinal()].getTotal() - resources[type.ordinal()].getReserved()));
				
				if (reserved < amount) {
					int		delta = amount - reserved;
					
					for (ResourceAncestor item : type.getAncestors()) {
						for(; delta > 0; delta--) {
							final int 	wanted = delta * item.getAmount();
							final int 	got = internalBalance(item.getType(), wanted, processed, callback);
							if (got == wanted) {
								callback.process(item.type, wanted);	
								break;
							}
						}
						reserved += delta;
					}
				}
				callback.process(type, reserved);	
				return reserved;
			}
			else {
				return 0;
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
