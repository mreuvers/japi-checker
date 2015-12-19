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
import com.googlecode.japi.checker.Utils;
import com.googlecode.japi.checker.model.ClassData;
import com.googlecode.japi.checker.model.JavaItem;
import com.googlecode.japi.checker.model.Scope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Tomas Rohovsky
 */
// CLASS
public class CheckInheritanceChanges implements Rule {

	@Override
	public void checkBackwardCompatibility(Reporter reporter, JavaItem reference, JavaItem newItem) {

		ClassData referenceClass = (ClassData) reference;
		ClassData newClass = (ClassData) newItem;

		// contracted superclass set
		List<String> referenceAPISuperClasses = filterAPITypes(referenceClass.getClassDataLoader(), referenceClass.getSuperClasses());
		List<String> newAPISuperClasses = filterAPITypes(newClass.getClassDataLoader(), newClass.getSuperClasses());
		if (!newAPISuperClasses.containsAll(referenceAPISuperClasses)) {

			List<String> subtractedClasses = new ArrayList<String>(referenceAPISuperClasses);
			subtractedClasses.removeAll(newAPISuperClasses);

			reporter.report(reference, newItem,
					DifferenceType.CLASS_CONTRACTED_SUPERCLASS_SET,
					referenceClass, Utils.join(subtractedClasses, ", "));
		}

		// contracted interface set
		List<String> referenceAPIInterfaces = filterAPITypes(referenceClass.getClassDataLoader(), referenceClass.getAllInterfaces());
		List<String> newAPIInterfaces = filterAPITypes(newClass.getClassDataLoader(), newClass.getAllInterfaces());
		if (!newAPIInterfaces.containsAll(referenceAPIInterfaces)) {

			List<String> subtractedInterfaces = new ArrayList<String>(referenceAPIInterfaces);
			subtractedInterfaces.removeAll(newAPIInterfaces);

			reporter.report(reference, newItem,
					DifferenceType.CLASS_CONTRACTED_SUPERINTERFACE_SET,
					referenceClass, Utils.join(subtractedInterfaces, ", "));
		}
	}

	/**
	 * Filters types, which are API (with visibility higher than package).
	 *
	 * @return names of API types
	 */
	private List<String> filterAPITypes(ClassDataLoader<?> loader, Collection<String> typeNames) {
		List<String> APITypes = new ArrayList<String>();
		for (String typeName : typeNames) {
			ClassData type = loader.fromName(typeName);
			if (type != null) {
				if (type.getVisibility().isMoreVisibleThan(Scope.PACKAGE)) {
					APITypes.add(typeName);
				}
			} else {
				// if type's visibility is not known, it is API type
				APITypes.add(typeName);
			}
		}
		return APITypes;
	}
}
