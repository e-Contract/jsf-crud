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

import java.util.ArrayList;
import java.util.List;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(ContainerComponent.COMPONENT_TYPE)
public class ContainerComponent extends UIComponentBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerComponent.class);

    public static final String COMPONENT_TYPE = "crud.container";

    private List<UIComponent> children = null;

    @Override
    public String getFamily() {
        return "crud";
    }

    @Override
    public int getChildCount() {
        if (this.children == null) {
            return 0;
        }
        return this.children.size();
    }

    @Override
    public List<UIComponent> getChildren() {
        LOGGER.debug("getChildren");
        // this works, for some reason...
        if (null == this.children) {
            this.children = new ArrayList<>();
        }
        return this.children;
    }
}
