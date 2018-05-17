package com.bagri.client.hazelcast.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bagri.core.api.ResultCursor;

public class CombinedCursorImpl<T> implements ResultCursor<T> {
	
    private final static Logger logger = LoggerFactory.getLogger(CombinedCursorImpl.class);
	
    private int limit;
	protected List<ResultCursor<T>> results = new ArrayList<>();
	
	public CombinedCursorImpl() {
		this(0);
	}
	
	public CombinedCursorImpl(int limit) {
		this.limit = limit;
	}

	@Override
	public void close() throws Exception {
		for (ResultCursor<T> rc: results) {
			rc.close();
		}
		//index = 0;
		// close iterators?
	}
	
	public void addResults(ResultCursor<T> result) {
		results.add(result);
	}

	@Override
	public boolean isAsynch() {
		for (ResultCursor<T> cursor: results) {
			if (cursor.isAsynch()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isComplete() {
		for (ResultCursor<T> cursor: results) {
			if (!cursor.isComplete()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		for (ResultCursor<T> cursor: results) {
			if (!cursor.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Iterator<T> iterator() {
		return new CombinedCursorIterator<>();
	}
	
	@Override
	public int size() {
		return UNKNOWN;
	}

	
	private class CombinedCursorIterator<T> implements Iterator<T> {

		private int resIndex = 0;
		private int curIndex = 0; //-1;
		private Iterator<T> curIter = null;
		private ResultCursor<T> curResult = null;
		
		@Override
		public boolean hasNext() {
			if (limit > 0 && resIndex >= limit) {
				logger.trace("hasNext; limit: {}, index: {}", limit, resIndex);
				return false;
			}
			if (curResult == null) {
				if (results.isEmpty()) {
					return false;
				}
				if (curIndex < results.size()) {
					curResult = (ResultCursor<T>) results.get(curIndex);
					curIter = curResult.iterator();
				} else {
					return false;
				}
				logger.trace("hasNext; got index: {}; results: {}", curIndex, curResult);
			}
			if (curIter.hasNext()) {
				return true;
			} else {
				if (curResult.isComplete()) {
					logger.trace("hasNext; complete");
					curResult = null;
					curIndex++;
					return hasNext();
				} else {
					// could get asynch results..
					return curIter.hasNext();
				}
			}
		}
	
		@Override
		public T next() {
			if (curIter == null) {
				if (!hasNext()) {
					throw new NoSuchElementException("No more elements in the cursor");
				}
			}
			resIndex++;
			return (T) curIter.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("remove() method not implemented");
		}
	
	}


}
