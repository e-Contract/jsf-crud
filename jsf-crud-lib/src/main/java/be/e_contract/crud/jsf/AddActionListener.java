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

public class AddActionListener extends AbstractCRUDComponentStateHolder implements ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddActionListener.class);

    public AddActionListener() {
        super();
    }

    public AddActionListener(String crudComponentId) {
        super(crudComponentId);
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        LOGGER.debug("processAction add");
        CRUDComponent crudComponent = super.getCRUDComponent();
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        UserTransaction userTransaction = crudController.getUserTransaction();
        Object entity = crudComponent.getNewEntity();
        EntityInspector entityInspector = new EntityInspector(entityManager, entity);
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
        crudController.firePreCreateEvent(entity);
        try {
            entityManager.merge(entity);
        } catch (Exception ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            String entityHumanReadable = entityInspector.toHumanReadable(entity);
            crudComponent.addMessage(FacesMessage.SEVERITY_ERROR, "Could not add " + entityHumanReadable);
            crudComponent.setNewEntity(null);
            try {
                userTransaction.rollback();
            } catch (SecurityException | IllegalStateException | SystemException ex2) {
                LOGGER.error("error: " + ex2.getMessage(), ex2);
                crudComponent.addMessage(FacesMessage.SEVERITY_ERROR, "Could not add " + entityHumanReadable);
                crudComponent.setNewEntity(null);
                return;
            }
            return;
        }
        try {
            userTransaction.commit();
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            crudComponent.addMessage(FacesMessage.SEVERITY_ERROR, "Could not add " + entity);
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
