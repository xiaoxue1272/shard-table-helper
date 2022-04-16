package com.tiangou.helper;

public class ShardTableProperties {

    private boolean enable = true;

    private String placeholder = "suffixName";

    private String replaceLogic = ":@";

    public boolean isEnable() {
        return enable;
    }

    public synchronized void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getReplaceLogic() {
        return replaceLogic;
    }

    public void setReplaceLogic(String replaceLogic) {
        this.replaceLogic = replaceLogic;
    }
}
