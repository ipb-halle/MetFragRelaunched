package de.ipbhalle.metfraglib.additionals;

public class Bond {

	private String atom1;
	private String atom2;
	
	private char bondType;
	
	public Bond(String atom1, String atom2, char bondType) {
		this.atom1 = atom1;
		this.atom2 = atom2;
		if(atom1.compareTo(atom2) < 0) {
			String tmp = this.atom2;
			this.atom2 = this.atom1;
			this.atom1 = tmp;
		}
		this.bondType = bondType;
	}

	public String getAtom1() {
		return this.atom1;
	}

	public void setAtom1(String atom1) {
		this.atom1 = atom1;
	}

	public String getAtom2() {
		return this.atom2;
	}

	public void setAtom2(String atom2) {
		this.atom2 = atom2;
	}

	public char getBondType() {
		return this.bondType;
	}

	public void setBondType(char bondType) {
		this.bondType = bondType;
	}
	
	@Override
	public boolean equals(Object obj) {
		Bond bond = (Bond)obj;
		if(((bond.getAtom1().equals(this.atom1) && bond.getAtom2().equals(this.atom2)) 
			|| (bond.getAtom1().equals(this.atom2) && bond.getAtom2().equals(this.atom1))) 
			&& bond.bondType == this.bondType)
			return true;
		return false;
	}
	
	public String toString() {
		return this.atom1 + this.bondType + this.atom2;
	}
	
	@Override
    public int hashCode() {
		String tmp1 = this.atom1;
		String tmp2 = this.atom2;
        String hash = "";
        //1 + tmp1 + tmp2 + this.bondType;
        for(int i = 0; i < tmp1.length(); i++) hash += ((int)tmp1.charAt(i) - 64);
        for(int i = 0; i < tmp2.length(); i++) hash += ((int)tmp2.charAt(i) - 64);
        hash += (int)this.bondType;
        return Integer.parseInt(hash);
    }
	
}
