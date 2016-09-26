package MaastCTS2.iw;

import tools.Vector2d;
import MaastCTS2.Globals;

/**
 * Contains data describing that a sprite was observed in a certain position at a certain point in the game.
 * Used for novelty tests.
 * 
 * <p>Contains the same data as described in:
 *  Tomas Geffner and Hector Geffner. Width-based Planning for General 
 *  Video-Game Playing. ( link: http://giga15.ru.is/giga15-paper2.pdf )
 *
 * @author Dennis Soemers
 */
public class ObservedSprite {
	
	public int position;
	public int stype;
	
	public ObservedSprite(Vector2d position, int stype){
		this.position = Globals.knowledgeBase.positionToInt(position);
		this.stype = stype;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + stype;
		result = prime * result + position;
		return result;
	}

	@Override
	public boolean equals(Object otherObject) {
		if (this == otherObject){
			return true;
		}
		
		if (otherObject == null){
			return false;
		}
		
		if (!(otherObject instanceof ObservedSprite)){
			return false;
		}
		
		ObservedSprite other = (ObservedSprite) otherObject;
		return (position == other.position	&&
				stype == other.stype			);
	}
	
	@Override
	public String toString(){
		return "[Sprite of type: \'" + stype + "\' observed in position: \'" + position + "\']";
	}

}
