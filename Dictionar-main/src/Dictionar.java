import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class Dictionar {

    @SuppressWarnings("unchecked")
    public static void creeazaDictionar() {
        JSONObject obj = new JSONObject();
        obj.put("java", "Definiție: O insulă din Indonezia, la sud de Borneo.");
        obj.put("python", "Definiție: (Mitologia greacă) Dragon ucis de Apollo la Delphi.");
        obj.put("c", "Definiție: A 3-a literă a alfabetului latin.");

        try (FileWriter file = new FileWriter("dictionar.json")) {
            file.write(obj.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}