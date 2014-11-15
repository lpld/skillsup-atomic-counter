package ua.dp.skillsup;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author leopold
 * @since 11/7/14
 */
public class ThreadLocalCounter implements Counter {

  private final List<MutableLong> buckets = new CopyOnWriteArrayList<>();
  private final ThreadLocal<MutableLong> counters = new ThreadLocal<MutableLong>() {
    @Override
    protected MutableLong initialValue() {
      MutableLong counter = new MutableLong();
      buckets.add(counter);
      return counter;
    }
  };

  @Override
  public void inc() {
    counters.get().value++;
  }

  @Override
  public long get() {
    long value = 0;
    for (MutableLong counter : buckets) {
      value += counter.value;
    }

    return value;
  }

  private static final class MutableLong {
    private long value = 0;
  }
}
