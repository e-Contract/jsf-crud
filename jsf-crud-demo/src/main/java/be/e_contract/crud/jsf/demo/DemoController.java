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

import be.e_contract.crud.jsf.api.CreateEvent;
import be.e_contract.crud.jsf.api.DeleteEvent;
import be.e_contract.crud.jsf.api.UpdateEvent;
import java.io.ByteArrayInputStream;
import java.io.Serializable;
import javax.annotation.Resource;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("demoController")
@SessionScoped
public class DemoController implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;

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

    public FacesMessage globalAction() {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Custom global action", null);
        return facesMessage;
    }

    //@javax.transaction.Transactional only since JTA1.2 hence Java EE 7
    // so for the demo we do it manually
    public void deleteAll() {
        try {
            this.userTransaction.begin();
        } catch (NotSupportedException | SystemException ex) {
            LOGGER.error("transaction error: " + ex.getMessage(), ex);
            return;
        }
        LOGGER.debug("DELETE FROM AutoIdEntity");
        Query query = this.entityManager.createQuery("DELETE FROM AutoIdEntity");
        query.executeUpdate();
        try {
            this.userTransaction.commit();
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
            LOGGER.error("transaction error: " + ex.getMessage(), ex);
            return;
        }
    }

    public StreamedContent download() {
        LOGGER.debug("download");
        StreamedContent streamedContent = new DefaultStreamedContent(new ByteArrayInputStream("hello world".getBytes()), "text/plain", "filename.txt");
        return streamedContent;
    }

    public StreamedContent downloadTextFile(AutoIdEntity entity) {
        LOGGER.debug("download");
        byte[] textFile = entity.getTextFile();
        if (null == textFile) {
            return null;
        }
        StreamedContent streamedContent = new DefaultStreamedContent(new ByteArrayInputStream(textFile), "text/plain", "filename.txt");
        return streamedContent;
    }
}
