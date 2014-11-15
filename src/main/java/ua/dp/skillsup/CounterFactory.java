package ua.dp.skillsup;

/**
 * @author Andrey Lomakin <a href="mailto:lomakin.andrey@gmail.com">Andrey Lomakin</a>
 * @since 03/11/14
 */
public class CounterFactory {
	public enum CounterType {
		ATOMIC, THREAD_LOCAL, DIVIDE_ARRAY
//      , DIVIDE_ONE, DIVIDE_TWO
	}

	public static Counter build(CounterType type) {
		switch (type) {
			case ATOMIC:
				return new AtomicCounter();
			case THREAD_LOCAL:
				return new ThreadLocalCounter();

            case DIVIDE_ARRAY:
              return new DivideArrayCounter();
//			case DIVIDE_TWO:
//				return new CounterDivideAndRuleOne();
		}

		throw new IllegalArgumentException();
	}
}