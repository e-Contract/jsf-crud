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

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.FacesComponent;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.persistence.EntityManager;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.dialog.Dialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(SaveButton.COMPONENT_TYPE)
public class SaveButton extends UIComponentBase implements SystemEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaveButton.class);

    public static final String COMPONENT_TYPE = "crud.saveButton";

    public enum PropertyKeys {
        action,
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

    @Override
    public boolean isListenerForSource(Object source) {
        return (source instanceof UIViewRoot);
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();

        CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        getChildren().add(commandButton);
        commandButton.setId("saveButton");
        commandButton.setValue("Save");
        commandButton.addActionListener(new SaveActionListener(getAction()));
        String dialogWidgetVar = getDialogWidgetVar();
        commandButton.setOncomplete("PF('" + dialogWidgetVar + "').hide()");
    }

    private String getDialogWidgetVar() {
        UIComponent parent = getParent();
        while (parent != null) {
            if (parent instanceof Dialog) {
                Dialog dialog = (Dialog) parent;
                return dialog.getWidgetVar();
            }
            parent = parent.getParent();
        }
        throw new AbortProcessingException();
    }

    public class SaveActionListener implements ActionListener, StateHolder {

        private boolean _transient;

        private MethodExpression methodExpression;

        public SaveActionListener(MethodExpression methodExpression) {
            this.methodExpression = methodExpression;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            LOGGER.debug("processAction save");

            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext elContext = facesContext.getELContext();

            Object entity = getEntity();

            if (null != this.methodExpression) {
                this.methodExpression.invoke(elContext, new Object[]{entity});

            }

            Application application = facesContext.getApplication();
            ExpressionFactory expressionFactory = application.getExpressionFactory();
            ValueExpression valueExpression = expressionFactory.createValueExpression(elContext, "#{crudController}", CRUDController.class);
            CRUDController crudController = (CRUDController) valueExpression.getValue(elContext);
            EntityManager entityManager = crudController.getEntityManager();
            UserTransaction userTransaction = crudController.getUserTransaction();

            try {
                userTransaction.begin();
            } catch (NotSupportedException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return;
            }

            entityManager.merge(entity);

            try {
                userTransaction.commit();
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return;
            }
            //CRUDComponent.this.setSelection(null);

            //String entityHumanReadable = this.entityInspector.toHumanReadable(entity);
            //CRUDComponent.this.addMessage(FacesMessage.SEVERITY_INFO, "Updated " + entityHumanReadable);
            UpdateEvent updateEvent = new UpdateEvent(SaveButton.this, entity);
            updateEvent.queue();
            // we definitely need to be able to update CRUDComponent here...
            // maybe let CRUDComponent listen to UpdateEvent itself?
        }

        @Override
        public Object saveState(FacesContext context) {
            if (context == null) {
                throw new NullPointerException();
            }
            return new Object[]{this.methodExpression};
        }

        @Override
        public void restoreState(FacesContext context, Object state) {
            if (context == null) {
                throw new NullPointerException();
            }
            if (state == null) {
                return;
            }
            this.methodExpression = (MethodExpression) ((Object[]) state)[0];
        }

        @Override
        public boolean isTransient() {
            return this._transient;
        }

        @Override
        public void setTransient(boolean newTransientValue) {
            this._transient = newTransientValue;
        }

        private Object getEntity() {
            UIComponent component = getParent();
            while (component != null) {
                if (component instanceof EntityComponent) {
                    EntityComponent entityComponent = (EntityComponent) component;
                    return entityComponent.getEntity();
                }
                component = component.getParent();
            }
            throw new AbortProcessingException();
        }
    }
}