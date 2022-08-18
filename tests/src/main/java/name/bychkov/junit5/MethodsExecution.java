package name.bychkov.junit5;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Set all test-methods of class specified execution mode
 * 
 * @author Vladimir V. Bychkov
 */
@Retention(SOURCE)
@Target({ TYPE })
@Documented
public @interface MethodsExecution
{
	ExecutionMode value();
}