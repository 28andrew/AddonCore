package me.andrew28.addons.core;

/**
 * @author Andrew Tran
 */
public class ElementSyntax {
    private Boolean usingBinds;
    private String[] rawSyntaxes;
    private String[] binds;

    public ElementSyntax(Boolean usingBinds, String[] rawSyntaxes) {
        this.usingBinds = usingBinds;
        this.rawSyntaxes = rawSyntaxes;
    }

    public Boolean isUsingBinds() {
        return usingBinds;
    }

    public void setUsingBinds(Boolean usingBinds) {
        this.usingBinds = usingBinds;
    }

    public String[] getRawSyntaxes() {
        return rawSyntaxes;
    }

    public String[] getBinds() {
        return binds;
    }

    public void setBinds(String[] binds) {
        this.binds = binds;
    }
}
