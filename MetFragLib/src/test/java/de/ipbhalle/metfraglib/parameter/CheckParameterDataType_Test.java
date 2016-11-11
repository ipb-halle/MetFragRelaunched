package de.ipbhalle.metfraglib.parameter;

import static org.junit.Assert.*;

import org.junit.Test;

import de.ipbhalle.metfraglib.exceptions.ParameterNotKnownException;

public class CheckParameterDataType_Test {

	@Test
	public void test() {
		/*
		 * check types of some parameters
		 */
		try {
			assertTrue("1.0 " + VariableNames.ABSOLUTE_MASS_DEVIATION_NAME + " not Double", 
					ParameterDataTypes.getParameter("1.0", VariableNames.ABSOLUTE_MASS_DEVIATION_NAME).getClass() == Double.class);
			assertTrue("ScoreType1,ScoreType2 " + VariableNames.METFRAG_SCORE_TYPES_NAME + " not String[]", 
					ParameterDataTypes.getParameter("ScoreType1,ScoreType2", VariableNames.METFRAG_SCORE_TYPES_NAME).getClass() == String[].class);
			assertTrue("1.0, 0.5 " + VariableNames.METFRAG_SCORE_WEIGHTS_NAME + " not Double[]", 
					ParameterDataTypes.getParameter("1.0, 0.5", VariableNames.METFRAG_SCORE_WEIGHTS_NAME).getClass() == Double[].class);
			assertTrue("True " + VariableNames.IS_POSITIVE_ION_MODE_NAME + " not Boolean[]", 
					ParameterDataTypes.getParameter("True", VariableNames.IS_POSITIVE_ION_MODE_NAME).getClass() == Boolean.class);
			assertTrue("-1 " + VariableNames.PRECURSOR_ION_MODE_NAME + " not Integer", 
					ParameterDataTypes.getParameter("-1", VariableNames.PRECURSOR_ION_MODE_NAME).getClass() == Integer.class);
		} catch (ParameterNotKnownException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * check exception
	 * 
	 * @throws ParameterNotKnownException
	 */
	@Test(expected=ParameterNotKnownException.class)
	public void testIndexOutOfBoundsException() throws ParameterNotKnownException {
		ParameterDataTypes.getParameter("-1", "NotKnownParameterName");
	}
}
