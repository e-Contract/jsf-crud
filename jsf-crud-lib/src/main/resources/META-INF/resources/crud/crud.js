/*
 * JSF CRUD project.
 *
 * Copyright 2020 e-Contract.be BV. All rights reserved.
 * e-Contract.be BV proprietary/confidential. Use is subject to license terms.
 */

function addEntityResponse(xhr, status, args) {
    if (args.validationFailed) {
        return;
    }
    PF('addDialog').hide();
}


function updateEntityResponse(xhr, status, args) {
    if (args.validationFailed) {
        return;
    }
    PF('updateDialog').hide();
}