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
import org.ballerinalang.bre.bvm.BLangVMStructs;
import org.ballerinalang.ftp.util.ClientConstants;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.nativeimpl.io.BallerinaIOException;
import org.ballerinalang.nativeimpl.io.IOConstants;
import org.ballerinalang.nativeimpl.io.channels.base.Channel;
import org.ballerinalang.nativeimpl.io.channels.base.readers.BlockingReader;
import org.ballerinalang.nativeimpl.io.channels.base.writers.BlockingWriter;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.natives.annotations.ReturnType;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.wso2.transport.remotefilesystem.client.connector.contract.VFSClientConnector;
import org.wso2.transport.remotefilesystem.client.connector.contractimpl.VFSClientConnectorImpl;
import org.wso2.transport.remotefilesystem.message.RemoteFileSystemBaseMessage;
import org.wso2.transport.remotefilesystem.message.RemoteFileSystemMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;

import static org.ballerinalang.ftp.util.ServerConstants.FTP_PACKAGE_NAME;

/**
* FTP Get operation.
*/
@BallerinaFunction(
        orgName = "wso2",
        packageName = "ftp",
        functionName = "get",
        receiver = @Receiver(type = TypeKind.STRUCT, structType = "ClientConnector", structPackage = FTP_PACKAGE_NAME),
        args = {@Argument(name = "ftpClientConnector", type = TypeKind.CONNECTOR),
                @Argument(name = "path", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.STRUCT, structType = "ByteChannel", structPackage = "ballerina.io"),
                      @ReturnType(type = TypeKind.STRUCT, structType = "FTPClientError",
                                  structPackage = FTP_PACKAGE_NAME)
        }
)
public class Get extends AbstractFtpAction {

    @Override
    public void execute(Context context) {
        BStruct clientConnector = (BStruct) context.getRefArgument(0);
        String pathString = context.getStringArgument(0);

        String url = (String) clientConnector.getNativeData(ClientConstants.URL);
        //Create property map to send to transport.
        Map<String, String> propertyMap = new HashMap<>(4);
        propertyMap.put(ClientConstants.PROPERTY_URI, url + pathString);
        propertyMap.put(ClientConstants.PROPERTY_ACTION, ClientConstants.ACTION_GET);
        propertyMap.put(ClientConstants.PROTOCOL, ClientConstants.PROTOCOL_FTP);
        propertyMap.put(ClientConstants.FTP_PASSIVE_MODE, Boolean.TRUE.toString());

        FTPReadClientConnectorListener connectorListener = new FTPReadClientConnectorListener(context);
        VFSClientConnector connector = new VFSClientConnectorImpl(propertyMap, connectorListener);
        connector.send(null);
    }

    private static class FTPReadClientConnectorListener extends FTPClientConnectorListener {

        FTPReadClientConnectorListener(Context context) {
            super(context);
        }

        @Override
        public boolean onMessage(RemoteFileSystemBaseMessage remoteFileSystemBaseMessage) {
            if (remoteFileSystemBaseMessage instanceof RemoteFileSystemMessage) {
                final InputStream in = ((RemoteFileSystemMessage) remoteFileSystemBaseMessage).getInputStream();
                ByteChannel byteChannel = new ReadByteChannel(in);
                Channel channel = new FTPGetAbstractChannel(byteChannel);
                BStruct channelStruct = getBStruct();
                channelStruct.addNativeData(IOConstants.BYTE_CHANNEL_NAME, channel);
                getContext().setReturnValues(channelStruct);
            }
            return true;
        }

        @Override
        public void onError(Throwable throwable) {
            BStruct error = getClientErrorStruct(getContext());
            error.setStringField(0, throwable.getMessage());
            getContext().setReturnValues(error);
        }

        private BStruct getBStruct() {
            PackageInfo timePackageInfo = getContext().getProgramFile().getPackageInfo("ballerina.io");
            final StructInfo structInfo = timePackageInfo.getStructInfo("ByteChannel");
            return BLangVMStructs.createBStruct(structInfo);
        }
    }

    /**
     * This class will use to concrete implementation of the {@link Channel}.
     */
    private static class FTPGetAbstractChannel extends Channel {

        FTPGetAbstractChannel(ByteChannel channel) throws BallerinaIOException {
            super(channel, new BlockingReader(), new BlockingWriter(), 1024);
        }

        @Override
        public void transfer(int i, int i1, WritableByteChannel writableByteChannel) throws BallerinaIOException {
            throw new BallerinaIOException("Unsupported operation.");
        }
    }

    /**
     * This class will use to create ByteChannel by encapsulating InputStream that coming from transport layer.
     */
    private static class ReadByteChannel implements ByteChannel {
        private InputStream in;
        private ReadableByteChannel inChannel;

        ReadByteChannel(InputStream in) {
            this.in = in;
            this.inChannel = Channels.newChannel(in);
        }

        @Override
        public int read(ByteBuffer dst) throws IOException {
            return inChannel.read(dst);
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            return 0;
        }

        @Override
        public boolean isOpen() {
            return inChannel.isOpen();
        }

        @Override
        public void close() throws IOException {
            inChannel.close();
            in.close();
        }
    }
}
