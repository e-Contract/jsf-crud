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
package be.e_contract.crud.jsf.action;

import be.e_contract.crud.jsf.CRUDComponent;
import be.e_contract.crud.jsf.api.UpdateEvent;
import be.e_contract.crud.jsf.component.EntityComponent;
import be.e_contract.crud.jsf.jpa.CRUDController;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
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
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionAdapter implements ActionListener, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionAdapter.class);

    private MethodExpression methodExpression;

    private String update;

    private boolean _transient;

    private String crudComponentId;

    public ActionAdapter() {
        super();
    }

    public ActionAdapter(MethodExpression methodExpression, String update, CRUDComponent crudComponent) {
        this.methodExpression = methodExpression;
        this.update = update;
        this.crudComponentId = crudComponent.getId();
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        return new Object[]{this.methodExpression, this.update, this.crudComponentId};
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
        this.update = (String) ((Object[]) state)[1];
        this.crudComponentId = (String) ((Object[]) state)[2];
    }

    @Override
    public boolean isTransient() {
        return this._transient;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        this._transient = newTransientValue;
    }

    private void setEntity(Object entity, UIComponent component) {
        if (null == component) {
            throw new AbortProcessingException();
        }
        if (component instanceof EntityComponent) {
            EntityComponent entityComponent = (EntityComponent) component;
            entityComponent.setEntity(entity, this.crudComponentId);
        }
        for (UIComponent child : component.getChildren()) {
            setEntity(entity, child);
        }
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ELResolver elResolver = elContext.getELResolver();
        Object entity = elResolver.getValue(elContext, null, "row");
        if (null != this.update) {
            UIViewRoot view = facesContext.getViewRoot();
            UIComponent component = view.findComponent(this.update);
            setEntity(entity, component);
        }
        if (null == this.methodExpression) {
            return;
        }
        Object result = this.methodExpression.invoke(elContext, new Object[]{entity});

        UIComponent component = event.getComponent();
        CRUDComponent crudComponent = CRUDComponent.getParentCRUDComponent(component);
        crudComponent.resetCache();
        PrimeFaces primeFaces = PrimeFaces.current();
        String dataTableClientId = getParentDataTableClientId(component);
        primeFaces.ajax().update(dataTableClientId);

        if (null == result) {
            return;
        }
        if (result instanceof FacesMessage) {
            FacesMessage facesMessage = (FacesMessage) result;
            facesContext.addMessage(dataTableClientId, facesMessage);
        } else if (result.getClass().equals(entity.getClass())) {
            CRUDController crudController = CRUDController.getCRUDController();
            EntityManager entityManager = crudController.getEntityManager();
            UserTransaction userTransaction = crudController.getUserTransaction();

            try {
                userTransaction.begin();
            } catch (NotSupportedException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return;
            }

            entityManager.merge(result);

            try {
                userTransaction.commit();
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return;
            }

            UpdateEvent updateEvent = new UpdateEvent(crudComponent, entity);
            updateEvent.queue();
        } else if (result instanceof String) {
            Application application = facesContext.getApplication();
            NavigationHandler navigationHandler = application.getNavigationHandler();
            String outcome = (String) result;
            navigationHandler.handleNavigation(facesContext, null, outcome);
        } else {
            LOGGER.warn("unsupported return type: {}", result.getClass().getName());
        }
    }

    private String getParentDataTableClientId(UIComponent component) {
        while (component.getParent() != null) {
            component = component.getParent();
            if (component instanceof DataTable) {
                return component.getClientId();
            }
        }
        throw new AbortProcessingException();
    }
}
