/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oracle.truffle.bf;

import com.oracle.truffle.api.TruffleLanguage;

@TruffleLanguage.Registration(name = "bf", version = "1.0", mimeType = "application/x-bf")
public class BFLanguage extends TruffleLanguage<BFLanguage.Ctx>{

    @Override
    protected Ctx createContext(Env env) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object getLanguageGlobal(Ctx context) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        throw new UnsupportedOperationException();
    }
    static final class Ctx {
    }
}
