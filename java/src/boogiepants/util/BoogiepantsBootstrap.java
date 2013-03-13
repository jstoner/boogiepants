/*
 * SweetHome3DBootstrap.java 2 sept. 07
 *
 * Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Copied from Emmanuel's SweetHome3DBootstrap.java. Changed class from
 * com.eteks.sweethome3d.SweetHome3DBootstrap to
 * boogiepants.util.BoogiepantsBootstrap
 * 
 */
package boogiepants.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

/**
 * This bootstrap class loads boogiepants application classes from jars in classpath 
 * or from extension jars stored as resources.
 * @author Emmanuel Puybaret
 */
public class BoogiepantsBootstrap {
  public static void main(String [] args) throws MalformedURLException, IllegalAccessException, 
        InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    Class boogiepantsBootstrapClass = BoogiepantsBootstrap.class;
    String [] java3DFiles = {
            "j3dcore.jar", // Main Java 3D 1.5.1 jars
            "vecmath.jar",
            "j3dutils.jar",
            "j3dcore-d3d.dll", // Windows DLLs
            "j3dcore-ogl-cg.dll",
            "j3dcore-ogl-chk.dll",
            "j3dcore-ogl.dll",
            "bluecove.dll",
            "libj3dcore-ogl.so", // Linux DLLs
            "libj3dcore-ogl-cg.so",
            "gluegen-rt.jar", // Mac OS X jars and DLLs
            "jogl.jar",
            "libgluegen-rt.jnilib",
            "libjogl.jnilib",
            "libjogl_awt.jnilib",
            "libjogl_cg.jnilib"};
    String [] applicationPackages = {
            // Your product package
            "boogiepants.display",
            "boogiepants.instruments",
            "boogiepants.wiiInput",
            "boogiepants.model",
            "boogiepants.util",
            // Java 3D packages
        "javax.media.j3d",
        "javax.vecmath",
        "com.sun.j3d",
        "com.sun.opengl",
        "com.sun.gluegen.runtime",
        "javax.media.opengl",
        "javax.media.opengl.glu",
        "com.sun.opengl.impl.macosx",
        "com.sun.j3d.utils.universe"
        // Add some other packages used by your application
        };
    ClassLoader java3DClassLoader = new ExtensionsClassLoader(
        boogiepantsBootstrapClass.getClassLoader(), 
        boogiepantsBootstrapClass.getProtectionDomain(),
        java3DFiles, applicationPackages);  
    
    String applicationClassName = "boogiepants.util.WiiJarLoader";
    Class applicationClass = java3DClassLoader.loadClass(applicationClassName);
    Method applicationClassMain = 
      applicationClass.getMethod("main", Array.newInstance(String.class, 0).getClass());
    // Call application class main method with reflection
    applicationClassMain.invoke(null, new Object [] {args});
  }
}