import java.lang.reflect.*;

public class Reflection {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
       Class clazz = Class.forName("SuperCode");
       Object o = clazz.newInstance();
   }
}
