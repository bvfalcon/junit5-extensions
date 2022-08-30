package name.bychkov.junit5.params.provider;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ArgumentsSource(MethodArgumentsProvider.class)
public @interface MethodSource {

	String[] value() default "";

}
