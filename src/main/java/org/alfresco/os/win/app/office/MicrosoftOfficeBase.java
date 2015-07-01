/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.os.win.app.office;

import java.io.File;

import org.alfresco.os.common.ApplicationBase;
import org.alfresco.os.win.Application;
import org.alfresco.utilities.LdtpUtils;

import com.cobra.ldtp.Ldtp;
import com.cobra.ldtp.LdtpExecutionError;
import org.apache.log4j.Logger;

/**
 * This class has all the method involved in using the actions in Office application
 *
 * @author Subashni Prasanna
 * @author Paul Brodner
 */
/**
 * @author qa
 *
 */
public class MicrosoftOfficeBase extends Application
{
    private static Logger logger = Logger.getLogger(MicrosoftOfficeBase.class);
    protected VersionDetails applicationDetails;
    protected String fileMenuPage = "File";
    protected String userName;
    protected String userPassword;

    public MicrosoftOfficeBase(VersionDetails officeApplication, String version)
    {
        applicationDetails = officeApplication;
        setApplicationName(applicationDetails.getName());
        setApplicationVersion(version);
        setApplicationPath(new File(getOfficePath(), applicationDetails.getExeName()).getPath());
        setWaitWindow(applicationDetails.getWaitWindow());
    }

    /**
     * Enums to hold all the applications name.
     *
     * @author Subashni Prasanna
     * @author Paul Brodner
     */
    public enum VersionDetails
    {

        WORD("Microsoft Word Document", "Microsoft Word", "WINWORD.EXE", "Blank document"), EXCEL(
                "Microsoft Excel Worksheet",
                "Microsoft Excel",
                "EXCEL.EXE",
                "Blank workbook"), POWERPOINT("Microsoft PowerPoint Presentation", "Microsoft PowerPoint", "POWERPNT.EXE", "Blank Presentation"), OUTLOOK(
                "Outlook",
                "Microsoft Outlook",
                "OUTLOOK.exe",
                "TBD");

        private String application;
        private String waitWindow;
        private String exeName;
        private String blank;

        private VersionDetails(String type, String waitWindow, String exeName, String blank)
        {
            this.application = type;
            this.waitWindow = waitWindow;
            this.exeName = exeName;
            this.blank = blank;
        }

        public String getName()
        {
            return application;
        }

        public String getWaitWindow()
        {
            return waitWindow;
        }

        public String getExeName()
        {
            return exeName;
        }

        public String getBlankDoc()
        {
            return blank;
        }
    }

    /*
     * Need to use the EXE NAME in order to kill the application
     */
    public void killProcess()
    {
        LdtpUtils.execute(new String[] { "taskkill", "/F", "/IM", applicationDetails.getExeName() });
    }

    /**
     * Close Application from Close button
     */
    public void closeApplication()
    {
        getLdtp().generateKeyEvent("<esc>");
        getLdtp().click("Close");
        setWaitWindow(applicationDetails.getWaitWindow());
    }

    /**
     * Close Application from Close button, based on filename already opened
     * @param file
     */
    public void closeApplication(File file)
    {
        focus(file);
        getLdtp().click("btnFileTab");
        getLdtp().waitTillGuiExist("File", LdtpUtils.RETRY_COUNT);
        getLdtp().click("Exit");
        setWaitWindow(applicationDetails.getWaitWindow());
//        closeApplication();
    }

    /**
     * Focus application based on Filename
     * You can pass as arguments just Files or Strings
     *
     * @param excelFileTitle
     */
    public void focus(Object fileName)
    {
        String _waitFor = "";
        try
        {
            if (fileName instanceof File)
            {
                _waitFor = ((File) fileName).getName();
            }
            else if (fileName instanceof String)
            {
                _waitFor = fileName.toString();
            }
            waitForApplicationWindow(_waitFor, true);
            getLdtp().activateWindow(getWaitWindow());
        }
        catch (Exception e)
        {
            logger.error("Could not find window: " + fileName + " to focus!");
        }
    }

    public ApplicationBase openApplication()
    {
        try
        {
            openApplication(new String[] { getApplicationPath() });
            getLdtp().generateKeyEvent("<esc>");
        }
        catch (Exception e)
        {
            logger.error("Could not open Application " + getApplicationName() + "Error: " + e);
        }
        return this;
    }

    /*
     * Method to implement adding a data inside the Office application based on the application
     */
    public void editOffice(String data)
    {
        getLdtp().generateKeyEvent(data);
    }

    public void saveOffice() throws LdtpExecutionError
    {
        getLdtp().generateKeyEvent("<ctrl><s>");
    }

    /*
     * Method to implement Save for the first time
     */
    public void saveOffice(String location) throws Exception
    {
        getLdtp().click("btnSave");
        waitForWindow("Save As");
        getLdtp().deleteText("txtFilename", 0);
        getLdtp().enterString("txtFilename", location);
        getLdtp().click("btnSave");
        getLdtp().waitTillGuiNotExist("Save As");
    }

    /*
     * Method to implement Opening a file inside the office application
     */
    public void openOfficeFromFileMenu(String location) throws Exception
    {
        goToFile();
        getLdtp().click("Open");
        waitForWindow("dlgOpen");
        getLdtp().activateWindow("dlgOpen");
        getLdtp().enterString("txtFilename", location);
        getLdtp().mouseLeftClick("uknOpen");
    }

    /**
     * Click in File
     *
     * @param ldtp
     */
    public void goToFile()
    {
        LdtpUtils.logInfo("Go to File");
        getLdtp().click("btnFileTab");
        getLdtp().waitTillGuiExist("File", LdtpUtils.RETRY_COUNT);
    }

    
    

    

    /**
     * Helper method that will activate the Open dialog window
     */
    public void activateDialog(String dialogObjectName)
    {
        LdtpUtils.logInfo("Activate '" + dialogObjectName + "' dialog");

        try
        {
            if (waitForWindow(dialogObjectName) == null)
            {
                throw new LdtpExecutionError("Cannot find the diaglog: " + dialogObjectName);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        getLdtp().activateWindow(getWaitWindow()); // getWaitWindow defined on LDTP
    }

    
    
   

    //
   

    
    /**
     * get the path of the office on client machine
     *
     * @return
     */
    protected String getOfficePath()
    {
        String val = getProperty("win.office" + getApplicationVersion() + ".path");
      //  return ((val == null) ? OFFICE_PATH : val);
        return val;
    }

    

    public void saveAsOffice(String path) throws Exception
    {
        goToSaveAsMenu();
        waitForWindow("Save As");
        getLdtp().activateWindow("Save As");
        getLdtp().deleteText("txtFilename", 0);
        getLdtp().enterString("txtFilename", path);
        getLdtp().click("btnSave");
        getLdtp().waitTillGuiNotExist("Save As");
    }

    /**
     * Open SaveAs dialog from File menu
     */
    private void goToSaveAsMenu()
    {
        goToFile();
        getLdtp().selectMenuItem("mnuSaveAs");
    }

}
