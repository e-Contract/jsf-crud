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
package be.e_contract.crud.jsf.action;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileDownloadTagHandler extends ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadTagHandler.class);

    public FileDownloadTagHandler(ComponentConfig config) {
        super(config);
    }

    @Override
    public void onComponentCreated(FaceletContext faceletContext, UIComponent component, UIComponent parent) {
        TagAttribute valueTagAttribute = getTagAttribute("value");
        String valueValue = valueTagAttribute.getValue();
        FileDownloadComponent fileDownloadComponent = (FileDownloadComponent) component;
        FacesContext facesContext = faceletContext.getFacesContext();
        ELContext elContext = facesContext.getELContext();
        ValueExpression valueExpression = faceletContext.getExpressionFactory().createValueExpression(elContext, valueValue, StreamedContent.class);
        fileDownloadComponent.setValue(valueExpression);
    }
}
