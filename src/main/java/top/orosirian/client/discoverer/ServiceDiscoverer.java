package top.orosirian.client.discoverer;

import java.net.InetSocketAddress;

public interface ServiceDiscoverer {

    InetSocketAddress discoverService(String serviceName);

}
