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
import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldUploadMethodExpression extends MethodExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldUploadMethodExpression.class);

    private final String crudComponentId;

    private final String entityFieldName;

    private final boolean create;

    public FieldUploadMethodExpression(String crudComponentId, String entityFieldName, boolean create) {
        this.crudComponentId = crudComponentId;
        this.entityFieldName = entityFieldName;
        this.create = create;
    }

    @Override
    public MethodInfo getMethodInfo(ELContext elContext) {
        LOGGER.debug("getMethodInfo");
        return null;
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
    public Object invoke(ELContext elContext, Object[] params) {
        LOGGER.debug("invoke");
        FileUploadEvent fileUploadEvent = (FileUploadEvent) params[0];
        UploadedFile uploadedFile = fileUploadEvent.getFile();
        LOGGER.debug("filename: {}", uploadedFile.getFileName());
        LOGGER.debug("file size: {}", uploadedFile.getSize());
        Field entityField = getEntityField();
        entityField.setAccessible(true);
        try {
            entityField.set(getEntity(), uploadedFile.getContent());
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.error("reflection error: " + ex.getMessage(), ex);
        }
        return null;
    }

    private Object getEntity() {
        Object entity;
        CRUDComponent crudComponent = getCRUDComponent();
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
    public String getExpressionString() {
        LOGGER.debug("getExpressionString");
        return null;
    }

    @Override
    public boolean equals(Object o) {
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
        LOGGER.debug("isLiteralText");
        return false;
    }
}
