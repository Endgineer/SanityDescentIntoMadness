package croissantnova.sanitydim.util;

import java.util.List;
import java.util.Objects;

public class ListHelper {

    public static <T> T getLast(List<T> list) {
        try {
            return list.get(list.size() - 1);
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public static <T> List<T> getLastAsList(List<T> list) {
        return list.isEmpty() ? List.of() : List.of(list.get(list.size() - 1));
    }

    public static <T> List<T> getLastAsList(T[] array) {
        return array.length == 0 ? List.of() : List.of(array[array.length - 1]);
    }
}
