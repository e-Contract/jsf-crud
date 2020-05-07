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

import be.e_contract.crud.jsf.jpa.CRUDController;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.persistence.EntityManager;

public class QueryParameterTagHandler extends ComponentHandler {

    public QueryParameterTagHandler(ComponentConfig config) {
        super(config);
    }

    @Override
    public void onComponentCreated(FaceletContext faceletContext, UIComponent component, UIComponent parent) {
        if (!ComponentHandler.isNew(parent)) {
            return;
        }
        TagAttribute valueTagAttribute = getTagAttribute("value");
        String valueValue = valueTagAttribute.getValue();
        FacesContext facesContext = faceletContext.getFacesContext();
        ELContext elContext = facesContext.getELContext();
        ExpressionFactory expressionFactory = faceletContext.getExpressionFactory();
        QueryComponent queryComponent = (QueryComponent) parent;
        QueryParameterComponent queryParameterComponent = (QueryParameterComponent) component;
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        Class<?> parameterType = queryComponent.getQueryParameterType(entityManager, queryParameterComponent.getName());
        ValueExpression valueExpression = expressionFactory.createValueExpression(elContext, valueValue, parameterType);
        queryParameterComponent.setValue(valueExpression);
    }
}
