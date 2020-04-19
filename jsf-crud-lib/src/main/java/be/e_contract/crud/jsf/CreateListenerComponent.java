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

import javax.el.MethodExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(CreateListenerComponent.COMPONENT_TYPE)
public class CreateListenerComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.createListener";

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateListenerComponent.class);

    public enum PropertyKeys {
        action
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public void setAction(MethodExpression methodExpression) {
        getStateHelper().put(PropertyKeys.action, methodExpression);
    }

    public MethodExpression getAction() {
        return (MethodExpression) getStateHelper().eval(PropertyKeys.action);
    }
}
