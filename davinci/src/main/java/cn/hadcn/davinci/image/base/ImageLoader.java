package cn.hadcn.davinci.image.base;

import android.os.Handler;
import android.os.Looper;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;

import cn.hadcn.davinci.volley.Request;
import cn.hadcn.davinci.volley.RequestQueue;
import cn.hadcn.davinci.volley.Response.*;
import cn.hadcn.davinci.volley.VolleyError;


public class ImageLoader {
    /** RequestQueue for dispatching ImageRequests onto. */
    private final RequestQueue mRequestQueue;

    /** Amount of time to wait after first response arrives before delivering all responses. */
    private int mBatchResponseDelayMs = 100;

    /**
     * HashMap of Cache keys -> BatchedImageRequest used to track in-flight requests so
     * that we can coalesce multiple requests to the same URL into a single network request.
     */
    private final HashMap<String, BatchedImageRequest> mInFlightRequests =
            new HashMap<>();

    /** HashMap of the currently pending responses (waiting to be delivered). */
    private final HashMap<String, BatchedImageRequest> mBatchedResponses =
            new HashMap<>();

    /** Handler to the main thread. */
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /** Runnable for in-flight response delivery. */
    private Runnable mRunnable;

    /**
     * Constructs a new ImageLoader.
     * @param queue The RequestQueue to use for making image requests.
     */
    public ImageLoader(RequestQueue queue) {
        mRequestQueue = queue;
    }

    /**
     * Interface for the response handlers on image requests.
     *
     * The call flow is this:
     * 1. Upon being  attached to a request, onResponse(response, true) will
     * be invoked to reflect any cached data that was already available. If the
     * data was available, response.getBitmap() will be non-null.
     *
     * 2. After a network response returns, only one of the following cases will happen:
     *   - onResponse(response, false) will be called if the image was loaded.
     *   or
     *   - onErrorResponse will be called if there was an error loading the image.
     */
    public interface ImageListener extends ErrorListener {
        /**
         * Listens for non-error changes to the loading of the image request.
         *
         * @param response Holds all information pertaining to the request, as well
         * as the bitmap (if it is loaded).
         * @param isImmediate True if this was called during ImageLoader.get() variants.
         * This can be used to differentiate between a cached image loading and a network
         * image loading in order to, for example, run an animation to fade in network loaded
         * images.
         */
        void onResponse(ImageContainer response, boolean isImmediate);
    }

    /**
     * Issues a bitmap request with the given URL if that image is not available
     * in the cache, and returns a bitmap container that contains all of the data
     * relating to the request (as well as the default image if the requested
     * image is not available).
     * @param requestUrl The url of the remote image
     * @param imageListener The listener to call when the remote image is loaded
     * @return A container object that contains all of the properties of the request, as well as
     *     the currently available image (default if remote is not loaded).
     */
    public ImageContainer get(String requestUrl, String requestBody, ImageListener imageListener) {
        throwIfNotOnMainThread();

        // The bitmap did not exist in the cache, fetch it!
        ImageContainer imageContainer =
                new ImageContainer(null, requestUrl, imageListener);

        // Update the caller to let them know that they should use the default bitmap.
        imageListener.onResponse(imageContainer, true);

        // Check to see if a request is already in-flight.
        BatchedImageRequest request = mInFlightRequests.get(requestUrl);
        if (request != null) {
            // If it is, add this request to the list of listeners.
            request.addContainer(imageContainer);
            return imageContainer;
        }

        // The request is not already in flight. Send the new request to the network and
        // track it.
        Request<ByteBuffer> newRequest;
        if ( requestBody == null ) {
            newRequest = makeImageRequest(Request.Method.GET, requestUrl, null);
        } else {
            newRequest = makeImageRequest(Request.Method.POST, requestUrl, requestBody);
        }

        mRequestQueue.add(newRequest);
        mInFlightRequests.put(requestUrl, new BatchedImageRequest(newRequest, imageContainer));
        return imageContainer;
    }

    protected Request<ByteBuffer> makeImageRequest(int method, final String requestUrl, String requestBody) {
        return new ByteRequest(method, requestUrl, requestBody, new Listener<ByteBuffer>() {
            @Override
            public void onResponse(ByteBuffer response) {
                onGetImageSuccess(requestUrl, response);
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onGetImageError(requestUrl, error);
            }
        });
    }

    /**
     * Sets the amount of time to wait after the first response arrives before delivering all
     * responses. Batching can be disabled entirely by passing in 0.
     * @param newBatchedResponseDelayMs The time in milliseconds to wait.
     */
    public void setBatchedResponseDelay(int newBatchedResponseDelayMs) {
        mBatchResponseDelayMs = newBatchedResponseDelayMs;
    }

    /**
     * Handler for when an image was successfully loaded.
     * @param requestUrl The cache key that is associated with the image request.
     * @param response The bitmap that was returned from the network.
     */
    protected void onGetImageSuccess(String requestUrl, ByteBuffer response) {

        // remove the request from the list of in-flight requests.
        BatchedImageRequest request = mInFlightRequests.remove(requestUrl);

        if (request != null) {
            // Update the response bitmap.
            request.mResponseBitmap = response;

            // Send the batched response
            batchResponse(requestUrl, request);
        }
    }

