package chav1961.purelibnavigator.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator.OfDouble;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.DoubleToIntFunction;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongToIntFunction;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import chav1961.purelib.basic.growablearrays.GrowableIntArray;

public class TestIntStream implements IntStream {
	private final IntStream				parent;
	private final LongStream			parentLong;
	private final LongToIntFunction		lif;
	private final DoubleStream			parentDouble;
	private final DoubleToIntFunction	dif;
	private List<Runnable>				closers = null;
	private IntPredicate				filter = null;
	private Set<Integer>				distincts = null;
	private IntUnaryOperator			unaryOp = null;
	private IntFunction<? extends IntStream> mapper = null;
	private boolean						needSort = false;
	private long						limit = Long.MIN_VALUE;
	private long						skip = Long.MIN_VALUE;
	private OfInt						iterator = null;
	
	public TestIntStream(final LongStream ls, final LongToIntFunction f) {
		this.parent = null;
		this.parentLong = ls;
		this.lif = f;
		this.parentDouble = null;
		this.dif = null;
	}

	public TestIntStream(final DoubleStream ds, final DoubleToIntFunction f) {
		this.parent = null;
		this.parentLong = null;
		this.lif = null;
		this.parentDouble = ds;
		this.dif = f;
	}

	public TestIntStream(final IntStream is) {
		this.parent = is;
		this.parentLong = null;
		this.lif = null;
		this.parentDouble = null;
		this.dif = null;
	}

	public TestIntStream(final IntStream is, final IntFunction<? extends IntStream> mapper) {
		this.parent = is;
		this.parentLong = null;
		this.lif = null;
		this.parentDouble = null;
		this.dif = null;
	}
	
	@Override
	public boolean isParallel() {
		return false;
	}

	@Override
	public IntStream unordered() {
		return this;
	}

	@Override
	public IntStream onClose(final Runnable closeHandler) {
		if (closers == null) {
			closers = new ArrayList<>();
		}
		closers.add(closeHandler);
		return this;
	}

	@Override
	public void close() {
		if (closers != null) {
			for (Runnable item : closers) {
				try{
					item.run();
				} catch (Exception exc) {
				}
			}
		}
		if (parent != null) {
			parent.close();
		}
	}

