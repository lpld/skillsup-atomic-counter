package ua.dp.skillsup;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author leopold
 * @since 15/7/14
 */
public class ResizeableArrayCounter implements Counter {

  private static final int HASH_INCREMENT = 0x61c88647;
  private static final int INITIAL_BUFFER_SIZE = 64;
  private static final int INCREMENT_MAX_ATTEMPTS = 5;
  private static final AtomicInteger nextHashCode = new AtomicInteger();

  private int bufferSize = INITIAL_BUFFER_SIZE;
  private int bufferMask = bufferSize - 1;

  private final AtomicBoolean bufferIsResized = new AtomicBoolean(false);
  private volatile AtomicLong[] counters = new AtomicLong[bufferSize];

  private static final ThreadLocal<Integer> hashCodeLocal = new ThreadLocal<Integer>() {
    @Override
    protected Integer initialValue() {
      return nextHashCode();
    }
  };

  public ResizeableArrayCounter() {

    for (int i = 0; i < bufferSize; i++) {
      counters[i] = new AtomicLong();
    }
  }

  @Override
  public void inc() {
    AtomicLong bucket = counters[hashCodeLocal.get() & bufferMask];

    boolean updated;
    int attempts = 0;

    do {
      long current = bucket.get();
      updated = bucket.compareAndSet(current, current + 1);
      attempts++;
    } while (attempts <= INCREMENT_MAX_ATTEMPTS && !updated);

    if (!updated) {
      if (bufferIsResized.compareAndSet(false, true)) {
        resizeBuffer();

        bufferIsResized.lazySet(false);
      }

      // not sure about this:
      inc();
    }
  }

  private void resizeBuffer() {
    int newSize = bufferSize * 2;
    AtomicLong[] newBuffer = new AtomicLong[newSize];
    System.arraycopy(counters, 0, newBuffer, 0, bufferSize);
    for (int i = bufferSize; i < newSize; i ++) {
      newBuffer[i] = new AtomicLong();
    }
    counters = newBuffer;
    bufferSize = newSize;
    bufferMask = newSize - 1;
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
