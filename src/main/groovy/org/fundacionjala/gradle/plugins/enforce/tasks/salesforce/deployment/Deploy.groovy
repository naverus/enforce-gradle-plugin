/*
 * Copyright (c) Fundacion Jala. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package org.fundacionjala.gradle.plugins.enforce.tasks.salesforce.deployment

import org.fundacionjala.gradle.plugins.enforce.filemonitor.ComponentMonitor
import org.fundacionjala.gradle.plugins.enforce.interceptor.Interceptor
import org.fundacionjala.gradle.plugins.enforce.utils.AnsiColor
import org.fundacionjala.gradle.plugins.enforce.utils.Constants
import org.fundacionjala.gradle.plugins.enforce.utils.Util
import org.fundacionjala.gradle.plugins.enforce.utils.salesforce.FileValidator
import org.fundacionjala.gradle.plugins.enforce.utils.salesforce.filter.Filter

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

/**
 * Deploys all project source
 */
class Deploy extends Deployment {
    private boolean deprecateTruncateOn
    private boolean codeTruncateOn
    private String deployPackagePath

    public ArrayList<String> foldersNotDeploy
    public String folderDeploy
    public String folders
    public String excludes
    public ArrayList<File> filesToDeploy

    /**
     * Sets description task and its group
     */
    Deploy() {
        super(Constants.DEPLOY_DESCRIPTION, Constants.DEPLOYMENT)
        deprecateTruncateOn = true
        codeTruncateOn = true
    }

    /**
     * Executes the steps for deploy
     */
    @Override
    public void runTask() {
        setupFilesToDeploy()
        logger.debug("${'Creating folder  Deploy at: '}${folderDeploy}")
        createDeploymentDirectory(folderDeploy)
        loadParameters()
        addFiles()
        if (Util.isValidProperty(parameters, Constants.FOLDERS_DEPLOY)) {
            deployByFolder()
        } else {
            checkStatusTruncate()
            displayFolderNoDeploy()
            deployTruncateFiles()
            deployAllComponents()
        }
        deployToSalesForce()
    }

    /**
     * Adds all files into files to deploy
     */
    def addFiles() {
        Filter filter = new Filter(project,projectPath)
        filesToDeploy = filter.getFiles(folders,excludes)
        filesToDeploy = FileValidator.getValidFiles(projectPath, filesToDeploy)
    }

    /**
     * Sets path of build/deploy directory
     * Sets path of package from build/deploy directory
     * Sets path of package from project directory
     */
    public void setupFilesToDeploy() {
        folderDeploy = Paths.get(buildFolderPath, Constants.FOLDER_DEPLOY).toString()
        deployPackagePath = Paths.get(folderDeploy, PACKAGE_NAME).toString()
    }

    /**
     * Deploys by folder
     */
    public void deployByFolder() {
        if (folders) {
            fileManager.copy(projectPath, filesToDeploy, folderDeploy)
            writePackage(deployPackagePath, filesToDeploy)
            combinePackageToUpdate(deployPackagePath)
        }
    }

    /**
     * Deploys files truncated and files no truncated
     */
    public void deployTruncateFiles() {
        if (codeTruncateOn) {
            Files.copy(Paths.get(projectPath, PACKAGE_NAME), Paths.get(deployPackagePath), StandardCopyOption.REPLACE_EXISTING)
            logger.debug('Copying files to deploy')
            fileManager.copy(projectPath, filesToDeploy, folderDeploy)
            logger.debug('Generating package')
            writePackage(deployPackagePath, filesToDeploy)
            combinePackage(deployPackagePath)
            truncateComponents()
            componentDeploy.startMessage = Constants.DEPLOYING_TRUNCATED_CODE
            componentDeploy.successMessage = Constants.DEPLOYING_TRUNCATED_CODE_SUCCESSFULLY
            logger.debug("Deploying to truncate components from: $folderDeploy")
            executeDeploy(folderDeploy)
            createDeploymentDirectory(folderDeploy)
        }
    }

    /**
     * Deploys all components from project directory
     */
    public void deployAllComponents() {
        ArrayList<File> filteredFiles = excludeFiles(fileManager.getValidElements(projectPath))
        fileManager.copy(projectPath, filteredFiles, folderDeploy)
        writePackage(deployPackagePath, filteredFiles)
        combinePackage(deployPackagePath)
        componentDeploy.startMessage = Constants.DEPLOYING_CODE
        componentDeploy.successMessage = Constants.DEPLOYING_CODE_SUCCESSFULLY
    }

