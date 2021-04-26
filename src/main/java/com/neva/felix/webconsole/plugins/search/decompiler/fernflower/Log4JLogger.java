package com.neva.felix.webconsole.plugins.search.decompiler.fernflower;

import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Log4JLogger extends IFernflowerLogger {
    private static final Logger LOG = LoggerFactory.getLogger(Log4JLogger.class);
    private int indent = 0;

    public void writeMessage(String message, Severity severity) {
        if (this.accepts(severity)) {
            final String indentedMessage = TextUtil.getIndentString(this.indent) + message;
            switch (severity) {
                case INFO:
                    LOG.info(indentedMessage);
                    break;
                case ERROR:
                    LOG.error(indentedMessage);
                    break;
                case TRACE:
                    LOG.trace(indentedMessage);
                    break;
                case WARN:
                    LOG.warn(indentedMessage);
                    break;
            }
        }

    }

    public void writeMessage(String message, Severity severity, Throwable t) {
        if (this.accepts(severity)) {
            final StringWriter stringWriter = new StringWriter();
            t.printStackTrace(new PrintWriter(stringWriter));
            this.writeMessage(message + "\n" + stringWriter, severity);
        }

    }

    public void startReadingClass(String className) {
        if (this.accepts(Severity.INFO)) {
            this.writeMessage("Decompiling class " + className, Severity.INFO);
            ++this.indent;
        }

    }

    public void endReadingClass() {
        if (this.accepts(Severity.INFO)) {
            --this.indent;
            this.writeMessage("... done", Severity.INFO);
        }

    }

    public void startClass(String className) {
        if (this.accepts(Severity.INFO)) {
            this.writeMessage("Processing class " + className, Severity.TRACE);
            ++this.indent;
        }

    }

    public void endClass() {
        if (this.accepts(Severity.INFO)) {
            --this.indent;
            this.writeMessage("... proceeded", Severity.TRACE);
        }

    }

    public void startMethod(String methodName) {
        if (this.accepts(Severity.INFO)) {
            this.writeMessage("Processing method " + methodName, Severity.TRACE);
            ++this.indent;
        }

    }

    public void endMethod() {
        if (this.accepts(Severity.INFO)) {
            --this.indent;
            this.writeMessage("... proceeded", Severity.TRACE);
        }

    }

    public void startWriteClass(String className) {
        if (this.accepts(Severity.INFO)) {
            this.writeMessage("Writing class " + className, Severity.TRACE);
            ++this.indent;
        }

    }

    public void endWriteClass() {
        if (this.accepts(Severity.INFO)) {
            --this.indent;
            this.writeMessage("... written", Severity.TRACE);
        }
    }
}

