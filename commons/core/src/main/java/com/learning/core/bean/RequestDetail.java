package com.learning.core.bean;

import java.io.Serializable;

/**
 * 请求信息主体类
 */
public class RequestDetail implements Serializable {
    private static final long serialVersionUID = 2543641512850125440L;
    private String ip;
    private String path;

    public RequestDetail() {
    }

    public String getIp() {
        return this.ip;
    }

    public String getPath() {
        return this.path;
    }

    public RequestDetail setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public RequestDetail setPath(String path) {
        this.path = path;
        return this;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof RequestDetail)) {
            return false;
        } else {
            RequestDetail other = (RequestDetail)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$ip = this.getIp();
                Object other$ip = other.getIp();
                if (this$ip == null) {
                    if (other$ip != null) {
                        return false;
                    }
                } else if (!this$ip.equals(other$ip)) {
                    return false;
                }

                Object this$path = this.getPath();
                Object other$path = other.getPath();
                if (this$path == null) {
                    if (other$path != null) {
                        return false;
                    }
                } else if (!this$path.equals(other$path)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof RequestDetail;
    }

    public int hashCode() {
        int result = 1;
        Object $ip = this.getIp();
        result = result * 59 + ($ip == null ? 43 : $ip.hashCode());
        Object $path = this.getPath();
        result = result * 59 + ($path == null ? 43 : $path.hashCode());
        return result;
    }

    public String toString() {
        return "RequestDetail(ip=" + this.getIp() + ", path=" + this.getPath() + ")";
    }
}
