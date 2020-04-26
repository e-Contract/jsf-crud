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
package be.e_contract.crud.jsf.el;

import java.beans.FeatureDescriptor;
import java.lang.reflect.Field;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used for fields that do not have a corresponding bean property. Notice this
 * might not be a smart move from a security point of view.
 *
 * @author Frank Cornelis
 */
public class EntityELResolver extends ELResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityELResolver.class);

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        LOGGER.debug("getValue: {} {}", base, property);
        if (null == base) {
            return null;
        }
        if (null == property) {
            return null;
        }
        Class<?> baseClass = base.getClass();
        Entity entityAnnotation = baseClass.getAnnotation(Entity.class);
        if (null == entityAnnotation) {
            return null;
        }
        Field[] fields = baseClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().equals(property)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(base);
                    LOGGER.debug("could resolve: {}.{}", base, property);
                    context.setPropertyResolved(true);
                    return value;
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    LOGGER.error("reflection error: " + ex, ex);
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        LOGGER.debug("getType: {} {}", base, property);
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        LOGGER.debug("setValue: {} {} = {}", base, property, value);
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        LOGGER.debug("isReadOnly: {} {}", base, property);
        return true;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        LOGGER.debug("getFeatureDescriptors: {}", base);
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        LOGGER.debug("getCommonPropertyType: {}", base);
        return null;
    }
}
