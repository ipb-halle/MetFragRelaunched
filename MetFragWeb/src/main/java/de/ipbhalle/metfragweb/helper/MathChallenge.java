package de.ipbhalle.metfragweb.helper;

import java.util.Random;

/**
 * Simple math challenge generator for anti-spam protection.
 * Generates basic arithmetic problems with addition, subtraction, and multiplication
 * using numbers between 1 and 20 with results between 1 and 20.
 */
public class MathChallenge {
    
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
                    // Ensure sum is between 1 and 20
                    number1 = random.nextInt(10) + 1; // 1-10
                    number2 = random.nextInt(20 - number1) + 1; // ensures sum <= 20
                    correctAnswer = number1 + number2;
                    validChallenge = (correctAnswer >= 1 && correctAnswer <= 20);
                    break;
                    
                case 1: // Subtraction
                    operator = '-';
                    // Ensure difference is between 1 and 20
                    // number1 must be at least 2 to allow subtraction with result >= 1
                    number1 = random.nextInt(19) + 2; // 2-20
                    number2 = random.nextInt(number1 - 1) + 1; // 1 to number1-1
                    correctAnswer = number1 - number2;
                    validChallenge = (correctAnswer >= 1 && correctAnswer <= 20);
                    break;
                    
                case 2: // Multiplication
                    operator = '*';
                    // Ensure product is between 1 and 20
                    // Possible pairs: 1*1..1*20, 2*1..2*10, 3*1..3*6, 4*1..4*5, 5*1..5*4
                    number1 = random.nextInt(5) + 1; // 1-5
                    int maxMultiplier = 20 / number1;
                    number2 = random.nextInt(maxMultiplier) + 1;
                    correctAnswer = number1 * number2;
                    validChallenge = (correctAnswer >= 1 && correctAnswer <= 20);
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
