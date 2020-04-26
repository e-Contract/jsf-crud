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

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import org.primefaces.component.commandbutton.CommandButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(CRUDCommandButton.COMPONENT_TYPE)
public class CRUDCommandButton extends CommandButton {

    private static final Logger LOGGER = LoggerFactory.getLogger(CRUDCommandButton.class);

    public static final String COMPONENT_TYPE = "crud.button";

    public enum PropertyKeys {
        renderedValueExpression,
    }

    @Override
    public boolean isRendered() {
        ValueExpression renderedValueExpression = (ValueExpression) getStateHelper().get(PropertyKeys.renderedValueExpression);
        if (null == renderedValueExpression) {
            return super.isRendered();
        }
        LOGGER.debug("rendered value expression: {}", renderedValueExpression.getExpressionString());
        ELContext elContext = getFacesContext().getELContext();
        Boolean rendered = (Boolean) renderedValueExpression.getValue(elContext);
        LOGGER.debug("rendered: {}", rendered);
        return rendered;
    }

    public void setRenderedValueExpression(ValueExpression valueExpression) {
        getStateHelper().put(PropertyKeys.renderedValueExpression, valueExpression);
    }
}
