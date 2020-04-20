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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityInspector {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityInspector.class);

    private Class<?> entityClass;

    public EntityInspector(String entityClassName) {
        if (entityClassName.endsWith(".class")) {
            entityClassName = entityClassName.substring(0, entityClassName.indexOf(".class"));
        }
        try {
            this.entityClass = Class.forName(entityClassName);
        } catch (ClassNotFoundException ex) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            Application application = facesContext.getApplication();
            ExpressionFactory expressionFactory = application.getExpressionFactory();
            ELContext elContext = facesContext.getELContext();
            ValueExpression valueExpression = expressionFactory.createValueExpression(elContext, "#{crudController}", CRUDController.class);
            CRUDController crudController = (CRUDController) valueExpression.getValue(elContext);
            EntityManager entityManager = crudController.getEntityManager();
            Metamodel metamodel = entityManager.getMetamodel();
            Set<EntityType<?>> entities = metamodel.getEntities();
            for (EntityType<?> entity : entities) {
                String entityName = entity.getName();
                if (entityClassName.equals(entityName)) {
                    this.entityClass = entity.getJavaType();
                    break;
                }
            }
            if (null == this.entityClass) {
                LOGGER.error("entity class not found: " + entityClassName);
                throw new IllegalArgumentException("entity class not found: " + entityClassName);
            }
        }
        Entity entityAnnotation = this.entityClass.getAnnotation(Entity.class);
        if (null == entityAnnotation) {
            LOGGER.error("class is not a JPA entity: {}", entityClassName);
            throw new IllegalArgumentException("class is not a JPA entity: " + entityClassName);
        }
    }

    public Class<?> getEntityClass() {
        return this.entityClass;
    }

    public EntityInspector(Class<?> entityClass) {
        this.entityClass = entityClass;
        Entity entityAnnotation = this.entityClass.getAnnotation(Entity.class);
        if (null == entityAnnotation) {
            LOGGER.error("class is not a JPA entity: {}", entityClass.getName());
            throw new IllegalArgumentException("class is not a JPA entity: " + entityClass.getName());
        }
    }

    public EntityInspector(Object entity) {
        this(entity.getClass());
    }

    public String getEntityName() {
        String entityName = this.entityClass.getSimpleName();
        if (entityName.endsWith("Entity")) {
            entityName = entityName.substring(0, entityName.indexOf("Entity"));
        }
        return entityName;
    }

    public Field getIdField() {
        Field[] entityFields = this.entityClass.getDeclaredFields();
        for (Field entityField : entityFields) {
            Id idAnnotation = entityField.getAnnotation(Id.class);
            if (idAnnotation != null) {
                return entityField;
            }
        }
        throw new RuntimeException("@Id field not present");
    }

    public Object getIdentifier(Object entity) {
        Field idField = getIdField();
        idField.setAccessible(true);
        try {
            return idField.get(entity);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.error("reflection error: " + ex.getMessage(), ex);
            return null;
        }
    }

    public String toHumanReadable(Object entity) {
        Method toStringMethod;
        try {
            toStringMethod = this.entityClass.getMethod("toString", new Class[]{});
        } catch (NoSuchMethodException | SecurityException ex) {
            LOGGER.error("reflection error: " + ex.getMessage(), ex);
            return entity.toString();
        }
        if (toStringMethod.getDeclaringClass() == this.entityClass) {
            return entity.toString();
        }
        Object identifier = getIdentifier(entity);
        return identifier.toString();
    }

    public String toHumanReadable(Field field) {
        String str = WordUtils.capitalize(field.getName());
        Pattern pattern = Pattern.compile("(?=\\p{Lu})");
        String[] splits = pattern.split(str);
        StringBuilder result = new StringBuilder();
        result.append(splits[0]);
        for (int idx = 1; idx < splits.length; idx++) {
            result.append(" ");
            result.append(splits[idx]);
        }
        return result.toString();
    }

    public List<Field> getOtherFields() {
        List<Field> otherFields = new LinkedList<>();
        Field[] entityFields = this.entityClass.getDeclaredFields();
        for (Field entityField : entityFields) {
            Id idAnnotation = entityField.getAnnotation(Id.class);
            if (idAnnotation != null) {
                continue;
            }
            if (Modifier.isStatic(entityField.getModifiers())) {
                continue;
            }
            if (Modifier.isTransient(entityField.getModifiers())) {
                continue;
            }
            if (entityField.getName().startsWith("_persistence_")) {
                // payara
                continue;
            }
            otherFields.add(entityField);
        }
        return otherFields;
    }
}
