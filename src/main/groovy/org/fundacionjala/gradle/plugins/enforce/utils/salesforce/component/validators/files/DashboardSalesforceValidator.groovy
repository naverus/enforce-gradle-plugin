package org.fundacionjala.gradle.plugins.enforce.utils.salesforce.component.validators.files

import org.fundacionjala.gradle.plugins.enforce.utils.Constants
import org.fundacionjala.gradle.plugins.enforce.utils.Util
import org.fundacionjala.gradle.plugins.enforce.utils.salesforce.MetadataComponents

class DashboardSalesforceValidator implements SalesforceValidator {

    /**
     * Validates the report file that belongs to dashboards folder defined in Salesforce
     * @param file is File that will be validate
     * @param folderComponent is a String
     * @return true if file is valid
     */
    @Override
    boolean validateFile(File file, String folderComponent) {
        return (folderComponent.equals(MetadataComponents.DASHBOARDS.getDirectory()) &&
                !file.isDirectory()) && (Util.getFileExtension(file).equals(MetadataComponents.DASHBOARDS.getExtension()) ||
                file.getName().endsWith(Constants.META_XML))
    }

    /**
     * Validates the report file with xml extension
     * @param file is File that will be validate
     * @param folderComponent is a String
     * @return true if file is valid
     */
    @Override
    boolean hasMetadataFile(File file, String folderComponent) {
        return true
    }
}
