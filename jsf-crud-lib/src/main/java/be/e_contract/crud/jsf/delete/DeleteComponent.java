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
package be.e_contract.crud.jsf.delete;

import java.io.IOException;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

@FacesComponent(DeleteComponent.COMPONENT_TYPE)
public class DeleteComponent extends UIComponentBase {

    public static final String COMPONENT_TYPE = "crud.delete";

    public enum PropertyKeys {
        disabled,
        deleteAll,
        title,
        icon,
        value,
        tooltip,
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public boolean isDisabled() {
        Boolean disabled = (Boolean) getStateHelper().get(PropertyKeys.disabled);
        if (null == disabled) {
            return false;
        }
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        getStateHelper().put(PropertyKeys.disabled, disabled);
    }

    public boolean isDeleteAll() {
        Boolean deleteAll = (Boolean) getStateHelper().get(PropertyKeys.deleteAll);
        if (null == deleteAll) {
            return false;
        }
        return deleteAll;
    }

    public void setDeleteAll(boolean deleteAll) {
        getStateHelper().put(PropertyKeys.deleteAll, deleteAll);
    }

    public void setTitle(String title) {
        getStateHelper().put(PropertyKeys.title, title);
    }

    public String getTitle() {
        return (String) getStateHelper().eval(PropertyKeys.title);
    }

    public String getIcon() {
        return (String) getStateHelper().eval(PropertyKeys.icon, null);
    }

    public void setIcon(String icon) {
        getStateHelper().put(PropertyKeys.icon, icon);
    }

    public void setValue(String value) {
        getStateHelper().put(PropertyKeys.value, value);
    }

    public String getValue() {
        return (String) getStateHelper().eval(PropertyKeys.value);
    }

    public String getTooltip() {
        return (String) getStateHelper().eval(PropertyKeys.tooltip, null);
    }

    public void setTooltip(String tooltip) {
        getStateHelper().put(PropertyKeys.tooltip, tooltip);
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        // empty
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
