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
package test.unit.be.e_contract.crud.jsf;

import be.e_contract.crud.jsf.jpa.EntityInspector;
import java.lang.reflect.Field;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityInspectorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityInspectorTest.class);

    private EntityManager entityManager;

    private Metamodel metamodel;

    @BeforeEach
    public void setUp() throws Exception {
        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("test");
        this.entityManager = entityManagerFactory.createEntityManager();
        this.metamodel = this.entityManager.getMetamodel();
    }

    @AfterEach
    public void tearDown() throws Exception {
        this.entityManager.close();
    }

    @Test
    public void testGetEntityName() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.entityManager, MyEntity.class.getSimpleName());
        String result = entityInspector.getEntityName();
        assertEquals("My", result);
    }

    @Test
    public void testGetIdField() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.entityManager, MyEntity.class.getName() + ".class");
        Field result = entityInspector.getIdFields().iterator().next();
        assertEquals("name", result.getName());
    }

    @Test
    public void testGetIdFieldPropertyAccessType() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.entityManager, PropertyAccessTypeEntity.class.getSimpleName());
        Field result = entityInspector.getIdFields().iterator().next();
        assertEquals("name", result.getName());

        EntityType entityType = this.metamodel.entity(PropertyAccessTypeEntity.class);
        SingularAttribute singularAttribute = entityType.getId(entityType.getIdType().getJavaType());
        LOGGER.debug("attribute: {}", singularAttribute);
        LOGGER.debug("attribute class: {}", singularAttribute.getClass().getName());
        assertTrue(entityInspector.isIdGeneratedValue());
    }

    @Test
    public void testToHumanReadable() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.entityManager, MyEntity.class.getName() + ".class");
        MyEntity entity = new MyEntity();
        entity.name = "frank";
        String result = entityInspector.toHumanReadable(entity);
        assertEquals("frank", result);

        EntityInspector entityInspector2 = new EntityInspector(this.entityManager, MyToStringEntity.class.getName() + ".class");
        MyToStringEntity entity2 = new MyToStringEntity();
        entity2.setName("frank");
        String result2 = entityInspector2.toHumanReadable(entity2);
        assertEquals("test: frank", result2);
    }

    @Test
    public void testFieldToHumanReadable() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.entityManager, MyEntity.class.getName() + ".class");
        Field name = MyToStringEntity.class.getDeclaredField("name");
        String nameHumanReadable = entityInspector.toHumanReadable(name);
        assertEquals("Name", nameHumanReadable);

        Field aFunnyField = MyToStringEntity.class.getDeclaredField("aFunnyField");
        String aFunnyFieldHumanReadable = entityInspector.toHumanReadable(aFunnyField);
        assertEquals("A Funny Field", aFunnyFieldHumanReadable);
    }
}
