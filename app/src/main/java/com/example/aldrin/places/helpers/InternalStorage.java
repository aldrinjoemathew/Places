package com.example.aldrin.places.helpers;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by aldrin on 2/9/16.
 * To add or remove files from SD card.
 */

public class InternalStorage {
    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath();

    /**
     * Creates a folder in SD card specified by the path value folderPath.
     * Creates a file named placeId inside the folder.
     * Write the content into the file.
     * @param context
     * @param folderPath
     * @param fileName
     * @param content
     */
    public void addToSdCard(Context context, String folderPath, String fileName, String content) {
        try {
            File favDir = new File(context.getFilesDir(), folderPath);
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
     * @param context
     * @param folderPath
     * @param fileName
     */
    public void removeFromSdCard(Context context, String folderPath, String fileName) {
        File favDir = new File(context.getFilesDir(), folderPath);
        favDir.mkdirs();
        File myFile = new File(favDir, fileName);
        myFile.delete();
    }

    /**
     * Reads information from internal storage.
     * @param context
     * @param folderPath
     * @param fileName
     * @return
     */
    public String getFromSdCard(Context context, String folderPath, String fileName) {
        File fileDirectory = new File(context.getFilesDir(), folderPath);
        fileDirectory.mkdirs();
        String dataFromFile = "";
        try {
            File myFile = new File(fileDirectory, fileName);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(
                    new InputStreamReader(fIn));
            String aDataRow = "";
            while ((aDataRow = myReader.readLine()) != null) {
                dataFromFile += aDataRow + "\n";
            }
            myReader.close();
        } catch (Exception e) {
        }
        return dataFromFile;
    }
}
