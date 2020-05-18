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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.message.Message;
import org.primefaces.component.outputlabel.OutputLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(BeanComponent.COMPONENT_TYPE)
public class BeanComponent extends UINamingContainer implements SystemEventListener {

    public static final String COMPONENT_TYPE = "crud.bean";

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanComponent.class);

    public enum PropertyKeys {
        name,
    }

    public BeanComponent() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = facesContext.getViewRoot();
        viewRoot.subscribeToViewEvent(PostAddToViewEvent.class, this);
    }

    public void setName(String name) {
        LOGGER.debug("setName: {}", name);
        getStateHelper().put(PropertyKeys.name, name);
    }

    public String getName() {
        return (String) getStateHelper().eval(PropertyKeys.name);
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {

        for (UIComponent child : getChildren()) {
            if (child instanceof HtmlForm) {
                // already initialized
                return;
            }
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        ValueExpression beanValueExpression = expressionFactory.createValueExpression(elContext, "#{" + getName() + "}", Object.class);
        Object beanObject = beanValueExpression.getValue(elContext);
        if (null == beanObject) {
            LOGGER.error("no bean object");
            return;
        }

        HtmlForm htmlForm = (HtmlForm) application.createComponent(HtmlForm.COMPONENT_TYPE);
        getChildren().add(htmlForm);

        HtmlPanelGrid htmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        htmlForm.getChildren().add(htmlPanelGrid);
        htmlPanelGrid.setColumns(3);

        Class<?> beanClass = beanObject.getClass();
        Method[] methods = beanClass.getDeclaredMethods();
        List<Method> actionMethods = new LinkedList<>();
        for (Method method : methods) {
            LOGGER.debug("method: {}", method);
            if (method.getName().startsWith("set")) {
                String property = method.getName().substring(3);
                OutputLabel outputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(outputLabel);
                outputLabel.setValue(property);
                outputLabel.setFor(property);

                InputText inputText = (InputText) application.createComponent(InputText.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(inputText);
                inputText.setId(property);
                ValueExpression valueExpression = expressionFactory.createValueExpression(elContext, "#{" + getName() + "." + property.substring(0, 1).toLowerCase() + property.substring(1) + "}", method.getParameters()[0].getType());
                inputText.setValueExpression("value", valueExpression);

                Message message = (Message) application.createComponent(Message.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(message);
                message.setFor(property);
            } else if (method.getName().startsWith("get")) {
                continue;
            } else {
                actionMethods.add(method);
            }
        }

        HtmlPanelGrid actionPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        htmlForm.getChildren().add(actionPanelGrid);
        actionPanelGrid.setColumns(actionMethods.size());

        for (Method actionMethod : actionMethods) {
            CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
            actionPanelGrid.getChildren().add(commandButton);
            commandButton.setValue(actionMethod.getName());
            commandButton.setId(actionMethod.getName());
            MethodExpression methodExpression = expressionFactory.createMethodExpression(elContext, "#{" + getName() + "." + actionMethod.getName() + "}", void.class, new Class[]{});
            commandButton.setActionExpression(methodExpression);
            commandButton.setUpdate(htmlForm.getClientId());
        }
    }

    @Override
    public boolean isListenerForSource(Object source) {
        return (source instanceof UIViewRoot);
    }
}
