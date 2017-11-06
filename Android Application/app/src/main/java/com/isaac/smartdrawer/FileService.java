package com.isaac.smartdrawer;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileService {
    Context context;
    public FileService(Context context){
        this.context=context;
    }

    public void FileSave(String filename,String content) throws IOException{
        FileOutputStream fos=context.openFileOutput(filename, Context.MODE_PRIVATE);
        fos.write(content.getBytes());
        fos.close();
    }

    public String FileRead(String filename) throws IOException{
        FileInputStream fin=context.openFileInput(filename);
        byte[] b=new byte[fin.available()];
        ByteArrayOutputStream buffer=new ByteArrayOutputStream();

        while((fin.read(b))!=-1){
            buffer.write(b);
        }
        byte[] data;
        data=buffer.toByteArray();

        buffer.close();
        fin.close();
        return new String(data);
    }

}
