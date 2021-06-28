/*
 * Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.ftp.transport.message;

import java.util.List;

/**
 * This class represent the events that happen in remote file system.
 */
public class RemoteFileSystemEvent extends RemoteFileSystemBaseMessage {

    private List<FileInfo> addedFiles;
    private List<String> deletedFiles;

    public RemoteFileSystemEvent(List<FileInfo> addedFiles, List<String> deletedFiles) {
        this.addedFiles = addedFiles;
        this.deletedFiles = deletedFiles;
    }

    public List<FileInfo> getAddedFiles() {
        return addedFiles;
    }

    public List<String> getDeletedFiles() {
        return deletedFiles;
    }
}
