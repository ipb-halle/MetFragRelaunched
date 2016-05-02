package de.ipbhalle.metfraglib.score;

import org.apache.log4j.Logger;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.model.LinearRetentionTimeModel;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class RetentionTimeScore extends AbstractScore {

	private Logger logger = Logger.getLogger(RetentionTimeScore.class);
	
	/*
	 * the real logValue of the molecule which we want to predict with the linear model
	 */
	protected Double logValue;
	protected ICandidate scoredCandidate;
	
	/**
	 * 
	 * @param settings
	 */
	public RetentionTimeScore(Settings settings) {
		super(settings);
		this.scoredCandidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
		this.hasInterimResults = true;

		LinearRetentionTimeModel linearModel = (LinearRetentionTimeModel)settings.get(VariableNames.RETENTION_TIME_SCORE_LINEAR_MODEL_NAME);
		boolean userLogP = linearModel.isEnabledUserLogP();
		/*
		 * !!!!
		 * if UserLogP is defined then the linearModel has also to be trained with the same UserLogP values
		 * -> therefore add column UserLogP, calculated with the same method, in VariableNames.RETENTION_TIME_TRAINING_FILE_NAME
		 * !!!!
		 * otherwise predictions won't be valid
		 */
		
		if(this.settings.get(VariableNames.USER_LOG_P_VALUE_NAME) != null && this.scoredCandidate.getProperties().containsKey((String)this.settings.get(VariableNames.USER_LOG_P_VALUE_NAME))) { 
			if(!userLogP) {
				logger.error("Prediction model trained with CDK based logP in " + (String)this.settings.get(VariableNames.RETENTION_TIME_TRAINING_FILE_NAME));
				logger.error("External UserLogP given with the candidates causing a model mismatch -> No prediction made.");
				this.logValue = null;
			}
			try {
				this.logValue = Double.parseDouble((String)this.scoredCandidate.getProperty((String)this.settings.get(VariableNames.USER_LOG_P_VALUE_NAME)));
			}
			catch (NumberFormatException e) {
				this.logValue = null;
			}
		}
		else {
			if(userLogP) {
				logger.error("Prediction model trained with external UserLogP in " + (String)this.settings.get(VariableNames.RETENTION_TIME_TRAINING_FILE_NAME));
				logger.error("No external UserLogP given with the candidates causing a model mismatch -> No prediction made.");
				this.logValue = null;
			}
			else {
				this.logValue = linearModel.calculateLogPValue(this.scoredCandidate);
				if(this.logValue.isNaN()) {
					this.logValue = null;
				}
			}
		}
	}

	public void calculate() {
		LinearRetentionTimeModel linearModel = (LinearRetentionTimeModel)settings.get(VariableNames.RETENTION_TIME_SCORE_LINEAR_MODEL_NAME);
		Double predictedValue = linearModel.predict((Double) settings.get(VariableNames.EXPERIMENTAL_RETENTION_TIME_VALUE_NAME));
		if(predictedValue == null || this.logValue == null) {
			this.value = 0.0;
			this.optimalValues = null;
		}
		else {
			this.value = MathTools.getNormalDistributionDensity(Math.abs(predictedValue - this.logValue), 0.0, Constants.DEFAULT_RETENTION_TIME_STANDARD_DEVIATION_NORMAL_DISTRIBUTION);
			this.optimalValues = new double[] {predictedValue, this.logValue, Math.abs(predictedValue - this.logValue)};
		}
		this.calculationFinished = true;
	}

	public void shallowNullify() {
		super.shallowNullify();
		this.logValue = null;
	}
	
	public void nullify() {
		super.nullify();
		this.logValue = null;
	}

	public double calculateSingleMatch() {
		return 0.0;
	}

	@Override
	public Double[] calculateSingleMatch(IMatch match) {
		return new Double[] {0.0, null};
	}
	
	public boolean isBetterValue(double value) {
		return this.value < value ? true : false;
	}
	
}
