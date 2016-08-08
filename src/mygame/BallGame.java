/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.FlyByCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;
import com.jme3.bullet.collision.shapes.*;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.shape.Cylinder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author tomek
 */
public class BallGame extends AbstractAppState implements Observer,  PhysicsCollisionListener {

    private static final Trigger TRIGGER_KEY_PUSH = new KeyTrigger(KeyInput.KEY_P);
    private static final String MAPPING_KEY_PUSH = "Map push";
    
    private static final Trigger TRIGGER_MOUSE_PUSH = new MouseButtonTrigger(MouseInput.BUTTON_LEFT);
    private static final String MAPPING_MOUSE_PUSH = "Map mouse push";
    
    private SimpleApplication app;
    private Node rootNode;
    private Camera cam;
    private FlyByCamera flyCam;
    private BulletAppState bulletAppState;
    private AssetManager assetManager;
    private InputManager inputManager;
    
    static final int NUM_BALLS = 6;
    private List<Geometry> balls;
    private Geometry mainBall;
    
    private Spatial poolTable;           
    private MainBallCamera ballCam;
    
    private Geometry marker;
    
    private int nOutOfPoolBalls;
    
    private EventBoard eventBoard;
    
    private Players player = Players.FIRST;
    
    public enum Players {
        FIRST,
        SECOND
    }
      
    public Spatial getPoolTable() {
        return poolTable;
    }
    
    public Geometry getMainBall() {
        return mainBall;
    }
    
    public List<Geometry> getBalls() {
        return balls;
    }
    
    public MainBallCamera getCamera() {
        return ballCam;
    }
    
    public int getNumOutOfPoolBalls() {
        return nOutOfPoolBalls;
    }
    
    public Players getCurrentPlayer() {
        return player;
    }
    
    public void changePlayer() {
        player = (player == Players.FIRST) ? Players.SECOND : Players.FIRST;
    }
    
    public void loadPoolTable() {        
        poolTable = assetManager.loadModel(
                    "Models/pool_table_low.j3o");                
        poolTable.setLocalScale(6.f);
        poolTable.center();        
        
        RigidBodyControl poolPhy = new RigidBodyControl(CollisionShapeFactory.createMeshShape(poolTable), 1000.f);
        poolTable.addControl(poolPhy);        
        poolPhy.setKinematic(true);
        poolPhy.setFriction(1000.f);
        poolPhy.setRestitution(1.f);        
        bulletAppState.getPhysicsSpace().add(poolPhy);        
        rootNode.attachChild(poolTable);        
    }
    
    public Geometry loadBall(Vector3f pos, ColorRGBA color, String name, Players player) {
        System.out.println("Creating ball: " + name);                          
        
        final float radius = .4f;
        
        Sphere b = new Sphere(100, 100, radius);        
        Geometry ballGeom = new Geometry(name, b);        
        ballGeom.setLocalTranslation( pos );

        Material ballMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        ballMat.setBoolean("UseMaterialColors", true);        
        ballMat.setColor("Diffuse", color );
        ballMat.setColor("Ambient", ColorRGBA.Black );        
        ballMat.setColor("Specular", ColorRGBA.White );
        ballMat.setFloat("Shininess", 50.f);
        ballGeom.setMaterial(ballMat);
        
        RigidBodyControl ballPhy = new RigidBodyControl(new SphereCollisionShape(radius), 1.f);
        ballPhy.setRestitution(1.f);
        ballGeom.addControl(ballPhy);
        ballGeom.addControl(new BallControl(this, player));
//        boxPhy.setFriction(100.f);
//        boxPhy.setCcdMotionThreshold(.1f);
        bulletAppState.getPhysicsSpace().add(ballPhy);        
        
        rootNode.attachChild( ballGeom );
                      
        return ballGeom;
    }    
        
    public float getPoolY() {
        BoundingBox volume = (BoundingBox)getPoolTable().getWorldBound();        
        return volume.getYExtent();
    }
    
    public List<Geometry> getCollisionGeometries(CollisionResults collResults) {
        List<Geometry> results = new ArrayList<Geometry>();
        
        for (CollisionResult res: collResults) {
            results.add( res.getGeometry() );
        }
        
        return results;
    }
        
