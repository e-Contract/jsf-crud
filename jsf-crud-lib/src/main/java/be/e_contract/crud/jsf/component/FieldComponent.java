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

import java.util.HashMap;
import java.util.Map;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(FieldComponent.COMPONENT_TYPE)
public class FieldComponent extends UIComponentBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldComponent.class);

    public static final String COMPONENT_TYPE = "crud.field";

    public enum PropertyKeys {
        name,
        label,
        hide,
        sort,
        filter,
        size,
        required,
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public String getName() {
        return (String) getStateHelper().get(PropertyKeys.name);
    }

    public void setName(String name) {
        getStateHelper().put(PropertyKeys.name, name);
    }

    public String getLabel() {
        return (String) getStateHelper().get(PropertyKeys.label);
    }

    public void setLabel(String label) {
        getStateHelper().put(PropertyKeys.label, label);
    }

    public Boolean isHide() {
        Boolean hide = (Boolean) getStateHelper().get(PropertyKeys.hide);
        return hide;
    }

    public void setHide(Boolean hide) {
        getStateHelper().put(PropertyKeys.hide, hide);
    }

    public Boolean isSort() {
        Boolean sort = (Boolean) getStateHelper().get(PropertyKeys.sort);
        return sort;
    }

    public void setSort(Boolean sort) {
        getStateHelper().put(PropertyKeys.sort, sort);
    }

    public boolean isFilter() {
        Boolean filter = (Boolean) getStateHelper().get(PropertyKeys.filter);
        if (null == filter) {
            return false;
        }
        return filter;
    }

    public void setFilter(boolean filter) {
        getStateHelper().put(PropertyKeys.filter, filter);
    }

    public Integer getSize() {
        return (Integer) getStateHelper().get(PropertyKeys.size);
    }

    public void setSize(Integer size) {
        getStateHelper().put(PropertyKeys.size, size);
    }

    public Boolean isRequired() {
        Boolean required = (Boolean) getStateHelper().get(PropertyKeys.required);
        return required;
    }

    public void setRequired(Boolean required) {
        getStateHelper().put(PropertyKeys.required, required);
    }

    public void addValidators(UIInput input) {
        for (UIComponent child : getChildren()) {
            if (!(child instanceof ValidatorComponent)) {
                continue;
            }
            ValidatorComponent validatorComponent = (ValidatorComponent) child;
            validatorComponent.applyValidator(input);
        }
    }

    public boolean isPasswordField() {
        for (UIComponent child : getChildren()) {
            if (child instanceof PasswordComponent) {
                return true;
            }
        }
        return false;
    }

    public boolean isFeedbackPassword() {
        for (UIComponent child : getChildren()) {
            if (child instanceof PasswordComponent) {
                PasswordComponent passwordComponent = (PasswordComponent) child;
                return passwordComponent.isFeedback();
            }
        }
        return false;
    }

    public boolean isMatchPassword() {
        for (UIComponent child : getChildren()) {
            if (child instanceof PasswordComponent) {
                PasswordComponent passwordComponent = (PasswordComponent) child;
                return passwordComponent.isMatch();
            }
        }
        return false;
    }

    public Map<String, FieldComponent> getEmbeddableFields() {
        Map<String, FieldComponent> embeddableFields = new HashMap<>();
        for (UIComponent fieldComponentChild : getChildren()) {
            if (!(fieldComponentChild instanceof FieldComponent)) {
                continue;
            }
            FieldComponent childFieldComponent = (FieldComponent) fieldComponentChild;
            embeddableFields.put(childFieldComponent.getName(), childFieldComponent);
        }
        return embeddableFields;
    }

    public UIInput getFieldInputComponent() {
        UIComponent inputComponent = getFacet("input");
        if (null == inputComponent) {
            return null;
        }
        if (!(inputComponent instanceof UIInput)) {
            LOGGER.error("field input component not UIInput: {}", inputComponent);
            return null;
        }
        return (UIInput) inputComponent;
    }

    public String getBinaryContentType() {
        for (UIComponent fieldChild : getChildren()) {
            if (fieldChild instanceof BinaryComponent) {
                BinaryComponent binaryComponent = (BinaryComponent) fieldChild;
                return binaryComponent.getContentType();
            }
        }
        return null;
    }
}
