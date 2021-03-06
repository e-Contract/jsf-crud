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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.PropertyNotFoundException;
import org.primefaces.event.FileUploadEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldUploadMethodExpression extends MethodExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldUploadMethodExpression.class);

    private final String crudComponentId;

    private final String entityFieldName;

    private final boolean create;

    public FieldUploadMethodExpression(CRUDComponent crudComponent, String entityFieldName, boolean create) {
        this.crudComponentId = crudComponent.getId();
        this.entityFieldName = entityFieldName;
        this.create = create;
    }

    @Override
    public MethodInfo getMethodInfo(ELContext elContext) {
        LOGGER.debug("getMethodInfo");
        return null;
    }

    private Field getEntityField() {
        CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(this.crudComponentId);
        Class<?> entityClass = crudComponent.getEntityClass();
        Field entityField;
        try {
            entityField = entityClass.getDeclaredField(this.entityFieldName);
        } catch (NoSuchFieldException | SecurityException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new PropertyNotFoundException();
        }
        if (null == entityField) {
            LOGGER.error("unknown entity field: {}", this.entityFieldName);
            throw new PropertyNotFoundException();
        }
        return entityField;
    }

    @Override
    public Object invoke(ELContext elContext, Object[] params) {
        LOGGER.debug("invoke");
        FileUploadEvent fileUploadEvent = (FileUploadEvent) params[0];
        Object uploadedFile = fileUploadEvent.getFile();
        byte[] fileContent;
        Method getContentMethod = null;
        Method[] uploadedFileMethods = uploadedFile.getClass().getMethods();
        for (Method uploadedFileMethod : uploadedFileMethods) {
            LOGGER.debug("method: {}", uploadedFileMethod.getName());
            if (!Modifier.isPublic(uploadedFileMethod.getModifiers())) {
                continue;
            }
            if (uploadedFileMethod.getName().equals("getContents")) {
                // primefaces 7-
                getContentMethod = uploadedFileMethod;
                break;
            } else if (uploadedFileMethod.getName().equals("getContent")) {
                // primefaces 8+
                getContentMethod = uploadedFileMethod;
                break;
            }
        }
        if (getContentMethod != null) {
            try {
                fileContent = (byte[]) getContentMethod.invoke(uploadedFile, new Object[]{});
            } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.error("reflection error: " + ex.getMessage(), ex);
                throw new PropertyNotFoundException();
            }
        } else {
            LOGGER.error("UploadedFile content not retrieved");
            throw new PropertyNotFoundException();
        }
        Field entityField = getEntityField();
        entityField.setAccessible(true);
        try {
            entityField.set(getEntity(), fileContent);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.error("reflection error: " + ex.getMessage(), ex);
            throw new PropertyNotFoundException();
        }
        return null;
    }

    private Object getEntity() {
        Object entity;
        CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(this.crudComponentId);
        if (this.create) {
            entity = crudComponent.getNewEntity();
            if (null == entity) {
                Field entityField = getEntityField();
                try {
                    entity = entityField.getDeclaringClass().newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    LOGGER.error("error: " + ex.getMessage(), ex);
                    throw new PropertyNotFoundException();
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
