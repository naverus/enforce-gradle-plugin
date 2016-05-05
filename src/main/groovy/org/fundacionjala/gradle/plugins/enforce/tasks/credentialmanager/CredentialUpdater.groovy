/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.gradle.plugins.enforce.tasks.credentialmanager

import org.fundacionjala.gradle.plugins.enforce.credentialmanagement.CredentialMessage
import org.fundacionjala.gradle.plugins.enforce.exceptions.CredentialException
import org.fundacionjala.gradle.plugins.enforce.utils.Util

class CredentialUpdater extends CredentialManagerTask {

    /**
     * This class Updates credential from credentials.dat file
     */
    CredentialUpdater() {
        super(CredentialMessage.UPDATE_CREDENTIAL_DESCRIPTION.value(), CredentialMessage.CREDENTIAL_MANAGER_GROUP.value())
    }

    @Override
    void runTask() {
        if (CredentialParameterValidator.hasIdCredential(project)) {
            updateCredentialByParameters()
        }
        if (!CredentialParameterValidator.hasIdCredential(project) || !CredentialParameterValidator.hasUserName(project)) {
            while (credentialManagerInput.finished) {
                Util.showExceptionWhenSystemConsoleIsNull(System.console())
                credentialManagerInput.updateCredentialByConsole()
            }
        }
    }

    /**
     * Updates a credential by parameters
     */
    public void updateCredentialByParameters() {
        String credentialId = project.properties[CredentialMessage.ID_PARAM.value()].toString()
        if (!credentialManagerInput.hasCredential(credentialId)) {
            throw new CredentialException("${credentialId} ${CredentialMessage.MESSAGE_ID_CREDENTIAL_DOES_NOT_EXIST.value()}" )
        }
        if (CredentialParameterValidator.validateFieldsCredential(project)) {
            String credentialType = credentialManagerInput.getCredentialToUpdate(credentialId).type
            credentialManagerInput.updateCredential(CredentialParameterValidator.getCredentialInserted(project, credentialType))
        }
    }
}
