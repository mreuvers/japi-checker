/*
 * Copyright 2011 William Bernardet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.japi.checker.rules;

import com.googlecode.japi.checker.ClassDataLoader;
import com.googlecode.japi.checker.DifferenceType;
import com.googlecode.japi.checker.Reporter;
import com.googlecode.japi.checker.RuleHelpers;
import com.googlecode.japi.checker.model.JavaItem;
import com.googlecode.japi.checker.model.MethodData;

import java.util.List;

/**
 * This rule checks if a method is still throwing a compatible set of exceptions. The check is not occurring for private
 * scope. '
 */
// METHOD
public class CheckMethodExceptions implements Rule {

	/**
	 * Implementation of the check.
	 */
	@Override
	public void checkBackwardCompatibility(Reporter reporter, JavaItem reference, JavaItem newItem) {

		MethodData referenceMethod = (MethodData) reference;
		MethodData newMethod = (MethodData) newItem;
		for (String exception : referenceMethod.getExceptions()) {
			if (!isCompatibleWithAnyOfTheException(newItem.getOwner().getClassDataLoader(), exception, newMethod.getExceptions())) {
				reporter.report(reference, newItem,
						DifferenceType.METHOD_REMOVED_EXCEPTION,
						referenceMethod,
						exception);
			}
		}
		for (String exception : newMethod.getExceptions()) {
			if (!hasCompatibleExceptionInItsHierarchy(newItem.getOwner().getClassDataLoader(), exception, referenceMethod.getExceptions())) {
				reporter.report(reference, newItem,
						DifferenceType.METHOD_ADDED_EXCEPTION,
						referenceMethod,
						exception);
			}
		}
	}

	/**
	 * Check if exception is part of inheritance tree of any of the referenceExceptions members.
	 */
	private boolean isCompatibleWithAnyOfTheException(ClassDataLoader<?> loader, String exception, List<String> referenceExceptions) {
		for (String referenceException : referenceExceptions) {
			if (RuleHelpers.isClassPartOfClassTree(loader, exception, referenceException)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if any of the referenceException are part of the inherirance tree of exception.
	 */
	private boolean hasCompatibleExceptionInItsHierarchy(ClassDataLoader<?> loader, String exception, List<String> referenceExceptions) {
		for (String referenceException : referenceExceptions) {
			if (RuleHelpers.isClassPartOfClassTree(loader, referenceException, exception)) {
				return true;
			}
		}
		return false;
	}

}
