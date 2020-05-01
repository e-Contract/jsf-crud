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
import javax.persistence.metamodel.Metamodel;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class EntityInspectorTest {

    private EntityManager entityManager;

    private Metamodel metamodel;

    @Before
    public void setUp() throws Exception {
        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("test");
        this.entityManager = entityManagerFactory.createEntityManager();
        this.metamodel = this.entityManager.getMetamodel();
    }

    @After
    public void tearDown() throws Exception {
        this.entityManager.close();
    }

    @Test
    public void testGetEntityName() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.metamodel, MyEntity.class.getSimpleName());
        String result = entityInspector.getEntityName();
        assertEquals("My", result);
    }

    @Test
    public void testGetIdField() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.metamodel, MyEntity.class.getName() + ".class");
        Field result = entityInspector.getIdField();
        assertEquals("name", result.getName());
    }

    @Test
    public void testGetIdFieldPropertyAccessType() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.metamodel, PropertyAccessTypeEntity.class.getSimpleName());
        Field result = entityInspector.getIdField();
        assertEquals("name", result.getName());
    }

    @Test
    public void testToHumanReadable() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.metamodel, MyEntity.class.getName() + ".class");
        MyEntity entity = new MyEntity();
        entity.name = "frank";
        String result = entityInspector.toHumanReadable(entity);
        assertEquals("frank", result);

        EntityInspector entityInspector2 = new EntityInspector(this.metamodel, MyToStringEntity.class.getName() + ".class");
        MyToStringEntity entity2 = new MyToStringEntity();
        entity2.setName("frank");
        String result2 = entityInspector2.toHumanReadable(entity2);
        assertEquals("test: frank", result2);
    }

    @Test
    public void testFieldToHumanReadable() throws Exception {
        EntityInspector entityInspector = new EntityInspector(this.metamodel, MyEntity.class.getName() + ".class");
        Field name = MyToStringEntity.class.getDeclaredField("name");
        String nameHumanReadable = entityInspector.toHumanReadable(name);
        assertEquals("Name", nameHumanReadable);

        Field aFunnyField = MyToStringEntity.class.getDeclaredField("aFunnyField");
        String aFunnyFieldHumanReadable = entityInspector.toHumanReadable(aFunnyField);
        assertEquals("A Funny Field", aFunnyFieldHumanReadable);
    }
}
