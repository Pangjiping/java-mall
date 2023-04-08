package org.epha.common.mq;

/**
 * @author pangjiping
 */
public class StockLockedMessage {

    /**
     * 库存工作单ID
     */
    private Long wareOrderTaskId;

    /**
     * 工作单详情的ID
     */
    private WareOrderTaskDetail taskDetail;

    public Long getWareOrderTaskId() {
        return wareOrderTaskId;
    }

    public void setWareOrderTaskId(Long wareOrderTaskId) {
        this.wareOrderTaskId = wareOrderTaskId;
    }

    public WareOrderTaskDetail getTaskDetail() {
        return taskDetail;
    }

    public void setTaskDetail(WareOrderTaskDetail taskDetail) {
        this.taskDetail = taskDetail;
    }

    @Override
    public String toString() {
        return "StockLockedMessage{" +
                "wareOrderTaskId=" + wareOrderTaskId +
                ", taskDetail=" + taskDetail +
                '}';
    }

    public static class WareOrderTaskDetail{
        private Long id;
        /**
         * sku_id
         */
        private Long skuId;
        /**
         * sku_name
         */
        private String skuName;
        /**
         * 购买个数
         */
        private Integer skuNum;
        /**
         * 工作单id
         */
        private Long taskId;

        public WareOrderTaskDetail(){}

        public WareOrderTaskDetail(Long id, Long skuId, String skuName, Integer skuNum, Long taskId) {
            this.id = id;
            this.skuId = skuId;
            this.skuName = skuName;
            this.skuNum = skuNum;
            this.taskId = taskId;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getSkuId() {
            return skuId;
        }

        public void setSkuId(Long skuId) {
            this.skuId = skuId;
        }

        public String getSkuName() {
            return skuName;
        }

        public void setSkuName(String skuName) {
            this.skuName = skuName;
        }

        public Integer getSkuNum() {
            return skuNum;
        }

        public void setSkuNum(Integer skuNum) {
            this.skuNum = skuNum;
        }

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        @Override
        public String toString() {
            return "WareOrderTaskDetail{" +
                    "id=" + id +
                    ", skuId=" + skuId +
                    ", skuName='" + skuName + '\'' +
                    ", skuNum=" + skuNum +
                    ", taskId=" + taskId +
                    '}';
        }
    }
}
