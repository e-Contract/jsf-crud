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
package be.e_contract.crud.jsf.api;

import javax.faces.component.UIComponent;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;

public abstract class AbstractEntityEvent<T extends EntityListener> extends FacesEvent {

    private final Object entity;

    private final Class<T> listenerClass;

    public AbstractEntityEvent(UIComponent component, Object entity, Class<T> listenerClass) {
        super(component);
        this.entity = entity;
        this.listenerClass = listenerClass;
    }

    public Object getEntity() {
        return this.entity;
    }

    @Override
    public final boolean isAppropriateListener(FacesListener listener) {
        return this.listenerClass.isAssignableFrom(listener.getClass());
    }

    @Override
    public final void processListener(FacesListener listener) {
        T entityListener = (T) listener;
        processEntityListener(entityListener);
    }

    protected abstract void processEntityListener(T entityListener);
}
