package danielbernal.app;

import danielbernal.app.spark.SparkProcessor;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");
        SparkProcessor sparkProcessor = new SparkProcessor();
        try {
            sparkProcessor.createContext();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
