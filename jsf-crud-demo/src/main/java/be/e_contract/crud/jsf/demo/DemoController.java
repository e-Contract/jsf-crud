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

import be.e_contract.crud.jsf.CreateEvent;
import be.e_contract.crud.jsf.DeleteEvent;
import be.e_contract.crud.jsf.UpdateEvent;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("demoController")
public class DemoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    public void created(CreateEvent createEvent) {
        LOGGER.debug("created: {}", createEvent.getEntity());
    }

    public void updated(UpdateEvent updateEvent) {
        LOGGER.debug("updated: {}", updateEvent.getEntity());
    }

    public void deleted(DeleteEvent deleteEvent) {
        LOGGER.debug("deleted: {}", deleteEvent.getEntity());
    }

    public void action(Object entity) {
        LOGGER.debug("custom action: {}", entity);
        if (entity instanceof AutoIdEntity) {
            AutoIdEntity autoIdEntity = (AutoIdEntity) entity;
            LOGGER.debug("entity identifier: {}", autoIdEntity.getId());
        }
    }
}
