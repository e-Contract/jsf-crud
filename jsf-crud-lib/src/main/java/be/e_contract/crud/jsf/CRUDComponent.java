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

import be.e_contract.crud.jsf.action.ActionAdapter;
import be.e_contract.crud.jsf.action.ActionComponent;
import be.e_contract.crud.jsf.action.GlobalActionAdapter;
import be.e_contract.crud.jsf.action.GlobalActionComponent;
import be.e_contract.crud.jsf.api.CRUD;
import be.e_contract.crud.jsf.api.CreateListener;
import be.e_contract.crud.jsf.api.DeleteListener;
import be.e_contract.crud.jsf.api.UpdateListener;
import be.e_contract.crud.jsf.component.CRUDCommandButton;
import be.e_contract.crud.jsf.component.ContainerComponent;
import be.e_contract.crud.jsf.component.DismissButton;
import be.e_contract.crud.jsf.component.EntityComponent;
import be.e_contract.crud.jsf.component.FieldComponent;
import be.e_contract.crud.jsf.component.LimitingOutputText;
import be.e_contract.crud.jsf.component.OrderComponent;
import be.e_contract.crud.jsf.component.PropertyComponent;
import be.e_contract.crud.jsf.component.QueryComponent;
import be.e_contract.crud.jsf.component.ReadComponent;
import be.e_contract.crud.jsf.converter.CalendarConverter;
import be.e_contract.crud.jsf.converter.EntityConverter;
import be.e_contract.crud.jsf.converter.TriStateBooleanConverter;
import be.e_contract.crud.jsf.create.CreateComponent;
import be.e_contract.crud.jsf.delete.DeleteComponent;
import be.e_contract.crud.jsf.el.CRUDELContext;
import be.e_contract.crud.jsf.el.CRUDFunctions;
import be.e_contract.crud.jsf.el.EntityFieldValueExpression;
import be.e_contract.crud.jsf.el.EntitySelectItemsValueExpression;
import be.e_contract.crud.jsf.el.EntityValueExpression;
import be.e_contract.crud.jsf.el.FieldStreamedContentValueExpression;
import be.e_contract.crud.jsf.el.FieldUploadMethodExpression;
import be.e_contract.crud.jsf.jpa.CRUDController;
import be.e_contract.crud.jsf.jpa.EntityInspector;
import be.e_contract.crud.jsf.update.UpdateComponent;
import be.e_contract.crud.jsf.validator.BeanValidationValidator;
import be.e_contract.crud.jsf.validator.NonExistingIdentifierValidator;
import be.e_contract.crud.jsf.validator.UniqueValidator;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.ClientBehavior;
import javax.faces.component.behavior.ClientBehaviorHolder;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesListener;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.validator.LengthValidator;
import javax.persistence.Basic;
import javax.persistence.ElementCollection;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
import org.primefaces.component.chips.Chips;
import org.primefaces.component.column.Column;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.datalist.DataList;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.dialog.Dialog;
import org.primefaces.component.fieldset.Fieldset;
import org.primefaces.component.filedownload.FileDownloadActionListener;
import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.message.Message;
import org.primefaces.component.outputlabel.OutputLabel;
import org.primefaces.component.password.Password;
import org.primefaces.component.resetinput.ResetInputActionListener;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.component.selectmanymenu.SelectManyMenu;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.component.spacer.Spacer;
import org.primefaces.component.tooltip.Tooltip;
import org.primefaces.component.tristatecheckbox.TriStateCheckbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FacesComponent(CRUDComponent.COMPONENT_TYPE)
@ResourceDependencies(value = {
    @ResourceDependency(library = "crud", name = "crud.js"),
    @ResourceDependency(library = "crud", name = "crud.css")
})
public class CRUDComponent extends UINamingContainer implements SystemEventListener, CRUD, Serializable, ClientBehaviorHolder {

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
        ordering,
        sort,
        roleAllowed,
        filter,
        hide,
    }

    public void setEntity(String entity) {
        LOGGER.debug("setEntity: {}", entity);
        getStateHelper().put(PropertyKeys.entity, entity);
    }

    private void updateEntityComponents(Object entity) {
        updateEntityComponents(entity, this);
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

    public Class<?> getEntityClass() {
        String entityClassName = getEntity();
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        EntityInspector entityInspector = new EntityInspector(entityManager, entityClassName);
        Class<?> entityClass = entityInspector.getEntityClass();
        return entityClass;
    }

    public void setTitle(String title) {
        getStateHelper().put(PropertyKeys.title, title);
    }

    public String getTitle() {
        return (String) getStateHelper().eval(PropertyKeys.title);
    }

    void setSelection(Object entity) {
        LOGGER.debug("setSelection: {}", entity);
        entity = eagerLoad(entity);
        getStateHelper().put(PropertyKeys.selection, entity);
        updateEntityComponents(entity);
    }

    public Object getSelection() {
        return getStateHelper().eval(PropertyKeys.selection);
    }

    public Object getNewEntity() {
        return getStateHelper().eval(PropertyKeys.newEntity);
    }

    public void setNewEntity(Object entity) {
        getStateHelper().put(PropertyKeys.newEntity, entity);
    }

    public String getOrderBy() {
        return (String) getStateHelper().get(PropertyKeys.orderBy);
    }

    public void setOrderBy(String orderBy) {
        getStateHelper().put(PropertyKeys.orderBy, orderBy);
    }

    public String getOrdering() {
        return (String) getStateHelper().get(PropertyKeys.ordering);
    }

    public void setOrdering(String ordering) {
        getStateHelper().put(PropertyKeys.ordering, ordering);
    }

    public boolean isAscending() {
        String ordering = getOrdering();
        if (UIInput.isEmpty(ordering)) {
            return true;
        }
        return !ordering.toLowerCase().equals("desc");
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

    public boolean isFilter() {
        Boolean filter = (Boolean) getStateHelper().get(PropertyKeys.filter);
        if (null == filter) {
            return false;
        }
        return filter;
    }

    public void setFilter(boolean filter) {
        getStateHelper().put(PropertyKeys.filter, filter);
    }

    public boolean isHide() {
        Boolean hide = (Boolean) getStateHelper().get(PropertyKeys.hide);
        if (null == hide) {
            return false;
        }
        return hide;
    }

    public void setHide(boolean hide) {
        getStateHelper().put(PropertyKeys.hide, hide);
    }

    public void setRoleAllowed(String roleAllowed) {
        getStateHelper().put(PropertyKeys.roleAllowed, roleAllowed);
    }

    public String getRoleAllowed() {
        return (String) getStateHelper().eval(PropertyKeys.roleAllowed);
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {

        for (UIComponent child : getChildren()) {
            if (child instanceof HtmlForm) {
                // already initialized
                return;
            }
        }

        FacesContext facesContext = getFacesContext();
        LOGGER.debug("constructing component");
        Application application = facesContext.getApplication();
        ELContext elContext = facesContext.getELContext();

        try {
            registerToHumanReadableFunction();
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.error("reflection error: " + ex.getMessage(), ex);
            throw new AbortProcessingException();
        }

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
        List<GlobalActionComponent> globalActions = new LinkedList<>();
        OrderComponent order = null;
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
                showView = !readComponent.isDisabled();
            } else if (child instanceof FieldComponent) {
                FieldComponent fieldComponent = (FieldComponent) child;
                fields.put(fieldComponent.getName(), fieldComponent);
            } else if (child instanceof ActionComponent) {
                ActionComponent action = (ActionComponent) child;
                actions.add(action);
            } else if (child instanceof PropertyComponent) {
                PropertyComponent propertyComponent = (PropertyComponent) child;
                properties.add(propertyComponent);
            } else if (child instanceof GlobalActionComponent) {
                GlobalActionComponent globalAction = (GlobalActionComponent) child;
                globalActions.add(globalAction);
            } else if (child instanceof OrderComponent) {
                order = (OrderComponent) child;
            }
        }

        String roleAllowed = getRoleAllowed();
        ExternalContext externalContext = facesContext.getExternalContext();
        if (UIInput.isEmpty(roleAllowed)) {
            roleAllowed = externalContext.getInitParameter("crud.roleAllowed");
        }
        if (!UIInput.isEmpty(roleAllowed)) {
            LOGGER.debug("role allowed: {}", roleAllowed);
            HttpServletRequest httpServletRequest = (HttpServletRequest) externalContext.getRequest();
            if (!httpServletRequest.isUserInRole(roleAllowed)) {
                LOGGER.warn("caller principal not in role: {}", roleAllowed);
                throw new AbortProcessingException();
            }
        }

        String entityClassName = getEntity();
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        EntityInspector entityInspector = new EntityInspector(entityManager, entityClassName);
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

        AjaxUpdateListener ajaxUpdateCreateListener = new AjaxUpdateListener(this);
        ajaxUpdateCreateListener.addClientId(message.getClientId());
        ajaxUpdateCreateListener.addClientId(dataTable.getClientId());
        addFacesListener(ajaxUpdateCreateListener);

        ValueExpression valueExpression = new EntityValueExpression(this);
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
        List<Field> idFields = entityInspector.getIdFields();
        for (Field idField : idFields) {
            addColumn(dataTable, idField, fields);
        }

        // next we add all the others
        List<Field> otherFields = entityInspector.getOtherFields();
        otherFields = order(otherFields, order);
        for (Field entityField : otherFields) {
            addColumn(dataTable, entityField, fields);
        }

        for (PropertyComponent property : properties) {
            addColumn(dataTable, property);
        }

        if (showDelete || showUpdate || showView || !actions.isEmpty()) {
            Column column = new Column();
            dataTable.getChildren().add(column);
            column.setHeaderText("Actions");

            addViewDialog(showView, readComponent, column, entityName, message, entityInspector, idFields, fields, order);

            addUpdateDialog(showUpdate, updateComponent, column, entityName, message, entityInspector, idFields, fields, updateFields, order);

            addDeleteDialog(showDelete, column, deleteComponent, entityName, message);

            addCustomActions(actions, column, dataTable, message);
        }

        boolean needFooter = false;
        if (showCreate) {
            needFooter = true;
        }
        if (null != deleteComponent && deleteComponent.isDeleteAll()) {
            needFooter = true;
        }
        if (!globalActions.isEmpty()) {
            needFooter = true;
        }
        if (needFooter) {
            HtmlPanelGroup footerHtmlPanelGroup = (HtmlPanelGroup) application.createComponent(HtmlPanelGroup.COMPONENT_TYPE);
            dataTable.getFacets().put("footer", footerHtmlPanelGroup);
            footerHtmlPanelGroup.setStyle("display:block; text-align: left;");

            addCreateDialog(showCreate, createComponent, footerHtmlPanelGroup, entityName, message, idFields, entityInspector, fields, createFields, order);

            addDeleteAllDialog(deleteComponent, footerHtmlPanelGroup, message, dataTable);

            int globalActionIdx = 1;
            for (GlobalActionComponent globalAction : globalActions) {
                addGlobalAction(globalAction, globalActionIdx, dataTable, message, footerHtmlPanelGroup);
                globalActionIdx++;
            }
        }
    }

    private Dialog createDialog() {
        FacesContext facesContext = getFacesContext();
        Application application = facesContext.getApplication();
        ExternalContext externalContext = facesContext.getExternalContext();
        Dialog dialog = (Dialog) application.createComponent(Dialog.COMPONENT_TYPE);
        dialog.setModal(true);
        dialog.setDynamic(true);
        String closable = externalContext.getInitParameter("crud.dialog.closable");
        if (null != closable) {
            dialog.setClosable(Boolean.parseBoolean(closable));
        }
        return dialog;
    }

    private void addDeleteAllDialog(DeleteComponent deleteComponent, HtmlPanelGroup footerHtmlPanelGroup, Message message, DataTable dataTable) throws FacesException {
        if (null == deleteComponent) {
            return;
        }
        if (!deleteComponent.isDeleteAll()) {
            return;
        }
        FacesContext facesContext = getFacesContext();
        Application application = facesContext.getApplication();
        ExternalContext externalContext = facesContext.getExternalContext();

        CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        footerHtmlPanelGroup.getChildren().add(commandButton);
        commandButton.setValue("Delete all...");
        commandButton.setOncomplete("PF('deleteAllDialog').show()");
        commandButton.setId("deleteAllButton");
        commandButton.setUpdate(message.getClientId());
        commandButton.setIcon(deleteComponent.getIcon());

        Dialog deleteAllDialog = createDialog();
        getChildren().add(deleteAllDialog);
        deleteAllDialog.setWidgetVar("deleteAllDialog");
        deleteAllDialog.setId("deleteAllDialog");
        deleteAllDialog.setHeader("Delete all?");

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
        deleteCommandButton.addActionListener(new DeleteAllActionListener(this));
        deleteCommandButton.setOncomplete("PF('deleteAllDialog').hide()");
        deleteCommandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId());
        String deleteButtonIcon = externalContext.getInitParameter("crud.dialog.deleteButton.icon");
        deleteCommandButton.setIcon(deleteButtonIcon);

        DismissButton dismissCommandButton = (DismissButton) application.createComponent(DismissButton.COMPONENT_TYPE);
        htmlPanelGrid.getChildren().add(dismissCommandButton);
    }

    private void addCustomActions(List<ActionComponent> actions, Column column, DataTable dataTable, Message message) throws FacesException {
        FacesContext facesContext = getFacesContext();
        Application application = facesContext.getApplication();
        int actionIdx = 0;
        for (ActionComponent action : actions) {
            CRUDCommandButton commandButton = (CRUDCommandButton) application.createComponent(CRUDCommandButton.COMPONENT_TYPE);
            column.getChildren().add(commandButton);
            commandButton.setValue(action.getValue());
            commandButton.setId("Action" + actionIdx);
            commandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId());
            commandButton.addActionListener(new ActionAdapter(action.getAction(), action.getUpdate(), this));
            commandButton.setOncomplete(action.getOncomplete());
            commandButton.setIcon(action.getIcon());
            commandButton.setAjax(action.isAjax());

            UIComponent actionDialogComponent = action.getFacet("dialog");
            if (null != actionDialogComponent) {
                Dialog customActionDialog = createDialog();
                getChildren().add(customActionDialog);
                customActionDialog.setId("ActionDialog" + actionIdx);
                customActionDialog.setWidgetVar("ActionDialog" + actionIdx);
                customActionDialog.setHeader(action.getValue());

                UIComponent actionDialogTitleComponent = action.getFacet("dialogTitle");
                if (null != actionDialogTitleComponent) {
                    String dialogTitle = actionDialogTitleComponent.toString().trim();
                    customActionDialog.setHeader(dialogTitle);
                }

                customActionDialog.getChildren().add(actionDialogComponent);

                commandButton.setOncomplete("PF('" + "ActionDialog" + actionIdx + "').show()");
                commandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId() + "," + actionDialogComponent.getClientId());
                commandButton.addActionListener(new SelectRowActionListener(this));
            }

            ValueExpression renderedValueExpression = action.getRenderedValueExpression();
            commandButton.setRenderedValueExpression(renderedValueExpression);

            String update = action.getUpdate();
            if (null != update) {
                UIViewRoot view = facesContext.getViewRoot();
                UIComponent component = view.findComponent(update);
                commandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId() + "," + component.getClientId());
            }

            ValueExpression fileDownloadValueExpression = action.findDownloadValueExpression();
            if (null != fileDownloadValueExpression) {
                commandButton.addActionListener(new FileDownloadActionListener(fileDownloadValueExpression, null, null));
                commandButton.setAjax(false);
            }

            if (!UIInput.isEmpty(action.getTooltip())) {
                Tooltip tooltip = (Tooltip) application.createComponent(Tooltip.COMPONENT_TYPE);
                column.getChildren().add(tooltip);
                tooltip.setFor("Action" + actionIdx);
                tooltip.setValue(action.getTooltip());
            }
            actionIdx++;
        }
    }

    private void addGlobalAction(GlobalActionComponent globalAction, int globalActionIdx, DataTable dataTable, Message message, HtmlPanelGroup footerHtmlPanelGroup) {
        FacesContext facesContext = getFacesContext();
        Application application = facesContext.getApplication();
        CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        footerHtmlPanelGroup.getChildren().add(commandButton);
        commandButton.setId("GlobalAction" + globalActionIdx);
        commandButton.setValue(globalAction.getValue());
        commandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId());
        commandButton.setOncomplete(globalAction.getOncomplete());
        commandButton.setIcon(globalAction.getIcon());
        commandButton.setAjax(globalAction.isAjax());

        UIComponent actionDialogComponent = globalAction.getFacet("dialog");
        if (null != actionDialogComponent) {
            Dialog customActionDialog = createDialog();
            getChildren().add(customActionDialog);
            customActionDialog.setId("GlobalActionDialog" + globalActionIdx);
            customActionDialog.setWidgetVar("GlobalActionDialog" + globalActionIdx);
            customActionDialog.setHeader(globalAction.getValue());

            UIComponent actionDialogTitleComponent = globalAction.getFacet("dialogTitle");
            if (null != actionDialogTitleComponent) {
                String dialogTitle = actionDialogTitleComponent.toString().trim();
                customActionDialog.setHeader(dialogTitle);
            }

            customActionDialog.getChildren().add(actionDialogComponent);

            commandButton.setOncomplete("PF('" + "GlobalActionDialog" + globalActionIdx + "').show()");
            commandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId() + "," + actionDialogComponent.getClientId());
        }

        String update = globalAction.getUpdate();
        if (null != update) {
            UIViewRoot view = facesContext.getViewRoot();
            UIComponent component = view.findComponent(update);
            commandButton.setUpdate(dataTable.getClientId() + "," + message.getClientId() + "," + component.getClientId());
        }
        commandButton.addActionListener(new GlobalActionAdapter(globalAction.getAction()));
        ValueExpression fileDownloadValueExpression = globalAction.findDownloadValueExpression();
        if (null != fileDownloadValueExpression) {
            commandButton.addActionListener(new FileDownloadActionListener(fileDownloadValueExpression, null, null));
            commandButton.setAjax(false);
        }

        if (!UIInput.isEmpty(globalAction.getTooltip())) {
            Tooltip tooltip = (Tooltip) application.createComponent(Tooltip.COMPONENT_TYPE);
            footerHtmlPanelGroup.getChildren().add(tooltip);
            tooltip.setFor("GlobalAction" + globalActionIdx);
            tooltip.setValue(globalAction.getTooltip());
        }
    }

    private void addCreateDialog(boolean showCreate, CreateComponent createComponent, HtmlPanelGroup footerHtmlPanelGroup, String entityName,
            Message message, List<Field> idFields, EntityInspector entityInspector,
            Map<String, FieldComponent> fields, Map<String, FieldComponent> createFields, OrderComponent order) throws FacesException {
        if (!showCreate) {
            return;
        }
        FacesContext facesContext = getFacesContext();
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();

        CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        footerHtmlPanelGroup.getChildren().add(commandButton);
        String value;
        if (null != createComponent) {
            value = createComponent.getValue();
            if (null == value) {
                value = "Add...";
            } else if ("".equals(value)) {
                value = null;
            }
        } else {
            value = "Add...";
        }
        commandButton.setValue(value);
        commandButton.setOncomplete("PF('addDialog').show()");
        commandButton.setId("addButton");
        if (null != createComponent) {
            commandButton.setIcon(createComponent.getIcon());

            if (!UIInput.isEmpty(createComponent.getTooltip())) {
                Tooltip tooltip = (Tooltip) application.createComponent(Tooltip.COMPONENT_TYPE);
                footerHtmlPanelGroup.getChildren().add(tooltip);
                tooltip.setFor("addButton");
                tooltip.setValue(createComponent.getTooltip());
            }
        }

        Dialog addDialog = createDialog();
        getChildren().add(addDialog);
        addDialog.setWidgetVar("addDialog");
        addDialog.setId("addDialog");
        addDialog.setHeader("Add " + entityName);

        HtmlForm addDialogHtmlForm = (HtmlForm) application.createComponent(HtmlForm.COMPONENT_TYPE);
        addDialog.getChildren().add(addDialogHtmlForm);
        addDialogHtmlForm.setId("addForm");

        commandButton.setUpdate(addDialog.getClientId() + "," + message.getClientId());
        commandButton.addActionListener(new ResetInputActionListener(expressionFactory.createValueExpression(addDialogHtmlForm.getClientId(), String.class), null));

        HtmlPanelGrid htmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        addDialogHtmlForm.getChildren().add(htmlPanelGrid);
        htmlPanelGrid.setColumns(3);

        boolean isIdGeneratedValue = entityInspector.isIdGeneratedValue();
        if (!isIdGeneratedValue) {
            if (!entityInspector.isEmbeddedIdField()) {
                for (Field idField : idFields) {
                    UIInput input = addInputComponent(idField, null, true, entityInspector, fields, createFields, htmlPanelGrid, true);
                    if (idFields.size() == 1) {
                        // TODO: validate multiple ID fields together
                        input.addValidator(new NonExistingIdentifierValidator(this));
                    }
                }
            } else {
                Field idField = idFields.iterator().next();
                Map<String, FieldComponent> embeddableFields = getEmbeddableFields(idField, fields);
                for (Field embeddableIdField : idField.getType().getDeclaredFields()) {
                    if (Modifier.isStatic(embeddableIdField.getModifiers())) {
                        continue;
                    }
                    if (embeddableIdField.getName().startsWith("_persistence_")) {
                        // payara
                        continue;
                    }
                    addInputComponent(idField, embeddableIdField, true, entityInspector, embeddableFields, null, htmlPanelGrid, true);
                }
            }
        }

        List<Field> otherFields = entityInspector.getOtherFields();
        OrderComponent overrideOrder;
        if (null != createComponent) {
            overrideOrder = createComponent.findOrderComponent();
        } else {
            overrideOrder = null;
        }
        otherFields = order(otherFields, order, overrideOrder);
        for (Field entityField : otherFields) {
            addInputComponent(entityField, null, true, entityInspector, fields, createFields, htmlPanelGrid, false);
        }

        for (Field embeddedField : entityInspector.getEmbeddedFields()) {
            Fieldset fieldset = (Fieldset) application.createComponent(Fieldset.COMPONENT_TYPE);
            addDialogHtmlForm.getChildren().add(fieldset);
            fieldset.setLegend(getFieldLabel(embeddedField, fields));

            HtmlPanelGrid embeddedHtmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
            fieldset.getChildren().add(embeddedHtmlPanelGrid);
            embeddedHtmlPanelGrid.setColumns(3);

            Map<String, FieldComponent> embeddableFields = getEmbeddableFields(embeddedField, fields);

            for (Field embeddableField : embeddedField.getType().getDeclaredFields()) {
                if (Modifier.isStatic(embeddableField.getModifiers())) {
                    continue;
                }
                if (embeddableField.getName().startsWith("_persistence_")) {
                    // payara
                    continue;
                }
                addInputComponent(embeddedField, embeddableField, true, entityInspector, embeddableFields, null, embeddedHtmlPanelGrid, false);
            }
        }

        HtmlPanelGrid buttonHtmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        addDialogHtmlForm.getChildren().add(buttonHtmlPanelGrid);
        buttonHtmlPanelGrid.setColumns(2);

        CommandButton addCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        buttonHtmlPanelGrid.getChildren().add(addCommandButton);
        addCommandButton.setId("addButton");
        addCommandButton.setValue("Add");
        addCommandButton.setOncomplete("crudDialogResponse(args, 'addDialog')");
        addCommandButton.addActionListener(new AddActionListener(this));
        addCommandButton.setUpdate(addDialogHtmlForm.getClientId());
        ExternalContext externalContext = facesContext.getExternalContext();
        String addButtonIcon = externalContext.getInitParameter("crud.dialog.createButton.icon");
        addCommandButton.setIcon(addButtonIcon);

        DismissButton dismissCommandButton = (DismissButton) application.createComponent(DismissButton.COMPONENT_TYPE);
        buttonHtmlPanelGrid.getChildren().add(dismissCommandButton);
    }

    private void addDeleteDialog(boolean showDelete, Column column, DeleteComponent deleteComponent,
            String entityName,
            Message message) throws FacesException {
        if (!showDelete) {
            return;
        }
        FacesContext facesContext = getFacesContext();
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();

        CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        column.getChildren().add(commandButton);

        String value;
        if (null != deleteComponent) {
            value = deleteComponent.getValue();
            if (null == value) {
                value = "Delete...";
            } else if ("".equals(value)) {
                value = null;
            }
        } else {
            value = "Delete...";
        }
        commandButton.setValue(value);

        commandButton.setId("deleteButton");
        commandButton.setOncomplete("PF('deleteDialog').show()");
        if (null != deleteComponent) {
            commandButton.setIcon(deleteComponent.getIcon());

            if (!UIInput.isEmpty(deleteComponent.getTooltip())) {
                Tooltip tooltip = (Tooltip) application.createComponent(Tooltip.COMPONENT_TYPE);
                column.getChildren().add(tooltip);
                tooltip.setFor("deleteButton");
                tooltip.setValue(deleteComponent.getTooltip());
            }
        }

        Dialog deleteDialog = createDialog();
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

        if (!relocateChildren(application, deleteComponent, deleteDialog)) {
            EntityComponent entityComponent = (EntityComponent) application.createComponent(EntityComponent.COMPONENT_TYPE);
            deleteDialog.getChildren().add(entityComponent);
            entityComponent.setVar("entity");
            HtmlOutputText htmlOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);

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
        deleteCommandButton.addActionListener(new DeleteActionListener(this));
        deleteCommandButton.setOncomplete("PF('deleteDialog').hide()");
        ExternalContext externalContext = facesContext.getExternalContext();
        String deleteButtonIcon = externalContext.getInitParameter("crud.dialog.deleteButton.icon");
        deleteCommandButton.setIcon(deleteButtonIcon);

        DismissButton dismissCommandButton = (DismissButton) application.createComponent(DismissButton.COMPONENT_TYPE);
        htmlPanelGrid.getChildren().add(dismissCommandButton);

        commandButton.setUpdate(deleteDialog.getClientId() + "," + message.getClientId());
        commandButton.addActionListener(new SelectRowActionListener(this));
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

    private void addUpdateDialog(boolean showUpdate, UpdateComponent updateComponent, Column column, String entityName, Message message,
            EntityInspector entityInspector, List<Field> idFields, Map<String, FieldComponent> fields,
            Map<String, FieldComponent> updateFields, OrderComponent order) throws FacesException {
        if (!showUpdate) {
            return;
        }
        FacesContext facesContext = getFacesContext();
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();

        CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        column.getChildren().add(commandButton);

        String value;
        if (null != updateComponent) {
            value = updateComponent.getValue();
            if (null == value) {
                value = "Update...";
            } else if ("".equals(value)) {
                value = null;
            }
        } else {
            value = "Update...";
        }
        commandButton.setValue(value);

        commandButton.setOncomplete("PF('updateDialog').show()");
        commandButton.setId("updateButton");
        if (null != updateComponent) {
            commandButton.setIcon(updateComponent.getIcon());

            if (!UIInput.isEmpty(updateComponent.getTooltip())) {
                Tooltip tooltip = (Tooltip) application.createComponent(Tooltip.COMPONENT_TYPE);
                column.getChildren().add(tooltip);
                tooltip.setFor("updateButton");
                tooltip.setValue(updateComponent.getTooltip());
            }
        }

        Dialog updateDialog = createDialog();
        getChildren().add(updateDialog);
        updateDialog.setWidgetVar("updateDialog");
        updateDialog.setId("updateDialog");
        updateDialog.setHeader("Update " + entityName);

        HtmlForm updateDialogHtmlForm = (HtmlForm) application.createComponent(HtmlForm.COMPONENT_TYPE);
        updateDialog.getChildren().add(updateDialogHtmlForm);
        updateDialogHtmlForm.setId("updateForm");

        commandButton.setUpdate(updateDialog.getClientId() + "," + message.getClientId());
        commandButton.addActionListener(new SelectRowActionListener(this));
        commandButton.addActionListener(new ResetInputActionListener(expressionFactory.createValueExpression(updateDialogHtmlForm.getClientId(), String.class), null));

        HtmlPanelGrid htmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        updateDialogHtmlForm.getChildren().add(htmlPanelGrid);
        htmlPanelGrid.setColumns(3);

        for (Field idField : idFields) {
            if (!isHideField(idField, fields, updateFields)) {
                OutputLabel idOutputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(idOutputLabel);
                idOutputLabel.setValue(EntityInspector.toHumanReadable(idField));

                LimitingOutputText identifierOutputText = (LimitingOutputText) application.createComponent(LimitingOutputText.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(identifierOutputText);
                identifierOutputText.setValueExpression("value", new EntityFieldValueExpression(this, idField, null, false));

                HtmlOutputText voidOutputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(voidOutputText);
            }
        }

        List<Field> otherFields = entityInspector.getOtherFields();
        OrderComponent overrideOrder;
        if (null != updateComponent) {
            overrideOrder = updateComponent.findOrderComponent();
        } else {
            overrideOrder = null;
        }
        otherFields = order(otherFields, order, overrideOrder);
        for (Field entityField : otherFields) {
            addInputComponent(entityField, null, false, entityInspector, fields, updateFields, htmlPanelGrid, false);
        }

        for (Field embeddedField : entityInspector.getEmbeddedFields()) {
            Fieldset fieldset = (Fieldset) application.createComponent(Fieldset.COMPONENT_TYPE);
            updateDialogHtmlForm.getChildren().add(fieldset);
            fieldset.setLegend(getFieldLabel(embeddedField, fields));

            HtmlPanelGrid embeddedHtmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
            fieldset.getChildren().add(embeddedHtmlPanelGrid);
            embeddedHtmlPanelGrid.setColumns(3);

            Map<String, FieldComponent> embeddableFields = getEmbeddableFields(embeddedField, fields);

            for (Field embeddableField : embeddedField.getType().getDeclaredFields()) {
                if (Modifier.isStatic(embeddableField.getModifiers())) {
                    continue;
                }
                if (embeddableField.getName().startsWith("_persistence_")) {
                    // payara
                    continue;
                }
                addInputComponent(embeddedField, embeddableField, false, entityInspector, embeddableFields, null, embeddedHtmlPanelGrid, false);
            }
        }

        HtmlPanelGrid buttonHtmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        updateDialogHtmlForm.getChildren().add(buttonHtmlPanelGrid);
        buttonHtmlPanelGrid.setColumns(2);

        CommandButton saveCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        buttonHtmlPanelGrid.getChildren().add(saveCommandButton);
        saveCommandButton.setId("saveButton");
        saveCommandButton.setValue("Save");
        saveCommandButton.setOncomplete("crudDialogResponse(args, 'updateDialog')");
        saveCommandButton.addActionListener(new SaveActionListener(this));
        saveCommandButton.setUpdate(updateDialogHtmlForm.getClientId());
        ExternalContext externalContext = facesContext.getExternalContext();
        String saveButtonIcon = externalContext.getInitParameter("crud.dialog.updateButton.icon");
        saveCommandButton.setIcon(saveButtonIcon);

        DismissButton dismissCommandButton = (DismissButton) application.createComponent(DismissButton.COMPONENT_TYPE);
        buttonHtmlPanelGrid.getChildren().add(dismissCommandButton);
    }

    private void addViewDialog(boolean showView, ReadComponent readComponent,
            Column column, String entityName, Message message, EntityInspector entityInspector,
            List<Field> idFields, Map<String, FieldComponent> fields, OrderComponent order) throws FacesException {
        if (!showView) {
            return;
        }
        FacesContext facesContext = getFacesContext();
        Application application = facesContext.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ELContext elContext = facesContext.getELContext();
        CommandButton commandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
        column.getChildren().add(commandButton);

        String value = readComponent.getValue();
        if (null == value) {
            value = "View...";
        } else if ("".equals(value)) {
            value = null;
        }
        commandButton.setValue(value);

        commandButton.setOncomplete("PF('viewDialog').show()");
        commandButton.setId("viewButton");
        commandButton.setIcon(readComponent.getIcon());
        if (!UIInput.isEmpty(readComponent.getTooltip())) {
            Tooltip tooltip = (Tooltip) application.createComponent(Tooltip.COMPONENT_TYPE);
            column.getChildren().add(tooltip);
            tooltip.setFor("viewButton");
            tooltip.setValue(readComponent.getTooltip());
        }

        Dialog viewDialog = createDialog();
        getChildren().add(viewDialog);
        viewDialog.setWidgetVar("viewDialog");
        viewDialog.setId("viewDialog");
        viewDialog.setHeader("View " + entityName);

        HtmlForm viewDialogHtmlForm = (HtmlForm) application.createComponent(HtmlForm.COMPONENT_TYPE);
        viewDialog.getChildren().add(viewDialogHtmlForm);
        viewDialogHtmlForm.setId("viewForm");

        commandButton.setUpdate(viewDialog.getClientId() + "," + message.getClientId());
        commandButton.addActionListener(new SelectRowActionListener(this));

        HtmlPanelGrid htmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        viewDialogHtmlForm.getChildren().add(htmlPanelGrid);
        htmlPanelGrid.setColumns(2);

        for (Field idField : idFields) {
            OutputLabel idOutputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(idOutputLabel);
            idOutputLabel.setValue(EntityInspector.toHumanReadable(idField));

            LimitingOutputText identifierOutputText = (LimitingOutputText) application.createComponent(LimitingOutputText.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(identifierOutputText);
            identifierOutputText.setValueExpression("value", new EntityFieldValueExpression(this, idField, null, false));
            if (isPasswordField(idField, fields)) {
                identifierOutputText.setPassword(true);
            }
        }

        List<Field> otherFields = entityInspector.getOtherFields();
        OrderComponent overrideOrder = readComponent.findOrderComponent();
        otherFields = order(otherFields, order, overrideOrder);
        for (Field entityField : otherFields) {
            String fieldLabel = getFieldLabel(entityField, fields);
            OutputLabel outputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(outputLabel);
            outputLabel.setValue(fieldLabel);

            if (entityField.getType().isAssignableFrom(List.class)) {
                DataList dataList = (DataList) application.createComponent(DataList.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(dataList);
                dataList.setValueExpression("value", new EntityFieldValueExpression(this, entityField, null, false));
                dataList.setVar("entity");
                dataList.setStyleClass("crudDataList");

                HtmlOutputText outputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
                dataList.getChildren().add(outputText);
                ValueExpression outputTextValueExpression = expressionFactory.createValueExpression(new CRUDELContext(elContext), "#{crud:toHumanReadable(entity)}", String.class);
                outputText.setValueExpression("value", outputTextValueExpression);
            } else if (entityField.getType().equals(byte[].class)) {
                CommandButton downloadCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(downloadCommandButton);
                downloadCommandButton.setId(entityField.getName() + "Download");
                downloadCommandButton.setValue("Download");
                downloadCommandButton.setUpdate(viewDialogHtmlForm.getClientId());
                downloadCommandButton.setAjax(false);
                String contentType = getContentType(entityField, fields);
                ValueExpression fieldStreamedContentValueExpression = new FieldStreamedContentValueExpression(this, entityField.getName(), contentType);
                FileDownloadActionListener fileDownloadActionListener = new FileDownloadActionListener(fieldStreamedContentValueExpression, null, null);
                downloadCommandButton.addActionListener(fileDownloadActionListener);
            } else {
                LimitingOutputText outputText = (LimitingOutputText) application.createComponent(LimitingOutputText.COMPONENT_TYPE);
                htmlPanelGrid.getChildren().add(outputText);
                outputText.setValueExpression("value", new EntityFieldValueExpression(this, entityField, null, false));
                if (isPasswordField(entityField, fields)) {
                    outputText.setPassword(true);
                }
            }
        }

        for (Field embeddedField : entityInspector.getEmbeddedFields()) {
            Fieldset fieldset = (Fieldset) application.createComponent(Fieldset.COMPONENT_TYPE);
            viewDialogHtmlForm.getChildren().add(fieldset);
            fieldset.setLegend(getFieldLabel(embeddedField, fields));

            HtmlPanelGrid embeddedHtmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
            fieldset.getChildren().add(embeddedHtmlPanelGrid);
            embeddedHtmlPanelGrid.setColumns(2);

            Map<String, FieldComponent> embeddableFields = getEmbeddableFields(embeddedField, fields);

            for (Field embeddableField : embeddedField.getType().getDeclaredFields()) {
                if (Modifier.isStatic(embeddableField.getModifiers())) {
                    continue;
                }
                if (embeddableField.getName().startsWith("_persistence_")) {
                    // payara
                    continue;
                }

                String fieldLabel = getFieldLabel(embeddableField, embeddableFields);
                OutputLabel outputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
                embeddedHtmlPanelGrid.getChildren().add(outputLabel);
                outputLabel.setValue(fieldLabel);

                LimitingOutputText outputText = (LimitingOutputText) application.createComponent(LimitingOutputText.COMPONENT_TYPE);
                embeddedHtmlPanelGrid.getChildren().add(outputText);
                outputText.setValueExpression("value", new EntityFieldValueExpression(this, embeddedField, embeddableField, false));
                if (isPasswordField(embeddableField, fields)) {
                    outputText.setPassword(true);
                }
            }
        }

        HtmlPanelGrid buttonHtmlPanelGrid = (HtmlPanelGrid) application.createComponent(HtmlPanelGrid.COMPONENT_TYPE);
        viewDialogHtmlForm.getChildren().add(buttonHtmlPanelGrid);
        buttonHtmlPanelGrid.setColumns(1);

        DismissButton dismissCommandButton = (DismissButton) application.createComponent(DismissButton.COMPONENT_TYPE);
        buttonHtmlPanelGrid.getChildren().add(dismissCommandButton);
    }

    private void registerToHumanReadableFunction() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        FacesContext facesContext = getFacesContext();
        ELContext elContext = facesContext.getELContext();
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

    private List<Field> order(List<Field> fields, OrderComponent orderComponent, OrderComponent overrideOrderComponent) {
        if (null == overrideOrderComponent) {
            return order(fields, orderComponent);
        }
        return order(fields, overrideOrderComponent);
    }

    private List<Field> order(List<Field> fields, OrderComponent orderComponent) {
        if (null == orderComponent) {
            return fields;
        }
        List<FieldComponent> order = orderComponent.getOrder();
        if (order.isEmpty()) {
            return fields;
        }

        Map<String, Field> fieldsMap = new HashMap<>();
        for (Field field : fields) {
            fieldsMap.put(field.getName(), field);
        }

        List<Field> orderedFields = new LinkedList<>();
        for (FieldComponent orderFieldComponent : order) {
            Field field = fieldsMap.remove(orderFieldComponent.getName());
            if (null == field) {
                continue;
            }
            orderedFields.add(field);
            fields.remove(field);
        }

        for (Field field : fields) {
            orderedFields.add(field);
        }
        return orderedFields;
    }

    private String getFieldLabel(Field entityField, Map<String, FieldComponent> fields) {
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null != fieldComponent) {
            if (!UIInput.isEmpty(fieldComponent.getLabel())) {
                return fieldComponent.getLabel();
            }
        }
        return EntityInspector.toHumanReadable(entityField);
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
            return isHide();
        }
        Boolean hide = fieldComponent.isHide();
        if (null == hide) {
            return isHide();
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

    private boolean isRequiredField(Field entityField, Map<String, FieldComponent> fields, Map<String, FieldComponent> overrideFields) {
        if (null != overrideFields) {
            FieldComponent fieldComponent = overrideFields.get(entityField.getName());
            if (null != fieldComponent) {
                Boolean required = fieldComponent.isRequired();
                if (null != required) {
                    return required;
                }
            }
        }
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return false;
        }
        Boolean required = fieldComponent.isRequired();
        if (null == required) {
            return false;
        }
        return required;
    }

    private boolean isSortProperty(PropertyComponent property) {
        Boolean sort = property.isSort();
        if (null == sort) {
            return isSort();
        }
        return sort;
    }

    private boolean isFilterProperty(PropertyComponent property) {
        Boolean filter = property.isFilter();
        if (null == filter) {
            return isFilter();
        }
        return filter;
    }

    private boolean isFilterField(Field entityField, Map<String, FieldComponent> fields) {
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return isFilter();
        }
        return fieldComponent.isFilter();
    }

    private String getContentType(Field entityField, Map<String, FieldComponent> fields) {
        final String defaultContentType = "application/octet-stream";
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return defaultContentType;
        }
        String contentType = fieldComponent.getBinaryContentType();
        if (null != contentType) {
            return contentType;
        }
        return defaultContentType;
    }

    private boolean isPasswordField(Field entityField, Map<String, FieldComponent> fields) {
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return false;
        }
        return fieldComponent.isPasswordField();
    }

    private boolean isFeedbackPassword(Field entityField, Map<String, FieldComponent> fields) {
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return false;
        }
        return fieldComponent.isFeedbackPassword();
    }

    private boolean isMatchPassword(Field entityField, Map<String, FieldComponent> fields) {
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return false;
        }
        return fieldComponent.isMatchPassword();
    }

    private Map<String, FieldComponent> getEmbeddableFields(Field entityField, Map<String, FieldComponent> fields) {
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return Collections.EMPTY_MAP;
        }
        return fieldComponent.getEmbeddableFields();
    }

    private Integer getFieldSize(Field entityField, Map<String, FieldComponent> fields, Map<String, FieldComponent> overrideFields) {
        if (null != overrideFields) {
            FieldComponent fieldComponent = overrideFields.get(entityField.getName());
            if (null != fieldComponent) {
                Integer size = fieldComponent.getSize();
                if (null != size) {
                    return size;
                }
            }
        }
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return null;
        }
        Integer size = fieldComponent.getSize();
        return size;
    }

    private QueryComponent getFieldQueryComponent(Field entityField, Map<String, FieldComponent> fields, Map<String, FieldComponent> overrideFields) {
        if (null != overrideFields) {
            FieldComponent fieldComponent = overrideFields.get(entityField.getName());
            if (null != fieldComponent) {
                return fieldComponent.getQueryComponent();
            }
        }
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return null;
        }
        return fieldComponent.getQueryComponent();
    }

    private UIInput getFieldInputComponent(Field entityField, Map<String, FieldComponent> fields) {
        if (fields == null) {
            return null;
        }
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return null;
        }
        return fieldComponent.getFieldInputComponent();
    }

    private UIInput addInputComponent(Field entityField, Field embeddableField, boolean addNotUpdate, EntityInspector entityInspector,
            Map<String, FieldComponent> fields, Map<String, FieldComponent> overrideFields, HtmlPanelGrid htmlPanelGrid, boolean forceRender) {
        if (isHideField(entityField, fields, overrideFields) && !forceRender) {
            return null;
        }
        JoinColumn joinColumnAnnotation = entityInspector.getAnnotation(entityField, embeddableField, JoinColumn.class);
        if (addNotUpdate) {
            if (null != joinColumnAnnotation) {
                if (!joinColumnAnnotation.insertable()) {
                    return null;
                }
            }
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Application application = facesContext.getApplication();

        String fieldLabel;
        String inputId;
        if (null == embeddableField) {
            inputId = entityField.getName();
            fieldLabel = getFieldLabel(entityField, fields);
        } else {
            inputId = entityField.getName() + embeddableField.getName();
            fieldLabel = getFieldLabel(embeddableField, fields);
        }

        OutputLabel outputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
        htmlPanelGrid.getChildren().add(outputLabel);
        outputLabel.setValue(fieldLabel);
        outputLabel.setFor(inputId);

        boolean disabled = false;
        javax.persistence.Column columnAnnotation = entityInspector.getAnnotation(entityField, embeddableField, javax.persistence.Column.class);
        if (!addNotUpdate) {
            if (null != columnAnnotation) {
                if (!columnAnnotation.updatable()) {
                    disabled = true;
                }
            }
            if (null != joinColumnAnnotation) {
                if (!joinColumnAnnotation.updatable()) {
                    disabled = true;
                }
            }
        }

        Field actualField;
        if (null == embeddableField) {
            actualField = entityField;
        } else {
            actualField = embeddableField;
        }

        UIInput input;
        OneToOne oneToOneAnnotation = entityInspector.getAnnotation(entityField, embeddableField, OneToOne.class);
        ManyToOne manyToOneAnnotation = entityInspector.getAnnotation(entityField, embeddableField, ManyToOne.class);
        OneToMany oneToManyAnnotation = entityInspector.getAnnotation(entityField, embeddableField, OneToMany.class);
        ManyToMany manyToManyAnnotation = entityInspector.getAnnotation(entityField, embeddableField, ManyToMany.class);
        ElementCollection elementCollectionAnnotation = entityInspector.getAnnotation(entityField, ElementCollection.class);
        input = getFieldInputComponent(entityField, overrideFields);
        if (null != input) {
            LOGGER.debug("custom input component {} for field {}", input, entityField.getName());
            htmlPanelGrid.getChildren().add(input);
        } else if (null != elementCollectionAnnotation) {
            input = (Chips) application.createComponent(Chips.COMPONENT_TYPE);
            Chips chips = (Chips) input;
            chips.setDisabled(disabled);
            htmlPanelGrid.getChildren().add(input);
        } else if (null != manyToManyAnnotation) {
            input = (SelectManyMenu) application.createComponent(SelectManyMenu.COMPONENT_TYPE);
            SelectManyMenu selectManyMenu = (SelectManyMenu) input;
            selectManyMenu.setDisabled(disabled);
            selectManyMenu.setShowCheckbox(true);
            UISelectItems selectItems = (UISelectItems) application.createComponent(UISelectItems.COMPONENT_TYPE);
            input.getChildren().add(selectItems);
            Type type = actualField.getGenericType();
            LOGGER.debug("type class: {}", type.getClass().getName());
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> listTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            selectManyMenu.setConverter(new EntityConverter());
            QueryComponent queryComponent = getFieldQueryComponent(actualField, fields, overrideFields);
            if (null != queryComponent) {
                selectItems.setValueExpression("value", new EntitySelectItemsValueExpression(this, queryComponent));
            } else {
                selectItems.setValueExpression("value", new EntitySelectItemsValueExpression(listTypeClass));
            }
            htmlPanelGrid.getChildren().add(input);
        } else if (null != manyToOneAnnotation || null != oneToOneAnnotation) {
            input = (SelectOneMenu) application.createComponent(SelectOneMenu.COMPONENT_TYPE);
            SelectOneMenu selectOneMenu = (SelectOneMenu) input;
            selectOneMenu.setDisabled(disabled);
            UISelectItem emptySelectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
            input.getChildren().add(emptySelectItem);
            input.setConverter(new EntityConverter());

            UISelectItems selectItems = (UISelectItems) application.createComponent(UISelectItems.COMPONENT_TYPE);
            input.getChildren().add(selectItems);
            QueryComponent queryComponent = getFieldQueryComponent(actualField, fields, overrideFields);
            if (null != queryComponent) {
                selectItems.setValueExpression("value", new EntitySelectItemsValueExpression(this, queryComponent));
            } else {
                selectItems.setValueExpression("value", new EntitySelectItemsValueExpression(actualField.getType()));
            }

            htmlPanelGrid.getChildren().add(input);
        } else if (null != oneToManyAnnotation) {
            input = (SelectManyMenu) application.createComponent(SelectManyMenu.COMPONENT_TYPE);
            SelectManyMenu selectManyMenu = (SelectManyMenu) input;
            selectManyMenu.setDisabled(disabled);
            selectManyMenu.setShowCheckbox(true);
            UISelectItems selectItems = (UISelectItems) application.createComponent(UISelectItems.COMPONENT_TYPE);
            input.getChildren().add(selectItems);
            Type type = actualField.getGenericType();
            LOGGER.debug("type class: {}", type.getClass().getName());
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> listTypeClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            selectManyMenu.setConverter(new EntityConverter());
            QueryComponent queryComponent = getFieldQueryComponent(actualField, fields, overrideFields);
            if (null != queryComponent) {
                selectItems.setValueExpression("value", new EntitySelectItemsValueExpression(this, queryComponent));
            } else if (addNotUpdate) {
                selectItems.setValueExpression("value", new EntitySelectItemsValueExpression(listTypeClass, entityField.getDeclaringClass(), entityField.getName(), null));
            } else {
                selectItems.setValueExpression("value", new EntitySelectItemsValueExpression(listTypeClass, entityField.getDeclaringClass(), entityField.getName(), this));
            }
            htmlPanelGrid.getChildren().add(input);
        } else if (actualField.getType() == Boolean.TYPE) {
            input = (SelectBooleanCheckbox) application.createComponent(SelectBooleanCheckbox.COMPONENT_TYPE);
            SelectBooleanCheckbox selectBooleanCheckbox = (SelectBooleanCheckbox) input;
            selectBooleanCheckbox.setDisabled(disabled);
            htmlPanelGrid.getChildren().add(input);
        } else if (actualField.getType() == Boolean.class) {
            input = (TriStateCheckbox) application.createComponent(TriStateCheckbox.COMPONENT_TYPE);
            input.setConverter(new TriStateBooleanConverter());
            TriStateCheckbox triStateCheckbox = (TriStateCheckbox) input;
            triStateCheckbox.setDisabled(disabled);
            htmlPanelGrid.getChildren().add(input);
        } else if (actualField.getType() == Date.class) {
            input = (Calendar) application.createComponent(Calendar.COMPONENT_TYPE);
            Calendar calendarComponent = (Calendar) input;
            calendarComponent.setDisabled(disabled);
            if (entityInspector.isTemporal(actualField)) {
                Calendar calendar = (Calendar) input;
                Temporal temporal = entityInspector.getAnnotation(entityField, embeddableField, Temporal.class);
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
            htmlPanelGrid.getChildren().add(input);
        } else if (actualField.getType() == java.util.Calendar.class) {
            input = (Calendar) application.createComponent(Calendar.COMPONENT_TYPE);
            Calendar calendarComponent = (Calendar) input;
            calendarComponent.setDisabled(disabled);
            input.setConverter(new CalendarConverter());
            Calendar calendar = (Calendar) input;
            calendar.setPattern("dd/MM/yyyy");
            htmlPanelGrid.getChildren().add(input);
        } else if (actualField.getType().isEnum()) {
            input = (SelectOneMenu) application.createComponent(SelectOneMenu.COMPONENT_TYPE);
            SelectOneMenu selectOneMenu = (SelectOneMenu) input;
            selectOneMenu.setDisabled(disabled);
            UISelectItem emptySelectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
            input.getChildren().add(emptySelectItem);
            Object[] enumConstants = actualField.getType().getEnumConstants();
            for (Object enumConstant : enumConstants) {
                UISelectItem selectItem = (UISelectItem) application.createComponent(UISelectItem.COMPONENT_TYPE);
                selectItem.setItemValue(enumConstant);
                selectItem.setItemLabel(enumConstant.toString());
                input.getChildren().add(selectItem);
            }
            htmlPanelGrid.getChildren().add(input);
        } else if (actualField.getType() == byte[].class) {
            FileUpload fileUpload = (FileUpload) application.createComponent(FileUpload.COMPONENT_TYPE);
            fileUpload.setAuto(true);
            fileUpload.setId(inputId);
            MethodExpression fileUploadListener = new FieldUploadMethodExpression(this, entityField.getName(), addNotUpdate);
            Method[] fileUploadMethods = FileUpload.class.getMethods();
            Method setFileUploadListenerMethod = null;
            for (Method fileUploadMethod : fileUploadMethods) {
                // primefaces 7-
                if (fileUploadMethod.getName().equals("setFileUploadListener")) {
                    setFileUploadListenerMethod = fileUploadMethod;
                    break;
                } else if (fileUploadMethod.getName().equals("setListener")) {
                    // primefaces 8+
                    setFileUploadListenerMethod = fileUploadMethod;
                    break;
                }
            }
            if (null != setFileUploadListenerMethod) {
                try {
                    setFileUploadListenerMethod.invoke(fileUpload, fileUploadListener);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    LOGGER.error("reflection error: " + ex.getMessage(), ex);
                }
            } else {
                LOGGER.error("FileUpload listener not set!");
            }
            if (null != columnAnnotation) {
                fileUpload.setSizeLimit(Long.valueOf(columnAnnotation.length()));
            }
            input = fileUpload;
            htmlPanelGrid.getChildren().add(input);

            CommandButton clearCommandButton = (CommandButton) application.createComponent(CommandButton.COMPONENT_TYPE);
            fileUpload.getChildren().add(clearCommandButton);
            clearCommandButton.setValue("Clear");
            clearCommandButton.setId(inputId + "Clear");
            clearCommandButton.addActionListener(new ClearFieldActionListener(this, entityField, addNotUpdate));
            clearCommandButton.setImmediate(true);
            clearCommandButton.setUpdate(fileUpload.getClientId() + "," + inputId + "Message");
        } else if (isPasswordField(actualField, fields)) {
            input = (Password) application.createComponent(Password.COMPONENT_TYPE);
            Password password = (Password) input;
            password.setDisabled(disabled);
            if (isFeedbackPassword(actualField, fields)) {
                password.setFeedback(true);
            }
            if (isMatchPassword(actualField, fields)) {
                password.setMatch(inputId + "Match");
            }
            Integer size = getFieldSize(actualField, fields, overrideFields);
            if (null != size) {
                password.setSize(size);
            }
            int length = 255;
            if (null != columnAnnotation) {
                length = columnAnnotation.length();
            }
            input.addValidator(new LengthValidator(length));
            htmlPanelGrid.getChildren().add(input);
        } else {
            int length = 255;
            if (null != columnAnnotation) {
                length = columnAnnotation.length();
            }
            if (length <= 255) {
                input = (InputText) application.createComponent(InputText.COMPONENT_TYPE);
                InputText inputText = (InputText) input;
                inputText.setDisabled(disabled);
                Integer size = getFieldSize(entityField, fields, overrideFields);
                if (null != size) {
                    inputText.setSize(size);
                }
            } else {
                input = (InputTextarea) application.createComponent(InputTextarea.COMPONENT_TYPE);
                InputTextarea inputTextarea = (InputTextarea) input;
                inputTextarea.setDisabled(disabled);
                inputTextarea.setCols(80);
                inputTextarea.setRows(10);
                inputTextarea.setAutoResize(false);
            }
            input.addValidator(new LengthValidator(length));
            htmlPanelGrid.getChildren().add(input);
        }
        input.setId(inputId);
        input.setValueExpression("value", new EntityFieldValueExpression(this, entityField, embeddableField, addNotUpdate));
        if (null != columnAnnotation) {
            if (!columnAnnotation.nullable()) {
                input.setRequired(true);
            }
        }
        Basic basicAnnotation = entityInspector.getAnnotation(entityField, embeddableField, Basic.class);
        if (null != basicAnnotation) {
            if (!basicAnnotation.optional()) {
                input.setRequired(true);
            }
        }
        if (null != manyToOneAnnotation) {
            if (!manyToOneAnnotation.optional()) {
                input.setRequired(true);
            }
        }
        if (null != oneToOneAnnotation && !oneToOneAnnotation.optional()) {
            input.setRequired(true);
        }
        if (isRequiredField(entityField, fields, overrideFields)) {
            input.setRequired(true);
        }
        if (null != joinColumnAnnotation && !joinColumnAnnotation.nullable()) {
            input.setRequired(true);
        }
        if (forceRender) {
            input.setRequired(true);
        }
        input.addValidator(new BeanValidationValidator());
        input.addValidator(new UniqueValidator());
        addValidators(entityField, fields, input);

        Message inputTextMessage = (Message) application.createComponent(Message.COMPONENT_TYPE);
        htmlPanelGrid.getChildren().add(inputTextMessage);
        inputTextMessage.setId(inputId + "Message");
        inputTextMessage.setFor(inputId);

        if (isMatchPassword(actualField, fields)) {
            OutputLabel passwordMatchOutputLabel = (OutputLabel) application.createComponent(OutputLabel.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(passwordMatchOutputLabel);
            passwordMatchOutputLabel.setValue(fieldLabel);
            passwordMatchOutputLabel.setFor(inputId + "Match");

            Password matchPassword = (Password) application.createComponent(Password.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(matchPassword);
            Password password = (Password) input;
            matchPassword.setDisabled(password.isDisabled());
            matchPassword.setId(inputId + "Match");
            matchPassword.setFeedback(password.isFeedback());
            matchPassword.setSize(password.getSize());
            matchPassword.setRequired(password.isRequired());
            int length = 255;
            if (null != columnAnnotation) {
                length = columnAnnotation.length();
            }
            matchPassword.addValidator(new LengthValidator(length));
            matchPassword.setValueExpression("value", new EntityFieldValueExpression(this, entityField, embeddableField, addNotUpdate));

            Message matchMessage = (Message) application.createComponent(Message.COMPONENT_TYPE);
            htmlPanelGrid.getChildren().add(matchMessage);
            matchMessage.setFor(inputId + "Match");
        }
        return input;
    }

    private void addValidators(Field entityField, Map<String, FieldComponent> fields, UIInput input) {
        FieldComponent fieldComponent = fields.get(entityField.getName());
        if (null == fieldComponent) {
            return;
        }
        fieldComponent.addValidators(input);
    }

    private void addColumn(DataTable dataTable, Field field, Map<String, FieldComponent> fields) {
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

        String fieldLabel = getFieldLabel(field, fields);
        column.setHeaderText(fieldLabel);

        UIOutput outputComponent = getFieldOutputComponent(field, fields);
        if (null != outputComponent) {
            column.getChildren().add(outputComponent);
            reloadId(outputComponent);
        } else {
            LimitingOutputText outputText = (LimitingOutputText) application.createComponent(LimitingOutputText.COMPONENT_TYPE);
            column.getChildren().add(outputText);
            if (isPasswordField(field, fields)) {
                outputText.setPassword(true);
            }
            outputComponent = outputText;
        }
        outputComponent.setValueExpression("value", expressionFactory.createValueExpression(elContext, "#{row." + field.getName() + "}", field.getType()));
    }

    private UIOutput getFieldOutputComponent(Field field, Map<String, FieldComponent> fields) {
        FieldComponent fieldComponent = fields.get(field.getName());
        if (null == fieldComponent) {
            return null;
        }
        return fieldComponent.getFieldOutputComponent();
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
        if (isFilterProperty(property)) {
            column.setValueExpression("filterBy", expressionFactory.createValueExpression(elContext, "#{row." + property.getName() + "}", String.class));
            column.setFilterMatchMode("contains");
        }

        HtmlOutputText outputText = (HtmlOutputText) application.createComponent(HtmlOutputText.COMPONENT_TYPE);
        column.getChildren().add(outputText);
        outputText.setValueExpression("value", expressionFactory.createValueExpression(elContext, "#{row." + property.getName() + "}", Object.class));
    }

    public void addMessage(FacesMessage.Severity severity, String message) {
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
        for (UIComponent child : getChildren()) {
            if (child instanceof HtmlForm) {
                for (UIComponent htmlFormChild : child.getChildren()) {
                    if (htmlFormChild instanceof DataTable) {
                        ValueExpression valueExpression = htmlFormChild.getValueExpression("value");
                        EntityValueExpression entityValueExpression = (EntityValueExpression) valueExpression;
                        entityValueExpression.resetCache();

                        String dataTableClientId = htmlFormChild.getClientId();
                        PrimeFaces primeFaces = PrimeFaces.current();
                        if (primeFaces.isAjaxRequest()) {
                            primeFaces.ajax().update(dataTableClientId);
                        }
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void addCreateListener(CreateListener listener) {
        FacesListener[] facesListeners = getFacesListeners(FacesListener.class);
        for (FacesListener existingFacesListener : facesListeners) {
            if (existingFacesListener == listener) {
                return;
            }
        }
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
        FacesListener[] facesListeners = getFacesListeners(FacesListener.class);
        for (FacesListener existingFacesListener : facesListeners) {
            if (existingFacesListener == listener) {
                return;
            }
        }
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
        FacesListener[] facesListeners = getFacesListeners(FacesListener.class);
        for (FacesListener existingFacesListener : facesListeners) {
            if (existingFacesListener == listener) {
                return;
            }
        }
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

    private Object eagerLoad(Object entity) {
        if (null == entity) {
            return null;
        }
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        EntityInspector entityInspector = new EntityInspector(entityManager, entity);
        Class<?> entityClass = entityInspector.getEntityClass();
        Object identifier = entityInspector.getIdentifier(entity);
        UserTransaction userTransaction = crudController.getUserTransaction();

        try {
            userTransaction.begin();
        } catch (NotSupportedException | SystemException ex) {
            LOGGER.error("error: " + ex.getMessage(), ex);
            return entity;
        }
        Object loadedEntity;
        try {
            loadedEntity = entityManager.find(entityClass, identifier);
            if (null == loadedEntity) {
                LOGGER.error("could not find entity: " + identifier);
                return entity;
            }
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (field.getName().startsWith("_persistence_")) {
                    // payara
                    continue;
                }
                OneToMany oneToManyAnnotation = entityInspector.getAnnotation(field, OneToMany.class);
                ManyToMany manyToManyAnnotation = entityInspector.getAnnotation(field, ManyToMany.class);
                ElementCollection elementCollectionAnnotation = entityInspector.getAnnotation(field, ElementCollection.class);
                if (null == oneToManyAnnotation && null == manyToManyAnnotation && null == elementCollectionAnnotation) {
                    continue;
                }
                if (!Collection.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                Collection collectionValue;
                try {
                    field.setAccessible(true);
                    collectionValue = (Collection) field.get(loadedEntity);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    LOGGER.error("reflection error: " + ex.getMessage(), ex);
                    return loadedEntity;
                }
                int size = collectionValue.size(); // eager loading
                LOGGER.debug("eager loading {} of size {}", field.getName(), size);
            }
            return loadedEntity;
        } finally {
            try {
                userTransaction.commit();
            } catch (RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | SystemException ex) {
                LOGGER.error("error: " + ex.getMessage(), ex);
                return entity;
            }
        }
    }

    public static CRUDComponent getCRUDComponent(String crudComponentId) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot view = facesContext.getViewRoot();
        UIComponent component = view.findComponent(crudComponentId);
        if (null == component) {
            return null;
        }
        return (CRUDComponent) component;
    }

    public static CRUDComponent getParentCRUDComponent(UIComponent component) {
        if (null == component) {
            throw new AbortProcessingException();
        }
        while (component.getParent() != null) {
            component = component.getParent();
            if (component instanceof CRUDComponent) {
                return (CRUDComponent) component;
            }
        }
        throw new AbortProcessingException();
    }

    public QueryComponent findQueryComponent() {
        for (UIComponent child : getChildren()) {
            if (child instanceof QueryComponent) {
                return (QueryComponent) child;
            }
        }
        return null;
    }

    @Override
    public Collection<String> getEventNames() {
        List<String> eventNames = new LinkedList<>();
        eventNames.add("oncreate");
        eventNames.add("onupdate");
        eventNames.add("ondelete");
        return eventNames;
    }

    @Override
    public void addClientBehavior(String eventName, ClientBehavior behavior) {
        LOGGER.debug("addClientBehavior: {} {}", eventName, behavior);
        super.addClientBehavior(eventName, behavior);
    }
}
