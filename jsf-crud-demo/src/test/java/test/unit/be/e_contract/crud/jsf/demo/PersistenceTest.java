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
package test.unit.be.e_contract.crud.jsf.demo;

import be.e_contract.crud.jsf.demo.Address;
import be.e_contract.crud.jsf.demo.AutoIdEntity;
import be.e_contract.crud.jsf.demo.CarEntity;
import be.e_contract.crud.jsf.demo.DemoEntity;
import be.e_contract.crud.jsf.demo.IdClassEntity;
import be.e_contract.crud.jsf.demo.MessageEntity;
import be.e_contract.crud.jsf.demo.PersonEntity;
import be.e_contract.crud.jsf.demo.PropertyAccessTypeEntity;
import be.e_contract.crud.jsf.jpa.EntityInspector;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceTest.class);

    private EntityManager entityManager;

    @BeforeEach
    public void setUp() throws Exception {
        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("test");
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.entityManager.close();
    }

    @Test
    public void testEntityManager() throws Exception {
        EntityTransaction entityTransaction = this.entityManager
                .getTransaction();

        entityTransaction.begin();
        {
            CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
            CriteriaQuery<PersonEntity> criteriaQuery = criteriaBuilder.createQuery(PersonEntity.class);
            Root<PersonEntity> person = criteriaQuery.from(PersonEntity.class);
            criteriaQuery.select(person);

            criteriaQuery.orderBy(criteriaBuilder.asc(person.get("name")));

            TypedQuery<PersonEntity> query = this.entityManager.createQuery(criteriaQuery);
            List<PersonEntity> persons = query.getResultList();
        }
        entityTransaction.commit();

    }

    @Test
    public void testCarsWithoutPerson() throws Exception {
        EntityTransaction entityTransaction = this.entityManager
                .getTransaction();

        entityTransaction.begin();
        {
            CarEntity myCar = new CarEntity("My Car");
            this.entityManager.persist(myCar);

            CarEntity nobodyCar = new CarEntity("Nobody Car");
            this.entityManager.persist(nobodyCar);

            CarEntity somebodyElseCar = new CarEntity("Somebody Else Car");
            this.entityManager.persist(somebodyElseCar);

            PersonEntity me = new PersonEntity("Me");
            me.setCars(new LinkedList<>());
            me.getCars().add(myCar);
            Address myAddress = new Address();
            myAddress.setZip("1234");
            me.setAddress(myAddress);
            this.entityManager.persist(me);

            PersonEntity somebodyElse = new PersonEntity("Somebody Else");
            somebodyElse.setCars(new LinkedList<>());
            somebodyElse.getCars().add(somebodyElseCar);
            Address somebodyElseAddress = new Address();
            somebodyElseAddress.setZip("1234");
            somebodyElse.setAddress(somebodyElseAddress);
            this.entityManager.persist(somebodyElse);
        }
        entityTransaction.commit();

        entityTransaction.begin();
        {
            PersonEntity personEntity = this.entityManager.find(PersonEntity.class, "Me");
            Class<?> entityClass = CarEntity.class;
            Class<?> refClass = PersonEntity.class;
            CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
            CriteriaQuery criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root car = criteriaQuery.from(entityClass); // FROM CarEntity AS car
            criteriaQuery.select(car); // SELECT car

            Subquery subqueryCriteriaQuery = criteriaQuery.subquery(entityClass);
            Root person = subqueryCriteriaQuery.from(refClass); // FROM PersonEntity AS person
            Join cars2 = person.join("cars"); // JOIN person.cars AS car2
            subqueryCriteriaQuery.select(cars2); // SELECT DISTINCT car2
            subqueryCriteriaQuery.distinct(true);

            criteriaQuery.where(criteriaBuilder.not(car.in(subqueryCriteriaQuery))); // WHERE car NOT IN

            TypedQuery<CarEntity> query = this.entityManager.createQuery(criteriaQuery);
            List<CarEntity> cars = query.getResultList();
            LOGGER.debug("result list: {}", cars);

            //Query newEntityQuery = this.entityManager.createQuery("SELECT car FROM CarEntity AS car WHERE car NOT IN (SELECT DISTINCT car2 FROM PersonEntity AS person JOIN person.cars AS car2)");
            Query newEntityQuery = this.entityManager.createQuery("SELECT car FROM CarEntity AS car WHERE car NOT IN (SELECT DISTINCT car2 FROM PersonEntity AS person JOIN person.cars AS car2)");
            //newEntityQuery.setParameter("param", personEntity);
            LOGGER.debug("expected result list: {}", newEntityQuery.getResultList());
        }
        entityTransaction.commit();
    }

    @Test
    public void testMetadata() throws Exception {
        Metamodel metamodel = this.entityManager.getMetamodel();
        EntityType<PropertyAccessTypeEntity> entityType = metamodel.entity(PropertyAccessTypeEntity.class);
        LOGGER.debug("hasSingleIdAttribute: {} ", entityType.hasSingleIdAttribute());
        SingularAttribute idAttribute = entityType.getId(entityType.getIdType().getJavaType());
        LOGGER.debug("id attribute: {}", idAttribute.getName());
        LOGGER.debug("id attribute type: {}", idAttribute.getType().getJavaType().getName());

        PropertyAccessTypeEntity entity = new PropertyAccessTypeEntity();
        String name = "Alice";

        Member idMember = idAttribute.getJavaMember();
        LOGGER.debug("id member: {}", idMember);

    }

    @Test
    public void testMetadata2() throws Exception {
        Metamodel metamodel = this.entityManager.getMetamodel();
        EntityType<AutoIdEntity> entityType = metamodel.entity(AutoIdEntity.class);
        LOGGER.debug("hasSingleIdAttribute: {} ", entityType.hasSingleIdAttribute());
        SingularAttribute idAttribute = entityType.getId(entityType.getIdType().getJavaType());
        LOGGER.debug("id attribute: {}", idAttribute.getName());
        LOGGER.debug("id attribute type: {}", idAttribute.getType().getJavaType().getName());

        Member idMember = idAttribute.getJavaMember();
        LOGGER.debug("id member: {}", idMember);

        Set<Attribute<? super AutoIdEntity, ?>> attributes = entityType.getAttributes();
        for (Attribute<? super AutoIdEntity, ?> attribute : attributes) {
            LOGGER.debug("attribute: {}", attribute.getName());
            LOGGER.debug("persistent attribute type: {}", attribute.getPersistentAttributeType());
            LOGGER.debug("attribute class: {}", attribute.getClass().getName());
            if (attribute instanceof SingularAttribute) {
                SingularAttribute singularAttribute = (SingularAttribute) attribute;
                LOGGER.debug("optional: {}", singularAttribute.isOptional());
            }
        }
    }

    @Test
    public void testEmbeddableAnnotation() throws Exception {
        Metamodel metamodel = this.entityManager.getMetamodel();
        EntityType<PersonEntity> entityType = metamodel.entity(PersonEntity.class);
        Set<Attribute<? super PersonEntity, ?>> attributes = entityType.getAttributes();
        for (Attribute<? super PersonEntity, ?> attribute : attributes) {
            LOGGER.debug("attribute: {}", attribute.getName());
            LOGGER.debug("persistent attribute type: {}", attribute.getPersistentAttributeType());
            if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
                LOGGER.debug("attribute class: {}", attribute.getClass().getName());
                SingularAttribute singularAttribute = (SingularAttribute) attribute;
                LOGGER.debug("declaring type: {}", singularAttribute.getDeclaringType());
                LOGGER.debug("java type: {}", singularAttribute.getJavaType());
                EmbeddableType embeddableType = metamodel.embeddable(singularAttribute.getJavaType());
                for (Object embeddableAttributeObject : embeddableType.getAttributes()) {
                    Attribute embeddableAttribute = (Attribute) embeddableAttributeObject;
                    LOGGER.debug("embeddable attribute: {}", embeddableAttribute.getName());
                }
            }
        }
        EntityInspector entityInspector = new EntityInspector(this.entityManager, PersonEntity.class);
        Field nameField = PersonEntity.class.getDeclaredField("name");
        Id idAnnotation = entityInspector.getAnnotation(nameField, Id.class);
        assertNotNull(idAnnotation);
        Field addressField = PersonEntity.class.getDeclaredField("address");
        Field zipField = Address.class.getDeclaredField("zip");
        Basic basicAnnotation = entityInspector.getAnnotation(addressField, zipField, Basic.class);
        assertNotNull(basicAnnotation);
    }

    @Test
    public void testEmbeddedId() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.entityManager, MessageEntity.class);
        Field idField = entityInspector.getIdFields().iterator().next();
        LOGGER.debug("id Field: {}", idField.getName());
        List<Field> otherFields = entityInspector.getOtherFields();
        for (Field otherField : otherFields) {
            LOGGER.debug("other field: {}", otherField.getName());
        }
        List<Field> embeddedFields = entityInspector.getEmbeddedFields();
        for (Field embeddedField : embeddedFields) {
            LOGGER.debug("embedded field: {}", embeddedField.getName());
        }
    }

    @Test
    public void testGetIdField() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.entityManager, AutoIdEntity.class.getName() + ".class");
        Field result = entityInspector.getIdFields().iterator().next();
        assertEquals("id", result.getName());
    }

    @Test
    public void testIdClass() throws Exception {
        EntityTransaction entityTransaction = this.entityManager
                .getTransaction();

        DemoEntity demoEntity;
        entityTransaction.begin();
        {
            demoEntity = new DemoEntity("name", "description");
            this.entityManager.persist(demoEntity);
        }
        entityTransaction.commit();

        entityTransaction.begin();
        {
            IdClassEntity idClassEntity = new IdClassEntity();
            idClassEntity.setName("name");
            idClassEntity.setDemo(demoEntity);
            this.entityManager.persist(idClassEntity);
        }
        entityTransaction.commit();

        EntityInspector entityInspector = new EntityInspector(this.entityManager, IdClassEntity.class.getSimpleName());
        List<Field> idFields = entityInspector.getIdFields();
        for (Field idField : idFields) {
            LOGGER.debug("id field: {}", idField);
            ManyToOne manyToOneAnnotation = entityInspector.getAnnotation(idField, ManyToOne.class);
            LOGGER.debug("has ManyToOne annotation: {}", null != manyToOneAnnotation);
        }
    }
}
