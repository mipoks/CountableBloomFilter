import java.util.Random;

public class BloomFilter<T> {

    private double precision;
    private final int[] array;
    private AbstractHashFunction<T>[] hashFunctions;

    public BloomFilter(double precision, int countObjects, int capacity) {
        this.array = new int[capacity];
        int i = 1;
        double tempPrecision = 1;
        while (Double.compare(tempPrecision, precision) > 0) {
            tempPrecision = Math.pow((1 - Math.pow(Math.E, (-1.0 * i * countObjects / capacity))), i);
            i++;
            if (i > capacity) {
                throw new IllegalStateException("Невозможно достичь такой точности");
            }
        }
        this.precision = tempPrecision;
        this.hashFunctions = generateHashFunc(i);
    }

    public BloomFilter(int countObjects, int countHashFunc, int capacity) {
        this.array = new int[capacity];
        this.hashFunctions = generateHashFunc(countHashFunc);
        this.precision = Math.pow(
                (1 - Math.pow(Math.E, (-1.0 * countHashFunc * countObjects / capacity))), countHashFunc);
    }

    public BloomFilter(int countObjects, AbstractHashFunction<T>[] hashFunctions, int capacity) {
        this.array = new int[capacity];
        this.hashFunctions = generateHashFunc(hashFunctions.length);
        this.precision = Math.pow(
                (1 - Math.pow(Math.E, (-1.0 * hashFunctions.length * countObjects / capacity))), hashFunctions.length);
    }

    private XorHashFunction<T>[] generateHashFunc(int count) {
        XorHashFunction[] temp = new XorHashFunction[count];
        for (int i = 0; i < count; i++) {
            temp[i] = new XorHashFunction();
        }
        return temp;
    }

    public void add(T element) {
        for (AbstractHashFunction<T> hashFunction : hashFunctions) {
            array[Math.abs(hashFunction.hash(element)) % array.length]++;
        }
    }

    public boolean contains(T element) {
        for (AbstractHashFunction<T> hashFunction : hashFunctions) {
            if (array[Math.abs(hashFunction.hash(element)) %
                    array.length] == 0) {
                return false;
            }
        }
        return true;
    }

    public double getPrecision() {
        return precision;
    }

    class XorHashFunction<T> extends AbstractHashFunction<T> {

        private final int mask;

        public XorHashFunction() {
            this(new Random().nextInt());
        }

        public int getMask() {
            return mask;
        }

        public XorHashFunction(int mask) {
            this.mask = mask;
        }

        @Override
        public int hash(T element) {
            return element.hashCode() ^ mask;
        }
    }

}