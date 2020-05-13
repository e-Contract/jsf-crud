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

import java.util.LinkedList;
import java.util.List;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;

@FacesComponent(QueryComponent.COMPONENT_TYPE)
public class QueryComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.query";

    public enum PropertyKeys {
        query,
        namedQuery,
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public String getQuery() {
        return (String) getStateHelper().eval(PropertyKeys.query, null);
    }

    public void setQuery(String query) {
        getStateHelper().put(PropertyKeys.query, query);
    }

    public String getNamedQuery() {
        return (String) getStateHelper().eval(PropertyKeys.namedQuery, null);
    }

    public void setNamedQuery(String namedQuery) {
        getStateHelper().put(PropertyKeys.namedQuery, namedQuery);
    }

    public List<QueryParameterComponent> getQueryParameters() {
        List<QueryParameterComponent> queryParameters = new LinkedList<>();
        for (UIComponent child : getChildren()) {
            if (child instanceof QueryParameterComponent) {
                QueryParameterComponent queryParameterComponent = (QueryParameterComponent) child;
                queryParameters.add(queryParameterComponent);
            }
        }
        return queryParameters;
    }

    public Query getQuery(EntityManager entityManager) {
        String namedQuery = getNamedQuery();
        if (null != namedQuery) {
            return entityManager.createNamedQuery(namedQuery);
        }
        return entityManager.createQuery(getQuery());
    }

    public Query getQuery(EntityManager entityManager, ELContext context) {
        Query query = getQuery(entityManager);
        List<QueryParameterComponent> queryParameters = getQueryParameters();
        for (QueryParameterComponent queryParameter : queryParameters) {
            ValueExpression valueExpression = (ValueExpression) queryParameter.getValue();
            Object value = valueExpression.getValue(context);
            query.setParameter(queryParameter.getName(), value);
        }
        return query;
    }

    public Class<?> getQueryParameterType(EntityManager entityManager, String parameterName) {
        Query query = getQuery(entityManager);
        Parameter<?> parameter = query.getParameter(parameterName);
        return parameter.getParameterType();
    }
}
