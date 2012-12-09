//package org.timecrunch;

public class FifoQScheme implements IQScheme {
	private ISchedulable[] mData;
	private int mHead;
	private int mTail;
	private int mSize;

	public FifoQScheme(int size) {
		mData = new ISchedulable[size];
		mHead = 0;
		mTail = 0;
		mSize = 0;
	}

	public boolean enqueue(ISchedulable event) {
		if(mSize == mData.length) {
			return false;
		}

		mData[mTail++] = event;
		if(mTail == mData.length) {
			mTail = 0;
		}

		mSize++;

		return true;
	}

	public ISchedulable dequeue() {
		if(mSize == 0) {
			return null;
		}

		ISchedulable event = mData[mHead++];
		if(mHead == mData.length) {
			mHead = 0;
		}

		mSize--;
		return event;

	}

	public ISchedulable peek() {
		if(mSize == 0) {
			return null;
		}

		ISchedulable event = mData[mHead];
		return event;
	}

	public int size() {
		return mSize;
	}

	public int capacity() {
		return mData.length;
	}

}
