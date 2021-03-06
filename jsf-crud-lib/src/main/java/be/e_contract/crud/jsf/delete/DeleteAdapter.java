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
package be.e_contract.crud.jsf.delete;

import be.e_contract.crud.jsf.api.DeleteListener;
import be.e_contract.crud.jsf.api.DeleteEvent;
import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteAdapter implements DeleteListener, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteAdapter.class);

    private MethodExpression methodExpression;

    private boolean _transient;

    public DeleteAdapter() {
        super();
    }

    public DeleteAdapter(MethodExpression methodExpression) {
        this.methodExpression = methodExpression;
    }

    @Override
    public void entityDeleted(DeleteEvent event) {
        LOGGER.debug("entityDeleted: {}", event);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        this.methodExpression.invoke(elContext, new Object[]{event});
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
