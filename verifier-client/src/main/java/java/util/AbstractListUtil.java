package java.util;

public final class AbstractListUtil {

    public static int getModCount(AbstractList<?> list) {
        return list.modCount;
    }

    public static void setModCount(AbstractList<?> list, int modCount) {
        list.modCount = modCount;
    }

    public static void rangeRemove(AbstractList<?> list, int fromIndex, int toIndex) {
        list.removeRange(fromIndex, toIndex);
    }
}