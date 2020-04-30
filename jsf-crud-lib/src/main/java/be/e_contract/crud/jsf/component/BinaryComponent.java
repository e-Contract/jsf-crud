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

@FacesComponent(BinaryComponent.COMPONENT_TYPE)
public class BinaryComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.binary";

    public enum PropertyKeys {
        contentType,
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public String getContentType() {
        return (String) getStateHelper().eval(PropertyKeys.contentType, null);
    }

    public void setContentType(String contentType) {
        getStateHelper().put(PropertyKeys.contentType, contentType);
    }
}
