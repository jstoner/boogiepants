/*
 *
 * Copyright (c) 2008-2009, John Stoner
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this 
 * list of conditions and the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * Neither the name of the John Stoner nor the names of its contributors may be 
 * used to endorse or promote products derived from this software without specific 
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * 
 * John Stoner is reachable at johnstoner2 [[at]] gmail [[dot]] com.
 * His current physical address is
 * 
 * 2358 S Marshall 
 * Chicago, IL 60623
 */

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