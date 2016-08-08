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
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @author tomek
 */
public class BallGame2D extends AbstractAppState implements Observer {

    private BallGame ballGame;
    
    private SimpleApplication app;
    private Node guiNode;
    private AssetManager assetManager;
    private BitmapFont guiFont;
    private AppSettings settings;
    
    private BitmapText nOutOfPoolBallsText;            
    private BitmapText playerText;
    private BitmapText forceText;
    
    private int currentPlayer = 0;
    private String[] playerTexts =  new String[] { "Player 1", "Player 2" };
    
    public BallGame2D(BallGame ballGame) {
        this.ballGame = ballGame;
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        System.out.println("[INITIALIZING BallGame2D]");
        
        super.initialize(stateManager, app); 
        this.app = (SimpleApplication) app;
        this.guiNode = this.app.getGuiNode();
        this.assetManager = this.app.getAssetManager();        
        this.settings = this.app.getContext().getSettings();
                        
        loadFontAndTexts();
    }        
    
    public void loadFontAndTexts() {
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        
        nOutOfPoolBallsText = new BitmapText(guiFont);
        nOutOfPoolBallsText.setSize(24);//guiFont.getCharSet().getRenderedSize());
        nOutOfPoolBallsText.move(
            20,            
            settings.getHeight() - nOutOfPoolBallsText.getLineHeight(), 
            0);            
        nOutOfPoolBallsText.setText("0");
        guiNode.attachChild(nOutOfPoolBallsText);
        
        playerText = new BitmapText(guiFont);
        playerText.setSize(24);
        playerText.move(
            settings.getWidth() - 100,
            settings.getHeight() - nOutOfPoolBallsText.getLineHeight(), 
            0);
        playerText.setText( playerTexts[currentPlayer++] );
        guiNode.attachChild(playerText);
        
        forceText = new BitmapText(guiFont);
        forceText.setSize(24);
        forceText.move(
            settings.getWidth() - 250,
            settings.getHeight() - nOutOfPoolBallsText.getLineHeight(), 
            0);
        forceText.setText( "Force: 0" );        
        guiNode.attachChild(forceText);
    }    
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
    }
        
    public void update(Observable o, Object arg) {
        EventBoard.Event event = (EventBoard.Event)arg;
        System.out.println("2D handler received: " + event.toString()); 
        if ( EventBoard.isOutOfPoolEvent(event) ) {
            nOutOfPoolBallsText.setText( Integer.toString( ballGame.getNumOutOfPoolBalls() ) );
        } else if ( event.eventType == EventBoard.EventType.SwitchPlayer ) {
            currentPlayer = ++currentPlayer % 2;
            playerText.setText( playerTexts[currentPlayer] );
        }
    }        
}
