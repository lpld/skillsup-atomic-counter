package ua.dp.skillsup;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author leopold
 * @since 15/7/14
 */
public class ResizeableArrayCounter implements Counter {

  private static final int HASH_INCREMENT = 0x61c88647;
  private static final int INITIAL_BUFFER_SIZE = 2;
  private static final int INCREMENT_MAX_ATTEMPTS = 5;
  private static final AtomicInteger nextHashCode = new AtomicInteger();

  private final AtomicBoolean bufferIsResized = new AtomicBoolean(false);
  private volatile AtomicLong[] counters = new AtomicLong[INITIAL_BUFFER_SIZE];

  private static final ThreadLocal<Integer> hashCodeLocal = new ThreadLocal<Integer>() {
    @Override
    protected Integer initialValue() {
      return nextHashCode();
    }
  };

  public ResizeableArrayCounter() {

    for (int i = 0; i < INITIAL_BUFFER_SIZE; i++) {
      counters[i] = new AtomicLong();
    }
  }

  @Override
  public void inc() {

    boolean updated = false;

    do {
      AtomicLong[] counterLocal = counters;

      AtomicLong bucket = counterLocal[hashCodeLocal.get() & (counterLocal.length - 1)];

      // the array may ne not initialized yet:
      if (bucket == null) {
        continue;
      }
      int attempts = 0;

      do {
        long current = bucket.get();
        updated = bucket.compareAndSet(current, current + 1);
        attempts++;
      } while (attempts <= INCREMENT_MAX_ATTEMPTS && !updated);

      if (!updated) {
        // if the reference is changed there's no need to resize the array.
        if (!bufferIsResized.get() &&
            bufferIsResized.compareAndSet(false, true) &&
            counterLocal == counters) {
          resizeBuffer();

          bufferIsResized.lazySet(false);

          // initializing new part of buffer:
          for (int i = counters.length / 2; i < counters.length; i++) {
            counters[i] = new AtomicLong();
          }
        }

      }
    } while (!updated);


  }

  private void resizeBuffer() {
    int oldSize = counters.length;
    int newSize = oldSize * 2;
    AtomicLong[] newBuffer = new AtomicLong[newSize];
    System.arraycopy(counters, 0, newBuffer, 0, oldSize);

    counters = newBuffer;
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
