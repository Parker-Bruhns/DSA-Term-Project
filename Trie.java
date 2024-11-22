import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class Trie {
   public class Node {
      private char data;
      private Node next;
      private Suffix last;
      private Map<Character, Node> children;

      public Node (char data) {
         this.data = data;
         this.children = new HashMap<>();
      }

      public Node (char data, Node next) {
         this.data = data;
         this.next = next;
         this.children = new HashMap<>();
      }

      public Node (char data, Suffix last) {
         this.data = data;
         this.last = last;
         this.children = new HashMap<>();
      }
   }

   public class Suffix {
      private String data;

      public Suffix(String data) {
         this.data = data;
      }
   }

   // Trie class
   private Node root;
   
   public Trie () {
      this.root = new Node(' ');
   }

   public void insert(String word) {
      Node current = root;

      for (char c : word.toCharArray()) {
         current = current.children.computeIfAbsent(c, k -> new Node(c));
      }
      current.last = new Suffix(word);
   }

  public void loadWordsFromFile(String fileName) {
      try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
         String line;
         while ((line = reader.readLine()) != null) {
            insert(line.trim()); 
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void printAllWords() {
      printAllWordsRecursive(root, new StringBuilder());
   }

   private void printAllWordsRecursive(Node node, StringBuilder prefix) {
      if (node.last != null) {
         System.out.println(prefix.toString() + node.last.data);
      }

      for (Map.Entry<Character, Node> entry : node.children.entrySet()) {
         prefix.append(entry.getKey());
         printAllWordsRecursive(entry.getValue(), prefix);
         prefix.deleteCharAt(prefix.length() - 1);
      }
   }
   
   // Mutator methods for Trie class
   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   
   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   // End of Mutator methods for Trie class

   
}
