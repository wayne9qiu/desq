package de.uni_mannheim.desq.fst;

import java.util.Iterator;

import de.uni_mannheim.desq.dictionary.Dictionary;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

public class BasicTransition extends Transition {
	public enum InputLabelType { SELF, SELF_DESCENDANTS };
	public enum OutputLabelType { SELF, SELF_ASCENDANTS, CONSTANT, EPSILON };

	// parameters
	final int inputLabel; 
	final InputLabelType inputLabelType;
	final int outputLabel; // only when outputlabel = constant or self_gen
	final OutputLabelType outputLabelType;

	// internal indexes
	final IntSet inputFids;
	final Dictionary outputDict;

	private BasicTransition(BasicTransition other) {
		this.inputLabel = other.inputLabel;
		this.inputLabelType = other.inputLabelType;
		this.outputLabel = other.outputLabel;
		this.outputLabelType = other.outputLabelType;
		this.inputFids = other.inputFids;
		this.outputDict = other.outputDict;
	}
	
	// */* (no dots)
	public BasicTransition(int inputLabel, InputLabelType inputLabelType, 
			int outputLabel, OutputLabelType outputLabelType, 
			State toState, Dictionary dict) {
		this.inputLabel = inputLabel;
		this.inputLabelType = inputLabelType;
		this.outputLabel = outputLabel;
		this.outputLabelType = outputLabelType;
		this.toState = toState;

		if (inputLabel == 0)
			inputFids = null;
		else switch (inputLabelType) {
		case SELF:
			inputFids = IntSets.singleton(inputLabel);
			break;
		case SELF_DESCENDANTS:
			inputFids = dict.descendantsFids(inputLabel);
			break;
		default:
			 inputFids = null;
		}
		
		if (outputLabelType == OutputLabelType.SELF_ASCENDANTS) {
			if (outputLabel == 0) 
				outputDict = dict;
			else
				outputDict = dict.restrictedCopy(dict.descendantsFids(outputLabel));
		} else outputDict = null;
	}	

	
	private static class ItemStateIterator implements Iterator<ItemState> {
		BasicTransition transition;
	    int fid;
		ItemState itemState = new ItemState();
		IntIterator fidIterator;
		
		@Override
		public boolean hasNext() {
            return fid>=0;
		}

		@Override
		public ItemState next() {
			assert fid>=0;

			switch (transition.outputLabelType) {
			case EPSILON:
				itemState.itemFid = 0;
				fid = -1;
				break;
			case CONSTANT:
				itemState.itemFid = transition.outputLabel;
				fid = -1;
				break;
			case SELF:
				itemState.itemFid = fid;
				fid = -1;
				break;
			case SELF_ASCENDANTS:
				itemState.itemFid = fid;
				if (fidIterator.hasNext())
					fid = fidIterator.nextInt();
				else
					fid = -1;
			}

			return itemState;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	@Override
	public boolean matches(int itemFid) {
		return inputLabel==0 || inputFids.contains(itemFid);
	}
	
	@Override
	public Iterator<ItemState> consume(int itemFid) {
		return consume(itemFid, null);
	}

	@Override
	public Iterator<ItemState> consume(int itemFid, Iterator<ItemState> it) {
		ItemStateIterator resultIt = null;
		if (it != null && it instanceof ItemStateIterator) 
			resultIt = (ItemStateIterator)it;
		else
			resultIt = new ItemStateIterator();
		
		if (inputLabel==0 || inputFids.contains(itemFid)) {
			resultIt.transition = this;
		    resultIt.fid = itemFid;
			resultIt.itemState.state = toState;
			if (outputLabelType == OutputLabelType.SELF_ASCENDANTS) {
				resultIt.fidIterator = outputDict.ascendantsFids(itemFid).iterator();
				resultIt.fid = resultIt.fidIterator.nextInt();
			}
		} else {
			resultIt.fid = -1;
		}

		return resultIt;
	}

	@Override
	public Transition shallowCopy() {
		return new BasicTransition(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(inputLabel == 0)
			sb.append(".");
		else {
			sb.append(inputLabel);
			if(inputLabelType == InputLabelType.SELF) 
				sb.append("=");
		}
		sb.append(":");
		switch(outputLabelType) {
		case SELF:
			sb.append("$");
			break;
		case SELF_ASCENDANTS:
			sb.append("$-" + outputLabel);
			break;
		case CONSTANT:
			sb.append(outputLabel);
			break;
		case EPSILON:
			sb.append("EPS");
			
		}
		return sb.toString();
	}

}