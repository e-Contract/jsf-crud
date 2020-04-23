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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.faces.application.Application;
import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import org.primefaces.component.datatable.DataTable;
import java.util.List;
import java.util.Map;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UISelectItem;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.ExternalContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.validator.LengthValidator;
import javax.persistence.Basic;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Temporal;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.primefaces.PrimeFaces;
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
import org.primefaces.component.tristatecheckbox.TriStateCheckbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(CRUDComponent.COMPONENT_TYPE)
@ResourceDependencies(value = {
    @ResourceDependency(library = "crud", name = "crud.js")
})
public class CRUDComponent extends UINamingContainer implements SystemEventListener, CreateSource, UpdateSource, DeleteSource {

    public static final String COMPONENT_TYPE = "crud.crud";

    private static final Logger LOGGER = LoggerFactory.getLogger(CRUDComponent.class);

    public CRUDComponent() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = facesContext.getViewRoot();
        viewRoot.subscribeToViewEvent(PostAddToViewEvent.class, this);
    }

    @Override
    public boolean isListenerForSource(Object source) {
        return (source instanceof UIViewRoot);
    }

    public enum PropertyKeys {
        entity,
        selection,
        newEntity,
        title,
        orderBy,
        sort,
        roleAllowed,
    }

    public void setEntity(String entity) {
        LOGGER.debug("setEntity: {}", entity);
        getStateHelper().put(PropertyKeys.entity, entity);
    }

    private void updateEntityComponents(Object entity, UIComponent component) {
        if (component instanceof EntityComponent) {
            EntityComponent entityComponent = (EntityComponent) component;
            entityComponent.setEntity(entity, this.getId());
        }
        for (UIComponent child : component.getChildren()) {
            updateEntityComponents(entity, child);
        }
    }

    public String getEntity() {
        return (String) getStateHelper().eval(PropertyKeys.entity);
    }

    public void setTitle(String title) {
        getStateHelper().put(PropertyKeys.title, title);
    }

    public String getTitle() {
        return (String) getStateHelper().eval(PropertyKeys.title);
    }

    void setSelection(Object entity) {
        getStateHelper().put(PropertyKeys.selection, entity);
        updateEntityComponents(entity, this);
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

    public String getOrderBy() {
        return (String) getStateHelper().get(PropertyKeys.orderBy);
    }

    public void setOrderBy(String orderBy) {
        getStateHelper().put(PropertyKeys.orderBy, orderBy);
    }

    public boolean isSort() {
        Boolean sort = (Boolean) getStateHelper().get(PropertyKeys.sort);
        if (null == sort) {
            return false;
        }
        return sort;
    }

    public void setSort(boolean sort) {
        getStateHelper().put(PropertyKeys.sort, sort);
    }

    public void setRoleAllowed(String roleAllowed) {
        getStateHelper().put(PropertyKeys.roleAllowed, roleAllowed);
    }

    public String getRoleAllowed() {
        return (String) getStateHelper().eval(PropertyKeys.roleAllowed);
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        LOGGER.debug("constructing component");
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();

        boolean showCreate = true;
        boolean showDelete = true;
        boolean showUpdate = true;
        boolean showView = false;
        CreateComponent createComponent = null;
        DeleteComponent deleteComponent = null;
        UpdateComponent updateComponent = null;
        ReadComponent readComponent = null;
        Map<String, FieldComponent> fields = new HashMap<>();
        Map<String, FieldComponent> createFields = new HashMap<>();
        Map<String, FieldComponent> updateFields = new HashMap<>();
        List<ActionComponent> actions = new LinkedList<>();
        List<PropertyComponent> properties = new LinkedList<>();
        List<UIComponent> children = getChildren();
        for (UIComponent child : children) {
            if (child instanceof CreateComponent) {
                createComponent = (CreateComponent) child;
                if (createComponent.isDisabled()) {
                    showCreate = false;
                }
                for (UIComponent createChild : createComponent.getChildren()) {
                    if (createChild instanceof FieldComponent) {
                        FieldComponent createFieldComponent = (FieldComponent) createChild;
                        createFields.put(createFieldComponent.getName(), createFieldComponent);
                    }
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
                for (UIComponent updateChild : updateComponent.getChildren()) {
                    if (updateChild instanceof FieldComponent) {
                        FieldComponent createFieldComponent = (FieldComponent) updateChild;
                        updateFields.put(createFieldComponent.getName(), createFieldComponent);
                    }
                }
            } else if (child instanceof ReadComponent) {
                readComponent = (ReadComponent) child;
                showView = true;
            } else if (child instanceof CreateListenerComponent) {
                CreateListenerComponent createListenerComponent = (CreateListenerComponent) child;
                MethodExpression methodExpression = createListenerComponent.getAction();
                CreateAdapter createAdapter = new CreateAdapter(methodExpression);
                addCreateListener(createAdapter);
            } else if (child instanceof UpdateListenerComponent) {
                UpdateListenerComponent updateListenerComponent = (UpdateListenerComponent) child;
                MethodExpression methodExpression = updateListenerComponent.getAction();
                UpdateAdapter updateAdapter = new UpdateAdapter(methodExpression);
                addUpdateListener(updateAdapter);
            } else if (child instanceof DeleteListenerComponent) {
                DeleteListenerComponent deleteListenerComponent = (DeleteListenerComponent) child;
                MethodExpression methodExpresssion = deleteListenerComponent.getAction();
                DeleteAdapter deleteAdapter = new DeleteAdapter(methodExpresssion);
                addDeleteListener(deleteAdapter);
            } else if (child instanceof FieldComponent) {
                FieldComponent fieldComponent = (FieldComponent) child;
                fields.put(fieldComponent.getName(), fieldComponent);
            } else if (child instanceof ActionComponent) {
                ActionComponent action = (ActionComponent) child;
                actions.add(action);
            } else if (child instanceof PropertyComponent) {
                PropertyComponent propertyComponent = (PropertyComponent) child;
                properties.add(propertyComponent);
            }
        }

        String roleAllowed = getRoleAllowed();
        if (!UIInput.isEmpty(roleAllowed)) {
            LOGGER.debug("role allowed: {}", roleAllowed);
            ExternalContext externalContext = facesContext.getExternalContext();
            HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext.getRequest();
            if (!httpServletRequest.isUserInRole(roleAllowed)) {
                LOGGER.warn("caller principal not in role: {}", roleAllowed);
                throw new AbortProcessingException();
            }
        }

        String entityClassName = getEntity();
        EntityInspector entityInspector = new EntityInspector(entityClassName);
        Class<?> entityClass = entityInspector.getEntityClass();

        String entityName = entityInspector.getEntityName();

        HtmlForm htmlForm = (HtmlForm) application.createComponent(HtmlForm.COMPONENT_TYPE);
        getChildren().add(htmlForm);
        htmlForm.setId("form");

        Message message = (Message) application.createComponent(Message.COMPONENT_TYPE);
        htmlForm.getChildren().add(message);
        message.setId("message");
        message.setFor("table");

        Spacer spacer = (Spacer) application.createComponent(Spacer.COMPONENT_TYPE);
        htmlForm.getChildren().add(spacer);
        spacer.setId("spacer");
        spacer.setHeight("5px");

        DataTable dataTable = (DataTable) application.createComponent(DataTable.COMPONENT_TYPE);
        htmlForm.getChildren().add(dataTable);
        dataTable.setId("table");

        AjaxUpdateListener ajaxUpdateCreateListener = new AjaxUpdateListener();
        ajaxUpdateCreateListener.addClientId(message.getClientId());
        ajaxUpdateCreateListener.addClientId(dataTable.getClientId());
        addFacesListener(ajaxUpdateCreateListener);

        ValueExpression valueExpression = new EntityValueExpression(entityClass, getId(), getOrderBy());
        dataTable.setValueExpression("value", valueExpression);
        dataTable.setVar("row");
        dataTable.setResizableColumns(true);
        dataTable.setTableStyle("table-layout: auto !important;");
        List entityList = (List) valueExpression.getValue(elContext);
        if (entityList.size() > 20) {
            dataTable.setPaginator(true);
            dataTable.setRows(20);
        }

        if (!UIInput.isEmpty(getTitle())) {
            HtmlOutputText headerOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
            dataTable.getFacets().put("header", headerOutputText);
            headerOutputText.setId("title");
            headerOutputText.setValue(getTitle());
        }

        // first column is the @Id column
        Field idField = entityInspector.getIdField();
        addColumn(dataTable, idField, entityInspector, fields);

        // next we add all the others
        for (Field entityField : entityInspector.getOtherFields()) {
            addColumn(dataTable, entityField, entityInspector, fields);
        }

        for (PropertyComponent property : properties) {
            addColumn(dataTable, property);
        }

        if (showDelete || showUpdate || showView || !actions.isEmpty()) {
            Column column = new Column();
            dataTable.getChildren().add(column);
            column.setHeaderText("Actions");

            addViewDialog(showView, application, column, entityName, message, entityInspector, idField, fields);

            addUpdateDialog(showUpdate, application, column, entityName, message, expressionFactory, entityInspector, idField, fields, updateFields);

            addDeleteDialog(showDelete, application, column, deleteComponent, entityName, elContext, expressionFactory, entityInspector, dataTable, message);

            addCustomActions(actions, application, column, dataTable, message, facesContext);
        }

        HtmlPanelGroup footerHtmlPanelGroup = (HtmlPanelGroup) application.createComponent(HtmlPanelGroup.COMPONENT_TYPE);
        dataTable.getFacets().put("footer", footerHtmlPanelGroup);
        footerHtmlPanelGroup.setStyle("display:block; text-align: left;");

        addCreateDialog(showCreate, application, footerHtmlPanelGroup, entityName, message, expressionFactory, idField, entityInspector, entityClass, fields, createFields);

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

            DismissButton dismissCommandButton = (DismissButton) application.createComponent(DismissButton.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(dismissCommandButton);
        }
    }

    private void addCustomActions(List<ActionComponent> actions, Application application, Column column, DataTable dataTable, Message message, FacesContext facesContext) throws FacesException {
        int actionIdx = 0;
        for (ActionComponent action : actions) {
            CRUDCommandButton commandButton = (CRUDCommandButton) application.createComponent(CRUDCommandButton.COMPONENT_TYPE);
            column.getChildren().add(commandButton);
            commandButton.setValue(action.getValue());
            commandButton.setId("Action" + actionIdx);
            actionIdx++;
            commandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId());
            commandButton.addActionListener(new ActionAdapter(action.getAction(), action.getUpdate(), getId()));
            commandButton.setOncomplete(action.getOncomplete());

            ValueExpression renderedValueExpression = action.getRenderedValueExpression();
            commandButton.setRenderedValueExpression(renderedValueExpression);

            String update = action.getUpdate();
            if (null != update) {
                UIViewRoot view = facesContext.getViewRoot();
                UIComponent component = view.findComponent(update);
                commandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId() + "," + component.getClientId());
            }
        }
    }

    private void addCreateDialog(boolean showCreate, Application application, HtmlPanelGroup footerHtmlPanelGroup, String entityName,
            Message message, ExpressionFactory expressionFactory, Field idField, EntityInspector entityInspector,
            Class<?> entityClass, Map<String, FieldComponent> fields, Map<String, FieldComponent> createFields) throws FacesException {
        if (!showCreate) {
            return;
        }
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
            addInputComponent(entityField, true, entityInspector, fields, createFields, htmlPanelGrid);
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
        addCommandButton.setUpdate(addDialogHtmlForm.getClientId());

        DismissButton dismissCommandButton = (DismissButton) application.createComponent(DismissButton.COMPONENT_TYPE);
        buttonHtmlPanelGrid.getChildren().add(dismissCommandButton);
    }

    private void addDeleteDialog(boolean showDelete, Application application, Column column, DeleteComponent deleteComponent, String entityName, ELContext elContext, ExpressionFactory expressionFactory, EntityInspector entityInspector, DataTable dataTable, Message message) throws FacesException {
        if (!showDelete) {
            return;
        }
        CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        column.getChildren().add(commandButton);
        commandButton.setValue("Delete...");
        commandButton.setId("deleteButton");
        commandButton.setOncomplete("PF('deleteDialog').show()");

        Dialog deleteDialog = (Dialog) application.createComponent(Dialog.COMPONENT_TYPE);
        getChildren().add(deleteDialog);
        deleteDialog.setWidgetVar("deleteDialog");
        String deleteDialogHeader = null;
        if (null != deleteComponent) {
            deleteDialogHeader = deleteComponent.getTitle();
        }
        if (null == deleteDialogHeader) {
            deleteDialogHeader = "Delete " + entityName;
        }
        deleteDialog.setHeader(deleteDialogHeader);
        deleteDialog.setId("deleteDialog");
        deleteDialog.setModal(true);

        if (!relocateChildren(application, deleteComponent, deleteDialog)) {
            EntityComponent entityComponent = (EntityComponent) application.createComponent(EntityComponent.COMPONENT_TYPE);
            deleteDialog.getChildren().add(entityComponent);
            entityComponent.setVar("entity");
            HtmlOutputText htmlOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);

            try {
                registerToHumanReadableFunction(elContext);
            } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                LOGGER.error("reflection error: " + ex.getMessage(), ex);
                throw new AbortProcessingException();
            }
            ValueExpression deleteOutputTextValueExpression = expressionFactory.createValueExpression(new CRUDELContext(elContext), "Do you want to delete #{crud:toHumanReadable(entity)} ?", String.class);
            htmlOutputText.setValueExpression("value", deleteOutputTextValueExpression);
            entityComponent.getChildren().add(htmlOutputText);
        }

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

        DismissButton dismissCommandButton = (DismissButton) application.createComponent(DismissButton.COMPONENT_TYPE);
        htmlPanelGrid.getChildren().add(dismissCommandButton);

        commandButton.setUpdate(deleteDialog.getClientId() + "," + message.getClientId());
        commandButton.addActionListener(new SelectRowActionListener());
    }

    private boolean relocateChildren(Application application, UIComponent oldParent, UIComponent newParent) {
        if (null == oldParent) {
            return false;
        }
        if (oldParent.getChildCount() == 0) {
            return false;
        }
        List<UIComponent> children = new LinkedList<>(oldParent.getChildren());
        oldParent.getChildren().clear();
        ContainerComponent containerComponent = (ContainerComponent) application.createComponent(ContainerComponent.COMPONENT_TYPE);
        containerComponent.setId(newParent.getId() + "Container");
        newParent.getChildren().add(containerComponent);
        for (UIComponent child : children) {
            containerComponent.getChildren().add(child);
            reloadId(child);
        }
        return true;
    }

    private void reloadId(UIComponent component) {
        component.setId(component.getId());
        for (UIComponent child : component.getChildren()) {
            reloadId(child);
        }
    }

    private void addUpdateDialog(boolean showUpdate, Application application, Column column, String entityName, Message message,
            ExpressionFactory expressionFactory, EntityInspector entityInspector, Field idField, Map<String, FieldComponent> fields, Map<String, FieldComponent> updateFields) throws FacesException {
        if (!showUpdate) {
            return;
        }

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

        if (!isHideField(idField, fields, updateFields)) {
            OutputLabel idOutputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(idOutputLabel);
            idOutputLabel.setValue(entityInspector.toHumanReadable(idField));

            HtmlOutputText identifierOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(identifierOutputText);
            identifierOutputText.setValueExpression("value", new EntityFieldValueExpression(this, idField, false));

            HtmlOutputText voidOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(voidOutputText);
        }

        for (Field entityField : entityInspector.getOtherFields()) {
            addInputComponent(entityField, false, entityInspector, fields, updateFields, htmlPanelGrid);
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
        saveCommandButton.setUpdate(updateDialogHtmlForm.getClientId());

        DismissButton dismissCommandButton = (DismissButton) application.createComponent(DismissButton.COMPONENT_TYPE);
        buttonHtmlPanelGrid.getChildren().add(dismissCommandButton);
    }

    private void addViewDialog(boolean showView, Application application, Column column, String entityName, Message message, EntityInspector entityInspector, Field idField, Map<String, FieldComponent> fields) throws FacesException {
        if (!showView) {
            return;
        }
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
            String fieldLabel = getFieldLabel(entityField, entityInspector, fields);
            OutputLabel outputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(outputLabel);
            outputLabel.setValue(fieldLabel);

            HtmlOutputText outputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(outputText);
            outputText.setValueExpression("value", new EntityFieldValueExpression(this, entityField, false));
            outputText.setConverter(new FieldConverter());
        }

        HtmlPanelGrid buttonHtmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        viewDialog.getChildren().add(buttonHtmlPanelGrid);
        buttonHtmlPanelGrid.setColumns(1);

        DismissButton dismissCommandButton = (DismissButton) application.createComponent(DismissButton.COMPONENT_TYPE);
        buttonHtmlPanelGrid.getChildren().add(dismissCommandButton);
    }

    private void registerToHumanReadableFunction(ELContext elContext) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FunctionMapper functionMapper = elContext.getFunctionMapper();
        if (null == functionMapper) {
            // open liberty
            LOGGER.warn("missing FunctionMapper");
            return;
        }
        LOGGER.debug("FunctionMapper class: {}", functionMapper.getClass().getName());

        Method mapFunctionMethod = null;
        Method[] methods = FunctionMapper.class.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("mapFunction")) {
                mapFunctionMethod = method;
                break;
            }
        }
        if (null == mapFunctionMethod) {
            // not available on JBoss EAP 6.4.22
            return;
        }

        Method toHumanReadableMethod = CRUDFunctions.class.getMethod("toHumanReadable", new Class[]{Object.class});

        mapFunctionMethod.invoke(functionMapper, "crud", "toHumanReadable", toHumanReadableMethod);
    }

    private String getFieldLabel(Field entityField, EntityInspector entityInspector, Map<String, FieldComponent> fields) {
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null != fieldComponent) {
            if (!UIInput.isEmpty(fieldComponent.getLabel())) {
                return fieldComponent.getLabel();
            }
        }
        return entityInspector.toHumanReadable(entityField);
    }

    private boolean isHideField(Field entityField, Map<String, FieldComponent> fields, Map<String, FieldComponent> overrideFields) {
        if (null != overrideFields) {
            FieldComponent fieldComponent = overrideFields.get(entityField.getName());
            if (null != fieldComponent) {
                Boolean hide = fieldComponent.isHide();
                if (null != hide) {
                    return hide;
                }
            }
        }
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return false;
        }
        Boolean hide = fieldComponent.isHide();
        if (null == hide) {
            return false;
        }
        return hide;
    }

    private boolean isSortField(Field entityField, Map<String, FieldComponent> fields) {
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return isSort();
        }
        Boolean sort = fieldComponent.isSort();
        if (null == sort) {
            return isSort();
        }
        return sort;
    }

    private boolean isSortProperty(PropertyComponent property) {
        Boolean sort = property.isSort();
        if (null == sort) {
            return isSort();
        }
        return sort;
    }

    private boolean isFilterField(Field entityField, Map<String, FieldComponent> fields) {
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return false;
        }
        return fieldComponent.isFilter();
    }

    private void addInputComponent(Field entityField, boolean addNotUpdate, EntityInspector entityInspector, Map<String, FieldComponent> fields, Map<String, FieldComponent> overrideFields, HtmlPanelGrid htmlPanelGrid) {
        if (isHideField(entityField, fields, overrideFields)) {
            return;
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();

        String fieldLabel = getFieldLabel(entityField, entityInspector, fields);

        OutputLabel outputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
        htmlPanelGrid.getChildren().add(outputLabel);
        outputLabel.setValue(fieldLabel);
        outputLabel.setFor(entityField.getName());

        boolean disabled = false;
        javax.persistence.Column columnAnnotation = entityField.getAnnotation(javax.persistence.Column.class);
        if (null != columnAnnotation && !addNotUpdate) {
            disabled = !columnAnnotation.updatable();
        }

        UIInput input;
        ManyToOne manyToOne = entityField.getAnnotation(ManyToOne.class);
        if (null != manyToOne) {
            input = (SelectOneMenu) application.createComponent(SelectOneMenu.COMPONENT_TYPE);
            SelectOneMenu selectOneMenu = (SelectOneMenu) input;
            selectOneMenu.setDisabled(disabled);
            UISelectItem emptySelectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
            input.getChildren().add(emptySelectItem);
            input.setConverter(new EntityConverter(entityField.getType()));

            CRUDController crudController = CRUDController.getCRUDController();
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
            SelectBooleanCheckbox selectBooleanCheckbox = (SelectBooleanCheckbox) input;
            selectBooleanCheckbox.setDisabled(disabled);
        } else if (entityField.getType() == Boolean.class) {
            input = (TriStateCheckbox) application.createComponent(TriStateCheckbox.COMPONENT_TYPE);
            input.setConverter(new TriStateBooleanConverter());
            TriStateCheckbox triStateCheckbox = (TriStateCheckbox) input;
            triStateCheckbox.setDisabled(disabled);
        } else if (entityField.getType() == Date.class) {
            input = (Calendar) application.createComponent(Calendar.COMPONENT_TYPE);
            Calendar calendarComponent = (Calendar) input;
            calendarComponent.setDisabled(disabled);
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
        } else if (entityField.getType() == java.util.Calendar.class) {
            input = (Calendar) application.createComponent(Calendar.COMPONENT_TYPE);
            Calendar calendarComponent = (Calendar) input;
            calendarComponent.setDisabled(disabled);
            input.setConverter(new CalendarConverter());
            Calendar calendar = (Calendar) input;
            calendar.setPattern("dd/MM/yyyy");
        } else if (entityField.getType().isEnum()) {
            input = (SelectOneMenu) application.createComponent(SelectOneMenu.COMPONENT_TYPE);
            SelectOneMenu selectOneMenu = (SelectOneMenu) input;
            selectOneMenu.setDisabled(disabled);
            UISelectItem emptySelectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
            input.getChildren().add(emptySelectItem);
            Object[] enumConstants = entityField.getType().getEnumConstants();
            for (Object enumConstant : enumConstants) {
                UISelectItem selectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
                selectItem.setItemValue(enumConstant);
                selectItem.setItemLabel(enumConstant.toString());
                input.getChildren().add(selectItem);
            }
        } else {
            input = (InputText) application.createComponent(InputText.COMPONENT_TYPE);
            input.addValidator(new LengthValidator(255));
            InputText inputText = (InputText) input;
            inputText.setDisabled(disabled);
        }
        htmlPanelGrid.getChildren().add(input);
        input.setId(entityField.getName());
        input.setValueExpression("value", new EntityFieldValueExpression(this, entityField, addNotUpdate));
        if (null != columnAnnotation) {
            if (!columnAnnotation.nullable()) {
                input.setRequired(true);
            }
            if (columnAnnotation.length() != 255) {
                input.addValidator(new LengthValidator(columnAnnotation.length()));
            }
        }
        Basic basicAnnotation = entityField.getAnnotation(Basic.class);
        if (null != basicAnnotation) {
            if (!basicAnnotation.optional()) {
                input.setRequired(true);
            }
        }

        Message inputTextMessage = (Message) application.createComponent(Message.COMPONENT_TYPE);
        htmlPanelGrid.getChildren().add(inputTextMessage);
        inputTextMessage.setFor(entityField.getName());
    }

    private void addColumn(DataTable dataTable, Field field, EntityInspector entityInspector, Map<String, FieldComponent> fields) {
        if (isHideField(field, fields, null)) {
            return;
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();

        Column column = (Column) application.createComponent(Column.COMPONENT_TYPE);
        dataTable.getChildren().add(column);
        column.setId(field.getName() + "Column");
        if (isSortField(field, fields)) {
            column.setValueExpression("sortBy", expressionFactory.createValueExpression(elContext, "#{row." + field.getName() + "}", String.class));
        }
        if (isFilterField(field, fields)) {
            column.setValueExpression("filterBy", expressionFactory.createValueExpression(elContext, "#{row." + field.getName() + "}", String.class));
            column.setFilterMatchMode("contains");
        }

        String fieldLabel = getFieldLabel(field, entityInspector, fields);
        column.setHeaderText(fieldLabel);

        HtmlOutputText outputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        column.getChildren().add(outputText);
        outputText.setValueExpression("value", expressionFactory.createValueExpression(elContext, "#{row." + field.getName() + "}", field.getType()));
        outputText.setConverter(new FieldConverter());
    }

    private void addColumn(DataTable dataTable, PropertyComponent property) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();

        Column column = (Column) application.createComponent(Column.COMPONENT_TYPE);
        dataTable.getChildren().add(column);
        column.setId(property.getName() + "Column");

        String propertyLabel = property.getLabel();
        if (UIInput.isEmpty(propertyLabel)) {
            propertyLabel = EntityInspector.toHumanReadable(property.getName());
        }
        column.setHeaderText(propertyLabel);
        if (isSortProperty(property)) {
            column.setValueExpression("sortBy", expressionFactory.createValueExpression(elContext, "#{row." + property.getName() + "}", String.class));
        }
        if (property.isFilter()) {
            column.setValueExpression("filterBy", expressionFactory.createValueExpression(elContext, "#{row." + property.getName() + "}", String.class));
            column.setFilterMatchMode("contains");
        }

        HtmlOutputText outputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        column.getChildren().add(outputText);
        outputText.setValueExpression("value", expressionFactory.createValueExpression(elContext, "#{row." + property.getName() + "}", Object.class));
    }

    public class SaveActionListener implements ActionListener {

        private final EntityInspector entityInspector;

        public SaveActionListener(EntityInspector entityInspector) {
            this.entityInspector = entityInspector;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            LOGGER.debug("processAction save");

            CRUDController crudController = CRUDController.getCRUDController();
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

            UpdateEvent updateEvent = new UpdateEvent(CRUDComponent.this, entity);
            updateEvent.queue();
        }
    }

    private void addMessage(FacesMessage.Severity severity, String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String dataTableClientId = null;
        String messageClientId = null;
        for (UIComponent child : getChildren()) {
            if (child instanceof HtmlForm) {
                for (UIComponent htmlFormChild : child.getChildren()) {
                    if (htmlFormChild instanceof DataTable) {
                        dataTableClientId = htmlFormChild.getClientId();
                    } else if (htmlFormChild instanceof Message) {
                        messageClientId = htmlFormChild.getClientId();
                    }
                }
            }
        }
        facesContext.addMessage(dataTableClientId, new FacesMessage(severity, message, null));

        PrimeFaces primeFaces = PrimeFaces.current();
        if (primeFaces.isAjaxRequest()) {
            primeFaces.ajax().update(messageClientId);
        }
    }

    public void resetCache() {
        resetCache(this);
    }

    public void resetCache(UIComponent component) {
        for (UIComponent child : component.getChildren()) {
            if (child instanceof DataTable) {
                ValueExpression valueExpression = child.getValueExpression("value");
                EntityValueExpression entityValueExpression = (EntityValueExpression) valueExpression;
                entityValueExpression.resetCache();
            }
            resetCache(child);
        }
    }

    public class AddActionListener implements ActionListener {

        private final EntityInspector entityInspector;

        public AddActionListener(EntityInspector entityInspector) {
            this.entityInspector = entityInspector;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            LOGGER.debug("processAction add");

            CRUDController crudController = CRUDController.getCRUDController();
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

            CreateEvent createEvent = new CreateEvent(CRUDComponent.this, entity);
            createEvent.queue();
        }
    }

    public class DeleteAllActionListener implements ActionListener {

        private final Class<?> entityClass;

        public DeleteAllActionListener(Class<?> entityClass) {
            this.entityClass = entityClass;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {

            CRUDController crudController = CRUDController.getCRUDController();
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
            CRUDComponent.this.resetCache();
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

            CRUDController crudController = CRUDController.getCRUDController();
            EntityManager entityManager = crudController.getEntityManager();
            UserTransaction userTransaction = crudController.getUserTransaction();

            Object selection = CRUDComponent.this.getSelection();

            try {
                userTransaction.begin();
            } catch (NotSupportedException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return;
            }
            Object entity;
            try {
                Object identifier = this.entityInspector.getIdentifier(selection);
                entity = entityManager.find(selection.getClass(), identifier);
                if (null != entity) {
                    entityManager.remove(entity);
                } else {
                    LOGGER.error("missing entity");
                    String entityHumanReadable = this.entityInspector.toHumanReadable(selection);
                    CRUDComponent.this.addMessage(FacesMessage.SEVERITY_ERROR, "Could not delete " + entityHumanReadable);
                    return;
                }
            } finally {
                try {
                    userTransaction.commit();
                } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
                    LOGGER.error("error: " + ex.getMessage(), ex);
                    String entityHumanReadable = this.entityInspector.toHumanReadable(selection);
                    CRUDComponent.this.addMessage(FacesMessage.SEVERITY_ERROR, "Could not delete " + entityHumanReadable);
                    return;
                }
            }
            CRUDComponent.this.setSelection(null);

            String entityHumanReadable = this.entityInspector.toHumanReadable(entity);
            CRUDComponent.this.addMessage(FacesMessage.SEVERITY_INFO, "Deleted " + entityHumanReadable);

            DeleteEvent deleteEvent = new DeleteEvent(CRUDComponent.this, entity);
            deleteEvent.queue();
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

    @Override
    public void addCreateListener(CreateListener listener) {
        addFacesListener(listener);
    }

    @Override
    public void removeCreateListener(CreateListener listener) {
        removeFacesListener(listener);
    }

    @Override
    public CreateListener[] getCreateListeners() {
        return (CreateListener[]) getFacesListeners(CreateListener.class);
    }

    @Override
    public void addUpdateListener(UpdateListener listener) {
        addFacesListener(listener);
    }

    @Override
    public void removeUpdateListener(UpdateListener listener) {
        removeFacesListener(listener);
    }

    @Override
    public UpdateListener[] getUpdateListeners() {
        return (UpdateListener[]) getFacesListeners(UpdateListener.class);
    }

    @Override
    public void addDeleteListener(DeleteListener listener) {
        addFacesListener(listener);
    }

    @Override
    public void removeDeleteListener(DeleteListener listener) {
        removeFacesListener(listener);
    }

    @Override
    public DeleteListener[] getDeleteListeners() {
        return (DeleteListener[]) getFacesListeners(DeleteListener.class);
    }

    private class AjaxUpdateListener implements CreateListener, UpdateListener, DeleteListener {

        private final List<String> clientIds;

        public AjaxUpdateListener() {
            this.clientIds = new LinkedList<>();
        }

        public void addClientId(String clientId) {
            this.clientIds.add(clientId);
        }

        @Override
        public void entityCreated(CreateEvent event) {
            CRUDComponent.this.resetCache();
            Object entity = event.getEntity();
            fireUpdates(entity);
        }

        @Override
        public void entityUpdated(UpdateEvent event) {
            Object entity = event.getEntity();
            fireUpdates(entity);
            EntityInspector entityInspector = new EntityInspector(entity);
            String entityHumanReadable = entityInspector.toHumanReadable(entity);
            CRUDComponent.this.addMessage(FacesMessage.SEVERITY_INFO, "Updated " + entityHumanReadable);
        }

        private void fireUpdates(Object entity) {
            if (null == entity) {
                return;
            }
            PrimeFaces primeFaces = PrimeFaces.current();
            if (primeFaces.isAjaxRequest()) {
                LOGGER.debug("firing updates: {}", this.clientIds);
                primeFaces.ajax().update(this.clientIds);
            }
        }

        @Override
        public void entityDeleted(DeleteEvent event) {
            CRUDComponent.this.resetCache();
            Object entity = event.getEntity();
            fireUpdates(entity);
        }
    }
}
