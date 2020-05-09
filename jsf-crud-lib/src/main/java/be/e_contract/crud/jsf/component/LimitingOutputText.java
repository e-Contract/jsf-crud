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
package be.e_contract.crud.jsf.component;

import be.e_contract.crud.jsf.jpa.CRUDController;
import be.e_contract.crud.jsf.jpa.EntityInspector;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIOutput;
import javax.persistence.Entity;
import javax.persistence.EntityManager;

@FacesComponent(LimitingOutputText.COMPONENT_TYPE)
public class LimitingOutputText extends UIOutput {

    public static final String COMPONENT_TYPE = "crud.limitingOutputText";

    public enum PropertyKeys {
        password,
    }

    public LimitingOutputText() {
        setRendererType(LimitingOutputRenderer.RENDERER_TYPE);
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public boolean isPassword() {
        return (Boolean) getStateHelper().eval(PropertyKeys.password, false);
    }

    public void setPassword(boolean password) {
        getStateHelper().put(PropertyKeys.password, password);
    }

    String getLimitedValue() {
        Object value = getValue();
        if (null == value) {
            return "";
        }
        Entity entityAnnotation = value.getClass().getAnnotation(Entity.class);
        if (null != entityAnnotation) {
            CRUDController crudController = CRUDController.getCRUDController();
            EntityManager entityManager = crudController.getEntityManager();
            EntityInspector entityInspector = new EntityInspector(entityManager, value.getClass());
            return entityInspector.toHumanReadable(value);
        }
        if (value instanceof Calendar) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = (Calendar) value;
            return format.format(calendar.getTime());
        }
        if (value instanceof List) {
            // avoid lazy loading issue
            return "...";
        }
        if (value instanceof byte[]) {
            return "[binary data]";
        }
        if (isPassword()) {
            return "...";
        }
        String strValue = value.toString();
        if (strValue.length() > 40) {
            return strValue.substring(0, 40) + "...";
        }
        return strValue;
    }
}
