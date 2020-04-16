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

import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateEvent extends FacesEvent {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateEvent.class);

    private final Object entity;

    public CreateEvent(UIComponent component, Object entity) {
        super(component);
        this.entity = entity;
    }

    @Override
    public boolean isAppropriateListener(FacesListener listener) {
        return (listener instanceof CreateListener);
    }

    @Override
    public void processListener(FacesListener listener) {
        LOGGER.debug("processListener: {}", listener);
        CreateListener createListener = (CreateListener) listener;
        createListener.entityCreated(this);
    }

    public Object getEntity() {
        return this.entity;
    }
}
