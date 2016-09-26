package MaastCTS2.model;

import ontology.Types.ACTIONS;

/**
 * Encapsulates an action with a location in which the avatar executes that action
 *
 * @author Dennis Soemers
 */
public class ActionLocation {
	
	private ACTIONS action;
	private int avatarCell;
	
	public ActionLocation(ACTIONS action, int avatarCell){
		this.action = action;
		this.avatarCell = avatarCell;
	}
	
	public ACTIONS getAction(){
		return action;
	}
	
	public int getAvatarCell(){
		return avatarCell;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + action.hashCode();
		result = prime * result + avatarCell;
		
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		
		if (obj == null){
			return false;
		}
		
		if(!(obj instanceof ActionLocation)){
			return false;
		}
		
		ActionLocation other = (ActionLocation) obj;
		return (action == other.getAction() && avatarCell == other.getAvatarCell());
	}

}
