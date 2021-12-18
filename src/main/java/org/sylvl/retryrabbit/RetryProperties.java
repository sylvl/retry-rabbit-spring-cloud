package org.sylvl.retryrabbit;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "retry-rabbit")
public class RetryProperties {

  public static final String DEFAULT_RETRY_DESTINATION_HEADER = "x-retry-destination";
  public static final String DEFAULT_RETRY_COUNT_HEADER = "x-retry.count";

  private String retryCountHeader = DEFAULT_RETRY_COUNT_HEADER;

  private String retryDestinationHeader = DEFAULT_RETRY_DESTINATION_HEADER;

  private String routerExchangeName;

  private String inputExchangeName;

  private String parkingDestinationName;

  private Boolean declareInputChannel;

  private String delayGroupName;

  private Map<String, DelayChannelProperties> delayChannels;

  public String getRetryDestinationHeader() {
    return retryDestinationHeader;
  }

  public void setRetryDestinationHeader(String retryDestinationHeader) {
    this.retryDestinationHeader = retryDestinationHeader;
  }

  public String getRouterExchangeName() {
    return routerExchangeName;
  }

  public void setRouterExchangeName(String routerExchangeName) {
    this.routerExchangeName = routerExchangeName;
  }

  public String getInputExchangeName() {
    return inputExchangeName;
  }

  public void setInputExchangeName(String inputExchangeName) {
    this.inputExchangeName = inputExchangeName;
  }

  public Boolean getDeclareInputChannel() {
    return declareInputChannel;
  }

  public void setDeclareInputChannel(Boolean declareInputChannel) {
    this.declareInputChannel = declareInputChannel;
  }

  public String getParkingDestinationName() {
    return parkingDestinationName;
  }

  public void setParkingDestinationName(String parkingDestinationName) {
    this.parkingDestinationName = parkingDestinationName;
  }

  public Map<String, DelayChannelProperties> getDelayChannels() {
    return delayChannels;
  }

  public void setDelayChannels(Map<String, DelayChannelProperties> delayChannels) {
    this.delayChannels = delayChannels;
  }

  public String getDelayGroupName() {
    return delayGroupName;
  }

  public void setDelayGroupName(String delayGroupName) {
    this.delayGroupName = delayGroupName;
  }

  public String getRetryCountHeader() {
    return retryCountHeader;
  }

  public void setRetryCountHeader(String retryCountHeader) {
    this.retryCountHeader = retryCountHeader;
  }

  public static class DelayChannelProperties {
    /**
     * Backoff time in milliseconds
     */
    private Integer waitingTime;
    private String destinationName;

    public Integer getWaitingTime() {
      return waitingTime;
    }

    public void setWaitingTime(Integer waitingTime) {
      this.waitingTime = waitingTime;
    }

    public String getDestinationName() {
      return destinationName;
    }

    public void setDestinationName(String destinationName) {
      this.destinationName = destinationName;
    }
  }
}


