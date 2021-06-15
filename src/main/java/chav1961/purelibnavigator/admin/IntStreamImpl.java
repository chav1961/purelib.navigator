package chav1961.purelibnavigator.admin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator.OfInt;
import java.util.PrimitiveIterator.OfLong;
import java.util.Set;
import java.util.PrimitiveIterator.OfDouble;
import java.util.function.BiConsumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import chav1961.purelib.basic.growablearrays.GrowableIntArray;

public class IntStreamImpl implements IntStream {
	private final OfInt			iterator;
	private final AutoCloseable	close;
	private List<Runnable>		onClose = null;
	
	public IntStreamImpl(final OfInt iterator, AutoCloseable close) {
		this.iterator = iterator;
		this.close = close;
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
		if (onClose == null) {
			onClose = new ArrayList<>();
		}
		onClose.add(closeHandler);
		return this;
	}

	@Override
	public void close() {
		try{close.close();
		} catch (Exception exc) {
		}
		
		if (onClose != null) {
			for (Runnable item : onClose) {
				try {
					item.run();
				} catch (Exception exc) {
				}
			}
		}
	}

	@Override
	public IntStream filter(final IntPredicate predicate) {
		return new IntStreamImpl(
				new OfInt() {
					int		lastValue;
					
					@Override
					public boolean hasNext() {
						while (iterator.hasNext()) {
							lastValue = iterator.nextInt();
							
							if (predicate.test(lastValue)) {
								return true;
							}
						}
						return false;
					}

					@Override
					public int nextInt() {
						return lastValue;
					}}
				, this::close);
	}

