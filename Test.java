import java.io.IOException;

public class Test {
   public static void main(String[] args) throws IOException {
      Trie trie = new Trie();
      trie.loadWordsFromFile("Input Files/words.txt");
      trie.printAllWords();
   }
}
