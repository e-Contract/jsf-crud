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
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

@FacesRenderer(componentFamily = "crud", rendererType = LimitingOutputRenderer.RENDERER_TYPE)
public class LimitingOutputRenderer extends Renderer {

    public static final String RENDERER_TYPE = "crud.limitingOutputTextRenderer";

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        LimitingOutputText limitingOutputText = (LimitingOutputText) component;
        String limitedValue = limitingOutputText.getLimitedValue();
        ResponseWriter responseWriter = context.getResponseWriter();

        String id = component.getClientId(context);
        responseWriter.startElement("span", component);
        responseWriter.writeAttribute("id", id, "id");
        responseWriter.writeText(limitedValue, "value");
        responseWriter.endElement("span");
    }
}
