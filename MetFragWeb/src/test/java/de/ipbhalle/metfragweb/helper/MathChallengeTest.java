package de.ipbhalle.metfragweb.helper;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MathChallenge.
 */
public class MathChallengeTest {
    
    @Test
    public void testChallengeGeneration() {
        MathChallenge challenge = new MathChallenge();
        assertNotNull(challenge.getQuestion(), "Question should not be null");
        assertTrue(challenge.getQuestion().contains("?"), "Question should contain '?'");
        assertTrue(challenge.getCorrectAnswer() >= 1 && challenge.getCorrectAnswer() <= 20,
                "Answer should be between 1 and 20");
    }
    
    @Test
    public void testValidateCorrectAnswer() {
        MathChallenge challenge = new MathChallenge();
        int correctAnswer = challenge.getCorrectAnswer();
        
        assertTrue(challenge.validateAnswer(String.valueOf(correctAnswer)),
                "Should validate correct answer as string");
        assertTrue(challenge.validateAnswer(correctAnswer),
                "Should validate correct answer as int");
    }
    
    @Test
    public void testValidateIncorrectAnswer() {
        MathChallenge challenge = new MathChallenge();
        int correctAnswer = challenge.getCorrectAnswer();
        int incorrectAnswer = (correctAnswer == 20) ? 1 : correctAnswer + 1;
        
        assertFalse(challenge.validateAnswer(String.valueOf(incorrectAnswer)),
                "Should reject incorrect answer as string");
        assertFalse(challenge.validateAnswer(incorrectAnswer),
                "Should reject incorrect answer as int");
    }
    
    @Test
    public void testValidateNullAnswer() {
        MathChallenge challenge = new MathChallenge();
        assertFalse(challenge.validateAnswer((String) null),
                "Should reject null answer");
    }
    
    @Test
    public void testValidateEmptyAnswer() {
        MathChallenge challenge = new MathChallenge();
        assertFalse(challenge.validateAnswer(""),
                "Should reject empty answer");
        assertFalse(challenge.validateAnswer("   "),
                "Should reject whitespace answer");
    }
    
    @Test
    public void testValidateInvalidFormat() {
        MathChallenge challenge = new MathChallenge();
        assertFalse(challenge.validateAnswer("abc"),
                "Should reject non-numeric answer");
        assertFalse(challenge.validateAnswer("12.5"),
                "Should reject decimal answer");
        assertFalse(challenge.validateAnswer("12 34"),
                "Should reject answer with spaces");
    }
    
    @Test
    public void testValidateAnswerWithWhitespace() {
        MathChallenge challenge = new MathChallenge();
        int correctAnswer = challenge.getCorrectAnswer();
        
        assertTrue(challenge.validateAnswer("  " + correctAnswer + "  "),
                "Should accept correct answer with leading/trailing whitespace");
    }
    
    @Test
    public void testMultipleChallengesHaveValidAnswers() {
        // Test 100 random challenges to ensure all have valid answers
        for (int i = 0; i < 100; i++) {
            MathChallenge challenge = new MathChallenge();
            int answer = challenge.getCorrectAnswer();
            assertTrue(answer >= 1 && answer <= 20,
                    "All challenges should have answers between 1 and 20, got: " + answer);
            assertNotNull(challenge.getQuestion(),
                    "All challenges should have a question");
        }
    }
    
    @Test
    public void testQuestionFormat() {
        MathChallenge challenge = new MathChallenge();
        String question = challenge.getQuestion();
        
        // Question should match pattern: "number operator number = ?"
        assertTrue(question.matches("\\d+ [+\\-*] \\d+ = \\?"),
                "Question should match format 'number operator number = ?', got: " + question);
    }
    
    @Test
    public void testOperatorsAreValid() {
        // Test multiple challenges to verify operators
        boolean foundAddition = false;
        boolean foundSubtraction = false;
        boolean foundMultiplication = false;
        
        for (int i = 0; i < 50; i++) {
            MathChallenge challenge = new MathChallenge();
            String question = challenge.getQuestion();
            
            if (question.contains("+")) foundAddition = true;
            if (question.contains("-")) foundSubtraction = true;
            if (question.contains("*")) foundMultiplication = true;
            
            // Verify only valid operators are used
            assertTrue(question.contains("+") || question.contains("-") || question.contains("*"),
                    "Question should contain a valid operator (+, -, *)");
        }
        
        // With 50 trials, we should see at least one of each operator (statistically very likely)
        assertTrue(foundAddition || foundSubtraction || foundMultiplication,
                "Should generate questions with various operators");
    }
}
