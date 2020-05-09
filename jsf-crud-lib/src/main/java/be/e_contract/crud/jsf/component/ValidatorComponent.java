/*
 * JSF CRUD project.
 * Copyright (C) 2020 e-Contract.be BV.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see
 * http://www.gnu.org/licenses/.
 */
package be.e_contract.crud.jsf.component;

import javax.faces.application.Application;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;

@FacesComponent(ValidatorComponent.COMPONENT_TYPE)
public class ValidatorComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.validator";

    public enum PropertyKeys {
        validatorId,
        disabled,
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public boolean isDisabled() {
        Boolean disabled = (Boolean) getStateHelper().get(PropertyKeys.disabled);
        if (null == disabled) {
            return false;
        }
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        getStateHelper().put(PropertyKeys.disabled, disabled);
    }

    public String getValidatorId() {
        return (String) getStateHelper().get(PropertyKeys.validatorId);
    }

    public void setValidatorId(String validatorId) {
        getStateHelper().put(PropertyKeys.validatorId, validatorId);
    }

    public void applyValidator(UIInput inputComponent) {
        if (isDisabled()) {
            return;
        }
        String validatorId = getValidatorId();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        Validator validator = application.createValidator(validatorId);
        inputComponent.addValidator(validator);
    }
}
