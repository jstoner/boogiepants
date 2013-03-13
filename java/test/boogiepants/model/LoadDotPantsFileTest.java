package boogiepants.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LoadDotPantsFileTest {

    public static void main(String args[]){
        readInstruments(args[0]);
    }
    
    private static void readInstruments(String filename) {
        ZipInputStream zipin = null;
        try {
            zipin = new ZipInputStream(new FileInputStream(filename));
            ZipEntry ze = zipin.getNextEntry();
            while (ze!= null) {
                System.out.println("name of shape from .pants file "+ ze.getName());
                ze = zipin.getNextEntry();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
