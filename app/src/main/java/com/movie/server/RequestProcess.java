package com.movie.server;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * @author aim
 * @date :2021/1/5
 * @description:
 */
public interface RequestProcess {
    int KEY_ACTION_PRESSED = 0;
    int KEY_ACTION_DOWN = 1;
    int KEY_ACTION_UP = 2;

    /**
     * isRequest
     *
     * @param session
     * @param fileName
     * @return
     */
    boolean isRequest(NanoHTTPD.IHTTPSession session, String fileName);

    /**
     * doResponse
     *
     * @param session
     * @param fileName
     * @param params
     * @param files
     * @return
     */
    NanoHTTPD.Response doResponse(NanoHTTPD.IHTTPSession session, String fileName, Map<String, String> params, Map<String, String> files);
}