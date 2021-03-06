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
import javax.faces.view.facelets.MetaRule;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.TagAttribute;
import org.primefaces.facelets.MethodRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionTagHandler extends ComponentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionTagHandler.class);

    public ActionTagHandler(ComponentConfig config) {
        super(config);

    }

    @Override
    public void onComponentCreated(FaceletContext faceletContext, UIComponent component, UIComponent parent) {
        super.onComponentCreated(faceletContext, component, parent);
        TagAttribute renderedTagAttribute = getTagAttribute("rendered");
        if (null != renderedTagAttribute) {
            String renderedValue = renderedTagAttribute.getValue();
            LOGGER.debug("rendered: {}", renderedValue);
            ActionComponent actionComponent = (ActionComponent) component;
            FacesContext facesContext = faceletContext.getFacesContext();
            ELContext elContext = facesContext.getELContext();
            ValueExpression valueExpression = faceletContext.getExpressionFactory().createValueExpression(elContext, renderedValue, Boolean.class);
            actionComponent.setRenderedValueExpression(valueExpression);
        }
    }

    @Override
    protected MetaRuleset createMetaRuleset(Class type) {
        MetaRuleset metaRuleset = super.createMetaRuleset(type);
        MetaRule metaRule = new MethodRule("action", void.class, new Class[]{Object.class});
        metaRuleset.addRule(metaRule);
        return metaRuleset;
    }
}
