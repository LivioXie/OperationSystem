package OperationSystem;

import java.util.Random;

/**
 * RandomDevice - Provides random data
 */
public class RandomDevice implements Device {
    private Random[] randomGenerators;
    
    public RandomDevice() {
        randomGenerators = new Random[10];
    }
    
    @Override
    public int Open(String s) {
        // Find an empty slot
        for (int i = 0; i < randomGenerators.length; i++) {
            if (randomGenerators[i] == null) {
                // If string is not empty, use it as seed
                if (s != null && !s.isEmpty()) {
                    try {
                        int seed = Integer.parseInt(s);
                        randomGenerators[i] = new Random(seed);
                    } catch (NumberFormatException e) {
                        // If parsing fails, use current time as seed
                        randomGenerators[i] = new Random();
                    }
                } else {
                    randomGenerators[i] = new Random();
                }
                return i;
            }
        }
        // No empty slots
        return -1;
    }
    
    @Override
    public void Close(int id) {
        if (id >= 0 && id < randomGenerators.length) {
            randomGenerators[id] = null;
        }
    }
    
    @Override
    public byte[] Read(int id, int size) {
        if (id >= 0 && id < randomGenerators.length && randomGenerators[id] != null) {
            byte[] data = new byte[size];
            randomGenerators[id].nextBytes(data);
            return data;
        }
        return new byte[0];
    }
    
    @Override
    public void Seek(int id, int to) {
        // For random device, seek just consumes random bytes
        if (id >= 0 && id < randomGenerators.length && randomGenerators[id] != null) {
            byte[] dummy = new byte[to];
            randomGenerators[id].nextBytes(dummy);
        }
    }
    
    @Override
    public int Write(int id, byte[] data) {
        // Cannot write to a random device
        return 0;
    }
}