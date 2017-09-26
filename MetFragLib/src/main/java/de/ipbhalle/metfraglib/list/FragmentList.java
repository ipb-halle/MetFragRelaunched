package de.ipbhalle.metfraglib.list;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;

public class FragmentList extends DefaultList {

	public FragmentList() {
		super();
	}

	public FragmentList(IFragment fragment) {
		super();
		this.list.add(fragment);
	}
	
	public IFragment getElement(int index) {
		return (IFragment)this.list.get(index);
	}
	
	public void addElement(IFragment fragment) {
		this.list.add(fragment);
	}

	public void addElement(int index, IFragment fragment) {
		this.list.add(index, fragment);
	}

	public void removeElement(int index) {
		this.list.remove(index);
	}

	/**
	 * replaces the fragment at the specified index and reassignes the maximal tree depth
	 * 
	 */
	public void setElement(int index, IFragment fragment) {
		this.list.set(index, fragment);
	}

	public void shallowNullify() {
		if(this.list != null)
			this.list.clear();
	}
	
	/**
	 * 
	 * @param fragment
	 * @return
	 */
	public byte suitsToFragmentList(IMolecularStructure precursorMolecule, IFragment fragment) {
		try {
			return ((IFragment)this.list.get(0)).shareEqualProperties(precursorMolecule, fragment);
		} catch (AtomTypeNotKnownFromInputListException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
