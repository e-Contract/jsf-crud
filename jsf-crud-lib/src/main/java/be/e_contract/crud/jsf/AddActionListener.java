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

import be.e_contract.crud.jsf.api.CreateEvent;
import be.e_contract.crud.jsf.jpa.CRUDController;
import be.e_contract.crud.jsf.jpa.EntityInspector;
import java.lang.reflect.Field;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddActionListener implements ActionListener, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddActionListener.class);

    private String crudComponentId;
    private boolean _transient;

    public AddActionListener() {
        super();
    }

    public AddActionListener(String crudComponentId) {
        this.crudComponentId = crudComponentId;
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        return new Object[]{this.crudComponentId};
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }
        this.crudComponentId = (String) ((Object[]) state)[0];
    }

    @Override
    public boolean isTransient() {
        return this._transient;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        this._transient = newTransientValue;
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

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        LOGGER.debug("processAction add");
        CRUDComponent crudComponent = getCRUDComponent();
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        UserTransaction userTransaction = crudController.getUserTransaction();
        Object entity = crudComponent.getNewEntity();
        EntityInspector entityInspector = new EntityInspector(CRUDController.getMetamodel(), entity);
        Field[] fields = entity.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(List.class)) {
                field.setAccessible(true);
                try {
                    List listValue = (List) field.get(entity);
                    int listSize;
                    if (null == listValue) {
                        listSize = 0;
                    } else {
                        listSize = listValue.size();
                    }
                    LOGGER.debug("field {} list size {}", field.getName(), listSize);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    LOGGER.error("reflection error: " + ex.getMessage(), ex);
                    return;
                }
            }
        }
        try {
            userTransaction.begin();
        } catch (NotSupportedException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            return;
        }
        entityManager.merge(entity);
        try {
            userTransaction.commit();
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            String entityHumanReadable = entityInspector.toHumanReadable(entity);
            crudComponent.addMessage(FacesMessage.SEVERITY_ERROR, "Could not add " + entityHumanReadable);
            crudComponent.setNewEntity(null);
            return;
        }
        crudComponent.setNewEntity(null);
        String entityHumanReadable = entityInspector.toHumanReadable(entity);
        crudComponent.addMessage(FacesMessage.SEVERITY_INFO, "Added " + entityHumanReadable);
        CreateEvent createEvent = new CreateEvent(crudComponent, entity);
        createEvent.queue();
    }
}