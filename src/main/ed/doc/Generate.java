package ed.doc;

import java.io.*;

import ed.js.*;
import ed.js.func.*;
import ed.js.engine.*;
import static ed.js.JSInternalFunctions.*;


public class Generate {
    public static void toHTML(String objStr) {

        try {
            Process p = Runtime.getRuntime().exec(new String[]{"java", "-jar", "jsrun.jar", "app/run.js", "-d=../../../corejs/admin/doc/", "-t=templates/jsdoc2"},
                                                  null,
                                                  new File("../external/jsdoc-toolkit/current/")
                                                  );
            BufferedWriter in = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            in.write(objStr);
            in.close();

            BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line2;

            while ((line2 = out.readLine()) != null) {
                System.out.println(line2);
            }

            BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line;

            while ((line = err.readLine()) != null) {
                System.out.println(line);
            }

            p.waitFor();
            System.out.println("exit: "+p.exitValue());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void toDB(String corejs) {

    }
}
