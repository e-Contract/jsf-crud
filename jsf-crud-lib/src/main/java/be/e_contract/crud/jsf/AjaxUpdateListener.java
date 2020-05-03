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

import be.e_contract.crud.jsf.api.CreateEvent;
import be.e_contract.crud.jsf.api.CreateListener;
import be.e_contract.crud.jsf.api.DeleteEvent;
import be.e_contract.crud.jsf.api.DeleteListener;
import be.e_contract.crud.jsf.api.UpdateEvent;
import be.e_contract.crud.jsf.api.UpdateListener;
import be.e_contract.crud.jsf.jpa.CRUDController;
import be.e_contract.crud.jsf.jpa.EntityInspector;
import java.util.LinkedList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AjaxUpdateListener implements CreateListener, UpdateListener, DeleteListener, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AjaxUpdateListener.class);

    private List<String> clientIds;

    private String crudComponentId;

    private boolean _transient;

    public AjaxUpdateListener() {
        LOGGER.debug("AjaxUpdateListener default constructor");
        this.clientIds = new LinkedList<>();
    }

    public AjaxUpdateListener(String crudComponentId) {
        this.crudComponentId = crudComponentId;
        this.clientIds = new LinkedList<>();
    }

    public void addClientId(String clientId) {
        this.clientIds.add(clientId);
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        return new Object[]{this.crudComponentId, this.clientIds};
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
        this.clientIds = (List<String>) ((Object[]) state)[1];
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
    public void entityCreated(CreateEvent event) {
        CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(this.crudComponentId);
        crudComponent.resetCache();
        Object entity = event.getEntity();
        fireUpdates(entity);
    }

    @Override
    public void entityUpdated(UpdateEvent event) {
        Object entity = event.getEntity();
        fireUpdates(entity);
        EntityInspector entityInspector = new EntityInspector(CRUDController.getMetamodel(), entity);
        String entityHumanReadable = entityInspector.toHumanReadable(entity);
        CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(this.crudComponentId);
        crudComponent.addMessage(FacesMessage.SEVERITY_INFO, "Updated " + entityHumanReadable);
    }

    private void fireUpdates(Object entity) {
        if (null == entity) {
            return;
        }
        PrimeFaces primeFaces = PrimeFaces.current();
        if (primeFaces.isAjaxRequest()) {
            LOGGER.debug("firing updates: {}", this.clientIds);
            primeFaces.ajax().update(this.clientIds);
        }
    }

    @Override
    public void entityDeleted(DeleteEvent event) {
        CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(this.crudComponentId);
        crudComponent.resetCache();
        Object entity = event.getEntity();
        fireUpdates(entity);
    }
}
