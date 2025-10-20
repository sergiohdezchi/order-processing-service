package com.hacom.telecom.order_processing_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "smpp")
public class SmppProperties {

    private String host;
    private int port;
    private String systemId;
    private String password;
    private String systemType;
    private String sourceAddress;
    private int sourceAddressTon;
    private int sourceAddressNpi;
    private int destAddressTon;
    private int destAddressNpi;
    private long requestExpiryTimeout;
    private long windowMonitorInterval;
    private int windowSize;
    private long connectTimeout;
    private long bindTimeout;
    private boolean enabled;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public int getSourceAddressTon() {
        return sourceAddressTon;
    }

    public void setSourceAddressTon(int sourceAddressTon) {
        this.sourceAddressTon = sourceAddressTon;
    }

    public int getSourceAddressNpi() {
        return sourceAddressNpi;
    }

    public void setSourceAddressNpi(int sourceAddressNpi) {
        this.sourceAddressNpi = sourceAddressNpi;
    }

    public int getDestAddressTon() {
        return destAddressTon;
    }

    public void setDestAddressTon(int destAddressTon) {
        this.destAddressTon = destAddressTon;
    }

    public int getDestAddressNpi() {
        return destAddressNpi;
    }

    public void setDestAddressNpi(int destAddressNpi) {
        this.destAddressNpi = destAddressNpi;
    }

    public long getRequestExpiryTimeout() {
        return requestExpiryTimeout;
    }

    public void setRequestExpiryTimeout(long requestExpiryTimeout) {
        this.requestExpiryTimeout = requestExpiryTimeout;
    }

    public long getWindowMonitorInterval() {
        return windowMonitorInterval;
    }

    public void setWindowMonitorInterval(long windowMonitorInterval) {
        this.windowMonitorInterval = windowMonitorInterval;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getBindTimeout() {
        return bindTimeout;
    }

    public void setBindTimeout(long bindTimeout) {
        this.bindTimeout = bindTimeout;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
