package com.redhat.jhalliday.impl;

import java.util.Map;

public class MethodJuice<T> {

    private T method;
    private Map<String, String> _naming;
    private String _body;

    public MethodJuice(T method, Map<String, String> naming, String body) {
        this.method = method;
        this._naming = naming;
        this._body = body;
    }

    public String getBody() {
        return _body;
    }

    public T getMethod() { return method; }
}
