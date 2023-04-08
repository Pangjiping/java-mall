package org.epha.common.constant;

/**
 * @author pangjiping
 */
public class OrderConstant {

    public static final String USER_ORDER_TOKEN_PREFIX = "order:token:";

    public static final String MQ_EXCHANGE_ORDER_EVENT = "order-event-exchange";

    public static final String MQ_QUEUE_ORDER_RELEASE_ORDER = "order.release.order.queue";
    public static final String MQ_QUEUE_ORDER_DELAY = "order.delay.queue";

    public static final String MQ_ROUTING_KEY_ORDER_CREATE = "order.create.order";
    public static final String MQ_ROUTING_KEY_ORDER_RELEASE = "order.release.order";
    public static final String MQ_ROUTING_KEY_ORDER_RELEASE_OTHER = "order.release.other.#";
}
