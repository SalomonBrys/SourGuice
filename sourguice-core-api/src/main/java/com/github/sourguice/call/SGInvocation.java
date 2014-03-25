package com.github.sourguice.call;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.github.sourguice.throwable.invocation.HandledException;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;

/**
 * An invocation is an object that will call a method on a given object.
 * The parameters of this call are precalculated and given when needed by {@link ArgumentFetcher}s.
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public interface SGInvocation {

	/**
	 * This is where the magic happens: This will invoke the method by fetching all of its arguments and call it
	 *
	 * @param controller The object that will receive the method call
	 * @param throwWhenHandled Whether to throw a {@link HandledException} when an exception has been caught and handled.
	 *                         This allows to cancel all future work until the {@link HandledException} has been caught (and ignored).
	 * @return What the method call returned
	 * @throws NoSuchRequestParameterException In case of a parameter asked from request argument or path variable that does not exists
	 * @throws InvocationTargetException Any thing that the method call might have thrown
	 * @throws HandledException If an exception has been caught and handled. It is safe to ignore and used to cancel any depending work.
	 * @throws IOException IO failure while writing the response
	 */
	public abstract Object invoke(Object controller, boolean throwWhenHandled) throws NoSuchRequestParameterException, InvocationTargetException, HandledException, IOException;

}
