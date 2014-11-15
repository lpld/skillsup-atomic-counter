package ua.dp.skillsup;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author leopold
 * @since 11/7/14
 */
public class DivideArrayCounter implements Counter {

  private static final int HASH_INCREMENT = 0x61c88647;
  private static final int BUFFER_SIZE = 64;
  private static final int BUFFER_MASK = BUFFER_SIZE - 1;
  private static final AtomicInteger nextHashCode = new AtomicInteger();

  private final AtomicLong[] counters = new AtomicLong[BUFFER_SIZE];

  private static final ThreadLocal<Integer> hashCodeLocal = new ThreadLocal<Integer>() {
    @Override
    protected Integer initialValue() {
      return nextHashCode();
    }
  };

  public DivideArrayCounter() {
    for (int i = 0; i < counters.length; i++) {
      counters[i] = new AtomicLong();
    }
  }

  @Override
  public void inc() {
    counters[hashCodeLocal.get() & BUFFER_MASK].incrementAndGet();
  }

  @Override
  public long get() {
    long value = 0;
    for (AtomicLong counter : counters) {
      value += counter.get();
    }

    return value;
  }

  /**
   * Returns the next hash code.
   */
  private static int nextHashCode() {
    return nextHashCode.getAndAdd(HASH_INCREMENT);
  }
}
