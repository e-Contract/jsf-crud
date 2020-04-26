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
import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalActionAdapter implements ActionListener, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalActionAdapter.class);

    private MethodExpression methodExpression;

    private boolean _transient;

    public GlobalActionAdapter() {
        // empty
    }

    public GlobalActionAdapter(MethodExpression methodExpression) {
        this.methodExpression = methodExpression;
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

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        if (null == this.methodExpression) {
            return;
        }
        Object result = this.methodExpression.invoke(elContext, new Object[]{});

        UIComponent component = event.getComponent();
        CRUDComponent crudComponent = getCRUDComponent(component);
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

    private CRUDComponent getCRUDComponent(UIComponent component) {
        while (component.getParent() != null) {
            component = component.getParent();
            if (component instanceof CRUDComponent) {
                return (CRUDComponent) component;
            }
        }
        throw new AbortProcessingException();
    }
}
