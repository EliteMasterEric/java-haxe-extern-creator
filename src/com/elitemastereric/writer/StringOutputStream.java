package com.elitemastereric.writer;

import java.io.IOException;
import java.io.OutputStream;

public class StringOutputStream extends OutputStream {
    private StringBuilder string = new StringBuilder();

    @Override
    public void write(int b) throws IOException {
        this.string.append((char) b );
    }

    //Netbeans IDE automatically overrides this toString()
    public String toString() {
        return this.string.toString();
    }
}
