package com.github.sourguice.call;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.annotation.CheckForNull;

import com.github.sourguice.annotation.controller.Callable;
import com.github.sourguice.annotation.request.PathVariable;
import com.github.sourguice.annotation.request.PathVariablesMap;
import com.github.sourguice.annotation.request.RequestMapping;
import com.github.sourguice.throwable.invocation.HandledException;
import com.github.sourguice.throwable.invocation.NoSuchRequestParameterException;

/**
 * Class that permits to call @{@link Callable} annotated methods like @{@link RequestMapping}
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public interface SGCaller {

	/**
	 * Executes a call to a given method
	 * The object on which to call the method will be retrived from Guice,
	 * therefore, the given class must be registered in Guice (or it will be instanciate by Guice via Just-In-Time binding)
	 *
	 * @param clazz The class of the controller of the method
	 * @param method The method to call
	 * @param pathVariables The URIPathVariables object to use to retrieve @{@link PathVariable} annotated method parameters
	 * @param throwWhenHandled throwWhenHandled Whether or not to throw a {@link HandledException} when an exception has been thrown AND handled by the Exception Service
	 *        This should mainly be set to false. It should be set to true when you want to prevent treatment on the returned object when an exception has been thrown, even when handled.
	 * @return The object returned by the method
	 *         If throwWhenHandled is false and an exception has been thrown AND handled, will return null
	 * @throws HandledException Only when throwWhenHandled is true and an exception has been thrown AND handled
	 * @throws NoSuchRequestParameterException When retrieval of parameter annotated with anotations from the {@link com.github.sourguice.annotation.request} package failed
	 * @throws InvocationTargetException Any exception thrown by the method being invoked
	 * @throws IOException IO failure while writing the response
	 */
	public @CheckForNull Object call(Class<?> clazz, Method method, @CheckForNull @PathVariablesMap Map<String, String> pathVariables, boolean throwWhenHandled) throws HandledException, NoSuchRequestParameterException, InvocationTargetException, IOException;

}