    public void addKeyMappings() {
        inputManager.addMapping(MAPPING_KEY_PUSH, TRIGGER_KEY_PUSH);
        inputManager.addMapping(MAPPING_MOUSE_PUSH, TRIGGER_MOUSE_PUSH);                        
        inputManager.addListener(new ActionListener() {

            public void onAction(String name, boolean isPressed, float tpf) {
                StickControl stickControl = mainBall.getControl(StickControl.class);
                if (isPressed) {
                    System.out.println(name + " was pressed");                    
                    stickControl.startCountForceMultiplier();
                } else {
                    System.out.println(name + " was released");                
                    RigidBodyControl ballPhy = mainBall.getControl(RigidBodyControl.class);                                                
                    Vector3f mainBallPos = mainBall.getLocalTranslation();                    
                    Vector3f camPos = cam.getLocation();                    
                    Vector3f dir = mainBallPos.subtract(camPos).normalizeLocal();                    
                    dir.y = 0.f;                    
                    System.out.println("[Force multiplier]: " + stickControl.getForceMultiplier());
                    Vector3f force = dir.mult( stickControl.getForceMultiplier() );
                    ballPhy.applyCentralForce( force );
                    stickControl.stopCountForceMultiplier();
                    System.out.println("force " + force + " applied");
                    if ( marker != null ) {
                        rootNode.detachChild(marker);
                    }
                    
                    new Thread() {
                        public void run() {
//                            Util.wait(1);
                            ballCam.followToMainView();
                        }                        
                    }.start();                    
                }               
            }}, MAPPING_KEY_PUSH);        
        
        
//        // enable mouse pointer
//        inputManager.setCursorVisible(true);
////        inputManager.setSimulateMouse(true);
//        flyCam.setEnabled(false);
//        flyCam.setDragToRotate(true);
//        inputManager.addListener(new ActionListener() {
//
//            public void onAction(String name, boolean isPressed, float tpf) {
//                if (isPressed) {
//                    System.out.println("Mouse was pressed");
//                    CollisionResults results = new CollisionResults();
//                    Vector2f click2d = inputManager.getCursorPosition();
//                    Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
//                    Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();                    
//                    Ray ray = new Ray(click3d, dir);
//                    rootNode.collideWith(ray, results);
//                    
//                    List<Geometry> geoms = getCollisionGeometries(results);
//                    for ( Geometry ballGeom : balls ) {
//                        if ( geoms.contains( ballGeom ) ) {
//                            System.out.println( ballGeom + " was clicked" );
//                            RigidBodyControl ballPhy = ballGeom.getControl(RigidBodyControl.class);
//                            ballPhy.applyCentralForce(new Vector3f(500.f, 0.f, 500.f));
//                            System.out.println("force applied...");
//                        }                      
//                    }                    
//                }
//            }
//        }, MAPPING_MOUSE_PUSH);
    }
            
    
    public Vector3f[] getInitialPosDelta() {
        final float r = .8f;
        Vector3f x = new Vector3f(r, 0.f, 0.f);
        Vector3f xp = x.mult(-1.f);
        Vector3f d = new Quaternion().fromAngleAxis(30 * FastMath.DEG_TO_RAD, Vector3f.UNIT_Y)
                        .mult( new Vector3f(0.f, 0.f, r) );
        Vector3f dp = new Quaternion().fromAngleAxis(-30 * FastMath.DEG_TO_RAD, Vector3f.UNIT_Y)
                        .mult( new Vector3f(0.f, 0.f, r) );
                
        return new Vector3f[] {
            d,
            dp, x, 
            d, xp, xp, 
            dp, x, x, x, 
            d, xp, xp, xp, xp,                         
        };
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        System.out.println("[INITIALIZING BallGame]");
        
        super.initialize(stateManager, app); 
        this.app = (SimpleApplication) app;
        this.rootNode = this.app.getRootNode();
        this.cam = this.app.getCamera();
        this.flyCam = this.app.getFlyByCamera();
        this.bulletAppState = this.app.getStateManager().getState(BulletAppState.class);
        this.assetManager = this.app.getAssetManager();
        this.inputManager = this.app.getInputManager();
        this.balls = new ArrayList<Geometry>();
        this.eventBoard = EventBoard.getEventBoard();
        
        loadPoolTable();
        
        Vector3f[] dd = getInitialPosDelta();
        Vector3f pos = new Vector3f(1.5f, getPoolY() - .2f, -10.f);
        Players ballPlayer = Players.FIRST;
        for ( int i = dd.length-1; i >= 0; --i ) {            
            ColorRGBA color = (i==3) ? ColorRGBA.Black : ((ballPlayer == Players.FIRST ? ColorRGBA.Red : ColorRGBA.Blue) );
            Geometry ball = loadBall(pos, color, "Ball " + i, ballPlayer);
            balls.add(ball);
            pos.addLocal( dd[i] );
            
            ballPlayer = (ballPlayer == Players.FIRST) ? Players.SECOND : Players.FIRST;            
        }
        
        mainBall = loadBall(new Vector3f(0.f, getPoolY() - .2f, 0.f), ColorRGBA.White, "Main ball", Players.FIRST);
        mainBall.addControl( new StickControl(this) );                            
        
        addKeyMappings();
        
        flyCam.setEnabled(true);
        flyCam.setMoveSpeed(10);
        ballCam = new MainBallCamera(cam, this);
        ballCam.init();        
        ballCam.registerWithInput(inputManager);
        mainBall.addControl( ballCam );
        
        bulletAppState.getPhysicsSpace().addCollisionListener(this);
    }
    
