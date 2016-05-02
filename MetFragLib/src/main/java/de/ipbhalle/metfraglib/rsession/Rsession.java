package de.ipbhalle.metfraglib.rsession;

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

public class Rsession {
	
	private static org.rosuda.REngine.Rserve.RConnection rconnection;
	
	static {
		try {
			rconnection = new org.rosuda.REngine.Rserve.RConnection("localhost");
		} catch (RserveException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isSessionLoaded() {
		return rconnection == null ? false : true;
	}
	
	public static REXP loadLibrary(String library) {
		try {
			REXP rexp = rconnection.eval("library("+ library +")");
			return rexp;
		} catch (RserveException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static org.rosuda.REngine.REXP executeCommand(String command) {
		try {
			REXP rexp = rconnection.eval(command);
			return rexp;
		} catch (RserveException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void removeSessionObjects(org.rosuda.REngine.Rserve.RConnection rconnection) {
		try {
			rconnection.eval("rm(list=ls())");
		} catch (RserveException e) {
			e.printStackTrace();
		}
	}
	

	public static double[] giveMemberships(String c1, String c2, String c3) {
		executeCommand(c1);
		executeCommand(c2);
		double[] membershipDouble = null;
		try {
			membershipDouble = executeCommand(c3).asDoubles();
		} catch (REXPMismatchException e) {
			e.printStackTrace();
		}
		return membershipDouble;
	}
}
