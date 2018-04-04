/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.ftp.server.nativeimpl.endpoint;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.ftp.server.Constants;
import org.wso2.transport.remotefilesystem.exception.RemoteFileSystemConnectorException;
import org.wso2.transport.remotefilesystem.server.connector.contract.RemoteFileSystemServerConnector;

/**
 * Stop the server connector.
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "net.ftp",
        functionName = "stop",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ServiceEndpoint",
                             structPackage = "ballerina.net.ftp"),
        isPublic = true
)
public class Stop extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {
        Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        RemoteFileSystemServerConnector serverConnector = (RemoteFileSystemServerConnector) serviceEndpoint
                .getNativeData(Constants.FTP_SERVER_CONNECTOR);
        try {
            serverConnector.stop();
        } catch (RemoteFileSystemConnectorException e) {
            throw new BallerinaConnectorException("Unable to stop server connector", e);
        }
        context.setReturnValues();
    }
}
