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

import be.e_contract.crud.jsf.api.UpdateEvent;
import be.e_contract.crud.jsf.jpa.CRUDController;
import be.e_contract.crud.jsf.jpa.EntityInspector;
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

public class SaveActionListener extends AbstractCRUDComponentStateHolder implements ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveActionListener.class);

    public SaveActionListener() {
        super();
    }

    public SaveActionListener(String crudComponentId) {
        super(crudComponentId);
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        LOGGER.debug("processAction save");
        CRUDComponent crudComponent = super.getCRUDComponent();
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        UserTransaction userTransaction = crudController.getUserTransaction();
        Object entity = crudComponent.getSelection();
        if (null == entity) {
            LOGGER.error("missing selection");
            return;
        }
        EntityInspector entityInspector = new EntityInspector(CRUDController.getMetamodel(), entity);
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
            crudComponent.addMessage(FacesMessage.SEVERITY_ERROR, "Could not update " + entityHumanReadable);
            crudComponent.resetCache();
            return;
        }
        crudComponent.resetCache();
        crudComponent.setSelection(null);
        String entityHumanReadable = entityInspector.toHumanReadable(entity);
        crudComponent.addMessage(FacesMessage.SEVERITY_INFO, "Updated " + entityHumanReadable);
        UpdateEvent updateEvent = new UpdateEvent(crudComponent, entity);
        updateEvent.queue();
    }
}
