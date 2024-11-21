public class Trie {
   public class Node {
      private char data;
      private Node next;
      private Suffix last;

      public Node (char data) {
         this.data = data;
      }

      public Node (char data, Node next) {
         this.data = data;
         this.next = next;
      }

      public Node (char data, Suffix last) {
         this.data = data;
         this.last = last;
      }
   }

   public class Suffix {

   }

   // Trie class
   private Node root;
   
   public Trie () {
      this.root = new Node();

   }

   
}
