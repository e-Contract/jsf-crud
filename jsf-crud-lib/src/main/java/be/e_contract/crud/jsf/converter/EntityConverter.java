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
package be.e_contract.crud.jsf.converter;

import be.e_contract.crud.jsf.jpa.CRUDController;
import be.e_contract.crud.jsf.jpa.EntityInspector;
import java.util.HashMap;
import java.util.Map;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityConverter implements Converter, StateHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityConverter.class);

    private String entityClassName;

    private boolean _transient;

    public EntityConverter() {
        super();
        LOGGER.debug("default constructor");
    }

    public EntityConverter(String entityClassName) {
        this.entityClassName = entityClassName;
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        return new Object[]{this.entityClassName};
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }
        this.entityClassName = (String) ((Object[]) state)[0];
    }

    @Override
    public boolean isTransient() {
        return this._transient;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        this._transient = newTransientValue;
    }

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        if (null == value) {
            return null;
        }
        if (value.isEmpty()) {
            return null;
        }
        LOGGER.debug("getAsObject: {}", value);
        Map<String, Object> viewMap = getViewMap(facesContext);
        Object entity = viewMap.get(value);
        return entity;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object object) {
        if (null == object) {
            return null;
        }
        LOGGER.debug("getAsString: entity class name {}", this.entityClassName);
        EntityInspector entityInspector = new EntityInspector(CRUDController.getMetamodel(), this.entityClassName);
        Object identifier = entityInspector.getIdentifier(object);
        String identifierString = identifier.toString();
        Map<String, Object> viewMap = getViewMap(facesContext);
        viewMap.put(identifierString, object);
        return identifierString;
    }

    private Map<String, Object> getViewMap(FacesContext facesContext) {
        Map<String, Object> viewMap = facesContext.getViewRoot().getViewMap();
        String key = EntityConverter.class.getName();
        Map<String, Object> entityConverterViewMap = (Map) viewMap.get(key);
        if (entityConverterViewMap == null) {
            entityConverterViewMap = new HashMap<>();
            viewMap.put(key, entityConverterViewMap);
        }
        return entityConverterViewMap;
    }
}
