/*

   Authors (group members): Jack Fishbein, Nick Speranza, and Parker Bruhns
   Email addresses of group members: nrinconspera2022@my.fit.edu, pbruhns2023@my.fit.edu, jfishbein2022@my.fit.edu
   Group name: 12Three

Course: Algorithms and Data Structures (CSE 2010)
Section: 1

Description of the overall algorithm:


*/

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SmartWord {
   public static void main(String[] args) throws Exception {
      if (args.length != 2) {
         System.out.println("Usage: java SmartWord <wordFile> <oldWords>");
         return;
      }

      SmartWord2 smartWord = new SmartWord2(args[0]);

      smartWord.guess('a', 0, 0);
      smartWord.guess('b', 1, 0);
      smartWord.guess('o', 2, 0);
      smartWord.guess('u', 3, 0);
      smartWord.guess('t', 3, 0);
   }

   String[] guesses = new String[3];  // 3 guesses from SmartWord
   FileReader dictionaryPath;

   Trie trie;
   OldWords oldWords;
   String currentWord;

   // initialize SmartWord with a file of English words
   public SmartWord(String wordFile) throws IOException {
      this.dictionaryPath = new FileReader(wordFile);
      this.trie = new Trie();
      this.currentWord = "";
      loadWordsIntoTrie(wordFile); 
   }

   private void loadWordsIntoTrie(String wordFile) {
      try (BufferedReader br = new BufferedReader(dictionaryPath)) {
            String line;
            while ((line = br.readLine()) != null) {
                // Remove any surrounding whitespace and convert to lowercase
                String word = line.trim().toLowerCase();
                if (!word.isEmpty()) {
                    trie.insert(word);  // Insert each word into the Trie
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
   }


   // process old messages from oldMessageFile
   public void processOldMessages(String oldMessageFile) throws IOException {
      BufferedReader br = new BufferedReader(new FileReader(oldMessageFile));
      String line;
      while ((line = br.readLine()) != null) {
         // Removes everything that is not a letter
         line = line.replaceAll("[^a-zA-Z]", "");
      }
      br.close();
   }

   // based on a letter typed in by the user, return 3 word guesses in an array
   // letter: letter typed in by the user
   // letterPosition:  position of the letter in the word, starts from 0
   // wordPosition: position of the word in a message, starts from 0
   public String[] guess(char letter,  int letterPosition, int wordPosition) {
      currentWord = currentWord + letter;
      System.out.println(currentWord);

      // TODO
      String[] suggestions = trie.traverse(currentWord);
      for (int i = 0; i < guesses.length; i++) {
         guesses[i] = i < suggestions.length ? suggestions[i] : null;
      }
      System.out.println("guesses: " + Arrays.toString(guesses));
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
      if (isCorrectGuess) {
         currentWord = "";
      } else if (correctWord != null) {
         //Add correct word to the trie to improve
         trie.insert(correctWord);
         //Reset current word to blank
         currentWord = ""; 
      }
   }
}
