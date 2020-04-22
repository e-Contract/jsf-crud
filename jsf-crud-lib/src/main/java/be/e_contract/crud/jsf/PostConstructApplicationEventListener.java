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

import java.io.InputStream;
import java.util.Properties;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostConstructApplicationEventListener implements SystemEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostConstructApplicationEventListener.class);

    @Override
    public void processEvent(SystemEvent se) throws AbortProcessingException {
        try (InputStream inputStream = PostConstructApplicationEventListener.class.getResourceAsStream("/META-INF/maven/be.e-contract.jsf-crud/jsf-crud-lib/pom.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String version = properties.getProperty("version");
            LOGGER.info("Running on JSF-CRUD version {}", version);
        } catch (Exception ex) {
            LOGGER.error("error retrieving version: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean isListenerForSource(Object o) {
        return true;
    }
}
