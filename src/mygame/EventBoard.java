/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.scene.Geometry;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 *
 * @author tomek
 */
public class EventBoard extends Observable {

    private static EventBoard eventBoard;
    
    private EventBoard() {        
    }
    
    public static EventBoard getEventBoard() {
        if ( eventBoard == null ) {
            eventBoard = new EventBoard();
        }
        return eventBoard;        
    }
    
    public enum EventType {
        BallChangedState,
        SwitchPlayer,
        GameFinished,
        CollisionWithMainBall,
    }    
    
    public static class Event {
        public Geometry obj;
        public EventType eventType;
        public Object arg1;
        public Object arg2;
        public long timestamp;
        
        public Event(Geometry obj, EventType et, Object arg1, Object arg2) {
            this.obj = obj;
            this.eventType = et;
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.timestamp = System.currentTimeMillis();
        }
        
        public Event(Geometry obj, EventType et, Object arg1) {
            this(obj, et, arg1, null);
        }
        
        public Event(Geometry obj, EventType et) {
            this(obj, et, null, null);
        }
        
        @Override
        public boolean equals(Object o) {
            if ( this == o ) return true;
            if ( o == null ) return false;
            if ( !(o instanceof Event) ) return false;
            
            Event e = (Event)o;
            
            return  obj.equals(e.obj) &&
                    eventType == e.eventType &&
                    ((arg1 == null && e.arg1 == null) || arg1.equals(e.arg1)) &&
                    ((arg2 == null && e.arg2 == null) || arg2.equals(e.arg2));
        }
        
        @Override
        public String toString() {
            return "[" + obj.getName() + ": " + eventType.toString() + "]"; 
        }
    }
    
    public static boolean isOutOfPoolEvent(Event e) {
        return ( e.eventType == EventType.BallChangedState && 
                e.arg2 == BallControl.BallState.OUT_OF_POOL );
    }
    
    List<Event> eventsCache = new ArrayList<Event>();
    
    public void addEvent(Event event) {                
        for ( Event e : eventsCache ) {
            if ( event.equals(e) && (event.timestamp-e.timestamp) < 2*1000L ) {
                // Found same event, skipping
                return;
            }            
        }
        
        eventsCache.add(event);
        if (eventsCache.size() > 100) {
            eventsCache.remove(0);
        }
                        
           
        setChanged();
        notifyObservers(event);
        clearChanged();
    }        
}
