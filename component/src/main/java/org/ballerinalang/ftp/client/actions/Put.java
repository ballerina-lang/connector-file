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
package org.ballerinalang.ftp.client.actions;

import org.ballerinalang.bre.Context;
import org.ballerinalang.ftp.util.ClientConstants;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.wso2.transport.remotefilesystem.client.connector.contract.VFSClientConnector;
import org.wso2.transport.remotefilesystem.client.connector.contractimpl.VFSClientConnectorImpl;
import org.wso2.transport.remotefilesystem.message.RemoteFileSystemMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * FTP Put operation.
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "ftp",
        functionName = "put",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ClientConnector", structPackage = "ballerina.ftp"),
        args = {@Argument(name = "ftpClientConnector", type = TypeKind.CONNECTOR),
                @Argument(name = "path", type = TypeKind.STRING),
                @Argument(name = "source", type = TypeKind.STRUCT, structType = "ByteChannel",
                          structPackage = "ballerina.io")},
        returnType = {
                @ReturnType(type = TypeKind.STRUCT, structType = "FTPClientError", structPackage = "ballerina.ftp")
        }
)
public class Put extends AbstractFtpAction {

    @Override
    public void execute(Context context) {
        BStruct clientConnector = (BStruct) context.getRefArgument(0);
        String url = (String) clientConnector.getNativeData(ClientConstants.URL);
        BStruct sourceChannel = (BStruct) context.getRefArgument(1);
        String path = context.getStringArgument(0);

        Channel byteChannel = (Channel) sourceChannel.getNativeData(IOConstants.BYTE_CHANNEL_NAME);
        RemoteFileSystemMessage message = new RemoteFileSystemMessage(byteChannel.getInputStream());

        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>(4);
        propertyMap.put(ClientConstants.PROPERTY_URI, url + path);
        propertyMap.put(ClientConstants.PROPERTY_ACTION, ClientConstants.ACTION_PUT);
        propertyMap.put(ClientConstants.PROTOCOL, ClientConstants.PROTOCOL_FTP);
        propertyMap.put(ClientConstants.FTP_PASSIVE_MODE, Boolean.TRUE.toString());

        FTPClientConnectorListener connectorListener = new FTPClientConnectorListener(context);
        VFSClientConnector connector = new VFSClientConnectorImpl(propertyMap, connectorListener);
        connector.send(message);
    }
}
