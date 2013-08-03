//********************************************************************/
//* Copyright (C) 2011                                               */
//* MEL O'CAT  X178G243 (at) yahoo (dot) com                         */
//* License terms: GNU General Public License Version 2              */
//*                or any later version                              */
//********************************************************************/
//*4567890123456 (71-character line to adjust editor window) 23456789*/

/**
 *  GMFFFolder.java  0.01 11/01/2011
 *
 *  Version 0.01:
 *  Nov-01-2011: new.
 */

package mmj.gmff;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 *   GMFFFolder is a helper class for GMFF reading and
 *   writing.
 */
public class GMFFFolder {

    private File folderFile;

    /**
     *  Constructor for GMFFFolder using pathname String.
     *  <p>
     *  The input <code>folderName</code> String may be
     *  an absolute or relative path name. If it is null
     *  or an empty string, does not exist, is not a
     *  directory, or is security protected, then an
     *  exception is thrown.
     *  <p>
     *  @param filePath path name for building folder.
     *            May be null, relative or absolute.
     *  @param folderName relative or absolute path name
     *  @param exportType Export Type parm related to this
     *            folder for use in error messages.
     *  @throws GMFFException if folderName not found, not
     *            a readable directory, security protected,
     *            or just plain invalid.
     */
    public GMFFFolder(final File filePath, final String folderName,
        final String exportType) throws GMFFException
    {

        if (folderName == null || folderName.trim().length() == 0)
            throw new GMFFException(
                GMFFConstants.ERRMSG_GMFF_FOLDER_NAME_BLANK_1);

        try {
            folderFile = new File(folderName);
            if (filePath == null || folderFile.isAbsolute()) {}
            else
                folderFile = new File(filePath, folderName);
            if (folderFile.exists()) {
                if (folderFile.isDirectory()) {
                    // okey dokey!
                }
                else
                    throw new GMFFException(
                        GMFFConstants.ERRMSG_NOT_A_GMFF_FOLDER_1 + folderName
                            + GMFFConstants.ERRMSG_NOT_A_GMFF_FOLDER_2
                            + exportType
                            + GMFFConstants.ERRMSG_NOT_A_GMFF_FOLDER_3
                            + getAbsolutePath());
            }
            else
                throw new GMFFException(
                    GMFFConstants.ERRMSG_GMFF_FOLDER_NOTFND_1 + folderName
                        + GMFFConstants.ERRMSG_GMFF_FOLDER_NOTFND_2
                        + exportType
                        + GMFFConstants.ERRMSG_GMFF_FOLDER_NOTFND_3
                        + getAbsolutePath());
        } catch (final SecurityException e) {
            throw new GMFFException(
                GMFFConstants.ERRMSG_GMFF_FOLDER_MISC_ERROR_1 + folderName
                    + GMFFConstants.ERRMSG_GMFF_FOLDER_MISC_ERROR_2
                    + exportType
                    + GMFFConstants.ERRMSG_GMFF_FOLDER_MISC_ERROR_3
                    + getAbsolutePath()
                    + GMFFConstants.ERRMSG_GMFF_FOLDER_MISC_ERROR_4
                    + e.getMessage());
        }
    }

    /**
     *  Returns a name-sorted array listing the files in the
     *  directory which match the input fileType and have a name
     *  greater than or equal to the input lowestNamePrefix.
     *  <p>
     *  NOTE: "fileNamePrefix" refers to the file name without
     *        the dotted file type (e.g. "help.txt" has file
     *        name prefix = "help".)
     *  <p>
     *  @param fileType dot followed by file suffix
     *          (e.g. ".mmp"). Selected files must
     *          have matching file suffix.
     *  @param lowestNamePrefix Selected files must
     *          have names >= lowestNamePrefix, ignoring
     *          case.
     *  @return File array sorted by file name prefix
     *          of files within the folder with matching
     *          file type and file name prefix >= lowest
     *          file name.
     *  @throws GMFFException is security exception.
     */
    public File[] listFiles(final String fileType, final String lowestNamePrefix)
        throws GMFFException
    {

        final File[] fileArray = folderFile.listFiles(new GMFFFileFilter(
            fileType, lowestNamePrefix));

        if (fileArray == null)
            throw new GMFFException(
                GMFFConstants.ERRMSG_GMFF_FOLDER_READ_ERROR_1
                    + folderFile.getAbsolutePath()
                    + GMFFConstants.ERRMSG_GMFF_FOLDER_READ_ERROR_2);

        if (fileArray.length > 1)
            return sortFileArrayByNamePrefix(fileArray, fileType);

        return fileArray;
    }

    /**
     *  Returns the File object for the GMFFFolder.
     *  <p>
     *  @return File object for the GMFFFolder.
     */
    public File getFolderFile() {
        return folderFile;
    }

    /**
     *  Returns the absolute pathname of the GMFFFolder.
     *  <p>
     *  @return Absolute pathname of the GMFFFolder or null if the
     *              underlying File is null.
     */
    public String getAbsolutePath() {
        if (folderFile == null)
            return null;
        return folderFile.getAbsolutePath();
    }

    private File[] sortFileArrayByNamePrefix(final File[] fileArray,
        final String fileType)
    {

        final Map<String, File> treeMap = new TreeMap<String, File>();

        for (final File element : fileArray) {

            final String name = element.getName();

            final String namePrefix = name.substring(0, name.length()
                - fileType.length());

            treeMap.put(namePrefix, element);
        }

        return treeMap.values().toArray(new File[fileArray.length]);
    }
}
