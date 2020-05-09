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

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesRenderer(componentFamily = "crud", rendererType = EntityComponent.DEFAULT_RENDERER)
public class EntityRenderer extends Renderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityRenderer.class);

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        LOGGER.debug("encodeChildren begin");
        EntityComponent entityComponent = (EntityComponent) component;
        Object oldVar = entityComponent.setLocalVariable();
        super.encodeChildren(context, component);
        entityComponent.removeLocalVariable(oldVar);
        LOGGER.debug("encodeChildren end");
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
