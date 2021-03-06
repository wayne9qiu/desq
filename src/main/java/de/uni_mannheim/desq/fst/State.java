package de.uni_mannheim.desq.fst;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;


public final class State {

	int id;
	// List of transitions
	ArrayList<Transition> transitionList;
	boolean isFinal;
	boolean isFinalComplete = false;

	public State() {
		this(false);
	}
	
	
	public State(boolean isFinal) {
		this.transitionList = new ArrayList<>();
		this.isFinal = isFinal;
	}
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public void addTransition(Transition t) {
		transitionList.add(t);
	}
	
	public void simulateEpsilonTransitionTo(State to) {
		if (to.isFinal)
			isFinal = true;
		for (Transition t : to.transitionList) {
			transitionList.add(t);
		}
	}
	
	public static final class ItemStateIterator implements Iterator<ItemState> {
		private ArrayList<Transition> transitions;
		private int nextTransitionIndex;
		private Iterator<ItemState> currentIt;
		private boolean currentItHasNext;
		private BitSet validToStates;
		private int fid;
		private final Transition.ItemStateIteratorCache itCache;

		public ItemStateIterator(boolean isForest) {
			 itCache = new Transition.ItemStateIteratorCache(isForest);
		}

		public boolean hasNext() {
			if (!currentItHasNext) {
				moveToNextTransition();
			}
			return currentItHasNext;
		}

		public ItemState next() {
			ItemState result = currentIt.next();
			currentItHasNext = currentIt.hasNext();
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		void moveToNextTransition() {
			assert !currentItHasNext;
			while (!currentItHasNext & nextTransitionIndex < transitions.size()) {
				Transition nextTransition = transitions.get(nextTransitionIndex++);
				if (validToStates == null || validToStates.get(nextTransition.toState.getId())) {
					currentIt = nextTransition.consume(fid, itCache);
					currentItHasNext = currentIt.hasNext();
				}
			};
		}
	}
	
	public ItemStateIterator newIterator(boolean isForest) {
		return new ItemStateIterator(isForest);
	}
	
	public ItemStateIterator consume(int itemFid, ItemStateIterator it) {
		return consume(itemFid, it, null);
	}

	/** Returns an iterator over (output item, next state)-pairs consistent with the given input item. Only
	 * produces pairs for which the next state is contained in validToStates (BitSet indexed by state ids).
	 *
	 * If the output item is epsilon, returns (0, next state) pair.
	 *
	 * @param fid input item
	 * @param it iterator to reuse
	 * @param validToStates set of next states to consider
	 *
	 * @return an iterator over (output item fid, next state) pairs
	 */
	public ItemStateIterator consume(int fid, ItemStateIterator it, BitSet validToStates) {
		it.transitions = transitionList;
		it.nextTransitionIndex = 0;
		it.validToStates = validToStates;
		it.fid = fid;
		it.currentItHasNext = false;
		return it;
	}

	public boolean isFinal() { 
		return isFinal; 
	}

	public boolean isFinalComplete() {
		return isFinalComplete;
	}
	
	public List<Transition> getTransitions() {
		return transitionList;
	}
	
	private static class StateIterator implements Iterator<State> {
		Iterator<Transition> transitionsIt;
		Transition transition;
		int fid;
		State toState;
		BitSet toStatesOutput = new BitSet(); // to states already output
		@Override
		public boolean hasNext() {
			while(transitionsIt.hasNext()) {
				transition = transitionsIt.next();
				if(!toStatesOutput.get(transition.toState.id) && transition.matches(fid)) { // returns false if toStatesOutput is to small
					toState = transition.toState; 
					toStatesOutput.set(toState.id); // automatically resizes upwards if necessary
					return true;
				}
			}
			toState = null;
			return false;
		}

		@Override
		public State next() {
			return toState;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	public StateIterator toStateIterator(int itemFid) {
		return toStateIterator(itemFid, null);
	}
	
	public StateIterator toStateIterator(int itemFid, StateIterator it) {
		if(it != null) {
			it.toStatesOutput.clear();
		} else {
			it = new StateIterator();
		}
		
		it.transitionsIt = transitionList.iterator();
		it.fid = itemFid;
		it.toState = null;
		return it;
	}

	/** Remove all outgoing transitions from this state */
	public void removeAllTransitions() {
		transitionList.clear();
	}
}
