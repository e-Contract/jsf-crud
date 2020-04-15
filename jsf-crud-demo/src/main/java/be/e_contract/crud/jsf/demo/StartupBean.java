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
package be.e_contract.crud.jsf.demo;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class StartupBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupBean.class);

    @PersistenceContext
    private EntityManager entityManager;

    @PostConstruct
    private void startUp() {
        LOGGER.info("startup");
        DemoEntity demoEntity = new DemoEntity("Item 1", "Description for item 1.");
        this.entityManager.persist(demoEntity);

        DemoEntity demoEntity2 = new DemoEntity("Item 2", "Description for item 2.");
        this.entityManager.persist(demoEntity2);
    }
}
