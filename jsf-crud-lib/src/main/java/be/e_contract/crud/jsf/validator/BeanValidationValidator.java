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
package be.e_contract.crud.jsf.validator;

import be.e_contract.crud.jsf.jpa.CRUDController;
import be.e_contract.crud.jsf.jpa.EntityInspector;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanValidationValidator implements Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeanValidationValidator.class);

    @Override
    public void validate(FacesContext facesContext, UIComponent component, Object value) throws ValidatorException {
        LOGGER.debug("validate");
        ValueExpression valueExpression = component.getValueExpression("value");
        if (null == valueExpression) {
            return;
        }
        ValueReference valueReference = valueExpression.getValueReference(facesContext.getELContext());
        if (null == valueReference) {
            return;
        }
        Object entity = valueReference.getBase();
        String property = (String) valueReference.getProperty();
        LOGGER.debug("entity: {}", entity);
        LOGGER.debug("property: {}", property);
        if (null == entity) {
            return;
        }
        if (null == property) {
            return;
        }
        CRUDController crudController = CRUDController.getCRUDController();
        EntityManager entityManager = crudController.getEntityManager();
        EntityInspector entityInspector = new EntityInspector(entityManager, entity);
        Class<?> entityClass = entityInspector.getEntityClass();

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        javax.validation.Validator validator = validatorFactory.getValidator();

        Class<?>[] validationGroupsArray = new Class[]{};
        Set violationsRaw = validator.validateValue(entityClass, property, value, validationGroupsArray);
        Set<ConstraintViolation<?>> violations = violationsRaw;
        if (null == violations) {
            return;
        }
        LOGGER.debug("violations: {}", violations);
        if (violations.isEmpty()) {
            return;
        }
        if (violations.size() == 1) {
            ConstraintViolation<?> violation = violations.iterator().next();
            FacesMessage facesMessage = new FacesMessage(violation.getMessage());
            facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(facesMessage);
        } else {
            Set<FacesMessage> messages = new LinkedHashSet<>(violations.size());
            for (ConstraintViolation<?> violation : violations) {
                FacesMessage facesMessage = new FacesMessage(violation.getMessage());
                facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
                messages.add(facesMessage);
            }
            throw new ValidatorException(messages);
        }
    }
}
