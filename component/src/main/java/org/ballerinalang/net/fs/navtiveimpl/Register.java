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

package org.ballerinalang.net.fs.navtiveimpl;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.connector.api.BLangConnectorSPIUtil;
import org.ballerinalang.connector.api.BallerinaConnectorException;
import org.ballerinalang.connector.api.ParamDetail;
import org.ballerinalang.connector.api.Resource;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.connector.api.Struct;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.fs.server.DirectoryListenerConstants;
import org.ballerinalang.net.fs.server.LFSListener;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.StructInfo;
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemConnectorFactory;
import org.wso2.transport.localfilesystem.server.connector.contract.LocalFileSystemServerConnector;
import org.wso2.transport.localfilesystem.server.connector.contractimpl.LocalFileSystemConnectorFactoryImpl;
import org.wso2.transport.localfilesystem.server.exception.LocalFileSystemServerConnectorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Register file listener service.
 */

@BallerinaFunction(
        orgName = "ballerina",
        packageName = "net.fs",
        functionName = "register",
        receiver = @Receiver(type = TypeKind.STRUCT,
                             structType = "DirectoryListener",
                             structPackage = "ballerina.net.fs"),
        args = {@Argument(name = "serviceType", type = TypeKind.TYPEDESC)},
        isPublic = true
)
public class Register extends BlockingNativeCallableUnit {
    @Override
    public void execute(Context context) {
        Service service = BLangConnectorSPIUtil.getServiceRegistered(context);
        Struct serviceEndpoint = BLangConnectorSPIUtil.getConnectorEndpointStruct(context);
        Struct serviceEndpointConfig = serviceEndpoint
                .getStructField(DirectoryListenerConstants.SERVICE_ENDPOINT_CONFIG);
        try {
            final Map<String, Resource> resourceRegistry = getResourceRegistry(service);
            final String events = String.join(",", resourceRegistry.keySet());
            final Map<String, String> paramMap = getParamMap(serviceEndpointConfig, events);
            LocalFileSystemConnectorFactory connectorFactory = new LocalFileSystemConnectorFactoryImpl();
            StructInfo structInfo = getStructInfo(context);
            LocalFileSystemServerConnector serverConnector = connectorFactory
                    .createServerConnector(service.getName(), paramMap, new LFSListener(resourceRegistry, structInfo));
            serviceEndpoint.addNativeData(DirectoryListenerConstants.FS_SERVER_CONNECTOR, serverConnector);
        } catch (LocalFileSystemServerConnectorException e) {
            throw new BallerinaConnectorException("Unable to initialize server connector", e);
        }
        context.setReturnValues();
    }

    private StructInfo getStructInfo(Context context) {
        PackageInfo httpPackageInfo = context.getProgramFile().getPackageInfo(DirectoryListenerConstants.PACKAGE_NAME);
        return httpPackageInfo.getStructInfo(DirectoryListenerConstants.FILE_SYSTEM_EVENT);
    }

    private Map<String, String> getParamMap(Struct serviceEndpointConfig, String events) {
        final String path = serviceEndpointConfig.getStringField(DirectoryListenerConstants.ANNOTATION_PATH);
        final boolean recursive = serviceEndpointConfig
                .getBooleanField(DirectoryListenerConstants.ANNOTATION_DIRECTORY_RECURSIVE);
        Map<String, String> paramMap = new HashMap<>(3);
        if (path != null && !path.isEmpty()) {
            paramMap.put(DirectoryListenerConstants.ANNOTATION_DIR_URI, path);
        }
        paramMap.put(DirectoryListenerConstants.ANNOTATION_EVENTS, events);
        paramMap.put(DirectoryListenerConstants.ANNOTATION_DIRECTORY_RECURSIVE, String.valueOf(recursive));
        return paramMap;
    }

    private Map<String, Resource> getResourceRegistry(Service service) {
        Map<String, Resource> registry = new HashMap<>(3);
        for (Resource resource : service.getResources()) {
            switch (resource.getName()) {
            case DirectoryListenerConstants.RESOURCE_NAME_ON_CREATE:
                validateParameter(resource);
                registry.put(DirectoryListenerConstants.EVENT_CREATE, resource);
                break;
            case DirectoryListenerConstants.RESOURCE_NAME_ON_DELETE:
                validateParameter(resource);
                registry.put(DirectoryListenerConstants.EVENT_DELETE, resource);
                break;
            case DirectoryListenerConstants.RESOURCE_NAME_ON_MODIFY:
                validateParameter(resource);
                registry.put(DirectoryListenerConstants.EVENT_MODIFY, resource);
                break;
            default:
                // Do nothing.
            }
        }
        if (registry.size() == 0) {
            String msg = "At least a single resource requires from following: "
                    + DirectoryListenerConstants.RESOURCE_NAME_ON_CREATE + " ,"
                    + DirectoryListenerConstants.RESOURCE_NAME_ON_DELETE + " ,"
                    + DirectoryListenerConstants.RESOURCE_NAME_ON_MODIFY + ". " + "Parameter should be of type - "
                    + DirectoryListenerConstants.PACKAGE_NAME + ":" + DirectoryListenerConstants.FILE_SYSTEM_EVENT;
            throw new BallerinaConnectorException(msg);
        }
        return registry;
    }

    private void validateParameter(Resource resource) {
        final List<ParamDetail> paramDetails = resource.getParamDetails();
        if (paramDetails.size() != 1) {
            String msg = "Invalid resource signature. " + "A single " + DirectoryListenerConstants.PACKAGE_NAME + ":"
                    + DirectoryListenerConstants.FILE_SYSTEM_EVENT + " parameter allow in the resource signature.";
            throw new BallerinaConnectorException(msg);
        }
        final ParamDetail paramDetail = paramDetails.get(0);
        final BType varType = paramDetail.getVarType();
        if (!DirectoryListenerConstants.PACKAGE_NAME.equals(varType.getPackagePath())
                || !DirectoryListenerConstants.FILE_SYSTEM_EVENT.equals(varType.getName())) {
            String msg = "Parameter should be of type - " + DirectoryListenerConstants.PACKAGE_NAME + ":"
                    + DirectoryListenerConstants.FILE_SYSTEM_EVENT;
            throw new BallerinaConnectorException(msg);
        }
    }
}
