package de.jensknipper.re_director.web.controller;

import java.util.Objects;

public final class RedirectDto {
    private String source;
    private String target;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RedirectDto) obj;
        return Objects.equals(this.source, that.source) &&
                Objects.equals(this.target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

    @Override
    public String toString() {
        return "RedirectDto[" +
                "source=" + source + ", " +
                "target=" + target + ']';
    }

}
