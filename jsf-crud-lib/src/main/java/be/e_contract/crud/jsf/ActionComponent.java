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

@FacesComponent(value = "crud.action")
public class ActionComponent extends UIComponentBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionComponent.class);

    public enum PropertyKeys {
        value,
        action,
        oncomplete,
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public void setAction(MethodExpression methodExpression) {
        LOGGER.debug("setAction: {}", methodExpression);
        getStateHelper().put(PropertyKeys.action, methodExpression);
    }

    public MethodExpression getAction() {
        return (MethodExpression) getStateHelper().eval(PropertyKeys.action);
    }

    public void setValue(String value) {
        getStateHelper().put(PropertyKeys.value, value);
    }

    public String getValue() {
        return (String) getStateHelper().eval(PropertyKeys.value);
    }

    public void setOncomplete(String oncomplete) {
        getStateHelper().put(PropertyKeys.oncomplete, oncomplete);
    }

    public String getOncomplete() {
        return (String) getStateHelper().eval(PropertyKeys.oncomplete);
    }
}
