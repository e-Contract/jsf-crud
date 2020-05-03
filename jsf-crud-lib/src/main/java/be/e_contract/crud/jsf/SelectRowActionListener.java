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
import javax.el.ELResolver;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectRowActionListener extends AbstractCRUDComponentStateHolder implements ActionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectRowActionListener.class);

    public SelectRowActionListener() {
        super();
    }

    public SelectRowActionListener(String crudComponentId) {
        super(crudComponentId);
    }

    @Override
    public void processAction(ActionEvent event) throws AbortProcessingException {
        LOGGER.debug("processAction");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();
        ELResolver elResolver = elContext.getELResolver();
        Object entity = elResolver.getValue(elContext, null, "row");
        CRUDComponent crudComponent = super.getCRUDComponent();
        crudComponent.setSelection(entity);
    }
}
