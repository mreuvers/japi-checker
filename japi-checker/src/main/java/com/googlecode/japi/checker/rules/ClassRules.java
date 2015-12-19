package com.googlecode.japi.checker.rules;

import com.googlecode.japi.checker.Reporter;
import com.googlecode.japi.checker.model.JavaItem;
import com.googlecode.japi.checker.model.Scope;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tomas Rohovsky
 */
public class ClassRules implements Rule {

	private List<Rule> rules = new ArrayList<Rule>();
	private List<Rule> nonAPIrules = new ArrayList<Rule>();

	public ClassRules() {
		nonAPIrules.add(new CheckChangeOfScope());

		rules.add(new ChangeKindOfAPIType());
		rules.add(new CheckAddedMethod());
		rules.add(new CheckAddedField());
		rules.add(new CheckClassVersion());
		rules.add(new CheckInheritanceChanges());
		rules.add(new CheckRemovedField());
		rules.add(new CheckRemovedMethod());
		rules.add(new ClassChangedToAbstract());
		rules.add(new ClassChangedToFinal());
		rules.add(new CheckTypeParameters());
	}

	@Override
	public void checkBackwardCompatibility(Reporter reporter, JavaItem reference, JavaItem newItem) {

		for (Rule rule : nonAPIrules) {
			rule.checkBackwardCompatibility(reporter, reference, newItem);
		}

		if ((reference.getOwner() == null && newItem.getOwner() == null) ||
				(reference.getOwner().getVisibility().isMoreVisibleThan(Scope.PACKAGE) &&
						reference.getOwner().getVisibility().isMoreVisibleThan(Scope.PACKAGE))) {

			if (reference.getVisibility().isMoreVisibleThan(Scope.PACKAGE) &&
					newItem.getVisibility().isMoreVisibleThan(Scope.PACKAGE)) {

				for (Rule rule : rules) {
					rule.checkBackwardCompatibility(reporter, reference, newItem);
				}
			}
		}
	}

}
