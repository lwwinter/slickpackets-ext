package org.timecrunch;

public interface IQScheme {

	public boolean enqueue(ISchedulable event);

	public ISchedulable dequeue();

	public ISchedulable peek();

	public int size();

}