    /**
     * Checks if property turn off truncate exists and sets respective values
     */
    private void checkStatusTruncate() {
        if (!Util.isValidProperty(parameters, Constants.TURN_OFF_TRUNCATE)) {
            return
        }
        String turnOffOptionTruncate = parameters[Constants.TURN_OFF_TRUNCATE].toString()
        if (turnOffOptionTruncate.indexOf(Constants.TRUNCATE_DEPRECATE) != Constants.NOT_FOUND) {
            deprecateTruncateOn = false
            logger.quiet(Constants.TRUNCATE_DEPRECATE_TURNED_OFF)
        }
        if (turnOffOptionTruncate.indexOf(Constants.TRUNCATE_CODE) != Constants.NOT_FOUND) {
            codeTruncateOn = false
            logger.quiet(Constants.TRUNCATE_CODE_TURNED_OFF)
        }
    }

    /**
     * Deploys code to salesForce Organization
     */
    private void deployToSalesForce() {
        if (deprecateTruncateOn) {
            interceptorsToExecute = [Interceptor.REMOVE_DEPRECATE.id]
            interceptorsToExecute += interceptors
            logger.debug("Truncating components from: $folderDeploy")
            truncateComponents(folderDeploy)
        }
        logger.debug("Deploying all components from: $folderDeploy")
        executeDeploy(folderDeploy)
        updateFileTracker()
    }

    /**
     * Displays folders that will not be deployed
     */
    public void displayFolderNoDeploy() {
        foldersNotDeploy = fileManager.getFoldersNotDeploy(folderDeploy)
        def index = 1
        if (foldersNotDeploy.size() > 0) {
            println ''
            println AnsiColor.ANSI_YELLOW.value()
            println("Folders not deployed ")
            println('___________________________________________')
            println ''
            foldersNotDeploy.each { nameFolder ->
                println("${"\t"}${index}${".- "}${nameFolder}")
                index++
            }
            println('___________________________________________')
            println AnsiColor.ANSI_RESET.value()
        }
    }

    /**
     * Updates a file tracker
     */
    public void updateFileTracker() {
        ComponentMonitor componentMonitor = new ComponentMonitor(projectPath)
        logger.debug('Getting components signatures')
        Map initMapSave = componentMonitor.getComponentsSignature(fileManager.getValidElements(projectPath, excludeFilesToMonitor))
        logger.debug('Saving initial file tracker')
        componentMonitor.componentSerializer.save(initMapSave)
    }

    /**
     * Truncates the classes, objects, triggers, pages and workflows
     */
    public void truncateComponents() {
        String srcPath = Paths.get(buildFolderPath, Constants.FOLDER_DEPLOY).toString()
        interceptorsToExecute = [Interceptor.TRUNCATE_CLASSES.id, Interceptor.TRUNCATE_FIELD_SETS.id, Interceptor.TRUNCATE_ACTION_OVERRIDES.id,
                                 Interceptor.TRUNCATE_FIELD.id, Interceptor.TRUNCATE_FORMULAS.id, Interceptor.TRUNCATE_WEB_LINKS.id,
                                 Interceptor.TRUNCATE_PAGES.id, Interceptor.TRUNCATE_TRIGGERS.id, Interceptor.TRUNCATE_WORKFLOWS.id,
                                 Interceptor.TRUNCATE_COMPONENTS.id]
        interceptorsToExecute += interceptors
        logger.debug("Truncating components at: $srcPath")
        truncateComponents(srcPath)
    }

    /**
     * Initializes all task parameters
     * @param properties the task properties
     * @return A map of all task parameters
     */
    def loadParameters() {
        folders = "null"
        excludes = "null"
        if (Util.isValidProperty(parameters, Constants.PARAMETER_FOLDERS)) {
            folders = parameters[Constants.PARAMETER_FOLDERS].toString()
        }
        if (Util.isValidProperty(parameters, Constants.PARAMETER_EXCLUDES)) {
            excludes = parameters[Constants.PARAMETER_EXCLUDES].toString()
        }
    }
}