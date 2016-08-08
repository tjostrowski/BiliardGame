package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.light.AmbientLight;
import com.jme3.light.PointLight;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;

/**
 * test
 * @author normenhansen
 */
public class Main extends SimpleApplication {
    
    private BulletAppState bulletAppState;
    private RigidBodyControl boxPhy;
    private RigidBodyControl floorPhy;
    private Geometry floorGeom;

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {                
        System.out.println("[CAMERA POS]: " + cam.getLocation());
        System.out.println("[CAMERA DIR]: " + cam.getDirection());
        
        cam.setLocation(new Vector3f(20.f, 20.f, 20.f));
        Quaternion camRot = cam.getRotation();
        Quaternion rot1 = new Quaternion();
        rot1.fromAngleAxis(45 * FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
        Quaternion rot2 = new Quaternion();
        rot2.fromAngleAxis(45 * FastMath.DEG_TO_RAD, Vector3f.UNIT_X);        
        cam.setRotation(camRot.mult( rot1 ).mult( rot2 ));
        
        System.out.println("[CAMERA POS UPDATED]: " + cam.getLocation());
        System.out.println("[CAMERA DIR UPDATED]: " + cam.getDirection());
      
        PointLight pointLight = new PointLight();
        pointLight.setPosition(new Vector3f(0.f, 25.f, 0.f));
        pointLight.setColor(ColorRGBA.White);         
        rootNode.addLight(pointLight);
        
//        DirectionalLight sun = new DirectionalLight();
//        sun.setDirection(new Vector3f(0, -1, 0));
//        sun.setColor(ColorRGBA.White);
//        rootNode.addLight(sun);
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(ColorRGBA.White);
        rootNode.addLight(ambient);        
        
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        
        BallGame ballGame = new BallGame();
        stateManager.attach( ballGame );
        
        BallGame2D ballGame2D = new BallGame2D( ballGame );
        stateManager.attach( ballGame2D );        
        
        EventBoard.getEventBoard().addObserver(ballGame2D);
        EventBoard.getEventBoard().addObserver(ballGame);
        
//                
//        Box floor = new Box(Vector3f.ZERO, 10.f, .2f, 10.f);
//        floorGeom = new Geometry("Floor", floor);
//        
//        Material floorMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
//        floorMat.setBoolean("UseMaterialColors", true);
//        floorMat.setColor("Diffuse", ColorRGBA.Green );
//        floorMat.setColor("Ambient", ColorRGBA.Gray );     
//        floorMat.setColor("Specular", ColorRGBA.White);
//        floorGeom.setMaterial(floorMat);
//        
//        floorPhy = new RigidBodyControl(CollisionShapeFactory.createBoxShape(floorGeom), 10.f);
//        floorGeom.addControl(floorPhy);
////        System.out.println("[COLLISION_SHAPE]: " + ((BoxCollisionShape)floorPhy.getCollisionShape()));
//        rootNode.attachChild(floorGeom);
//        
////        floorPhy.setFriction(1000.f);
////        floorPhy.setRestitution(0.f);
//        floorPhy.setCcdMotionThreshold(1.f);
//        floorPhy.setKinematic(true);        
//        bulletAppState.getPhysicsSpace().add(floorPhy);                                
        
//        flyCam.setEnabled(true);
//        flyCam.setMoveSpeed(10.f);
        
        bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0f, -4f, 0f));
        bulletAppState.getPhysicsSpace().setAccuracy(1.f/(4*60.f));
        bulletAppState.getPhysicsSpace().setMaxSubSteps(4);
                     
//        bulletAppState.getPhysicsSpace().enableDebug(assetManager);        
        
        this.settings.setFrameRate(30);        
    }

    @Override
    public void simpleUpdate(float tpf) {        
//        floorGeom.move(0.f, .5f*tpf, 0.f);        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}
