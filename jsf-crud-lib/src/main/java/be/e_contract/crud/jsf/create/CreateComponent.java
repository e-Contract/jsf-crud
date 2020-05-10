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
package be.e_contract.crud.jsf.create;

import be.e_contract.crud.jsf.component.OrderComponent;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;

@FacesComponent(CreateComponent.COMPONENT_TYPE)
public class CreateComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.create";

    public enum PropertyKeys {
        disabled,
        icon,
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

    public String getIcon() {
        return (String) getStateHelper().eval(PropertyKeys.icon, null);
    }

    public void setIcon(String icon) {
        getStateHelper().put(PropertyKeys.icon, icon);
    }

    public OrderComponent findOrderComponent() {
        for (UIComponent child : getChildren()) {
            if (child instanceof OrderComponent) {
                return (OrderComponent) child;
            }
        }
        return null;
    }
}
