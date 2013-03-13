package boogiepants.instruments;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;

import boogiepants.display.FileShape;

/**
 * A cache for shapes. Don't directly call new FileShape, call one of 
 * the importShape methods, and it will either get the shape from the 
 * filesystem and cache it, or get it from the cache.
 * 
 * @author jstoner
 *
 */
public class ShapeCache {
    private HashMap<String, FileShape> importedShapes;

    /**
     * get a new empty cache
     */
    public ShapeCache() {
        importedShapes = new HashMap<String, FileShape>();
    }
    
    /**
     * returns a shape from a file--imported directly or cached
     * 
     * @param keyFilename
     * @return
     */
    public FileShape importShape(String filename) {
        String keyFilename = shapeCacheKey(filename);
        if (importedShapes.containsKey(keyFilename)) {
            return importedShapes.get(keyFilename).clone();
        } else {
            FileShape shape = new FileShape(filename, 60);
            importedShapes.put(keyFilename, shape);
            return shape.clone();
        }
    }

    /**
     * returns a shape from an InputStream--imported directly or cached
     * @param input
     * @param keyFilename
     * @return
     */
    public FileShape importShape(InputStream input, String filename) {
        String keyFilename = shapeCacheKey(filename);
        if (importedShapes.containsKey(keyFilename)) {
            return importedShapes.get(keyFilename).clone();
        } else {
            FileShape shape = new FileShape(input, filename, 60);
            importedShapes.put(keyFilename, shape);
            return shape.clone();
        }
    }
    
    /**
     * returns the cache key for this shape
     * 
     * @param filename
     * @return
     */
    public String shapeCacheKey(String filename){
        int sep = filename.lastIndexOf(File.separator);
        if(sep > -1){
            return filename.substring(sep + 1);
        } else {
            sep = filename.lastIndexOf("/");// zip files use '/'
            if(sep > -1){
                return filename.substring(sep + 1);
            }
        }
        return filename;
    }
    
    /**
     * encapsulating a HashMap thinly here--why hide it?
     * @return
     */
    public Set<String> keySet(){
        return importedShapes.keySet();
    }
    
    /**
     * encapsulating a HashMap thinly here--why hide it?
     */
    public void clear(){
        importedShapes.clear();
    }
    
    /**
     * encapsulating a HashMap thinly here--why hide it?
     * @return
     */
    public boolean isEmpty(){
        return importedShapes.isEmpty();
    }
}