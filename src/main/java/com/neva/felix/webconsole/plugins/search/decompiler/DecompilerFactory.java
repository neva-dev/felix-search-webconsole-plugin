package com.neva.felix.webconsole.plugins.search.decompiler;

import com.neva.felix.webconsole.plugins.search.decompiler.jd.JdDecompiler;
import org.benf.cfr.reader.CfrDecompiler;

public class DecompilerFactory {
    public static Decompiler get() {
        return get(Decompilers.JD_CORE);
    }

    public static Decompiler get(Decompilers decompiler) {
        final JdDecompiler jdDecompiler = new JdDecompiler();
        switch (decompiler) {
            case CFR:
                return new CfrDecompiler();
            case JD_CORE:
            default:
                return jdDecompiler;
        }
    }
}
