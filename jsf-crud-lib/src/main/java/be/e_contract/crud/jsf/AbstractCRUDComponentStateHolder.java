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

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;

public abstract class AbstractCRUDComponentStateHolder implements StateHolder {

    private String crudComponentId;

    private boolean _transient;

    public AbstractCRUDComponentStateHolder() {
        super();
    }

    protected AbstractCRUDComponentStateHolder(String crudComponentId) {
        this.crudComponentId = crudComponentId;
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        return new Object[]{this.crudComponentId};
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }
        this.crudComponentId = (String) ((Object[]) state)[0];
    }

    @Override
    public boolean isTransient() {
        return this._transient;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        this._transient = newTransientValue;
    }

    protected CRUDComponent getCRUDComponent() {
        return CRUDComponent.getCRUDComponent(this.crudComponentId);
    }
}
