// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
import ballerina/task;
import ballerina/io;

ListenerEndpointConfig c;

documentation {
    Represents a service listener that monitors the FTP location.
}
public type Listener object {
    ListenerEndpointConfig config;
    task:Appointment? appointment;
    task:Timer? task;

    public function init(ListenerEndpointConfig listenerConfig) {
        self.config = listenerConfig;
        c = listenerConfig;
    }

    public native function register(typedesc serviceType);

    public function start() {
        (function() returns error?) onTriggerFunction = tempFunc;
        match config.cronExpression {
            string expression => {
                appointment = new task:Appointment(onTriggerFunction, onError, expression);
                _ = appointment.schedule();
            }
            () => {
                task = new task:Timer(onTriggerFunction, onError, config.pollingInterval, delay = 100);
                _ = task.start();
            }
        }
    }

    public function stop() {
        match appointment {
            task:Appointment t => {
                t.cancel();
            }
            () => {
                _ = task.stop();
            }
        }
    }
};

function onError(error e) {
    io:println("[ERROR] FTP listener poll failed. ");
    io:println(e);
}

function tempFunc() returns error? {
    return poll(c);
}

native function poll(ListenerEndpointConfig config) returns error?;

documentation {
    Configuration for FTP listener endpoint.

    F{{protocol}} Supported FTP protocols
    F{{host}} Target service url
    F{{port}} Port number of the remote service
    F{{secureSocket}} Authenthication options
    F{{path}} Remote FTP direcotry location
    F{{fileNamePattern}} File name pattern that event need to trigger
    F{{pollingInterval}} Periodic time interval to check new update
    F{{cronExpression}} Cron expression to check new update
}
public type ListenerEndpointConfig record {
    Protocol protocol = FTP,
    string host,
    int port,
    SecureSocket? secureSocket,
    string path,
    string fileNamePattern,
    int pollingInterval = 60000,
    string? cronExpression,
};