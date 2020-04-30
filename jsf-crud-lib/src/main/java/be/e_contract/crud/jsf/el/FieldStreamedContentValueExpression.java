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
import be.e_contract.crud.jsf.jpa.EntityInspector;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldStreamedContentValueExpression extends ValueExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldStreamedContentValueExpression.class);

    private final String crudComponentId;

    private final String entityFieldName;

    private final String contentType;

    public FieldStreamedContentValueExpression(String crudComponentId, String entityFieldName, String contentType) {
        this.crudComponentId = crudComponentId;
        this.entityFieldName = entityFieldName;
        this.contentType = contentType;
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
    public Object getValue(ELContext elContext) {
        Field entityField = getEntityField();
        LOGGER.debug("getValue: {}", entityField.getName());
        CRUDComponent crudComponent = getCRUDComponent();
        Object entity = crudComponent.getSelection();
        if (null == entity) {
            LOGGER.warn("missing selection");
            return null;
        }
        byte[] value;
        try {
            entityField.setAccessible(true);
            value = (byte[]) entityField.get(entity);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.error("reflection error: " + ex.getMessage(), ex);
            return null;
        }
        if (null == value) {
            return null;
        }
        LOGGER.debug("getValue: {} bytes", value.length);
        EntityInspector entityInspector = new EntityInspector(entity);
        String name = entityInspector.toHumanReadable(entity) + "-" + entityField.getName();
        StreamedContent streamedContent = new DefaultStreamedContent(new ByteArrayInputStream(value), this.contentType, name);
        return streamedContent;
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
        return null;
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
