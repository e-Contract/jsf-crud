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

import java.io.IOException;
import javax.el.ValueExpression;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;

public class ResetTagHandler extends TagHandler {

    private final TagAttribute target;

    public ResetTagHandler(TagConfig config) {
        super(config);
        this.target = getRequiredAttribute("target");
    }

    @Override
    public void apply(FaceletContext faceletContext, UIComponent parent) throws IOException {
        if (parent == null) {
            return;
        }
        if (!ComponentHandler.isNew(parent)) {
            return;
        }
        if (!(parent instanceof ActionSource)) {
            throw new TagException(this.tag, "Reset can only be attached to ActionSource components.");
        }

        ValueExpression targetValueExpression = this.target.getValueExpression(faceletContext, Object.class);

        ActionSource actionSource = (ActionSource) parent;
        actionSource.addActionListener(new ResetActionListener(targetValueExpression));
    }
}
