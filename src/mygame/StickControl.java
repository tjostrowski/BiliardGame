/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.io.IOException;

/**
 *
 * @author tomek
 */
public class StickControl extends AbstractControl {
    private float forceMultiplier;
    private boolean countForceMultiplier = false;
    private BallGame ballGame;
    private static float MAX_FORCE_MULTIPLIER = 4500.f;
    
    public StickControl(BallGame ballGame) {
        this.ballGame = ballGame;
    }
    
    public void startCountForceMultiplier() {
        countForceMultiplier = true;
        forceMultiplier = 0.f;
    }
    
    public void stopCountForceMultiplier() {
        countForceMultiplier = false;
    }
    
    public float getForceMultiplier() {
        return Math.min(forceMultiplier, MAX_FORCE_MULTIPLIER);
    }
    
    @Override
    protected void controlUpdate(float tpf) {  
        if ( !countForceMultiplier ) {
            return;
        }
        
        forceMultiplier += (4.f / tpf);
    }   
    
//    public void addStateEvent(BallControl.BallState fromState, BallControl.BallState toState) {
//        eventBoard.addEvent( new EventBoard.Event(
//                (Geometry)spatial, 
//                EventBoard.EventType.BallChangedState,                
//                fromState,
//                toState) );
//    }
    
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
}
