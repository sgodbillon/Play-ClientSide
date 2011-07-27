package play.modules.clientside;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientRoute {
	/**
	 * Route category.
	 * @return
	 */
	String[] value() default {};
	boolean excludedFromAll() default false;
}
