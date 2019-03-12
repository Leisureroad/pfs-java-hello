import java.util.function.Function;

public class HelloFunction implements Function<String, String> {
    public String apply(String s) {
        String result = "Hello, " + s + ", updated!";
        System.out.println(result);
        return result;
    }
}
