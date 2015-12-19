package com.googlecode.japi.checker.rules;

import com.googlecode.japi.checker.DifferenceType;
import com.googlecode.japi.checker.Reporter;
import com.googlecode.japi.checker.model.ClassData;
import com.googlecode.japi.checker.model.FieldData;
import com.googlecode.japi.checker.model.JavaItem;
import com.googlecode.japi.checker.model.Scope;

/**
 * @author Tomas Rohovsky
 */
public class CheckAddedField implements Rule {

	@Override
	public void checkBackwardCompatibility(Reporter reporter, JavaItem reference, JavaItem newItem) {

		ClassData referenceClass = (ClassData) reference;
		ClassData newClass = (ClassData) newItem;

		// applicable for both - classes and interfaces
		for (FieldData newField : newClass.getFields()) {
			if (newField.getVisibility().isMoreVisibleThan(Scope.PACKAGE)) {

				boolean found = false;
				for (FieldData oldField : referenceClass.getFields()) {
					if (oldField.isSame(newField) && oldField.getVisibility().isMoreVisibleThan(Scope.PACKAGE)) {
						found = true;
						break;
					}
				}

				if (!found) {
					// class is subclassable of it is interface
					if (newClass.isSubclassable() || newClass.isInterface()) {
						reporter.report(reference,
								newItem,
								DifferenceType.CLASS_ADDED_FIELD,
								newField);
					}
				}
			}
		}
	}
}
