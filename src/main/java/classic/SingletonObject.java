package classic;

public class SingletonObject {
    private final int x;
    private final String str;

    public SingletonObject(int x, String str) {
        this.x = x;
        this.str = str;
    }

    private static SingletonObject INSTANCE;
    private static boolean initialized;

    public static SingletonObject getInstance(int x, String str) {
        if (INSTANCE == null && !initialized) {
            synchronized (SingletonObject.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SingletonObject(x, str);
                }
                initialized = INSTANCE != null;
            }
        }
        return INSTANCE;
    }
}
