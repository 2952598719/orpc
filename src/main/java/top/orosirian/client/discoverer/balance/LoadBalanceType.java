package top.orosirian.client.discoverer.balance;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum LoadBalanceType {

    RANDOM(0, "random"),
    ROUND(1, "round"),
    CONSISTENT_HASH(2, "consistentHash"),
    ;

    public final int code;

    public final String name;
}