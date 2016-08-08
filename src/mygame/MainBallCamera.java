/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bounding.BoundingBox;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import static mygame.BallGame.NUM_BALLS;

/**
 *
 * @author tomek
 */
public class MainBallCamera extends AbstractControl implements ActionListener, AnalogListener {
    private Camera cam;
    private BallGame ballGame;
    private boolean enabled = true;        
    private Geometry currentBallToLookAt;
    
    private boolean following;
    private Vector3f followPoint;
    
    public MainBallCamera(Camera cam, BallGame ballGame) {
        this.cam = cam;
        this.ballGame = ballGame;
        this.following = false;
    }
    
    public Geometry getClosestBallToMain() {
        List<Geometry> balls = ballGame.getBalls();                        
        Geometry closest = null;
        float minDist = Float.MAX_VALUE;
        for ( Geometry geom : balls ) {
            BallControl ballControl = geom.getControl(BallControl.class);
            if ( ballControl != null && ballControl.isOutOfPool() ) {
                continue;
            }            
            float dist = geom.getLocalTranslation().distance( ballGame.getMainBall().getLocalTranslation() ); 
            if ( dist < minDist ) {
                minDist = dist;
                closest = geom;
            }
        }
        return closest;
    }
    
    public Geometry getCurrentBallToLookAt() {
        return currentBallToLookAt;
    }
    
    public void init() {        
        setFollowing( false );
        currentBallToLookAt = getClosestBallToMain();        
        Vector3f ballPos = currentBallToLookAt.getLocalTranslation();
        System.out.println("Position to look at: " + ballPos);
        Vector3f mainBallPos = ballGame.getMainBall().getLocalTranslation();        
        System.out.println("Main ball position: " + mainBallPos);
        Vector3f dir = mainBallPos.subtract( ballPos ).normalizeLocal();                
        cam.setLocation( mainBallPos.add(dir.mult(10.f)).add(0.f, ballGame.getPoolY()+2.f, 0.f) );                
        cam.lookAt(ballPos, Vector3f.UNIT_Y);           
        System.out.println("Camera location: " + cam.getLocation());             
        
        drawArrow();
    }
    
    public void reload() {
        System.out.println("[CAMERA RELOADING]!!!");
        init();
    }        
    
    public void registerWithInput(InputManager inputManager) {
        String[] mappings = new String[] {
            "RotateL",
            "RotateR"
        }; 
        
        inputManager.addMapping("RotateL", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addMapping("RotateR", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
        
        inputManager.addListener(this, mappings);
    }
            
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    } 
    
    public void setFollowing(boolean following) {
        this.following = following;
    }
    
    public boolean isFollowing() {
        return following;
    }
        
    public void onAction(String name, boolean value, float tpf) {
        if (!enabled) return;        
        
        Vector3f mainBallPos = ballGame.getMainBall().getLocalTranslation();
        Vector3f camPos = cam.getLocation();
        Vector3f lookTo = currentBallToLookAt.getLocalTranslation();
        
        float angle;
        if (name.equals("RotateL")) {
            angle = -.5f * FastMath.DEG_TO_RAD;
        } else if (name.equals("RotateR")) {
            angle = .5f * FastMath.DEG_TO_RAD;
        } else {
            return;
        }
        
        Vector3f pointToRot = camPos.subtract(mainBallPos);
        pointToRot.y = camPos.y;
        Vector3f pp = new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y).mult(pointToRot);
        cam.setLocation( pp.addLocal(mainBallPos.x, 0.f, mainBallPos.z) );
        cam.lookAt(lookTo, Vector3f.UNIT_Y);                
        
        drawArrow();
    }
    
    public void drawArrow() {
        Vector3f mainBallPos = ballGame.getMainBall().getLocalTranslation();
        Vector3f camPos = new Vector3f( cam.getLocation() );
        camPos.y = mainBallPos.y;
        Vector3f dir = mainBallPos.subtract(camPos).normalize();
                
        ballGame.drawArrow(mainBallPos, dir, ballGame.getCurrentPlayer());       
    }

    public void onAnalog(String name, float value, float tpf) {
        onAction(name, true, tpf);
    }

//    @Override
//    protected void controlUpdate(float tpf) {        
//    }
//
//    @Override
//    protected void controlRender(RenderManager rm, ViewPort vp) {        
//    }
    
    public void setMainView() {
        Vector3f dir = cam.getDirection().mult( -1.f );
        Vector3f camPos = cam.getLocation();
        cam.setLocation( camPos.add( dir.mult(10.f) ) );        
    }
    
    public void followToMainView() {
        Vector3f dir = cam.getDirection().mult( -1.f );
        Vector3f camPos = cam.getLocation();
        followToPoint( camPos.add( dir.mult(10.f) ) );
    }
    
    public void followToPoint( Vector3f point ) {
        setFollowing( true );
        followPoint = point;        
    }

    @Override
    protected void controlUpdate(float tpf) {      
        if ( isFollowing() ) {
            Vector3f camPos = cam.getLocation();
            
            if ( camPos.distance(followPoint) < .1f ) {
                
                if ( !Util.isVisibleInCamera(cam, ballGame.getMainBall()) ) {
                    Vector3f dir = cam.getDirection().mult( -1.f );
                    followToPoint( camPos.add( dir.mult(3.f) ) );                    
                } else {                
                    setFollowing(false);
                    return;
                }                
            }        
            
            Vector3f dir = followPoint.subtract( camPos ).normalize();
            
            cam.setLocation( camPos.add(dir.mult(tpf * 4.f)) );                        
        }        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {        
    }
}
