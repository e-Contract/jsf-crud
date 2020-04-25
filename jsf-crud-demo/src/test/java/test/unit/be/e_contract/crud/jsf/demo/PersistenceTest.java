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

import be.e_contract.crud.jsf.demo.CarEntity;
import be.e_contract.crud.jsf.demo.PersonEntity;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceTest.class);

    private EntityManager entityManager;

    @Before
    public void setUp() throws Exception {
        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("test");
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @After
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
            CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
            CriteriaQuery<CarEntity> criteriaQuery = criteriaBuilder.createQuery(CarEntity.class);
            Root<CarEntity> car = criteriaQuery.from(CarEntity.class);
            criteriaQuery.select(car);

            Metamodel metamodel = this.entityManager.getMetamodel();
            EntityType<CarEntity> entityType = metamodel.entity(CarEntity.class);
            LOGGER.debug("car attributes: {}", entityType.getAttributes());

            TypedQuery<CarEntity> query = this.entityManager.createQuery(criteriaQuery);
            List<CarEntity> cars = query.getResultList();

            Query classicQuery = this.entityManager.createQuery("SELECT car FROM CarEntity AS car WHERE car NOT IN (SELECT DISTINCT car2 FROM PersonEntity AS person JOIN person.cars AS car2)");
            //Query classicQuery = this.entityManager.createQuery("SELECT DISTINCT person.cars FROM PersonEntity AS person");

            classicQuery.getResultList();
        }
        entityTransaction.commit();

    }
}
