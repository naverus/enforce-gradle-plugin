package org.fundacionjala.gradle.plugins.enforce.utils.salesforce.PackageManager

import com.sforce.soap.metadata.PackageTypeMembers
import org.fundacionjala.gradle.plugins.enforce.utils.Util
import org.fundacionjala.gradle.plugins.enforce.utils.salesforce.MetadataComponents

import java.nio.file.Paths

class PackageUtil {
    private final static EMPTY = ''
    private final static AURA_FOLDER_NAME = 'aura'
    /**
     * Gets an array list of package member objects from type name and member list
     * @param name represents a type name in package xml file
     * @param members an array list of member
     */
    public static ArrayList<PackageTypeMembers> getPackageTypeMembers(String name, ArrayList<String> members) {
        ArrayList<PackageTypeMembers> packageTypeMembers = new ArrayList<PackageTypeMembers>()
        PackageTypeMembers packageTypeMember = new PackageTypeMembers()
        packageTypeMember.members = members.toArray() as String[]
        packageTypeMember.name = name
        packageTypeMembers.push(packageTypeMember)

        return packageTypeMembers
    }

    /**
     * Filters folders from array of files
     * @param files contains all files
     * @param basePath contains path of files
     * @return folders
     */
    public static ArrayList<String> selectFolders(ArrayList<File> files, String basePath) {
        ArrayList<String> folders = []
        if (basePath == EMPTY) {
            folders = selectFoldersWithoutBasePath(files)
        } else {
            folders = selectFoldersWithBasePath(files, basePath)
        }
        return folders
    }

    /**
     * Filters folders from array of files without their base path
     * @param files contains all files
     * @return folders
     */
    private static ArrayList<String> selectFoldersWithoutBasePath(ArrayList<File> files) {
        ArrayList<String> folders = []
        files.each { file ->
            MetadataComponents component = MetadataComponents.getComponentByFolder(file.name)
            if (component && !folders.contains(file.name)) {
                folders.push(file.name)
            } else {
                if (!folders.contains(file.getParentFile().getName())) {
                    folders.push(file.getParentFile().getName())
                }
            }
        }
        return folders
    }

    /**
     * Filters folders from array of files taking in account to base path
     * @param files is an array list o files
     * @param basePath is path of files
     * @return folders
     */
    private static ArrayList<String> selectFoldersWithBasePath(ArrayList<File> files, String basePath) {
        ArrayList<String> folders = []
        files.each { File file ->
            String relativePath = Util.getRelativePath(file, basePath)
            String folderName = Util.getFirstPath(relativePath)
            if(!folders.contains(folderName)) {
                folders.push(folderName)
            }
        }
        return folders
    }

    /**
     * Selects all files inside folder required
     * @param folder contains the folder to get all files
     * @param files contains the list of files
     * @return all files inside folders
     */
    public static ArrayList<String> selectFilesMembers(ArrayList<File> files, String basePath) {
        ArrayList<String> members = []
        if (basePath == EMPTY){
            members = selectFilesMembersWithoutPath(files)
        } else {
            members = selectFilesMembersWithBasePath(files, basePath)
        }
        return members.unique()
    }

    /**
     * Filters files taking in account base path o files
     * @param files contains a files
     * @param basePath contains a files bse path
     * @return an array of files filtered
     */
    private static ArrayList<String> selectFilesMembersWithBasePath(ArrayList<File> files, String basePath) {
        ArrayList<String> members = []
        files.each { File file ->
            String relativePath = Util.getRelativePath(file, basePath)
            String parentName = Util.getFirstPath(relativePath)
            String fileName = Util.getRelativePath(file, Paths.get(basePath, parentName).toString(), false)
            if (parentName == AURA_FOLDER_NAME && file.isFile()) {
                members.push(file.getParentFile().getName() as String)
                return
            }
            if (fileName && !fileName.isEmpty()) {
                members.push(Util.getFileName(fileName as String))
            }
        }
        return members
    }

    /**
     * Filters files without base path
     * @param folder contains a folder name
     * @param files contains an array of files
     * @return an arrayList of fileNames
     */
    private static ArrayList<String> selectFilesMembersWithoutPath(ArrayList<File> files) {
        ArrayList<String> members = []
        files.each { file ->
            File parentFile = file.getParentFile()
            if (parentFile.getName() == AURA_FOLDER_NAME) {
                members.push(parentFile.getName() as String)
                return
            }
            if (parentFile) {
                members.push(Util.getFileName(file.getName() as String))
            }
        }
        return members
    }

    /**
     * Gets files by folder
     * @param folderName is folder name
     * @param files is an array list of files
     * @return array list of files
     */
    public static ArrayList<File> getFilesByFolder(String folderName, ArrayList<File> files) {
        return files.grep({ File file ->
            String filePath = file.getPath()
            filePath.contains(folderName)
        })
    }
}
