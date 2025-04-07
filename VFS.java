package OperationSystem;

/**
 * VFS - Virtual File System
 * Maps device calls to the appropriate device
 */
public class VFS implements Device {
    private static final int MAX_DEVICES = 10;
    
    // Class to hold device and id mapping
    private static class DeviceMapping {
        Device device;
        int deviceId;
        
        DeviceMapping(Device device, int deviceId) {
            this.device = device;
            this.deviceId = deviceId;
        }
    }
    
    private DeviceMapping[] deviceMappings;
    private RandomDevice randomDevice;
    private FakeFileSystem fileSystem;
    
    public VFS() {
        deviceMappings = new DeviceMapping[MAX_DEVICES];
        randomDevice = new RandomDevice();
        fileSystem = new FakeFileSystem();
    }
    
    @Override
    public int Open(String s) {
        if (s == null || s.isEmpty()) {
            return -1;
        }
        
        Device targetDevice;
        int deviceId;
        
        // Determine which device to use based on the string
        if (s.startsWith("/dev/random")) {
            // Extract seed if provided (format: /dev/random:seed)
            String seed = "";
            if (s.contains(":")) {
                seed = s.split(":")[1];
            }
            targetDevice = randomDevice;
            deviceId = randomDevice.Open(seed);
        } else {
            // Assume it's a file
            targetDevice = fileSystem;
            deviceId = fileSystem.Open(s);
        }
        
        if (deviceId == -1) {
            return -1; // Device couldn't open
        }
        
        // Find an empty slot in VFS
        for (int i = 0; i < deviceMappings.length; i++) {
            if (deviceMappings[i] == null) {
                deviceMappings[i] = new DeviceMapping(targetDevice, deviceId);
                return i;
            }
        }
        
        // No empty slots in VFS, close the device
        targetDevice.Close(deviceId);
        return -1;
    }
    
    @Override
    public void Close(int id) {
        if (id >= 0 && id < deviceMappings.length && deviceMappings[id] != null) {
            DeviceMapping mapping = deviceMappings[id];
            mapping.device.Close(mapping.deviceId);
            deviceMappings[id] = null;
        }
    }
    
    @Override
    public byte[] Read(int id, int size) {
        if (id >= 0 && id < deviceMappings.length && deviceMappings[id] != null) {
            DeviceMapping mapping = deviceMappings[id];
            return mapping.device.Read(mapping.deviceId, size);
        }
        return new byte[0];
    }
    
    @Override
    public void Seek(int id, int to) {
        if (id >= 0 && id < deviceMappings.length && deviceMappings[id] != null) {
            DeviceMapping mapping = deviceMappings[id];
            mapping.device.Seek(mapping.deviceId, to);
        }
    }
    
    @Override
    public int Write(int id, byte[] data) {
        if (id >= 0 && id < deviceMappings.length && deviceMappings[id] != null) {
            DeviceMapping mapping = deviceMappings[id];
            return mapping.device.Write(mapping.deviceId, data);
        }
        return 0;
    }
}