package org.timecrunch;

import java.util.LinkedList;
import java.lang.Long;

public class FairQScheme implements IQScheme {
	private class IndexedFifoQ extends FifoQScheme {
		int mEventGroupId;
		long mVTime;

		public IndexedFifoQ(int capacity,int eventGroupId) {
			super(capacity);
			mEventGroupId = eventGroupId;
			mVTime = -1;
		}

		public boolean enqueue(ISchedulable event,long vtime) {
			if(size() == 0) {
				mVTime = vtime + event.size();
			}

			return enqueue(event);
		}

		@Override
		public ISchedulable dequeue() {
			ISchedulable temp = super.dequeue();
			if(size() != 0) {
				mVTime += peek().size();
			}

			return temp;
		}

		public long getVTime() {
			return mVTime;
		}

		public void wrapVTime() {
			mVTime += Long.MAX_VALUE + 1;
		}
	}

	private LinkedList<IndexedFifoQ> mData;
	private int mQueueCapacity;
	private long mMinVTime;

	public FairQScheme(int size) {
		mData = new LinkedList<IndexedFifoQ>();
		mQueueCapacity = size;
		mMinVTime = 0;
	}

	public boolean enqueue(ISchedulable event) {
		for(IndexedFifoQ ifq : mData) {
			if(ifq.mEventGroupId == event.eventGroupId()) {
				return ifq.enqueue(event);
			}
		}

		IndexedFifoQ temp = new IndexedFifoQ(mQueueCapacity,event.eventGroupId());
		temp.enqueue(event,mMinVTime);
		mData.add(temp);

		return true;
	}

	public ISchedulable dequeue() {
		long minVTime = -1;
		int i = 0;
		int minQueue = 0;
		for(IndexedFifoQ ifq : mData) {
			if(ifq.getVTime() > 0 && (minVTime < 0 || ifq.getVTime() < minVTime)) {
				minVTime = ifq.getVTime();
				minQueue = i;
			}
		}

		if(minVTime == -1) {
			for(IndexedFifoQ ifq : mData) {
				ifq.wrapVTime();
			}
			return dequeue();
		}

		mMinVTime = minVTime;
		return mData.get(minQueue).dequeue();
	}

	// TODO: Optimize so that a peek followed by dequeue is fast (same ops)
	public ISchedulable peek() {
		long minVTime = -1;
		int i = 0;
		int minQueue = 0;
		for(IndexedFifoQ ifq : mData) {
			if(ifq.getVTime() > 0 && (minVTime < 0 || ifq.getVTime() < minVTime)) {
				minVTime = ifq.getVTime();
				minQueue = i;
			}
		}

		if(minVTime == -1) {
			for(IndexedFifoQ ifq : mData) {
				ifq.wrapVTime();
			}
			return peek();
		}

		return mData.get(minQueue).peek();
	}

	public int size() {
		int size = 0;
		for(IndexedFifoQ ifq : mData) {
			size += ifq.size();
		}

		return size;
	}

}
