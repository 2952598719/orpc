package top.orosirian.server.register;

public interface ServiceRegister {

    void register(Object serviceImpl);

    Object getService(String serviceName);

}
