package com.github.sourguice.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.CheckForNull;

/**
 * Annotations related utils
 *
 * @author Salomon BRYS <salomon.brys@gmail.com>
 */
public final class Annotations {

	/**
	 * Encapsulates an array of annotations into an AnnotatedElement
	 * For example, this can be used with the return of {@link Method#getParameterAnnotations()}
	 *
	 * @param annotations The array of annotations to encapsulate
	 * @return The encapsulation
	 */
	public static AnnotatedElement fromArray(final Annotation[] annotations) {
		return new AnnotatedElement() {
			@Override
			public boolean isAnnotationPresent(Class<? extends Annotation> clazz) {
				return getAnnotation(clazz) != null;
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return annotations;
			}

			@Override
			public Annotation[] getAnnotations() {
				return annotations;
			}

			@Override
			@SuppressWarnings("unchecked")
			public @CheckForNull <T extends Annotation> T getAnnotation(Class<T> clazz) {
				for (Annotation a : annotations) {
					if (a.annotationType().equals(clazz))
						return (T)a;
				}
				return null;
			}
		};
	}

	// ===================== ONE =====================

	/**
	 * Gets an annotation of a specific type OR an annotation annotated by the specific type
	 * and recursive in an array of annotations
	 *
	 * @param annotationClass The class of the annotation to search
	 * @param annotations The annotations to search in
	 * @return The found annotation or null
	 */
	public static @CheckForNull Annotation GetOneAnnotated(final Class<? extends Annotation> annotationClass, Annotation[] annotations) {
		final Arrays.Getter<Annotation> getter = new Arrays.Getter<>();
		return getter.get(annotations, new Arrays.Getter.Finder<Annotation, Annotation>() {
			@Override public Annotation findIn(Annotation obj) {
				if (obj.annotationType().equals(annotationClass))
					return obj;
				else
					if (getter.get(obj.annotationType().getAnnotations(), this) != null)
						return obj;
				return null;
			}
			@Override protected Object getCheck(Annotation obj) {
				return obj.annotationType();
			}
		});
	}

	/**
	 * Gets an annotation of a specific type from an array of annotations and their respectives annotations
	 *
	 * @param annotationClass The class of the annotation to search
	 * @param annotations The annotations to search in
	 * @return The found annotation or null
	 */
	@SuppressWarnings("unchecked")
	public static @CheckForNull <T extends Annotation> T GetOneRecursive(final Class<T> annotationClass, Annotation[] annotations) {
		final Arrays.Getter<T> getter = new Arrays.Getter<>();
		return getter.get(annotations, new Arrays.Getter.Finder<T, Annotation>() {
			@Override public T findIn(Annotation obj) {
				if (obj.annotationType().equals(annotationClass))
					return (T)obj;
				return getter.get(obj.annotationType().getAnnotations(), this);
			}
			@Override protected Object getCheck(Annotation obj) {
				return obj.annotationType();
			}
		});
	}

	/**
	 * Gets an annotation from an annotated elements and it's parent. (Member > class > Package)
	 *
	 * @param element The element from which to search the annotation
	 * @param annotationClass The class of the annotation to find
	 * @return The annotation if found or null
	 */
	public static @CheckForNull <T extends Annotation> T GetOneTree(Class<T> annotationClass, AnnotatedElement element) {

		T find = element.getAnnotation(annotationClass);
		if (find != null)
			return find;

		if (element instanceof Member)
			return GetOneTree(annotationClass, ((Member)element).getDeclaringClass());

		if (element instanceof Class) {
			Class<?> objClass = ((Class<?>)element).getSuperclass();
			while (objClass != null) {
				find = objClass.getAnnotation(annotationClass);
				if (find != null)
					return find;
				objClass = objClass.getSuperclass();
			}
			return GetOneTree(annotationClass, ((Class<?>)element).getPackage());
		}

		return null;
	}

	/**
	 * Gets an annotation from an annotated elements, the annotation's annotations,
	 * the element's parents' annotations, the element's parents' annotations' annotations, etc.
	 *
	 * @param element The element from which to search the annotation
	 * @param annotationClass The class of the annotation to find
	 * @return The annotation if found or null
	 */
	public static @CheckForNull <T extends Annotation> T GetOneTreeRecursive(Class<T> annotationClass, AnnotatedElement element) {
		T find = GetOneRecursive(annotationClass, element.getAnnotations());
		if (find != null)
			return find;

		if (element instanceof Member)
			return GetOneTreeRecursive(annotationClass, ((Member)element).getDeclaringClass());

		if (element instanceof Class) {
			Class<?> objClass = ((Class<?>)element).getSuperclass();
			while (objClass != null) {
				find = GetOneRecursive(annotationClass, objClass.getAnnotations());
				if (find != null)
					return find;
				objClass = objClass.getSuperclass();
			}
			return GetOneTreeRecursive(annotationClass, ((Class<?>)element).getPackage());
		}

		return null;
	}

