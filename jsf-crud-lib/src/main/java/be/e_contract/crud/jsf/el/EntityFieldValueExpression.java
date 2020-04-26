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

import be.e_contract.crud.jsf.CRUDComponent;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityFieldValueExpression extends ValueExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityFieldValueExpression.class);

    private final CRUDComponent crudComponent;

    private final Field entityField;

    private final boolean create;

    public EntityFieldValueExpression(CRUDComponent crudComponent, Field entityField, boolean create) {
        this.crudComponent = crudComponent;
        this.entityField = entityField;
        this.create = create;
    }

    @Override
    public Object getValue(ELContext context) {
        Object entity = getEntity();
        if (null == entity) {
            return null;
        }
        try {
            this.entityField.setAccessible(true);
            Object value = this.entityField.get(entity);
            if (value instanceof List) {
                LOGGER.debug("list class: {}", value.getClass().getName());
                // avoid passing org.hibernate.collection.internal.PersistentBag that later on can yield 
                // org.hibernate.LazyInitializationException exceptions
                LinkedList newList = new LinkedList((List) value);
                return newList;
            }
            return value;
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            return null;
        }
    }

    private Object getEntity() {
        Object entity;
        if (this.create) {
            entity = this.crudComponent.getNewEntity();
            if (null == entity) {
                try {
                    entity = this.entityField.getDeclaringClass().newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    LOGGER.error("error: " + ex.getMessage(), ex);
                    return null;
                }
                this.crudComponent.setNewEntity(entity);
            }
        } else {
            entity = this.crudComponent.getSelection();
        }
        return entity;
    }

    @Override
    public void setValue(ELContext context, Object value) {
        LOGGER.debug("setValue: {} = {}", this.entityField.getName(), value);
        Object entity = getEntity();
        if (null == entity) {
            return;
        }
        try {
            if (null == value) {
                if (this.entityField.getType().isPrimitive()) {
                    return;
                }
            }
            this.entityField.setAccessible(true);
            this.entityField.set(entity, value);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        LOGGER.debug("isReadOnly");
        return false;
    }

    @Override
    public Class<?> getType(ELContext context) {
        //LOGGER.debug("getType");
        return this.entityField.getType();
    }

    @Override
    public Class<?> getExpectedType() {
        LOGGER.debug("getExpectedType");
        return null;
    }

    @Override
    public String getExpressionString() {
        LOGGER.debug("getExpressionString");
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        LOGGER.debug("equals");
        return false;
    }

    @Override
    public int hashCode() {
        LOGGER.debug("hashCode");
        return 0;
    }

    @Override
    public boolean isLiteralText() {
        //LOGGER.debug("isLiteralText");
        return false;
    }

    @Override
    public ValueReference getValueReference(ELContext context) {
        return new ValueReference(getEntity(), this.entityField.getName());
    }
}
