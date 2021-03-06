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
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.dialog.Dialog;

@FacesComponent(DismissButton.COMPONENT_TYPE)
public class DismissButton extends CommandButton {

    public static final String COMPONENT_TYPE = "crud.dismissButton";

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        if (UIInput.isEmpty(getValue())) {
            setValue("Dismiss");
        }
        String dialogWidgetVar = findDialogWidgetVar();
        if (null != dialogWidgetVar) {
            setOncomplete("PF('" + dialogWidgetVar + "').hide()");
        }
        if (UIInput.isEmpty(getIcon())) {
            ExternalContext externalContext = context.getExternalContext();
            String iconInitParam = externalContext.getInitParameter("crud.dialog.dismissButton.icon");
            setIcon(iconInitParam);
        }
        super.encodeBegin(context);
    }

    private String findDialogWidgetVar() {
        UIComponent parent = getParent();
        while (parent != null) {
            if (parent instanceof Dialog) {
                Dialog dialog = (Dialog) parent;
                return dialog.getWidgetVar();
            }
            parent = parent.getParent();
        }
        return null;
    }
}
