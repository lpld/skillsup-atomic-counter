package ua.dp.skillsup;


import org.openjdk.jmh.annotations.*;

@Fork(1)
@State(Scope.Group)
public class CountersBenchmark {
	private Counter counter;

	@Param
	CounterFactory.CounterType counterType;

	@Setup
	public void buildMeCounterHearty() {
		counter = CounterFactory.build(counterType);
	}

	@Benchmark
	@Group("rw")
	@GroupThreads(100)
	public void inc() {
		counter.inc();
	}

	@Benchmark
	@Group("rw")
	@GroupThreads(1)
	public long get() {
		return counter.get();
	}
}