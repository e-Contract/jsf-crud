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

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;

@FacesComponent(PropertyComponent.COMPONENT_TYPE)
public class PropertyComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.property";

    public enum PropertyKeys {
        name,
        label,
        sort,
        filter,
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

    public Boolean isSort() {
        Boolean sort = (Boolean) getStateHelper().get(PropertyKeys.sort);
        return sort;
    }

    public void setSort(Boolean sort) {
        getStateHelper().put(PropertyKeys.sort, sort);
    }

    public Boolean isFilter() {
        Boolean filter = (Boolean) getStateHelper().get(PropertyKeys.filter);
        return filter;
    }

    public void setFilter(Boolean filter) {
        getStateHelper().put(PropertyKeys.filter, filter);
    }
}
