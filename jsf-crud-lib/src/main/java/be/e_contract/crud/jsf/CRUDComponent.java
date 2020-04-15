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

import java.lang.reflect.Field;
import java.util.Date;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.faces.application.Application;
import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import org.primefaces.component.datatable.DataTable;
import java.util.List;
import javax.el.ELResolver;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UISelectItem;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.message.Message;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.component.resetinput.ResetInputActionListener;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.spacer.Spacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent("crud.crud")
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
@ResourceDependencies(value = {
    @ResourceDependency(library = "crud", name = "crud.js")
})
public class CRUDComponent extends UINamingContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CRUDComponent.class);

    public enum PropertyKeys {
        entity,
        selection,
        newEntity,
    }

    public void setEntity(String entity) {
        LOGGER.debug("setEntity: {}", entity);
        getStateHelper().put(PropertyKeys.entity, entity);
    }

    public String getEntity() {
        return (String) getStateHelper().eval(PropertyKeys.entity);
    }

    void setSelection(Object entity) {
        getStateHelper().put(PropertyKeys.selection, entity);
    }

    Object getSelection() {
        return getStateHelper().eval(PropertyKeys.selection);
    }

    Object getNewEntity() {
        return getStateHelper().eval(PropertyKeys.newEntity);
    }

    void setNewEntity(Object entity) {
        getStateHelper().put(PropertyKeys.newEntity, entity);
    }

    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        LOGGER.debug("processEvent: {}", event);
        if (!(event instanceof PostAddToViewEvent)) {
            return;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext.isValidationFailed()) {
            return;
        }

        boolean showCreate = true;
        boolean showDelete = true;
        boolean showUpdate = true;
        boolean showView = false;
        CreateComponent createComponent = null;
        DeleteComponent deleteComponent = null;
        UpdateComponent updateComponent = null;
        ReadComponent readComponent = null;
        List<UIComponent> children = getChildren();
        for (UIComponent child : children) {
            if (child instanceof CreateComponent) {
                createComponent = (CreateComponent) child;
                if (createComponent.isDisabled()) {
                    showCreate = false;
                }
            } else if (child instanceof DeleteComponent) {
                deleteComponent = (DeleteComponent) child;
                if (deleteComponent.isDisabled()) {
                    showDelete = false;
                }
            } else if (child instanceof UpdateComponent) {
                updateComponent = (UpdateComponent) child;
                if (updateComponent.isDisabled()) {
                    showUpdate = false;
                }
            } else if (child instanceof ReadComponent) {
                readComponent = (ReadComponent) child;
                showView = true;
            }
        }

        String entityClassName = getEntity();
        EntityInspector entityInspector = new EntityInspector(entityClassName);
        Class<?> entityClass;
        try {
            entityClass = Class.forName(entityClassName);
        } catch (ClassNotFoundException ex) {
            LOGGER.error("entity class not found: " + entityClassName);
            throw new AbortProcessingException("entity class not found: " + entityClassName);
        }

        String entityName = entityInspector.getEntityName();

        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();

        HtmlForm htmlForm = (HtmlForm) application.createComponent(HtmlForm.COMPONENT_TYPE);
        getChildren().add(htmlForm);
        htmlForm.setId("form");

        Message message = (Message) application.createComponent(Message.COMPONENT_TYPE);
        htmlForm.getChildren().add(message);
        message.setId("message");
        message.setFor("table");

        Spacer spacer = (Spacer) application.createComponent(Spacer.COMPONENT_TYPE);
        htmlForm.getChildren().add(spacer);
        spacer.setHeight("5px");

        DataTable dataTable = (DataTable) application.createComponent(DataTable.COMPONENT_TYPE);
        htmlForm.getChildren().add(dataTable);

        ValueExpression valueExpression = new EntityValueExpression(entityClass);
        dataTable.setValueExpression("value", valueExpression);
        dataTable.setVar("row");
        dataTable.setId("table");
        dataTable.setResizableColumns(true);
        dataTable.setTableStyle("table-layout: auto !important;");
        ELContext elContext = facesContext.getELContext();
        List entityList = (List) valueExpression.getValue(elContext);
        if (entityList.size() > 20) {
            dataTable.setPaginator(true);
            dataTable.setRows(20);
        }

        // first column is the @Id column
        Field idField = entityInspector.getIdField();
        addColumn(dataTable, idField, entityInspector);

        // next we add all the others
        for (Field entityField : entityInspector.getOtherFields()) {
            addColumn(dataTable, entityField, entityInspector);
        }

        if (showDelete || showUpdate || showView) {
            Column column = new Column();
            dataTable.getChildren().add(column);
            column.setHeaderText("Actions");

            if (showView) {
                CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
                column.getChildren().add(commandButton);
                commandButton.setValue("View...");
                commandButton.setOncomplete("PF('viewDialog').show()");
                commandButton.setId("viewButton");

                Dialog viewDialog = (Dialog) application.createComponent(Dialog.COMPONENT_TYPE);
                getChildren().add(viewDialog);
                viewDialog.setWidgetVar("viewDialog");
                viewDialog.setId("viewDialog");
                viewDialog.setHeader("View " + entityName);
                viewDialog.setModal(true);

                commandButton.setUpdate(viewDialog.getClientId() + "," + message.getClientId());
                commandButton.addActionListener(new SelectRowActionListener());

                HtmlPanelGrid htmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
                viewDialog.getChildren().add(htmlPanelGrid);
                htmlPanelGrid.setColumns(2);

                OutputLabel idOutputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(idOutputLabel);
                idOutputLabel.setValue(entityInspector.toHumanReadable(idField));

                HtmlOutputText identifierOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(identifierOutputText);
                identifierOutputText.setValueExpression("value", new EntityFieldValueExpression(this, idField, false));

                for (Field entityField : entityInspector.getOtherFields()) {
                    OutputLabel outputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
                    htmlPanelGrid.getChildren().add(outputLabel);
                    outputLabel.setValue(entityInspector.toHumanReadable(entityField));

                    HtmlOutputText outputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
                    htmlPanelGrid.getChildren().add(outputText);
                    outputText.setValueExpression("value", new EntityFieldValueExpression(this, entityField, false));
                    outputText.setConverter(new FieldConverter());
                }

                HtmlPanelGrid buttonHtmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
                viewDialog.getChildren().add(buttonHtmlPanelGrid);
                buttonHtmlPanelGrid.setColumns(1);

                CommandButton dismissCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
                buttonHtmlPanelGrid.getChildren().add(dismissCommandButton);
                dismissCommandButton.setValue("Dismiss");
                dismissCommandButton.setOncomplete("PF('viewDialog').hide()");
            }

            if (showUpdate) {
                CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
                column.getChildren().add(commandButton);
                commandButton.setValue("Update...");
                commandButton.setOncomplete("PF('updateDialog').show()");
                commandButton.setId("updateButton");

                Dialog updateDialog = (Dialog) application.createComponent(Dialog.COMPONENT_TYPE);
                getChildren().add(updateDialog);
                updateDialog.setWidgetVar("updateDialog");
                updateDialog.setId("updateDialog");
                updateDialog.setHeader("Update " + entityName);
                updateDialog.setModal(true);

                HtmlForm updateDialogHtmlForm = (HtmlForm) application.createComponent(HtmlForm.COMPONENT_TYPE);
                updateDialog.getChildren().add(updateDialogHtmlForm);
                updateDialogHtmlForm.setId("updateForm");

                commandButton.setUpdate(updateDialog.getClientId() + "," + message.getClientId());
                commandButton.addActionListener(new SelectRowActionListener());
                commandButton.addActionListener(new ResetInputActionListener(expressionFactory.createValueExpression(updateDialogHtmlForm.getClientId(), String.class), null));

                HtmlPanelGrid htmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
                updateDialogHtmlForm.getChildren().add(htmlPanelGrid);
                htmlPanelGrid.setColumns(3);

                OutputLabel idOutputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(idOutputLabel);
                idOutputLabel.setValue(entityInspector.toHumanReadable(idField));

                HtmlOutputText identifierOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(identifierOutputText);
                identifierOutputText.setValueExpression("value", new EntityFieldValueExpression(this, idField, false));

                HtmlOutputText voidOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(voidOutputText);

                for (Field entityField : entityInspector.getOtherFields()) {
                    addInputComponent(entityField, false, entityInspector, htmlPanelGrid);
                }

                HtmlPanelGrid buttonHtmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
                updateDialogHtmlForm.getChildren().add(buttonHtmlPanelGrid);
                buttonHtmlPanelGrid.setColumns(2);

                CommandButton saveCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
                buttonHtmlPanelGrid.getChildren().add(saveCommandButton);
                saveCommandButton.setId("saveButton");
                saveCommandButton.setValue("Save");
                saveCommandButton.setOncomplete("updateEntityResponse(xhr, status, args)");
                saveCommandButton.addActionListener(new SaveActionListener(entityInspector));
                saveCommandButton.setUpdate(updateDialogHtmlForm.getClientId() + "," + dataTable.getClientId() + "," + message.getClientId());

                CommandButton dismissCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
                buttonHtmlPanelGrid.getChildren().add(dismissCommandButton);
                dismissCommandButton.setValue("Dismiss");
                dismissCommandButton.setOncomplete("PF('updateDialog').hide()");
            }

            if (showDelete) {
                CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
                column.getChildren().add(commandButton);
                commandButton.setValue("Delete...");
                commandButton.setId("deleteButton");
                commandButton.setOncomplete("PF('deleteDialog').show()");

                Dialog deleteDialog = (Dialog) application.createComponent(Dialog.COMPONENT_TYPE);
                getChildren().add(deleteDialog);
                deleteDialog.setWidgetVar("deleteDialog");
                deleteDialog.setHeader("Delete " + entityName);
                deleteDialog.setId("deleteDialog");
                deleteDialog.setModal(true);

                HtmlOutputText htmlOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
                deleteDialog.getChildren().add(htmlOutputText);

                HtmlForm deleteDialogHtmlForm = (HtmlForm) application.createComponent(HtmlForm.COMPONENT_TYPE);
                deleteDialog.getChildren().add(deleteDialogHtmlForm);
                deleteDialogHtmlForm.setId("deleteForm");

                HtmlPanelGrid htmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
                deleteDialogHtmlForm.getChildren().add(htmlPanelGrid);
                htmlPanelGrid.setColumns(2);

                CommandButton deleteCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(deleteCommandButton);
                deleteCommandButton.setValue("Delete");
                deleteCommandButton.setId("deleteButton");
                deleteCommandButton.addActionListener(new DeleteActionListener(entityInspector));
                deleteCommandButton.setOncomplete("PF('deleteDialog').hide()");
                deleteCommandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId());

                CommandButton dismissCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(dismissCommandButton);
                dismissCommandButton.setValue("Dismiss");
                dismissCommandButton.setOncomplete("PF('deleteDialog').hide()");

                commandButton.setUpdate(deleteDialog.getClientId() + "," + message.getClientId());
                commandButton.addActionListener(new UpdateDeleteDialogText(entityInspector, htmlOutputText));
            }
        }

        HtmlPanelGroup footerHtmlPanelGroup = (HtmlPanelGroup) application.createComponent(HtmlPanelGroup.COMPONENT_TYPE);
        dataTable.getFacets().put("footer", footerHtmlPanelGroup);

        if (showCreate) {
            CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
            footerHtmlPanelGroup.getChildren().add(commandButton);
            commandButton.setValue("Add...");
            commandButton.setOncomplete("PF('addDialog').show()");
            commandButton.setId("addButton");

            Dialog addDialog = (Dialog) application.createComponent(Dialog.COMPONENT_TYPE);
            getChildren().add(addDialog);
            addDialog.setWidgetVar("addDialog");
            addDialog.setId("addDialog");
            addDialog.setHeader("Add " + entityName);
            addDialog.setModal(true);

            HtmlForm addDialogHtmlForm = (HtmlForm) application.createComponent(HtmlForm.COMPONENT_TYPE);
            addDialog.getChildren().add(addDialogHtmlForm);
            addDialogHtmlForm.setId("addForm");

            commandButton.setUpdate(addDialog.getClientId() + "," + message.getClientId());
            commandButton.addActionListener(new ResetInputActionListener(expressionFactory.createValueExpression(addDialogHtmlForm.getClientId(), String.class), null));

            HtmlPanelGrid htmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
            addDialogHtmlForm.getChildren().add(htmlPanelGrid);
            htmlPanelGrid.setColumns(3);

            GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);
            if (null == generatedValue) {
                OutputLabel idOutputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(idOutputLabel);
                idOutputLabel.setValue(entityInspector.toHumanReadable(idField));
                idOutputLabel.setFor("identifierInput");

                InputText identifierInputText = (InputText) application.createComponent(InputText.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(identifierInputText);
                identifierInputText.setId("identifierInput");
                identifierInputText.setValueExpression("value", new EntityFieldValueExpression(this, idField, true));
                identifierInputText.setRequired(true);
                identifierInputText.addValidator(new NonExistingIdentifierValidator(entityClass));

                Message identifierInputTextMessage = (Message) application.createComponent(Message.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(identifierInputTextMessage);
                identifierInputTextMessage.setFor("identifierInput");
            }

            for (Field entityField : entityInspector.getOtherFields()) {
                addInputComponent(entityField, true, entityInspector, htmlPanelGrid);
            }

            HtmlPanelGrid buttonHtmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
            addDialogHtmlForm.getChildren().add(buttonHtmlPanelGrid);
            buttonHtmlPanelGrid.setColumns(2);

            CommandButton addCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
            buttonHtmlPanelGrid.getChildren().add(addCommandButton);
            addCommandButton.setId("addButton");
            addCommandButton.setValue("Add");
            addCommandButton.setOncomplete("addEntityResponse(xhr, status, args)");
            addCommandButton.addActionListener(new AddActionListener(entityInspector));
            addCommandButton.setUpdate(addDialogHtmlForm.getClientId() + "," + dataTable.getClientId() + "," + message.getClientId());

            CommandButton dismissCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
            buttonHtmlPanelGrid.getChildren().add(dismissCommandButton);
            dismissCommandButton.setValue("Dismiss");
            dismissCommandButton.setOncomplete("PF('addDialog').hide()");
        }

        if (null != deleteComponent && deleteComponent.isDeleteAll()) {
            CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
            footerHtmlPanelGroup.getChildren().add(commandButton);
            commandButton.setValue("Delete all...");
            commandButton.setOncomplete("PF('deleteAllDialog').show()");
            commandButton.setId("deleteAllButton");
            commandButton.setUpdate(message.getClientId());

            Dialog deleteAllDialog = (Dialog) application.createComponent(Dialog.COMPONENT_TYPE);
            getChildren().add(deleteAllDialog);
            deleteAllDialog.setWidgetVar("deleteAllDialog");
            deleteAllDialog.setId("deleteAllDialog");
            deleteAllDialog.setHeader("Delete all?");
            deleteAllDialog.setModal(true);

            HtmlOutputText htmlOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
            deleteAllDialog.getChildren().add(htmlOutputText);
            htmlOutputText.setValue("Are you sure that you want to delete all entries?");

            HtmlForm deleteAllDialogHtmlForm = (HtmlForm) application.createComponent(HtmlForm.COMPONENT_TYPE);
            deleteAllDialog.getChildren().add(deleteAllDialogHtmlForm);
            deleteAllDialogHtmlForm.setId("deleteAllForm");

            HtmlPanelGrid htmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
            deleteAllDialogHtmlForm.getChildren().add(htmlPanelGrid);
            htmlPanelGrid.setColumns(2);

            CommandButton deleteCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(deleteCommandButton);
            deleteCommandButton.setValue("Delete All");
            deleteCommandButton.setId("deleteAllButton");
            deleteCommandButton.addActionListener(new DeleteAllActionListener(entityClass));
            deleteCommandButton.setOncomplete("PF('deleteAllDialog').hide()");
            deleteCommandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId());

            CommandButton dismissCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(dismissCommandButton);
            dismissCommandButton.setValue("Dismiss");
            dismissCommandButton.setOncomplete("PF('deleteAllDialog').hide()");
        }
    }

    private void addInputComponent(Field entityField, boolean addNotUpdate, EntityInspector entityInspector, HtmlPanelGrid htmlPanelGrid) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();

        OutputLabel outputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
        htmlPanelGrid.getChildren().add(outputLabel);
        outputLabel.setValue(entityInspector.toHumanReadable(entityField));
        outputLabel.setFor(entityField.getName());

        UIInput input;
        ManyToOne manyToOne = entityField.getAnnotation(ManyToOne.class);
        if (null != manyToOne) {
            input = (SelectOneMenu) application.createComponent(SelectOneMenu.COMPONENT_TYPE);
            UISelectItem emptySelectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
            input.getChildren().add(emptySelectItem);
            input.setConverter(new EntityConverter(entityField.getType()));

            ELContext context = facesContext.getELContext();
            ValueExpression valueExpression = expressionFactory.createValueExpression(context, "#{crudController}", CRUDController.class);
            CRUDController crudController = (CRUDController) valueExpression.getValue(context);
            EntityManager entityManager = crudController.getEntityManager();

            EntityInspector otherEntityInspector = new EntityInspector(entityField.getType());
            Query query = entityManager.createQuery("SELECT entity FROM " + entityField.getType().getSimpleName() + " AS entity");
            List resultList = query.getResultList();
            for (Object otherEntity : resultList) {
                UISelectItem selectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
                selectItem.setItemValue(otherEntity);
                selectItem.setItemLabel(otherEntityInspector.toHumanReadable(otherEntity));
                input.getChildren().add(selectItem);
            }
        } else if (entityField.getType() == Boolean.TYPE) {
            input = (SelectBooleanCheckbox) application.createComponent(SelectBooleanCheckbox.COMPONENT_TYPE);
        } else if (entityField.getType() == Date.class) {
            input = (Calendar) application.createComponent(Calendar.COMPONENT_TYPE);
            Temporal temporal = entityField.getAnnotation(Temporal.class);
            if (null != temporal) {
                Calendar calendar = (Calendar) input;
                if (null == temporal.value()) {
                    calendar.setPattern("dd/MM/yyyy");
                } else {
                    switch (temporal.value()) {
                        case TIME:
                            calendar.setTimeOnly(true);
                            calendar.setPattern("HH:mm:ss");
                            break;
                        case TIMESTAMP:
                            calendar.setPattern("dd/MM/yyyy HH:mm:ss");
                            break;
                        default:
                            calendar.setPattern("dd/MM/yyyy");
                            break;
                    }
                }
            }
        } else if (entityField.getType().isEnum()) {
            input = (SelectOneMenu) application.createComponent(SelectOneMenu.COMPONENT_TYPE);
            UISelectItem emptySelectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
            input.getChildren().add(emptySelectItem);
            Object[] enumConstants = entityField.getType().getEnumConstants();
            LOGGER.debug("enum constants: {}", enumConstants);
            for (Object enumConstant : enumConstants) {
                UISelectItem selectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
                selectItem.setItemValue(enumConstant);
                selectItem.setItemLabel(enumConstant.toString());
                input.getChildren().add(selectItem);
            }
        } else {
            input = (InputText) application.createComponent(InputText.COMPONENT_TYPE);
        }
        htmlPanelGrid.getChildren().add(input);
        input.setId(entityField.getName());
        input.setValueExpression("value", new EntityFieldValueExpression(this, entityField, addNotUpdate));
        javax.persistence.Column column = entityField.getAnnotation(javax.persistence.Column.class);
        if (null != column) {
            if (!column.nullable()) {
                input.setRequired(true);
            }
        }

        Message inputTextMessage = (Message) application.createComponent(Message.COMPONENT_TYPE);
        htmlPanelGrid.getChildren().add(inputTextMessage);
        inputTextMessage.setFor(entityField.getName());
    }

    private void addColumn(DataTable dataTable, Field field, EntityInspector entityInspector) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();

        Column column = new Column();
        dataTable.getChildren().add(column);
        column.setHeaderText(entityInspector.toHumanReadable(field));

        HtmlOutputText outputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        column.getChildren().add(outputText);
        outputText.setValueExpression("value", expressionFactory.createValueExpression(elContext, "#{row. " + field.getName() + "}", Object.class));
        outputText.setConverter(new FieldConverter());
    }

    public class SaveActionListener implements ActionListener {

        private final EntityInspector entityInspector;

        public SaveActionListener(EntityInspector entityInspector) {
            this.entityInspector = entityInspector;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            LOGGER.debug("processAction add");

            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext context = facesContext.getELContext();
            Application application = facesContext.getApplication();
            ExpressionFactory expressionFactory = application.getExpressionFactory();
            ValueExpression valueExpression = expressionFactory.createValueExpression(context, "#{crudController}", CRUDController.class);
            CRUDController crudController = (CRUDController) valueExpression.getValue(context);
            EntityManager entityManager = crudController.getEntityManager();
            UserTransaction userTransaction = crudController.getUserTransaction();

            Object entity = CRUDComponent.this.getSelection();

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
            CRUDComponent.this.setSelection(null);

            String entityHumanReadable = this.entityInspector.toHumanReadable(entity);
            CRUDComponent.this.addMessage(FacesMessage.SEVERITY_INFO, "Updated " + entityHumanReadable);
        }
    }

    private void addMessage(FacesMessage.Severity severity, String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String dataTableClientId = null;
        for (UIComponent child : getChildren()) {
            if (child instanceof HtmlForm) {
                for (UIComponent htmlFormChild : child.getChildren()) {
                    if (htmlFormChild instanceof DataTable) {
                        dataTableClientId = htmlFormChild.getClientId();
                        break;
                    }
                }
            }
        }
        facesContext.addMessage(dataTableClientId, new FacesMessage(severity, message, null));
    }

    public class AddActionListener implements ActionListener {

        private final EntityInspector entityInspector;

        public AddActionListener(EntityInspector entityInspector) {
            this.entityInspector = entityInspector;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            LOGGER.debug("processAction add");

            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext context = facesContext.getELContext();
            Application application = facesContext.getApplication();
            ExpressionFactory expressionFactory = application.getExpressionFactory();
            ValueExpression valueExpression = expressionFactory.createValueExpression(context, "#{crudController}", CRUDController.class);
            CRUDController crudController = (CRUDController) valueExpression.getValue(context);
            EntityManager entityManager = crudController.getEntityManager();
            UserTransaction userTransaction = crudController.getUserTransaction();

            Object entity = CRUDComponent.this.getNewEntity();

            try {
                userTransaction.begin();
            } catch (NotSupportedException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return;
            }

            entityManager.persist(entity);

            try {
                userTransaction.commit();
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return;
            }
            CRUDComponent.this.setNewEntity(null);

            String entityHumanReadable = this.entityInspector.toHumanReadable(entity);
            CRUDComponent.this.addMessage(FacesMessage.SEVERITY_INFO, "Added " + entityHumanReadable);
        }
    }

    public class DeleteAllActionListener implements ActionListener {

        private final Class<?> entityClass;

        public DeleteAllActionListener(Class<?> entityClass) {
            this.entityClass = entityClass;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext context = facesContext.getELContext();
            Application application = facesContext.getApplication();
            ExpressionFactory expressionFactory = application.getExpressionFactory();
            ValueExpression valueExpression = expressionFactory.createValueExpression(context, "#{crudController}", CRUDController.class);
            CRUDController crudController = (CRUDController) valueExpression.getValue(context);
            EntityManager entityManager = crudController.getEntityManager();
            UserTransaction userTransaction = crudController.getUserTransaction();

            try {
                userTransaction.begin();
            } catch (NotSupportedException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return;
            }

            Query query = entityManager.createQuery("DELETE FROM " + this.entityClass.getSimpleName());
            int count = query.executeUpdate();

            try {
                userTransaction.commit();
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                CRUDComponent.this.addMessage(FacesMessage.SEVERITY_ERROR, "Could not delete entries.");
                return;
            }

            CRUDComponent.this.addMessage(FacesMessage.SEVERITY_INFO, "Deleted " + count + " entries.");
        }
    }

    public class DeleteActionListener implements ActionListener {

        private final EntityInspector entityInspector;

        public DeleteActionListener(EntityInspector entityInspector) {
            this.entityInspector = entityInspector;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            LOGGER.debug("processAction DeleteActionListener");
            LOGGER.debug("delete: {}", CRUDComponent.this.getSelection());

            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext context = facesContext.getELContext();
            Application application = facesContext.getApplication();
            ExpressionFactory expressionFactory = application.getExpressionFactory();
            ValueExpression valueExpression = expressionFactory.createValueExpression(context, "#{crudController}", CRUDController.class);
            CRUDController crudController = (CRUDController) valueExpression.getValue(context);
            EntityManager entityManager = crudController.getEntityManager();
            UserTransaction userTransaction = crudController.getUserTransaction();

            Object selection = CRUDComponent.this.getSelection();

            try {
                userTransaction.begin();
            } catch (NotSupportedException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return;
            }

            Object identifier = this.entityInspector.getIdentifier(selection);
            Object entity = entityManager.find(selection.getClass(), identifier);

            entityManager.remove(entity);
            try {
                userTransaction.commit();
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                String entityHumanReadable = this.entityInspector.toHumanReadable(entity);
                CRUDComponent.this.addMessage(FacesMessage.SEVERITY_ERROR, "Could not delete " + entityHumanReadable);
                return;
            }
            CRUDComponent.this.setSelection(null);

            String entityHumanReadable = this.entityInspector.toHumanReadable(entity);
            CRUDComponent.this.addMessage(FacesMessage.SEVERITY_INFO, "Deleted " + entityHumanReadable);
        }
    }

    public class SelectRowActionListener implements ActionListener {

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            LOGGER.debug("processAction");
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext elContext = facesContext.getELContext();
            ELResolver elResolver = elContext.getELResolver();
            Object entity = elResolver.getValue(elContext, null, "row");
            CRUDComponent.this.setSelection(entity);
        }
    }

    public class UpdateDeleteDialogText implements ActionListener {

        private final HtmlOutputText htmlOutputText;

        private final EntityInspector entityInspector;

        public UpdateDeleteDialogText(EntityInspector entityInspector, HtmlOutputText htmlOutputText) {
            this.htmlOutputText = htmlOutputText;
            this.entityInspector = entityInspector;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            LOGGER.debug("processAction");
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext elContext = facesContext.getELContext();
            ELResolver elResolver = elContext.getELResolver();
            Object entity = elResolver.getValue(elContext, null, "row");
            String entityHumanReadable = this.entityInspector.toHumanReadable(entity);
            this.htmlOutputText.setValue("Do you want to delete: " + entityHumanReadable + " ?");
            CRUDComponent.this.setSelection(entity);
        }
    }
}
