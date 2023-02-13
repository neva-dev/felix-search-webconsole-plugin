package com.neva.felix.webconsole.plugins.search.decompiler;

import java.io.File;

public interface Decompiler {
    String decompile(File jar, String className, boolean showLineNumbers) throws Exception;
}
