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

package wso2.ftp;

@Field {value:"uri: Absolute file URI for the triggerd event"}
@Field {value:"baseName: File/directory name for the triggerd event"}
@Field {value:"path: Relative file/directory path for the triggerd event"}
@Field {value:"size: Size of the file"}
@Field {value:"lastModifiedTimeStamp: Last modified timestamp of the file"}
public type FileEvent {
    string uri,
    string baseName,
    string path,
    int size,
    int lastModifiedTimeStamp,
};

public type Connection {};
