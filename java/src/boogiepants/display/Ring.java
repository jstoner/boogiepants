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

package boogiepants.display;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;

/**
 * creates geometry for ring shape used with pelvic circle instrument
 * 
 * @author jstoner
 *
 */
public class Ring extends Shape3D {

    private Appearance voAppearance;
    private Color3f color;

    /**
     * @param color 
     * 
     */
    public Ring(Color3f color) {
        this.color = color;
        createGeometry(); 
        voAppearance = createAppearance(); 
        this.setAppearance(voAppearance); 
    }

    /**
     * creates overall appearance parameters
     * @return
     */
    private Appearance createAppearance() {
        Appearance a = new Appearance();
        ColoringAttributes c = new ColoringAttributes();
        c.setShadeModel(ColoringAttributes.SHADE_FLAT);
        a.setColoringAttributes(c);
        PolygonAttributes p = new PolygonAttributes(); 
        p.setCullFace(PolygonAttributes.CULL_NONE);
        a.setPolygonAttributes(p);

        return a;
    }

    /**
     * creates actual ring geometry
     * 
     */
    private void createGeometry() {
        int numFaces = 30;
        Point3d topVertices[]= new Point3d[numFaces * 2];
        Point3d bottomVertices[]= new Point3d[numFaces * 2];
        Point3d outerVertices[]= new Point3d[numFaces * 2];
        Point3d innerVertices[]= new Point3d[numFaces * 2];
        Color3f[] colors = new Color3f[numFaces * 2];
        double innerDiam = .8;
        double outerDiam = 1;
        
        double angle;
        int i;
        for (i = 0, angle = 0; i<numFaces; angle = 2.0 * Math.PI / (numFaces - 1) * ++i){
            double x = innerDiam * Math.cos(angle);
            double z = innerDiam * Math.sin(angle);
            topVertices[i * 2] = new Point3d(x, 0.025, z);
            bottomVertices[i * 2] = new Point3d(x, -0.025, z);
            x = outerDiam * Math.cos(angle);
            z = outerDiam * Math.sin(angle);
            topVertices[i * 2 + 1] = new Point3d(x, 0.025, z);
            bottomVertices[i * 2 + 1] = new Point3d(x, -0.025, z);

            outerVertices[i * 2] = new Point3d(topVertices[i * 2]);
            outerVertices[i * 2 + 1] = new Point3d(bottomVertices[i * 2]);
            innerVertices[i * 2] = new Point3d(topVertices[i * 2 + 1]);
            innerVertices[i * 2 + 1] = new Point3d(bottomVertices[i * 2 + 1]);

            colors[i * 2] = color;
            colors[i * 2 + 1] = color;
        }
        colors[numFaces * 2 - 2] = new Color3f(0, 0, 0);
        colors[numFaces * 2 - 1] = new Color3f(0, 0, 0);

        TriangleStripArray tsa = new TriangleStripArray(numFaces * 2,
                                                        TriangleStripArray.COORDINATES|TriangleStripArray.COLOR_3, 
                                                        new int[]{numFaces * 2});
        tsa.setCoordinates(0, topVertices);
        tsa.setColors(0, colors);
        this.addGeometry(tsa);
        
        tsa = new TriangleStripArray(numFaces * 2,
                TriangleStripArray.COORDINATES|TriangleStripArray.COLOR_3, 
                new int[]{numFaces * 2});
        tsa.setCoordinates(0, bottomVertices);
        tsa.setColors(0, colors);
        this.addGeometry(tsa);

        tsa = new TriangleStripArray(numFaces * 2,
                TriangleStripArray.COORDINATES|TriangleStripArray.COLOR_3, 
                new int[]{numFaces * 2});
        tsa.setCoordinates(0, innerVertices);
        tsa.setColors(0, colors);
        this.addGeometry(tsa);

        tsa = new TriangleStripArray(numFaces * 2,
                TriangleStripArray.COORDINATES|TriangleStripArray.COLOR_3, 
                new int[]{numFaces * 2});
        tsa.setCoordinates(0, outerVertices);
        tsa.setColors(0, colors);
        this.addGeometry(tsa);
    }

}
