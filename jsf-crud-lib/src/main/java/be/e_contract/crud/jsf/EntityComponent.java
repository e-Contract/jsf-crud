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

import java.io.IOException;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.component.ContextCallback;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(EntityComponent.COMPONENT_TYPE)
public class EntityComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.entity";

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityComponent.class);

    public enum PropertyKeys {
        var,
        entity,
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public void setVar(String var) {
        LOGGER.debug("setVar: {}", var);
        getStateHelper().put(PropertyKeys.var, var);
    }

    public String getVar() {
        return (String) getStateHelper().eval(PropertyKeys.var);
    }

    public void setEntity(Object entity) {
        LOGGER.debug("setEntity: {}", entity);
        getStateHelper().put(PropertyKeys.entity, entity);
    }

    public Object getEntity() {
        return getStateHelper().eval(PropertyKeys.entity);
    }

    private void setLocalVariable() {
        String var = getVar();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, Object> requestMap
                = facesContext.getExternalContext().getRequestMap();
        Object entity = getEntity();
        LOGGER.debug("var: {} = {}", var, entity);
        requestMap.put(var, entity);
    }

    private void removeLocalVariable() {
        String var = getVar();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, Object> requestMap
                = facesContext.getExternalContext().getRequestMap();
        requestMap.remove(var);
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        LOGGER.debug("encodeBegin");
        setLocalVariable();
        super.encodeBegin(context);
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        LOGGER.debug("broadcast: {}", event);
        super.broadcast(event);
    }

    @Override
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback) throws FacesException {
        LOGGER.debug("invokeOnComponent");
        boolean result = super.invokeOnComponent(context, clientId, callback);
        return result;
    }

    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback) {
        LOGGER.debug("visitTree");
        boolean result = super.visitTree(context, callback);
        return result;
    }

    @Override
    public void processUpdates(FacesContext context) {
        LOGGER.debug("processUpdates");
        setLocalVariable();
        super.processUpdates(context);
    }
}
