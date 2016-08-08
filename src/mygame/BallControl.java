/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import java.util.List;

/**
 *
 * @author tomek
 */
public class BallControl extends AbstractControl {
    
    public enum BallState {
        SCENE_POSITIONING,
        STILL,
        MOVING,        
        OUT_OF_POOL,
    }    
        
    private BallGame ballGame;
    private BallState ballState;    
    private RigidBodyControl bodyControl;
    private float groundPos;
    private EventBoard eventBoard;
    private BallGame.Players player;
    
    public BallControl(BallGame ballGame, BallGame.Players player) {
        this.ballGame = ballGame;
        ballState = BallState.SCENE_POSITIONING;        
        eventBoard = EventBoard.getEventBoard();
        this.player = player;
    }    
        
    @Override
    protected void controlUpdate(float tpf) {   
        bodyControl = spatial.getControl(RigidBodyControl.class);                
        Vector3f velocity = bodyControl.getLinearVelocity();        
        Vector3f pos = spatial.getLocalTranslation();

        BallState initState = ballState;
        
        if ( ballState == BallState.SCENE_POSITIONING && velocity.y == 0.f ) {
            System.out.println( toString() + "changing state from: " +  BallState.SCENE_POSITIONING.toString() + " to: " + BallState.STILL.toString());
            groundPos = spatial.getLocalTranslation().y;
            setZeroVelocity();
            ballState = BallState.STILL;                        
        } else if ( ballState == BallState.STILL && velocity.length() > .1f ) {
            System.out.println( toString() + "changing state from: " +  BallState.STILL.toString() + " to: " + BallState.MOVING.toString());
            ballState = BallState.MOVING;            
        } else if ( ballState == BallState.MOVING && pos.y < groundPos - .1f ) {
            System.out.println( toString() + "changing state from: " +  BallState.MOVING.toString() + " to: " + BallState.OUT_OF_POOL.toString());
            ballState = BallState.OUT_OF_POOL;            
        } else if ( ballState == BallState.MOVING && velocity.length() < .05f ) {
            System.out.println( toString() + "changing state from: " +  BallState.MOVING.toString() + " to: " + BallState.STILL.toString());            
            if ( isMainBall() ) {
                ballGame.changePlayer();
                ballGame.getCamera().reload();
                addSwitchPlayerEvent();
            }
            setZeroVelocity();
            ballState = BallState.STILL;            
        }
                
        handleStateChanged(initState, ballState);
        handleState();
    }
    
    public BallGame.Players getPlayer() {
        return player;
    }
    
    public void handleStateChanged(BallState fromState, BallState toState) {
        if ( fromState != toState ) {
            addChangeStateEvent(fromState, toState);
        }
    }
    
    public void addChangeStateEvent(BallState fromState, BallState toState) {
        eventBoard.addEvent( new EventBoard.Event(
                (Geometry)spatial, 
                EventBoard.EventType.BallChangedState,                
                fromState,
                toState) );
    }
    
    public void addSwitchPlayerEvent() {
        eventBoard.addEvent( new EventBoard.Event(
                (Geometry)spatial, 
                EventBoard.EventType.SwitchPlayer ));
    }
    
    public BallState getBallState() {
        return ballState;
    }
    
    public boolean isOutOfPool() {
        return ballState == BallState.OUT_OF_POOL;
    }
    
    protected void setZeroVelocity() {
        bodyControl.setLinearVelocity( Vector3f.ZERO );        
        bodyControl.setAngularVelocity( Vector3f.ZERO );                
    }
    
    protected void handleState() {
        switch ( ballState ) {
            case MOVING: {
                handleMovingState();
                break;
            }
            case STILL: {
                handleStillState();
                break;
            }                
        }                
    }
    
    protected void handleStillState() {        
    }
    
    protected void handleMovingState() {        
        Vector3f linearVelocity = bodyControl.getLinearVelocity();
        if (linearVelocity.y > .1f) {
            linearVelocity.y = 0.f;
        }        
        bodyControl.setLinearVelocity( linearVelocity.mult(0.995f) );        
        bodyControl.setAngularVelocity(bodyControl.getAngularVelocity().mult(0.995f));        
    }
    
    protected boolean isMainBall() {
        return getSpatial().equals( ballGame.getMainBall() );
    }        

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }    
        
    @Override
    public String toString() {
        if ( isMainBall() ) {
            return "[MAIN BALL]: ";
        } 
        
        List<Geometry> balls = ballGame.getBalls();
        int index = balls.indexOf(spatial);
        return "[BALL (" + index + ")]: ";       
    }
}
