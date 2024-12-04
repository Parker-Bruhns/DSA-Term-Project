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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java SmartWord <wordFile> <oldWordsFile>");
            return;
        }

        SmartWord smartWord = new SmartWord(args[0]); // Initializes SmartWord with the word file
        smartWord.processOldMessages(args[1]); // Processes old messages to populate models
        smartWord.pruneUnusedWords(); // Optimizes the Trie

        // Example usage
        smartWord.guess('t', 0, 0);
        smartWord.guess('h', 1, 0);
        smartWord.guess('e', 2, 0);

        smartWord.feedback(true, "the"); // Provide feedback for testing
    }
}

// Trie structure for storing words and retrieving suggestions
class Trie {
    private final TrieNode root;
    private final Map<String, List<String>> suggestionCache; // Cache for frequently requested prefixes

    public Trie() {
        this.root = new TrieNode();
        this.suggestionCache = new HashMap<>();
    }

    // Inserts a word into the Trie
    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isWord = true;
        node.frequency++;
    }

    // Marks a word as confirmed
    public void confirmWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return; // Word does not exist
            }
            node = node.children.get(c);
        }
        node.confirmed++;
    }

    // Prunes low-frequency and unconfirmed nodes
    public void pruneUnused() {
        prune(root, new StringBuilder());
    }

    private boolean prune(TrieNode node, StringBuilder prefix) {
        Iterator<Map.Entry<Character, TrieNode>> it = node.children.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Character, TrieNode> entry = it.next();
            TrieNode child = entry.getValue();
            prefix.append(entry.getKey());
            if (prune(child, prefix)) {
                it.remove(); // Remove unused child
            }
            prefix.deleteCharAt(prefix.length() - 1);
        }
        return !node.isWord && node.frequency <= 5 && node.confirmed == 0 && node.children.isEmpty();
    }

    // Compresses the Trie for efficient storage
    public void compressTrie() {
        compress(root);
    }

    private void compress(TrieNode node) {
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            TrieNode child = entry.getValue();
            compress(child);

            // Merge single-child nodes
            if (child.children.size() == 1 && !child.isWord) {
                Map.Entry<Character, TrieNode> grandchild = child.children.entrySet().iterator().next();
                node.children.put(grandchild.getKey(), grandchild.getValue());
                node.children.remove(entry.getKey());
            }
        }
    }

    // Retrieves suggestions based on prefix and weighted scoring
    public List<String> getWeightedSuggestions(String prefix, int limit, String currentWord,
                                               Map<String, Map<String, Integer>> bigramModel,
                                               Map<String, Map<String, Integer>> trigramModel) {
        if (suggestionCache.containsKey(prefix)) {
            return suggestionCache.get(prefix); // Return cached suggestions if available
        }

        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList(); // No suggestions available
            }
            node = node.children.get(c);
        }

        // Calculate scores for suggestions
        List<WordScore> wordScores = new ArrayList<>();
        findWeightedWords(node, new StringBuilder(prefix), wordScores, currentWord, bigramModel, trigramModel);

        // Sort by weight in descending order
        wordScores.sort((a, b) -> Double.compare(b.weight, a.weight));

        // Prepare the result list
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, wordScores.size()); i++) {
            result.add(wordScores.get(i).word);
        }

        suggestionCache.put(prefix, result); // Cache the result for future use
        return result;
    }

    private void findWeightedWords(TrieNode node, StringBuilder prefix, List<WordScore> wordScores,
                                   String currentWord, Map<String, Map<String, Integer>> bigramModel,
                                   Map<String, Map<String, Integer>> trigramModel) {
        if (node.isWord) {
            // Calculate bigram and trigram probabilities
            double bigramProbability = bigramModel.getOrDefault(currentWord, Collections.emptyMap())
                                                  .getOrDefault(prefix.toString(), 0);
            String[] words = currentWord.split("\\s+");
            String trigramKey = words.length > 1 ? words[words.length - 2] + " " + words[words.length - 1] : null;
            double trigramProbability = trigramKey != null ? trigramModel.getOrDefault(trigramKey, Collections.emptyMap())
                                                           .getOrDefault(prefix.toString(), 0) : 0;

            // Calculate weight
            double weight = (node.frequency * 0.25) 
                          + (node.confirmed * 2.0) 
                          + (bigramProbability * 1.5) 
                          + (trigramProbability * 2.0);

            wordScores.add(new WordScore(prefix.toString(), weight));
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            findWeightedWords(entry.getValue(), prefix, wordScores, currentWord, bigramModel, trigramModel);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    private static class TrieNode {
        Map<Character, TrieNode> children = new TreeMap<>(); // Stores child nodes
        boolean isWord = false; // Indicates if the node represents a complete word
        int frequency = 0; // Tracks how often the word appears
        int confirmed = 0; // Tracks how often the word is confirmed by feedback
    }

    private static class WordScore {
        String word;
        double weight;

        WordScore(String word, double weight) {
            this.word = word;
            this.weight = weight;
        }
    }
}
