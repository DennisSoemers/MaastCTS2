package MaastCTS2.utils;

public class OrderedIntPair {
	
	public int first;
	public int second;
	
	public OrderedIntPair(int first, int second){
		this.first = first;
		this.second = second;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + first;
		result = prime * result + second;
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
		
		if (!(otherObject instanceof OrderedIntPair)){
			return false;
		}
		
		OrderedIntPair other = (OrderedIntPair) otherObject;
		return (first == other.first 	&&
				second == other.second		);
	}
	
	@Override
	public String toString(){
		return "(" + first + ", " + second + ")";
	}

}
