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
import java.util.Map;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(EntityComponent.COMPONENT_TYPE)
public class EntityComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.entity";

    public static final String DEFAULT_RENDERER = "crud.entityRenderer";

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
        return CRUDComponent.getCRUDComponent(getCrudComponentId());
    }

    Object setLocalVariable() {
        String var = getVar();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();
        Object entity = getEntity();
        LOGGER.debug("setting variable: {} = {}", var, entity);
        Object oldVar = requestMap.get(var);
        requestMap.put(var, entity);
        return oldVar;
    }

    void removeLocalVariable(Object oldVar) {
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

    public static EntityComponent getParentEntityComponent(UIComponent component) {
        while (component != null) {
            if (component instanceof EntityComponent) {
                EntityComponent entityComponent = (EntityComponent) component;
                return entityComponent;
            }
            component = component.getParent();
        }
        throw new AbortProcessingException();
    }
}
