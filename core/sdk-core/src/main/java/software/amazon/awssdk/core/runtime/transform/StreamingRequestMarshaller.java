/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.core.runtime.transform;

import static software.amazon.awssdk.http.Header.CONTENT_LENGTH;
import static software.amazon.awssdk.http.Header.CONTENT_TYPE;
import static software.amazon.awssdk.utils.Validate.paramNotNull;

import software.amazon.awssdk.annotations.SdkProtectedApi;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.utils.StringUtils;

/**
 * Augments a {@link Marshaller} to add contents for a streamed request.
 *
 * @param <T> Type of POJO being marshalled.
 */
@SdkProtectedApi
public final class StreamingRequestMarshaller<T> implements Marshaller<T> {

    private final Marshaller<T> delegate;
    private final RequestBody requestBody;

    /**
     * @param delegate    POJO marshaller (for path/query/header members).
     * @param requestBody {@link RequestBody} representing HTTP contents.
     */
    public StreamingRequestMarshaller(Marshaller<T> delegate, RequestBody requestBody) {
        this.delegate = paramNotNull(delegate, "delegate");
        this.requestBody = paramNotNull(requestBody, "requestBody");
    }

    @Override
    public SdkHttpFullRequest marshall(T in) {
        SdkHttpFullRequest.Builder marshalled = delegate.marshall(in).toBuilder();
        marshalled.contentStreamProvider(requestBody.contentStreamProvider());
        String contentType = marshalled.firstMatchingHeader(CONTENT_TYPE)
                                       .orElse(null);
        if (StringUtils.isEmpty(contentType)) {
            marshalled.putHeader(CONTENT_TYPE, requestBody.contentType());
        }

        marshalled.putHeader(CONTENT_LENGTH, String.valueOf(requestBody.contentLength()));
        return marshalled.build();
    }
}
