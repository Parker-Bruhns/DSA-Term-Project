/*

   Authors (group members): Jack Fishbein, Nick Speranza, and Parker Bruhns
   Email addresses of group members: 
   Group name:

Course: Algorithms and Data Structures (CSE 2010)
Section:

Description of the overall algorithm:


*/

import java.io.FileNotFoundException;
import java.io.FileReader;

public class SmartWord {
   String[] guesses = new String[3];  // 3 guesses from SmartWord
   FileReader path;

   // initialize SmartWord with a file of English words
   public SmartWord(String wordFile) throws FileNotFoundException {
      this.path = new FileReader(wordFile);

   }

   // process old messages from oldMessageFile
   public void processOldMessages(String oldMessageFile) {
      // TODO: create trie & weight edges

   }

   // based on a letter typed in by the user, return 3 word guesses in an array
   // letter: letter typed in by the user
   // letterPosition:  position of the letter in the word, starts from 0
   // wordPosition: position of the word in a message, starts from 0
   public String[] guess(char letter,  int letterPosition, int wordPosition) {
      // TODO: 

      return guesses;
   }

   // feedback on the 3 guesses from the user
   // isCorrectGuess: true if one of the guesses is correct
   // correctWord: 3 cases:
   // a.  correct word if one of the guesses is correct
   // b.  null if none of the guesses is correct, before the user has typed in 
   //            the last letter
   // c.  correct word if none of the guesses is correct, and the user has 
   //            typed in the last letter
   // That is:
   // Case       isCorrectGuess      correctWord   
   // a.         true                correct word
   // b.         false               null
   // c.         false               correct word
   public void feedback(boolean isCorrectGuess, String correctWord) {

   }

}