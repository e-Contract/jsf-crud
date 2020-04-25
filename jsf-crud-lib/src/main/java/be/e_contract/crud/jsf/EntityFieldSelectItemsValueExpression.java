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

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityFieldSelectItemsValueExpression extends ValueExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityFieldSelectItemsValueExpression.class);

    private final Field entityField;

    public EntityFieldSelectItemsValueExpression(Field entityField) {
        this.entityField = entityField;
    }

    @Override
    public Object getValue(ELContext context) {
        LOGGER.debug("getValue");
        Type type = this.entityField.getGenericType();
        LOGGER.debug("type class: {}", type.getClass().getName());
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Class<?> listTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
        LOGGER.debug("list type class: {}", listTypeClass.getName());
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> criteriaQuery = criteriaBuilder.createQuery(Object.class);
        Root<? extends Object> entity = criteriaQuery.from(listTypeClass);
        criteriaQuery.select(entity);

        TypedQuery<Object> query = entityManager.createQuery(criteriaQuery);
        List entities = query.getResultList();

        EntityInspector entityInspector = new EntityInspector(listTypeClass);
        List<SelectItem> selectItems = new LinkedList<>();
        for (Object entityObject : entities) {
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
