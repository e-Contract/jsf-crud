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
package be.e_contract.crud.jsf.el;

import be.e_contract.crud.jsf.CRUDComponent;
import be.e_contract.crud.jsf.component.QueryComponent;
import be.e_contract.crud.jsf.jpa.CRUDController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.el.ELContext;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityValueExpression extends ValueExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityValueExpression.class);

    private String crudComponentId;

    public EntityValueExpression() {
        super();
        LOGGER.debug("default constructor");
    }

    public EntityValueExpression(String crudComponentId) {
        this.crudComponentId = crudComponentId;
    }

    @Override
    public Object getValue(ELContext context) {
        LOGGER.debug("getValue");
        CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(this.crudComponentId);
        Class<?> entityClass = crudComponent.getEntityClass();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = facesContext.getViewRoot();
        Map<String, Object> viewMap = viewRoot.getViewMap();
        String key = EntityValueExpression.class.getName();
        Map<String, Object> entityViewMap = (Map<String, Object>) viewMap.get(key);
        if (null != entityViewMap) {
            Object cachedObject = entityViewMap.get(this.crudComponentId);
            if (null != cachedObject) {
                return cachedObject;
            }
        } else {
            entityViewMap = new HashMap<>();
            viewMap.put(key, entityViewMap);
        }

        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        UserTransaction userTransaction = crudController.getUserTransaction();

        try {
            userTransaction.begin();
        } catch (NotSupportedException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new PropertyNotFoundException();
        }

        List resultList;
        QueryComponent queryComponent = crudComponent.findQueryComponent();
        if (null != queryComponent) {
            Query query = queryComponent.getQuery(entityManager, context);
            resultList = query.getResultList();
        } else {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
            Root<? extends Object> entity = criteriaQuery.from(entityClass);
            criteriaQuery.select(entity);

            if (!UIInput.isEmpty(crudComponent.getOrderBy())) {
                Path path = entity.get(crudComponent.getOrderBy());
                if (crudComponent.isAscending()) {
                    criteriaQuery.orderBy(criteriaBuilder.asc(path));
                } else {
                    criteriaQuery.orderBy(criteriaBuilder.desc(path));
                }
            }

            TypedQuery<Object> query = entityManager.createQuery(criteriaQuery);
            resultList = query.getResultList();
        }

        try {
            userTransaction.commit();
        } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            throw new PropertyNotFoundException();
        }

        entityViewMap.put(crudComponent.getId(), resultList);
        return resultList;
    }

    public void resetCache() {
        LOGGER.debug("resetCache");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = facesContext.getViewRoot();
        Map<String, Object> viewMap = viewRoot.getViewMap();
        String key = EntityValueExpression.class.getName();
        Map<String, Object> entityViewMap = (Map<String, Object>) viewMap.get(key);
        if (null != entityViewMap) {
            entityViewMap.remove(this.crudComponentId);
            if (entityViewMap.isEmpty()) {
                viewMap.remove(key);
            }
        }
    }

    @Override
    public void setValue(ELContext context, Object value) {
        LOGGER.debug("setValue");
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        LOGGER.debug("isReadOnly");
        return true;
    }

    @Override
    public Class<?> getType(ELContext context) {
        LOGGER.debug("getType");
        return List.class;
    }

    @Override
    public Class<?> getExpectedType() {
        LOGGER.debug("getExpectedType");
        return null;
    }

    @Override
    public String getExpressionString() {
        LOGGER.debug("getExpressionString");
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        LOGGER.debug("equals");
        return false;
    }

    @Override
    public int hashCode() {
        LOGGER.debug("hashCode");
        return 0;
    }

    @Override
    public boolean isLiteralText() {
        LOGGER.debug("isLiteralText");
        return false;
    }
}
