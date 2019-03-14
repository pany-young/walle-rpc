/*
 * Copyright 2018-2019 Pany Young.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.pany.walle.remoting.exception;


public final class WalleRpcException extends RuntimeException {

    public static final int UNKNOWN_EXCEPTION = 0;
    public static final int NETWORK_EXCEPTION = 1;
    public static final int TIMEOUT_EXCEPTION = 2;
    public static final int BIZ_EXCEPTION = 3;
    public static final int FORBIDDEN_EXCEPTION = 4;
    public static final int SERIALIZATION_EXCEPTION = 5;
    public static final int NO_CLIENT_EXCEPTION = 6;

    private int code; // RpcException不能有子类，异常类型用ErrorCode表示，以便保持兼容。

    public WalleRpcException() {
        super();
    }

    public WalleRpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public WalleRpcException(String message) {
        super(message);
    }

    public WalleRpcException(Throwable cause) {
        super(cause);
    }

    public WalleRpcException(int code) {
        super();
        this.code = code;
    }

    public WalleRpcException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public WalleRpcException(int code, String message) {
        super(message);
        this.code = code;
    }

    public WalleRpcException(int code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isBiz() {
        return code == BIZ_EXCEPTION;
    }

    public boolean isForbidded() {
        return code == FORBIDDEN_EXCEPTION;
    }

    public boolean isTimeout() {
        return code == TIMEOUT_EXCEPTION;
    }

    public boolean isNetwork() {
        return code == NETWORK_EXCEPTION;
    }

    public boolean isSerialization() {
        return code == SERIALIZATION_EXCEPTION;
    }
}