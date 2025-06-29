package top.orosirian.client.discoverer;

import java.net.InetSocketAddress;

public interface Discoverer {

    InetSocketAddress discoverService(String serviceName);

    void stop();

}
