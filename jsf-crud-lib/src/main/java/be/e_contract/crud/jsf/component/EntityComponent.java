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

import be.e_contract.crud.jsf.CRUDComponent;
import be.e_contract.crud.jsf.jpa.CRUDController;
import be.e_contract.crud.jsf.jpa.EntityInspector;
import java.io.IOException;
import java.util.Map;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(EntityComponent.COMPONENT_TYPE)
public class EntityComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.entity";

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityComponent.class);

    public enum PropertyKeys {
        var,
        entity,
        crudComponentId,
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public void setVar(String var) {
        getStateHelper().put(PropertyKeys.var, var);
    }

    public String getVar() {
        return (String) getStateHelper().eval(PropertyKeys.var);
    }

    public void setEntity(Object entity, String crudComponentId) {
        getStateHelper().put(PropertyKeys.entity, entity);
        getStateHelper().put(PropertyKeys.crudComponentId, crudComponentId);
    }

    public Object getEntity() {
        return getStateHelper().eval(PropertyKeys.entity);
    }

    String getCrudComponentId() {
        return (String) getStateHelper().eval(PropertyKeys.crudComponentId);
    }

    public CRUDComponent getCRUDComponent() {
        FacesContext facesContext = getFacesContext();
        UIViewRoot view = facesContext.getViewRoot();
        String crudComponentId = getCrudComponentId();
        UIComponent component = view.findComponent(crudComponentId);
        if (null == component) {
            return null;
        }
        return (CRUDComponent) component;
    }

    private Object setLocalVariable() {
        String var = getVar();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();
        Object entity = getEntity();
        if (null != entity) {
            EntityInspector entityInspector = new EntityInspector(CRUDController.getMetamodel(), entity);
            LOGGER.debug("setting variable: {} = {}", var, entityInspector.toHumanReadable(entity));
        }
        Object oldVar = requestMap.get(var);
        requestMap.put(var, entity);
        return oldVar;
    }

    private void removeLocalVariable(Object oldVar) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();
        String var = getVar();
        requestMap.remove(var);
        if (null != oldVar) {
            requestMap.put(var, oldVar);
        }
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        LOGGER.debug("encodeChildren begin");
        Object oldVar = setLocalVariable();
        super.encodeChildren(context);
        removeLocalVariable(oldVar);
        LOGGER.debug("encodeChildren end");
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public void processUpdates(FacesContext context) {
        LOGGER.debug("processUpdates begin");
        Object oldVar = setLocalVariable();
        super.processUpdates(context);
        // without removeLocalVariable it works
        //removeLocalVariable(oldVar);
        LOGGER.debug("processUpdates end");
    }

    @Override
    public void processValidators(FacesContext context) {
        LOGGER.debug("processValidators begin");
        Object oldVar = setLocalVariable();
        super.processValidators(context);
        removeLocalVariable(oldVar);
        LOGGER.debug("processValidators end");
    }
}
