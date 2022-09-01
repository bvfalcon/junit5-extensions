package name.bychkov.junit5.params.provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @see org.junit.jupiter.params.provider.NullSource
 * */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.SOURCE)
public @interface NullSource
{
}
