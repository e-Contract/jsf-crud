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

import be.e_contract.crud.jsf.api.DeleteEvent;
import be.e_contract.crud.jsf.jpa.CRUDController;
import be.e_contract.crud.jsf.jpa.EntityInspector;
import javax.faces.application.FacesMessage;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteActionListener extends AbstractCRUDComponentStateHolder implements ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteActionListener.class);

    public DeleteActionListener() {
        super();
    }

    public DeleteActionListener(CRUDComponent crudComponent) {
        super(crudComponent);
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        LOGGER.debug("processAction DeleteActionListener");
        CRUDComponent crudComponent = super.getCRUDComponent();
        LOGGER.debug("delete: {}", crudComponent.getSelection());
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        UserTransaction userTransaction = crudController.getUserTransaction();
        Object selection = crudComponent.getSelection();
        if (null == selection) {
            LOGGER.error("missing selection");
            return;
        }
        EntityInspector entityInspector = new EntityInspector(entityManager, selection);
        try {
            userTransaction.begin();
        } catch (NotSupportedException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            return;
        }
        Object entity;
        try {
            PersistenceUnitUtil persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
            Object identifier = persistenceUnitUtil.getIdentifier(selection);
            entity = entityManager.find(selection.getClass(), identifier);
            crudController.firePreDeleteEvent(entity);
            if (null != entity) {
                entityManager.remove(entity);
            } else {
                LOGGER.error("missing entity");
                String entityHumanReadable = entityInspector.toHumanReadable(selection);
                crudComponent.addMessage(FacesMessage.SEVERITY_ERROR, "Could not delete " + entityHumanReadable);
                return;
            }
        } finally {
            try {
                userTransaction.commit();
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                String entityHumanReadable = entityInspector.toHumanReadable(selection);
                crudComponent.addMessage(FacesMessage.SEVERITY_ERROR, "Could not delete " + entityHumanReadable);
                return;
            }
        }
        crudComponent.setSelection(null);
        String entityHumanReadable = entityInspector.toHumanReadable(entity);
        crudComponent.addMessage(FacesMessage.SEVERITY_INFO, "Deleted " + entityHumanReadable);
        DeleteEvent deleteEvent = new DeleteEvent(crudComponent, entity);
        deleteEvent.queue();
    }
}
