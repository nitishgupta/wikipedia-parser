package edu.illinois.cs.cogcomp.wikiparse.util;

import java.io.*;

/**
 * Created by nitishgupta on 9/30/16.
 */
public class Utilities {

    public static <T> void serializeObject(T ob, String pathToWrite){
        try (
                OutputStream file = new FileOutputStream(pathToWrite);
                OutputStream buffer = new BufferedOutputStream(file);
                ObjectOutput output = new ObjectOutputStream(buffer);
        ){
            output.writeObject(ob);
            System.out.println("Serialized Object successfully written.");
        }
        catch(IOException ex){
            System.err.println("Cannot Write Serialized Object");
        }
    }

    public static <T> T deserializeObject(String filePath){
        T ob = null;
        try
        {
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            ob = (T) in.readObject();
            in.close();
            fileIn.close();
            return ob;
        }catch(IOException i) {
            i.printStackTrace();
        }catch(ClassNotFoundException c) {
            System.out.println("Corpus class type object not found");
            c.printStackTrace();
        }
        if(ob == null){
            System.err.print("Error in reading serialized object.");
        }
        return ob;
    }
}