    public void update(Observable o, Object arg) {
        EventBoard.Event event = (EventBoard.Event)arg;
        System.out.println("Game State handler received: " + event.toString());      
        if ( EventBoard.isOutOfPoolEvent(event) ) {
            nOutOfPoolBalls++;
        } else if ( event.eventType == EventBoard.EventType.CollisionWithMainBall ) {            
            Geometry ballCollided = event.obj;
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            System.out.println(dateFormat.format(cal.getTime()) + " Collision with main ball from: " + ballCollided.getName() );            
        }
    }        
    
    public void drawArrow(Vector3f from, Vector3f dir, Players player) {
        if ( marker != null ) {
            rootNode.detachChild(marker);
        }                
        
        Cylinder cyl = new Cylinder(8, 16, .05f, 2.f, true);
        marker = new Geometry("Arrow", cyl);        
        Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", (player == Players.FIRST) ? ColorRGBA.Red : ColorRGBA.Blue);
        marker.setMaterial(material);       
                                
        Vector3f o = Vector3f.UNIT_Z;
        float angle = Util.angleBetween(dir, o);        
        marker.setLocalRotation( new Quaternion().fromAngleAxis(angle, Vector3f.UNIT_Y).mult( marker.getLocalRotation() ));
        marker.setLocalTranslation( FastMath.interpolateLinear(.5f, from, from.add(dir)) );
        marker.setLocalTranslation( marker.getLocalTranslation().add(marker.getLocalRotation().mult( o.mult(-2.5f) ) ));
        rootNode.attachChild(marker);
    }    
        
    @Override
    public boolean isInitialized() {
        return super.isInitialized(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEnabled() {
        return super.isEnabled(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stateAttached(AppStateManager stateManager) {
        super.stateAttached(stateManager); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        super.stateDetached(stateManager); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(float tpf) {
        super.update(tpf); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void render(RenderManager rm) {
        super.render(rm); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void postRender() {
        super.postRender(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cleanup() {
        super.cleanup(); //To change body of generated methods, choose Tools | Templates.
    }

    public void collision(PhysicsCollisionEvent event) {
        Spatial nodeA = event.getNodeA();
        Spatial nodeB = event.getNodeB();                                
        
        Geometry ballCollided = null;
        if ( nodeA == mainBall && balls.contains(nodeB)) {
            ballCollided = (Geometry)nodeB;            
        } else if ( balls.contains(nodeA) && nodeB == mainBall ) {
            ballCollided = (Geometry)nodeA;            
        } else {
            // Ignore this collision
            return;
        }                
        
        eventBoard.addEvent(new EventBoard.Event(
                    ballCollided, 
                    EventBoard.EventType.CollisionWithMainBall));                        
    }    
}
