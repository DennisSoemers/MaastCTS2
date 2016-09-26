package MaastCTS2.model;


public class ActionNGram {
	private ActionLocation[] actions;

	public ActionNGram(ActionLocation[] actions) {
		this.actions = actions;
	}
	
	private ActionLocation[] getActions(){
		return actions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		for(ActionLocation action : actions){
			result = prime * result + action.hashCode();
		}
		
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
		
		if(!(obj instanceof ActionNGram)){
			return false;
		}
		
		ActionNGram other = (ActionNGram) obj;
		ActionLocation[] otherActions = other.getActions();
		
		if(actions.length != otherActions.length){
			return false;
		}
		
		for(int i = 0; i < actions.length; ++i){
			if(!(actions[i].equals(otherActions[i]))){
				return false;
			}
		}

		return true;
	}
	
	@Override
	public String toString(){
		String result = "[";
		for(int i = 0; i < actions.length; ++i){
			result += actions[i].getAction();
			
			if(i < actions.length - 1){
				result += ", ";
			}
		}
		return result + "]";
	}
	
}