	@Override
	public IntStream map(final IntUnaryOperator mapper) {
		return new IntStreamImpl(new OfInt() {
					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}
		
					@Override
					public int nextInt() {
						return mapper.applyAsInt(iterator.nextInt());
					}}
				,this::close);
	}

	@Override
	public <U> Stream<U> mapToObj(IntFunction<? extends U> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LongStream mapToLong(final IntToLongFunction mapper) {
		return new LongStreamImpl(new OfLong() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public long nextLong() {
				return mapper.applyAsLong(iterator.nextInt());
			}}
		, this::close);
	}

	@Override
	public DoubleStream mapToDouble(IntToDoubleFunction mapper) {
		return new DoubleStreamImpl(new OfDouble() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public double nextDouble() {
				return mapper.applyAsDouble(iterator.nextInt());
			}}
		, this::close);
	}

	@Override
	public IntStream flatMap(IntFunction<? extends IntStream> mapper) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream distinct() {
		return new IntStreamImpl(new OfInt() {
				final Set<Integer>	set = new HashSet<>();
				int					value;
				
				@Override
				public boolean hasNext() {
					while (iterator.hasNext()) {
						value = iterator.nextInt();
						
						if (!set.contains(value)) {
							return true;
						}
					}
					return false;
				}
	
				@Override
				public int nextInt() {
					return value;
				}}
				, this::close);
	}

	@Override
	public IntStream sorted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IntStream peek(IntConsumer action) {
		return new IntStreamImpl(new OfInt() {
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}
	
				@Override
				public int nextInt() {
					final int	value = iterator.nextInt();
					
					action.accept(value);
					return value;
				}}
				, this::close);
	}

	@Override
	public IntStream limit(long maxSize) {
		return new IntStreamImpl(new OfInt() {
			long	limit = maxSize;
			
			@Override
			public boolean hasNext() {
				final boolean	hasNext = iterator.hasNext();
				
				if (hasNext) {
					return --limit >= 0;
				}
				else {
					return false;
				}
			}

			@Override
			public int nextInt() {
				return iterator.nextInt();
			}}
			, this::close);
	}

	@Override
	public IntStream skip(long n) {
		return new IntStreamImpl(new OfInt() {
			long	offset = n;
			
			@Override
			public boolean hasNext() {
				boolean	result;
				while ((result = iterator.hasNext()) && --offset >= 0);
				
				return result; 
			}

			@Override
			public int nextInt() {
				return iterator.nextInt();
			}}
			, this::close);
	}

	@Override
	public void forEach(IntConsumer action) {
		try {
			while (iterator.hasNext()) {
				action.accept(iterator.nextInt());
			}
		} finally {
			close();
		}
	}

	@Override
	public void forEachOrdered(IntConsumer action) {
		sorted().forEach(action);
	}

	@Override
	public int[] toArray() {
		try {
			final GrowableIntArray	gia = new GrowableIntArray(false);
			
			while (iterator.hasNext()) {
				gia.append(iterator.nextInt());
			}
			return gia.extract();
		} finally {
			close();
		}
	}

	@Override
	public int reduce(int identity, IntBinaryOperator op) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public OptionalInt reduce(IntBinaryOperator op) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> accumulator, BiConsumer<R, R> combiner) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int sum() {
		return (int)summaryStatistics().getSum();
	}

	@Override
	public OptionalInt min() {
		final IntSummaryStatistics	stat = summaryStatistics();
		
		if (stat.getCount() != 0) {
			return OptionalInt.of(summaryStatistics().getMin());
		}
		else {
			return OptionalInt.empty();
		}
	}

	@Override
	public OptionalInt max() {
		final IntSummaryStatistics	stat = summaryStatistics();
		
		if (stat.getCount() != 0) {
			return OptionalInt.of(summaryStatistics().getMax());
		}
		else {
			return OptionalInt.empty();
		}
	}

	@Override
	public long count() {
		return (int)summaryStatistics().getCount();
	}

	@Override
	public OptionalDouble average() {
		final IntSummaryStatistics	stat = summaryStatistics();
		
		if (stat.getCount() == 0) {
			return OptionalDouble.empty();
		}
		else {
			return OptionalDouble.of(1.0*stat.getSum()/stat.getCount());
		}
	}

	@Override
	public IntSummaryStatistics summaryStatistics() {
		try {
			long	sum = 0, count = 0;
			int		min, max;
			
			if (iterator.hasNext()) {
				int	value = iterator.nextInt();
				
				sum += value;
				count++;
				min = value;
				max = value;
				while (iterator.hasNext()) {
					value = iterator.nextInt();
					sum += value;
					count++;
					min = Math.min(min, value);
					max = Math.max(min, value);
				}
				return new IntSummaryStatistics(count,min,max,sum);
			}
			else {
				return new IntSummaryStatistics();
			}
		} finally {
			close();
		}
	}

	@Override
	public boolean anyMatch(IntPredicate predicate) {
		try {
			while (iterator.hasNext()) {
				if (predicate.test(iterator.nextInt())) {
					return true;
				}
			}
			return false;
		} finally {
			close();
		}
	}

	@Override
	public boolean allMatch(IntPredicate predicate) {
		try {
			while (iterator.hasNext()) {
				if (!predicate.test(iterator.nextInt())) {
					return false;
				}
			}
			return true;
		} finally {
			close();
		}
	}

	@Override
	public boolean noneMatch(IntPredicate predicate) {
		try {
			while (iterator.hasNext()) {
				if (predicate.test(iterator.nextInt())) {
					return false;
				}
			}
			return true;
		} finally {
			close();
		}
	}

	@Override
	public OptionalInt findFirst() {
		try {
			if (iterator.hasNext()) {
				return OptionalInt.of(iterator.nextInt());
			}
			else {
				return OptionalInt.empty();
			}
		} finally {
			close();
		}
	}

	@Override
	public OptionalInt findAny() {
		return findFirst();
	}

	@Override
	public LongStream asLongStream() {
		return mapToLong((e)->(long)e);
	}

	@Override
	public DoubleStream asDoubleStream() {
		return mapToDouble((e)->(double)e);
	}

	@Override
	public Stream<Integer> boxed() {
		return mapToObj((e)->Integer.valueOf(e));
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
		return iterator;
	}

	@Override
	public java.util.Spliterator.OfInt spliterator() {
		// TODO:
		return null;
	}
}
