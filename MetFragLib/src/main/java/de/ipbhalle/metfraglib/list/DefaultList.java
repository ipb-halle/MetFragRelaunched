package de.ipbhalle.metfraglib.list;

import de.ipbhalle.metfraglib.interfaces.IList;

public class DefaultList implements IList {

	protected java.util.ArrayList<Object> list;
	
	public DefaultList() {
		this.list = new java.util.ArrayList<Object>();
	}
	
	public void addElement(Object obj) {
		this.list.add(obj);
	}

	public void addElement(int index, Object obj) {
		this.list.add(index, obj);
	}
	
	public Object getElement(int index) {
		return this.list.get(index);
	}

	public int getNumberElements() {
		return this.list.size();
	}

	public boolean containsElement(Object object) {
		return this.list.contains(object);
	}
	
	public int indexOfElement(Object object) {
		return this.list.indexOf(object);
	}
	
	public void removeElement(int index) {
		this.list.remove(index);
	}
	
	public void setElement(int index, Object object) {
		this.list.set(index, object);
	}

	public void setList(java.util.ArrayList<Object> list) {
		this.list = list;
	}
	
	public void nullify() {
		this.list = null;
	}
}
