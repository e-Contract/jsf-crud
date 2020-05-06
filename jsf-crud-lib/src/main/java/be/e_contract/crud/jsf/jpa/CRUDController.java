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
package be.e_contract.crud.jsf.jpa;

import be.e_contract.crud.jsf.api.cdi.PreCreateEvent;
import be.e_contract.crud.jsf.api.cdi.PreDeleteEvent;
import be.e_contract.crud.jsf.api.cdi.PreUpdateEvent;
import javax.annotation.Resource;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.enterprise.event.Event;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.UserTransaction;

@Named("crudController")
public class CRUDController {

    @PersistenceContext
    private EntityManager entityManager;

    @Resource
    private UserTransaction userTransaction;

    @Inject
    private Event<PreCreateEvent> preCreateEvent;

    @Inject
    private Event<PreUpdateEvent> preUpdateEvent;

    @Inject
    private Event<PreDeleteEvent> preDeleteEvent;

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public UserTransaction getUserTransaction() {
        return this.userTransaction;
    }

    public static CRUDController getCRUDController() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExpression = expressionFactory.createValueExpression(elContext, "#{crudController}", CRUDController.class);
        CRUDController crudController = (CRUDController) valueExpression.getValue(elContext);
        return crudController;
    }

    public static Metamodel getMetamodel() {
        CRUDController crudController = getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        Metamodel metamodel = entityManager.getMetamodel();
        return metamodel;
    }

    public void firePreCreateEvent(Object entity) {
        PreCreateEvent event = new PreCreateEvent(entity);
        this.preCreateEvent.fire(event);
    }

    public void firePreUpdateEvent(Object entity) {
        PreUpdateEvent event = new PreUpdateEvent(entity);
        this.preUpdateEvent.fire(event);
    }

    public void firePreDeleteEvent(Object entity) {
        PreDeleteEvent event = new PreDeleteEvent(entity);
        this.preDeleteEvent.fire(event);
    }
}
