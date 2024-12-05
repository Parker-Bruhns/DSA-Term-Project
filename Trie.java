import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Trie structure for storing words and retrieving suggestions
class Trie {
    // Inner Classes of Trie
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // TrieNode inner class to hold the data for each node
    private static class TrieNode {
       Map<Character, TrieNode> children = new TreeMap<>(); // Stores child nodes
       boolean isWord = false; // Indicates if the node represents a complete word
       int frequency = 0; // Tracks how often the word appears
       int confirmed = 0; // Tracks how often the word is confirmed by feedback
    }

    // WordScore inner class to hold the weights of each word
    private static class WordScore {
       String word;
       double weight;

       WordScore(String word, double weight) {
          this.word = word;
          this.weight = weight;
       }
    }
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // End of Inner Classes

    // Trie class
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private final TrieNode root;
    private final Map<String, List<String>> suggestionCache; // Cache for frequently requested prefixes

    // Trie class default constructor
    public Trie() {
       this.root = new TrieNode();
       this.suggestionCache = new HashMap<>();
    }

    // Manipulation methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
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

    // Confirms a word for reuse in later guesses
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

    // Prunes low-frequency and unconfirmed nodes
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
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // End of manipultation methods

    // Popularity methods
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
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

    // Searches the tree for weighted words
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
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // End of Popularity methods
}
// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// End of Trie Class
