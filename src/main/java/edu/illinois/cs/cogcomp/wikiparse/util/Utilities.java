package edu.illinois.cs.cogcomp.wikiparse.util;

import java.io.*;
import java.util.*;

/**
 * Created by nitishgupta on 9/30/16.
 */
public class Utilities {

	public static <T> void serializeObject(T ob, String pathToWrite) {
		try (
						OutputStream file = new FileOutputStream(pathToWrite);
						OutputStream buffer = new BufferedOutputStream(file);
						ObjectOutput output = new ObjectOutputStream(buffer);
		) {
			output.writeObject(ob);
			System.out.println("Serialized Object successfully written.");
		} catch (IOException ex) {
			System.err.println("Cannot Write Serialized Object");
		}
	}

	public static <T> T deserializeObject(String filePath) {
		T ob = null;
		try {
			FileInputStream fileIn = new FileInputStream(filePath);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			ob = (T) in.readObject();
			in.close();
			fileIn.close();
			return ob;
		} catch (IOException i) {
			i.printStackTrace();
		} catch (ClassNotFoundException c) {
			System.out.println("Corpus class type object not found");
			c.printStackTrace();
		}
		if (ob == null) {
			System.err.print("Error in reading serialized object.");
		}
		return ob;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V>  sortByDecreasingValue( Map<K, V> map ) {
		List<Map.Entry<K, V>> list =
						new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
		{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				return (o2.getValue()).compareTo( o1.getValue() );
			}
		} );

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Map.Entry<K, V> entry : list)
		{
			result.put( entry.getKey(), entry.getValue() );
		}
		return result;
	}

}
