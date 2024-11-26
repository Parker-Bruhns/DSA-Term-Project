import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Trie {
   // Node Inner class to support Trie class
   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   public class Node {
      private Node[] children = new Node[26];
      private String prefix;
      private boolean isEnd;


      public Node(String prefix) {
         this.isEnd = false;
         this.prefix = prefix;
      }

      private void insertChild(Node newNode) {
         children[Node.getIndex(newNode.prefix)] = newNode;
      }


      // You can index an array with characters 
      //    b/c 'a'-'a' = 0, 'b'-'a' = 1, and 'c' - 'a' = 2 etc...
      private static int getIndex(char c) {
         return c - 'a';
      }

      private static int getIndex(String s) {
         char start = s.charAt(0);
         return start - 'a';
      }

      private boolean hasChild(int index) {
         return children[index] != null;
      }
   }
   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   // End of Node class

   // Trie class
   private Node root;

   public Trie () {
      this.root = new Node(null);
   }

   // Helper method for Trie class
   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   public static int commonPrefixLen(String a, String b) {
      int len = 0;
      while (len < a.length() && len < b.length() && a.charAt(len) == b.charAt(len)) {
         len++;
      }
      return len;
   }
   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   // End of Helper Methods

   // Mutator methods for Trie class
   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   public void insert(String word) {
      word = word.toLowerCase();
      Node current = this.root;
      int i = 0;

      while (i < word.length()) {
         int key = word.charAt(i) - 'a';
         boolean childExists = current.hasChild(key);
         if (!childExists) {
            Node newNode = new Node(word.substring(i));
            newNode.isEnd = true;
            current.insertChild(newNode);
            return;
         }

         current = current.children[key];
         int prefixLen = commonPrefixLen(word.substring(i), current.prefix);
         i += prefixLen;
         if (prefixLen < current.prefix.length()) {
            Node newChild = new Node(current.prefix.substring(prefixLen));
            newChild.isEnd = current.isEnd;
            newChild.children = current.children;
            current.prefix = current.prefix.substring(0, prefixLen);
            current.children = new Node[26];
            current.insertChild(newChild);
            current.isEnd = i == word.length();
         }
      }
   }
   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   // End of Mutator methods for Trie class

   public boolean contains(String word) {
      Node current = root;
      int i = 0;
      while (i < word.length()) {
         int key = Node.getIndex(word);
         if (!current.hasChild(key)) {
            return false;
         }

         current = current.children[key];
         int prefixLen = commonPrefixLen(word.substring(i), current.prefix);
         if (prefixLen != current.prefix.length()) {
            return false;
         }
         i += prefixLen;
         if (i == word.length()) {
            return current.isEnd;
         }
      }
      return false;
   }

   // Edit to return String array
   // public String[] traverse(String prefix) {
   //    prefix = prefix.toLowerCase();
   //    Node current = root;
   //    int i = 0;
   //
   //    // Traverse the trie to find the node corresponding to the prefix
   //    while (i < prefix.length()) {
   //       int key = Node.getIndex(prefix.charAt(i));
   //       if (!current.hasChild(key)) {
   //          return new String[0]; // Return an empty array if the prefix does not exist
   //       }
   //
   //       current = current.children[key];
   //       int prefixLen = commonPrefixLen(prefix.substring(i), current.prefix);
   //       if (prefixLen != current.prefix.length()) {
   //          return new String[0]; // Prefix does not match fully
   //       }
   //       i += prefixLen;
   //    }
   //
   //    // Collect all words starting from the current node
   //    return collectWords(current, new StringBuilder(prefix)).toArray(new String[0]);
   // }

   public String[] traverse(String prefix) {
      prefix = prefix.toLowerCase();
      Node current = root;
      int i = 0;

      while (i < prefix.length()) {
         int key = Node.getIndex(prefix.charAt(i));
         if (!current.hasChild(key)) {
            return new String[0]; // Return empty array if prefix doesn't exist
         }

         current = current.children[key];
         i++;
      }

      List<String> words = collectWords(current, new StringBuilder());
      return words.toArray(new String[words.size()]);
   }

   private List<String> collectWords(Node node, StringBuilder prefix) {
      List<String> words = new ArrayList<>();

      if (node.isEnd) {
         words.add(prefix.toString());
      }

      for (Node child : node.children) {
         if (child != null) {
            // Create a new StringBuilder with the current prefix
            StringBuilder childPrefix = new StringBuilder();
            if (node.prefix == null) {
               childPrefix.append(child.prefix);
            } else {
               childPrefix.append(node.prefix).append(child.prefix);
            }

            // Recursively collect words from the child node
            collectWords(child, childPrefix).forEach(words::add);
         }
      }

      return words;
   }

   // private List<String> collectWords(Node node, StringBuilder prefix) {
   //    List<String> words = new ArrayList<>();
   //
   //    // If the current node is the end of a word, add it
   //    if (node.isEnd) {
   //       words.add(prefix.toString());
   //    }
   //
   //    // Recursively collect words from children
   //    for (Node child : node.children) {
   //       if (child != null) {
   //          collectWords(child, new StringBuilder(prefix).append(child.prefix)).forEach(words::add);
   //          // prefix.setLength(prefix.length() - child.prefix.length()); // Backtrack
   //       }
   //    }
   //
   //    return words;
   // }

   public void loadWordsFromFile(String fileName) throws FileNotFoundException, IOException {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String line;
      while ((line = reader.readLine()) != null) {
         this.insert(line.replaceAll("\\s+", "").toLowerCase()); 
      }
      reader.close();
   }

   public void display() {
      displayRecursive(root, "", 0);
   }

   private void displayRecursive(Node node, String prefix, int level) {
      if (node.prefix != null) {
         // Print the current node's prefix
         System.out.println(" ".repeat(level * 2) + "|-- \"" + node.prefix + "\"" + (node.isEnd ? " (word)" : ""));
      }

      // Recursively display all children
      for (Node child : node.children) {
         if (child != null) {
            displayRecursive(child, prefix + (node.prefix == null ? "" : node.prefix), level + 1);
         }
      }
   }

   // @Override
   // public String toString() {
   //    Node current = this.root;
   //    return null;
   // }
}
