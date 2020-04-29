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
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityFieldValueExpression extends ValueExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityFieldValueExpression.class);

    private String crudComponentId;

    private String entityFieldName;

    private boolean create;

    public EntityFieldValueExpression() {
        super();
        LOGGER.debug("default constructor");
    }

    public EntityFieldValueExpression(String crudComponentId, String entityFieldName, boolean create) {
        this.crudComponentId = crudComponentId;
        this.entityFieldName = entityFieldName;
        this.create = create;
    }

    private CRUDComponent getCRUDComponent() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot view = facesContext.getViewRoot();
        UIComponent component = view.findComponent(this.crudComponentId);
        if (null == component) {
            return null;
        }
        return (CRUDComponent) component;
    }

    private Field getEntityField() {
        CRUDComponent crudComponent = getCRUDComponent();
        Class<?> entityClass = crudComponent.getEntityClass();
        Field entityField;
        try {
            entityField = entityClass.getDeclaredField(this.entityFieldName);
        } catch (NoSuchFieldException | SecurityException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            return null;
        }
        if (null == entityField) {
            LOGGER.error("unknown entity field: {}", this.entityFieldName);
        }
        return entityField;
    }

    @Override
    public Object getValue(ELContext context) {
        Object entity = getEntity();
        if (null == entity) {
            return null;
        }
        Field entityField = getEntityField();
        try {
            entityField.setAccessible(true);
            Object value = entityField.get(entity);
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
        CRUDComponent crudComponent = getCRUDComponent();
        Object entity;
        if (this.create) {
            entity = crudComponent.getNewEntity();
            if (null == entity) {
                Field entityField = getEntityField();
                try {
                    entity = entityField.getDeclaringClass().newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    LOGGER.error("error: " + ex.getMessage(), ex);
                    return null;
                }
                crudComponent.setNewEntity(entity);
            }
        } else {
            entity = crudComponent.getSelection();
        }
        return entity;
    }

    @Override
    public void setValue(ELContext context, Object value) {
        Field entityField = getEntityField();
        LOGGER.debug("setValue: {} = {}", entityField.getName(), value);
        Object entity = getEntity();
        if (null == entity) {
            return;
        }
        try {
            if (null == value) {
                if (entityField.getType().isPrimitive()) {
                    return;
                }
            }
            entityField.setAccessible(true);
            entityField.set(entity, value);
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
        Field entityField = getEntityField();
        return entityField.getType();
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
        Field entityField = getEntityField();
        return new ValueReference(getEntity(), entityField.getName());
    }
}
