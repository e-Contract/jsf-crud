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

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

public class CRUDELContext extends ELContext {

    private final ELContext parent;

    public CRUDELContext(ELContext parent) {
        this.parent = parent;
    }

    @Override
    public ELResolver getELResolver() {
        return this.parent.getELResolver();
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return new CRUDFunctionMapper(this.parent.getFunctionMapper());
    }

    @Override
    public VariableMapper getVariableMapper() {
        return this.parent.getVariableMapper();
    }
}
