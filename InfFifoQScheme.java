package org.timecrunch;

import java.util.ArrayDeque;

public class InfFifoQScheme implements IQScheme {
	private ArrayDeque<ISchedulable> mData;

	public InfFifoQScheme() {
		mData = new ArrayDeque<ISchedulable>();
	}

	public boolean enqueue(ISchedulable event) {
		mData.addLast(event);
		return true;
	}

	public ISchedulable dequeue() {
		ISchedulable event;
		event = mData.peek();
		if(event == null) {
			return event;
		}

		event = mData.removeFirst();

		return event;

	}

	public ISchedulable peek() {
		return mData.peek();
	}

	public int size() {
		return mData.size();
	}

}
