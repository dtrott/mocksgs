package net.java.dev.mocksgs;

import static org.junit.Assert.assertEquals;

import java.util.PriorityQueue;

import org.junit.Before;
import org.junit.Test;

public class TaskQueueTest
{
	@Before
	public void setup()
	{
		MockSGS.init();
	}
	
	@Test
	public void verifyScheduler()
	{
		PriorityQueue<AbstractMockTaskHandle> tasks = new PriorityQueue<AbstractMockTaskHandle>();
		for (long i = 0; i < 50; ++i)
		{
			tasks.add(new MockPeriodicTaskHandle(null, -1, -1, i));
		}
		for (long i = 99; i >= 50; --i)
		{
			tasks.add(new MockPeriodicTaskHandle(null, -1, -1, i));
		}
		
		for (long i = 0; i < 100; ++i)
		{
			AbstractMockTaskHandle task = tasks.poll();
			assertEquals(i, task.getScheduleTime());
		}
	}
}
