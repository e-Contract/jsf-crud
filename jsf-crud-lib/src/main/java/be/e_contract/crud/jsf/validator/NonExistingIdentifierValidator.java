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
package be.e_contract.crud.jsf.validator;

import be.e_contract.crud.jsf.AbstractCRUDComponentStateHolder;
import be.e_contract.crud.jsf.CRUDComponent;
import be.e_contract.crud.jsf.jpa.CRUDController;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NonExistingIdentifierValidator extends AbstractCRUDComponentStateHolder implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NonExistingIdentifierValidator.class);

    public NonExistingIdentifierValidator() {
        super();
        LOGGER.debug("default constructor");
    }

    public NonExistingIdentifierValidator(String crudComponentId) {
        super(crudComponentId);
    }

    private Class<?> getEntityClass() {
        CRUDComponent crudComponent = super.getCRUDComponent();
        return crudComponent.getEntityClass();
    }

    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        UserTransaction userTransaction = crudController.getUserTransaction();

        try {
            userTransaction.begin();
        } catch (NotSupportedException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            return;
        }
        Class<?> entityClass = getEntityClass();
        try {
            Object existingEntity = entityManager.find(entityClass, value);
            if (null != existingEntity) {
                FacesMessage facesMessage = new FacesMessage("Existing entity.");
                facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
                throw new ValidatorException(facesMessage);
            }

        } finally {
            try {
                userTransaction.commit();
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return;
            }
        }
    }
}
