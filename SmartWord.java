/*
   Authors (group members): Jack Fishbein, Nick Speranza, and Parker Bruhns
   Email addresses of group members: nrinconspera2022@my.fit.edu, pbruhns2023@my.fit.edu, jfishbein2022@my.fit.edu
   Group name: 12Three

   Course: Algorithms and Data Structures (CSE 2010)
   Section: 1

   Description of the overall algorithm: The SmartWord program predicts and suggests word completions by using a Trie
   to store words and quickly find matches based on prefixes. It also uses context from past messages with bigram and trigram
   models to prioritize suggestions that fit well with recent words. When users type, it suggests up to three words ranked by
   how often they appear and how relevant they are to the current context. It learns from feedback by confirming correct words
   or adding new ones to improve future predictions. The program is optimized to balance accuracy, speed, and memory efficiency.
*/


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;



public class SmartWord {
    private final Trie trie;
    private final Map<String, Map<String, Integer>> bigramModel; // Tracks word pairs and their frequencies
    private final Map<String, Map<String, Integer>> trigramModel; // Tracks word triplets and their frequencies
    private String currentWord; // Tracks the current word being guessed
    private static final int MAX_GUESSES = 3; // Maximum number of suggestions

    // Constructor to initialize the Trie and models from the word file
    public SmartWord(String wordFile) throws IOException {
        this.trie = new Trie();
        this.bigramModel = new HashMap<>();
        this.trigramModel = new HashMap<>();
        this.currentWord = "";
        loadWordsIntoTrie(wordFile);
    }

    // Loads words from the word file into the Trie
    private void loadWordsIntoTrie(String wordFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(wordFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String word = line.trim().toLowerCase(); // Normalize to lowercase
                if (!word.isEmpty()) {
                    trie.insert(word); // Inserts words into Trie
                }
            }
        }
    }

    // Processes old messages to populate the Trie and n-gram models
    public void processOldMessages(String oldMessageFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(oldMessageFile))) {
            String line;
            String prevWord = null; // Tracks the previous word for bigram updates
            String prevPrevWord = null; // Tracks the word before the previous word for trigram updates

            while ((line = br.readLine()) != null) {
                line = line.replaceAll("[^a-zA-Z']", " ").toLowerCase(); // Normalizes and removes non-alphabetic characters
                String[] words = line.split("\\s+");

                for (String word : words) {
                    if (!word.isEmpty()) {
                        trie.insert(word); // Inserts word into Trie

                        // Updates bigram model
                        if (prevWord != null) {
                            bigramModel.computeIfAbsent(prevWord, k -> new HashMap<>())
                                       .merge(word, 1, Integer::sum);
                        }

                        // Updates trigram model
                        if (prevPrevWord != null && prevWord != null) {
                            String bigram = prevPrevWord + " " + prevWord;
                            trigramModel.computeIfAbsent(bigram, k -> new HashMap<>())
                                        .merge(word, 1, Integer::sum);
                        }

                        // Updates word trackers
                        prevPrevWord = prevWord;
                        prevWord = word;
                    }
                }
            }
        }
    }

    // Prunes unused words and compresses the Trie for efficiency
    public void pruneUnusedWords() {
        trie.pruneUnused(); // Removes low-frequency or unconfirmed words
        trie.compressTrie(); // Optimizes Trie structure
    }

    // Generates suggestions for the current word based on the letter typed
    public String[] guess(char letter, int letterPosition, int wordPosition) {
        currentWord += letter; // Update the current word with the new letter

        // Gets suggestions from the Trie using weighted scores
        List<String> suggestions = trie.getWeightedSuggestions(
            currentWord, MAX_GUESSES, currentWord, bigramModel, trigramModel);

        // Prepares the output array of guesses
        String[] guesses = new String[MAX_GUESSES];
        for (int i = 0; i < MAX_GUESSES; i++) {
            guesses[i] = i < suggestions.size() ? suggestions.get(i) : null;
        }

        // Prints debug information
        System.out.println("Current word: " + currentWord);
        System.out.println("Suggestions: " + Arrays.toString(guesses));
        return guesses;
    }

    // Provides feedback on the guesses and updates the Trie if necessary
    public void feedback(boolean isCorrectGuess, String correctWord) {
        if (isCorrectGuess) {
            currentWord = ""; // Reset current word if the guess was correct
        } else if (correctWord != null) {
            trie.insert(correctWord.toLowerCase()); // Insert the correct word into Trie
            trie.confirmWord(correctWord.toLowerCase()); // Mark the word as confirmed
            currentWord = ""; // Reset current word
        }
    }
}
