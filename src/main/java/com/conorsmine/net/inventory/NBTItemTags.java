package com.conorsmine.net.inventory;

public enum NBTItemTags {
    ID      ("id"),
    DAMAGE  ("Damage"),
    COUNT   ("Count"),
    SLOT    ("Slot");

    private final String tagName;

    NBTItemTags(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }
}
