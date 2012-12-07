package org.timecrunch;

import java.util.LinkedList;
import java.lang.Long;

public class InfFairQScheme implements IQScheme {
	private class InfIndexedFifoQ extends InfFifoQScheme {
		int mEventGroupId;
		long mVTime;

		public InfIndexedFifoQ(int eventGroupId) {
			super();
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

	private LinkedList<InfIndexedFifoQ> mData;
	private long mMinVTime;

	public InfFairQScheme() {
		mData = new LinkedList<InfIndexedFifoQ>();
		mMinVTime = 0;
	}

	public boolean enqueue(ISchedulable event) {
		for(InfIndexedFifoQ ifq : mData) {
			if(ifq.mEventGroupId == event.eventGroupId()) {
				return ifq.enqueue(event);
			}
		}

		InfIndexedFifoQ temp = new InfIndexedFifoQ(event.eventGroupId());
		temp.enqueue(event,mMinVTime);
		mData.add(temp);

		return true;
	}

	public ISchedulable dequeue() {
		long minVTime = -1;
		int i = 0;
		int minQueue = 0;
		for(InfIndexedFifoQ ifq : mData) {
			if(ifq.getVTime() > 0 && (minVTime < 0 || ifq.getVTime() < minVTime)) {
				minVTime = ifq.getVTime();
				minQueue = i;
			}
		}

		if(minVTime == -1) {
			for(InfIndexedFifoQ ifq : mData) {
				ifq.wrapVTime();
			}
			return dequeue();
		}

		mMinVTime = minVTime;
		return mData.get(minQueue).dequeue();
	}

	public ISchedulable peek() {
		long minVTime = -1;
		int i = 0;
		int minQueue = 0;
		for(InfIndexedFifoQ ifq : mData) {
			if(ifq.getVTime() > 0 && (minVTime < 0 || ifq.getVTime() < minVTime)) {
				minVTime = ifq.getVTime();
				minQueue = i;
			}
		}

		if(minVTime == -1) {
			for(InfIndexedFifoQ ifq : mData) {
				ifq.wrapVTime();
			}
			return peek();
		}

		return mData.get(minQueue).peek();
	}

	public int size() {
		int size = 0;
		for(InfIndexedFifoQ ifq : mData) {
			size += ifq.size();
		}

		return size;
	}

}
