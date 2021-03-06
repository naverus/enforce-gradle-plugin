/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.gradle.plugins.enforce.interceptor.interceptors

import org.fundacionjala.gradle.plugins.enforce.interceptor.Interceptor
import org.fundacionjala.gradle.plugins.enforce.interceptor.MetadataInterceptor
import org.fundacionjala.gradle.plugins.enforce.interceptor.commands.*
import org.fundacionjala.gradle.plugins.enforce.utils.ManagementFile
import org.fundacionjala.gradle.plugins.enforce.utils.salesforce.MetadataComponents
import groovy.util.logging.Slf4j
/**
 * Implements methods to manage interceptors and load the objects to truncate
 */
@Slf4j
class ObjectInterceptor extends MetadataInterceptor {

    /**
     * Loads the object files to truncate
     */
    @Override
    void loadFiles(String sourcePath) {
        ManagementFile managementFile = new ManagementFile(sourcePath)
        files = managementFile.getFilesByFileExtension(MetadataComponents.OBJECTS.extension)
    }

    /**
     * Loads interceptors by default
     */
    @Override
    void loadInterceptors() {
        addInterceptor(Interceptor.TRUNCATE_FORMULAS.id, new ObjectFormula().execute)
        addInterceptor(Interceptor.TRUNCATE_WEB_LINKS.id, new ObjectWebLink().execute)
        addInterceptor(Interceptor.TRUNCATE_FIELD_SETS.id, new ObjectFieldSet().execute)
        addInterceptor(Interceptor.TRUNCATE_ACTION_OVERRIDES.id, new ObjectActionOverride().execute)
        addInterceptor(Interceptor.TRUNCATE_FIELD.id, new ObjectField().execute)
    }
}