    /**
     * Handler for when an image failed to load.
     * @param requestUrl The cache key that is associated with the image request.
     */
    protected void onGetImageError(String requestUrl, VolleyError error) {
        // Notify the requesters that something failed via a null result.
        // Remove this request from the list of in-flight requests.
        BatchedImageRequest request = mInFlightRequests.remove(requestUrl);

        if (request != null) {
            // Set the error for this request
            request.setError(error);

            // Send the batched response
            batchResponse(requestUrl, request);
        }
    }

    /**
     * Container object for all of the data surrounding an image request.
     */
    public class ImageContainer {
        /**
         * The most relevant bitmap for the container. If the image was in cache, the
         * Holder to use for the final bitmap (the one that pairs to the requested URL).
         */
        private ByteBuffer mBytes;

        private final ImageListener mListener;

        /** The request URL that was specified */
        private final String mRequestUrl;

        /**
         * Constructs a BitmapContainer object.
         * @param bytes bytes (if it exists).
         * @param requestUrl The requested URL for this container.
         */
        public ImageContainer(ByteBuffer bytes, String requestUrl, ImageListener listener) {
            mBytes = bytes;
            mRequestUrl = requestUrl;
            mListener = listener;
        }

        /**
         * Releases interest in the in-flight request (and cancels it if no one else is listening).
         */
        public void cancelRequest() {
            if (mListener == null) {
                return;
            }

            BatchedImageRequest request = mInFlightRequests.get(mRequestUrl);
            if (request != null) {
                boolean canceled = request.removeContainerAndCancelIfNecessary(this);
                if (canceled) {
                    mInFlightRequests.remove(mRequestUrl);
                }
            } else {
                // check to see if it is already batched for delivery.
                request = mBatchedResponses.get(mRequestUrl);
                if (request != null) {
                    request.removeContainerAndCancelIfNecessary(this);
                    if (request.mContainers.size() == 0) {
                        mBatchedResponses.remove(mRequestUrl);
                    }
                }
            }
        }

        /**
         * Returns the bitmap associated with the request URL if it has been loaded, null otherwise.
         */
        public ByteBuffer getBitmap() {
            return mBytes;
        }

        /**
         * Returns the requested URL for this container.
         */
        public String getRequestUrl() {
            return mRequestUrl;
        }
    }

    /**
     * Wrapper class used to map a Request to the set of active ImageContainer objects that are
     * interested in its results.
     */
    private class BatchedImageRequest {
        /** The request being tracked */
        private final Request<?> mRequest;

        /** The result of the request being tracked by this item */
        private ByteBuffer mResponseBitmap;

        /** Error if one occurred for this response */
        private VolleyError mError;

        /** List of all of the active ImageContainers that are interested in the request */
        private final LinkedList<ImageContainer> mContainers = new LinkedList<ImageContainer>();

        /**
         * Constructs a new BatchedImageRequest object
         * @param request The request being tracked
         * @param container The ImageContainer of the person who initiated the request.
         */
        public BatchedImageRequest(Request<?> request, ImageContainer container) {
            mRequest = request;
            mContainers.add(container);
        }

        /**
         * Set the error for this response
         */
        public void setError(VolleyError error) {
            mError = error;
        }

        /**
         * Get the error for this response
         */
        public VolleyError getError() {
            return mError;
        }

        /**
         * Adds another ImageContainer to the list of those interested in the results of
         * the request.
         */
        public void addContainer(ImageContainer container) {
            mContainers.add(container);
        }

        /**
         * Detatches the bitmap container from the request and cancels the request if no one is
         * left listening.
         * @param container The container to remove from the list
         * @return True if the request was canceled, false otherwise.
         */
        public boolean removeContainerAndCancelIfNecessary(ImageContainer container) {
            mContainers.remove(container);
            if (mContainers.size() == 0) {
                mRequest.cancel();
                return true;
            }
            return false;
        }
    }

    /**
     * Starts the runnable for batched delivery of responses if it is not already started.
     * @param requestUrl The cacheKey of the response being delivered.
     * @param request The BatchedImageRequest to be delivered.
     */
    private void batchResponse(String requestUrl, BatchedImageRequest request) {
        mBatchedResponses.put(requestUrl, request);
        // If we don't already have a batch delivery runnable in flight, make a new one.
        // Note that this will be used to deliver responses to all callers in mBatchedResponses.
        if (mRunnable == null) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    for (BatchedImageRequest bir : mBatchedResponses.values()) {
                        for (ImageContainer container : bir.mContainers) {
                            // If one of the callers in the batched request canceled the request
                            // after the response was received but before it was delivered,
                            // skip them.
                            if (container.mListener == null) {
                                continue;
                            }
                            if (bir.getError() == null) {
                                container.mBytes = bir.mResponseBitmap;
                                container.mListener.onResponse(container, false);
                            } else {
                                container.mListener.onErrorResponse(bir.getError());
                            }
                        }
                    }
                    mBatchedResponses.clear();
                    mRunnable = null;
                }

            };
            // Post the runnable.
            mHandler.postDelayed(mRunnable, mBatchResponseDelayMs);
        }
    }

    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
        }
    }
}
