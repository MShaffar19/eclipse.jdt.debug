/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.eval.ast.instructions;

public class DivideAssignmentOperator extends DivideOperator {

	public DivideAssignmentOperator(int variableTypeId, int valueTypeId,
			int start) {
		super(variableTypeId, variableTypeId, valueTypeId, true, start);
	}

	@Override
	public String toString() {
		return InstructionsEvaluationMessages.DivideAssignmentOperator_operator_1;
	}

}
