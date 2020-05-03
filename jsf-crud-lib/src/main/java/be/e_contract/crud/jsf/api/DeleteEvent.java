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

public class DeleteEvent extends AbstractEntityEvent<DeleteListener> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteEvent.class);

    public DeleteEvent(UIComponent component, Object entity) {
        super(component, entity, DeleteListener.class);
    }

    @Override
    public void processEntityListener(DeleteListener listener) {
        LOGGER.debug("processListener: {}", listener);
        listener.entityDeleted(this);
    }
}
