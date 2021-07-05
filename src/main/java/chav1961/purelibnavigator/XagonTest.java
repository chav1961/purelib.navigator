package chav1961.purelibnavigator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class XagonTest {
	private static final EntityAndRange[]	EAR = {new EntityAndRange(0,0,Entity.Capital)};
	private static final Random				rand = new Random(1234567890);
	
	public enum EntityGroup {
		City(true),
		Mine(true),
		Empty(false);
		
		private final boolean		theOnly;
		
		private EntityGroup(final boolean theOnly) {
			this.theOnly = theOnly;
		}

		public boolean isTheOnly() {
			return theOnly;
		}
	}
	
	public enum Entity {
		Capital(EntityGroup.City),
		City(EntityGroup.City),
		Entity3(EntityGroup.Mine),
		Empty(EntityGroup.Empty);
		
		private final EntityGroup	group;
		
		private Entity(final EntityGroup group) {
			this.group = group;
		}

		public EntityGroup getGroup() {
			return group;
		}
	}
	
	public enum Cnt {
		Cnt1(true, Entity.Capital, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City),		
		Cnt2(true, Entity.Capital, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City),		
		Cnt3(true, Entity.Capital, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City),		
		Cnt4(true, Entity.Capital, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City),		
		Cnt5(true, Entity.Capital, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City),
		Cnt6(true, Entity.Capital, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City),		
		Cnt7(true, Entity.Capital, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City, Entity.City),		
		Cnt8(false, Entity.Capital, Entity.City, Entity.City, Entity.City),	
		Cnt9(false, Entity.Capital, Entity.City, Entity.City, Entity.City),	
		Cnt10(false, Entity.Capital, Entity.City, Entity.City, Entity.City),
		Cnt11(false, Entity.Capital, Entity.City, Entity.City, Entity.City),	
		Cnt12(false, Entity.Capital, Entity.City, Entity.City, Entity.City),	
		Cnt13(false, Entity.Capital, Entity.City, Entity.City, Entity.City),	
		Cnt14(false, Entity.Capital, Entity.City, Entity.City, Entity.City),	
		Cnt15(false, Entity.Capital, Entity.City, Entity.City, Entity.City),
		Cnt16(false, Entity.Capital, Entity.City, Entity.City, Entity.City),	
		Cnt17(false, Entity.Capital, Entity.City, Entity.City, Entity.City),	
		Cnt18(false, Entity.Capital, Entity.City, Entity.City, Entity.City),	
		Cnt19(false, Entity.Capital, Entity.City, Entity.City, Entity.City),	
		Cnt20(false, Entity.Capital, Entity.City, Entity.City, Entity.City),
		Cnt21(false, Entity.Capital, Entity.City, Entity.City, Entity.City);
		
		private final boolean	imper;
		private final Entity[]	mandatories;
		
		private Cnt(final boolean imper, final Entity... mandatories) {
			this.imper = imper;
			this.mandatories = mandatories;
		}
		
		public boolean isImper() {
			return imper;
		}
		
		public Entity[] getMandatories() {
			return mandatories.clone();
		}
	}

	public static void main(String[] args) {
		final Cnt[]				values = Cnt.values();
		final Xagon[][]			content = new Xagon[values.length][];
		final Entity[][]		mandatories = new Entity[values.length][];
		final Entity[][]		optionals = new Entity[values.length][];
		final Entity[][]		totals = new Entity[values.length][];

		for (int index = 0, maxIndex = values.length; index < maxIndex; index++) {
			final int			size = values[index].isImper() ? 8 : 4;
			
			content[index] = new Xagon[size];
			mandatories[index] = values[index].getMandatories();
			
			for (int xagonIndex = 0; xagonIndex < size; xagonIndex++) {
				content[index][xagonIndex] = new Xagon();
			}
			
			final int			optCount = size * Xagon.getXagonSize() - mandatories[index].length;
			final Entity[]		item = new Entity[optCount];
			
			for (int optIndex = 0; optIndex < optCount; optIndex++) {
				item[optIndex] = buildEntity(scratch());
			}
			optionals[index] = item;
			totals[index] = new Entity[mandatories[index].length + optionals[index].length];
			
			System.arraycopy(mandatories[index], 0, totals[index], 0, mandatories[index].length);
			System.arraycopy(optionals[index], 0, totals[index], mandatories[index].length, optionals[index].length);
		}
		
		for (int index = 0, maxIndex = content.length; index < maxIndex; index++) {
			final Xagon[]		xagon = content[index];
			final Entity[]		total = totals[index];
			
			for (int inner = 0; inner < total.length; inner++) {
				long			scr = Math.abs(scratch());
				
				for(;;) {
					final int	where = (int) ((scr / Xagon.getXagonSize()) % xagon.length), whereInsize = (int) (scr % Xagon.getXagonSize());
		
					if (xagon[where].isFree(whereInsize) && (!total[inner].getGroup().isTheOnly() || !xagon[where].hasEntityGroup(total[inner].getGroup()))) {
						xagon[where].place(whereInsize, total[inner]);
						break;
					}
					scr++;
				} 
			}
		}
		System.err.println("Xagon: "+Arrays.deepToString(content));
	}

	private static Entity buildEntity(final long scratch) {
		for (EntityAndRange item : EAR) {
			if (item.inRange(scratch)) {
				return item.getEntity();
			}
		}
		return Entity.Empty;
	}

	private static long scratch() {
		return rand.nextLong();
	}
	
	public static class Xagon {
		private final Set<Entity>	entities = new HashSet<>();
		private final Entity[]		locations = new Entity[getXagonSize()];
		
		public static int getXagonSize() {
			return 7;
		}
		
		public boolean hasEntity(final Entity entity) {
			return entities.contains(entity);
		}

		public boolean hasEntityGroup(final EntityGroup group) {
			for (Entity item : entities) {
				if (item.getGroup() == group) {
					return true;
				}
			}
			return false;
		}
		
		public boolean isFree(final int position) {
			return locations[position] == null;
		}
		
		public boolean hasFree() {
			for (Entity item : locations) {
				if (item == null) {
					return true;
				}
			}
			return false;
		}
		
		public void place(final int position, final Entity entity) {
			locations[position] = entity;
			entities.add(entity);
		}

		@Override
		public String toString() {
			return "Xagon [entities=" + entities + ", locations=" + Arrays.toString(locations) + "]";
		}
	}
	
	private static class EntityAndRange {
		private final long		lowBound, highBound;
		private final Entity	entityAssociated;
		
		public EntityAndRange(long lowBound, long highBound, Entity entityAssociated) {
			this.lowBound = lowBound;
			this.highBound = highBound;
			this.entityAssociated = entityAssociated;
		}

		public boolean inRange(final long value) {
			return value >= lowBound && value <= highBound;
		}
		
		public Entity getEntity() {
			return entityAssociated;
		}
		
		@Override
		public String toString() {
			return "EntityAndRange [lowBound=" + lowBound + ", highBound=" + highBound + ", entityAssociated=" + entityAssociated + "]";
		}
	}
}
