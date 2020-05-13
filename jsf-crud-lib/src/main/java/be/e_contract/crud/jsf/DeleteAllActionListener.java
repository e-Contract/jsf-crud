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

import be.e_contract.crud.jsf.jpa.CRUDController;
import javax.faces.application.FacesMessage;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteAllActionListener extends AbstractCRUDComponentStateHolder implements ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteAllActionListener.class);

    public DeleteAllActionListener() {
        super();
    }

    public DeleteAllActionListener(CRUDComponent crudComponent) {
        super(crudComponent);
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        CRUDComponent crudComponent = super.getCRUDComponent();
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        UserTransaction userTransaction = crudController.getUserTransaction();
        try {
            userTransaction.begin();
        } catch (NotSupportedException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            return;
        }
        Class<?> entityClass = crudComponent.getEntityClass();
        Query query = entityManager.createQuery("DELETE FROM " + entityClass.getSimpleName());
        int count = query.executeUpdate();
        try {
            userTransaction.commit();
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            crudComponent.addMessage(FacesMessage.SEVERITY_ERROR, "Could not delete entries.");
            return;
        }
        crudComponent.addMessage(FacesMessage.SEVERITY_INFO, "Deleted " + count + " entries.");
        crudComponent.resetCache();
    }
}
