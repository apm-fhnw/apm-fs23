public class Producer {
    public long produce(int item) {
        long result = 0;
        for (int i = 0; i < 100_000; ++i) {
            long[] pattern = {i, i + 1, i + 2, i + 3};
            if (item == 200) {
                result += pattern[0];
            } else if (item == 300) {
                result += pattern[1];
            } else if (item == 400) {
                result += pattern[2];
            } else {
                result += pattern[3];
            }
        }
        return result;
    }
}
