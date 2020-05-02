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
package be.e_contract.crud.jsf.el;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityMethodStreamedContentValueExpression extends ValueExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityMethodStreamedContentValueExpression.class);

    private MethodExpression methodExpression;

    public EntityMethodStreamedContentValueExpression() {
        super();
    }

    public EntityMethodStreamedContentValueExpression(MethodExpression methodExpression) {
        this.methodExpression = methodExpression;
    }

    @Override
    public Object getValue(ELContext elContext) {
        Object entity = elContext.getELResolver().getValue(elContext, null, "row");
        Object result = this.methodExpression.invoke(elContext, new Object[]{entity});
        return result;
    }

    @Override
    public void setValue(ELContext elContext, Object value) {
        // empty
    }

    @Override
    public boolean isReadOnly(ELContext elContext) {
        return true;
    }

    @Override
    public Class<?> getType(ELContext elContext) {
        return StreamedContent.class;
    }

    @Override
    public Class<?> getExpectedType() {
        return StreamedContent.class;
    }

    @Override
    public String getExpressionString() {
        return this.methodExpression.getExpressionString();
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean isLiteralText() {
        return false;
    }
}
