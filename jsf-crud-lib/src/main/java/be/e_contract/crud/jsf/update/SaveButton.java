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
package be.e_contract.crud.jsf.update;

import javax.el.MethodExpression;
import javax.faces.application.Application;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlForm;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(SaveButton.COMPONENT_TYPE)
@ResourceDependencies(value = {
    @ResourceDependency(library = "crud", name = "crud.js")
})
public class SaveButton extends UIComponentBase implements SystemEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveButton.class);

    public static final String COMPONENT_TYPE = "crud.saveButton";

    public enum PropertyKeys {
        action,
        value,
    }

    public SaveButton() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = facesContext.getViewRoot();
        viewRoot.subscribeToViewEvent(PostAddToViewEvent.class, this);
    }

    @Override
    public String getFamily() {
        return "crud";
    }

    public void setAction(MethodExpression methodExpression) {
        getStateHelper().put(PropertyKeys.action, methodExpression);
    }

    public MethodExpression getAction() {
        return (MethodExpression) getStateHelper().eval(PropertyKeys.action);
    }

    public void setValue(String value) {
        getStateHelper().put(PropertyKeys.value, value);
    }

    public String getValue() {
        return (String) getStateHelper().eval(PropertyKeys.value);
    }

    @Override
    public boolean isListenerForSource(Object source) {
        return (source instanceof UIViewRoot);
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();

        for (UIComponent child : getChildren()) {
            if (child instanceof CommandButton) {
                return;
            }
        }

        CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        getChildren().add(commandButton);
        commandButton.setId("saveButton");
        String commandButtonValue = getValue();
        if (UIInput.isEmpty(commandButtonValue)) {
            commandButtonValue = "Save";
        }
        commandButton.setValue(commandButtonValue);
        commandButton.addActionListener(new SaveActionListener(getAction()));
        Dialog dialog = getDialog();
        String dialogWidgetVar = dialog.getWidgetVar();
        commandButton.setOncomplete("crudDialogResponse(xhr, status, args, '" + dialogWidgetVar + "')");
        HtmlForm htmlForm = getHtmlForm();
        String htmlFormClientId = htmlForm.getClientId();
        commandButton.setUpdate(htmlFormClientId);
    }

    private Dialog getDialog() {
        UIComponent parent = getParent();
        while (parent != null) {
            if (parent instanceof Dialog) {
                return (Dialog) parent;
            }
            parent = parent.getParent();
        }
        throw new AbortProcessingException();
    }

    private HtmlForm getHtmlForm() {
        UIComponent parent = getParent();
        while (parent != null) {
            if (parent instanceof HtmlForm) {
                return (HtmlForm) parent;
            }
            parent = parent.getParent();
        }
        throw new AbortProcessingException();
    }
}
