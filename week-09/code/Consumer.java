public class Consumer {
    private void log(int item, long time) {
        System.out.println("Consuming item " + item +
            " took " + time / 1000L + " microseconds");
    }
    public void consume(Producer producer, int count) {
        for (int item = 0; item < count; ++item) {
            long start = System.nanoTime();
            long result = producer.produce(item);
            long latency = System.nanoTime() - start;
            log(item, latency);
        }
    }
    public static void main(String[] args) {
        Producer producer = new Producer();
        Consumer consumer = new Consumer();
	System.out.println("===============");
	System.out.println("Run 1");
	System.out.println("===============");
        consumer.consume(producer, 500);
	System.out.println("===============");
	System.out.println("Run 2");
	System.out.println("===============");
        consumer.consume(producer, 500);
    }
}
