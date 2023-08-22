package com.conorsmine.net.mojangson.data;

public class NBTPrimitiveData implements IPrimitiveData<Object> {

    private final Object data;

    public NBTPrimitiveData(Object data) {
        this.data = data;
    }

    @Override
    public NBTDataType getType() {
        return NBTDataType.PRIMITIVE;
    }

    @Override
    public Object getData() {
        return data;
    }
}
