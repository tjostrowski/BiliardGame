/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bounding.BoundingVolume;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;

/**
 *
 * @author tomek
 */
public class Util {

    public static float angleBetween(Vector3f v1, Vector3f v2) {
        float angle = v1.angleBetween( v2 );
        if (v1.cross(v2).y > 0) angle = 2*FastMath.PI - angle;        
        return angle;
    } 
    
    public static void wait(int seconds) {
        try {
            Thread.sleep( seconds*1000L );
        } catch ( InterruptedException e ) {            
        }        
    }
    
    public static boolean isVisibleInCamera(Camera cam, Spatial spatial) {
        BoundingVolume bv = spatial.getWorldBound();
        
        int cp = bv.getCheckPlane();
        bv.setCheckPlane(0);
        Camera.FrustumIntersect intersect = cam.contains(bv);
        bv.setCheckPlane(cp);
        
        return ( (intersect == Camera.FrustumIntersect.Inside || intersect == Camera.FrustumIntersect.Intersects) );
    }
}
