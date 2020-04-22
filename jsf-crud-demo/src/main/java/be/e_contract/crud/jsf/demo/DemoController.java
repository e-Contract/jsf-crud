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
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("demoController")
@SessionScoped
public class DemoController implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    private int amount;

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

    public FacesMessage messagingAction(Object entity) {
        AutoIdEntity autoIdEntity = (AutoIdEntity) entity;
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Custom action on " + autoIdEntity.getId(), null);
        return facesMessage;
    }

    public AutoIdEntity increaseAmount(AutoIdEntity entity) {
        long amount = entity.getAmount();
        amount += 100;
        entity.setAmount(amount);
        // return to get merged
        return entity;
    }

    public String navigationAction(DemoEntity entity) {
        LOGGER.debug("entity: {}", entity.getName());
        return "/demo";
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void addAmount(AutoIdEntity entity) {
        LOGGER.debug("add amount: {}", this.amount);
        entity.setAmount(entity.getAmount() + this.amount);
    }
}
