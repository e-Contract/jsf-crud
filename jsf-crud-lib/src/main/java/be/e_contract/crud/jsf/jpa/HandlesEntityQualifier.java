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
package be.e_contract.crud.jsf.jpa;

import be.e_contract.crud.jsf.api.cdi.HandlesEntity;
import javax.enterprise.util.AnnotationLiteral;

public class HandlesEntityQualifier extends AnnotationLiteral<HandlesEntity> implements
        HandlesEntity {

    private final Class<?> entityClass;

    public HandlesEntityQualifier(Object entity) {
        this.entityClass = entity.getClass();
    }

    @Override
    public Class<?> value() {
        return this.entityClass;
    }
}
