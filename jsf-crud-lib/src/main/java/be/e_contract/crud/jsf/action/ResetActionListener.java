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
import javax.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetActionListener implements ActionListener, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetActionListener.class);

    private ValueExpression target;

    private boolean _transient;

    public ResetActionListener() {
        super();
    }

    public ResetActionListener(ValueExpression target) {
        this.target = target;
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        return new Object[]{this.target};
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }
        this.target = (ValueExpression) ((Object[]) state)[0];
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
        String targetId = (String) this.target.getValue(elContext);
        CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(targetId);
        crudComponent.resetCache();
    }
}
