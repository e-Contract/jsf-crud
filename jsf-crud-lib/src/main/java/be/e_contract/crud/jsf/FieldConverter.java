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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.persistence.Entity;

public class FieldConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        // this converter is only meant to be used as output converter
        return value;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (null == value) {
            return null;
        }
        Entity entityAnnotation = value.getClass().getAnnotation(Entity.class);
        if (null != entityAnnotation) {
            EntityInspector entityInspector = new EntityInspector(value.getClass());
            return entityInspector.toHumanReadable(value);
        }
        if (value instanceof Calendar) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            Calendar calendar = (Calendar) value;
            return format.format(calendar.getTime());
        }
        if (value instanceof List) {
            // avoid lazy loading issue
            return null;
        }
        return value.toString();
    }
}
