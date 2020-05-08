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
package be.e_contract.crud.jsf.demo;

import be.e_contract.crud.jsf.api.CRUD;
import be.e_contract.crud.jsf.api.CreateEvent;
import be.e_contract.crud.jsf.api.CreateListener;
import be.e_contract.crud.jsf.api.DeleteEvent;
import be.e_contract.crud.jsf.api.DeleteListener;
import be.e_contract.crud.jsf.api.UpdateEvent;
import be.e_contract.crud.jsf.api.UpdateListener;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("apiDemoController")
@SessionScoped
public class APIDemoController implements CreateListener, UpdateListener, DeleteListener, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(APIDemoController.class);

    private UIComponent binding;

    public UIComponent getBinding() {
        return this.binding;
    }

    public void setBinding(UIComponent binding) {
        LOGGER.debug("setBinding: {}", this);
        this.binding = binding;
    }

    @Override
    public void entityCreated(CreateEvent event) {
        LOGGER.debug("entity created: {}", event.getEntity());
    }

    @Override
    public void entityUpdated(UpdateEvent event) {
        LOGGER.debug("entity updated: {}", event.getEntity());
    }

    @Override
    public void entityDeleted(DeleteEvent event) {
        LOGGER.debug("entity deleted: {}", event.getEntity());
    }

    public void init() {
        LOGGER.debug("init: {}", this);
        CRUD crud = (CRUD) this.binding;
        // NPE on Open Liberty 20.0.0.5 here
        crud.addCreateListener(this);
        crud.addUpdateListener(this);
        crud.addDeleteListener(this);
    }
}
