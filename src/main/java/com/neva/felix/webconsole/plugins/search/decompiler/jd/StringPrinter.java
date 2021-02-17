package com.neva.felix.webconsole.plugins.search.decompiler.jd;

import org.jd.core.v1.api.printer.Printer;

public class StringPrinter implements Printer {
    protected static final String TAB = "  ";
    protected static final String NEWLINE = "\n";
    protected String format;
    protected int indentationCount = 0;
    protected StringBuilder sb = new StringBuilder();
    private boolean displayLineNumbers;
    private int currentLineNumber = 0;

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public void start(int maxLineNumber, int majorVersion, int minorVersion) {
        this.indentationCount = 0;
        if (maxLineNumber == 0) {
            format = "%4d";
        } else {
            int width = 2;

            while (maxLineNumber >= 10) {
                width++;
                maxLineNumber /= 10;
            }

            format = "%" + width + "d";
        }
    }

    @Override
    public void end() {
    }

    @Override
    public void printText(String text) {
        sb.append(text);
    }

    @Override
    public void printNumericConstant(String constant) {
        sb.append(constant);
    }

    @Override
    public void printStringConstant(String constant, String ownerInternalName) {
        sb.append(constant);
    }

    @Override
    public void printKeyword(String keyword) {
        sb.append(keyword);
    }

    @Override
    public void printDeclaration(int type, String internalTypeName, String name, String descriptor) {
        sb.append(name);
    }

    @Override
    public void printReference(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) {
        sb.append(name);
    }

    @Override
    public void indent() {
        this.indentationCount++;
    }

    @Override
    public void unindent() {
        this.indentationCount--;
    }

    @Override
    public void startLine(int lineNumber) {
        printLineNumber(lineNumber);
        for (int i = 0; i < indentationCount; i++) sb.append(TAB);
    }

    @Override
    public void endLine() {
        sb.append(NEWLINE);
    }

    @Override
    public void extraLine(int count) {
        while (count-- > 0) sb.append(NEWLINE);
    }

    @Override
    public void startMarker(int type) {
    }

    @Override
    public void endMarker(int type) {
    }

    public boolean isDisplayLineNumbers() {
        return displayLineNumbers;
    }

    public void setDisplayLineNumbers(boolean displayLineNumbers) {
        this.displayLineNumbers = displayLineNumbers;
    }

    protected void printLineNumber(int lineNumber) {
        if (isDisplayLineNumbers()) {
            sb.append("/* ");
            currentLineNumber = lineNumber > 0 ? lineNumber : currentLineNumber + 1;
            sb.append(String.format(format, currentLineNumber));
            sb.append(" */ ");
        }
    }
}
