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

import java.lang.reflect.Field;
import javax.faces.application.FacesMessage;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.primefaces.component.fileupload.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClearFieldActionListener implements ActionListener, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClearFieldActionListener.class);

    private String crudComponentId;

    private String entityFieldName;

    private boolean addNotUpdate;

    private boolean _transient;

    public ClearFieldActionListener() {
        super();
    }

    public ClearFieldActionListener(String crudComponentId, Field entityField, boolean addNotUpdate) {
        this.crudComponentId = crudComponentId;
        this.entityFieldName = entityField.getName();
        this.addNotUpdate = addNotUpdate;
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        return new Object[]{this.crudComponentId, this.entityFieldName, this.addNotUpdate};
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }
        Object[] states = (Object[]) state;
        this.crudComponentId = (String) states[0];
        this.entityFieldName = (String) states[1];
        this.addNotUpdate = (Boolean) states[2];
    }

    @Override
    public boolean isTransient() {
        return this._transient;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        this._transient = newTransientValue;
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        LOGGER.debug("processAction");
        Field entityField = getEntityField();
        Object entity = getEntity();
        entityField.setAccessible(true);
        try {
            entityField.set(entity, null);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new AbortProcessingException();
        }
        UIComponent component = event.getComponent();
        FileUpload fileUpload = getParentFileUpload(component);
        FacesMessage facesMessage = new FacesMessage("Binary field cleared.");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(fileUpload.getClientId(), facesMessage);
    }

    private FileUpload getParentFileUpload(UIComponent component) {
        if (null == component) {
            throw new AbortProcessingException();
        }
        while (component.getParent() != null) {
            component = component.getParent();
            if (component instanceof FileUpload) {
                return (FileUpload) component;
            }
        }
        throw new AbortProcessingException();
    }

    private Field getEntityField() {
        CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(this.crudComponentId);
        Class<?> entityClass = crudComponent.getEntityClass();
        Field entityField;
        try {
            entityField = entityClass.getDeclaredField(this.entityFieldName);
        } catch (NoSuchFieldException | SecurityException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new AbortProcessingException();
        }
        if (null == entityField) {
            LOGGER.error("unknown entity field: {}", this.entityFieldName);
            throw new AbortProcessingException();
        }
        return entityField;
    }

    private Object getEntity() {
        CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(this.crudComponentId);
        Object entity;
        if (this.addNotUpdate) {
            entity = crudComponent.getNewEntity();
            if (null == entity) {
                Field entityField = getEntityField();
                try {
                    entity = entityField.getDeclaringClass().newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    LOGGER.error("error: " + ex.getMessage(), ex);
                    throw new AbortProcessingException();
                }
                crudComponent.setNewEntity(entity);
            }
        } else {
            entity = crudComponent.getSelection();
        }
        return entity;
    }
}
