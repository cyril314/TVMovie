package com.movie.server;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * @author aim
 * @date :2021/1/5
 * @description: 资源文件加载
 */
public class RawRequestProcess implements RequestProcess {
    private final Context mContext;
    private final String fileName;
    private final int resourceId;
    private final String mimeType;

    public RawRequestProcess(Context context, String fileName, int resourceId, String mimeType) {
        this.mContext = context;
        this.fileName = fileName;
        this.resourceId = resourceId;
        this.mimeType = mimeType;
    }

    @Override
    public boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName) {
        return session.getMethod() == NanoHTTPD.Method.GET && this.fileName.equalsIgnoreCase(fileName);
    }

    @Override
    public NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files) {
        InputStream inputStream = mContext.getResources().openRawResource(this.resourceId);
        try {
            return RemoteServer.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, mimeType + "; charset=utf-8", inputStream, inputStream.available());
        } catch (IOException IOExc) {
            return RemoteServer.createPlainTextResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "SERVER INTERNAL ERROR: IOException: " + IOExc.getMessage());
        }
    }
}