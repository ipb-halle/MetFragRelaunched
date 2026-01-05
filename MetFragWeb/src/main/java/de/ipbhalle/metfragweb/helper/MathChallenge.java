package de.ipbhalle.metfragweb.helper;

import java.util.Random;

/**
 * Simple math challenge generator for anti-spam protection.
 * Generates basic arithmetic problems with addition, subtraction, and multiplication
 * using numbers between 1 and 20 with results between 1 and 20.
 */
public class MathChallenge {
    
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 20;
    private static final int MIN_RESULT = 1;
    private static final int MAX_RESULT = 20;
    private static final int MAX_ADDITION_OPERAND = 10;
    private static final int MAX_MULTIPLICATION_OPERAND = 5;
    
    private int number1;
    private int number2;
    private char operator;
    private int correctAnswer;
    private String question;
    
    /**
     * Creates a new math challenge with a random arithmetic problem.
     */
    public MathChallenge() {
        generateChallenge();
    }
    
    /**
     * Generates a new random math challenge.
     * The challenge uses addition (+), subtraction (-), or multiplication (*).
     * Numbers are between 1 and 20, and results are guaranteed to be between 1 and 20.
     */
    private void generateChallenge() {
        Random random = new Random();
        boolean validChallenge = false;
        
        while (!validChallenge) {
            // Pick a random operator: 0=addition, 1=subtraction, 2=multiplication
            int operatorChoice = random.nextInt(3);
            
            switch (operatorChoice) {
                case 0: // Addition
                    operator = '+';
                    // Ensure sum is between MIN_RESULT and MAX_RESULT
                    number1 = random.nextInt(MAX_ADDITION_OPERAND) + MIN_NUMBER; // 1-10
                    number2 = random.nextInt(MAX_RESULT - number1) + MIN_NUMBER; // ensures sum <= MAX_RESULT
                    correctAnswer = number1 + number2;
                    validChallenge = (correctAnswer >= MIN_RESULT && correctAnswer <= MAX_RESULT);
                    break;
                    
                case 1: // Subtraction
                    operator = '-';
                    // Ensure difference is between MIN_RESULT and MAX_RESULT
                    // number1 must be at least 2 to allow subtraction with result >= MIN_RESULT
                    number1 = random.nextInt(MAX_NUMBER - 1) + 2; // 2-20
                    number2 = random.nextInt(number1 - 1) + MIN_NUMBER; // 1 to number1-1
                    correctAnswer = number1 - number2;
                    validChallenge = (correctAnswer >= MIN_RESULT && correctAnswer <= MAX_RESULT);
                    break;
                    
                case 2: // Multiplication
                    operator = '*';
                    // Ensure product is between MIN_RESULT and MAX_RESULT
                    number1 = random.nextInt(MAX_MULTIPLICATION_OPERAND) + MIN_NUMBER; // 1-5
                    int maxMultiplier = MAX_RESULT / number1;
                    number2 = random.nextInt(maxMultiplier) + MIN_NUMBER;
                    correctAnswer = number1 * number2;
                    validChallenge = (correctAnswer >= MIN_RESULT && correctAnswer <= MAX_RESULT);
                    break;
            }
        }
        
        question = number1 + " " + operator + " " + number2 + " = ?";
    }
    
    /**
     * Validates the user's answer against the correct answer.
     * 
     * @param userAnswer the answer provided by the user
     * @return true if the answer is correct, false otherwise
     */
    public boolean validateAnswer(String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return false;
        }
        
        try {
            int answer = Integer.parseInt(userAnswer.trim());
            return answer == correctAnswer;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Validates the user's answer against the correct answer.
     * 
     * @param userAnswer the answer provided by the user
     * @return true if the answer is correct, false otherwise
     */
    public boolean validateAnswer(int userAnswer) {
        return userAnswer == correctAnswer;
    }
    
    /**
     * Gets the math question as a string.
     * 
     * @return the question string (e.g., "5 + 3 = ?")
     */
    public String getQuestion() {
        return question;
    }
    
    /**
     * Gets the correct answer (for testing purposes).
     * 
     * @return the correct answer
     */
    public int getCorrectAnswer() {
        return correctAnswer;
    }
}
