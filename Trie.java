import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

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


   // public void insert(String word) {
   //    Node current = root;
   //
   //    for (char c : word.toCharArray()) {
   //       current = current.children.computeIfAbsent(c, k -> new Node(c));
   //    }
   //    current.last = new Suffix(word);
   // }

   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   // End of Mutator methods for Trie class

   private void printAllWordsRecursive(Node node, StringBuilder prefix) {
      if (node.isEnd) {
         System.out.println(prefix.toString() + node.prefix);
      }
      for (Node child : node.children) {
         if (child == null) {
            continue;
         }
         if (node.prefix == null) {
            printAllWordsRecursive(child, new StringBuilder(prefix));
         } else {
            printAllWordsRecursive(child, new StringBuilder(prefix).append(node.prefix));
         }
      }
   }

   // // Nick Work
   //
   // private void printAllWordsRecursive(Node node, StringBuilder prefix, int level) {
   //    if (node.data != ' ') {
   //       System.out.println(" ".repeat(level) + "|-- " + node.data);
   //    }
   //
   //    if (node.last != null) {
   //       System.out.println(prefix.toString() + node.last.data);
   //       System.out.println(" ".repeat(level + 1) + "(word: " + node.last.data + ")");
   //    }
   //
   //    for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
   //       prefix.append(entry.getKey());
   //       printAllWordsRecursive(entry.getValue(), prefix, level + 1);
   //       // prefix.deleteCharAt(prefix.length() - 1);
   //    }
   // }
   public void loadWordsFromFile(String fileName) throws FileNotFoundException, IOException {
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String line;
      while ((line = reader.readLine()) != null) {
         this.insert(line.replaceAll("\\s+", "").toLowerCase()); 
      }
      reader.close();
   }

   public void printAllWords() {
      printAllWordsRecursive(root, new StringBuilder());
   }

   // @Override
   // public String toString() {
   //    Node current = this.root;
   //    return null;
   // }
}
