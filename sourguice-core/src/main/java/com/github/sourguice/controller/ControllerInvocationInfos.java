package com.github.sourguice.controller;

import java.util.regex.MatchResult;

import javax.annotation.CheckForNull;

import com.github.sourguice.annotation.request.View;

/**
 * Information of a specific invocation
 * Used to compare different invocation and to carry with the invocation, request matching intels
 */
public class ControllerInvocationInfos {
	/**
	 * The concerned invocation
	 */
	public ControllerInvocation invocation;

	/**
	 * Specialization indice for this method to match the request
	 * The higher it is, the more it is specialized for the request
	 */
	public int confidence = 0;

	/**
	 * The match result after parsing the request URL
	 */
	public @CheckForNull MatchResult urlMatch = null;

	/**
	 * The default view delcared on the method using {@link View}
	 */
	public @CheckForNull String defaultView = null;

	/**
	 * @param invocation The invocation on which calculates informations
	 */
	public ControllerInvocationInfos(final ControllerInvocation invocation) {
		this.invocation = invocation;
	}

	/**
	 * Compare a given InvocationInfos to this
	 *
	 * @param infos The InvocationInfos to compare to this
	 * @return Wheter this invocation is better than the one given
	 */
	public boolean isBetterThan(final @CheckForNull ControllerInvocationInfos infos) {
		if (infos == null) {
			return true;
		}
		if (this.urlMatch != null && infos.urlMatch != null) {
			if (this.urlMatch.groupCount() > infos.urlMatch.groupCount()) {
				return true;
			}
			else if (this.urlMatch.groupCount() < infos.urlMatch.groupCount()) {
				return false;
			}
		}

		return this.confidence > infos.confidence;
	}

	/**
	 * Calculates the best invocation between two given invocations
	 *
	 * @param left The first invocation to compare
	 * @param right The second invocation to compare
	 * @return The best invocation between both given (nulls are accepted)
	 */
	static public @CheckForNull ControllerInvocationInfos getBest(final @CheckForNull ControllerInvocationInfos left, final @CheckForNull ControllerInvocationInfos right) {
		if (left == null) {
			return right;
		}
		return left.isBetterThan(right) ? left : right;
	}
}