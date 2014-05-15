/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import java.util.ArrayList;
import java.util.List;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * Composition validation rules.
 * <ul>
 *  <li>Validate that a business object used in a composition is used in only one composition</li>
 *  <li>Validate that a business object used in a composition cannot have one of it's ancestor as a child (Circular references)</li>
 * </ul>
 *  
 * @author Colin PUY
 */
public class CompositionValidationRule extends ValidationRule<BusinessObjectModel> {

    public CompositionValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    ValidationStatus validate(BusinessObjectModel bom) {
        return validateThatCompositeObjectsAreComposedOnlyInOneBo(bom);
    }

    private ValidationStatus validateThatCompositeObjectsAreComposedOnlyInOneBo(BusinessObjectModel bom) {
        ValidationStatus validationStatus = new ValidationStatus();
        List<BusinessObject> alreadyComposedBOs = new ArrayList<BusinessObject>();
        for (BusinessObject compositeBO : bom.getReferencedBusinessObjectsByComposition()) {
            if (alreadyComposedBOs.contains(compositeBO)) {
                validationStatus.addError("Business object " + compositeBO.getQualifiedName() + " is referenced by composition in two business objects");
            } else {
                alreadyComposedBOs.add(compositeBO);
            }
        }
        return validationStatus;
    }
}
