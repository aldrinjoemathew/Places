package com.example.aldrin.places.helpers;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by aldrin on 2/9/16.
 * To add or remove files from SD card.
 */

public class StorageOnSdCard {
    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath();

    /**
     * Creates a folder in SD card specified by the path value folderPath.
     * Creates a file named placeId inside the folder.
     * Write the content into the file.
     * @param folderPath
     * @param fileName
     * @param content
     */
    public void addToSdCard(String folderPath, String fileName, String content) {
        try {
            File favDir = new File(SD_PATH, folderPath);
            favDir.mkdirs();
            File myFile = new File(favDir, fileName);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(content);
            myOutWriter.close();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes the file specified by the value of fileName.
     * @param folderPath
     * @param fileName
     */
    public void removeFromSdCard(String folderPath, String fileName) {
        File favDir = new File(SD_PATH, folderPath);
        favDir.mkdirs();
        File myFile = new File(favDir, fileName);
        myFile.delete();
    }
}
