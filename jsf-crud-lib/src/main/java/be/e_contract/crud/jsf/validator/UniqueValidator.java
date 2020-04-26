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
package be.e_contract.crud.jsf.validator;

import be.e_contract.crud.jsf.CRUDController;
import be.e_contract.crud.jsf.EntityInspector;
import java.lang.reflect.Field;
import java.util.List;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniqueValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueValidator.class);

    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
        LOGGER.debug("validate");
        ValueExpression valueExpression = component.getValueExpression("value");
        if (null == valueExpression) {
            return;
        }
        ValueReference valueReference = valueExpression.getValueReference(facesContext.getELContext());
        if (null == valueReference) {
            return;
        }
        Object entity = valueReference.getBase();
        String property = (String) valueReference.getProperty();
        LOGGER.debug("entity: {}", entity);
        LOGGER.debug("property: {}", property);
        if (null == entity) {
            return;
        }
        if (null == property) {
            return;
        }
        EntityInspector entityInspector = new EntityInspector(entity);
        Class<?> entityClass = entityInspector.getEntityClass();
        Field field;
        try {
            field = entityClass.getDeclaredField(property);
        } catch (NoSuchFieldException | SecurityException ex) {
            LOGGER.error("reflection error: " + ex.getMessage(), ex);
            return;
        }
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (null == columnAnnotation) {
            return;
        }
        if (!columnAnnotation.unique()) {
            return;
        }
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        UserTransaction userTransaction = crudController.getUserTransaction();
        try {
            userTransaction.begin();
        } catch (NotSupportedException | SystemException ex) {
            LOGGER.error("transaction error: " + ex.getMessage(), ex);
            return;
        }
        try {
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
            Root<? extends Object> entityRoot = criteriaQuery.from(entityClass);
            criteriaQuery.select(entityRoot);
            ParameterExpression<Object> parameter = criteriaBuilder.parameter(Object.class, property);
            Predicate predicate = criteriaBuilder.equal(entityRoot.get(property), parameter);
            criteriaQuery.where(predicate);

            TypedQuery<Object> query = entityManager.createQuery(criteriaQuery);
            query.setParameter(property, value);
            List entities = query.getResultList();

            if (entities.isEmpty()) {
                return;
            }
            Object existingEntity = entities.get(0);
            if (existingEntity.equals(entity)) {
                return;
            }
            FacesMessage facesMessage = new FacesMessage("Not unique.");
            facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(facesMessage);
        } finally {
            try {
                userTransaction.commit();
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
                LOGGER.error("transaction error: " + ex.getMessage(), ex);
            }
        }
    }
}
