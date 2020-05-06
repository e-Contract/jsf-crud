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
package be.e_contract.crud.jsf.update;

import be.e_contract.crud.jsf.CRUDComponent;
import be.e_contract.crud.jsf.api.UpdateEvent;
import be.e_contract.crud.jsf.component.EntityComponent;
import be.e_contract.crud.jsf.jpa.CRUDController;
import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.application.FacesMessage;
import javax.faces.component.StateHolder;
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

public class SaveActionListener implements ActionListener, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveActionListener.class);

    private boolean _transient;

    private MethodExpression methodExpression;

    public SaveActionListener() {
        super();
    }

    public SaveActionListener(MethodExpression methodExpression) {
        this.methodExpression = methodExpression;
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        LOGGER.debug("processAction save");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        EntityComponent entityComponent = EntityComponent.getParentEntityComponent(event.getComponent());
        Object entity = entityComponent.getEntity();
        CRUDComponent crudComponent = entityComponent.getCRUDComponent();
        if (null != this.methodExpression) {
            this.methodExpression.invoke(elContext, new Object[]{entity});
        }
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        UserTransaction userTransaction = crudController.getUserTransaction();
        try {
            userTransaction.begin();
        } catch (NotSupportedException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            crudComponent.addMessage(FacesMessage.SEVERITY_ERROR, "Could not save entity.");
            crudComponent.resetCache();
            return;
        }
        entityManager.merge(entity);
        try {
            userTransaction.commit();
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            crudComponent.addMessage(FacesMessage.SEVERITY_ERROR, "Could not save entity.");
            crudComponent.resetCache();
            return;
        }
        crudComponent.resetCache();
        UpdateEvent updateEvent = new UpdateEvent(crudComponent, entity);
        updateEvent.queue();
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        return new Object[]{this.methodExpression};
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }
        this.methodExpression = (MethodExpression) ((Object[]) state)[0];
    }

    @Override
    public boolean isTransient() {
        return this._transient;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        this._transient = newTransientValue;
    }
}
