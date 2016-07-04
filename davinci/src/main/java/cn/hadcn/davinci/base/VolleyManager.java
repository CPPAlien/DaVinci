/*
 * Copyright (C) 2012 The Android Open Source Project
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

package cn.hadcn.davinci.base;

import android.content.Context;

import java.io.File;

import cn.hadcn.davinci.volley.Network;
import cn.hadcn.davinci.volley.RequestQueue;
import cn.hadcn.davinci.volley.toolbox.BasicNetwork;
import cn.hadcn.davinci.volley.toolbox.DiskBasedCache;
import cn.hadcn.davinci.volley.toolbox.HttpStack;
import cn.hadcn.davinci.volley.toolbox.HurlStack;

public class VolleyManager {

    /** Default on-disk cache directory. */
    private static final String DEFAULT_CACHE_DIR = "volley";

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creating the cache dir.
     * @param poolSize thread pool size, 4 for default.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, int poolSize) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);

        HttpStack stack = new HurlStack();

        Network network = new BasicNetwork(stack);

        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network, poolSize);
        queue.start();

        return queue;
    }
}