	// ===================== ALL =====================

	/**
	 * Gets all annotations of a specific type and all annotations annotated by the specific type
	 * and recursive in an array of annotations
	 *
	 * @param annotationClass The class of the annotations to search
	 * @param annotations The annotations to search in
	 * @return A list of found annotations
	 */
	public static List<Annotation> GetAllAnnotated(final Class<? extends Annotation> annotationClass, Annotation[] annotations) {
		final Arrays.AllGetter<Annotation> getter = new Arrays.AllGetter<>();
		getter.find(annotations, new Arrays.AllGetter.Adder<Annotation, Annotation>() {
			@CheckForNull Annotation root = null;
			@Override public void addIn(List<Annotation> list, Annotation obj) {
				boolean rootIsEmpty = (root == null);
				if (rootIsEmpty)
					root = obj;
				if (obj.annotationType().equals(annotationClass))
					list.add(root);
				else
					getter.find(obj.annotationType().getAnnotations(), this);
				if (rootIsEmpty)
					root = null;
			}
			@Override protected Object getCheck(Annotation obj) {
				return obj.annotationType();
			}
		});
		return getter.found();
	}

	/**
	 * Gets all annotations of a specific type from an array of annotations and their respectives annotations
	 *
	 * @param annotationClass The class of the annotations to search
	 * @param annotations The annotations to search in
	 * @return A list of found annotations
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Annotation> List<T> GetAllRecursive(final Class<T> annotationClass, Annotation[] annotations) {
		final Arrays.AllGetter<T> getter = new Arrays.AllGetter<>();
		getter.find(annotations, new Arrays.AllGetter.Adder<T, Annotation>() {
			@Override public void addIn(List<T> list, Annotation obj) {
				if (obj.annotationType().equals(annotationClass))
					list.add((T)obj);
				else
					getter.find(obj.annotationType().getAnnotations(), this);
			}
			@Override protected Object getCheck(Annotation obj) {
				return obj.annotationType();
			}
		});
		return getter.found();
	}

	/**
	 * Gets all annotations from an annotated elements and it's parent. (Member > class > Package)
	 *
	 * @param element The element from which to search the annotations
	 * @param annotationClass The class of the annotations to find
	 * @return The annotation if found or null
	 */
	public static <T extends Annotation> List<T> GetAllTree(Class<T> annotationClass, AnnotatedElement element) {
		List<T> list = new LinkedList<>();

		T find = element.getAnnotation(annotationClass);
		if (find != null)
			list.add(find);

		if (element instanceof Member)
			list.addAll(GetAllTree(annotationClass, ((Member)element).getDeclaringClass()));

		if (element instanceof Class) {
			Class<?> objClass = ((Class<?>)element).getSuperclass();
			while (objClass != null) {
				find = objClass.getAnnotation(annotationClass);
				if (find != null)
					list.add(find);
				objClass = objClass.getSuperclass();
			}
			list.addAll(GetAllTree(annotationClass, ((Class<?>)element).getPackage()));
		}

		return list;
	}

	/**
	 * Gets all annotations from an annotated elements, the annotation's annotations,
	 * the element's parents' annotations, the element's parents' annotations' annotations, etc.
	 *
	 * @param element The element from which to search the annotations
	 * @param annotationClass The class of the annotations to find
	 * @return The annotation if found or null
	 */
	public static <T extends Annotation> List<T> GetAllTreeRecursive(Class<T> annotationClass, AnnotatedElement element) {
		List<T> list = new LinkedList<>();

		list.addAll(GetAllRecursive(annotationClass, element.getAnnotations()));

		if (element instanceof Member)
			list.addAll(GetAllTreeRecursive(annotationClass, ((Member)element).getDeclaringClass()));

		if (element instanceof Class) {
			Class<?> objClass = ((Class<?>)element).getSuperclass();
			while (objClass != null) {
				list.addAll(GetAllRecursive(annotationClass, objClass.getAnnotations()));
				objClass = objClass.getSuperclass();
			}
			list.addAll(GetAllTreeRecursive(annotationClass, ((Class<?>)element).getPackage()));
		}

		return list;
	}
}