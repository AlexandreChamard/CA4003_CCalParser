
public class JsonShowHelper {
    public String spaces = new String();
    public int n;

    public String increase() {
        n = n + 1;
        spaces = spaces.concat("  ");
        return spaces;
    }

    public String decrease() {
        if (n != 0) {
            n = n - 1;
            spaces = spaces.substring(0, spaces.length() - 2);
        }
        return spaces;
    }
}
