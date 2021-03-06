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
import be.e_contract.crud.jsf.jpa.EntityInspector;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.el.ELContext;
import javax.el.PropertyNotFoundException;
import javax.el.ValueExpression;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntitySelectItemsValueExpression extends ValueExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntitySelectItemsValueExpression.class);

    private final String entityClassName;

    private final String notReferencedByClassName;

    private final String entityAttribute;

    private final String crudComponentId;

    private final String queryComponentId;

    public EntitySelectItemsValueExpression(CRUDComponent crudComponent, QueryComponent queryComponent) {
        this(null, null, null, crudComponent, queryComponent);
    }

    public EntitySelectItemsValueExpression(Class<?> entityClass) {
        this(entityClass, null, null, null);
    }

    public EntitySelectItemsValueExpression(Class<?> entityClass, Class<?> notReferencedByClass, String entityAttribute, CRUDComponent crudComponent) {
        this(entityClass, notReferencedByClass, entityAttribute, crudComponent, null);
    }

    public EntitySelectItemsValueExpression(Class<?> entityClass, Class<?> notReferencedByClass, String entityAttribute, CRUDComponent crudComponent, QueryComponent queryComponent) {
        if (null != entityClass) {
            this.entityClassName = entityClass.getName();
        } else {
            this.entityClassName = null;
        }
        if (null != notReferencedByClass) {
            this.notReferencedByClassName = notReferencedByClass.getName();
        } else {
            this.notReferencedByClassName = null;
        }
        this.entityAttribute = entityAttribute;
        if (null != crudComponent) {
            this.crudComponentId = crudComponent.getId();
        } else {
            this.crudComponentId = null;
        }
        if (null != queryComponent) {
            this.queryComponentId = queryComponent.getId();
        } else {
            this.queryComponentId = null;
        }
    }

    @Override
    public Object getValue(ELContext context) {
        LOGGER.debug("getValue");
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();

        EntityInspector entityInspector = null;
        List entities;
        if (null != this.queryComponentId) {
            CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(this.crudComponentId);
            QueryComponent queryComponent = (QueryComponent) crudComponent.findComponent(this.queryComponentId);
            Query query = queryComponent.getQuery(entityManager, context);
            entities = query.getResultList();
        } else if (null == this.notReferencedByClassName) {
            entityInspector = new EntityInspector(entityManager, this.entityClassName);
            Class<?> entityClass = entityInspector.getEntityClass();
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<? extends Object> entity = criteriaQuery.from(entityClass);
            criteriaQuery.select(entity);
            TypedQuery<Object> query = entityManager.createQuery(criteriaQuery);
            entities = query.getResultList();
        } else {
            entities = new LinkedList();
            if (null != this.crudComponentId) {
                CRUDComponent crudComponent = CRUDComponent.getCRUDComponent(this.crudComponentId);
                Object entity = crudComponent.getSelection();
                if (null != entity) {
                    Field valueField;
                    try {
                        valueField = entity.getClass().getDeclaredField(this.entityAttribute);
                    } catch (NoSuchFieldException | SecurityException ex) {
                        throw new PropertyNotFoundException();
                    }
                    valueField.setAccessible(true);
                    Object value;
                    try {
                        value = valueField.get(entity);
                    } catch (IllegalArgumentException | IllegalAccessException ex) {
                        throw new PropertyNotFoundException();
                    }
                    LOGGER.debug("existing value: {}", value);
                    if (value != null) {
                        if (value instanceof Collection) {
                            Collection collectionValue = (Collection) value;
                            entities.addAll(collectionValue);
                        } else {
                            entities.add(value);
                        }
                    }
                }
            }
            entityInspector = new EntityInspector(entityManager, this.entityClassName);
            Class<?> entityClass = entityInspector.getEntityClass();
            CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
            CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root root = criteriaQuery.from(entityClass);
            criteriaQuery.select(root);

            Subquery subqueryCriteriaQuery = criteriaQuery.subquery(entityClass);
            Root subroot;
            try {
                subroot = subqueryCriteriaQuery.from(Class.forName(this.notReferencedByClassName));
            } catch (ClassNotFoundException ex) {
                throw new PropertyNotFoundException();
            }
            Join join = subroot.join(this.entityAttribute);
            subqueryCriteriaQuery.select(join);
            subqueryCriteriaQuery.distinct(true);

            criteriaQuery.where(criteriaBuilder.not(root.in(subqueryCriteriaQuery)));

            TypedQuery<Object> query = entityManager.createQuery(criteriaQuery);
            entities.addAll(query.getResultList());
        }

        List<SelectItem> selectItems = new LinkedList<>();
        for (Object entityObject : entities) {
            if (null == entityInspector) {
                entityInspector = new EntityInspector(entityManager, entityObject);
            }
            SelectItem selectItem = new SelectItem(entityObject, entityInspector.toHumanReadable(entityObject));
            selectItems.add(selectItem);
        }
        return selectItems;
    }

    @Override
    public void setValue(ELContext context, Object value) {
        LOGGER.debug("setValue: {}", value);
    }

    @Override
    public boolean isReadOnly(ELContext context) {
        LOGGER.debug("isReadOnly");
        return true;
    }

    @Override
    public Class<?> getType(ELContext context) {
        //LOGGER.debug("getType");
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
        //LOGGER.debug("isLiteralText");
        return false;
    }
}
