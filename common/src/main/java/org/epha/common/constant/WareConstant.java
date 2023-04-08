package org.epha.common.constant;

/**
 * @author pangjiping
 */
public class WareConstant {
    public static final String MQ_EXCHANGE_STOCK_EVENT = "stock-event-exchange";

    public static final String MQ_QUEUE_STOCK_RELEASE_STOCK = "stock.release.stock.queue";
    public static final String MQ_QUEUE_STOCK_DELAY = "stock.delay.queue";

    public static final String MQ_ROUTING_KEY_STOCK_RELEASE = "stock.release.#";
    public static final String MQ_ROUTING_KEY_STOCK_LOCKED = "stock.locked";
}
