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
package be.e_contract.crud.jsf.api;

import javax.faces.component.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateEvent extends AbstractEntityEvent<CreateListener> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateEvent.class);

    public CreateEvent(UIComponent component, Object entity) {
        super(component, entity, CreateListener.class);
    }

    @Override
    public void processEntityListener(CreateListener listener) {
        LOGGER.debug("processListener: {}", listener);
        listener.entityCreated(this);
    }
}
