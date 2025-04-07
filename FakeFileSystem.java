package OperationSystem;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * FakeFileSystem - Simple file system implementation
 */
public class FakeFileSystem implements Device {
    private RandomAccessFile[] files;
    
    public FakeFileSystem() {
        files = new RandomAccessFile[10];
    }
    
    @Override
    public int Open(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        
        // Find an empty slot
        for (int i = 0; i < files.length; i++) {
            if (files[i] == null) {
                try {
                    files[i] = new RandomAccessFile(s, "rw");
                    return i;
                } catch (IOException e) {
                    e.printStackTrace();
                    return -1;
                }
            }
        }
        // No empty slots
        return -1;
    }
    
    @Override
    public void Close(int id) {
        if (id >= 0 && id < files.length && files[id] != null) {
            try {
                files[id].close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                files[id] = null;
            }
        }
    }
    
    @Override
    public byte[] Read(int id, int size) {
        if (id >= 0 && id < files.length && files[id] != null) {
            try {
                byte[] data = new byte[size];
                int bytesRead = files[id].read(data, 0, size);
                
                // If we read fewer bytes than requested, trim the array
                if (bytesRead < size) {
                    byte[] trimmedData = new byte[bytesRead];
                    System.arraycopy(data, 0, trimmedData, 0, bytesRead);
                    return trimmedData;
                }
                
                return data;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }
    
    @Override
    public void Seek(int id, int to) {
        if (id >= 0 && id < files.length && files[id] != null) {
            try {
                files[id].seek(to);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public int Write(int id, byte[] data) {
        if (id >= 0 && id < files.length && files[id] != null) {
            try {
                files[id].write(data);
                return data.length;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }
}