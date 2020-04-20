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
package be.e_contract.crud.jsf;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

@FacesComponent(FieldComponent.COMPONENT_TYPE)
public class FieldComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.field";

    public enum PropertyKeys {
        name,
        label,
        hide,
        sort,
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

    public boolean isHide() {
        Boolean hide = (Boolean) getStateHelper().get(PropertyKeys.hide);
        if (null == hide) {
            return false;
        }
        return hide;
    }

    public void setHide(boolean hide) {
        getStateHelper().put(PropertyKeys.hide, hide);
    }

    public boolean isSort() {
        Boolean sort = (Boolean) getStateHelper().get(PropertyKeys.sort);
        if (null == sort) {
            return false;
        }
        return sort;
    }

    public void setSort(boolean sort) {
        getStateHelper().put(PropertyKeys.sort, sort);
    }
}
