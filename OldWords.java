import java.util.HashMap;

public class OldWords {
   private HashMap<String, Integer> popularity;

   public OldWords () {
      popularity = new HashMap<>();
   }

   public void insert(String word) {
      if (!popularity.containsKey(word)) {
         popularity.put(word, 1);
      } else {
         int newVal = popularity.get(word) + 1;
         popularity.replace(word, newVal);
      }

   }
}
