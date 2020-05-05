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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Temporal;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityInspector {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityInspector.class);

    private static final Map<String, Class<?>> ENTITY_CLASS_MAP;

    static {
        ENTITY_CLASS_MAP = new HashMap<>();
    }

    private Class<?> entityClass;

    private final EntityType<?> entityType;

    private final Metamodel metamodel;

    private final PersistenceUnitUtil persistenceUnitUtil;

    public EntityInspector(EntityManager entityManager, String entityClassName) {
        if (entityClassName.endsWith(".class")) {
            entityClassName = entityClassName.substring(0, entityClassName.indexOf(".class"));
        }
        this.entityClass = ENTITY_CLASS_MAP.get(entityClassName);
        this.metamodel = entityManager.getMetamodel();
        this.persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        if (null != this.entityClass) {
            this.entityType = metamodel.entity(this.entityClass);
            return;
        }
        try {
            this.entityClass = Class.forName(entityClassName);
        } catch (ClassNotFoundException ex) {
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
                LOGGER.error("Did you forget to put the @Entity annotation?");
                throw new IllegalArgumentException("entity class not found: " + entityClassName);
            }
        }
        Entity entityAnnotation = this.entityClass.getAnnotation(Entity.class);
        if (null == entityAnnotation) {
            LOGGER.error("class is not a JPA entity: {}", entityClassName);
            throw new IllegalArgumentException("class is not a JPA entity: " + entityClassName);
        }
        ENTITY_CLASS_MAP.put(entityClassName, this.entityClass);
        this.entityType = metamodel.entity(this.entityClass);
    }

    public Class<?> getEntityClass() {
        return this.entityClass;
    }

    public EntityInspector(EntityManager entityManager, Class<?> entityClass) {
        this.entityClass = entityClass;
        this.metamodel = entityManager.getMetamodel();
        this.entityType = metamodel.entity(this.entityClass);
        this.persistenceUnitUtil = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
        Entity entityAnnotation = this.entityClass.getAnnotation(Entity.class);
        if (null == entityAnnotation) {
            LOGGER.error("class is not a JPA entity: {}", entityClass.getName());
            throw new IllegalArgumentException("class is not a JPA entity: " + entityClass.getName());
        }
    }

    public EntityInspector(EntityManager entityManager, Object entity) {
        this(entityManager, entity.getClass());
    }

    public String getEntityName() {
        String entityName = this.entityClass.getSimpleName();
        if (entityName.endsWith("Entity")) {
            entityName = entityName.substring(0, entityName.indexOf("Entity"));
        }
        return toHumanReadable(entityName);
    }

    private List<SingularAttribute> getIdAttributes() {
        List<SingularAttribute> idAttributes = new LinkedList<>();
        Set attributes = this.entityType.getAttributes();
        for (Object attributeObject : attributes) {
            if (!(attributeObject instanceof SingularAttribute)) {
                continue;
            }
            SingularAttribute attribute = (SingularAttribute) attributeObject;
            if (attribute.isId()) {
                idAttributes.add(attribute);
            }
        }
        return idAttributes;
    }

    public List<Field> getIdFields() {
        List<Field> idFields = new LinkedList<>();
        List<SingularAttribute> idAttributes = getIdAttributes();
        for (SingularAttribute idAttribute : idAttributes) {
            Field idField;
            try {
                idField = this.entityClass.getDeclaredField(idAttribute.getName());
            } catch (NoSuchFieldException | SecurityException ex) {
                LOGGER.error("reflection error: " + ex.getMessage(), ex);
                continue;
            }
            idFields.add(idField);
        }
        return idFields;
    }

    public boolean isEmbeddedIdField() {
        List<SingularAttribute> idAttributes = getIdAttributes();
        SingularAttribute firstIdAttribute = idAttributes.iterator().next();
        return firstIdAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED;
    }

    public boolean isIdGeneratedValue() {
        List<SingularAttribute> idAttributes = getIdAttributes();
        SingularAttribute firstIdAttribute = idAttributes.iterator().next();
        String idAttributeName = firstIdAttribute.getName();
        return getAnnotation(idAttributeName, GeneratedValue.class) != null;
    }

    public <T extends Annotation> T getAnnotation(Field entityField, Field embeddableField, Class<T> annotationClass) {
        String attributeName = entityField.getName();
        String embeddedAttributeName;
        if (null != embeddableField) {
            embeddedAttributeName = embeddableField.getName();
        } else {
            embeddedAttributeName = null;
        }
        return getAnnotation(attributeName, embeddedAttributeName, annotationClass);
    }

    public <T extends Annotation> T getAnnotation(Field entityField, Class<T> annotationClass) {
        return getAnnotation(entityField.getName(), annotationClass);
    }

    public <T extends Annotation> T getAnnotation(String attributeName, Class<T> annotationClass) {
        return getAnnotation(attributeName, null, annotationClass);
    }

    public <T extends Annotation> T getAnnotation(String attributeName, String embeddableAttributeName, Class<T> annotationClass) {
        Attribute attribute = this.entityType.getAttribute(attributeName);
        if (null == attribute) {
            LOGGER.error("unknown attribute: {}", attributeName);
            return null;
        }
        Member member;
        if (embeddableAttributeName != null) {
            if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.EMBEDDED) {
                LOGGER.error("attribute {} is not embedded", attributeName);
                return null;
            }
            EmbeddableType embeddableType = this.metamodel.embeddable(attribute.getJavaType());
            attribute = embeddableType.getAttribute(embeddableAttributeName);
            member = attribute.getJavaMember();
        } else {
            member = attribute.getJavaMember();
            if (!member.getDeclaringClass().equals(this.entityClass)) {
                // can happen in case of IdClass attributes, where Attribute points to the IdClass, not the Entity class.
                // here we default to field access type
                try {
                    member = this.entityClass.getDeclaredField(attributeName);
                } catch (NoSuchFieldException | SecurityException ex) {
                    LOGGER.error("reflection error: " + ex.getMessage(), ex);
                }
            }
        }
        if (member instanceof Field) {
            Field field = (Field) member;
            return field.getAnnotation(annotationClass);
        }
        if (member instanceof Method) {
            Method method = (Method) member;
            return method.getAnnotation(annotationClass);
        }
        throw new RuntimeException("could not retrieve annoation for attribute: " + attributeName);
    }

    public boolean isTemporal(Field field) {
        return null != getAnnotation(field.getName(), Temporal.class);
    }

    public Object getIdentifier(Object entity) {
        return this.persistenceUnitUtil.getIdentifier(entity);
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

    public static String toHumanReadable(Field field) {
        return toHumanReadable(field.getName());
    }

    public static String toHumanReadable(String label) {
        Pattern pattern = Pattern.compile("(?=\\p{Lu})");
        String[] splits = pattern.split(WordUtils.capitalize(label));
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
            Attribute attribute = this.entityType.getAttribute(entityField.getName());
            if (attribute instanceof SingularAttribute) {
                SingularAttribute singularAttribute = (SingularAttribute) attribute;
                if (singularAttribute.isId()) {
                    continue;
                }
            }
            if (null != attribute && attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                continue;
            }
            otherFields.add(entityField);
        }
        return otherFields;
    }

    public List<Field> getEmbeddedFields() {
        List<Field> embeddedFields = new LinkedList<>();
        Field[] entityFields = this.entityClass.getDeclaredFields();
        for (Field entityField : entityFields) {
            if (Modifier.isStatic(entityField.getModifiers())) {
                continue;
            }
            if (entityField.getName().startsWith("_persistence_")) {
                // payara
                continue;
            }
            Attribute attribute = this.entityType.getAttribute(entityField.getName());
            if (null == attribute) {
                continue;
            }
            if (attribute instanceof SingularAttribute) {
                SingularAttribute singularAttribute = (SingularAttribute) attribute;
                if (singularAttribute.isId()) {
                    continue;
                }
            }
            if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                embeddedFields.add(entityField);
            }
        }
        return embeddedFields;
    }
}
