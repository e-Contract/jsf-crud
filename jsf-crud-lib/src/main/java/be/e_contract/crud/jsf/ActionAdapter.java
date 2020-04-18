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

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
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
import org.primefaces.component.datatable.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionAdapter implements ActionListener, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionAdapter.class);

    private MethodExpression methodExpression;

    private boolean _transient;

    public ActionAdapter() {
        // empty
    }

    public ActionAdapter(MethodExpression methodExpression) {
        this.methodExpression = methodExpression;
    }

    @Override
    public Object saveState(FacesContext context) {
        LOGGER.debug("saveState: {}", this.methodExpression);
        if (context == null) {
            throw new NullPointerException();
        }
        return new Object[]{this.methodExpression};
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        LOGGER.debug("restoreState");
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

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ELResolver elResolver = elContext.getELResolver();
        Object entity = elResolver.getValue(elContext, null, "row");
        Object result = this.methodExpression.invoke(elContext, new Object[]{entity});
        if (null == result) {
            return;
        }
        if (result instanceof FacesMessage) {
            FacesMessage facesMessage = (FacesMessage) result;
            UIComponent component = event.getComponent();
            String dataTableClientId = getParentDataTableClientId(component);
            facesContext.addMessage(dataTableClientId, facesMessage);
        } else if (result.getClass().equals(entity.getClass())) {
            Application application = facesContext.getApplication();
            ExpressionFactory expressionFactory = application.getExpressionFactory();
            ValueExpression valueExpression = expressionFactory.createValueExpression(elContext, "#{crudController}", CRUDController.class);
            CRUDController crudController = (CRUDController) valueExpression.getValue(elContext);
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

            UIComponent component = event.getComponent();
            String dataTableClientId = getParentDataTableClientId(component);
            EntityInspector entityInspector = new EntityInspector(result);
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Saved " + entityInspector.toHumanReadable(result), null);
            facesContext.addMessage(dataTableClientId, facesMessage);
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
