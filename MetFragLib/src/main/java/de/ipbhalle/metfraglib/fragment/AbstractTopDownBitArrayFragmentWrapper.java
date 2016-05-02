package de.ipbhalle.metfraglib.fragment;

public class AbstractTopDownBitArrayFragmentWrapper {

	protected Integer currentPeakIndexPointer;
	protected AbstractTopDownBitArrayFragment wrappedFragment;
	
	public AbstractTopDownBitArrayFragmentWrapper(
			AbstractTopDownBitArrayFragment wrappedFragment) {
		this.wrappedFragment = wrappedFragment;
	}

	public AbstractTopDownBitArrayFragmentWrapper(
			AbstractTopDownBitArrayFragment wrappedFragment, Integer currentPeakIndexPointer) {
		this.wrappedFragment = wrappedFragment;
		this.currentPeakIndexPointer = currentPeakIndexPointer;
	}

	public Integer getCurrentPeakIndexPointer() {
		return this.currentPeakIndexPointer;
	}
	
	public void setCurrentPeakIndexPointer(Integer currentPeakIndexPointer) {
		this.currentPeakIndexPointer = currentPeakIndexPointer;
	}
	
	public AbstractTopDownBitArrayFragment getWrappedFragment() {
		return this.wrappedFragment;
	}
	
	public void setWrappedFragment(AbstractTopDownBitArrayFragment wrappedFragment) {
		this.wrappedFragment = wrappedFragment;
	}
	
	public void shallowNullify() {
		this.currentPeakIndexPointer = null;
	}
}
