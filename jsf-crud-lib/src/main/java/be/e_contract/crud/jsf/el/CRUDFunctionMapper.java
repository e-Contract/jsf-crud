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

import java.lang.reflect.Method;
import javax.el.FunctionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CRUDFunctionMapper extends FunctionMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CRUDFunctionMapper.class);

    private final FunctionMapper original;

    public CRUDFunctionMapper(FunctionMapper original) {
        this.original = original;
    }

    @Override
    public Method resolveFunction(String prefix, String localName) {
        LOGGER.debug("resolveFunction: {}:{}", prefix, localName);
        if ("crud".equals(prefix) && "toHumanReadable".equals(localName)) {
            Method toHumanReadableMethod;
            try {
                toHumanReadableMethod = CRUDFunctions.class.getMethod("toHumanReadable", new Class[]{Object.class});
            } catch (NoSuchMethodException | SecurityException ex) {
                LOGGER.error("reflection error: " + ex.getMessage(), ex);
                return this.original.resolveFunction(prefix, localName);
            }
            return toHumanReadableMethod;
        }
        return this.original.resolveFunction(prefix, localName);
    }
}