	@Override
	public IntStream filter(final IntPredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("predicate can't be null");
		}
		else {
			if (filter == null) {
				filter = predicate;
				return this;
			}
			else {
				return new TestIntStream(this).filter(predicate);
			}
		}
	}

	@Override
	public IntStream map(final IntUnaryOperator mapper) {
		if (unaryOp == null) {
			unaryOp = mapper;
			return this;
		}
		else {
			return new TestIntStream(this).map(mapper);
		}
	}

	@Override
	public <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
		return new TestObjectStream<U>(this, mapper);
	}

	@Override
	public LongStream mapToLong(final IntToLongFunction mapper) {
		return new TestLongStream(this, mapper);
	}

	@Override
	public DoubleStream mapToDouble(final IntToDoubleFunction mapper) {
		return new TestDoubleStream(this, mapper);
	}

	@Override
	public IntStream flatMap(final IntFunction<? extends IntStream> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream distinct() {
		if (distincts != null) {
			distincts = new HashSet<>();
		}
		return this;
	}

	@Override
	public IntStream sorted() {
		if (!needSort) {
			needSort = true;
			return this;
		}
		else {
			return new TestIntStream(this).sorted();
		}
	}

	@Override
	public IntStream peek(final IntConsumer action) {
		if (action == null) {
			throw new NullPointerException("Action acnt be null");
		}
		else {
			return filter((e)->{
				action.accept(e);
				return true;
			});
		}
	}

	@Override
	public IntStream limit(final long maxSize) {
		if (limit == Long.MIN_VALUE) {
			limit = maxSize;
			return this;
		}
		else {
			return new TestIntStream(this).limit(maxSize);
		}
	}

	@Override
	public IntStream skip(final long n) {
		if (skip == Long.MIN_VALUE) {
			skip = n;
			return this;
		}
		else {
			return new TestIntStream(this).skip(n);
		}
	}

	@Override
	public void forEach(final IntConsumer action) {
		while (toNextValue()) {
			action.accept(getNextValue());
		}
	}

	@Override
	public void forEachOrdered(final IntConsumer action) {
		sorted().forEach(action);
	}

	@Override
	public int[] toArray() {
		final GrowableIntArray	gia = new GrowableIntArray(false);
		
		while (toNextValue()) {
			gia.append(getNextValue());
		}
		return gia.extract();
	}

	@Override
	public int reduce(final int identity, final IntBinaryOperator op) {
		int result = identity;
		
		while (toNextValue()) {
			result = op.applyAsInt(result, getNextValue()); 
		}
		return result;
	}

	@Override
	public OptionalInt reduce(final IntBinaryOperator op) {
		if (toNextValue()) {
			return OptionalInt.of(reduce(op.applyAsInt(0,getNextValue()),op));
		}
		else {
			return OptionalInt.empty();
		}
	}

	@Override
	public <R> R collect(final Supplier<R> supplier, final ObjIntConsumer<R> accumulator, final BiConsumer<R, R> combiner) {
		final R	result = supplier.get();
		
		while (toNextValue()) {
			accumulator.accept(result, getNextValue());
		}
		return result;
	}

	@Override
	public int sum() {
		int	sum = 0;
		
		while (toNextValue()) {
			sum += getNextValue();
		}
		return sum;
	}

	@Override
	public OptionalInt min() {
		if (toNextValue()) {
			int		lastMin = getNextValue();
			
			while (toNextValue()) {
				lastMin = Math.min(lastMin, getNextValue());
			}
			return OptionalInt.of(lastMin);
		}
		else {
			return OptionalInt.empty();
		}
	}

	@Override
	public OptionalInt max() {
		if (toNextValue()) {
			int		lastMax = getNextValue();
			
			while (toNextValue()) {
				lastMax = Math.max(lastMax, getNextValue());
			}
			return OptionalInt.of(lastMax);
		}
		else {
			return OptionalInt.empty();
		}
	}

	@Override
	public long count() {
		long	count = 0;
		
		while (toNextValue()) {
			count++;
		}
		return count;
	}

	@Override
	public OptionalDouble average() {
		if (toNextValue()) {
			long	sum = 0, count = 0;
			
			while (toNextValue()) {
				sum += getNextValue();
				count++;
			}
			return OptionalDouble.of(1.0 * sum / count);
		}
		else {
			return OptionalDouble.empty();
		}
	}

	@Override
	public IntSummaryStatistics summaryStatistics() {
		if (toNextValue()) {
			int		value = getNextValue(); 
			long	sum = value, count = 1;
			int		min = value, max = value;
			
			while (toNextValue()) {
				value = getNextValue();
				
				sum += value;
				count++;
				min = Math.min(min, value);
				max = Math.max(max, value);
			}
			return new IntSummaryStatistics(count, min, max, sum);
		}
		else {
			return new IntSummaryStatistics();
		}
	}

	@Override
	public boolean anyMatch(final IntPredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate to test can't be null"); 
		}
		else {
			while (toNextValue()) {
				if (predicate.test(getNextValue())) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public boolean allMatch(IntPredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate to test can't be null"); 
		}
		else {
			while (toNextValue()) {
				if (!predicate.test(getNextValue())) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public boolean noneMatch(final IntPredicate predicate) {
		if (predicate == null) {
			throw new NullPointerException("Predicate to test can't be null"); 
		}
		else {
			while (toNextValue()) {
				if (predicate.test(getNextValue())) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public OptionalInt findFirst() {
		if (toNextValue()) {
			return OptionalInt.of(getNextValue());
		}
		else {
			return OptionalInt.empty();
		}
	}

	@Override
	public OptionalInt findAny() {
		return findFirst();
	}

	@Override
	public LongStream asLongStream() {
		return new TestLongStream(this, (e)->(long)e);
	}

	@Override
	public DoubleStream asDoubleStream() {
		return new TestDoubleStream(this, (e)->(double)e);
	}

	@Override
	public Stream<Integer> boxed() {
		return mapToObj((val)->Integer.valueOf(val));
	}

	@Override
	public IntStream sequential() {
		return this;
	}

	@Override
	public IntStream parallel() {
		return this;
	}

	@Override
	public OfInt iterator() {
		return new OfInt() {
			@Override public boolean hasNext() {return toNextValue();}
			@Override public int nextInt() {return getNextValue();}
		};
	}

	@Override
	public Spliterator.OfInt spliterator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean toNextValue() {
		if (iterator == null) {
			if (parent != null) {
				iterator = parent.iterator();
			}
			else if (parentLong != null) {
				final OfLong	it = parentLong.iterator();
				
				iterator = new OfInt() {
					@Override public boolean hasNext() {return it.hasNext();}
					@Override public int nextInt() {return lif.applyAsInt(it.nextLong());}
				};
			}
			else if (parentDouble != null) {
				final OfDouble	it = parentDouble.iterator();
				
				iterator = new OfInt() {
					@Override public boolean hasNext() {return it.hasNext();}
					@Override public int nextInt() {return dif.applyAsInt(it.nextDouble());}
				};
			}
			else {
				throw new IllegalStateException();
			}
		}
		final boolean	next = iterator.hasNext();
		
		if (next) {
			if (skip != Long.MIN_VALUE) {
				while (skip > 0 && iterator.hasNext()) {
					skip--;
				}
				return toNextValue();
			}
			if (limit != Long.MIN_VALUE) {
			}
		}
		else {
			return false;
		}
		return false;
	}
	
	private int getNextValue() {
		return 0;
	}
}
